package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.model.Document
import ch.obermuhlner.chat.model.DocumentSegment
import ch.obermuhlner.chat.model.SplitterStrategy
import ch.obermuhlner.chat.service.DocumentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/documents")
class DocumentController @Autowired constructor(
    private val documentService: DocumentService
) {

    @PostMapping
    fun uploadDocument(@RequestParam("file") file: MultipartFile, @RequestParam("splitter", defaultValue = "AI") splitterStrategy: SplitterStrategy): ResponseEntity<Document> {
        val document = documentService.saveDocument(file, splitterStrategy)
        return ResponseEntity(document, HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getDocument(@PathVariable id: Long): ResponseEntity<Document> {
        val document = documentService.getDocument(id)
        return if (document != null) {
            ResponseEntity(document, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("/{id}/segments")
    fun getDocumentSegments(@PathVariable id: Long): ResponseEntity<List<DocumentSegment>> {
        val documentSegments = documentService.getDocumentSegments(id)
        // FIXME ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(documentSegments, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteDocument(@PathVariable id: Long): ResponseEntity<Void> {
        documentService.deleteDocument(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @GetMapping
    fun findAllDocuments(): ResponseEntity<List<Document>> {
        val documents = documentService.findAllDocuments()
        return ResponseEntity(documents, HttpStatus.OK)
    }
}
