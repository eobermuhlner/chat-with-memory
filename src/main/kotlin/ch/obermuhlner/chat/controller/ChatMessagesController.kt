package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.model.Chat
import ch.obermuhlner.chat.model.ChatDetails
import ch.obermuhlner.chat.model.ChatMessage
import ch.obermuhlner.chat.model.ChatRequest
import ch.obermuhlner.chat.model.ChatResponse
import ch.obermuhlner.chat.service.ChatMessageService
import ch.obermuhlner.chat.service.ChatService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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

    @PostMapping("/{chatId}/messages")
    fun sendMessage(@PathVariable chatId: Long, @RequestBody request: ChatRequest): ChatResponse {
        return chatMessageService.sendMessage(chatId, request.message)
    }

    @DeleteMapping("/{chatId}/messages/{messageId}")
    fun deleteMessage(@PathVariable chatId: Long, @PathVariable messageId: Long) {
        return chatMessageService.deleteMessage(chatId, messageId)
    }

    @DeleteMapping("/{chatId}/messages")
    fun deleteAllMessage(@PathVariable chatId: Long, @RequestParam(defaultValue = "false") transferToLongTermMemory: Boolean) {
        return chatMessageService.deleteAllMessages(chatId, transferToLongTermMemory)
    }
}
