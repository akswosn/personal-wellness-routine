package com.forlks.personal_wellness_routine.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.db.entity.DailyHealthScoreEntity
import com.forlks.personal_wellness_routine.data.preferences.AppPreferences
import com.forlks.personal_wellness_routine.data.repository.ChatRepository
import com.forlks.personal_wellness_routine.data.repository.DailyHealthRepository
import com.forlks.personal_wellness_routine.data.repository.DiaryRepository
import com.forlks.personal_wellness_routine.data.repository.RoutineRepository
import com.forlks.personal_wellness_routine.data.repository.WellnessPointRepository
import com.forlks.personal_wellness_routine.domain.model.CharacterState
import com.forlks.personal_wellness_routine.domain.model.CharacterType
import com.forlks.personal_wellness_routine.domain.model.WpEvent
import com.forlks.personal_wellness_routine.domain.model.calculateCharacterLevel
import com.forlks.personal_wellness_routine.domain.model.nextLevelWp
import com.forlks.personal_wellness_routine.util.DailyHealthCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val todayCompleted: Int = 0,
    val todayTotal: Int = 0,
    val streak: Int = 0,
    val emotionEmoji: String = "",
    val latestChatTemp: Float? = null,
    val characterState: CharacterState? = null,
    val dailyHealthScore: DailyHealthScoreEntity? = null  // 일 건강도
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val routineRepository: RoutineRepository,
    private val diaryRepository: DiaryRepository,
    private val chatRepository: ChatRepository,
    private val wellnessPointRepository: WellnessPointRepository,
    private val dailyHealthRepository: DailyHealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeUserName()
        loadTodayStats()
        loadStreak()
        loadLatestChatAnalysis()
        observeWellnessPoints()
        observeDailyHealthScore()
    }

    private fun observeUserName() {
        viewModelScope.launch {
            appPreferences.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }
    }

    private fun loadTodayStats() {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val (completed, total) = routineRepository.getTodayStats()
            val todayDiary = diaryRepository.getDiaryForDate(today)
            _uiState.update {
                it.copy(
                    todayCompleted = completed,
                    todayTotal = total,
                    emotionEmoji = todayDiary?.emotionEmoji ?: ""
                )
            }
        }
    }

    private fun loadStreak() {
        viewModelScope.launch {
            val streak = routineRepository.getStreak()
            _uiState.update { it.copy(streak = streak) }
        }
    }

    private fun loadLatestChatAnalysis() {
        viewModelScope.launch {
            val latest = chatRepository.getLatestAnalysis()
            _uiState.update { it.copy(latestChatTemp = latest?.temperature) }
        }
    }

    private fun observeWellnessPoints() {
        viewModelScope.launch {
            combine(
                wellnessPointRepository.totalPoints,
                appPreferences.characterType,
                appPreferences.characterName
            ) { totalWp, charTypeStr, charName ->
                Triple(totalWp, charTypeStr, charName)
            }.collect { (totalWp, charTypeStr, charName) ->
                val charType = try {
                    CharacterType.valueOf(charTypeStr)
                } catch (_: IllegalArgumentException) {
                    CharacterType.CAT
                }
                val todayWp = wellnessPointRepository.getTodayPoints()
                val (level, levelName) = calculateCharacterLevel(totalWp)
                val nextWp = nextLevelWp(totalWp)
                val characterState = CharacterState(
                    type = charType,
                    name = charName,
                    totalWp = totalWp,
                    level = level,
                    levelName = levelName,
                    nextLevelWp = nextWp,
                    todayWp = todayWp
                )
                _uiState.update { it.copy(characterState = characterState) }
            }
        }
    }

    private fun observeDailyHealthScore() {
        viewModelScope.launch {
            dailyHealthRepository.observeToday().collect { entity ->
                _uiState.update { it.copy(dailyHealthScore = entity) }
            }
        }
    }

    fun checkIn(emoji: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(emotionEmoji = emoji) }
            // 기분 체크인 → moodScore 업데이트
            val moodScore = DailyHealthCalculator.moodEmojiToScore(emoji)
            dailyHealthRepository.updateToday(moodScore = moodScore)
        }
    }

    fun earnAttendance() {
        viewModelScope.launch {
            wellnessPointRepository.earnPoints(
                eventType = WpEvent.ATTENDANCE,
                description = "오늘의 출석 체크"
            )
            val streak = routineRepository.getStreak()
            _uiState.update { it.copy(streak = streak) }
            if (streak > 0 && streak % 7 == 0) {
                wellnessPointRepository.earnPoints(
                    eventType = WpEvent.STREAK_7,
                    description = "${streak}일 연속 출석 보너스"
                )
            }
            if (streak > 0 && streak % 30 == 0) {
                wellnessPointRepository.earnPoints(
                    eventType = WpEvent.STREAK_30,
                    description = "${streak}일 연속 출석 보너스"
                )
            }
        }
    }

    fun refresh() {
        loadTodayStats()
        loadStreak()
        loadLatestChatAnalysis()
    }
}
