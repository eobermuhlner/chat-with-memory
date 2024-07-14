package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.service.AiService
import ch.obermuhlner.chat.service.MessageService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController(
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
        messageService.addUserMessage(request.message)
        val context = messageService.getContext(request.message)
        val answer = aiService.generate(context)
        messageService.addAssistantMessage(answer)
        return ChatResponse(answer)
    }
}

class ChatRequest(var message: String)
class ChatResponse(var response: String)
