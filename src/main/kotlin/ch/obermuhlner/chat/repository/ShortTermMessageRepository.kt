package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.ShortTermMessageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ShortTermMessageRepository : JpaRepository<ShortTermMessageEntity, Long>
