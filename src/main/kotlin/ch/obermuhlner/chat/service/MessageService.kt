package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.model.MessageType
import ch.obermuhlner.chat.entity.LongTermSummaryEntity
import ch.obermuhlner.chat.entity.ShortTermMessageEntity
import ch.obermuhlner.chat.entity.SystemMessageEntity
import ch.obermuhlner.chat.repository.LongTermSummaryRepository
import ch.obermuhlner.chat.repository.ShortTermMessageRepository
import ch.obermuhlner.chat.repository.SystemMessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class MessageService(
    private val aiService: AiService,
    private val properties: MessageServiceProperties,
    private val messageRetrievalService: MessageRetrievalService,
    private val systemMessageRepository: SystemMessageRepository,
    private val longTermSummaryRepository: LongTermSummaryRepository,
    private val shortTermMessageRepository: ShortTermMessageRepository
) {
    private val maxMessageCount: Int = properties.maxMessageCount
    private val minMessageCount: Int = properties.minMessageCount
    private val summaryWordCount: Int = properties.summaryWordCount

    @Transactional
    fun setSystemMessage(text: String) {
        systemMessageRepository.deleteAll()
        systemMessageRepository.save(SystemMessageEntity().apply { this.text = text })
    }

    @Transactional
    fun addUserMessage(text: String) = addMessage(Message(MessageType.User, text))

    @Transactional
    fun addAssistantMessage(text: String) = addMessage(Message(MessageType.Assistant, text))

    private fun addMessage(message: Message): Message {
        messageRetrievalService.addMessage(message)
        shortTermMessageRepository.save(message.toShortTermMessage())

        if (shortTermMessageRepository.count() > maxMessageCount) {
            summarize()
        }

        return message
    }

    private fun summarize() {
        val shortTermMessages = shortTermMessageRepository.findAll().toMutableList()
        val messagesToSummarize = mutableListOf<ShortTermMessageEntity>()

        while (shortTermMessages.size > minMessageCount) {
            messagesToSummarize.add(shortTermMessageRepository.deleteAndGet(shortTermMessages))
        }

        val summaryText = aiService.generate(createShortTermSummaryPrompt(messagesToSummarize))
        addSummary(0, summaryText)
    }

    private fun createShortTermSummaryPrompt(messages: List<ShortTermMessageEntity>): String {
        val messagesText = messages.joinToString("\n") { "${it.messageType} (${it.timestamp.truncatedTo(ChronoUnit.SECONDS)}): \n${it.text}" }
        return "Summarize this information as compact and accurate as possible in less than $summaryWordCount words:\n$messagesText"
    }

    private fun createLongTermSummaryPrompt(messages: List<LongTermSummaryEntity>): String {
        val messagesText = messages.joinToString("\n") { it.text }
        return "Summarize this information as compact and accurate as possible in less than $summaryWordCount words:\n$messagesText"
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

    fun getContext(prompt: String): String {
        val systemMessage = systemMessageRepository.findFirstByOrderByIdDesc()!!
        val shortTermMessages = shortTermMessageRepository.findAll()
        val relevantMessages = messageRetrievalService.retrieveMessages(prompt)
            .filterNot { it.text in shortTermMessages.map { msg -> msg.text } }

        val context = buildContext(systemMessage, shortTermMessages, relevantMessages)
        return context
    }

    private fun buildContext(
        systemMessage: SystemMessageEntity,
        shortTermMessages: List<ShortTermMessageEntity>,
        relevantMessages: List<Message>
    ): String {
        val relevantMessageText = relevantMessages.joinToString("\n") { it.toChatString() }
        val shortTermText = shortTermMessages.joinToString("\n") { "${it.messageType} (${it.timestamp.truncatedTo(ChronoUnit.SECONDS)}): \n${it.text}" }
        val longTermText = buildLongTermText()

        return """
Current time (UTC): ${Instant.now()}
Current local time: ${LocalDateTime.now()} ${LocalDate.now().dayOfWeek}

${systemMessage.text}

# Old relevant messages
$relevantMessageText

# Memory
$longTermText

$shortTermText

Assistant:
        """.trimIndent()
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

    private fun Message.toShortTermMessage() = ShortTermMessageEntity().apply {
        this.messageType = this@toShortTermMessage.messageType
        this.text = this@toShortTermMessage.text
        this.timestamp = this@toShortTermMessage.timestamp
    }
}

data class Message(
    val messageType: MessageType,
    val text: String,
    val timestamp: Instant = Instant.now()
) {
    fun toChatString() = "$messageType (${timestamp.truncatedTo(ChronoUnit.SECONDS)}):\n$text"
}
