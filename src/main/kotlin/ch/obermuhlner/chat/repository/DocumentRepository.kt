package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.entity.DocumentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentRepository : JpaRepository<DocumentEntity, Long> {

    fun findAllByUserId(userId: Long): List<DocumentEntity>
    fun findByUserIdAndId(userId: Long, id: Long): DocumentEntity?
}
