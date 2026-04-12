package com.forlks.personal_wellness_routine.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val userName: String = "",
    val step: Int = 0,  // 0 = name, 1 = goal, 2 = preview
    val selectedGoal: String = "HEALTH"
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun setGoal(goal: String) {
        _uiState.update { it.copy(selectedGoal = goal) }
    }

    fun nextStep() {
        _uiState.update { it.copy(step = (it.step + 1).coerceAtMost(2)) }
    }

    fun finish() {
        viewModelScope.launch {
            val currentState = _uiState.value
            appPreferences.saveUserName(currentState.userName)
            appPreferences.setOnboardingDone()
        }
    }

    // Keep old signature for compatibility
    fun finish(onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            appPreferences.saveUserName(currentState.userName)
            appPreferences.setOnboardingDone()
            onComplete()
        }
    }
}
