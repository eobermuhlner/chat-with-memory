package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.model.Assistant
import ch.obermuhlner.chat.model.Document
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.ChatMessageRepository
import ch.obermuhlner.chat.repository.ChatRepository
import ch.obermuhlner.chat.repository.DocumentRepository
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
    private val documentRepository: DocumentRepository
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
        val assistantEntity = assistant.toAssistantEntity()
        fillDocuments(assistantEntity, assistant.documents)
        val savedEntity = assistantRepository.save(assistantEntity)

        return savedEntity.toAssistant()
    }

    @Transactional
    fun update(assistant: Assistant): Assistant {
        val existingEntity = assistantRepository.findById(assistant.id).getOrNull() ?: throw IllegalArgumentException("Assistant not found: ${assistant.id}")
        assistant.toAssistantEntity(existingEntity)

        fillDocuments(existingEntity, assistant.documents)
        val savedEntity = assistantRepository.save(existingEntity)

        return savedEntity.toAssistant()
    }

    private fun fillDocuments(assistantEntity: AssistantEntity, documents: List<Document>) {
        val currentDocuments = documentRepository.findAllById(documents.map { it.id }).toMutableSet()

        // Remove documents no longer associated with the assistant
        assistantEntity.documents.filterNot { it in currentDocuments }.forEach {
            it.assistants.remove(assistantEntity)
        }
        assistantEntity.documents.clear()
        assistantEntity.documents.addAll(currentDocuments)
        assistantEntity.documents.forEach { it.assistants.add(assistantEntity) }
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

    @Transactional
    fun addDocumentToAssistant(assistantId: Long, documentId: Long) {
        val assistant = assistantRepository.findById(assistantId).orElseThrow { EntityNotFoundException("Assistant not found: $assistantId") }
        val document = documentRepository.findById(documentId).orElseThrow { EntityNotFoundException("Document not found: $documentId") }
        assistant.documents.add(document)
        assistantRepository.save(assistant)
    }

    @Transactional
    fun removeDocumentFromAssistant(assistantId: Long, documentId: Long) {
        val assistant = assistantRepository.findById(assistantId).orElseThrow { EntityNotFoundException("Assistant not found: $assistantId") }
        val document = documentRepository.findById(documentId).orElseThrow { EntityNotFoundException("Document not found: $documentId") }
        assistant.documents.remove(document)
        assistantRepository.save(assistant)
    }
}
