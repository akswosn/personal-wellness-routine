package com.forlks.personal_wellness_routine.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.repository.ChatRepository
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

data class StatsUiState(
    val weeklyData: List<Pair<String, Int>> = emptyList(),  // (dayLabel, completedCount)
    val monthlyAchievementRate: Float = 0f,
    val streak: Int = 0,
    val averageEmotion: Float = 0f,
    val latestChatAnalysis: ChatAnalysisResult? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val diaryRepository: DiaryRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadStats()
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

    private suspend fun loadWeeklyData() {
        val today = LocalDate.now()
        // Find Monday of the current week
        val monday = today.with(DayOfWeek.MONDAY)
        val sunday = monday.plusDays(6)

        val startDate = monday.format(fmt)
        val endDate = sunday.format(fmt)

        val weeklyStats = routineRepository.getWeeklyStats(startDate, endDate)

        // Build a map of date -> completedCount from DB results
        val statsMap = weeklyStats.associate { it.completedDate to it.count }

        // Build ordered list Mon-Sun with Korean day labels
        val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
        val weeklyData = (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            val label = dayLabels[offset]
            val count = statsMap[date.format(fmt)] ?: 0
            label to count
        }

        // Monthly achievement rate: days with at least 1 completion / total days this month
        val firstOfMonth = today.withDayOfMonth(1)
        val daysElapsed = today.dayOfMonth
        var daysWithCompletion = 0
        for (d in 0 until daysElapsed) {
            val date = firstOfMonth.plusDays(d.toLong()).format(fmt)
            if ((statsMap[date] ?: 0) > 0) daysWithCompletion++
        }
        val monthlyRate = if (daysElapsed > 0) daysWithCompletion.toFloat() / daysElapsed else 0f

        _uiState.update {
            it.copy(
                weeklyData = weeklyData,
                monthlyAchievementRate = monthlyRate
            )
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
        val avgEmotion = diaryRepository.getAverageEmotionScore(
            monday.format(fmt),
            sunday.format(fmt)
        )
        _uiState.update { it.copy(averageEmotion = avgEmotion) }
    }

    private suspend fun loadLatestChatAnalysis() {
        val latest = chatRepository.getLatestAnalysis()
        _uiState.update { it.copy(latestChatAnalysis = latest) }
    }
}
