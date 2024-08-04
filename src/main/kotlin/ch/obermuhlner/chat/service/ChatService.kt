package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.model.Assistant
import ch.obermuhlner.chat.model.Chat
import ch.obermuhlner.chat.model.Document
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.ChatRepository
import ch.obermuhlner.chat.repository.DocumentRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ChatService(
    private val authService: AuthService,
    private val chatRepository: ChatRepository,
    private val assistantRepository: AssistantRepository,
    private val documentRepository: DocumentRepository,
) {
    companion object {
        const val NO_ANSWER = "NO_ANSWER"
    }

    fun createNew(): Chat {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        return Chat(
            id = null,
            title = "Chat $now",
            prompt = "If you have no relevant answer or the answer was already given, respond with $NO_ANSWER.",
            assistants = mutableListOf()
        )
    }

    @Transactional(readOnly = true)
    fun findAll(): List<Chat> {
        val userId = authService.getCurrentUserId()

        return chatRepository.findAllByUserId(userId).map { it.toChat() }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Chat? {
        val userId = authService.getCurrentUserId()

        return chatRepository.findByUserIdAndId(userId, id)?.toChat()
    }

    @Transactional
    fun create(chat: Chat): Chat {
        val user = authService.getCurrentUserEntity()

        if (chat.id != null) {
            throw IllegalArgumentException("Cannot create chat with id")
        }
        val chatEntity = chat.toChatEntity()
        chatEntity.user = user
        fillAssistants(chatEntity, chat.assistants)
        fillDocuments(chatEntity, chat.documents)

        val savedEntity = chatRepository.save(chatEntity)
        return savedEntity.toChat()
    }

    @Transactional
    fun update(chat: Chat): Chat {
        val userId = authService.getCurrentUserId()

        val existingEntity = chatRepository.findByUserIdAndId(userId, chat.id!!) ?: throw IllegalArgumentException("Chat not found: ${chat.id}")

        chat.toChatEntity(existingEntity)
        fillAssistants(existingEntity, chat.assistants)
        fillDocuments(existingEntity, chat.documents)

        chatRepository.save(existingEntity)
        return existingEntity.toChat()
    }

    private fun fillAssistants(chatEntity: ChatEntity, assistants: List<Assistant>) {
        val currentAssistants = assistantRepository.findAllById(assistants.map { it.id }).toMutableSet()

        // Remove assistants no longer associated with the chat
        chatEntity.assistants.filterNot { it in currentAssistants }.forEach {
            it.chats.remove(chatEntity)
        }
        chatEntity.assistants.clear()
        chatEntity.assistants.addAll(currentAssistants)
        chatEntity.assistants.forEach { it.chats.add(chatEntity) }
    }

    private fun fillDocuments(chatEntity: ChatEntity, documents: List<Document>) {
        val currentDocuments = documentRepository.findAllById(documents.map { it.id }).toMutableSet()

        // Remove documents no longer associated with the assistant
        chatEntity.documents.filterNot { it in currentDocuments }.forEach {
            it.chats.remove(chatEntity)
        }
        chatEntity.documents.clear()
        chatEntity.documents.addAll(currentDocuments)
        chatEntity.documents.forEach { it.chats.add(chatEntity) }
    }

    @Transactional
    fun deleteById(id: Long) {
        val userId = authService.getCurrentUserId()

        val chat = chatRepository.findByUserIdAndId(userId, id) ?: throw EntityNotFoundException("Chat not found: $id")

        // Clear the relationship with assistants
        for (assistant in chat.assistants) {
            assistant.chats.remove(chat)
            assistantRepository.save(assistant)
        }
        chat.assistants.clear()

        // Now delete the chat, which will cascade delete chatMessages
        chatRepository.deleteById(id)
    }
}

