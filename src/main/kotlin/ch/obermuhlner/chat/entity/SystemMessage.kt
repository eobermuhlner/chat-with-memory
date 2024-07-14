package ch.obermuhlner.chat.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class SystemMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    var text: String = ""
}
