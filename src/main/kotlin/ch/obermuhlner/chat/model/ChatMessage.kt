package ch.obermuhlner.chat.model

import java.time.Instant

data class ChatMessage(
    val id: Long,
    val type: MessageType,
    val sender: String?,
    val text: String,
    val timestamp: Instant
)