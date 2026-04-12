package com.forlks.personal_wellness_routine.data.db.dao

import androidx.room.*
import com.forlks.personal_wellness_routine.data.db.entity.RoutineEntity
import com.forlks.personal_wellness_routine.data.db.entity.RoutineHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE isActive = 1 ORDER BY scheduledTime ASC")
    fun getAllActiveRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE isActive = 1 AND category = :category ORDER BY scheduledTime ASC")
    fun getRoutinesByCategory(category: String): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("UPDATE routines SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteRoutine(id: Long)

    @Query("SELECT * FROM routine_history WHERE completedDate = :date")
    fun getCompletedRoutinesForDate(date: String): Flow<List<RoutineHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: RoutineHistoryEntity)

    @Query("DELETE FROM routine_history WHERE routineId = :routineId AND completedDate = :date")
    suspend fun deleteHistory(routineId: Long, date: String)

    @Query("SELECT COUNT(*) FROM routine_history WHERE completedDate = :date")
    suspend fun getCompletedCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM routines WHERE isActive = 1")
    suspend fun getTotalActiveCount(): Int

    // Streak calculation: consecutive days with at least 1 routine completed
    @Query("""
        SELECT completedDate FROM routine_history
        GROUP BY completedDate
        ORDER BY completedDate DESC
        LIMIT 90
    """)
    suspend fun getCompletedDates(): List<String>

    // Weekly stats: count per day in range
    @Query("""
        SELECT completedDate, COUNT(*) as count
        FROM routine_history
        WHERE completedDate BETWEEN :startDate AND :endDate
        GROUP BY completedDate
        ORDER BY completedDate ASC
    """)
    suspend fun getWeeklyStats(startDate: String, endDate: String): List<DayCount>

    data class DayCount(val completedDate: String, val count: Int)
}
