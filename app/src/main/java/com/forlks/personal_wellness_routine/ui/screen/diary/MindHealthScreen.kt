package com.forlks.personal_wellness_routine.ui.screen.diary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.ui.theme.WellGreen

private val SkyBlue   = Color(0xFF29B6F6)
private val SoftGold  = Color(0xFFFFC107)
private val SoftRed   = Color(0xFFEF9A9A)
private val SoftGray  = Color(0xFFBDBDBD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindHealthScreen(
    onBack: () -> Unit,
    viewModel: MindHealthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💙 마음 건강도") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WellGreen)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Hero 점수 카드
            item {
                MindHealthHeroCard(
                    score = state.score,
                    level = state.level,
                    levelEmoji = state.levelEmoji
                )
            }

            // 인사이트 카드
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = WellGreen.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💡", fontSize = 18.sp)
                        Text(
                            text = state.insight,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 4주 추이 차트
            if (state.weeklyTrend.isNotEmpty() && state.weeklyTrend.any { it.second > 0 }) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "4주 마음 건강도 추이",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))
                            MindHealthBarChart(bars = state.weeklyTrend)
                        }
                    }
                }
            }

            // 감정 분포
            if (state.emotionDistribution.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "감정 분포",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            state.emotionDistribution.forEach { (label, ratio) ->
                                EmotionDistRow(label = label, ratio = ratio)
                            }
                        }
                    }
                }
            }

            // 전문가 상담 배너
            if (state.showCounselingBanner) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SkyBlue.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "💙 마음이 많이 힘드신가요?",
                                fontWeight = FontWeight.Bold,
                                color = SkyBlue
                            )
                            Text(
                                "전문 상담사와 이야기 나눠보는 것도 좋은 방법이에요. 혼자 힘들어하지 않아도 괜찮아요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 데이터 부족 안내
            if (state.score < 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📓", fontSize = 40.sp)
                            Text(
                                "50자 이상 일기를 5개 이상 작성하면\n마음 건강도를 분석할 수 있어요",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MindHealthHeroCard(score: Int, level: String, levelEmoji: String) {
    val displayScore = if (score < 0) "-" else "$score%"
    val levelColor = when {
        score >= 80 -> Color(0xFFFFC107)
        score >= 60 -> WellGreen
        score >= 40 -> Color(0xFF42A5F5)
        score >= 20 -> Color(0xFFFF9800)
        score >= 0  -> Color(0xFFEF5350)
        else        -> Color(0xFFBDBDBD)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = levelColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("이번 달 마음 건강도", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(displayScore, fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = levelColor)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(levelEmoji, fontSize = 22.sp)
                Text(level, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = levelColor)
            }
        }
    }
}

@Composable
private fun MindHealthBarChart(bars: List<Pair<String, Int>>) {
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val w = size.width
            val h = size.height - 24.dp.toPx()
            val count = bars.size
            val slotW = w / count
            val barW = slotW * 0.55f
            bars.forEachIndexed { i, (_, score) ->
                val barH = (score.coerceAtLeast(0).toFloat() / 100f) * h
                val x = i * slotW + slotW * 0.225f
                drawRect(
                    color = WellGreen.copy(alpha = 0.85f),
                    topLeft = Offset(x, h - barH),
                    size = Size(barW, barH)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            bars.forEach { (label, score) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(if (score > 0) "$score%" else "-",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WellGreen)
                    Text(label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmotionDistRow(label: String, ratio: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = WellGreen,
            trackColor = WellGreen.copy(alpha = 0.12f)
        )
        Text(
            "${(ratio * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}
