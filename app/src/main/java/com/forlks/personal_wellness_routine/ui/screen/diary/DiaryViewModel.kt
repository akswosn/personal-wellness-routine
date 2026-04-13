package com.forlks.personal_wellness_routine.ui.screen.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.db.entity.AnalysisSummaryEntity
import com.forlks.personal_wellness_routine.data.repository.AnalysisRepository
import com.forlks.personal_wellness_routine.data.repository.DailyHealthRepository
import com.forlks.personal_wellness_routine.data.repository.DiaryRepository
import com.forlks.personal_wellness_routine.data.repository.RoutineRepository
import com.forlks.personal_wellness_routine.data.repository.WellnessPointRepository
import com.forlks.personal_wellness_routine.domain.model.DiaryEntry
import com.forlks.personal_wellness_routine.domain.model.WpEvent
import com.forlks.personal_wellness_routine.util.DailyHealthCalculator
import com.forlks.personal_wellness_routine.util.MindHealthCalculator
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

data class DiaryUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val selectedDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
    val currentEntry: DiaryEntry? = null
)

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val wellnessPointRepository: WellnessPointRepository,
    private val analysisRepository: AnalysisRepository,
    private val routineRepository: RoutineRepository,
    private val dailyHealthRepository: DailyHealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        observeAllDiaries()
        loadEntryForDate(_uiState.value.selectedDate)
    }

    private fun observeAllDiaries() {
        viewModelScope.launch {
            diaryRepository.getAllDiaries().collect { entries ->
                _uiState.update { it.copy(entries = entries) }
            }
        }
    }

    fun selectDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        loadEntryForDate(date)
    }

    private fun loadEntryForDate(date: String) {
        viewModelScope.launch {
            val entry = diaryRepository.getDiaryForDate(date)
            _uiState.update { it.copy(currentEntry = entry) }
        }
    }

    fun saveDiary(entry: DiaryEntry) {
        viewModelScope.launch {
            val emotion = analyzeSentiment(entry.content)
            val scoredEntry = entry.copy(
                emotionScore = emotion,
                emotionLabel = buildEmotionLabel(emotion)
            )
            diaryRepository.saveDiary(scoredEntry)

            // 50자 이상일 때만 WP 적립 + 분석 스냅샷 저장
            if (entry.content.length >= 50) {
                // 출석 WP 자동 적립 (하루 최초 1회)
                wellnessPointRepository.earnPoints(WpEvent.ATTENDANCE, "출석 체크 (+10 WP)")
                wellnessPointRepository.earnPoints(
                    eventType = WpEvent.DIARY,
                    description = "일기 작성 (+3 WP)"
                )
                saveAnalysisSnapshot(scoredEntry)
                updateDailyHealthScore(scoredEntry)
            }

            _uiState.update { it.copy(currentEntry = scoredEntry) }
        }
    }

    private suspend fun saveAnalysisSnapshot(entry: DiaryEntry) {
        val (routineCompleted, routineTotal) = routineRepository.getTodayStats()
        val routineScore = RoutineAchievementCalculator.dailyScore(routineCompleted, routineTotal)

        // 단어 기반 마음건강도
        val mindHealthScore = MindHealthCalculator.calculateFromText(entry.content)

        val existing = analysisRepository.getByDate(entry.date)
        val snapshot = AnalysisSummaryEntity(
            id                = existing?.id ?: 0,
            date              = entry.date,
            routineTotal      = routineTotal,
            routineCompleted  = routineCompleted,
            routineScore      = routineScore,
            routineGrade      = RoutineAchievementCalculator.gradeFrom(routineScore),
            diaryCharCount    = entry.content.length,
            diaryEmotionLabel = entry.emotionLabel,
            diaryEmotionScore = entry.emotionScore,
            mindHealthScore   = mindHealthScore
        )
        analysisRepository.upsert(snapshot)
    }

    /** 일기 저장 후 DailyHealthScore의 diaryScore 항목 업데이트 (v0.0.2 레벨 기반) */
    private suspend fun updateDailyHealthScore(entry: DiaryEntry) {
        val mindHealth = MindHealthCalculator.calculateFromText(entry.content)
        if (mindHealth < 0) return // 데이터 부족 → 스킵

        // 원점수 → 레벨(1~5) → 서브점수(5/10/15/20/25)
        val level = MindHealthCalculator.scoreToLevel(mindHealth)
        val diaryScore = MindHealthCalculator.levelToSubScore(level)
        dailyHealthRepository.updateToday(diaryScore = diaryScore)
    }

    fun deleteDiary(entry: DiaryEntry) {
        viewModelScope.launch {
            diaryRepository.deleteDiary(entry)
            if (entry.date == _uiState.value.selectedDate) {
                _uiState.update { it.copy(currentEntry = null) }
            }
        }
    }

    private fun analyzeSentiment(content: String): Float {
        var pos = 0; var neg = 0
        MindHealthCalculator.POSITIVE_WORDS.forEach { if (content.contains(it)) pos++ }
        MindHealthCalculator.NEGATIVE_WORDS.forEach { if (content.contains(it)) neg++ }
        val total = pos + neg
        return if (total == 0) 0f
        else ((pos - neg).toFloat() / total.toFloat()).coerceIn(-1f, 1f)
    }

    private fun buildEmotionLabel(score: Float): String = when {
        score >= 0.5f  -> "기쁨"
        score >= 0.2f  -> "행복"
        score >= 0.05f -> "감사"
        score > -0.05f -> "보통"
        score > -0.2f  -> "걱정"
        score > -0.5f  -> "슬픔"
        else           -> "우울"
    }
}
