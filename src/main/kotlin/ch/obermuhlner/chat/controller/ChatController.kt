package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.model.Chat
import ch.obermuhlner.chat.model.ChatDetails
import ch.obermuhlner.chat.model.ChatRequest
import ch.obermuhlner.chat.model.ChatResponse
import ch.obermuhlner.chat.service.ChatService
import ch.obermuhlner.chat.service.ChatService.Companion.NO_ANSWER
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/chats")
class ChatController(
    private val chatService: ChatService,
) {

    @GetMapping("/new")
    fun createNew(): ChatDetails {
        return chatService.createNew()
    }

    @GetMapping
    fun findAll(): List<Chat> {
        return chatService.findAll()
    }

    @GetMapping("{id}")
    fun findById(@PathVariable id: Long): ChatDetails? {
        return chatService.findById(id)
    }

    // FIXME move to ChatMessagesController
    @PostMapping("/{id}/send")
    fun sendMessage(@PathVariable id: Long, @RequestBody request: ChatRequest): ChatResponse {
        return chatService.sendMessage(id, request.message)
    }

    @PostMapping
    fun create(@RequestBody chat: ChatDetails): ChatDetails {
        return chatService.create(chat)
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody chat: ChatDetails): ChatDetails {
        chat.id = id
        return chatService.update(chat)
    }

    @DeleteMapping("{id}")
    fun deleteById(@PathVariable id: Long) {
        chatService.deleteById(id)
    }

}
