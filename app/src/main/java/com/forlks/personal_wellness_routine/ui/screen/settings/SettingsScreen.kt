package com.forlks.personal_wellness_routine.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.BuildConfig
import com.forlks.personal_wellness_routine.ui.screen.character.CharacterViewModel
import com.forlks.personal_wellness_routine.ui.screen.onboarding.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToCharacter: () -> Unit,
    onNavigateToGoogleLogin: () -> Unit = {},
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
    characterViewModel: CharacterViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val onboardingState by onboardingViewModel.uiState.collectAsState()
    val characterUiState by characterViewModel.uiState.collectAsState()
    val characterState = characterUiState.characterState
    val settingsState by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 이름 상태
    var userName by remember { mutableStateOf("") }
    LaunchedEffect(onboardingState.userName) {
        if (userName.isEmpty() && onboardingState.userName.isNotEmpty()) {
            userName = onboardingState.userName
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙ 설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── Section: 프로필 ──────────────────────────────────────────────
            item { SectionHeader(title = "프로필") }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "이름",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = userName,
                        onValueChange = {
                            userName = it
                            onboardingViewModel.setUserName(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("이름을 입력하세요") },
                        singleLine = true
                    )
                    Button(
                        onClick = { onboardingViewModel.finish() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("저장")
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ── Section: 캐릭터 ──────────────────────────────────────────────
            item { SectionHeader(title = "캐릭터") }

            item {
                ListItem(
                    headlineContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = characterState?.type?.emoji ?: "🐱", fontSize = 32.sp)
                            Column {
                                Text(
                                    text = characterState?.name ?: "솔이",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Lv.${characterState?.level ?: 1} ${characterState?.levelName ?: "알"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    trailingContent = {
                        TextButton(onClick = onNavigateToCharacter) { Text("캐릭터 변경") }
                    }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ── Section: 계정 및 백업 ─────────────────────────────────────────
            item { SectionHeader(title = "계정 및 백업") }

            item {
                ListItem(
                    headlineContent = { Text("구글 계정") },
                    supportingContent = {
                        Text(
                            text = if (settingsState.isGoogleLoggedIn)
                                settingsState.googleEmail
                            else
                                "로그인되지 않음",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        if (settingsState.isGoogleLoggedIn) {
                            TextButton(
                                onClick = {
                                    settingsViewModel.signOut(context)
                                }
                            ) {
                                Text("로그아웃", color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            TextButton(onClick = onNavigateToGoogleLogin) {
                                Text("로그인")
                            }
                        }
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("구글 드라이브로 내보내기") },
                    supportingContent = { Text("데이터를 구글 드라이브에 백업합니다") },
                    trailingContent = {
                        IconButton(
                            onClick = { /* TODO: Drive export */ },
                            enabled = settingsState.isGoogleLoggedIn
                        ) {
                            Icon(
                                Icons.Filled.CloudUpload,
                                contentDescription = "내보내기",
                                tint = if (settingsState.isGoogleLoggedIn)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("구글 드라이브에서 가져오기") },
                    supportingContent = { Text("백업 데이터를 복원합니다") },
                    trailingContent = {
                        IconButton(
                            onClick = { /* TODO: Drive import */ },
                            enabled = settingsState.isGoogleLoggedIn
                        ) {
                            Icon(
                                Icons.Filled.CloudDownload,
                                contentDescription = "가져오기",
                                tint = if (settingsState.isGoogleLoggedIn)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                )
            }

            // 로그인 안내 문구
            if (!settingsState.isGoogleLoggedIn) {
                item {
                    Text(
                        text = "구글 로그인 후 내보내기/가져오기를 사용할 수 있습니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ── Section: 알림 — 숨김 처리 (WorkManager 미구현) ─────────────────
            // item { SectionHeader(title = "알림") }
            // item { ListItem(...) }

            // ── Section: 정보 ────────────────────────────────────────────────
            item { SectionHeader(title = "정보") }

            item {
                ListItem(
                    headlineContent = { Text("앱 버전") },
                    trailingContent = {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("오픈소스 라이선스") },
                    modifier = Modifier.clickable { }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
