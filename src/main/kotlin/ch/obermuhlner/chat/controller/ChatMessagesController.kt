package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.model.Chat
import ch.obermuhlner.chat.model.ChatDetails
import ch.obermuhlner.chat.model.ChatMessage
import ch.obermuhlner.chat.model.ChatRequest
import ch.obermuhlner.chat.model.ChatResponse
import ch.obermuhlner.chat.service.ChatMessageService
import ch.obermuhlner.chat.service.ChatService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chats")
class ChatMessagesController(
    private val chatMessageService: ChatMessageService,
) {

    @GetMapping("{chatId}/messages")
    fun findAllChatMessages(@PathVariable chatId: Long): List<ChatMessage> {
        return chatMessageService.findAllMessages(chatId)
    }

    @GetMapping("{chatId}/messages/{messageId}")
    fun findById(@PathVariable chatId: Long, @PathVariable messageId: Long): ChatMessage? {
        return chatMessageService.findById(chatId, messageId)
    }
}
