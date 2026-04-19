package com.forlks.personal_wellness_routine.util

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Sign-In via Credential Manager API.
 *
 * 사전 준비사항 (Google Cloud Console):
 *  1. OAuth 2.0 클라이언트 ID 생성 (웹 애플리케이션 타입)
 *  2. Android 앱용 OAuth 클라이언트 생성 (패키지명 + SHA-1 지문 등록)
 *  3. [WEB_CLIENT_ID]에 웹 클라이언트 ID 입력
 *
 * Drive 백업 연동 시 추가 스코프 필요:
 *  - https://www.googleapis.com/auth/drive.appdata
 */
@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // 개발용 OAuth 클라이언트 ID (project: well-flow, installed app type)
        // 운영 배포 시 Play Store 등록 후 웹 앱 타입 Client ID로 교체 필요
        const val WEB_CLIENT_ID = "599273408114-h9jmti2d3n6nb826h9ja1vdjh8a7rspp.apps.googleusercontent.com"
    }

    private val credentialManager = CredentialManager.create(context)

    data class GoogleSignInResult(
        val idToken: String,
        val email: String,
        val displayName: String?
    )

    sealed class SignInError {
        object Cancelled : SignInError()
        object NoCredential : SignInError()
        data class Unknown(val message: String?) : SignInError()
    }

    /**
     * 구글 로그인 시도.
     * @param activityContext 반드시 Activity 컨텍스트 사용 (Fragment/Composable의 LocalContext.current)
     */
    suspend fun signIn(activityContext: Context): Result<GoogleSignInResult> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)   // 모든 계정 표시
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)            // 계정 선택 UI 강제 표시
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialResponse = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val googleCredential = GoogleIdTokenCredential.createFrom(
                credentialResponse.credential.data
            )

            Result.success(
                GoogleSignInResult(
                    idToken = googleCredential.idToken,
                    email = googleCredential.id,
                    displayName = googleCredential.displayName
                )
            )
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("LOGIN_CANCELLED"))
        } catch (e: NoCredentialException) {
            Result.failure(Exception("NO_CREDENTIAL"))
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 구글 로그아웃 (Credential Manager 상태 초기화).
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
