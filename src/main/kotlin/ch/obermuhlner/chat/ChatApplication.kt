package ch.obermuhlner.chat

import ch.obermuhlner.chat.service.ChatMessageServiceProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ChatMessageServiceProperties::class)
class ChatApplication

fun main(args: Array<String>) {
    runApplication<ChatApplication>(*args)
}
