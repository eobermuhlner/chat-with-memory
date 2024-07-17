package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.SystemMessageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SystemMessageRepository : JpaRepository<SystemMessageEntity, Long> {
    fun findFirstByOrderByIdDesc(): SystemMessageEntity?
}
