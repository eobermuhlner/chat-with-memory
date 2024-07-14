package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.ShortTermMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ShortTermMessageRepository : JpaRepository<ShortTermMessage, Long>
