package ch.obermuhlner.chat.model

data class Chat(
    var id: Long?,
    var title: String,
    var prompt: String,
    var isTemplate: Boolean,
    val assistants: MutableList<Assistant> = mutableListOf(),
    val tools: List<String> = mutableListOf(),
    val documents: List<Document> = mutableListOf(),
)