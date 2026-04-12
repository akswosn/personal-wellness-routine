package com.forlks.personal_wellness_routine.ui.screen.character

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.data.db.entity.WellnessPointHistoryEntity
import com.forlks.personal_wellness_routine.domain.model.WpEvent
import com.forlks.personal_wellness_routine.ui.theme.LevelGold
import com.forlks.personal_wellness_routine.ui.theme.LevelGray
import com.forlks.personal_wellness_routine.ui.theme.LevelPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterProfileScreen(
    onBack: () -> Unit,
    viewModel: CharacterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val characterState = uiState.characterState
    val recentHistory = uiState.recentHistory

    val levelLabels = listOf("알", "아기", "성장", "성숙", "레전드")

    // Compute today WP breakdown from recent history (today only)
    val todayDate = remember {
        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    val todayHistory = remember(recentHistory) {
        recentHistory.filter { it.date == todayDate }
    }
    val attendanceWp = remember(todayHistory) {
        todayHistory.filter { it.eventType == WpEvent.ATTENDANCE }.sumOf { it.points }
    }
    val routineWp = remember(todayHistory) {
        todayHistory.filter { it.eventType == WpEvent.ROUTINE }.sumOf { it.points }
    }
    val diaryWp = remember(todayHistory) {
        todayHistory.filter { it.eventType == WpEvent.DIARY }.sumOf { it.points }
    }
    val todayTotalWp = characterState?.todayWp ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌟 캐릭터 성장") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
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

            // 1. Hero section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = characterState?.type?.emoji ?: "🐱",
                        fontSize = 80.sp
                    )
                    Text(
                        text = characterState?.levelName ?: "알",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    val nextLevelIndex = ((characterState?.level ?: 1)).coerceIn(1, 5)
                    val nextLevelName = levelLabels.getOrElse(nextLevelIndex) { "레전드" }
                    val nextLevelWp = characterState?.nextLevelWp ?: 100
                    Text(
                        text = "다음 단계: $nextLevelName (${nextLevelWp}WP)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. WP progress bar
            item {
                val totalWp = characterState?.totalWp ?: 0
                val nextWp = characterState?.nextLevelWp ?: 100
                val progressRatio = characterState?.progressRatio ?: 0f

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${totalWp}WP / ${nextWp}WP (${(progressRatio * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progressRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = LevelGold,
                        trackColor = LevelGray
                    )
                }
            }

            // 3. 5-step progress indicator
            item {
                val currentLevel = characterState?.level ?: 1

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    levelLabels.forEachIndexed { index, label ->
                        val levelNum = index + 1
                        val isCompleted = levelNum < currentLevel
                        val isCurrent = levelNum == currentLevel
                        val isFuture = levelNum > currentLevel

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Connecting line before circle (except first)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (index > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = if (isCompleted || isCurrent) LevelPurple else LevelGray,
                                        thickness = 2.dp
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCompleted -> LevelPurple
                                                isCurrent -> LevelGold
                                                else -> LevelGray
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when {
                                            isCompleted -> "✓"
                                            isCurrent -> "★"
                                            else -> levelNum.toString()
                                        },
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (index < levelLabels.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = if (levelNum < currentLevel) LevelPurple else LevelGray,
                                        thickness = 2.dp
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }

                            Text(
                                text = label,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = when {
                                    isCompleted -> LevelPurple
                                    isCurrent -> LevelGold
                                    else -> LevelGray
                                },
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 4. Today WP summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "오늘 획득 WP +$todayTotalWp",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = LevelGold
                        )
                        HorizontalDivider()
                        WpBreakdownRow(label = "📅 출석", points = attendanceWp)
                        WpBreakdownRow(label = "✅ 루틴 완료", points = routineWp)
                        WpBreakdownRow(label = "📓 일기 작성", points = diaryWp)
                    }
                }
            }

            // 5. Recent history
            item {
                Text(
                    text = "최근 WP 내역",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (recentHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 WP 내역이 없습니다.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(recentHistory) { historyItem ->
                    HistoryListItem(item = historyItem)
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun WpBreakdownRow(label: String, points: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "+$points WP",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = LevelGold
        )
    }
}

@Composable
private fun HistoryListItem(item: WellnessPointHistoryEntity) {
    val eventIcon = when (item.eventType) {
        WpEvent.ATTENDANCE -> "📅"
        WpEvent.ROUTINE -> "✅"
        WpEvent.DIARY -> "📓"
        WpEvent.CHAT_ANALYSIS -> "💬"
        WpEvent.STREAK_7 -> "🔥"
        WpEvent.STREAK_30 -> "🏆"
        else -> "⭐"
    }

    ListItem(
        headlineContent = {
            Text("$eventIcon ${item.description}")
        },
        supportingContent = {
            Text(
                text = item.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Text(
                text = "+${item.points}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = LevelGold
            )
        }
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
