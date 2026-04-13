package com.forlks.personal_wellness_routine.ui.screen.kakao

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.repository.ChatRepository
import com.forlks.personal_wellness_routine.data.repository.DailyHealthRepository
import com.forlks.personal_wellness_routine.data.repository.WellnessPointRepository
import com.forlks.personal_wellness_routine.domain.model.ChatAnalysisResult
import com.forlks.personal_wellness_routine.domain.model.DailyChatResult
import com.forlks.personal_wellness_routine.domain.model.WpEvent
import com.forlks.personal_wellness_routine.util.KakaoParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class KakaoUiState(
    val recentFiles: List<ChatAnalysisResult> = emptyList(),
    val isAnalyzing: Boolean = false,
    val analysisProgress: Float = 0f,
    val progressCurrentDay: Int = 0,
    val progressTotalDays: Int = 0,
    val currentAnalysis: ChatAnalysisResult? = null,
    val currentDailyResults: List<DailyChatResult> = emptyList(),
    val calendarData: Map<String, DailyChatResult> = emptyMap(), // date → merged result
    val error: String? = null
)

@HiltViewModel
class KakaoViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val wellnessPointRepository: WellnessPointRepository,
    private val dailyHealthRepository: DailyHealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KakaoUiState())
    val uiState: StateFlow<KakaoUiState> = _uiState.asStateFlow()

    init {
        loadRecentAnalyses()
        loadCalendarData()
    }

    fun analyzeFile(context: Context, uri: Uri, fileName: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    analysisProgress = 0f,
                    progressCurrentDay = 0,
                    progressTotalDays = 0,
                    error = null,
                    currentAnalysis = null
                )
            }
            try {
                // 날짜별 진행률 실시간 업데이트
                val fullResult = KakaoParser.parseAndAnalyzeFull(
                    context, uri, fileName
                ) { current, total ->
                    val progress = if (total > 0) (current.toFloat() / total.toFloat()) * 0.8f else 0f
                    _uiState.update {
                        it.copy(
                            analysisProgress = progress,
                            progressCurrentDay = current,
                            progressTotalDays = total
                        )
                    }
                }

                _uiState.update { it.copy(analysisProgress = 0.85f) }

                // 전체 분석 저장
                val savedId = chatRepository.saveAnalysis(fullResult.chatAnalysis)
                val savedResult = fullResult.chatAnalysis.copy(id = savedId)

                // 날짜별 결과 저장
                chatRepository.saveDailyResults(savedId, fullResult.dailyResults)

                _uiState.update { it.copy(analysisProgress = 0.95f) }

                // WP 적립
                wellnessPointRepository.earnPoints(WpEvent.ATTENDANCE, "출석 체크 (+10 WP)")
                wellnessPointRepository.earnPoints(
                    eventType = WpEvent.CHAT_ANALYSIS,
                    description = "카카오톡 대화 분석: $fileName"
                )

                // 오늘 날짜 기준 일별 결과로 건강도 업데이트
                val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val todayResult = fullResult.dailyResults.find { it.date == todayStr }
                    ?: fullResult.dailyResults.lastOrNull()
                if (todayResult != null) {
                    val level = KakaoParser.scoreToLevel(todayResult.temperature.toInt())
                    val chatTempScore = level * 4f
                    val relationLevel = KakaoParser.scoreToLevel(todayResult.relationshipScore.toInt())
                    dailyHealthRepository.updateToday(
                        chatTempScore = chatTempScore,
                        relationScore = relationLevel * 2f
                    )
                }

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        analysisProgress = 1f,
                        currentAnalysis = savedResult,
                        currentDailyResults = fullResult.dailyResults,
                        error = null
                    )
                }

                loadRecentAnalyses()
                loadCalendarData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        analysisProgress = 0f,
                        error = e.message ?: "파일 분석 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun loadAnalysis(id: Long) {
        viewModelScope.launch {
            val analyses = chatRepository.getRecentAnalyses(limit = 50)
            val found = analyses.find { it.id == id }
            if (found != null) {
                val dailyResults = chatRepository.getDailyResults(id)
                _uiState.update { it.copy(currentAnalysis = found, currentDailyResults = dailyResults) }
            }
        }
    }

    fun loadRecentAnalyses() {
        viewModelScope.launch {
            val recent = chatRepository.getRecentAnalyses(limit = 10)
            _uiState.update { it.copy(recentFiles = recent) }
        }
    }

    fun loadCalendarData() {
        viewModelScope.launch {
            chatRepository.getAllDailyFlow().collect { allDaily ->
                // 같은 날짜에 여러 파일이 있으면 머지 (메시지 합산, 온도 재계산)
                val grouped = allDaily.groupBy { it.date }
                val merged = grouped.mapValues { (_, results) ->
                    if (results.size == 1) {
                        results.first()
                    } else {
                        val totalMsgs = results.sumOf { it.totalMessages }
                        val totalPos = results.sumOf { it.positiveCount }
                        val totalNeg = results.sumOf { it.negativeCount }
                        val totalNeu = results.sumOf { it.neutralCount }
                        val sentTotal = (totalPos + totalNeg).coerceAtLeast(1)
                        val mergedTemp = (totalPos.toFloat() / sentTotal.toFloat()) * 100f
                        val totalAll = totalMsgs.coerceAtLeast(1)
                        val mergedRel = ((totalPos.toFloat() / totalAll) * 100f +
                                        (totalNeu.toFloat() / totalAll) * 50f -
                                        (totalNeg.toFloat() / totalAll) * 50f).coerceIn(0f, 100f)
                        results.first().copy(
                            totalMessages = totalMsgs,
                            positiveCount = totalPos,
                            negativeCount = totalNeg,
                            neutralCount = totalNeu,
                            temperature = mergedTemp,
                            relationshipScore = mergedRel
                        )
                    }
                }
                _uiState.update { it.copy(calendarData = merged) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearCurrentAnalysis() {
        _uiState.update { it.copy(currentAnalysis = null, analysisProgress = 0f) }
    }
}
