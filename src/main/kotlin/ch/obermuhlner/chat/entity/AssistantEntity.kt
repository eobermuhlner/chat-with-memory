package ch.obermuhlner.chat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany

@Entity
class AssistantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    var name: String = ""

    @Column(length = 4000)
    var description: String = ""

    @Column(length = 4000)
    var prompt: String = ""

    var sortIndex: Int = 50

    @ManyToMany
    @JoinTable(
        name = "assistant_chats",
        joinColumns = [JoinColumn(name = "assistant_id")],
        inverseJoinColumns = [JoinColumn(name = "chat_id")]
    )
    var chats: MutableSet<ChatEntity> = mutableSetOf()

    @ManyToMany
    @JoinTable(
        name = "assistant_short_term_memory",
        joinColumns = [JoinColumn(name = "assistant_id")],
        inverseJoinColumns = [JoinColumn(name = "chatmessage_id")]
    )
    val chatMessages: MutableSet<ChatMessageEntity> = mutableSetOf()
}
