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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class KakaoUiState(
    val recentFiles: List<ChatAnalysisResult> = emptyList(),
    val isAnalyzing: Boolean = false,
    val analysisProgress: Float = 0f,
    val progressCurrentLine: Int = 0,
    val progressTotalLines: Int = 0,
    val progressCurrentDay: Int = 0,   // 하위 호환 유지
    val progressTotalDays: Int = 0,    // 하위 호환 유지
    val currentAnalysis: ChatAnalysisResult? = null,
    val currentDailyResults: List<DailyChatResult> = emptyList(),
    val calendarData: Map<String, DailyChatResult> = emptyMap(),
    val error: String? = null,
    /** 오늘 분석에서 파싱된 메시지 수 (0이면 당일 대화 없음) */
    val todayMessageCount: Int = -1
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

    /**
     * 파일 선택 직후 즉시 호출 — UI에 "파일 읽는 중" 상태를 선제 표시
     * (파일 복사가 IO 스레드에서 진행되는 동안 인라인 카드가 먼저 보이게 함)
     */
    fun startFilePreparing() {
        _uiState.update {
            it.copy(
                isAnalyzing = true,
                analysisProgress = 0f,
                progressCurrentLine = 0,
                progressTotalLines = 0,   // 0 → AnalysisProgressCard가 indeterminate 표시
                todayMessageCount = -1,
                error = null,
                currentAnalysis = null
            )
        }
    }

    /**
     * [당일 전용] 파일에서 오늘 날짜 메시지만 파싱·분석
     *
     * IO 스레드에서 실행 → 메인 스레드 블록 없이 프로그레스 정상 업데이트
     * 오늘 메시지가 0건이면 error 상태로 안내
     */
    fun analyzeFile(context: Context, uri: Uri, fileName: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    analysisProgress = 0f,
                    progressCurrentLine = 0,
                    progressTotalLines = 0,
                    todayMessageCount = -1,
                    error = null,
                    currentAnalysis = null
                )
            }
            try {
                // ── 파일 파싱을 IO 스레드로 이동 ─────────────────────────────
                // MutableStateFlow.update()는 스레드 안전 → IO에서 직접 호출 가능
                val fullResult = withContext(Dispatchers.IO) {
                    KakaoParser.parseTodayOnly(
                        context, uri, fileName
                    ) { current, total ->
                        val progress = if (total > 0) (current.toFloat() / total) * 0.85f else 0f
                        _uiState.update {
                            it.copy(
                                analysisProgress = progress,
                                progressCurrentLine = current,
                                progressTotalLines = total
                            )
                        }
                    }
                }

                val todayCount = fullResult.chatAnalysis.totalMessages

                // 오늘 대화가 없으면 안내 오류로 처리
                if (todayCount == 0) {
                    val today = LocalDate.now()
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            analysisProgress = 0f,
                            todayMessageCount = 0,
                            error = "오늘(${today.monthValue}월 ${today.dayOfMonth}일) 날짜의 대화가 없습니다.\n" +
                                    "카카오톡에서 오늘 날짜가 포함된 파일을 내보내주세요."
                        )
                    }
                    return@launch
                }

                _uiState.update { it.copy(analysisProgress = 0.88f) }

                val savedId = chatRepository.saveAnalysis(fullResult.chatAnalysis)
                val savedResult = fullResult.chatAnalysis.copy(id = savedId)
                chatRepository.saveDailyResults(savedId, fullResult.dailyResults)

                _uiState.update { it.copy(analysisProgress = 0.95f) }

                // WP 적립
                wellnessPointRepository.earnPoints(WpEvent.ATTENDANCE, "출석 체크 (+10 WP)")
                wellnessPointRepository.earnPoints(
                    eventType = WpEvent.CHAT_ANALYSIS,
                    description = "카카오톡 대화 분석: $fileName"
                )

                // 건강도 업데이트
                val todayResult = fullResult.dailyResults.firstOrNull()
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
                        todayMessageCount = todayCount,
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
                val grouped = allDaily.groupBy { it.date }
                val merged = grouped.mapValues { (_, results) ->
                    if (results.size == 1) results.first()
                    else {
                        val totalMsgs = results.sumOf { it.totalMessages }
                        val totalPos = results.sumOf { it.positiveCount }
                        val totalNeg = results.sumOf { it.negativeCount }
                        val totalNeu = results.sumOf { it.neutralCount }
                        val sentTotal = (totalPos + totalNeg).coerceAtLeast(1)
                        val mergedTemp = (totalPos.toFloat() / sentTotal) * 100f
                        val totalAll = totalMsgs.coerceAtLeast(1)
                        val mergedRel = ((totalPos.toFloat() / totalAll) * 100f +
                                        (totalNeu.toFloat() / totalAll) * 50f -
                                        (totalNeg.toFloat() / totalAll) * 50f).coerceIn(0f, 100f)
                        results.first().copy(
                            totalMessages = totalMsgs,
                            positiveCount = totalPos, negativeCount = totalNeg, neutralCount = totalNeu,
                            temperature = mergedTemp, relationshipScore = mergedRel
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
