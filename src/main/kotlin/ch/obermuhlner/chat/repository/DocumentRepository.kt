package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.DocumentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentRepository : JpaRepository<DocumentEntity, Long>
