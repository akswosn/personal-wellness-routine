package com.forlks.personal_wellness_routine.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.forlks.personal_wellness_routine.BuildConfig
import com.forlks.personal_wellness_routine.ui.component.AD_BANNER_HEIGHT
import com.forlks.personal_wellness_routine.ui.component.AdBannerView
import com.forlks.personal_wellness_routine.ui.component.BottomNavBar
import com.forlks.personal_wellness_routine.ui.navigation.Screen
import com.forlks.personal_wellness_routine.ui.screen.routine.RoutineViewModel
import com.forlks.personal_wellness_routine.ui.theme.WellGold
import com.forlks.personal_wellness_routine.ui.theme.WellGreen
import com.forlks.personal_wellness_routine.util.DailyHealthCalculator
import com.forlks.personal_wellness_routine.util.LevelColorProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRoutines: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToKakao: () -> Unit,
    onNavigateToCharacter: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    routineViewModel: RoutineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val routineUiState by routineViewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREAN)
    val dateString = today.format(dateFormatter)
    val emojis = listOf("😢", "😔", "😐", "🙂", "😊")

    // 광고 배너 높이만큼 하단 여백 추가 (콘텐츠가 배너 뒤로 가리지 않도록)
    val adBottomPadding = if (BuildConfig.ADS_ENABLED) AD_BANNER_HEIGHT else 0.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌿 마음흐름",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WellGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🔥${uiState.streak}day",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = WellGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            bottomBar = {
                BottomNavBar(
                    currentRoute = Screen.Home.route,
                    onNavigate = { route ->
                        when (route) {
                            Screen.Home.route        -> { }
                            Screen.RoutineList.route -> onNavigateToRoutines()
                            Screen.Diary.route       -> onNavigateToDiary()
                            Screen.Stats.route       -> onNavigateToStats()
                            Screen.Settings.route    -> onNavigateToSettings()
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    // 배너 높이만큼 하단 여백 확보
                    bottom = 16.dp + adBottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 0. 일 건강도 카드
                item {
                    val dhs = uiState.dailyHealthScore
                    val level = dhs?.level ?: 1
                    val totalScore = dhs?.totalScore ?: 0f
                    val levelColors = LevelColorProvider.get(level, isDark)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToStats() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = levelColors.container)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "오늘의 건강도",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = levelColors.onContainer.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = DailyHealthCalculator.levelEmoji(level),
                                            fontSize = 22.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = DailyHealthCalculator.levelLabel(level),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = levelColors.onContainer
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Lv.$level",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = levelColors.accent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { (totalScore / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier.size(56.dp),
                                        strokeWidth = 5.dp,
                                        color = levelColors.accent,
                                        trackColor = levelColors.accent.copy(alpha = 0.2f)
                                    )
                                    Text(
                                        text = "${totalScore.toInt()}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = levelColors.onContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 서브 스코어 칩
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val chips = listOf(
                                    Triple("기분",   dhs?.moodScore,     20f),
                                    Triple("루틴",   dhs?.routineScore,  25f),
                                    Triple("일기",   dhs?.diaryScore,    25f),
                                    Triple("카카오", dhs?.chatTempScore, 20f),
                                    Triple("관계",   dhs?.relationScore, 10f)
                                )
                                items(chips) { (label, score, max) ->
                                    SubScoreChip(
                                        label = label,
                                        score = score,
                                        maxScore = max,
                                        levelColors = levelColors
                                    )
                                }
                            }
                        }
                    }
                }

                // 1. Character mini widget card
                item {
                    val character = uiState.characterState
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToCharacter() },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = character?.type?.emoji ?: "🐱", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = character?.name ?: "솔이",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Lv.${character?.level ?: 1} ${character?.levelName ?: "알"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "탭하여 성장 현황 보기 ›",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { character?.progressRatio ?: 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = WellGold,
                                    trackColor = WellGold.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val todayWp = character?.todayWp ?: 0
                                val nextWp = character?.nextLevelWp ?: 100
                                val totalWp = character?.totalWp ?: 0
                                val remaining = (nextWp - totalWp).coerceAtLeast(0)
                                Text(
                                    text = "오늘 +${todayWp}WP | 다음 단계까지 ${remaining}WP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if ((character?.level ?: 1) == 1 && (character?.totalWp ?: 0) == 0) {
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    color = WellGreen
                                ) {
                                    Text(
                                        text = "NEW",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Greeting + emotion check-in
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                text = "좋은 하루예요, ${uiState.userName.ifBlank { "웰플로우" }}님! ☀",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "오늘 기분은 어때요?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                emojis.forEach { emoji ->
                                    val isSelected = uiState.emotionEmoji == emoji
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (isSelected) Modifier.border(2.dp, WellGreen, CircleShape)
                                                else Modifier
                                            )
                                            .background(
                                                if (isSelected) WellGreen.copy(alpha = 0.1f)
                                                else Color.Transparent
                                            )
                                            .clickable { viewModel.checkIn(emoji) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 24.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Today's progress circle
                item {
                    val completed = uiState.todayCompleted
                    val total = uiState.todayTotal
                    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
                    val percent = (progress * 100).toInt()
                    val remaining = (total - completed).coerceAtLeast(0)

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "오늘의 루틴 진행률",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(100.dp),
                                    strokeWidth = 10.dp,
                                    color = WellGreen,
                                    trackColor = WellGreen.copy(alpha = 0.15f)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$completed/$total",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$percent%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${completed}개 완료 · ${remaining}개 남음",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 4. Routine cards
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "오늘의 루틴",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onNavigateToRoutines) { Text("전체보기 ›") }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val routines = routineUiState.routines
                        if (routines.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "루틴을 추가해보세요!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(routines) { routine ->
                                    Card(
                                        modifier = Modifier.width(140.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (routine.isCompletedToday)
                                                WellGreen.copy(alpha = 0.1f)
                                            else MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = routine.emoji, fontSize = 28.sp)
                                            Text(
                                                text = routine.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                            if (routine.scheduledTime.isNotBlank()) {
                                                Text(
                                                    text = routine.scheduledTime,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Checkbox(
                                                checked = routine.isCompletedToday,
                                                onCheckedChange = {
                                                    routineViewModel.toggleCompleteToday(
                                                        routine.id, !routine.isCompletedToday
                                                    )
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = WellGreen)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Kakao quick access
                item {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = onNavigateToKakao, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "💬 카카오 대화 분석하기")
                        }
                        val chatTemp = uiState.latestChatTemp
                        if (chatTemp != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🌡", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "오늘 대화 ${chatTemp.toInt()}°", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Fixed 광고 배너 (화면 최하단 고정) ────────────────────────────────
        // - BottomNavBar 위에 AdBannerView 위치
        // - Debug 빌드: AdBannerView 내부에서 early-return → 아무것도 표시 안 함
        AdBannerView(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()  // BottomNavBar 바로 위
        )
    }
}

/** 서브스코어 칩 */
@Composable
private fun SubScoreChip(
    label: String,
    score: Float?,
    maxScore: Float,
    levelColors: LevelColorProvider.LevelColors
) {
    val hasData = score != null
    val displayText = if (hasData) "${score!!.toInt()}/${maxScore.toInt()}" else "미기록"

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (hasData) levelColors.accent.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (hasData) levelColors.onContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (hasData) FontWeight.Bold else FontWeight.Normal,
                color = if (hasData) levelColors.accent
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
