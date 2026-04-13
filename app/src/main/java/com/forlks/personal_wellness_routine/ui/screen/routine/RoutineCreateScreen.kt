package com.forlks.personal_wellness_routine.ui.screen.routine

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.domain.model.Routine
import com.forlks.personal_wellness_routine.domain.model.RoutineCategory
import com.forlks.personal_wellness_routine.ui.theme.WellGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoutineCreateScreen(
    onBack: () -> Unit,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("💪") }
    var selectedCategory by remember { mutableStateOf(RoutineCategory.HEALTH) }
    var durationMinutes by remember { mutableFloatStateOf(20f) }

    val emojiList = listOf(
        "💪", "🥗", "📚", "💧", "🏃", "🧘", "😴", "🎯",
        "🎨", "🎵", "✍️", "🧹", "🛁", "💊", "🌿", "📖",
        "🏋️", "🚴", "🤸", "🧗"
    )

    val categories = listOf(RoutineCategory.HEALTH, RoutineCategory.MIND, RoutineCategory.STUDY)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("루틴 만들기", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Name input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("루틴 이름", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("예: 아침 스트레칭") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 2. Emoji picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("이모지 선택", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojiList.forEach { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, WellGreen, RoundedCornerShape(10.dp))
                                    else Modifier
                                )
                                .clickable { selectedEmoji = emoji },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) WellGreen.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = if (isSelected) 0.dp else 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = emoji, fontSize = 22.sp)
                            }
                        }
                    }
                }
            }

            // 3. Category chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("카테고리", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WellGreen.copy(alpha = 0.15f),
                                selectedLabelColor = WellGreen
                            )
                        )
                    }
                }
            }

            // 4. Duration slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "소요 시간",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${durationMinutes.toInt()}분",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WellGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    valueRange = 5f..60f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(thumbColor = WellGreen, activeTrackColor = WellGreen)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("5분", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("60분", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Save button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addRoutine(
                            Routine(
                                name = name.trim(),
                                category = selectedCategory,
                                emoji = selectedEmoji,
                                scheduledTime = "",
                                durationMinutes = durationMinutes.toInt()
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WellGreen),
                enabled = name.isNotBlank()
            ) {
                Text("저장하기", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
