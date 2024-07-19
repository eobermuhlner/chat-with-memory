package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.entity.ChatMessageEntity
import ch.obermuhlner.chat.entity.LongTermSummaryEntity
import ch.obermuhlner.chat.model.ChatMessage
import ch.obermuhlner.chat.model.ChatResponse
import ch.obermuhlner.chat.model.MessageType
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.ChatMessageRepository
import ch.obermuhlner.chat.repository.ChatRepository
import ch.obermuhlner.chat.repository.LongTermSummaryRepository
import ch.obermuhlner.chat.service.ChatService.Companion.NO_ANSWER
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.jvm.optionals.getOrNull

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRepository: ChatRepository,
    private val assistantRepository: AssistantRepository,
    private val messageRetrievalService: MessageRetrievalService,
    private val aiService: AiService,
    private val longTermSummaryRepository: LongTermSummaryRepository,
    private val properties: ChatMessageServiceProperties,
) {

    @Transactional(readOnly = true)
    fun findAllMessages(chatId: Long): List<ChatMessage> {
        return chatMessageRepository.findAllByChatId(chatId)
            .map { it.toChatMessage() }
    }

    @Transactional(readOnly = true)
    fun findById(chatId: Long, messageId: Long): ChatMessage? {
        val chatMessageEntity = chatMessageRepository.findById(messageId).orElse(null) ?: return null
        if (chatMessageEntity.chat?.id != chatId) {
            return null
        }

        return chatMessageEntity.toChatMessage()
    }

    @Transactional
    fun sendMessage(id: Long, message: String): ChatResponse {
        val chat = chatRepository.findById(id).getOrNull() ?: throw IllegalArgumentException("Chat not found: $id")

        if (message.startsWith("/")) {
            return executeCommand(chat, message)
        }

        val relevantMessagesText = retrieveRelevantMessagesText(chat, message)

        val userMessage = ChatMessageEntity().apply {
            this.chat = chat
            this.messageType = MessageType.User
            this.text = message
        }
        val savedUserMessageEntity = chatMessageRepository.save(userMessage)
        messageRetrievalService.addMessage(savedUserMessageEntity)

        val assistantMessages = mutableListOf<ChatMessage>()

        for (assistant in chat.assistants) {
            val context = createContext(chat, assistant, userMessage, relevantMessagesText)
            val answer = aiService.generate(context)
            if (answer.isNotBlank() && !answer.startsWith(NO_ANSWER)) {
                val assistantMessage = ChatMessageEntity().apply {
                    this.chat = chat
                    this.messageType = MessageType.Assistant
                    this.sender = assistant
                    this.text = answer
                }
                val savedAssistantMessageEntity = chatMessageRepository.save(assistantMessage)
                messageRetrievalService.addMessage(savedAssistantMessageEntity)
                assistantMessages.add(assistantMessage.toChatMessage())
            }
        }

        return ChatResponse(assistantMessages)
    }

    private fun retrieveRelevantMessagesText(chat: ChatEntity, message: String): String {
        if (message.isBlank()) {
            return ""
        }
        val relevantMessageIds = messageRetrievalService.retrieveMessageIds(message)
        val relevantMessages = chatMessageRepository.findAllByChatIdAndIdIn(chat.id, relevantMessageIds)
        return relevantMessages.joinToString("\n") { it.toChatString() }
    }

    private fun createContext(chat: ChatEntity, assistant: AssistantEntity, userMessage: ChatMessageEntity, relevantMessagesText: String): String {
        var shortTermMessages = chatMessageRepository.findAllShortTermMemory(chat)

        val shortTermCount = shortTermMessages.count()
        if (shortTermCount > properties.maxMessageCount) {
            val messagesToSummarize = shortTermMessages.subList(0, shortTermCount - properties.minMessageCount)
            shortTermMessages = shortTermMessages.subList(properties.minMessageCount, shortTermMessages.size)

            messagesToSummarize.forEach { message ->
                message.shortTermMemory = false
            }
            chatMessageRepository.saveAll(messagesToSummarize)
            summarize(messagesToSummarize)
        }

        val longTermText = buildLongTermText()
        val shortTermText = shortTermMessages.joinToString("\n") { it.toChatString() }

        val instantNow = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val localDateTimeNow = LocalDateTime.ofInstant(instantNow, ZoneId.systemDefault())
        return """
            |Current time (UTC): $instantNow
            |Current local time: $localDateTimeNow ${localDateTimeNow.dayOfWeek}
            |
            |## Chat: ${chat.title}
            |${chat.prompt}
            |
            |## Assistant
            |${assistant.prompt}
            |
            |## Relevant messages
            |$relevantMessagesText
            |
            |## Memory
            |$longTermText
            |
            |## Last Messages
            |$shortTermText
            |${userMessage.toShortChatString()}
        """.trimMargin()
    }

    private fun buildLongTermText(): String {
        val longTermText = StringBuilder()
        var level = 0
        do {
            val levelSummaries = longTermSummaryRepository.findByLevel(level)
            if (levelSummaries.isNotEmpty()) {
                longTermText.append(levelSummaries.joinToString("\n") { it.text })
            }
            level++
        } while (levelSummaries.isNotEmpty())
        return longTermText.toString()
    }

    private fun summarize(messagesToSummarize: List<ChatMessageEntity>) {
        val prompt = createShortTermSummaryPrompt(messagesToSummarize)
        val summary = aiService.generate(prompt)
        addSummary(0, summary)
    }

    private fun addSummary(level: Int, summary: String) {
        longTermSummaryRepository.save(LongTermSummaryEntity().apply {
            this.level = level
            this.text = summary.take(LongTermSummaryEntity.MAX_TEXT_LENGTH)
        })
        if (longTermSummaryRepository.findByLevel(level).size > properties.maxMessageCount) {
            summarizeLongTerm(level)
        }
    }

    private fun summarizeLongTerm(level: Int) {
        val levelSummaries = longTermSummaryRepository.findByLevel(level).toMutableList()
        val messagesToSummarize = mutableListOf<LongTermSummaryEntity>()

        while (levelSummaries.size > properties.minMessageCount) {
            messagesToSummarize.add(longTermSummaryRepository.deleteAndGet(levelSummaries))
        }

        val summaryText = aiService.generate(createLongTermSummaryPrompt(messagesToSummarize))
        addSummary(level + 1, summaryText)
    }

    private fun LongTermSummaryRepository.deleteAndGet(messages: MutableList<LongTermSummaryEntity>): LongTermSummaryEntity {
        val toDelete = messages.removeFirst()
        this.delete(toDelete)
        return toDelete
    }

    private fun createShortTermSummaryPrompt(messages: List<ChatMessageEntity>): String {
        val messagesText = messages.joinToString("\n") { it.toChatString() }
        return createSummaryPrompt(messagesText)
    }

    private fun createLongTermSummaryPrompt(messages: List<LongTermSummaryEntity>): String {
        val messagesText = messages.joinToString("\n") { it.text }
        return createSummaryPrompt(messagesText)
    }

    private fun createSummaryPrompt(messagesText: String): String {
        return "Summarize this information as compact and accurate as possible in less than ${properties.summaryWordCount} words:\n$messagesText"
    }

    private fun executeCommand(chat: ChatEntity, message: String): ChatResponse {
        val lines = message.lines()
        val command = lines.first().split(" ")

        val result = when (command[0]) {
            "/assistants" -> assistantRepository.findAll().joinToString("\n\n") { it.toChatString() }
            "/messages" -> chatMessageRepository.findAllByChatId(chat.id).joinToString("\n\n") { it.toChatString() }
            "/count" -> chatMessageRepository.findAll().count().toString()
            "/context" -> {
                val argumentText = lines.subList(1, lines.size).joinToString("\n")
                val relevantMessagesText = retrieveRelevantMessagesText(chat, argumentText)
                val userMessage = ChatMessageEntity().apply {
                    this.chat = chat
                    this.messageType = MessageType.User
                    this.text = argumentText
                }
                createContext(chat, chat.assistants.first(), userMessage, relevantMessagesText)
            }
            "/help" -> {
                """
                    Commands:
                    - /assistants
                    - /messages
                    - /count
                    - /context
                    - /help
                """.trimIndent()
            }
            else -> "Unknown command: $command[0]"
        }

        return ChatResponse(listOf(
            ChatMessage(
                id = -1,
                type = MessageType.System,
                sender = "System",
                text = result,
                timestamp = Instant.now()
            )))
    }
}