package com.forlks.personal_wellness_routine.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.db.entity.AnalysisSummaryEntity
import com.forlks.personal_wellness_routine.data.repository.AnalysisRepository
import com.forlks.personal_wellness_routine.data.repository.RoutineRepository
import com.forlks.personal_wellness_routine.data.repository.WellnessPointRepository
import com.forlks.personal_wellness_routine.domain.model.WpEvent
import com.forlks.personal_wellness_routine.util.RoutineAchievementCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AchievementUiState(
    val selectedTab: Int = 0,               // 0=일간 1=주간 2=월간
    val dailyScore: Int = 0,
    val weeklyScore: Int = 0,
    val monthlyScore: Int = 0,
    val currentScore: Int = 0,
    val currentGrade: String = "-",
    val barValues: List<Pair<String, Int>> = emptyList(),   // label to score
    val routineDetails: List<Triple<String, Int, Int>> = emptyList(), // name, completed, total
    val wpBonusMessage: String = "",
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
    private val dayLabel = DateTimeFormatter.ofPattern("M/d")

    init { loadData(tab = 0) }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
        loadData(tab)
    }

    private fun loadData(tab: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val today = LocalDate.now()
            val last7 = analysisRepository.getLast7Days()
            val last30 = analysisRepository.getLast30Days()

            // 일간
            val todaySummary = analysisRepository.getByDate(today.format(fmt))
            val dailyScore = todaySummary?.routineScore ?: run {
                val (completed, total) = routineRepository.getTodayStats()
                RoutineAchievementCalculator.dailyScore(completed, total)
            }

            // 주간 바 차트 (최근 7일)
            val weeklyBars = buildWeeklyBars(last7)
            val weeklyScore = RoutineAchievementCalculator.weeklyScore(weeklyBars.map { it.second })

            // 월간 바 차트 (최근 30일 → 4주 평균)
            val monthlyBars = buildMonthlyBars(last30, today)
            val monthlyScore = RoutineAchievementCalculator.monthlyScore(last30.map { it.routineScore })

            val (currentScore, barValues) = when (tab) {
                1    -> weeklyScore to weeklyBars
                2    -> monthlyScore to monthlyBars
                else -> dailyScore to listOf("오늘" to dailyScore)
            }

            val grade = RoutineAchievementCalculator.gradeFrom(currentScore)
            val wpMsg = buildWpMessage(tab, grade)

            _uiState.update {
                it.copy(
                    dailyScore = dailyScore,
                    weeklyScore = weeklyScore,
                    monthlyScore = monthlyScore,
                    currentScore = currentScore,
                    currentGrade = grade,
                    barValues = barValues,
                    wpBonusMessage = wpMsg,
                    isLoading = false
                )
            }
        }
    }

    private fun buildWeeklyBars(data: List<AnalysisSummaryEntity>): List<Pair<String, Int>> {
        val scoreMap = data.associate { it.date to it.routineScore }
        return (6 downTo 0).map { offset ->
            val date = LocalDate.now().minusDays(offset.toLong())
            val label = date.format(dayLabel)
            val score = scoreMap[date.format(fmt)] ?: 0
            label to score
        }
    }

    private fun buildMonthlyBars(
        data: List<AnalysisSummaryEntity>,
        today: LocalDate
    ): List<Pair<String, Int>> {
        return (3 downTo 0).map { weekOffset ->
            val weekEnd = today.minusWeeks(weekOffset.toLong())
            val weekStart = weekEnd.minusDays(6)
            val label = "${weekEnd.monthValue}/${weekEnd.dayOfMonth}주"
            val scores = data.filter {
                it.date >= weekStart.format(fmt) && it.date <= weekEnd.format(fmt)
            }.map { it.routineScore }
            label to (if (scores.isEmpty()) 0 else scores.average().toInt())
        }
    }

    private fun buildWpMessage(tab: Int, grade: String): String {
        return when {
            tab == 1 && (grade == "S" || grade == "A") -> {
                val bonus = RoutineAchievementCalculator.weeklyWpBonus(grade)
                "$grade 등급 달성! 이번 주 보너스 +$bonus WP 적립"
            }
            tab == 2 && grade == "S" -> {
                val bonus = RoutineAchievementCalculator.monthlyWpBonus(grade)
                "월간 S등급 달성! +$bonus WP 보너스"
            }
            else -> ""
        }
    }
}
