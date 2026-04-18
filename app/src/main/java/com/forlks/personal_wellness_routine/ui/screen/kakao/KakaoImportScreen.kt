package com.forlks.personal_wellness_routine.ui.screen.kakao

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.ui.theme.WellGreen
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KakaoImportScreen(
    onBack: () -> Unit,
    onCalendar: () -> Unit = {},
    onAnalysisDone: (Long) -> Unit = {},
    viewModel: KakaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val scope = rememberCoroutineScope()

    // 분석 완료 → 결과 화면 이동
    LaunchedEffect(uiState.isAnalyzing, uiState.currentAnalysis) {
        if (!uiState.isAnalyzing && uiState.currentAnalysis != null) {
            val id = uiState.currentAnalysis!!.id
            if (id > 0L) {
                viewModel.clearCurrentAnalysis()
                onAnalysisDone(id)
            }
        }
    }

    // 파일 선택 → IO 스레드에서 캐시 복사 후 분석 시작
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { originalUri ->
            val fileName = try {
                context.contentResolver.query(
                    originalUri, null, null, null, null
                )?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIdx >= 0) cursor.getString(nameIdx)
                    else "kakao_chat.txt"
                } ?: "kakao_chat.txt"
            } catch (_: Exception) { "kakao_chat.txt" }

            // ① 즉시 "파일 읽는 중…" 인라인 카드 표시
            viewModel.startFilePreparing()

            // ② 파일 복사 + 분석 → IO 스레드 (메인 스레드 블록 없음)
            scope.launch {
                try {
                    val fileUri = withContext(Dispatchers.IO) {
                        val cacheFile = java.io.File(context.cacheDir, fileName)
                        context.contentResolver.openInputStream(originalUri)?.use { input ->
                            cacheFile.outputStream().use { output -> input.copyTo(output) }
                        }
                        android.net.Uri.fromFile(cacheFile)
                    }
                    viewModel.analyzeFile(context, fileUri, fileName)
                } catch (_: Exception) {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            originalUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: SecurityException) { }
                    viewModel.analyzeFile(context, originalUri, fileName)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💬 대화 분석") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = onCalendar) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "분석 캘린더")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // ── ① 분석 정책 안내 카드 ────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WellGreen.copy(alpha = 0.09f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = WellGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "분석 안내",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = WellGreen
                            )
                        }
                        PolicyRow(
                            emoji = "📅",
                            text = "오늘(${today.monthValue}월 ${today.dayOfMonth}일) 날짜의 대화만 분석됩니다"
                        )
                        PolicyRow(
                            emoji = "🔄",
                            text = "21일 주기 최대 2회 분석을 권장합니다\n(과도한 분석은 감정 점수 왜곡 가능)"
                        )
                        PolicyRow(
                            emoji = "🔒",
                            text = "모든 분석은 기기 내에서만 처리됩니다"
                        )
                    }
                }
            }

            // ── ② 진행 중 인라인 프로그레스 ─────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = uiState.isAnalyzing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    AnalysisProgressCard(
                        progress = uiState.analysisProgress,
                        currentLine = uiState.progressCurrentLine,
                        totalLines = uiState.progressTotalLines,
                        today = today
                    )
                }
            }

            // ── ③ 오류 카드 ─────────────────────────────────────────────────
            if (uiState.error != null && !uiState.isAnalyzing) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("⚠️ 분석 실패", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                            Text(
                                uiState.error!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(
                                onClick = { viewModel.clearError() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("닫기")
                            }
                        }
                    }
                }
            }

            // ── ④ 가져오기 안내 + 파일 선택 버튼 ────────────────────────────
            if (!uiState.isAnalyzing) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "카카오톡 파일 가져오는 방법",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            listOf(
                                "1. 카카오톡 채팅방에 들어가세요",
                                "2. 우측 상단 메뉴(≡) 탭",
                                "3. '대화 내보내기' 선택",
                                "4. 텍스트(.txt) 형식으로 저장"
                            ).forEach { step ->
                                Text(
                                    step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "💡 오늘 날짜가 포함된 파일을 선택하세요",
                                style = MaterialTheme.typography.bodySmall,
                                color = WellGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { launcher.launch(arrayOf("text/plain", "text/*", "*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = WellGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "📂  오늘 대화 파일 선택",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // ── ⑤ 최근 분석 파일 ─────────────────────────────────────────────
            if (uiState.recentFiles.isNotEmpty() && !uiState.isAnalyzing) {
                item {
                    Text(
                        "최근 분석 기록",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(uiState.recentFiles.take(3)) { analysis ->
                    val dateStr = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
                        .format(Date(analysis.analyzedAt))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📄", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    analysis.fileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    "$dateStr · ${analysis.totalMessages}건",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onAnalysisDone(analysis.id) }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "결과 보기",
                                    modifier = Modifier.size(20.dp),
                                    tint = WellGreen
                                )
                            }
                        }
                    }
                }
            }

            // ── ⑥ 분석 달력 바로가기 ─────────────────────────────────────────
            if (!uiState.isAnalyzing) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCalendar() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = WellGreen.copy(alpha = 0.08f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("📅", fontSize = 22.sp)
                                Column {
                                    Text(
                                        "대화 분석 달력",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WellGreen
                                    )
                                    Text(
                                        "날짜별 대화 온도 달력 보기",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text("›", fontSize = 20.sp, color = WellGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ── 인라인 프로그레스 카드 ─────────────────────────────────────────────────────

@Composable
private fun AnalysisProgressCard(
    progress: Float,
    currentLine: Int,
    totalLines: Int,
    today: LocalDate
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WellGreen.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("💬", fontSize = 36.sp)
            Text(
                "${today.monthValue}월 ${today.dayOfMonth}일 대화 분석 중…",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // 진행률 바 — totalLines=0이면 indeterminate(파일 복사 중), 아니면 determinate
            if (totalLines <= 0) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = WellGreen,
                    trackColor = WellGreen.copy(alpha = 0.18f)
                )
            } else {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = WellGreen,
                    trackColor = WellGreen.copy(alpha = 0.18f)
                )
            }

            // 퍼센트 + 라인 수
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (totalLines > 0) "$currentLine / $totalLines 줄"
                    else "파일 준비 중…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (totalLines > 0) "${(progress * 100).toInt()}%" else "…",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = WellGreen
                )
            }

            Text(
                if (totalLines > 0) "오늘 날짜의 감정 키워드를 분석하고 있어요 🔍"
                else "파일을 읽고 있어요, 잠시만 기다려주세요 📂",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── 정책 안내 행 ──────────────────────────────────────────────────────────────

@Composable
private fun PolicyRow(emoji: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(emoji, fontSize = 15.sp, modifier = Modifier.padding(top = 1.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
