package com.forlks.personal_wellness_routine.ui.screen.character

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.data.preferences.AppPreferences
import com.forlks.personal_wellness_routine.domain.model.CharacterType
import com.forlks.personal_wellness_routine.ui.theme.WellGreen
import com.forlks.personal_wellness_routine.ui.theme.WellSurfaceVariant
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSelectScreen(
    onFinish: () -> Unit,
    prefs: AppPreferences? = null
) {
    var selectedType by remember { mutableStateOf(CharacterType.CAT) }
    var showNameSheet by remember { mutableStateOf(false) }
    var characterName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Inject prefs via ViewModel wrapper
    val viewModel: CharacterViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Text("🌿", fontSize = 32.sp)
        Text("내 캐릭터를 선택하세요", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("함께 성장할 웰니스 파트너예요 🌱", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(24.dp))

        // 3×2 Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CharacterType.entries) { type ->
                CharacterCard(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = { selectedType = type }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Hint card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WellSurfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(selectedType.emoji, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(selectedType.displayName, fontWeight = FontWeight.Bold)
                    Text(selectedType.recommendFor, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                characterName = selectedType.displayName
                showNameSheet = true
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("${selectedType.displayName} 선택하기 →", fontSize = 16.sp)
        }

        TextButton(onClick = onFinish) {
            Text("건너뛰기")
        }
        Spacer(Modifier.height(16.dp))
    }

    // Naming BottomSheet
    if (showNameSheet) {
        ModalBottomSheet(onDismissRequest = { showNameSheet = false }) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(selectedType.emoji, fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("캐릭터 이름을 지어주세요", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = characterName,
                    onValueChange = { characterName = it },
                    label = { Text("이름") },
                    placeholder = { Text(selectedType.displayName) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        val name = characterName.ifBlank { selectedType.displayName }
                        scope.launch {
                            viewModel.saveCharacter(selectedType.name, name)
                            showNameSheet = false
                            onFinish()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("완료")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CharacterCard(
    type: CharacterType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) WellGreen.copy(alpha = 0.15f) else WellSurfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, WellGreen) else null,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 4.dp, top = 4.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(WellGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", fontSize = 10.sp, color = androidx.compose.ui.graphics.Color.White)
                    }
                } else {
                    Spacer(Modifier.height(22.dp))
                }
                Text(type.emoji, fontSize = 32.sp)
                Text(type.displayName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Text(type.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
