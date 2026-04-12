package com.forlks.personal_wellness_routine.ui.screen.kakao

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.repository.ChatRepository
import com.forlks.personal_wellness_routine.data.repository.WellnessPointRepository
import com.forlks.personal_wellness_routine.domain.model.ChatAnalysisResult
import com.forlks.personal_wellness_routine.domain.model.WpEvent
import com.forlks.personal_wellness_routine.util.KakaoParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KakaoUiState(
    val recentFiles: List<ChatAnalysisResult> = emptyList(),
    val isAnalyzing: Boolean = false,
    val analysisProgress: Float = 0f,
    val currentAnalysis: ChatAnalysisResult? = null,
    val error: String? = null
)

@HiltViewModel
class KakaoViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val wellnessPointRepository: WellnessPointRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KakaoUiState())
    val uiState: StateFlow<KakaoUiState> = _uiState.asStateFlow()

    init {
        loadRecentAnalyses()
    }

    fun analyzeFile(context: Context, uri: Uri, fileName: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    analysisProgress = 0f,
                    error = null,
                    currentAnalysis = null
                )
            }
            try {
                _uiState.update { it.copy(analysisProgress = 0.2f) }

                val result = KakaoParser.parseAndAnalyze(context, uri, fileName)

                _uiState.update { it.copy(analysisProgress = 0.7f) }

                val savedId = chatRepository.saveAnalysis(result)
                val savedResult = result.copy(id = savedId)

                _uiState.update { it.copy(analysisProgress = 0.9f) }

                wellnessPointRepository.earnPoints(
                    eventType = WpEvent.CHAT_ANALYSIS,
                    description = "카카오톡 대화 분석: $fileName"
                )

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        analysisProgress = 1f,
                        currentAnalysis = savedResult,
                        error = null
                    )
                }

                loadRecentAnalyses()
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
            _uiState.update { it.copy(currentAnalysis = found) }
        }
    }

    fun loadRecentAnalyses() {
        viewModelScope.launch {
            val recent = chatRepository.getRecentAnalyses(limit = 10)
            _uiState.update { it.copy(recentFiles = recent) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearCurrentAnalysis() {
        _uiState.update { it.copy(currentAnalysis = null, analysisProgress = 0f) }
    }
}
