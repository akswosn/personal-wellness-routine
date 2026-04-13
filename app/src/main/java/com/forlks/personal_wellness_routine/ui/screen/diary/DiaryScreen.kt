package com.forlks.personal_wellness_routine.ui.screen.diary

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private const val MIN_CHARS = 50

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

    val selectedLocalDate = remember(selectedDate) {
        runCatching { LocalDate.parse(selectedDate) }.getOrDefault(LocalDate.now())
    }

    var displayYearMonth by remember { mutableStateOf(YearMonth.of(selectedLocalDate.year, selectedLocalDate.month)) }

    // 편집 모드 여부 — 기존 일기가 있으면 조회 모드로 시작
    var isEditing by remember { mutableStateOf(false) }
    // 날짜 바뀌면 편집 모드 초기화
    LaunchedEffect(selectedDate) { isEditing = false }

    var contentText by remember(currentEntry, isEditing) {
        mutableStateOf(if (isEditing) (currentEntry?.content ?: "") else "")
    }
    // currentEntry 갱신 시(저장 직후) 편집 취소 → 조회 모드 전환
    LaunchedEffect(currentEntry) {
        if (!isEditing) contentText = currentEntry?.content ?: ""
    }

    val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val entryDateSet = remember(entries) { entries.associateBy { it.date } }

    val charCount = contentText.length
    val isEnoughChars = charCount >= MIN_CHARS

    // 조회 모드: 오늘이 아닌 날짜도 포함해서 currentEntry 가 있으면 조회
    val hasEntry = currentEntry != null
    val showViewMode = hasEntry && !isEditing

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📓 감정 일기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    TextButton(onClick = { onNavigate(Screen.MindHealth.route) }) {
                        Text("마음 건강도", color = WellGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(currentRoute = Screen.Diary.route, onNavigate = onNavigate)
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
            // 1. 월 네비게이션
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayYearMonth = displayYearMonth.minusMonths(1) }) {
                        Text("◀", fontSize = 18.sp)
                    }
                    Text(
                        "${displayYearMonth.year}년 ${displayYearMonth.monthValue}월",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { displayYearMonth = displayYearMonth.plusMonths(1) }) {
                        Text("▶", fontSize = 18.sp)
                    }
                }
            }

            // 2. 달력 그리드
            item {
                val daysOfWeekLabels = listOf("일", "월", "화", "수", "목", "금", "토")
                val firstDay = displayYearMonth.atDay(1)
                val totalDays = displayYearMonth.lengthOfMonth()
                val startOffset = firstDay.dayOfWeek.value % 7

                val cells = buildList {
                    repeat(startOffset) { add(null) }
                    for (day in 1..totalDays) add(displayYearMonth.atDay(day))
                    val rem = size % 7
                    if (rem != 0) repeat(7 - rem) { add(null) }
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        daysOfWeekLabels.forEach { label ->
                            Text(
                                label,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    cells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { date ->
                                Box(
                                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp),
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
                                                .background(if (isSelected) WellGreen else Color.Transparent)
                                                .clickable { viewModel.selectDate(dateStr) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    date.dayOfMonth.toString(), fontSize = 11.sp,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (entry != null) Text(entry.emotionEmoji, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            repeat(7 - week.size) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            // 3. 날짜 헤더 + 수정/삭제 버튼 (조회 모드)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${selectedLocalDate.monthValue}월 ${selectedLocalDate.dayOfMonth}일 일기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (showViewMode) {
                        TextButton(onClick = {
                            contentText = currentEntry?.content ?: ""
                            isEditing = true
                        }) {
                            Text("수정", color = WellGreen, fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(onClick = {
                            currentEntry?.let { viewModel.deleteDiary(it) }
                        }) {
                            Text("삭제", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // 4-A. 조회 모드: 등록된 일기 노출
            if (showViewMode && currentEntry != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 감정 배지
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(currentEntry.emotionEmoji.ifBlank { "😐" }, fontSize = 22.sp)
                                Text(
                                    currentEntry.emotionLabel.ifBlank { "보통" },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WellGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            HorizontalDivider()
                            // 일기 본문
                            Text(
                                text = currentEntry.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                            // 태그
                            if (currentEntry.tags.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    currentEntry.tags.take(5).forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = WellGreen.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                "#$tag",
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

            // 4-B. 편집/작성 모드
            if (!showViewMode) {
                // 입력 필드 + 초기화
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isEditing) "일기 수정 중" else "오늘 일기 쓰기",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (contentText.isNotBlank()) {
                                TextButton(onClick = { contentText = "" }) {
                                    Text("초기화", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = contentText,
                            onValueChange = { contentText = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                            placeholder = { Text("오늘 하루는 어땠나요? 자유롭게 적어보세요.\n(50자 이상 작성하면 마음 건강도를 분석할 수 있어요)") },
                            minLines = 5
                        )
                        // 50자 카운터
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isEnoughChars) {
                                Text(
                                    "조금 더 작성하면 마음 건강도를 분석할 수 있어요 ✍️",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    "마음 건강도 분석 가능 ✅",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WellGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                "$charCount / $MIN_CHARS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isEnoughChars) WellGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 마음 건강도 미리보기
                item {
                    AnimatedVisibility(
                        visible = isEnoughChars,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        MindHealthPreviewCard(
                            emotionScore = currentEntry?.emotionScore ?: 0f,
                            emotionLabel = currentEntry?.emotionLabel ?: ""
                        )
                    }
                }

                // 저장/취소 버튼
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                isEditing = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isEnoughChars,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WellGreen,
                                disabledContainerColor = WellGreen.copy(alpha = 0.38f)
                            )
                        ) {
                            Text(if (isEnoughChars) "일기 저장  (+3 WP)" else "50자 이상 작성해야 저장할 수 있어요")
                        }
                        if (isEditing) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("취소")
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun MindHealthPreviewCard(emotionScore: Float, emotionLabel: String) {
    val (previewEmoji, previewLevel) = when {
        emotionScore >= 0.3f -> "☀️" to "밝음"
        emotionScore >= 0f   -> "⛅" to "보통"
        else                 -> "🌧" to "흐림"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WellGreen.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(previewEmoji, fontSize = 28.sp)
            Column {
                Text(
                    "마음 건강도 미리보기",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "현재 감정 → $previewLevel",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = WellGreen
                )
                if (emotionLabel.isNotEmpty()) {
                    Text(
                        emotionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
