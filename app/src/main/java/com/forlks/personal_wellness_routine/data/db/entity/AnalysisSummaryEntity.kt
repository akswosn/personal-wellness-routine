package com.forlks.personal_wellness_routine.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_summary")
data class AnalysisSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,                   // yyyy-MM-dd
    val routineTotal: Int,
    val routineCompleted: Int,
    val routineScore: Int,              // 0~100
    val routineGrade: String,           // S/A/B/C/D
    val diaryCharCount: Int,
    val diaryEmotionLabel: String,
    val diaryEmotionScore: Float,
    val mindHealthScore: Int,           // 0~100 (긍정일기/전체일기 × 100)
    val createdAt: Long = System.currentTimeMillis()
)
