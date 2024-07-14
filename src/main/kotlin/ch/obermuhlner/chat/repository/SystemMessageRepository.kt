package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.SystemMessage
import org.springframework.data.jpa.repository.JpaRepository

interface SystemMessageRepository : JpaRepository<SystemMessage, Long> {
    fun findFirstByOrderByIdDesc(): SystemMessage?
}
