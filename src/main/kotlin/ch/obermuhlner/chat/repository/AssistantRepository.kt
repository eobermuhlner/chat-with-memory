package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.AssistantEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AssistantRepository : JpaRepository<AssistantEntity, Long> {

    fun findAllByUserId(userId: Long): List<AssistantEntity>
    fun findByUserIdAndId(userId: Long, id: Long): AssistantEntity?
}