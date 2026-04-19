package com.forlks.personal_wellness_routine.ui.screen.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.preferences.AppPreferences
import com.forlks.personal_wellness_routine.util.GoogleAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginChoiceUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginChoiceViewModel @Inject constructor(
    private val googleAuthManager: GoogleAuthManager,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginChoiceUiState())
    val uiState: StateFlow<LoginChoiceUiState> = _uiState.asStateFlow()

    /**
     * Credential Manager로 구글 로그인 시도.
     * @param activityContext Composable의 LocalContext.current (Activity 컨텍스트)
     * @param onSuccess 로그인 성공 시 화면 전환 콜백
     * @param onCancelled 사용자가 취소한 경우 (아무 동작 없이 머물기)
     */
    fun signInWithGoogle(
        activityContext: Context,
        onSuccess: () -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            googleAuthManager.signIn(activityContext).fold(
                onSuccess = { result ->
                    appPreferences.setGoogleLoggedIn(result.email)
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    when (e.message) {
                        "LOGIN_CANCELLED" -> onCancelled()
                        "NO_CREDENTIAL"   -> _uiState.update {
                            it.copy(errorMessage = "등록된 구글 계정이 없습니다.\n기기에서 구글 계정을 먼저 추가해 주세요.")
                        }
                        else -> _uiState.update {
                            it.copy(errorMessage = "로그인에 실패했습니다: ${e.message}")
                        }
                    }
                }
            )
        }
    }

    /**
     * 로그인 없이 시작 — LoginChoice 완료 플래그만 저장.
     */
    fun skipLogin(onDone: () -> Unit) {
        viewModelScope.launch {
            appPreferences.setLoginChoiceDone()
            onDone()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
