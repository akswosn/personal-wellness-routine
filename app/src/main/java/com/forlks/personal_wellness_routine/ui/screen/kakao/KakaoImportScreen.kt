package com.forlks.personal_wellness_routine.ui.screen.kakao

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.ui.theme.WellGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KakaoImportScreen(
    onBack: () -> Unit,
    onFileSelected: (String) -> Unit,
    viewModel: KakaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 내부저장소 권한 문제 근본 해결:
    // content:// URI 권한은 화면 전환 후 만료될 수 있으므로
    // 파일 선택 직후 앱 캐시 디렉터리로 즉시 복사 → file:// URI 전달
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { originalUri ->
            // 원본 파일 이름 추출
            val fileName = try {
                context.contentResolver.query(
                    originalUri, null, null, null, null
                )?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIdx >= 0) cursor.getString(nameIdx)
                    else "kakao_chat.txt"
                } ?: "kakao_chat.txt"
            } catch (_: Exception) { "kakao_chat.txt" }

            // 캐시 디렉터리로 파일 복사 (권한 문제 우회)
            try {
                val cacheFile = java.io.File(context.cacheDir, fileName)
                context.contentResolver.openInputStream(originalUri)?.use { input ->
                    cacheFile.outputStream().use { output -> input.copyTo(output) }
                }
                onFileSelected(android.net.Uri.fromFile(cacheFile).toString())
            } catch (_: Exception) {
                // 복사 실패 시 원본 URI 로 fallback (영구 권한 시도)
                try {
                    context.contentResolver.takePersistableUriPermission(
                        originalUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) { }
                onFileSelected(originalUri.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💬 대화 분석") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
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
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Instruction card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "카카오톡 대화 파일을 가져오세요",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val steps = listOf(
                            "1. 카카오톡 앱을 열고 채팅방에 들어가세요",
                            "2. 우측 상단 메뉴(≡)를 탭하세요",
                            "3. '대화 내보내기'를 선택하세요",
                            "4. 텍스트 형식(.txt)으로 저장하세요"
                        )
                        steps.forEach { step ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "카카오톡 → 채팅방 → 메뉴 → 대화 내보내기 → .txt",
                            style = MaterialTheme.typography.bodySmall,
                            color = WellGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // File picker button
            item {
                Button(
                    onClick = { launcher.launch(arrayOf("text/plain", "text/*", "*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📂 .txt 파일 선택",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            // Recent files section
            if (uiState.recentFiles.isNotEmpty()) {
                item {
                    Text(
                        text = "최근 가져온 파일",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(uiState.recentFiles.take(3)) { analysis ->
                    val dateStr = SimpleDateFormat(
                        "yyyy.MM.dd",
                        Locale.getDefault()
                    ).format(Date(analysis.analyzedAt))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                            Text(
                                text = "📄",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = analysis.fileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onFileSelected(analysis.id.toString()) }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "분석 보기",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Privacy banner
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = WellGreen.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔒 모든 분석은 기기 내에서만 처리됩니다. 서버 전송 없음.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
