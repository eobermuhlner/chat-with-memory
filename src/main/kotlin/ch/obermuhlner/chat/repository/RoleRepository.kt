package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.RoleEntity
import ch.obermuhlner.chat.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<RoleEntity, Long> {

    fun findByName(name: String): RoleEntity?
}
