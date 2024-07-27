package ch.obermuhlner.chat.entity

import jakarta.persistence.*

@Entity
class LongTermSummaryEntity {
    companion object {
        const val MAX_TEXT_LENGTH = 1024
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    var chat: ChatEntity? = null

    var level: Int = 0

    @Column(length = MAX_TEXT_LENGTH)
    var text: String = ""
}
