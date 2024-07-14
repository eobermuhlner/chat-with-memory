package ch.obermuhlner.chat.service

import org.springframework.boot.context.properties.ConfigurationProperties

//@Configuration
@ConfigurationProperties(prefix = "message.service")
class MessageServiceProperties(
    var maxMessageCount: Int = 10,
    var minMessageCount: Int = 5,
    var summaryWordCount: Int = 50
)
