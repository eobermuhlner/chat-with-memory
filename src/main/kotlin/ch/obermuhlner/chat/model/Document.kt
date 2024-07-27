package ch.obermuhlner.chat.model

data class Document(
    var id: Long?,
    var name: String,
    var type: String,
    var size: Int
)