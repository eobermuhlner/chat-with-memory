package ch.obermuhlner.chat.entity

import ch.obermuhlner.chat.service.MessageType
import jakarta.persistence.*

import java.time.Instant

@Entity
class ShortTermMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
    @Enumerated(EnumType.STRING)
    var messageType: MessageType = MessageType.User
    @Lob
    var text: String = ""
    var timestamp: Instant = Instant.now()
}
