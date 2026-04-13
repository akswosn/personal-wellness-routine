package com.forlks.personal_wellness_routine.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 날짜별 카카오 대화 분석 결과 */
@Entity(tableName = "daily_chat_analyses")
data class DailyChatAnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatAnalysisId: Long,    // FK → chat_analyses.id
    val date: String,            // "yyyy-MM-dd"
    val totalMessages: Int,
    val positiveCount: Int,
    val negativeCount: Int,
    val neutralCount: Int,
    val temperature: Float,      // 0~100 (긍정/긍정+부정 * 100)
    val relationshipScore: Float // 0~100
)
