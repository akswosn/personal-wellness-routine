package com.forlks.personal_wellness_routine.ui.screen.kakao

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.forlks.personal_wellness_routine.domain.model.DailyChatResult
import com.forlks.personal_wellness_routine.domain.model.TemperatureLevel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KakaoCalendarScreen(
    onBack: () -> Unit,
    viewModel: KakaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val calendarData = uiState.calendarData

    var displayYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    val selectedResult = selectedDate?.let { calendarData[it] }

    val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💬 대화 분석 캘린더") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // ── 월 네비게이터 ──────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { displayYearMonth = displayYearMonth.minusMonths(1) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "이전 달")
                    }
                    Text(
                        text = "${displayYearMonth.year}년 ${displayYearMonth.monthValue}월",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { displayYearMonth = displayYearMonth.plusMonths(1) }) {
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

            // ── 날짜 그리드 ────────────────────────────────────────────────────
            item {
                val firstDay = displayYearMonth.atDay(1)
                val startOffset = firstDay.dayOfWeek.value % 7 // 0=일요일
                val daysInMonth = displayYearMonth.lengthOfMonth()
                val totalCells = startOffset + daysInMonth
                val rows = (totalCells + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val day = cellIndex - startOffset + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                ) {
                                    if (day in 1..daysInMonth) {
                                        val date = displayYearMonth.atDay(day)
                                        val dateStr = date.format(dateFmt)
                                        val result = calendarData[dateStr]
                                        val isSelected = selectedDate == dateStr
                                        val isToday = date == LocalDate.now()

                                        ChatDayCell(
                                            day = day,
                                            result = result,
                                            isSelected = isSelected,
                                            isToday = isToday,
                                            onClick = {
                                                selectedDate = if (selectedDate == dateStr) null else dateStr
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 선택된 날짜 상세 ────────────────────────────────────────────────
            if (selectedResult != null && selectedDate != null) {
                item {
                    DayDetailCard(date = selectedDate!!, result = selectedResult)
                }
            } else if (selectedDate != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = "이 날은 대화 기록이 없어요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── 범례 ──────────────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "대화 온도 범례",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(
                                Pair(Color(0xFFFF8C69), "따뜻함"),
                                Pair(Color(0xFF66BB6A), "보통"),
                                Pair(Color(0xFF42A5F5), "차가움"),
                                Pair(Color(0xFF78909C), "냉랭함")
                            ).forEach { (color, label) ->
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
                                    Text(label, style = MaterialTheme.typography.labelSmall)
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
private fun ChatDayCell(
    day: Int,
    result: DailyChatResult?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val tempColor = result?.let { temperatureColor(it.temperatureLevel) }
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        result != null -> tempColor!!.copy(alpha = 0.75f)
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        result != null -> Color.White
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxSize()
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontSize = 13.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (result != null && !isSelected) {
                Text(text = result.temperatureLevel.emoji, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun DayDetailCard(date: String, result: DailyChatResult) {
    val localDate = runCatching { LocalDate.parse(date) }.getOrNull()
    val displayDate = localDate?.format(DateTimeFormatter.ofPattern("M월 d일 (E)")) ?: date
    val color = temperatureColor(result.temperatureLevel)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(result.temperatureLevel.emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        displayDate,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = result.temperatureLevel.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
                Text(
                    text = "${result.temperature.toInt()}°",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DayStatChip("💬 메시지", "${result.totalMessages}건")
                DayStatChip("😊 긍정", "${result.positiveCount}건")
                DayStatChip("😢 부정", "${result.negativeCount}건")
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("관계 건강도", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${result.relationshipScore.toInt()}점",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinearProgressIndicator(
                    progress = { (result.relationshipScore / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun DayStatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun temperatureColor(level: TemperatureLevel): Color = when (level) {
    TemperatureLevel.WARM   -> Color(0xFFFF8C69)
    TemperatureLevel.NORMAL -> Color(0xFF66BB6A)
    TemperatureLevel.COOL   -> Color(0xFF42A5F5)
    TemperatureLevel.COLD   -> Color(0xFF78909C)
}
