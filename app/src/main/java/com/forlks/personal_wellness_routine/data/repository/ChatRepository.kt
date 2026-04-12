package com.forlks.personal_wellness_routine.data.repository

import com.forlks.personal_wellness_routine.data.db.dao.ChatAnalysisDao
import com.forlks.personal_wellness_routine.data.db.entity.ChatAnalysisEntity
import com.forlks.personal_wellness_routine.domain.model.ChatAnalysisResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatAnalysisDao: ChatAnalysisDao,
    private val gson: Gson
) {
    fun getAllAnalyses(): Flow<List<ChatAnalysisResult>> =
        chatAnalysisDao.getAllAnalyses().map { list -> list.map { it.toDomain() } }

    suspend fun getLatestAnalysis(): ChatAnalysisResult? =
        chatAnalysisDao.getLatestAnalysis()?.toDomain()

    suspend fun getRecentAnalyses(limit: Int = 5): List<ChatAnalysisResult> =
        chatAnalysisDao.getRecentAnalyses(limit).map { it.toDomain() }

    suspend fun saveAnalysis(result: ChatAnalysisResult): Long =
        chatAnalysisDao.insertAnalysis(result.toEntity())

    private fun ChatAnalysisEntity.toDomain(): ChatAnalysisResult {
        val keywords: List<String> = try {
            gson.fromJson(topKeywords, object : TypeToken<List<String>>() {}.type)
        } catch (_: Exception) { emptyList() }
        return ChatAnalysisResult(
            id = id, fileName = fileName, periodStart = periodStart, periodEnd = periodEnd,
            totalMessages = totalMessages, positiveRatio = positiveRatio,
            neutralRatio = neutralRatio, negativeRatio = negativeRatio,
            temperature = temperature, temperatureLabel = temperatureLabel,
            temperatureEmoji = "🌡", relationshipScore = relationshipScore,
            topKeywords = keywords, autoDiaryDraft = autoDiaryDraft, analyzedAt = analyzedAt
        )
    }

    private fun ChatAnalysisResult.toEntity() = ChatAnalysisEntity(
        id = id, fileName = fileName, fileUri = "",
        periodStart = periodStart, periodEnd = periodEnd,
        totalMessages = totalMessages, positiveRatio = positiveRatio,
        neutralRatio = neutralRatio, negativeRatio = negativeRatio,
        temperature = temperature, temperatureLabel = temperatureLabel,
        relationshipScore = relationshipScore,
        topKeywords = gson.toJson(topKeywords),
        autoDiaryDraft = autoDiaryDraft
    )
}
