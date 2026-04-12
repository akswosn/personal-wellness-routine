package com.forlks.personal_wellness_routine.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wellness_point_history")
data class WellnessPointHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,           // "yyyy-MM-dd"
    val eventType: String,      // ATTENDANCE, ROUTINE, DIARY, CHAT_ANALYSIS, STREAK_7, STREAK_30
    val points: Int,
    val description: String,
    val earnedAt: Long = System.currentTimeMillis()
)
