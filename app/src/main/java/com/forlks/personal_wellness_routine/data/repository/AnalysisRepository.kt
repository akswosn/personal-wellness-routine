package com.forlks.personal_wellness_routine.data.repository

import com.forlks.personal_wellness_routine.data.db.dao.AnalysisSummaryDao
import com.forlks.personal_wellness_routine.data.db.entity.AnalysisSummaryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalysisRepository @Inject constructor(
    private val dao: AnalysisSummaryDao
) {
    fun getAll(): Flow<List<AnalysisSummaryEntity>> = dao.getAll()

    suspend fun upsert(entity: AnalysisSummaryEntity) = dao.upsert(entity)

    suspend fun getByDate(date: String): AnalysisSummaryEntity? = dao.getByDate(date)

    suspend fun getLast7Days(): List<AnalysisSummaryEntity> {
        val to = LocalDate.now().toString()
        val from = LocalDate.now().minusDays(6).toString()
        return dao.getRange(from, to)
    }

    suspend fun getLast30Days(): List<AnalysisSummaryEntity> {
        val from = LocalDate.now().minusDays(29).toString()
        return dao.getFrom(from)
    }

    suspend fun getByYearMonth(year: Int, month: Int): List<AnalysisSummaryEntity> {
        val from = java.time.YearMonth.of(year, month).atDay(1).toString()
        val to   = java.time.YearMonth.of(year, month).atEndOfMonth().toString()
        return dao.getRange(from, to)
    }

    suspend fun countPositiveDiaries(): Int = dao.countPositiveDiaries()
    suspend fun countTotalDiaries(): Int = dao.countTotalDiaries()
}
