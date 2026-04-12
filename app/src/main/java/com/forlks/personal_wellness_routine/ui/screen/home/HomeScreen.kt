package com.forlks.personal_wellness_routine.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.forlks.personal_wellness_routine.ui.component.BottomNavBar
import com.forlks.personal_wellness_routine.ui.navigation.Screen
import com.forlks.personal_wellness_routine.ui.screen.routine.RoutineViewModel
import com.forlks.personal_wellness_routine.ui.theme.WellGold
import com.forlks.personal_wellness_routine.ui.theme.WellGreen
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

    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREAN)
    val dateString = today.format(dateFormatter)

    val emojis = listOf("😢", "😔", "😐", "🙂", "😊")

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
                        text = "🌿 WellFlow",
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
                        Screen.Home.route -> { /* already here */ }
                        Screen.RoutineList.route -> onNavigateToRoutines()
                        Screen.Diary.route -> onNavigateToDiary()
                        Screen.Stats.route -> onNavigateToStats()
                        Screen.Settings.route -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                                Text(
                                    text = character?.type?.emoji ?: "🐱",
                                    fontSize = 36.sp
                                )
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
                        // NEW badge
                        if ((character?.level ?: 1) == 1 && (character?.totalWp ?: 0) == 0) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val greeting = when (LocalDate.now().dayOfWeek.value) {
                            else -> "좋은 아침이에요"
                        }
                        Text(
                            text = "$greeting, ${uiState.userName.ifBlank { "웰플로우" }}님! ☀",
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
                                            if (isSelected) Modifier.border(
                                                2.dp,
                                                WellGreen,
                                                CircleShape
                                            )
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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

            // 4. Routine cards (horizontal scrollable LazyRow)
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
                        TextButton(onClick = onNavigateToRoutines) {
                            Text("전체보기 ›")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val routines = routineUiState.routines
                    if (routines.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
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
                                    modifier = Modifier
                                        .width(140.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (routine.isCompletedToday)
                                            WellGreen.copy(alpha = 0.1f)
                                        else
                                            MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
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
                                        Text(
                                            text = routine.scheduledTime,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Checkbox(
                                            checked = routine.isCompletedToday,
                                            onCheckedChange = {
                                                routineViewModel.toggleCompleteToday(
                                                    routine.id,
                                                    !routine.isCompletedToday
                                                )
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = WellGreen
                                            )
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onNavigateToKakao,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "💬 카카오 대화 분석하기")
                    }
                    val chatTemp = uiState.latestChatTemp
                    if (chatTemp != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🌡", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "오늘 대화 ${chatTemp.toInt()}°",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // 6. AdMob banner placeholder
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📢 AdMob 배너 광고",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
