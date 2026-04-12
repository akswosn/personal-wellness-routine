package com.forlks.personal_wellness_routine.ui.screen.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.forlks.personal_wellness_routine.domain.model.DiaryEntry
import com.forlks.personal_wellness_routine.ui.component.BottomNavBar
import com.forlks.personal_wellness_routine.ui.navigation.Screen
import com.forlks.personal_wellness_routine.ui.theme.WellGreen
import com.forlks.personal_wellness_routine.ui.theme.WellSurfaceVariant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val selectedDate = uiState.selectedDate
    val currentEntry = uiState.currentEntry
    val entries = uiState.entries

    // Parse current display year/month from selectedDate
    val selectedLocalDate = remember(selectedDate) {
        runCatching { LocalDate.parse(selectedDate) }.getOrDefault(LocalDate.now())
    }

    var displayYearMonth by remember { mutableStateOf(YearMonth.of(selectedLocalDate.year, selectedLocalDate.month)) }

    var contentText by remember(currentEntry) {
        mutableStateOf(currentEntry?.content ?: "")
    }

    // Sync contentText when currentEntry changes from outside
    LaunchedEffect(currentEntry) {
        contentText = currentEntry?.content ?: ""
    }

    val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Build set of dates that have diary entries for quick lookup
    val entryDateSet = remember(entries) {
        entries.associateBy { it.date }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📓 감정 일기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.Diary.route,
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
            // 1. Month header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        displayYearMonth = displayYearMonth.minusMonths(1)
                    }) {
                        Text("◀", fontSize = 18.sp)
                    }
                    Text(
                        text = "${displayYearMonth.year}년 ${displayYearMonth.monthValue}월",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        displayYearMonth = displayYearMonth.plusMonths(1)
                    }) {
                        Text("▶", fontSize = 18.sp)
                    }
                }
            }

            // 2. 7-column week grid
            item {
                val daysOfWeekLabels = listOf("일", "월", "화", "수", "목", "금", "토")
                val firstDay = displayYearMonth.atDay(1)
                val totalDays = displayYearMonth.lengthOfMonth()
                // firstDayOfWeek: Sunday = 0 ... Saturday = 6 (using DayOfWeek value)
                val startOffset = firstDay.dayOfWeek.value % 7 // Sunday=0

                val cells = buildList {
                    // Empty cells before first day
                    repeat(startOffset) { add(null) }
                    // Actual days
                    for (day in 1..totalDays) {
                        add(displayYearMonth.atDay(day))
                    }
                    // Pad to complete last row
                    val remainder = size % 7
                    if (remainder != 0) repeat(7 - remainder) { add(null) }
                }

                Column {
                    // Day of week header row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        daysOfWeekLabels.forEach { label ->
                            Text(
                                text = label,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // Fixed grid: use chunked rows to avoid nested LazyVerticalGrid issues
                    cells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { date ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (date != null) {
                                        val dateStr = date.format(dateFmt)
                                        val isSelected = dateStr == selectedDate
                                        val entry = entryDateSet[dateStr]

                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) WellGreen else Color.Transparent
                                                )
                                                .clickable {
                                                    viewModel.selectDate(dateStr)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = date.dayOfMonth.toString(),
                                                    fontSize = 11.sp,
                                                    color = if (isSelected) Color.White
                                                    else MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (entry != null) {
                                                    Text(
                                                        text = entry.emotionEmoji,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            // Fill remaining cells in last short row
                            repeat(7 - week.size) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            // 3. Date header
            item {
                Text(
                    text = "${selectedLocalDate.monthValue}월 ${selectedLocalDate.dayOfMonth}일 오늘의 일기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 4. Diary editor
            item {
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    placeholder = { Text("오늘 하루는 어땠나요? 자유롭게 적어보세요.") },
                    minLines = 5
                )
            }

            // 5. AI analysis card
            if (currentEntry != null && currentEntry.emotionScore != 0f) {
                item {
                    val score = currentEntry.emotionScore
                    val emoji = when {
                        score >= 0.5f -> "😄"
                        score >= 0.1f -> "🙂"
                        score > -0.1f -> "😐"
                        score > -0.5f -> "😕"
                        else -> "😢"
                    }
                    val scoreSign = if (score >= 0) "+" else ""
                    val scoreStr = "$scoreSign${"%.2f".format(score)}"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = WellSurfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🤖 AI 분석: $emoji ${currentEntry.emotionLabel} ($scoreStr)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (currentEntry.tags.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    currentEntry.tags.take(5).forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = WellGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "#$tag",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WellGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Kakao draft button
            item {
                OutlinedButton(
                    onClick = {
                        // Apply draft from content placeholder (latestAnalysis not in DiaryUiState)
                        // Placeholder: if there's a draft mechanism, apply it
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💬 오늘 카카오 대화에서 자동 초안 가져오기")
                }
            }

            // 7. Save button
            item {
                Button(
                    onClick = {
                        val entry = DiaryEntry(
                            id = currentEntry?.id ?: 0,
                            date = selectedDate,
                            content = contentText,
                            emotionEmoji = currentEntry?.emotionEmoji ?: "😐",
                            emotionScore = currentEntry?.emotionScore ?: 0f,
                            emotionLabel = currentEntry?.emotionLabel ?: "보통",
                            tags = currentEntry?.tags ?: emptyList(),
                            isAutoGenerated = false
                        )
                        viewModel.saveDiary(entry)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WellGreen)
                ) {
                    Text("일기 저장")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
