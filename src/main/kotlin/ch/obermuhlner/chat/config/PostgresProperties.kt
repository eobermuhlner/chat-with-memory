package ch.obermuhlner.chat.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
class PostgresProperties {
    lateinit var url: String
    lateinit var username: String
    lateinit var password: String

    val host: String
        get() = url.split("/")[2].split(":")[0]

    val port: Int
        get() = url.split("/")[2].split(":")[1].toInt()

    val database: String
        get() = url.split("/")[3]
}
