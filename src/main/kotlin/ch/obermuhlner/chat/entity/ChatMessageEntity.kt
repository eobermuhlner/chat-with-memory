package ch.obermuhlner.chat.entity

import ch.obermuhlner.chat.model.MessageType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import java.time.Instant

@Entity
class ChatMessageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Enumerated(EnumType.STRING)
    var messageType: MessageType = MessageType.User

    @ManyToOne(fetch = FetchType.LAZY)
    var sender: AssistantEntity? = null

    @Lob
    var text: String = ""

    var timestamp: Instant = Instant.now()

    var shortTermMemory: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    var chat: ChatEntity? = null

    @ManyToMany(mappedBy = "chatMessages")
    val assistants: MutableSet<AssistantEntity> = mutableSetOf()
}
