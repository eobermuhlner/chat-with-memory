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
    private val documentRetrievalService: DocumentRetrievalService,
) {

    @Transactional(readOnly = true)
    fun findAllMessages(chatId: Long): List<ChatMessage> =
        chatMessageRepository.findAllByChatId(chatId).map { it.toChatMessage() }

    @Transactional(readOnly = true)
    fun findById(chatId: Long, messageId: Long): ChatMessage? {
        val chatMessageEntity = chatMessageRepository.findById(messageId).orElse(null) ?: return null
        return if (chatMessageEntity.chat?.id == chatId) chatMessageEntity.toChatMessage() else null
    }

    @Transactional
    fun transferShortTermMessagesToLongTerm(chatId: Long) {
        val chat = chatRepository.findById(chatId).getOrNull() ?: throw IllegalArgumentException("Chat not found: $chatId")
        val shortTermMessages = chatMessageRepository.findAllShortTermMemory(chat)
        shortTermMessages.forEach { it.shortTermMemory = false }
        chatMessageRepository.saveAll(shortTermMessages)
        summarize(chat, shortTermMessages)
    }

    @Transactional
    fun deleteMessage(chatId: Long, messageId: Long) {
        val chatMessageEntity = chatMessageRepository.findById(messageId).orElse(null)
        if (chatMessageEntity?.chat?.id != chatId) {
            throw IllegalArgumentException("Message $messageId not found in chat: $chatId")
        }
        chatMessageRepository.deleteById(messageId)
    }

    @Transactional
    fun deleteAllMessages(chatId: Long, transferToLongTermMemory: Boolean) {
        val chat = chatRepository.findById(chatId).getOrNull() ?: throw IllegalArgumentException("Chat not found: $chatId")
        if (transferToLongTermMemory) {
            val shortTermMessages = chatMessageRepository.findAllShortTermMemory(chat)
            summarize(chat, shortTermMessages)
        }
        chatMessageRepository.deleteAllByChatId(chatId)
    }

    @Transactional
    fun deleteLongTermMessages(chatId: Long) {
        val chat = chatRepository.findById(chatId).getOrNull() ?: throw IllegalArgumentException("Chat not found: $chatId")
        longTermSummaryRepository.deleteAllByChat(chat)
    }

    @Transactional
    fun sendMessage(chatId: Long, message: String): ChatResponse {
        val chat = chatRepository.findById(chatId).getOrNull() ?: throw IllegalArgumentException("Chat not found: $chatId")

        if (message.startsWith("/")) return executeCommand(chat, message)

        val relevantMessagesText = retrieveRelevantMessagesText(chat, message)
        val userMessage = ChatMessageEntity().apply {
            this.chat = chat
            this.messageType = MessageType.User
            this.text = message
        }
        val savedUserMessageEntity = chatMessageRepository.save(userMessage)
        messageRetrievalService.addMessage(savedUserMessageEntity)

        val assistantMessages = chat.assistants.mapNotNull { assistant ->
            val relevantDocumentSegmentsText = retrieveRelevantDocumentSegmentsText(chat, assistant, message)

            val context = createContext(chat, assistant, userMessage, relevantMessagesText, relevantDocumentSegmentsText)
            val tools = chat.tools.toMutableSet()
            tools.addAll(assistant.tools)
            val answer = aiService.generateWithTools(context, tools)
            if (answer.isNotBlank() && !answer.startsWith(NO_ANSWER)) {
                val assistantMessage = ChatMessageEntity().apply {
                    this.chat = chat
                    this.messageType = MessageType.Assistant
                    this.sender = assistant
                    this.text = answer
                }
                val savedAssistantMessageEntity = chatMessageRepository.save(assistantMessage)
                messageRetrievalService.addMessage(savedAssistantMessageEntity)
                assistantMessage.toChatMessage()
            } else null
        }

        return ChatResponse(assistantMessages)
    }

    private fun retrieveRelevantDocumentSegmentsText(chat: ChatEntity, assistant: AssistantEntity, message: String): String {
        val documentIds = assistant.documents.mapNotNull { it.id }.toSet()
        val segments = documentRetrievalService.retrieveRelevantTextSegments(message, documentIds, 3, 0.5)

        return segments.joinToString("\n") { it.text() }
    }

    private fun retrieveRelevantMessagesText(chat: ChatEntity, message: String): String {
        val chatId = chat.id
        if (message.isBlank() || chatId == null) return ""
        val relevantMessageIds = messageRetrievalService.retrieveMessageIds(message, properties.relevantMessagesMaxResult)
        val relevantMessages = chatMessageRepository.findAllByChatIdAndIdIn(chatId, relevantMessageIds)
        val longTermMessages = relevantMessages.filter { !it.shortTermMemory }
        return longTermMessages.joinToString("\n") { it.toChatString() }
    }

    private fun createContext(chat: ChatEntity, assistant: AssistantEntity, userMessage: ChatMessageEntity, relevantMessagesText: String, relevantDocumentSegmentsText: String): String {

        var shortTermMessages = chatMessageRepository.findAllShortTermMemory(chat)
        val shortTermCount = shortTermMessages.count()
        if (shortTermCount > properties.maxMessageCount) {
            val messagesToSummarize = shortTermMessages.subList(0, shortTermCount - properties.minMessageCount)
            shortTermMessages = shortTermMessages.subList(properties.minMessageCount, shortTermMessages.size)
            messagesToSummarize.forEach { it.shortTermMemory = false }
            chatMessageRepository.saveAll(messagesToSummarize)
            summarize(chat, messagesToSummarize)
        }

        val longTermText = buildLongTermText(chat)
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
            |## Relevant documents
            |$relevantDocumentSegmentsText
            |
            |## Memory
            |$longTermText
            |
            |## Last Messages
            |$shortTermText
            |
            |${userMessage.toShortChatString()}
        """.trimMargin()
    }

    private fun buildLongTermText(chat: ChatEntity): String =
        StringBuilder().apply {
            var level = 0
            do {
                val levelSummaries = longTermSummaryRepository.findByChatAndLevel(chat, level)
                if (levelSummaries.isNotEmpty()) {
                    append(levelSummaries.joinToString("\n") { it.text })
                }
                level++
            } while (levelSummaries.isNotEmpty())
        }.toString()

    private fun summarize(chatEntity: ChatEntity, messagesToSummarize: List<ChatMessageEntity>) {
        val prompt = createSummaryPrompt(messagesToSummarize.joinToString("\n") { it.toChatString() })
        val summary = aiService.generate(prompt)
        addSummary(chatEntity, 0, summary)
    }

    private fun addSummary(chatEntity: ChatEntity, level: Int, summary: String) {
        longTermSummaryRepository.save(LongTermSummaryEntity().apply {
            this.level = level
            this.chat = chatEntity
            this.text = summary.take(LongTermSummaryEntity.MAX_TEXT_LENGTH)
        })
        if (longTermSummaryRepository.findByChatAndLevel(chatEntity, level).size > properties.maxMessageCount) {
            summarizeLongTerm(chatEntity, level)
        }
    }

    private fun summarizeLongTerm(chatEntity: ChatEntity, level: Int) {
        val levelSummaries = longTermSummaryRepository.findByChatAndLevel(chatEntity, level).toMutableList()
        val messagesToSummarize = mutableListOf<LongTermSummaryEntity>()
        while (levelSummaries.size > properties.minMessageCount) {
            messagesToSummarize.add(longTermSummaryRepository.deleteAndGet(levelSummaries))
        }
        val summaryText = aiService.generate(createSummaryPrompt(messagesToSummarize.joinToString("\n") { it.text }))
        addSummary(chatEntity, level + 1, summaryText)
    }

    private fun LongTermSummaryRepository.deleteAndGet(messages: MutableList<LongTermSummaryEntity>): LongTermSummaryEntity {
        val toDelete = messages.removeFirst()
        this.delete(toDelete)
        return toDelete
    }

    private fun createSummaryPrompt(messagesText: String): String = """
            |Summarize this information in bulletpoints as compact and accurate as possible using less than ${properties.summaryWordCount} words:
            |$messagesText
        """.trimMargin()

    private fun executeCommand(chat: ChatEntity, message: String): ChatResponse {
        val lines = message.lines()
        val command = lines.first().split(" ")

        val result = when (command[0]) {
            "/assistants" -> assistantRepository.findAll().joinToString("\n\n") { it.toChatString() }
            "/messages" -> chatMessageRepository.findAllByChatId(chat.id!!).joinToString("\n\n") { it.toChatString() }
            "/count" -> chatMessageRepository.findAll().count().toString()
            "/context" -> {
                val argumentText = lines.subList(1, lines.size).joinToString("\n")
                val assistant = chat.assistants.firstOrNull() ?: AssistantEntity()
                val relevantMessagesText = retrieveRelevantMessagesText(chat, argumentText)
                val relevantDocumentSegmentsText = retrieveRelevantDocumentSegmentsText(chat, assistant, message)
                val userMessage = ChatMessageEntity().apply {
                    this.chat = chat
                    this.messageType = MessageType.User
                    this.text = argumentText
                }
                createContext(chat, assistant, userMessage, relevantMessagesText, relevantDocumentSegmentsText)
            }
            "/help" -> """
                Commands:
                - /assistants
                - /messages
                - /count
                - /context
                - /help
            """.trimIndent()
            else -> "Unknown command: ${command[0]}"
        }

        return ChatResponse(
            listOf(
                ChatMessage(
                    id = -1,
                    type = MessageType.System,
                    sender = "System",
                    text = result,
                    timestamp = Instant.now()
                )
            )
        )
    }
}
