package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.model.ChatMessage
import ch.obermuhlner.chat.repository.ChatMessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository
) {

    @Transactional(readOnly = true)
    fun findAllMessages(chatId: Long): List<ChatMessage> {
        return chatMessageRepository.findAllByChatId(chatId)
            .map { it.toChatMessage() }
    }

    @Transactional(readOnly = true)
    fun findById(chatId: Long, messageId: Long): ChatMessage? {
        val chatMessageEntity = chatMessageRepository.findById(messageId).orElse(null) ?: return null
        if (chatMessageEntity.chat?.id != chatId) {
            return null
        }

        return chatMessageEntity.toChatMessage()
    }
}