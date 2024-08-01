package ch.obermuhlner.chat.entity

import jakarta.persistence.*

@Entity
@Table(name = "roles")
class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    var name: String = ""
}
