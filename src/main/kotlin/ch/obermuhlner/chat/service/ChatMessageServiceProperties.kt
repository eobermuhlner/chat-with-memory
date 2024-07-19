package ch.obermuhlner.chat.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "message.service")
class ChatMessageServiceProperties(
    var maxMessageCount: Int = 10,
    var minMessageCount: Int = 5,
    var summaryWordCount: Int = 50
)
