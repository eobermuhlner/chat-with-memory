package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.config.DataInitializerConfig
import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.entity.ChatMessageEntity
import ch.obermuhlner.chat.entity.LongTermSummaryEntity
import ch.obermuhlner.chat.model.Assistant
import ch.obermuhlner.chat.model.Chat
import ch.obermuhlner.chat.model.ChatDetails
import ch.obermuhlner.chat.model.ChatMessage
import ch.obermuhlner.chat.model.ChatResponse
import ch.obermuhlner.chat.model.MessageType
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.ChatMessageRepository
import ch.obermuhlner.chat.repository.ChatRepository
import ch.obermuhlner.chat.repository.LongTermSummaryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.jvm.optionals.getOrNull

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val assistantRepository: AssistantRepository,
    private val messageRetrievalService: MessageRetrievalService,
    private val aiService: AiService,
    private val longTermSummaryRepository: LongTermSummaryRepository,
) {
    companion object {
        const val NO_ANSWER = "NO_ANSWER"
    }
    val minMessageCount = 10
    val maxMessageCount = 20
    val summaryWordCount = 50

    @Transactional(readOnly = true)
    fun findAll(): List<Chat> = chatRepository.findAll().map { it.toChat() }

    @Transactional(readOnly = true)
    fun findById(id: Long): ChatDetails? = chatRepository.findByIdOrNull(id)?.toChatDetails()

    @Transactional
    fun create(chat: ChatDetails): ChatDetails {
        if (chat.id != 0L) {
            throw IllegalArgumentException("Cannot create chat with id 0")
        }
        val chatEntity = chat.toChatEntity()
        fillAssistants(chatEntity, chat.assistants)

        val savedEntity = chatRepository.save(chatEntity)
        return savedEntity.toChatDetails()
    }

    @Transactional
    fun update(chat: ChatDetails): ChatDetails {
        val existingEntity = chatRepository.findById(chat.id).getOrNull() ?: throw IllegalArgumentException("Chat not found: ${chat.id}")

        chat.toChatEntity(existingEntity)
        fillAssistants(existingEntity, chat.assistants)

        chatRepository.save(existingEntity)
        return existingEntity.toChatDetails()
    }

    private fun fillAssistants(chatEntity: ChatEntity, assistants: MutableList<Assistant>) {
        chatEntity.assistants.clear()
        chatEntity.assistants.addAll(assistantRepository.findAllById(assistants.map { it.id }))
        chatEntity.assistants.forEach { it.chats.add(chatEntity) }
    }

    @Transactional
    fun deleteById(id: Long) {
        chatRepository.deleteById(id)
    }

    @Transactional
    fun sendMessage(id: Long, message: String): ChatResponse {
        val chat = chatRepository.findById(id).getOrNull() ?: throw IllegalArgumentException("Chat not found: $id")

        if (message.startsWith("/")) {
            return executeCommand(chat, message)
        }

        val relevantMessagesText = retrieveRelevantMessagesText(message)

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

    private fun retrieveRelevantMessagesText(message: String): String {
        if (message.isBlank()) {
            return ""
        }
        val relevantMessageIds = messageRetrievalService.retrieveMessageIds(message)
        val relevantMessages = chatMessageRepository.findAllById(relevantMessageIds)
        return relevantMessages.joinToString("\n") { it.toChatString() }
    }

    private fun createContext(chat: ChatEntity, assistant: AssistantEntity, userMessage: ChatMessageEntity, relevantMessagesText: String): String {
        var shortTermMessages = chatMessageRepository.findAllShortTermMemory(chat)

        val shortTermCount = shortTermMessages.count()
        if (shortTermCount > maxMessageCount) {
            val messagesToSummarize = shortTermMessages.subList(0, shortTermCount - minMessageCount)
            shortTermMessages = shortTermMessages.subList(minMessageCount, shortTermMessages.size)

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
            |## Chat
            |${chat.prompt}
            |
            |## Assistant
            |${assistant.prompt}
            |If you have no relevant answer or the answer was already given, respond with $NO_ANSWER.
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
        if (longTermSummaryRepository.findByLevel(level).size > maxMessageCount) {
            summarizeLongTerm(level)
        }
    }

    private fun summarizeLongTerm(level: Int) {
        val levelSummaries = longTermSummaryRepository.findByLevel(level).toMutableList()
        val messagesToSummarize = mutableListOf<LongTermSummaryEntity>()

        while (levelSummaries.size > minMessageCount) {
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
        return "Summarize this information as compact and accurate as possible in less than $summaryWordCount words:\n$messagesText"
    }

    private fun executeCommand(chat: ChatEntity, message: String): ChatResponse {
        val lines = message.lines()
        val command = lines.first().split(" ")

        val result = when (command[0]) {
            "/assistants" -> assistantRepository.findAll().joinToString("\n\n") { it.toChatString() }
            "/messages" -> chatMessageRepository.findAll().joinToString("\n") { it.toChatString() }
            "/count" -> chatMessageRepository.findAll().count().toString()
            "/context" -> {
                val argumentText = lines.subList(1, lines.size).joinToString("\n")
                val relevantMessagesText = retrieveRelevantMessagesText(argumentText)
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

