package ch.obermuhlner.chat.model

data class Assistant(
    var id: Long,
    var name: String,
    var description: String,
    var prompt: String,
    var sortIndex: Int,
    var tools: List<String> = mutableListOf(),
)