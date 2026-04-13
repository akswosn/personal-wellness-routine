package com.forlks.personal_wellness_routine.ui.screen.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.forlks.personal_wellness_routine.ui.theme.WellGreen
import com.forlks.personal_wellness_routine.util.RoutineAchievementCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val GradeGold = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAchievementScreen(
    onBack: () -> Unit,
    viewModel: RoutineAchievementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📈 루틴 달성도") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
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
            // ── 월간 요약 카드 ──────────────────────────────────────────────────
            item {
                AchievementMonthSummaryCard(
                    ym = state.displayYearMonth,
                    score = state.monthlyScore,
                    grade = state.monthlyGrade
                )
            }

            // ── 월 네비게이터 ────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.prevMonth() }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "이전 달")
                    }
                    Text(
                        text = "${state.displayYearMonth.year}년 ${state.displayYearMonth.monthValue}월",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "다음 달")
                    }
                }
            }

            // ── 요일 헤더 ──────────────────────────────────────────────────────
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── 달력 그리드 ────────────────────────────────────────────────────
            item {
                val ym = state.displayYearMonth
                val firstDay = ym.atDay(1)
                val startOffset = firstDay.dayOfWeek.value % 7  // 0=일요일
                val daysInMonth = ym.lengthOfMonth()
                val rows = (startOffset + daysInMonth + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val day = row * 7 + col - startOffset + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                ) {
                                    if (day in 1..daysInMonth) {
                                        val date = ym.atDay(day)
                                        val dateStr = date.format(dateFmt)
                                        val score = state.calendarData[dateStr]  // null = 데이터없음
                                        val isSelected = state.selectedDate == dateStr
                                        val isToday = date == LocalDate.now()
                                        val isFuture = date.isAfter(LocalDate.now())

                                        AchievementDayCell(
                                            day = day,
                                            score = score,
                                            isSelected = isSelected,
                                            isToday = isToday,
                                            isFuture = isFuture,
                                            onClick = {
                                                if (!isFuture) viewModel.selectDate(dateStr)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 선택 날짜 상세 ────────────────────────────────────────────────
            if (state.selectedDate != null) {
                item {
                    SelectedDayDetail(
                        date = state.selectedDate!!,
                        score = state.selectedDayScore,
                        grade = state.selectedDayGrade
                    )
                }
            }

            // ── 범례 ──────────────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple(WellGreen, "80~", "S·A"),
                            Triple(Color(0xFF26C6DA), "50~79", "B·C"),
                            Triple(Color(0xFFEF5350), "~49", "D·F"),
                            Triple(MaterialTheme.colorScheme.surfaceVariant, "-", "미기록")
                        ).forEach { (color, range, grade) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Column {
                                    Text(grade, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(range, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
private fun AchievementMonthSummaryCard(
    ym: java.time.YearMonth,
    score: Int,
    grade: String
) {
    val gradeColor = gradeColor(grade)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = gradeColor.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "${ym.monthValue}월 평균 달성도",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${score}점",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = gradeColor
                )
                Text(
                    RoutineAchievementCalculator.gradeDesc(grade),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(grade, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = gradeColor)
                Text(RoutineAchievementCalculator.gradeEmoji(grade), fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun AchievementDayCell(
    day: Int,
    score: Int?,          // null = 기록없음, 0~100 = 달성도
    isSelected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        score == null || isFuture -> Color.Transparent
        score >= 80 -> WellGreen.copy(alpha = 0.8f)
        score >= 50 -> Color(0xFF26C6DA).copy(alpha = 0.8f)
        score > 0   -> Color(0xFFEF5350).copy(alpha = 0.8f)
        else        -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        score != null && !isFuture && score >= 0 -> Color.White
        isToday    -> MaterialTheme.colorScheme.primary
        isFuture   -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else       -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxSize()
            .clip(CircleShape)
            .background(bgColor)
            .clickable(enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontSize = 12.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (score != null && score >= 0 && !isFuture && !isSelected) {
                Text(
                    text = if (score >= 80) "✓" else "${score}%",
                    fontSize = 7.sp,
                    color = Color.White,
                    lineHeight = 8.sp
                )
            }
        }
    }
}

@Composable
private fun SelectedDayDetail(date: String, score: Int, grade: String) {
    val localDate = runCatching { LocalDate.parse(date) }.getOrNull()
    val displayDate = localDate?.format(DateTimeFormatter.ofPattern("M월 d일 (E)")) ?: date
    val color = if (score >= 0) gradeColor(grade) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                displayDate,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (score >= 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${score}점",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                    Column {
                        Text(
                            text = "등급: $grade",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = color
                        )
                        Text(
                            text = RoutineAchievementCalculator.gradeDesc(grade),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { (score / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
            } else {
                Text(
                    "이 날은 루틴 기록이 없어요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun gradeColor(grade: String): Color = when (grade) {
    "S"  -> Color(0xFFFFD700)
    "A"  -> WellGreen
    "B"  -> Color(0xFF26C6DA)
    "C"  -> Color(0xFFFF9800)
    else -> Color(0xFFEF5350)
}
