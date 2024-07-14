package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.LongTermSummary
import ch.obermuhlner.chat.entity.ShortTermMessage
import ch.obermuhlner.chat.entity.SystemMessage
import ch.obermuhlner.chat.repository.LongTermSummaryRepository
import ch.obermuhlner.chat.repository.ShortTermMessageRepository
import ch.obermuhlner.chat.repository.SystemMessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

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
        systemMessageRepository.save(SystemMessage().apply {
            this.text = text
        })
    }

    @Transactional
    fun addUserMessage(text: String) {
        addMessage(Message(MessageType.User, text))
    }

    @Transactional
    fun addAssistantMessage(text: String) {
        addMessage(Message(MessageType.Assistant, text))
    }

    private fun addMessage(message: Message) {
        messageRetrievalService.addMessage(message)
        shortTermMessageRepository.save(ShortTermMessage().apply {
            this.messageType =  message.messageType
            this.text = message.text
            this.timestamp = message.timestamp
        })

        if (shortTermMessageRepository.count() > maxMessageCount) {
            summarize()
        }
    }

    private fun summarize() {
        val shortTermMessages = shortTermMessageRepository.findAll()
        val messagesToSummarize = mutableListOf<ShortTermMessage>()
        while (shortTermMessages.size > minMessageCount) {
            val toDelete = shortTermMessages.removeFirst()
            shortTermMessageRepository.delete(toDelete)
            messagesToSummarize.add(toDelete)
        }

        val messagesToSummarizeText = messagesToSummarize
            .map { "${it.messageType}: \n${it.text}" }
            .joinToString("\n")

        val prompt = "Summarize this information as compact and accurate as possible in less than $summaryWordCount words:\n" +
                messagesToSummarizeText
        val summary = aiService.generate(prompt)

        addSummary(0, summary)
    }

    private fun addSummary(level: Int, summary: String) {
        longTermSummaryRepository.save(LongTermSummary().apply {
            this.level = level
            this.text = summary.take(LongTermSummary.MAX_TEXT_LENGTH)
        })
        val levelSummaries = longTermSummaryRepository.findByLevel(level).toMutableList()

        if (levelSummaries.size > maxMessageCount) {
            val messagesToSummarize = mutableListOf<LongTermSummary>()
            while (levelSummaries.size > minMessageCount) {
                val toSummarize = levelSummaries.removeFirst()
                longTermSummaryRepository.delete(toSummarize)
                messagesToSummarize.add(toSummarize)
            }

            val textToSummarize = messagesToSummarize.joinToString("\n")
            val prompt = "Summarize this information as compact and accurate as possible in less than $summaryWordCount words:\n" +
                    textToSummarize
            val nextSummary = aiService.generate(prompt)

            addSummary(level + 1, nextSummary)
        }
    }

    fun getContext(prompt: String): String {
        val systemMessage = systemMessageRepository.findFirstByOrderByIdDesc()!!

        val shortTermMessages = shortTermMessageRepository.findAll()
        val shortTermMessagesTexts = shortTermMessages.map { it.text }.toSet()

        val relevantMessages = messageRetrievalService.retrieveMessages(prompt)
            .filter { !shortTermMessagesTexts.contains(it.text) }

        val relevantMessageText = relevantMessages
            .map { it.toChatString() }
            .joinToString("\n")

        var longTermText = ""
        var level = 0
        do {
            val levelSummaries = longTermSummaryRepository.findByLevel(level)
            longTermText += levelSummaries.map { it.text }.joinToString("\n")
            level++
        } while (levelSummaries.isNotEmpty())

        val shortTermText = shortTermMessages
            .map { "${it.messageType}: \n${it.text}" }
            .joinToString("\n")

        val context = """
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
        return context
    }
}

data class Message(
    val messageType: MessageType,
    val text: String,
    val timestamp: Instant = Instant.now(),
) {
    fun toChatString(): String {
        return "$messageType ($timestamp):\n$text"
    }
}

enum class MessageType {
    User,
    Assistant
}
