package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.entity.ChatMessageEntity
import ch.obermuhlner.chat.model.Assistant
import ch.obermuhlner.chat.model.Chat
import ch.obermuhlner.chat.model.ChatDetails
import ch.obermuhlner.chat.model.ChatMessage
import java.time.temporal.ChronoUnit

fun AssistantEntity.toChatString(): String {
    return """
            |# ${name} - ${description} 
            |${prompt}
        """.trimMargin()
}

fun ChatMessageEntity.toChatString(): String {
    return """
            |${sender?.name ?: "User"} (${timestamp.truncatedTo(ChronoUnit.SECONDS)}):
            |${text}
        """.trimMargin()
}

fun ChatMessageEntity.toShortChatString(): String {
    return """
            |${this.sender?.name ?: "User"}:
            |${this.text}
        """.trimMargin()
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = this.id,
        type = this.messageType,
        sender = this.sender?.name,
        text = this.text,
        timestamp = this.timestamp
    )
}

fun ChatEntity.toChat(): Chat {
    return Chat(
        id = this.id,
        title = this.title
    )
}

fun ChatEntity.toChatDetails(): ChatDetails {
    return ChatDetails(
        id = this.id,
        title = this.title,
        prompt = this.prompt,
        assistants = this.assistants.map { it.toAssistant() }.toMutableList()
    )
}

fun ChatDetails.toChatEntity(chatEntity: ChatEntity = ChatEntity()): ChatEntity {
    return chatEntity.apply {
        id = this@toChatEntity.id
        title = this@toChatEntity.title
        prompt = this@toChatEntity.prompt
        // assistants are mapped in the ChatService
    }
}

fun AssistantEntity.toAssistant(): Assistant {
    return Assistant(
        id = this.id,
        name = this.name,
        description = this.description,
        prompt = this.prompt,
        sortIndex = this.sortIndex
    )
}

fun Assistant.toAssistantEntity(assistantEntity: AssistantEntity = AssistantEntity()): AssistantEntity {
    return assistantEntity.apply {
        id = this@toAssistantEntity.id
        name = this@toAssistantEntity.name
        description = this@toAssistantEntity.description
        prompt = this@toAssistantEntity.prompt
        sortIndex = this@toAssistantEntity.sortIndex
    }
}