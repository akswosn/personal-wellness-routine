package com.forlks.personal_wellness_routine.ui.screen.kakao

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "kakao_chat.txt"
        viewModel.analyzeFile(context, uri, fileName)
    }

    LaunchedEffect(uiState.isAnalyzing, uiState.currentAnalysis) {
        if (!uiState.isAnalyzing && uiState.currentAnalysis != null) {
            onAnalysisDone(uiState.currentAnalysis!!.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.error != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Text(
                        text = uiState.error ?: "오류가 발생했습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = onBack) { Text("돌아가기") }
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text("💬", fontSize = 56.sp)
                    Text(
                        text = "대화 분석 중...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // 날짜 카운트 표시
                    if (uiState.progressTotalDays > 0) {
                        Text(
                            text = "${uiState.progressCurrentDay} / ${uiState.progressTotalDays} 일 처리 중",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "파일을 읽는 중...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 프로그레스 바
                    LinearProgressIndicator(
                        progress = { uiState.analysisProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = WellGreen,
                        trackColor = WellGreen.copy(alpha = 0.2f)
                    )

                    Text(
                        text = "${(uiState.analysisProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = WellGreen,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "감정 키워드를 분석하고 있어요.\n잠시만 기다려 주세요 🔍",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
