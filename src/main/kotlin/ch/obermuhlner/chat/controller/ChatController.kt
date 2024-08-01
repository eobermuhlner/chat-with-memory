package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.model.Chat
import ch.obermuhlner.chat.service.ChatService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chats")
class ChatController(
    private val chatService: ChatService,
) {

    @GetMapping("/new")
    fun createNew(): Chat {
        return chatService.createNew()
    }

    @GetMapping
    fun findAll(): List<Chat> {
        return chatService.findAll()
    }

    @GetMapping("{id}")
    fun findById(@PathVariable id: Long): Chat? {
        return chatService.findById(id)
    }

    @PostMapping
    fun create(@RequestBody chat: Chat): Chat {
        return chatService.create(chat)
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody chat: Chat): Chat {
        chat.id = id
        return chatService.update(chat)
    }

    @DeleteMapping("{id}")
    fun deleteById(@PathVariable id: Long) {
        chatService.deleteById(id)
    }

}
