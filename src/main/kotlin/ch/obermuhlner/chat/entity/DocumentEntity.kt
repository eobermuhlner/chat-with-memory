package ch.obermuhlner.chat.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne

@Entity
class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var name: String = ""

    var type: String = ""

    @Lob
    var data: ByteArray = byteArrayOf()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    lateinit var user: UserEntity

    @ManyToMany(mappedBy = "documents")
    var assistants: MutableSet<AssistantEntity> = mutableSetOf()

    @ManyToMany(mappedBy = "documents")
    var chats: MutableSet<ChatEntity> = mutableSetOf()
}
