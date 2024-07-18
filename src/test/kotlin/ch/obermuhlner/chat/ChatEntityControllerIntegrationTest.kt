package ch.obermuhlner.chat

import ch.obermuhlner.chat.model.Chat
import ch.obermuhlner.chat.model.ChatRequest
import ch.obermuhlner.chat.model.ChatResponse
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat

class ChatEntityControllerIntegrationTest: AbstractControllerIntegrationTest() {

    @Test
    fun `test chat`() {
        requestPost("/chats/send", ChatRequest("Hi"), ChatResponse::class.java).let {
            assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(it.body).isNotNull
            println(it.body.messages)
        }
    }
}