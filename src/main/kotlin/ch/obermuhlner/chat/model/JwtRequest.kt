package ch.obermuhlner.chat.model

data class JwtRequest(
    val username: String,
    val password: String,
)