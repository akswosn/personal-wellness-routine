package com.forlks.personal_wellness_routine.ui.screen.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.ui.theme.WellGreen

@Composable
fun LoginChoiceScreen(
    onGoogleLogin: () -> Unit,
    onSkip: () -> Unit,
    viewModel: LoginChoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showSkipDialog by remember { mutableStateOf(false) }

    // 그냥 시작 확인 다이얼로그
    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            title = { Text("주의사항") },
            text = {
                Text(
                    text = "로그인 없이 시작하면 다음 기능을 사용할 수 없습니다:\n\n" +
                           "• 구글 드라이브 데이터 백업\n" +
                           "• 기기 변경 시 데이터 복원\n\n" +
                           "나중에 설정 > 계정 및 백업에서 로그인할 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSkipDialog = false
                    viewModel.skipLogin(onSkip)
                }) {
                    Text("그냥 시작하기", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 오류 다이얼로그
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("로그인 오류") },
            text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("확인") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌿", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "마음흐름",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = WellGreen
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "내 루틴과 감정의 흐름을 기록하세요",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(64.dp))

        // 구글 로그인 버튼
        Button(
            onClick = {
                viewModel.signInWithGoogle(
                    activityContext = context,
                    onSuccess = onGoogleLogin,
                    onCancelled = { /* 취소 시 아무 동작 없음 */ }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WellGreen),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "로그인 중...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "🔑  구글로 로그인",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 그냥 시작 버튼
        OutlinedButton(
            onClick = { showSkipDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !uiState.isLoading
        ) {
            Text(
                text = "로그인 없이 시작",
                style = MaterialTheme.typography.titleSmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "구글 로그인 시 드라이브 백업 및 데이터 복원이 가능합니다",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
