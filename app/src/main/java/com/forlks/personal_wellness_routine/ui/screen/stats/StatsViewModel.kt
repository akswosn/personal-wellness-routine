package com.forlks.personal_wellness_routine.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.db.entity.DailyHealthScoreEntity
import com.forlks.personal_wellness_routine.data.repository.ChatRepository
import com.forlks.personal_wellness_routine.data.repository.DailyHealthRepository
import com.forlks.personal_wellness_routine.data.repository.DiaryRepository
import com.forlks.personal_wellness_routine.data.repository.RoutineRepository
import com.forlks.personal_wellness_routine.domain.model.ChatAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DailyHealthSummary(
    val date: String,
    val label: String,          // 요일 or 날짜
    val level: Int,
    val totalScore: Float
)

data class StatsUiState(
    val weeklyData: List<Pair<String, Int>> = emptyList(),
    val monthlyAchievementRate: Float = 0f,
    val streak: Int = 0,
    val averageEmotion: Float = 0f,
    val latestChatAnalysis: ChatAnalysisResult? = null,
    val isLoading: Boolean = false,
    // 일 건강도 탭 데이터
    val todayHealthScore: DailyHealthScoreEntity? = null,
    val weeklyHealthData: List<DailyHealthSummary> = emptyList(),   // 주간 레벨 바차트
    val monthlyHealthData: List<DailyHealthSummary> = emptyList()   // 월간 캘린더 그리드
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val diaryRepository: DiaryRepository,
    private val chatRepository: ChatRepository,
    private val dailyHealthRepository: DailyHealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")

    init {
        loadStats()
        observeDailyHealthData()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadWeeklyData()
            loadStreak()
            loadAverageEmotion()
            loadLatestChatAnalysis()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun observeDailyHealthData() {
        // 오늘 건강도 실시간 관찰
        viewModelScope.launch {
            dailyHealthRepository.observeToday().collect { entity ->
                _uiState.update { it.copy(todayHealthScore = entity) }
            }
        }
        // 주간/월간은 초기 1회 로드 + loadStats 호출 시 갱신
        viewModelScope.launch {
            dailyHealthRepository.getLast7Days().collect { list ->
                val today = LocalDate.now()
                val monday = today.with(DayOfWeek.MONDAY)
                val dataMap = list.associateBy { it.date }

                val weekly = (0..6).map { offset ->
                    val date = monday.plusDays(offset.toLong())
                    val dateStr = date.format(fmt)
                    val entity = dataMap[dateStr]
                    DailyHealthSummary(
                        date       = dateStr,
                        label      = dayLabels[offset],
                        level      = entity?.level ?: 0,
                        totalScore = entity?.totalScore ?: 0f
                    )
                }
                _uiState.update { it.copy(weeklyHealthData = weekly) }
            }
        }
        viewModelScope.launch {
            dailyHealthRepository.getLast30Days().collect { list ->
                val today = LocalDate.now()
                val firstDay = today.withDayOfMonth(1)
                val daysInMonth = today.lengthOfMonth()
                val dataMap = list.associateBy { it.date }

                val monthly = (0 until daysInMonth).map { offset ->
                    val date = firstDay.plusDays(offset.toLong())
                    val dateStr = date.format(fmt)
                    val entity = dataMap[dateStr]
                    DailyHealthSummary(
                        date       = dateStr,
                        label      = (offset + 1).toString(),
                        level      = entity?.level ?: 0,
                        totalScore = entity?.totalScore ?: 0f
                    )
                }
                _uiState.update { it.copy(monthlyHealthData = monthly) }
            }
        }
    }

    private suspend fun loadWeeklyData() {
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        val sunday = monday.plusDays(6)
        val startDate = monday.format(fmt)
        val endDate = sunday.format(fmt)

        val weeklyStats = routineRepository.getWeeklyStats(startDate, endDate)
        val statsMap = weeklyStats.associate { it.completedDate to it.count }

        val weeklyData = (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            val count = statsMap[date.format(fmt)] ?: 0
            dayLabels[offset] to count
        }

        val firstOfMonth = today.withDayOfMonth(1)
        val daysElapsed = today.dayOfMonth
        var daysWithCompletion = 0
        for (d in 0 until daysElapsed) {
            val date = firstOfMonth.plusDays(d.toLong()).format(fmt)
            if ((statsMap[date] ?: 0) > 0) daysWithCompletion++
        }
        val monthlyRate = if (daysElapsed > 0) daysWithCompletion.toFloat() / daysElapsed else 0f

        _uiState.update {
            it.copy(weeklyData = weeklyData, monthlyAchievementRate = monthlyRate)
        }
    }

    private suspend fun loadStreak() {
        val streak = routineRepository.getStreak()
        _uiState.update { it.copy(streak = streak) }
    }

    private suspend fun loadAverageEmotion() {
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        val sunday = monday.plusDays(6)
        val avgEmotion = diaryRepository.getAverageEmotionScore(monday.format(fmt), sunday.format(fmt))
        _uiState.update { it.copy(averageEmotion = avgEmotion) }
    }

    private suspend fun loadLatestChatAnalysis() {
        val latest = chatRepository.getLatestAnalysis()
        _uiState.update { it.copy(latestChatAnalysis = latest) }
    }
}
