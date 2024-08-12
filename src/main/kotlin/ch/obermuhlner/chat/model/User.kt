package ch.obermuhlner.chat.model

data class User(
    var id: Long?,
    var username: String,
    var password: String,
    var prompt: String = "",
    var openaiApiKey: String = "",
    var githubApiKey: String = "",
    val roles: MutableList<String> = mutableListOf()
)