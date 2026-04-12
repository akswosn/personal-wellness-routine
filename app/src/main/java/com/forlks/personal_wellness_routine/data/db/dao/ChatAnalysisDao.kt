package com.forlks.personal_wellness_routine.data.db.dao

import androidx.room.*
import com.forlks.personal_wellness_routine.data.db.entity.ChatAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatAnalysisDao {
    @Query("SELECT * FROM chat_analyses ORDER BY analyzedAt DESC")
    fun getAllAnalyses(): Flow<List<ChatAnalysisEntity>>

    @Query("SELECT * FROM chat_analyses ORDER BY analyzedAt DESC LIMIT 1")
    suspend fun getLatestAnalysis(): ChatAnalysisEntity?

    @Query("SELECT * FROM chat_analyses ORDER BY analyzedAt DESC LIMIT :limit")
    suspend fun getRecentAnalyses(limit: Int): List<ChatAnalysisEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: ChatAnalysisEntity): Long

    @Delete
    suspend fun deleteAnalysis(analysis: ChatAnalysisEntity)
}
