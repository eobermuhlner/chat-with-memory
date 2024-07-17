package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.config.DataInitializerConfig
import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.entity.ChatMessageEntity
import ch.obermuhlner.chat.entity.LongTermSummaryEntity
import ch.obermuhlner.chat.entity.ShortTermMessageEntity
import ch.obermuhlner.chat.model.ChatMessage
import ch.obermuhlner.chat.model.ChatResponse
import ch.obermuhlner.chat.model.MessageType
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.ChatMessageRepository
import ch.obermuhlner.chat.repository.ChatRepository
import ch.obermuhlner.chat.repository.LongTermSummaryRepository
import ch.obermuhlner.chat.repository.ShortTermMessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val assistantRepository: AssistantRepository,
    private val aiService: AiService,
    private val longTermSummaryRepository: LongTermSummaryRepository,
) {

    val NO_ANSWER = "NO_ANSWER"
    val minMessageCount = 10
    val maxMessageCount = 20
    val summaryWordCount = 50

    @Transactional
    fun sendMessage(message: String): ChatResponse {
        val chat = chatRepository.findByTitle(DataInitializerConfig.INITIAL_CHAT_TITLE)!!
        if (message.startsWith("/")) {
            return executeCommand(chat, message)
        }

        val userMessage = ChatMessageEntity().apply {
            this.chat = chat
            this.messageType = MessageType.User
            this.text = message
        }
        chatMessageRepository.save(userMessage)

        val assistantMessages = mutableListOf<ChatMessage>()

        for (assistant in chat.assistants) {
            val context = createContext(chat, assistant, userMessage)
            val answer = aiService.generate(context)
            if (answer.isNotBlank() && !answer.startsWith(NO_ANSWER)) {
                val assistantMessage = ChatMessageEntity().apply {
                    this.chat = chat
                    this.messageType = MessageType.Assistant
                    this.sender = assistant
                    this.text = answer
                }
                chatMessageRepository.save(assistantMessage)
                assistantMessages.add(assistantMessage.toChatMessage())
            }
        }

        return ChatResponse(assistantMessages)
    }

    private fun createContext(chat: ChatEntity, assistant: AssistantEntity, userMessage: ChatMessageEntity): String {
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

        val instantNow = Instant.now()
        val localDateTimeNow = LocalDateTime.ofInstant(instantNow, ZoneId.systemDefault())
        return """
            |Current time (UTC): $instantNow
            |Current local time: ${localDateTimeNow.truncatedTo(ChronoUnit.SECONDS)} ${localDateTimeNow.dayOfWeek}
            |
            |${assistant.prompt}
            |If you have no relevant answer or the answer was already given, respond with $NO_ANSWER.
            |
            |# Memory
            |$longTermText
            |
            |# Last Messages
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

    private fun ShortTermMessageRepository.deleteAndGet(messages: MutableList<ShortTermMessageEntity>): ShortTermMessageEntity {
        val toDelete = messages.removeFirst()
        this.delete(toDelete)
        return toDelete
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
                val userMessage = ChatMessageEntity().apply {
                    this.chat = chat
                    this.messageType = MessageType.User
                    this.text = lines.subList(1, lines.size).joinToString("\n")
                }
                createContext(chat, chat.assistants.first(), userMessage)
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
                id = 0,
                messageType = MessageType.System,
                sender = "System",
                text = result,
                timestamp = Instant.now()
            )))
    }

    fun AssistantEntity.toChatString(): String {
        return """
            |# ${name} - ${description} 
            |${prompt}
        """.trimMargin()
    }

    fun ChatMessageEntity.toChatString(): String {
        return """
            |${sender?.name ?: "User"} (${timestamp.truncatedTo(ChronoUnit.SECONDS)}):
            |${text}
        """.trimMargin()
    }

    fun ChatMessageEntity.toShortChatString(): String {
        return """
            |${this.sender?.name ?: "User"}:
            |${this.text}
        """.trimMargin()
    }

    fun ChatMessageEntity.toChatMessage(): ChatMessage {
        return ChatMessage(
            id = this.id,
            messageType = this.messageType,
            sender = this.sender?.name,
            text = this.text,
            timestamp = this.timestamp
        )
    }
}

