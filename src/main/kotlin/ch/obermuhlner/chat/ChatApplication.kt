package ch.obermuhlner.chat

class ChatApplication(systemMessage: String) {
    private val apiKey = System.getenv("OPENAI_API_KEY") ?: "demo"

    private val aiService = AiService(apiKey)
    private val messageService = MessageService(aiService)

    init {
        messageService.setSystemMessage(systemMessage)
    }
    fun run() {
        var prompt = readPrompt()
        while (prompt != "") {
            val answer = send(prompt)
            println(answer)

            prompt = readPrompt()
        }
    }

    fun getContext(): String {
        return messageService.getContext()
    }

    fun send(prompt: String): String {
        messageService.addUserMessage(prompt)
        val context = messageService.getContext()

        val answer = aiService.generate(context)

        messageService.addAssistantMessage(answer)
        return answer
    }

    fun readPrompt(): String {
        print("> ")
        return readln()
    }
}

fun main() {
    val chat = ChatApplication("""
        You are my colleague and personal assistant Pedro.
        You pretend to be human.
        You talk naturally and informally, your answers are concise and to the point.
    """.trimIndent())

    chat.printSend("Who are you?")
    chat.printSend("My name is Eric")
    chat.printSend("I like blue")
    chat.printSend("I do not like onions")
    for (i in 0 .. 10) {
        chat.printSend("")
    }
    chat.printSend("I am allergic to Safran")
    chat.printSend("I do not like green")
    chat.printSend("My hobbies are astrophotography, diving and programming")
    chat.printSend("I recently started collecting fossils, I might take it up as a hobby")
    chat.printSend("Celery is terrible")
    for (i in 0 .. 10) {
        chat.printSend("")
    }
    chat.printSend("List what I like (complete answer in bulletpoints)")
    chat.printSend("List what I do not like (complete answer in bulletpoints)")

    //chat.run()
}

fun ChatApplication.printSend(prompt: String) {
    println(getContext())
    println("> $prompt")
    val answer = this.send(prompt)
    println("< $answer")
    println()
}
