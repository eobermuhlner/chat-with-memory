package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.config.DataInitializerConfig
import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.entity.ChatMessageEntity
import ch.obermuhlner.chat.entity.LongTermSummaryEntity
import ch.obermuhlner.chat.model.Assistant
import ch.obermuhlner.chat.model.Chat
import ch.obermuhlner.chat.model.ChatDetails
import ch.obermuhlner.chat.model.ChatMessage
import ch.obermuhlner.chat.model.ChatResponse
import ch.obermuhlner.chat.model.MessageType
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.ChatMessageRepository
import ch.obermuhlner.chat.repository.ChatRepository
import ch.obermuhlner.chat.repository.LongTermSummaryRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.jvm.optionals.getOrNull

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val assistantRepository: AssistantRepository,
) {
    companion object {
        const val NO_ANSWER = "NO_ANSWER"
    }

    fun createNew(): ChatDetails {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        return ChatDetails(
            id = 0,
            title = "Chat $now",
            prompt = "If you have no relevant answer or the answer was already given, respond with $NO_ANSWER.",
            assistants = mutableListOf()
        )
    }

    @Transactional(readOnly = true)
    fun findAll(): List<Chat> = chatRepository.findAll().map { it.toChat() }

    @Transactional(readOnly = true)
    fun findById(id: Long): ChatDetails? = chatRepository.findByIdOrNull(id)?.toChatDetails()

    @Transactional
    fun create(chat: ChatDetails): ChatDetails {
        if (chat.id != 0L) {
            throw IllegalArgumentException("Cannot create chat with id 0")
        }
        val chatEntity = chat.toChatEntity()
        fillAssistants(chatEntity, chat.assistants)

        val savedEntity = chatRepository.save(chatEntity)
        return savedEntity.toChatDetails()
    }

    @Transactional
    fun update(chat: ChatDetails): ChatDetails {
        val existingEntity = chatRepository.findById(chat.id).getOrNull() ?: throw IllegalArgumentException("Chat not found: ${chat.id}")

        chat.toChatEntity(existingEntity)
        fillAssistants(existingEntity, chat.assistants)

        chatRepository.save(existingEntity)
        return existingEntity.toChatDetails()
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

    @Transactional
    fun deleteById(id: Long) {
        val chat = chatRepository.findById(id).orElseThrow { EntityNotFoundException("Chat not found: $id") }

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

