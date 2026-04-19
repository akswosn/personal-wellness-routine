package com.forlks.personal_wellness_routine.ui.screen.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.repository.AnalysisRepository
import com.forlks.personal_wellness_routine.util.MindHealthCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MindHealthUiState(
    // 캘린더
    val displayYearMonth: YearMonth = YearMonth.now(),
    val calendarData: Map<String, Int> = emptyMap(),  // date → mindHealthScore (0-100, -1=없음)
    val selectedDate: String? = null,
    val selectedDayScore: Int = -1,
    val selectedDayEmoji: String = "",
    // 월간 요약
    val score: Int = -1,
    val level: String = "분석중",
    val levelEmoji: String = "🔍",
    val insight: String = "",
    val showCounselingBanner: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class MindHealthViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MindHealthUiState())
    val uiState: StateFlow<MindHealthUiState> = _uiState.asStateFlow()

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init { loadMonth(YearMonth.now()) }

    fun prevMonth() {
        val ym = _uiState.value.displayYearMonth.minusMonths(1)
        _uiState.update { it.copy(displayYearMonth = ym, selectedDate = null) }
        loadMonth(ym)
    }

    fun nextMonth() {
        val ym = _uiState.value.displayYearMonth.plusMonths(1)
        if (ym <= YearMonth.now()) {
            _uiState.update { it.copy(displayYearMonth = ym, selectedDate = null) }
            loadMonth(ym)
        }
    }

    fun selectDate(date: String) {
        val current = _uiState.value
        if (current.selectedDate == date) {
            _uiState.update { it.copy(selectedDate = null, selectedDayScore = -1, selectedDayEmoji = "") }
        } else {
            val score = current.calendarData[date] ?: -1
            val emoji = if (score >= 0) MindHealthCalculator.levelEmoji(score) else ""
            _uiState.update { it.copy(selectedDate = date, selectedDayScore = score, selectedDayEmoji = emoji) }
        }
    }

    fun loadData() = loadMonth(_uiState.value.displayYearMonth)

    private fun loadMonth(ym: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val data = analysisRepository.getByYearMonth(ym.year, ym.monthValue)

            // 날짜별 마음건강도 맵
            val calendarMap = data
                .filter { it.diaryCharCount >= 50 }
                .associate { it.date to it.mindHealthScore }

            // 월 평균 점수
            val validScores = calendarMap.values.filter { it >= 0 }
            val avgScore = if (validScores.isEmpty()) -1
                           else validScores.average().toInt().coerceIn(0, 100)

            _uiState.update {
                it.copy(
                    calendarData = calendarMap,
                    score = avgScore,
                    level = MindHealthCalculator.level(avgScore),
                    levelEmoji = MindHealthCalculator.levelEmoji(avgScore),
                    insight = MindHealthCalculator.insight(avgScore),
                    showCounselingBanner = MindHealthCalculator.shouldShowCounselingBanner(avgScore),
                    isLoading = false
                )
            }
        }
    }
}
