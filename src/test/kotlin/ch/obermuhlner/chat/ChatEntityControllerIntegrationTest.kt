package ch.obermuhlner.chat

import ch.obermuhlner.chat.controller.ChatRequest
import ch.obermuhlner.chat.controller.ChatResponse
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat

class ChatEntityControllerIntegrationTest: AbstractControllerIntegrationTest() {

    @Test
    fun `test chat`() {
        requestPost("/chat/send", ChatRequest("Hi"), ChatResponse::class.java).let {
            assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(it.body).isNotNull
            println(it.body.response)
        }
    }
}