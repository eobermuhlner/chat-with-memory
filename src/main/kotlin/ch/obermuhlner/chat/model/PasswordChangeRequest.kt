package ch.obermuhlner.chat.model

data class PasswordChangeRequest(
    val oldPassword: String,
    val newPassword: String
)