package ch.obermuhlner.chat

class MessageService(
    val aiService: AiService,
    val maxMessageCount: Int = 10,
    val minMessageCount: Int = 5,
    val summaryWordCount: Int = 50
) {
    //val messages = mutableListOf<Message>()

    private var systemMessage: String = ""
    private val longTermSummaries = mutableListOf<String>()
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

        val messagesToSummarizeText = shortTermMessages
            .map{ "${it.messageType}: \n${it.text}" }
            .joinToString("\n")

        val prompt = "Summarize the following compact and concise in less $summaryWordCount words:\n" +
                messagesToSummarizeText
        val summary = aiService.generate(prompt)

        longTermSummaries.add(summary)
    }

    fun getContext() : String {
        val longTermText = longTermSummaries.joinToString("\n")
        val shortTermText = shortTermMessages
            .map{ "${it.messageType}: \n${it.text}" }
            .joinToString("\n")

        return "$systemMessage\n$longTermText\n$shortTermText\nAssistant:"
    }
}

data class Message(val messageType: MessageType, val text: String)

enum class MessageType {
    User,
    Assistant
}
