package com.forlks.personal_wellness_routine.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 일 건강도 (Daily Health Score)
 *
 * 각 항목은 null = 오늘 미기록
 * totalScore = null 제외 후 가중 비율 재계산 (0~100)
 *
 * 가중치 원본:
 *   moodScore     최대 20pt  (오늘기분)
 *   routineScore  최대 25pt  (루틴달성도)
 *   diaryScore    최대 25pt  (마음건강도)
 *   chatTempScore 최대 20pt  (카카오온도)
 *   relationScore 최대 10pt  (관계건강도)
 */
@Entity(tableName = "daily_health_score")
data class DailyHealthScoreEntity(
    @PrimaryKey val date: String,          // "yyyy-MM-dd"
    val moodScore: Float? = null,          // 0~20
    val routineScore: Float? = null,       // 0~25
    val diaryScore: Float? = null,         // 0~25
    val chatTempScore: Float? = null,      // 0~20
    val relationScore: Float? = null,      // 0~10
    val totalScore: Float = 0f,            // 0~100 (재산정)
    val level: Int = 1,                    // 1~5
    val updatedAt: Long = System.currentTimeMillis()
)
