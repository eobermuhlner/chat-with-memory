package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<ChatEntity, Long> {

    fun findByTitle(title: String): ChatEntity?
}
