package com.forlks.personal_wellness_routine.ui.screen.kakao

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.ui.theme.WellGreen

@Composable
fun KakaoAnalyzingScreen(
    fileUri: String,
    onAnalysisDone: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: KakaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(fileUri) {
        val uri = Uri.parse(fileUri)
        val fileName = uri.lastPathSegment
            ?.substringAfterLast('/')
            ?: "kakao_chat.txt"
        viewModel.analyzeFile(context, uri, fileName)
    }

    LaunchedEffect(uiState.isAnalyzing, uiState.currentAnalysis) {
        if (!uiState.isAnalyzing && uiState.currentAnalysis != null) {
            onAnalysisDone(uiState.currentAnalysis!!.id)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.error != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "⚠️ 오류 발생",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = {
                        val uri = Uri.parse(fileUri)
                        val fileName = uri.lastPathSegment
                            ?.substringAfterLast('/')
                            ?: "kakao_chat.txt"
                        viewModel.analyzeFile(context, uri, fileName)
                    }) {
                        Text("다시 시도")
                    }
                    Button(
                        onClick = onBack
                    ) {
                        Text("돌아가기")
                    }
                }
            }

            else -> {
                val progress = uiState.analysisProgress

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "💬 분석 중...",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(80.dp),
                        color = WellGreen,
                        strokeWidth = 6.dp
                    )

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (progress > 0.1f) {
                            ProgressStep(text = "✅ 파일 파싱 완료")
                        }
                        if (progress > 0.3f) {
                            ProgressStep(text = "✅ Regex 패턴 매칭")
                        }
                        if (progress > 0.1f) {
                            val tfliteText = if (progress <= 0.7f) {
                                "⏳ TFLite 감정 분류 중..."
                            } else {
                                "✅ TFLite 감정 분류 완료"
                            }
                            ProgressStep(text = tfliteText)
                        }
                        if (progress > 0.3f) {
                            val keywordText = if (progress <= 0.9f) {
                                "⏳ 키워드 추출"
                            } else {
                                "✅ 키워드 추출 완료"
                            }
                            ProgressStep(text = keywordText)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🔒 기기 내 처리",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressStep(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}
