package com.forlks.personal_wellness_routine.ui.screen.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.db.entity.AnalysisSummaryEntity
import com.forlks.personal_wellness_routine.data.repository.AnalysisRepository
import com.forlks.personal_wellness_routine.util.MindHealthCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MindHealthUiState(
    val score: Int = -1,                            // -1 = 데이터 부족
    val level: String = "분석중",
    val levelEmoji: String = "🔍",
    val insight: String = "",
    val showCounselingBanner: Boolean = false,
    val weeklyTrend: List<Pair<String, Int>> = emptyList(), // 4주차 추이
    val emotionDistribution: List<Pair<String, Float>> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MindHealthViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MindHealthUiState())
    val uiState: StateFlow<MindHealthUiState> = _uiState.asStateFlow()

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val positive = analysisRepository.countPositiveDiaries()
            val total = analysisRepository.countTotalDiaries()
            val score = MindHealthCalculator.calculate(positive, total)

            val last30 = analysisRepository.getLast30Days()
            val weeklyTrend = buildWeeklyTrend(last30)
            val emotionDist = buildEmotionDistribution(last30)

            _uiState.update {
                it.copy(
                    score = score,
                    level = MindHealthCalculator.level(score),
                    levelEmoji = MindHealthCalculator.levelEmoji(score),
                    insight = MindHealthCalculator.insight(score),
                    showCounselingBanner = MindHealthCalculator.shouldShowCounselingBanner(score),
                    weeklyTrend = weeklyTrend,
                    emotionDistribution = emotionDist,
                    isLoading = false
                )
            }
        }
    }

    private fun buildWeeklyTrend(data: List<AnalysisSummaryEntity>): List<Pair<String, Int>> {
        val today = LocalDate.now()
        return (3 downTo 0).map { weekOffset ->
            val weekEnd = today.minusWeeks(weekOffset.toLong())
            val weekStart = weekEnd.minusDays(6)
            val label = "${4 - weekOffset}주차"
            val week = data.filter {
                it.date >= weekStart.format(fmt) && it.date <= weekEnd.format(fmt)
            }
            val pos = week.count { it.diaryEmotionScore > 0 && it.diaryCharCount >= 50 }
            val tot = week.count { it.diaryCharCount >= 50 }
            val weekScore = if (tot >= 2) MindHealthCalculator.calculate(pos, tot) else 0
            label to weekScore
        }
    }

    private fun buildEmotionDistribution(data: List<AnalysisSummaryEntity>): List<Pair<String, Float>> {
        val valid = data.filter { it.diaryCharCount >= 50 }
        if (valid.isEmpty()) return emptyList()
        val joy = valid.count { it.diaryEmotionLabel.contains("기쁨") || it.diaryEmotionLabel.contains("행복") }
        val gratitude = valid.count { it.diaryEmotionLabel.contains("감사") }
        val neutral = valid.count { it.diaryEmotionLabel.contains("보통") || it.diaryEmotionLabel.contains("중립") }
        val sadness = valid.count { it.diaryEmotionLabel.contains("슬픔") || it.diaryEmotionLabel.contains("우울") }
        val total = valid.size.toFloat()
        return listOf(
            "😊 기쁨" to (joy / total),
            "🙏 감사" to (gratitude / total),
            "😐 중립" to (neutral / total),
            "😢 슬픔" to (sadness / total)
        ).filter { it.second > 0f }
    }
}
