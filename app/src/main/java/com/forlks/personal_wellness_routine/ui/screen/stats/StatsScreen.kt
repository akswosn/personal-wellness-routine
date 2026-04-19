package com.forlks.personal_wellness_routine.ui.screen.stats

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.forlks.personal_wellness_routine.util.DailyHealthCalculator
import com.forlks.personal_wellness_routine.util.LevelColorProvider
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
    val isDark = isSystemInDarkTheme()

    // Tab: 0=일별, 1=주간, 2=월간  (기본: 일별)
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
            BottomNavBar(currentRoute = Screen.Stats.route, onNavigate = onNavigate)
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
            // ── 탭 ─────────────────────────────────────────────────────────────
            item {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("일별") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1; viewModel.loadStats() },
                        text = { Text("주간") }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2; viewModel.loadStats() },
                        text = { Text("월간") }
                    )
                }
            }

            when (selectedTabIndex) {
                // ────────────────────────────────────────────────────────────────
                // 탭 0: 일별 — 오늘 일 건강도 상세
                // ────────────────────────────────────────────────────────────────
                0 -> {
                    item {
                        val dhs = uiState.todayHealthScore
                        val level = dhs?.level ?: 1
                        val totalScore = dhs?.totalScore ?: 0f
                        val levelColors = LevelColorProvider.get(level, isDark)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = levelColors.container)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = DailyHealthCalculator.levelEmoji(level),
                                    fontSize = 40.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = DailyHealthCalculator.levelLabel(level),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = levelColors.onContainer
                                )
                                Text(
                                    text = "Lv.$level  ·  ${totalScore.toInt()}점",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = levelColors.accent
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LinearProgressIndicator(
                                    progress = { (totalScore / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    color = levelColors.accent,
                                    trackColor = levelColors.accent.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }

                    // 서브스코어 상세 카드 5개
                    item {
                        val dhs = uiState.todayHealthScore
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "오늘의 점수 상세",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            val items = listOf(
                                SubScoreItem("😊 오늘 기분", dhs?.moodScore, 20f),
                                SubScoreItem("📋 루틴 달성", dhs?.routineScore, 25f),
                                SubScoreItem("📓 마음 건강", dhs?.diaryScore, 25f),
                                SubScoreItem("💬 대화 온도", dhs?.chatTempScore, 20f),
                                SubScoreItem("🤝 관계 건강", dhs?.relationScore, 10f)
                            )
                            items.forEach { item ->
                                SubScoreDetailRow(item = item)
                            }
                        }
                    }

                    item {
                        Text(
                            "오늘 더 기록하면 건강도가 올라가요 📈",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ────────────────────────────────────────────────────────────────
                // 탭 1: 주간 — 기존 루틴 달성 바차트 + 일 건강도 레벨바
                // ────────────────────────────────────────────────────────────────
                1 -> {
                    // 루틴 달성 바 차트
                    item {
                        val weeklyData = uiState.weeklyData
                        if (weeklyData.isNotEmpty()) {
                            val maxCount = weeklyData.maxOf { it.second }.coerceAtLeast(1)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "이번 주 루틴 달성",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
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
                                            drawRect(color = WellGreen, topLeft = Offset(left, top), size = Size(barWidth, barHeight))
                                            if (count > 0) {
                                                drawCircle(color = WellGreen.copy(alpha = 0.4f), radius = 8f, center = Offset(left + barWidth / 2, top - 12f))
                                            }
                                        }
                                        drawLine(color = Color.LightGray, start = Offset(0f, baselineY), end = Offset(size.width, baselineY), strokeWidth = 2f)
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    weeklyData.forEach { (label, _) ->
                                        Text(text = label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // 일 건강도 주간 레벨 바
                    item {
                        val weeklyHealth = uiState.weeklyHealthData
                        if (weeklyHealth.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "이번 주 건강도",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    weeklyHealth.forEach { summary ->
                                        val lc = LevelColorProvider.get(
                                            if (summary.level > 0) summary.level else 1, isDark
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(64.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (summary.level > 0) lc.container
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (summary.level > 0) {
                                                    Text(
                                                        text = DailyHealthCalculator.levelEmoji(summary.level),
                                                        fontSize = 20.sp
                                                    )
                                                } else {
                                                    Text("—", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = summary.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 요약 스탯 카드
                    item {
                        val achievementPct = (uiState.monthlyAchievementRate * 100).roundToInt()
                        val streak = uiState.streak
                        val avgEmotion = uiState.averageEmotion
                        val emotionSign = if (avgEmotion >= 0) "+" else ""
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("달성률", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$achievementPct%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WellGreen)
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("스트릭", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("🔥$streak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("감정", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$emotionSign${"%.1f".format(avgEmotion)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                        color = when { avgEmotion >= 0.1f -> WellGreen; avgEmotion <= -0.1f -> MaterialTheme.colorScheme.error; else -> MaterialTheme.colorScheme.onSurface })
                                }
                            }
                        }
                    }

                    // 대화 분석 위젯
                    val latestChat = uiState.latestChatAnalysis
                    if (latestChat != null) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("💬 이번 주 대화 온도", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val positiveRatioPct = (latestChat.positiveRatio * 100).roundToInt()
                                    val stars = buildString {
                                        val filled = (latestChat.relationshipScore / 20).roundToInt().coerceIn(0, 5)
                                        repeat(filled) { append("★") }
                                        repeat(5 - filled) { append("☆") }
                                    }
                                    Text("🌡 ${latestChat.temperatureLabel} · 긍정 $positiveRatioPct% · 관계 $stars", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // 공유 버튼
                    item {
                        val achievementPct = (uiState.monthlyAchievementRate * 100).roundToInt()
                        val streak = uiState.streak
                        val avgEmotion = uiState.averageEmotion
                        val emotionSign = if (avgEmotion >= 0) "+" else ""
                        val latestChat = uiState.latestChatAnalysis

                        OutlinedButton(
                            onClick = {
                                val shareText = buildString {
                                    appendLine("📊 마음흐름 이번 주 통계")
                                    appendLine("달성률: $achievementPct%")
                                    appendLine("스트릭: 🔥$streak")
                                    appendLine("평균 감정: $emotionSign${"%.1f".format(avgEmotion)}")
                                    if (latestChat != null) {
                                        appendLine("대화 온도: ${latestChat.temperatureLabel} · 긍정 ${(latestChat.positiveRatio * 100).roundToInt()}%")
                                    }
                                    append("#마음흐름 #웰니스 #루틴챌린지")
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
                    }

                }

                // ────────────────────────────────────────────────────────────────
                // 탭 2: 월간 — 캘린더 그리드 (일 건강도 레벨 색상)
                // ────────────────────────────────────────────────────────────────
                2 -> {
                    item {
                        val monthlyData = uiState.monthlyHealthData
                        val firstDayOfWeek = if (monthlyData.isNotEmpty()) {
                            java.time.LocalDate.parse(monthlyData.first().date).dayOfWeek.value % 7
                        } else 0

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "이번 달 일 건강도",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // 요일 헤더
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                                    Text(
                                        text = day,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            // 달력 그리드 (7열)
                            val totalCells = firstDayOfWeek + monthlyData.size
                            val rows = (totalCells + 6) / 7
                            repeat(rows) { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(7) { col ->
                                        val cellIndex = row * 7 + col
                                        val dataIndex = cellIndex - firstDayOfWeek
                                        if (dataIndex < 0 || dataIndex >= monthlyData.size) {
                                            Spacer(modifier = Modifier.weight(1f).height(40.dp))
                                        } else {
                                            val summary = monthlyData[dataIndex]
                                            val lc = LevelColorProvider.get(
                                                if (summary.level > 0) summary.level else 1, isDark
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (summary.level > 0) lc.container
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (summary.level > 0) DailyHealthCalculator.levelEmoji(summary.level) else summary.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = if (summary.level > 0) 14.sp else 11.sp,
                                                    color = if (summary.level > 0) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }

                    // 레벨 범례
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("건강도 레벨", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                (1..5).forEach { lv ->
                                    val lc = LevelColorProvider.get(lv, isDark)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(lc.container),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(DailyHealthCalculator.levelEmoji(lv), fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Lv.$lv ${DailyHealthCalculator.levelLabel(lv)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = lc.onContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 월간 요약 스탯
                    item {
                        val achievementPct = (uiState.monthlyAchievementRate * 100).roundToInt()
                        val streak = uiState.streak
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("이번 달 달성률", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$achievementPct%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WellGreen)
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("최장 스트릭", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("🔥${streak}일", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ── 서브스코어 상세 Row ──────────────────────────────────────────────────────
data class SubScoreItem(val label: String, val score: Float?, val maxScore: Float)

@Composable
private fun SubScoreDetailRow(item: SubScoreItem) {
    val hasData = item.score != null
    val progress = if (hasData) (item.score!! / item.maxScore).coerceIn(0f, 1f) else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(90.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = WellGreen,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (hasData) "${item.score!!.toInt()}/${item.maxScore.toInt()}"
                   else "미기록",
            style = MaterialTheme.typography.labelSmall,
            color = if (hasData) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp),
            textAlign = TextAlign.End
        )
    }
}
