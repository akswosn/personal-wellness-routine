package com.forlks.personal_wellness_routine.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.forlks.personal_wellness_routine.data.db.dao.*
import com.forlks.personal_wellness_routine.data.db.entity.*

@Database(
    entities = [
        RoutineEntity::class,
        RoutineHistoryEntity::class,
        DiaryEntity::class,
        ChatAnalysisEntity::class,
        WellnessPointHistoryEntity::class,
        AnalysisSummaryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun diaryDao(): DiaryDao
    abstract fun chatAnalysisDao(): ChatAnalysisDao
    abstract fun wellnessPointDao(): WellnessPointDao
    abstract fun analysisSummaryDao(): AnalysisSummaryDao
}
