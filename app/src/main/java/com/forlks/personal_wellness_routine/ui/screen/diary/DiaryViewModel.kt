package com.forlks.personal_wellness_routine.ui.screen.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.db.entity.AnalysisSummaryEntity
import com.forlks.personal_wellness_routine.data.repository.AnalysisRepository
import com.forlks.personal_wellness_routine.data.repository.DiaryRepository
import com.forlks.personal_wellness_routine.data.repository.RoutineRepository
import com.forlks.personal_wellness_routine.data.repository.WellnessPointRepository
import com.forlks.personal_wellness_routine.domain.model.DiaryEntry
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
    private val routineRepository: RoutineRepository
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
                wellnessPointRepository.earnPoints(
                    eventType = WpEvent.DIARY,
                    description = "일기 작성 (+3 WP)"
                )
                saveAnalysisSnapshot(scoredEntry)
            }

            _uiState.update { it.copy(currentEntry = scoredEntry) }
        }
    }

    private suspend fun saveAnalysisSnapshot(entry: DiaryEntry) {
        val (routineCompleted, routineTotal) = routineRepository.getTodayStats()
        val routineScore = RoutineAchievementCalculator.dailyScore(routineCompleted, routineTotal)

        val existing = analysisRepository.getByDate(entry.date)
        val snapshot = AnalysisSummaryEntity(
            id = existing?.id ?: 0,
            date = entry.date,
            routineTotal = routineTotal,
            routineCompleted = routineCompleted,
            routineScore = routineScore,
            routineGrade = RoutineAchievementCalculator.gradeFrom(routineScore),
            diaryCharCount = entry.content.length,
            diaryEmotionLabel = entry.emotionLabel,
            diaryEmotionScore = entry.emotionScore,
            mindHealthScore = -1
        )
        analysisRepository.upsert(snapshot)
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
        val positiveWords = setOf(
            "좋아", "행복", "기쁘", "사랑", "감사", "고마워", "설레", "신나", "재미",
            "즐거", "최고", "멋있", "대단", "훌륭", "완벽", "보람", "뿌듯", "희망",
            "긍정", "웃음", "밝", "활기", "성공", "달성", "건강", "평화", "여유"
        )
        val negativeWords = setOf(
            "싫어", "힘들", "슬프", "우울", "화나", "짜증", "최악", "실망", "미워",
            "지쳐", "아파", "걱정", "불안", "무서", "후회", "억울", "외로", "피곤",
            "스트레스", "괴롭", "두렵", "실패", "좌절", "포기", "답답", "막막"
        )
        var pos = 0; var neg = 0
        positiveWords.forEach { if (content.contains(it)) pos++ }
        negativeWords.forEach { if (content.contains(it)) neg++ }
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
