package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.entity.ChatMessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatMessageRepository : JpaRepository<ChatMessageEntity, Long> {

    @Query("SELECT c FROM ChatMessageEntity c WHERE c.shortTermMemory = true AND c.chat = :chat order by c.timestamp")
    fun findAllShortTermMemory(chat: ChatEntity): List<ChatMessageEntity>

    fun findAllByChatId(chatId: Long): List<ChatMessageEntity>

    fun findAllByChatIdAndIdIn(chatId: Long, messageIds: Collection<Long>): List<ChatMessageEntity>

}
