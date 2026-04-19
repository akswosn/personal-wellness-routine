package com.forlks.personal_wellness_routine.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.repository.AnalysisRepository
import com.forlks.personal_wellness_routine.data.repository.RoutineRepository
import com.forlks.personal_wellness_routine.data.repository.WellnessPointRepository
import com.forlks.personal_wellness_routine.util.RoutineAchievementCalculator
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

data class AchievementUiState(
    // 캘린더
    val displayYearMonth: YearMonth = YearMonth.now(),
    val calendarData: Map<String, Int> = emptyMap(),   // date("yyyy-MM-dd") → routineScore (0-100, -1=없음)
    val selectedDate: String? = null,
    val selectedDayScore: Int = -1,
    val selectedDayGrade: String = "-",
    // 월간 요약
    val monthlyScore: Int = 0,
    val monthlyGrade: String = "-",
    val isLoading: Boolean = true
)

@HiltViewModel
class RoutineAchievementViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val analysisRepository: AnalysisRepository,
    private val wellnessPointRepository: WellnessPointRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

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
            _uiState.update { it.copy(selectedDate = null, selectedDayScore = -1, selectedDayGrade = "-") }
        } else {
            val score = current.calendarData[date] ?: -1
            val grade = if (score >= 0) RoutineAchievementCalculator.gradeFrom(score) else "-"
            _uiState.update { it.copy(selectedDate = date, selectedDayScore = score, selectedDayGrade = grade) }
        }
    }

    private fun loadMonth(ym: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val data = analysisRepository.getByYearMonth(ym.year, ym.monthValue)
            val calendarMap = data.associate { it.date to it.routineScore }

            // 이번 달인 경우 오늘 데이터를 실시간으로 보완
            val enriched = if (ym == YearMonth.now()) {
                val today = LocalDate.now().format(fmt)
                if (!calendarMap.containsKey(today)) {
                    val (completed, total) = routineRepository.getTodayStats()
                    val todayScore = RoutineAchievementCalculator.dailyScore(completed, total)
                    calendarMap + (today to todayScore)
                } else calendarMap
            } else calendarMap

            val scores = enriched.values.filter { it >= 0 }
            val monthScore = if (scores.isEmpty()) 0 else scores.average().toInt()
            val monthGrade = RoutineAchievementCalculator.gradeFrom(monthScore)

            _uiState.update {
                it.copy(
                    calendarData = enriched,
                    monthlyScore = monthScore,
                    monthlyGrade = monthGrade,
                    isLoading = false
                )
            }
        }
    }

    // 탭 기반 selectTab — 하위 호환성 유지 (StatsScreen에서 호출 가능성)
    fun selectTab(tab: Int) { /* no-op: 캘린더 UI로 전환 완료 */ }
}
