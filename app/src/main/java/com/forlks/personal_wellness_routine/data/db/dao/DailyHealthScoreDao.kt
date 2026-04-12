package com.forlks.personal_wellness_routine.data.db.dao

import androidx.room.*
import com.forlks.personal_wellness_routine.data.db.entity.DailyHealthScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHealthScoreDao {

    @Upsert
    suspend fun upsert(entity: DailyHealthScoreEntity)

    @Query("SELECT * FROM daily_health_score WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyHealthScoreEntity?

    @Query("SELECT * FROM daily_health_score WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DailyHealthScoreEntity?>

    @Query("SELECT * FROM daily_health_score ORDER BY date DESC LIMIT 7")
    fun getLast7Days(): Flow<List<DailyHealthScoreEntity>>

    @Query("SELECT * FROM daily_health_score ORDER BY date DESC LIMIT 30")
    fun getLast30Days(): Flow<List<DailyHealthScoreEntity>>

    @Query("SELECT * FROM daily_health_score WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    suspend fun getRange(from: String, to: String): List<DailyHealthScoreEntity>

    @Query("SELECT * FROM daily_health_score ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): DailyHealthScoreEntity?
}
