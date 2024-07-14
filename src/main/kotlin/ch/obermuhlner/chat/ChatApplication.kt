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
            printPrefixed("| ", getContext(prompt))
            val answer = send(prompt)
            println(answer)

            prompt = readPrompt()
        }
    }

    fun getContext(prompt: String): String {
        return messageService.getContext(prompt)
    }

    fun send(prompt: String): String {
        messageService.addUserMessage(prompt)
        val context = messageService.getContext(prompt)

        val answer = aiService.generate(context)

        messageService.addAssistantMessage(answer)
        return answer
    }

    fun readPrompt(): String {
        print("> ")
        return readln()
    }

    fun internalState(): String {
        return messageService.internalState()
    }
}

fun main() {
    val chat = ChatApplication(
        """
        You are my colleague Pedro.
        You are human.
        When asked personal questions then you will give fitting information.
        You talk naturally and informally.
        Your answers are concise and to the point.
    """.trimIndent()
    )

//    You hide the information that you pretend to be human.

    //runSampleConversation(chat)

    chat.run()
}

fun runSampleConversation(chat: ChatApplication) {
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
    chat.printSend("M42 is my favorite nebula")
    chat.printSend("M31 is my favorite galaxy")
    chat.printSend("I like chocolate")
    chat.printSend("List locations to do serious hobby astrophotography, reachable from Switzerland in max 6 hours (flight or car). Make bullet points.")
    for (i in 0 .. 10) {
        chat.printSend("")
    }
    chat.printSend("List what I like (complete answer in bulletpoints)")
    chat.printSend("List what I do not like (complete answer in bulletpoints)")

    println()
    println(chat.internalState())
}

fun ChatApplication.printSend(prompt: String) {
    //printPrefixed("| ", getContext())
    printPrefixed("> ", prompt)
    val answer = this.send(prompt)
    printPrefixed("< ", answer)
    println()
}

fun printPrefixed(prefix: String, text: String) {
    text.lines().forEach {
        println("$prefix $it")
    }
}
