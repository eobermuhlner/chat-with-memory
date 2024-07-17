package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.model.ChatRequest
import ch.obermuhlner.chat.model.ChatResponse
import ch.obermuhlner.chat.service.AiService
import ch.obermuhlner.chat.service.ChatService
import ch.obermuhlner.chat.service.MessageService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService,
    private val messageService: MessageService,
    private val aiService: AiService,
) {

    init {
        messageService.setSystemMessage("""
            You are an Assistant named Pedro.
            You talk naturally and informally.
            Your answers are concise and to the point.
        """.trimIndent()
        )
    }

    @PostMapping("/send")
    fun sendMessage(@RequestBody request: ChatRequest): ChatResponse {
        return chatService.sendMessage(request.message)
//        messageService.addUserMessage(request.message)
//        val context = messageService.getContext(request.message)
//        val answer = aiService.generate(context)
//        messageService.addAssistantMessage(answer)
//        return ChatResponse(answer)
    }
}
