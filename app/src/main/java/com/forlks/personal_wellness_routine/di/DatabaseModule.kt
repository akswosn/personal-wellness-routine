package com.forlks.personal_wellness_routine.di

import android.content.Context
import androidx.room.Room
import com.forlks.personal_wellness_routine.data.db.AppDatabase
import com.forlks.personal_wellness_routine.data.db.dao.*
import com.forlks.personal_wellness_routine.data.db.dao.DailyHealthScoreDao
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wellflow.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideRoutineDao(db: AppDatabase): RoutineDao = db.routineDao()
    @Provides fun provideDiaryDao(db: AppDatabase): DiaryDao = db.diaryDao()
    @Provides fun provideChatAnalysisDao(db: AppDatabase): ChatAnalysisDao = db.chatAnalysisDao()
    @Provides fun provideWellnessPointDao(db: AppDatabase): WellnessPointDao = db.wellnessPointDao()
    @Provides fun provideAnalysisSummaryDao(db: AppDatabase): AnalysisSummaryDao = db.analysisSummaryDao()
    @Provides fun provideDailyHealthScoreDao(db: AppDatabase): DailyHealthScoreDao = db.dailyHealthScoreDao()
    @Provides fun provideDailyChatAnalysisDao(db: AppDatabase): DailyChatAnalysisDao = db.dailyChatAnalysisDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
