package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.entity.ChatMessageEntity
import ch.obermuhlner.chat.entity.DocumentEntity
import ch.obermuhlner.chat.entity.UserEntity
import ch.obermuhlner.chat.model.Assistant
import ch.obermuhlner.chat.model.Chat
import ch.obermuhlner.chat.model.ChatMessage
import ch.obermuhlner.chat.model.Document
import ch.obermuhlner.chat.model.MessageType
import ch.obermuhlner.chat.model.Tool
import ch.obermuhlner.chat.model.User
import ch.obermuhlner.chat.repository.RoleRepository
import java.time.temporal.ChronoUnit

fun AssistantEntity.toChatString(): String {
    return """
            |### ${name} - ${description} 
            |${prompt}
        """.trimMargin()
}

fun ChatMessageEntity.toChatString(): String {
    return """
            |### ${sender?.name ?: "User"} (${timestamp.truncatedTo(ChronoUnit.SECONDS)}):
            |${text}
        """.trimMargin()
}

fun ChatMessageEntity.toShortChatString(): String {
    return """
            |### ${this.sender?.name ?: (if (messageType == MessageType.User) "User" else "Deleted")}:
            |${this.text}
        """.trimMargin()
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = this.id,
        type = this.messageType,
        sender = this.sender?.name ?: (if (messageType == MessageType.User) "User" else "Deleted"),
        text = this.text,
        timestamp = this.timestamp
    )
}

fun ChatEntity.toChat(): Chat {
    return Chat(
        id = this.id,
        title = this.title,
        prompt = this.prompt,
        assistants = this.assistants.map { it.toAssistant() }.toMutableList(),
        documents = this.documents.map { it.toDocument() }.toMutableList(),
        tools = this.tools.map { it.name }
    )
}

fun Chat.toChatEntity(chatEntity: ChatEntity = ChatEntity()): ChatEntity {
    return chatEntity.apply {
        id = this@toChatEntity.id
        title = this@toChatEntity.title
        prompt = this@toChatEntity.prompt
        tools = this@toChatEntity.tools.mapNotNull {
            try {
                Tool.valueOf(it)
            } catch (ex: Exception) {
                null
            }
        }
        // assistants are mapped in the ChatService
    }
}

fun AssistantEntity.toAssistant(): Assistant {
    return Assistant(
        id = this.id,
        name = this.name,
        description = this.description,
        prompt = this.prompt,
        sortIndex = this.sortIndex,
        tools = this.tools.map { it.name },
        documents = this.documents.map { it.toDocument() }
    )
}

fun Assistant.toAssistantEntity(assistantEntity: AssistantEntity = AssistantEntity()): AssistantEntity {
    return assistantEntity.apply {
        id = this@toAssistantEntity.id
        name = this@toAssistantEntity.name
        description = this@toAssistantEntity.description
        prompt = this@toAssistantEntity.prompt
        sortIndex = this@toAssistantEntity.sortIndex
        tools = this@toAssistantEntity.tools.mapNotNull {
            try {
                Tool.valueOf(it)
            } catch (ex: Exception) {
                null
            }
        }
        // documents are mapped in the AssistantService
    }
}

fun DocumentEntity.toDocument(): Document {
    return Document(
        id = this.id,
        name = this.name,
        type = this.type,
        size = this.data.size
    )
}
fun UserEntity.toUser(): User {
    return User(
        id = this.id,
        username = this.username,
        password = "",
        roles = this.roles.map { it.name },
    )
}

fun User.toUserEntity(userEntity: UserEntity = UserEntity(), roleRepository: RoleRepository, keepExistingPassword: Boolean = false): UserEntity {
    return userEntity.apply {
        id = this@toUserEntity.id
        username = this@toUserEntity.username
        if (!keepExistingPassword) {
            password = this@toUserEntity.password
        }
        roles = this@toUserEntity.roles.mapNotNull { role ->
            roleRepository.findByName(role)
        }.toMutableSet()
    }
}
