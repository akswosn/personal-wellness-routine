package com.forlks.personal_wellness_routine.data.db.dao

import androidx.room.*
import com.forlks.personal_wellness_routine.data.db.entity.AnalysisSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisSummaryDao {

    @Upsert
    suspend fun upsert(entity: AnalysisSummaryEntity)

    @Query("SELECT * FROM analysis_summary ORDER BY date DESC")
    fun getAll(): Flow<List<AnalysisSummaryEntity>>

    @Query("SELECT * FROM analysis_summary WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): AnalysisSummaryEntity?

    /** 최근 7일 */
    @Query("""
        SELECT * FROM analysis_summary
        WHERE date >= :from AND date <= :to
        ORDER BY date ASC
    """)
    suspend fun getRange(from: String, to: String): List<AnalysisSummaryEntity>

    /** 긍정 일기 수 (emotionScore > 0) — 마음 건강도 계산용 */
    @Query("SELECT COUNT(*) FROM analysis_summary WHERE diaryEmotionScore > 0 AND diaryCharCount >= 50")
    suspend fun countPositiveDiaries(): Int

    /** 전체 일기 수 (50자 이상) */
    @Query("SELECT COUNT(*) FROM analysis_summary WHERE diaryCharCount >= 50")
    suspend fun countTotalDiaries(): Int

    /** 최근 28일 주차별 마음 건강도 집계 */
    @Query("""
        SELECT * FROM analysis_summary
        WHERE date >= :from
        ORDER BY date ASC
    """)
    suspend fun getFrom(from: String): List<AnalysisSummaryEntity>
}
