package com.forlks.personal_wellness_routine.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,       // HEALTH, MIND, STUDY
    val emoji: String,
    val scheduledTime: String,  // "HH:mm" or "allday"
    val durationMinutes: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
