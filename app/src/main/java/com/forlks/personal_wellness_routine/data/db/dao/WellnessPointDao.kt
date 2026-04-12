package com.forlks.personal_wellness_routine.data.db.dao

import androidx.room.*
import com.forlks.personal_wellness_routine.data.db.entity.WellnessPointHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessPointDao {
    @Query("SELECT * FROM wellness_point_history ORDER BY earnedAt DESC")
    fun getAllHistory(): Flow<List<WellnessPointHistoryEntity>>

    @Query("SELECT SUM(points) FROM wellness_point_history")
    fun getTotalPoints(): Flow<Int?>

    @Query("SELECT * FROM wellness_point_history ORDER BY earnedAt DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int): List<WellnessPointHistoryEntity>

    @Query("SELECT SUM(points) FROM wellness_point_history WHERE date = :date")
    suspend fun getPointsForDate(date: String): Int?

    @Query("SELECT EXISTS(SELECT 1 FROM wellness_point_history WHERE date = :date AND eventType = :eventType)")
    suspend fun hasEarnedToday(date: String, eventType: String): Boolean

    @Insert
    suspend fun insertHistory(history: WellnessPointHistoryEntity)
}
