package com.forlks.personal_wellness_routine.ui.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.ui.theme.WellGreen

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // Logo
        Text("🌿", fontSize = 56.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "마음흐름",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = WellGreen
        )
        Text(
            "내 루틴의 시작",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        // Page Indicator
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == state.step) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(if (index == state.step) WellGreen else Color.LightGray)
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        // Step Content
        when (state.step) {
            0 -> StepName(
                name = state.userName,
                onNameChange = viewModel::setUserName
            )
            1 -> StepGoal(
                selectedGoal = state.selectedGoal,
                onGoalSelect = viewModel::setGoal
            )
            2 -> StepPreview()
        }

        Spacer(Modifier.weight(1f))

        // Button
        Button(
            onClick = {
                if (state.step < 2) viewModel.nextStep()
                else {
                    viewModel.finish()
                    onFinish()
                }
            },
            enabled = state.step != 0 || state.userName.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (state.step < 2) "다음" else "시작하기 →", fontSize = 16.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StepName(name: String, onNameChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("안녕하세요!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("이름이 무엇인가요?", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("이름 입력") },
            placeholder = { Text("홍길동") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun StepGoal(selectedGoal: String, onGoalSelect: (String) -> Unit) {
    val goals = listOf(
        "💪 건강관리" to "HEALTH",
        "🧘 마음챙김" to "MIND",
        "📚 학습향상" to "STUDY"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("목표를 선택하세요", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("나에게 맞는 웰니스 목표를 골라보세요", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        goals.forEach { (label, key) ->
            val selected = selectedGoal == key
            Card(
                onClick = { onGoalSelect(key) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) WellGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, WellGreen) else null,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    label, modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun StepPreview() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("마음흐름으로 할 수 있는 것", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        listOf(
            "📋 루틴 트래킹" to "매일 건강한 습관 관리",
            "📓 감정 일기" to "AI 감정 분석으로 나를 이해",
            "💬 카카오 대화 분석" to "대화 온도계 & 관계 건강도 확인",
            "🐱 캐릭터 성장" to "루틴으로 웰니스 포인트 획득"
        ).forEach { (icon, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(icon.drop(2), fontWeight = FontWeight.Medium)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
