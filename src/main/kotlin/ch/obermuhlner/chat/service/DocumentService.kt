package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.DocumentEntity
import ch.obermuhlner.chat.model.Document
import ch.obermuhlner.chat.model.DocumentSegment
import ch.obermuhlner.chat.model.SplitterStrategy
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.DocumentRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import kotlin.jvm.optionals.getOrNull

@Service
class DocumentService @Autowired constructor(
    private val documentRepository: DocumentRepository,
    private val assistantRepository: AssistantRepository,
    private val documentRetrievalService: DocumentRetrievalService,
) {

    fun saveDocument(file: MultipartFile, splitterStrategy: SplitterStrategy): Document {
        val documentEntity = DocumentEntity().apply {
            name = file.originalFilename ?: "Unnamed"
            type = file.contentType ?: "application/octet-stream"
            data = file.bytes
        }
        val savedDocument = documentRepository.save(documentEntity).toDocument()!!
        documentRetrievalService.addDocument(documentEntity, splitterStrategy)
        return savedDocument
    }

    fun getDocument(id: Long): Document? {
        return documentRepository
            .findById(id)
            .getOrNull()
            ?.toDocument()
    }

    fun getDocumentSegments(id: Long): List<DocumentSegment> {
        return documentRetrievalService.getAllTextSegments(id)
            .map { DocumentSegment(id, it.metadata().getInteger("index"), it.text()) }
            .sortedBy { it.index }
    }

    fun deleteDocument(id: Long) {
        val document = documentRepository.findById(id).orElseThrow { EntityNotFoundException("Document not found: $id") }

        // Remove the document from all assistants
        val assistants = assistantRepository.findAll()
        for (assistant in assistants) {
            if (assistant.documents.contains(document)) {
                assistant.documents.remove(document)
                assistantRepository.save(assistant)
            }
        }

        documentRepository.deleteById(id)
    }

    fun findAllDocuments(): List<Document> {
        return documentRepository.findAll().mapNotNull { it.toDocument() }
    }
}
