package com.forlks.personal_wellness_routine.ui.screen.routine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.forlks.personal_wellness_routine.ui.component.BottomNavBar
import com.forlks.personal_wellness_routine.ui.navigation.Screen
import com.forlks.personal_wellness_routine.ui.theme.WellGreen
import com.forlks.personal_wellness_routine.ui.theme.WellOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
    onBack: () -> Unit,
    onCreateRoutine: () -> Unit,
    onEditRoutine: (Long) -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = RoutineCategory.entries.toList()
    val selectedCategory = uiState.selectedCategory

    // Long-press dialog state
    var longPressedRoutine by remember { mutableStateOf<Routine?>(null) }

    if (longPressedRoutine != null) {
        AlertDialog(
            onDismissRequest = { longPressedRoutine = null },
            title = { Text(longPressedRoutine?.name ?: "") },
            text = { Text("루틴을 수정하거나 삭제하시겠어요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        longPressedRoutine?.id?.let { onEditRoutine(it) }
                        longPressedRoutine = null
                    }
                ) {
                    Text("수정")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        longPressedRoutine?.id?.let { viewModel.deleteRoutine(it) }
                        longPressedRoutine = null
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "📋 나의 루틴",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.RoutineList.route,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRoutine,
                containerColor = WellGreen
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "루틴 추가",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category filter tab row
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                edgePadding = 16.dp
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        text = { Text(category.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.routines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "📭", fontSize = 48.sp)
                        Text(
                            text = "루틴이 없어요",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+ 버튼으로 루틴을 추가해보세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.routines, key = { it.id }) { routine ->
                        RoutineListCard(
                            routine = routine,
                            onCheckToggle = {
                                viewModel.toggleCompleteToday(routine.id, !routine.isCompletedToday)
                            },
                            onLongPress = { longPressedRoutine = routine }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineListCard(
    routine: Routine,
    onCheckToggle: () -> Unit,
    onLongPress: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (routine.isCompletedToday)
                WellGreen.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = {},
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Text(
                text = routine.emoji,
                fontSize = 28.sp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Name + time/duration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${routine.scheduledTime} · ${routine.durationMinutes}분",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Streak badge
            if (routine.streak > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WellOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "🔥${routine.streak}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = WellOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Checkbox
            Checkbox(
                checked = routine.isCompletedToday,
                onCheckedChange = { onCheckToggle() },
                colors = CheckboxDefaults.colors(checkedColor = WellGreen)
            )
        }
    }
}
