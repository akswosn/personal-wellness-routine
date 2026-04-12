package com.forlks.personal_wellness_routine.data.repository

import com.forlks.personal_wellness_routine.data.db.dao.RoutineDao
import com.forlks.personal_wellness_routine.data.db.entity.RoutineEntity
import com.forlks.personal_wellness_routine.data.db.entity.RoutineHistoryEntity
import com.forlks.personal_wellness_routine.domain.model.Routine
import com.forlks.personal_wellness_routine.domain.model.RoutineCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao
) {
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getRoutinesForToday(): Flow<List<Routine>> {
        val today = LocalDate.now().format(fmt)
        return combine(
            routineDao.getAllActiveRoutines(),
            routineDao.getCompletedRoutinesForDate(today)
        ) { routines, completed ->
            val completedIds = completed.map { it.routineId }.toSet()
            routines.map { r ->
                r.toDomain(
                    streak = 0, // streak computed separately
                    isCompletedToday = r.id in completedIds
                )
            }
        }
    }

    fun getRoutinesByCategory(category: RoutineCategory): Flow<List<Routine>> {
        val today = LocalDate.now().format(fmt)
        val flow = if (category == RoutineCategory.ALL)
            routineDao.getAllActiveRoutines()
        else
            routineDao.getRoutinesByCategory(category.name)

        return combine(
            flow,
            routineDao.getCompletedRoutinesForDate(today)
        ) { routines, completed ->
            val completedIds = completed.map { it.routineId }.toSet()
            routines.map { r -> r.toDomain(isCompletedToday = r.id in completedIds) }
        }
    }

    suspend fun addRoutine(routine: Routine): Long {
        return routineDao.insertRoutine(routine.toEntity())
    }

    suspend fun updateRoutine(routine: Routine) {
        routineDao.updateRoutine(routine.toEntity())
    }

    suspend fun deleteRoutine(id: Long) {
        routineDao.softDeleteRoutine(id)
    }

    suspend fun toggleComplete(routineId: Long, date: String, isCompleted: Boolean) {
        if (isCompleted) {
            routineDao.insertHistory(RoutineHistoryEntity(routineId = routineId, completedDate = date))
        } else {
            routineDao.deleteHistory(routineId, date)
        }
    }

    suspend fun getTodayStats(): Pair<Int, Int> {
        val today = LocalDate.now().format(fmt)
        val completed = routineDao.getCompletedCountForDate(today)
        val total = routineDao.getTotalActiveCount()
        return completed to total
    }

    suspend fun getStreak(): Int {
        val dates = routineDao.getCompletedDates().toSet()
        var streak = 0
        var day = LocalDate.now()
        while (dates.contains(day.format(fmt))) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    suspend fun getWeeklyStats(startDate: String, endDate: String) =
        routineDao.getWeeklyStats(startDate, endDate)

    private fun RoutineEntity.toDomain(streak: Int = 0, isCompletedToday: Boolean = false) = Routine(
        id = id, name = name,
        category = RoutineCategory.values().find { it.name == category } ?: RoutineCategory.HEALTH,
        emoji = emoji, scheduledTime = scheduledTime,
        durationMinutes = durationMinutes, streak = streak, isCompletedToday = isCompletedToday
    )

    private fun Routine.toEntity() = RoutineEntity(
        id = id, name = name, category = category.name,
        emoji = emoji, scheduledTime = scheduledTime, durationMinutes = durationMinutes
    )
}
