package com.forlks.personal_wellness_routine.ui.screen.diary

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
import com.forlks.personal_wellness_routine.util.MindHealthCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindHealthScreen(
    onBack: () -> Unit,
    viewModel: MindHealthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = mindHealthColor(state.score))
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
                MindHealthSummaryCard(
                    ym = state.displayYearMonth,
                    score = state.score,
                    level = state.level,
                    levelEmoji = state.levelEmoji,
                    insight = state.insight
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
                val startOffset = firstDay.dayOfWeek.value % 7
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
                                        val score = state.calendarData[dateStr]
                                        val isSelected = state.selectedDate == dateStr
                                        val isToday = date == LocalDate.now()
                                        val isFuture = date.isAfter(LocalDate.now())

                                        MindHealthDayCell(
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
                    MindHealthDayDetail(
                        date = state.selectedDate!!,
                        score = state.selectedDayScore,
                        emoji = state.selectedDayEmoji
                    )
                }
            }

            // ── 전문가 상담 배너 ────────────────────────────────────────────────
            if (state.showCounselingBanner) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF29B6F6).copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "💙 마음이 많이 힘드신가요?",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF29B6F6)
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
                            Triple(Color(0xFFFFC107), "80~", "매우좋음"),
                            Triple(Color(0xFF4CAF50), "60~79", "좋음"),
                            Triple(Color(0xFF42A5F5), "40~59", "보통"),
                            Triple(Color(0xFFFF9800), "20~39", "주의")
                        ).forEach { (color, range, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Column {
                                    Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(range, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // ── 데이터 부족 안내 ────────────────────────────────────────────────
            if (state.score < 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📓", fontSize = 36.sp)
                            Text(
                                "50자 이상 일기를 작성하면\n마음 건강도를 분석할 수 있어요",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun MindHealthSummaryCard(
    ym: java.time.YearMonth,
    score: Int,
    level: String,
    levelEmoji: String,
    insight: String
) {
    val color = mindHealthColor(score)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "${ym.monthValue}월 마음 건강도",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (score >= 0) "${score}%" else "-",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(levelEmoji, fontSize = 20.sp)
                        Text(
                            level,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    if (insight.isNotBlank()) {
                        Text(
                            insight,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MindHealthDayCell(
    day: Int,
    score: Int?,
    isSelected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit
) {
    val cellColor = mindHealthColor(score ?: -1)
    val bgColor = when {
        isSelected          -> MaterialTheme.colorScheme.primary
        score != null && score >= 0 && !isFuture -> cellColor.copy(alpha = 0.75f)
        else                -> Color.Transparent
    }
    val textColor = when {
        isSelected          -> MaterialTheme.colorScheme.onPrimary
        score != null && score >= 0 && !isFuture -> Color.White
        isToday             -> MaterialTheme.colorScheme.primary
        isFuture            -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else                -> MaterialTheme.colorScheme.onSurface
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
                    text = MindHealthCalculator.levelEmoji(score),
                    fontSize = 7.sp,
                    lineHeight = 8.sp
                )
            }
        }
    }
}

@Composable
private fun MindHealthDayDetail(date: String, score: Int, emoji: String) {
    val localDate = runCatching { LocalDate.parse(date) }.getOrNull()
    val displayDate = localDate?.format(DateTimeFormatter.ofPattern("M월 d일 (E)")) ?: date
    val color = mindHealthColor(score)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(displayDate, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (score >= 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = emoji, fontSize = 32.sp)
                    Column {
                        Text(
                            text = "${score}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = color
                        )
                        Text(
                            MindHealthCalculator.level(score),
                            style = MaterialTheme.typography.bodySmall,
                            color = color
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
                    "이 날은 일기 기록이 없어요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

internal fun mindHealthColor(score: Int): Color = when {
    score >= 80 -> Color(0xFFFFC107)
    score >= 60 -> Color(0xFF4CAF50)
    score >= 40 -> Color(0xFF42A5F5)
    score >= 20 -> Color(0xFFFF9800)
    score >= 0  -> Color(0xFFEF5350)
    else        -> Color(0xFFBDBDBD)
}
