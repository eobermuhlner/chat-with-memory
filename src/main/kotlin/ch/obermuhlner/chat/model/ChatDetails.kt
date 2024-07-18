package ch.obermuhlner.chat.model

data class ChatDetails(
    var id: Long,
    var title: String,
    val assistants: MutableList<Assistant>
)