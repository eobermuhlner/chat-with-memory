package ch.obermuhlner.chat.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy

@Entity
class ChatEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    var title: String = ""

    @ManyToMany(mappedBy = "chats")
    @OrderBy("sortIndex ASC")
    val assistants: MutableList<AssistantEntity> = mutableListOf()

    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("timestamp ASC")
    val chatMessages: MutableList<ChatMessageEntity> = mutableListOf()
}
