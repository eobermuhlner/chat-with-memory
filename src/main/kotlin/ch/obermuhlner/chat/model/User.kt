package ch.obermuhlner.chat.model

data class User(
    var id: Long?,
    var username: String,
    var password: String,
    var prompt: String = "",
    var openApiKey: String?,
    val roles: List<String> = mutableListOf()
)