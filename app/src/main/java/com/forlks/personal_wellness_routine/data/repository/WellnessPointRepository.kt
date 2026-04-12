package com.forlks.personal_wellness_routine.data.repository

import com.forlks.personal_wellness_routine.data.db.dao.WellnessPointDao
import com.forlks.personal_wellness_routine.data.db.entity.WellnessPointHistoryEntity
import com.forlks.personal_wellness_routine.domain.model.WpEvent
import com.forlks.personal_wellness_routine.domain.model.calculateCharacterLevel
import com.forlks.personal_wellness_routine.domain.model.nextLevelWp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WellnessPointRepository @Inject constructor(
    private val dao: WellnessPointDao
) {
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val totalPoints: Flow<Int> = dao.getTotalPoints().map { it ?: 0 }

    suspend fun earnPoints(eventType: String, description: String, customPoints: Int? = null): Boolean {
        val today = LocalDate.now().format(fmt)
        // Prevent duplicate attendance/diary/chat per day
        if (eventType in listOf(WpEvent.ATTENDANCE, WpEvent.DIARY, WpEvent.CHAT_ANALYSIS)) {
            if (dao.hasEarnedToday(today, eventType)) return false
        }
        val pts = customPoints ?: WpEvent.points(eventType)
        dao.insertHistory(
            WellnessPointHistoryEntity(
                date = today, eventType = eventType,
                points = pts, description = description
            )
        )
        return true
    }

    suspend fun getTodayPoints(): Int = dao.getPointsForDate(LocalDate.now().format(fmt)) ?: 0

    suspend fun getRecentHistory(limit: Int = 10) = dao.getRecentHistory(limit)

    suspend fun getCurrentLevel(totalWp: Int) = calculateCharacterLevel(totalWp)
    suspend fun getNextLevelWp(totalWp: Int) = nextLevelWp(totalWp)
}
