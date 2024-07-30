package ch.obermuhlner.chat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    var username: String = ""

    @Column(nullable = false)
    var password: String = ""
}
