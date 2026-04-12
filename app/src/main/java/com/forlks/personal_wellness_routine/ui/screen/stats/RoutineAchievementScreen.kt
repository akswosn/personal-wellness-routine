package com.forlks.personal_wellness_routine.ui.screen.stats

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
import com.forlks.personal_wellness_routine.util.RoutineAchievementCalculator

private val BarGreen = Color(0xFF4CAF50)
private val BarLightGreen = Color(0xFFA5D6A7)
private val BarRed = Color(0xFFEF5350)
private val GradeGold = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAchievementScreen(
    onBack: () -> Unit,
    viewModel: RoutineAchievementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📈 루틴 달성도 분석") },
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
            // 탭 (일간/주간/월간)
            item {
                TabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    listOf("일간", "주간", "월간").forEachIndexed { idx, label ->
                        Tab(
                            selected = state.selectedTab == idx,
                            onClick = { viewModel.selectTab(idx) },
                            text = { Text(label) }
                        )
                    }
                }
            }

            // 점수 대형 카드
            item {
                ScoreCard(
                    score = state.currentScore,
                    grade = state.currentGrade
                )
            }

            // WP 보너스 칩
            if (state.wpBonusMessage.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = GradeGold.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎉 ${state.wpBonusMessage}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB8860B),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 바 차트
            if (state.barValues.size > 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = when (state.selectedTab) {
                                    1 -> "최근 7일 달성률"
                                    2 -> "4주 달성률"
                                    else -> "오늘 달성률"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))
                            AchievementBarChart(bars = state.barValues)
                        }
                    }
                }
            }

            // 루틴별 달성율 (일간 탭에서만)
            if (state.selectedTab == 0 && state.routineDetails.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "루틴별 완료율",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            state.routineDetails.forEach { (name, completed, total) ->
                                val ratio = if (total == 0) 0f else completed.toFloat() / total
                                RoutineProgressRow(name = name, ratio = ratio)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(score: Int, grade: String) {
    val gradeColor = when (grade) {
        "S" -> Color(0xFFFFD700)
        "A" -> WellGreen
        "B" -> Color(0xFF42A5F5)
        "C" -> Color(0xFFFF9800)
        else -> Color(0xFFEF5350)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = gradeColor.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${score}점",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = gradeColor
                )
                Text(
                    text = RoutineAchievementCalculator.gradeDesc(grade),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = grade,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = gradeColor
                )
                Text(
                    text = RoutineAchievementCalculator.gradeEmoji(grade),
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
private fun AchievementBarChart(bars: List<Pair<String, Int>>) {
    val maxScore = 100
    val barWidth = 0.6f

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val w = size.width
            val h = size.height
            val count = bars.size
            val slotW = w / count

            bars.forEachIndexed { i, (_, score) ->
                val barH = (score.toFloat() / maxScore) * (h - 24.dp.toPx())
                val x = i * slotW + slotW * (1 - barWidth) / 2
                val barColor = when (RoutineAchievementCalculator.barColorLevel(score)) {
                    2 -> BarGreen
                    1 -> BarLightGreen
                    else -> BarRed
                }
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, h - barH - 24.dp.toPx()),
                    size = Size(slotW * barWidth, barH)
                )
            }
        }
        // X축 레이블
        Row(modifier = Modifier.fillMaxWidth()) {
            bars.forEach { (label, _) ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RoutineProgressRow(name: String, ratio: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, style = MaterialTheme.typography.bodySmall)
            Text(
                "${(ratio * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = WellGreen
            )
        }
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = WellGreen,
            trackColor = WellGreen.copy(alpha = 0.15f)
        )
    }
}
