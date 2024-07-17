package ch.obermuhlner.chat.model

import java.time.Instant

class ChatRequest(var message: String)

class ChatResponse(var messages: List<ChatMessage>)

enum class MessageType {
    User,
    Assistant,
    System
}

data class ChatMessage(
    val id: Long,
    val messageType: MessageType,
    val sender: String?,
    val text: String,
    val timestamp: Instant
)