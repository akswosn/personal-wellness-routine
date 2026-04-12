package com.forlks.personal_wellness_routine.ui.screen.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.db.entity.WellnessPointHistoryEntity
import com.forlks.personal_wellness_routine.data.preferences.AppPreferences
import com.forlks.personal_wellness_routine.data.repository.WellnessPointRepository
import com.forlks.personal_wellness_routine.domain.model.CharacterState
import com.forlks.personal_wellness_routine.domain.model.CharacterType
import com.forlks.personal_wellness_routine.domain.model.calculateCharacterLevel
import com.forlks.personal_wellness_routine.domain.model.nextLevelWp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CharacterUiState(
    val characterState: CharacterState? = null,
    val recentHistory: List<WellnessPointHistoryEntity> = emptyList()
)

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val wellnessPointRepository: WellnessPointRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterUiState())
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    init {
        observeCharacterState()
        loadRecentHistory()
    }

    private fun observeCharacterState() {
        viewModelScope.launch {
            combine(
                wellnessPointRepository.totalPoints,
                appPreferences.characterType,
                appPreferences.characterName
            ) { totalWp, charTypeStr, charName ->
                Triple(totalWp, charTypeStr, charName)
            }.collect { (totalWp, charTypeStr, charName) ->
                val charType = try {
                    CharacterType.valueOf(charTypeStr)
                } catch (_: IllegalArgumentException) {
                    CharacterType.CAT
                }
                val todayWp = wellnessPointRepository.getTodayPoints()
                val (level, levelName) = calculateCharacterLevel(totalWp)
                val nextWp = nextLevelWp(totalWp)
                val characterState = CharacterState(
                    type = charType,
                    name = charName,
                    totalWp = totalWp,
                    level = level,
                    levelName = levelName,
                    nextLevelWp = nextWp,
                    todayWp = todayWp
                )
                _uiState.update { it.copy(characterState = characterState) }
            }
        }
    }

    private fun loadRecentHistory() {
        viewModelScope.launch {
            val history = wellnessPointRepository.getRecentHistory(limit = 20)
            _uiState.update { it.copy(recentHistory = history) }
        }
    }

    fun refresh() {
        loadRecentHistory()
    }

    suspend fun saveCharacter(type: String, name: String) {
        appPreferences.saveCharacter(type, name)
    }
}
