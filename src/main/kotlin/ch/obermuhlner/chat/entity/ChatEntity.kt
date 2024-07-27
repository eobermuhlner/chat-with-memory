package ch.obermuhlner.chat.entity

import ch.obermuhlner.chat.model.Tool
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy

@Entity
class ChatEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var title: String = ""

    @Column(length = 4000)
    var prompt: String = ""

    @ManyToMany(mappedBy = "chats")
    @OrderBy("sortIndex ASC")
    val assistants: MutableList<AssistantEntity> = mutableListOf()

    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("timestamp ASC")
    val chatMessages: MutableList<ChatMessageEntity> = mutableListOf()

    @Column(name = "tools")
    @Convert(converter = ToolListConverter::class)
    var tools: List<Tool> = mutableListOf()


    @ManyToMany
    @JoinTable(
        name = "chat_documents",
        joinColumns = [JoinColumn(name = "chat_id")],
        inverseJoinColumns = [JoinColumn(name = "document_id")]
    )
    var documents: MutableSet<DocumentEntity> = mutableSetOf()
}
