package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.model.Assistant
import ch.obermuhlner.chat.service.AssistantService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/assistants")
class AssistantController(
    private val assistantService: AssistantService,
) {

    @GetMapping
    fun findAll(): List<Assistant> {
        return assistantService.findAll()
    }

    @GetMapping("{id}")
    fun findById(@PathVariable id: Long): Assistant? {
        return assistantService.findById(id)
    }

    @PostMapping
    fun create(@RequestBody assistant: Assistant): Assistant {
        return assistantService.create(assistant)
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody assistant: Assistant): Assistant {
        assistant.id = id
        return assistantService.update(assistant)
    }

    @DeleteMapping("{id}")
    fun deleteById(@PathVariable id: Long, @RequestParam(defaultValue = "false") deleteMessages: Boolean) {
        assistantService.deleteById(id, deleteMessages)
    }

    @PostMapping("{assistantId}/documents/{documentId}")
    fun addDocumentToAssistant(@PathVariable assistantId: Long, @PathVariable documentId: Long) {
        assistantService.addDocumentToAssistant(assistantId, documentId)
    }

    @DeleteMapping("{assistantId}/documents/{documentId}")
    fun removeDocumentFromAssistant(@PathVariable assistantId: Long, @PathVariable documentId: Long) {
        assistantService.removeDocumentFromAssistant(assistantId, documentId)
    }
}
