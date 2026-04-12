package com.forlks.personal_wellness_routine.ui.screen.kakao

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.ui.theme.EmotionNegative
import com.forlks.personal_wellness_routine.ui.theme.EmotionNeutral
import com.forlks.personal_wellness_routine.ui.theme.EmotionPositive
import com.forlks.personal_wellness_routine.ui.theme.WellGreen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KakaoRelationshipScreen(
    analysisId: Long,
    onNext: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: KakaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val analysis = uiState.currentAnalysis

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👥 관계 건강도") },
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Emotion distribution bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "감정 분포",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        // Positive bar
                        EmotionBar(
                            label = "긍정",
                            ratio = analysis.positiveRatio,
                            color = EmotionPositive
                        )
                        // Neutral bar
                        EmotionBar(
                            label = "중립",
                            ratio = analysis.neutralRatio,
                            color = EmotionNeutral
                        )
                        // Negative bar
                        EmotionBar(
                            label = "부정",
                            ratio = analysis.negativeRatio,
                            color = EmotionNegative
                        )
                    }
                }
            }

            // Relationship score
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "채팅 상대별 건강도",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        val score = analysis.relationshipScore
                        val starCount = (score / 20f).roundToInt().coerceIn(0, 5)
                        val starDisplay = buildString {
                            repeat(starCount) { append("★") }
                            repeat(5 - starCount) { append("☆") }
                        }
                        Text(
                            text = starDisplay,
                            fontSize = 28.sp,
                            color = WellGreen,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "${score.roundToInt()}점 / 100점",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Health percentage
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "관계 건강 지수",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        val healthPercent = analysis.relationshipScore.roundToInt()
                        Text(
                            text = "$healthPercent%",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = WellGreen
                        )
                        LinearProgressIndicator(
                            progress = { (analysis.relationshipScore / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = WellGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            // Keywords section
            if (analysis.topKeywords.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "이번 달 자주 쓴 단어",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                analysis.topKeywords.forEach { keyword ->
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(keyword) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Next button
            item {
                Button(
                    onClick = { onNext(analysisId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("다음 →")
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun EmotionBar(
    label: String,
    ratio: Float,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "${(ratio * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
