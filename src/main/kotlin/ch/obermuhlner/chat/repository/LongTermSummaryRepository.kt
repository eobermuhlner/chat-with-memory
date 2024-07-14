package ch.obermuhlner.chat.repository

import ch.obermuhlner.chat.entity.LongTermSummary
import ch.obermuhlner.chat.entity.ShortTermMessage
import ch.obermuhlner.chat.entity.SystemMessage
import org.springframework.data.jpa.repository.JpaRepository

interface LongTermSummaryRepository : JpaRepository<LongTermSummary, Long> {
    fun findAllByOrderByLevelAsc(): List<LongTermSummary>
    fun findByLevel(level: Int): List<LongTermSummary>
}
