package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.model.Assistant
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.ChatMessageRepository
import ch.obermuhlner.chat.repository.ChatRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class AssistantService(
    private val assistantRepository: AssistantRepository,
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
) {

    @Transactional(readOnly = true)
    fun findAll(): List<Assistant> = assistantRepository.findAll().map { it.toAssistant() }

    @Transactional(readOnly = true)
    fun findById(id: Long): Assistant? = assistantRepository.findByIdOrNull(id)?.toAssistant()

    @Transactional
    fun create(assistant: Assistant): Assistant {
        if (assistant.id != 0L) {
            throw IllegalArgumentException("Cannot create assistant with id 0")
        }
        val savedEntity = assistantRepository.save(assistant.toAssistantEntity())
        return savedEntity.toAssistant()
    }

    @Transactional
    fun update(assistant: Assistant): Assistant {
        val existingEntity = assistantRepository.findById(assistant.id).getOrNull() ?: throw IllegalArgumentException("Assistant not found: ${assistant.id}")
        assistant.toAssistantEntity(existingEntity)
        val savedEntity = assistantRepository.save(existingEntity)
        return savedEntity.toAssistant()
    }

    @Transactional
    fun deleteById(id: Long, deleteMessage: Boolean) {
        val assistant = assistantRepository.findById(id).orElseThrow { EntityNotFoundException("Assistant not found: $id") }

        if (deleteMessage) {
            chatMessageRepository.deleteAllBySender(assistant)
        } else {
            val chatMessages = chatMessageRepository.findAllBySender(assistant)
            for (chatMessage in chatMessages) {
                chatMessage.sender = null
                chatMessageRepository.save(chatMessage)
            }
        }

        for (chat in assistant.chats) {
            chat.assistants.remove(assistant)
            chatRepository.save(chat)
        }
        assistant.chats.clear()

        assistantRepository.deleteById(id)
    }
}