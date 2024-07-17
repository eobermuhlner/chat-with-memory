package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.LongTermSummaryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface LongTermSummaryRepository : JpaRepository<LongTermSummaryEntity, Long> {
    fun findAllByOrderByLevelAsc(): List<LongTermSummaryEntity>
    fun findByLevel(level: Int): List<LongTermSummaryEntity>
}
