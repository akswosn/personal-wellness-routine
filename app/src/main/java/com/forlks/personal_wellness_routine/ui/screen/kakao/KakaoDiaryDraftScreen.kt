package com.forlks.personal_wellness_routine.ui.screen.kakao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KakaoDiaryDraftScreen(
    analysisId: Long,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: KakaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val analysis = uiState.currentAnalysis

    var editableContent by remember { mutableStateOf(analysis?.autoDiaryDraft ?: "") }

    LaunchedEffect(analysis) {
        if (analysis != null && editableContent.isEmpty()) {
            editableContent = analysis.autoDiaryDraft
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📝 자동 일기 초안") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (analysis == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "분석 데이터를 불러오는 중...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        val temperatureLabel = analysis.temperatureLabel
        val emotionChips = when {
            temperatureLabel.contains("따뜻") -> listOf("😊 설렘", "🙏 감사")
            temperatureLabel.contains("보통") -> listOf("🙂 평온", "💬 소통")
            temperatureLabel.contains("차가") -> listOf("😶 무덤덤")
            else -> listOf("😞 아쉬움")
        }

        val todayFormatted = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Sub-header
            item {
                Text(
                    text = "🤖 AI가 오늘의 대화에서 발췌했어요",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Date
            item {
                Text(
                    text = todayFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Emotion chips
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    emotionChips.forEach { chip ->
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text(chip) }
                        )
                    }
                }
            }

            // Editable text area
            item {
                OutlinedTextField(
                    value = editableContent,
                    onValueChange = { editableContent = it },
                    label = { Text("원하는 내용을 추가로 편집하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 200.dp),
                    minLines = 6
                )
            }

            // Save button
            item {
                Button(
                    onClick = { onSaved() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📓 일기로 저장하기")
                }
            }

            // Reward ad note
            item {
                Text(
                    text = "💡 광고 시청 → 일기 PDF 내보내기",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
