package ch.obermuhlner.chat

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class MessageService(
    val aiService: AiService,
    val maxMessageCount: Int = 10,
    val minMessageCount: Int = 5,
    val summaryWordCount: Int = 50
) {
    private val messageRetrievalService = MessageRetrievalService()

    private var systemMessage: String = ""
    private val longTermSummaries: MutableMap<Int, MutableList<String>> = mutableMapOf()
    private val shortTermMessages = mutableListOf<Message>()

    fun setSystemMessage(text: String) {
        systemMessage = text
    }

    fun addUserMessage(text: String) {
        addMessage(Message(MessageType.User, text))
    }

    fun addAssistantMessage(text: String) {
        addMessage(Message(MessageType.Assistant, text))
    }

    private fun addMessage(message: Message) {
        messageRetrievalService.addMessage(message)
        shortTermMessages.add(message)

        if (shortTermMessages.size > maxMessageCount) {
            summarize()
        }
    }

    private fun summarize() {
        val messagesToSummarize = mutableListOf<Message>()
        while (shortTermMessages.size > minMessageCount) {
            messagesToSummarize.add(shortTermMessages.removeFirst())
        }

        val messagesToSummarizeText = messagesToSummarize
            .map{ "${it.messageType} (${it.timestamp}): \n${it.text}" }
            .joinToString("\n")

        val prompt = "Summarize this information as compact and accurate as possible in less than $summaryWordCount words:\n" +
                messagesToSummarizeText
        val summary = aiService.generate(prompt)

        addSummary(0, summary)
    }

    private fun addSummary(level: Int, summary: String) {
        val levelSummaries = longTermSummaries.computeIfAbsent(level) { mutableListOf() }
        levelSummaries.add(summary)

        if (levelSummaries.size > maxMessageCount) {
            val messagesToSummarize = mutableListOf<String>()
            while (levelSummaries.size > minMessageCount) {
                messagesToSummarize.add(levelSummaries.removeFirst())
            }

            val textToSummarize = messagesToSummarize.joinToString("\n")
            val prompt = "Summarize this information as compact and accurate as possible in less than $summaryWordCount words:\n" +
                    textToSummarize
            val nextSummary = aiService.generate(prompt)
            addSummary(level + 1, nextSummary)
        }
    }

    fun getContext(prompt: String) : String {
        val relevantMessages = messageRetrievalService.retrieveMessages(prompt).toMutableList()
        relevantMessages.removeAll(shortTermMessages)
        val relevantMessageText = relevantMessages
            .map{ it.toChatString() }
            .joinToString("\n")

        var longTermText = ""
        for (k in longTermSummaries.keys.sorted()) {
            longTermText += longTermSummaries[k]!!.joinToString("\n")
        }
        val shortTermText = shortTermMessages
            .map{ it.toChatString() }
            .joinToString("\n")

        val context =  """
Current time (UTC): ${Instant.now()}
Current local time: ${LocalDateTime.now()} ${LocalDate.now().dayOfWeek}

$systemMessage

# Old relevant messages
$relevantMessageText

# Memory
$longTermText

$shortTermText
Assistant:
        """.trimIndent()
        return context
    }

    fun internalState(): String {
        val result = StringBuilder()

        result.append("# Short Term (${shortTermMessages.size} entries)\n")
        shortTermMessages.forEach {
            result.append("${it.messageType}: ${it.text}\n")
        }
        result.append("\n")

        result.append("# Long Term\n")
        for (k in longTermSummaries.keys.sorted()) {
            val levelSummaries = longTermSummaries[k]!!
            result.append("## Level $k (${levelSummaries.size} entries)\n")
            levelSummaries.forEach {
                result.append("$it\n")
            }
            result.append("\n")
        }

        return result.toString()
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
