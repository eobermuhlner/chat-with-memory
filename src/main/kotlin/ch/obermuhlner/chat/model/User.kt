package ch.obermuhlner.chat.model

data class User(
    var id: Long?,
    var username: String,
    var password: String,
)