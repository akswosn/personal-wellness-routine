package com.forlks.personal_wellness_routine.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_analyses")
data class ChatAnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val fileUri: String,
    val periodStart: String,    // "yyyy-MM-dd"
    val periodEnd: String,      // "yyyy-MM-dd"
    val totalMessages: Int,
    val positiveRatio: Float,   // 0.0~1.0
    val neutralRatio: Float,
    val negativeRatio: Float,
    val temperature: Float,     // 0~100
    val temperatureLabel: String, // 냉랭함/차가움/보통/따뜻함
    val relationshipScore: Float, // 0~100
    val topKeywords: String,    // JSON array string
    val autoDiaryDraft: String,
    val analyzedAt: Long = System.currentTimeMillis()
)
