package com.forlks.personal_wellness_routine.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_history",
    foreignKeys = [ForeignKey(
        entity = RoutineEntity::class,
        parentColumns = ["id"],
        childColumns = ["routineId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("routineId")]
)
data class RoutineHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val completedDate: String,  // "yyyy-MM-dd"
    val completedAt: Long = System.currentTimeMillis()
)
