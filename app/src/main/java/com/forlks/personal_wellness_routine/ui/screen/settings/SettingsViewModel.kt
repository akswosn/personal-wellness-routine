package com.forlks.personal_wellness_routine.ui.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.preferences.AppPreferences
import com.forlks.personal_wellness_routine.util.GoogleAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isGoogleLoggedIn: Boolean = false,
    val googleEmail: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appPreferences.isGoogleLoggedIn,
                appPreferences.googleAccountEmail
            ) { loggedIn, email ->
                SettingsUiState(isGoogleLoggedIn = loggedIn, googleEmail = email)
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            googleAuthManager.signOut()
            appPreferences.setGoogleLoggedOut()
        }
    }
}
