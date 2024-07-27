package ch.obermuhlner.chat.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.ManyToMany

@Entity
class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var name: String = ""

    var type: String = ""

    @Lob
    var data: ByteArray = byteArrayOf()

    @ManyToMany(mappedBy = "documents")
    var assistants: MutableSet<AssistantEntity> = mutableSetOf()

    @ManyToMany(mappedBy = "documents")
    var chats: MutableSet<ChatEntity> = mutableSetOf()
}
