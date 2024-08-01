package ch.obermuhlner.chat.model

data class Chat(
    var id: Long?,
    var title: String,
    var prompt: String,
    val assistants: MutableList<Assistant>,
    val tools: List<String> = mutableListOf(),
    val documents: List<Document> = mutableListOf(),
)