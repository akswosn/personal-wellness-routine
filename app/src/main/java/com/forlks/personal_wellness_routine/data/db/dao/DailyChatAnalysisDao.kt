package com.forlks.personal_wellness_routine.data.db.dao

import androidx.room.*
import com.forlks.personal_wellness_routine.data.db.entity.DailyChatAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyChatAnalysisDao {
    @Query("SELECT * FROM daily_chat_analyses WHERE chatAnalysisId = :chatAnalysisId ORDER BY date ASC")
    suspend fun getByAnalysisId(chatAnalysisId: Long): List<DailyChatAnalysisEntity>

    @Query("SELECT * FROM daily_chat_analyses WHERE date = :date")
    suspend fun getByDate(date: String): List<DailyChatAnalysisEntity>

    @Query("SELECT DISTINCT date FROM daily_chat_analyses ORDER BY date ASC")
    suspend fun getAllDates(): List<String>

    @Query("SELECT * FROM daily_chat_analyses ORDER BY date DESC")
    fun getAllFlow(): Flow<List<DailyChatAnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DailyChatAnalysisEntity>)

    @Query("DELETE FROM daily_chat_analyses WHERE chatAnalysisId = :chatAnalysisId")
    suspend fun deleteByAnalysisId(chatAnalysisId: Long)
}
