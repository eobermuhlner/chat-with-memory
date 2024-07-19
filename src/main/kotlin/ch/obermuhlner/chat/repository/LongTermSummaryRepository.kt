package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.entity.LongTermSummaryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface LongTermSummaryRepository : JpaRepository<LongTermSummaryEntity, Long> {

    fun findByChatAndLevel(chat: ChatEntity, level: Int): List<LongTermSummaryEntity>

    fun deleteAllByChat(chat: ChatEntity)
}
