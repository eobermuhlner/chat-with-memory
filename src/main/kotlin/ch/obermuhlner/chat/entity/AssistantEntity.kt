package ch.obermuhlner.chat.entity

import ch.obermuhlner.chat.model.Tool
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne

@Entity
class AssistantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var name: String = ""

    @Column(length = 4000)
    var description: String = ""

    @Column(length = 4000)
    var prompt: String = ""

    var sortIndex: Int = 50

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null

    @ManyToMany
    @JoinTable(
        name = "assistant_chats",
        joinColumns = [JoinColumn(name = "assistant_id")],
        inverseJoinColumns = [JoinColumn(name = "chat_id")]
    )
    var chats: MutableSet<ChatEntity> = mutableSetOf()

    @Column(name = "tools")
    @Convert(converter = ToolListConverter::class)
    var tools: List<Tool> = mutableListOf()

    @ManyToMany
    @JoinTable(
        name = "assistant_documents",
        joinColumns = [JoinColumn(name = "assistant_id")],
        inverseJoinColumns = [JoinColumn(name = "document_id")]
    )
    var documents: MutableSet<DocumentEntity> = mutableSetOf()
}
