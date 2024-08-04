package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.entity.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<ChatEntity, Long> {

    fun findAllByUserId(userId: Long): List<ChatEntity>
    fun findByUserIdAndId(userId: Long, id: Long): ChatEntity?

}
