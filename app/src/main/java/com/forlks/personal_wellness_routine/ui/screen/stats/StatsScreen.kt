package com.forlks.personal_wellness_routine.ui.screen.stats

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.ui.component.BottomNavBar
import com.forlks.personal_wellness_routine.ui.navigation.Screen
import com.forlks.personal_wellness_routine.ui.theme.WellGreen
import com.forlks.personal_wellness_routine.ui.theme.WellSurfaceVariant
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Tab state: 0 = weekly, 1 = monthly
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 통계 대시보드") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.Stats.route,
                onNavigate = onNavigate
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. Week/Month tab
            item {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = {
                            selectedTabIndex = 0
                            viewModel.loadStats()
                        },
                        text = { Text("주간") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            selectedTabIndex = 1
                            viewModel.loadStats()
                        },
                        text = { Text("월간") }
                    )
                }
            }

            // 2. Bar chart
            item {
                val weeklyData = uiState.weeklyData
                if (weeklyData.isNotEmpty()) {
                    val maxCount = weeklyData.maxOf { it.second }.coerceAtLeast(1)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (selectedTabIndex == 0) "이번 주 루틴 달성" else "이번 달 루틴 달성",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val barCount = weeklyData.size
                                val totalSpacing = size.width * 0.3f
                                val barWidth = (size.width - totalSpacing) / barCount
                                val spacingWidth = totalSpacing / (barCount + 1)
                                val chartHeight = size.height * 0.75f
                                val baselineY = size.height * 0.80f

                                weeklyData.forEachIndexed { index, (_, count) ->
                                    val barHeight = (count.toFloat() / maxCount) * chartHeight
                                    val left = spacingWidth + index * (barWidth + spacingWidth)
                                    val top = baselineY - barHeight

                                    // Draw bar
                                    drawRect(
                                        color = WellGreen,
                                        topLeft = Offset(left, top),
                                        size = Size(barWidth, barHeight)
                                    )

                                    // Count label above bar: small circle indicator
                                    if (count > 0) {
                                        drawCircle(
                                            color = WellGreen.copy(alpha = 0.4f),
                                            radius = 8f,
                                            center = Offset(left + barWidth / 2, top - 12f)
                                        )
                                    }
                                }

                                // Baseline
                                drawLine(
                                    color = Color.LightGray,
                                    start = Offset(0f, baselineY),
                                    end = Offset(size.width, baselineY),
                                    strokeWidth = 2f
                                )
                            }
                        }

                        // X-axis labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            weeklyData.forEach { (label, _) ->
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
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text(
                                "데이터가 없습니다.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Summary stats row
            item {
                val achievementPct = (uiState.monthlyAchievementRate * 100).roundToInt()
                val streak = uiState.streak
                val avgEmotion = uiState.averageEmotion
                val emotionSign = if (avgEmotion >= 0) "+" else ""

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Achievement rate card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = WellSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "달성률",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$achievementPct%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = WellGreen
                            )
                        }
                    }

                    // Streak card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = WellSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "스트릭",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "🔥$streak",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Average emotion card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = WellSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "감정",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$emotionSign${"%.1f".format(avgEmotion)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    avgEmotion >= 0.1f -> WellGreen
                                    avgEmotion <= -0.1f -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }

            // 4. Chat analysis widget
            val latestChat = uiState.latestChatAnalysis
            if (latestChat != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = WellSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "💬 이번 주 대화 온도",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val positiveRatioPct = (latestChat.positiveRatio * 100).roundToInt()
                            val relationshipStars = buildString {
                                val filled = (latestChat.relationshipScore / 20).roundToInt().coerceIn(0, 5)
                                repeat(filled) { append("★") }
                                repeat(5 - filled) { append("☆") }
                            }

                            Text(
                                text = "🌡 ${latestChat.temperatureLabel} · 긍정 $positiveRatioPct% · 관계 건강도 $relationshipStars",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 5. Best routines section
            item {
                Text(
                    text = "🏆 이번 주 Best 루틴",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val weeklyData = uiState.weeklyData
                val topRoutineLabels = listOf("루틴 완료 1위", "루틴 완료 2위", "루틴 완료 3위")
                topRoutineLabels.forEachIndexed { index, label ->
                    val medal = listOf("🥇", "🥈", "🥉")[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = medal, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 6. Share button
            item {
                val achievementPct = (uiState.monthlyAchievementRate * 100).roundToInt()
                val streak = uiState.streak
                val avgEmotion = uiState.averageEmotion
                val emotionSign = if (avgEmotion >= 0) "+" else ""

                OutlinedButton(
                    onClick = {
                        val shareText = buildString {
                            appendLine("📊 WellFlow 이번 주 통계")
                            appendLine("달성률: $achievementPct%")
                            appendLine("스트릭: 🔥$streak")
                            appendLine("평균 감정: $emotionSign${"%.1f".format(avgEmotion)}")
                            if (latestChat != null) {
                                val positiveRatioPct = (latestChat.positiveRatio * 100).roundToInt()
                                appendLine("대화 온도: ${latestChat.temperatureLabel} · 긍정 $positiveRatioPct%")
                            }
                            append("#WellFlow #웰니스 #루틴챌린지")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "공유하기"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💬 카카오톡으로 공유")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 7. 분석 바로가기 섹션
            item {
                Text(
                    text = "📊 상세 분석",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 루틴 달성도 분석 카드
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(Screen.RoutineAchievement.route) },
                        colors = CardDefaults.cardColors(
                            containerColor = WellGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📈", fontSize = 28.sp)
                            Text(
                                "루틴 달성도",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = WellGreen
                            )
                            Text(
                                "일간·주간·월간\n등급 분석",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    // 마음 건강도 카드
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(Screen.MindHealth.route) },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF29B6F6).copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("💙", fontSize = 28.sp)
                            Text(
                                "마음 건강도",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF29B6F6)
                            )
                            Text(
                                "일기 감정 분포\n4주 추이 분석",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
