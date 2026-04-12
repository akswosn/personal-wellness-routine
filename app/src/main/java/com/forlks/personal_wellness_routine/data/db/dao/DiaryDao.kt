package com.forlks.personal_wellness_routine.data.db.dao

import androidx.room.*
import com.forlks.personal_wellness_routine.data.db.entity.DiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_entries WHERE date = :date LIMIT 1")
    suspend fun getDiaryForDate(date: String): DiaryEntity?

    @Query("SELECT * FROM diary_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getDiariesInRange(startDate: String, endDate: String): List<DiaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity): Long

    @Update
    suspend fun updateDiary(diary: DiaryEntity)

    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)

    @Query("SELECT AVG(emotionScore) FROM diary_entries WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageEmotionScore(startDate: String, endDate: String): Float?
}
