package ch.obermuhlner.chat.entity

import jakarta.persistence.*

@Entity
class LongTermSummary {
    companion object {
        const val MAX_TEXT_LENGTH = 1024
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
    var level: Int = 0
    @Column(length = MAX_TEXT_LENGTH)
    var text: String = ""
}
