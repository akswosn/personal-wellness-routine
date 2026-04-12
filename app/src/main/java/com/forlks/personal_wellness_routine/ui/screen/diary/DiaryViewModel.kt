package com.forlks.personal_wellness_routine.ui.screen.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.repository.DiaryRepository
import com.forlks.personal_wellness_routine.data.repository.WellnessPointRepository
import com.forlks.personal_wellness_routine.domain.model.DiaryEntry
import com.forlks.personal_wellness_routine.domain.model.WpEvent
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
    private val wellnessPointRepository: WellnessPointRepository
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
            val scoredEntry = entry.copy(
                emotionScore = analyzeSentiment(entry.content)
            )
            diaryRepository.saveDiary(scoredEntry)
            wellnessPointRepository.earnPoints(
                eventType = WpEvent.DIARY,
                description = "일기 작성"
            )
            _uiState.update { it.copy(currentEntry = scoredEntry) }
        }
    }

    fun deleteDiary(entry: DiaryEntry) {
        viewModelScope.launch {
            diaryRepository.deleteDiary(entry)
            val currentDate = _uiState.value.selectedDate
            if (entry.date == currentDate) {
                _uiState.update { it.copy(currentEntry = null) }
            }
        }
    }

    /**
     * Simple on-device sentiment analysis.
     * Returns a score in [-1.0, 1.0]:
     *   positive keywords push score up, negative keywords push score down.
     */
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

        var positiveCount = 0
        var negativeCount = 0

        positiveWords.forEach { word -> if (content.contains(word)) positiveCount++ }
        negativeWords.forEach { word -> if (content.contains(word)) negativeCount++ }

        val total = positiveCount + negativeCount
        return if (total == 0) {
            0f
        } else {
            ((positiveCount - negativeCount).toFloat() / total.toFloat()).coerceIn(-1f, 1f)
        }
    }
}
