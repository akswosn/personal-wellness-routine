package com.forlks.personal_wellness_routine

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.forlks.personal_wellness_routine.data.preferences.AppPreferences
import com.forlks.personal_wellness_routine.ui.navigation.Screen
import com.forlks.personal_wellness_routine.ui.navigation.WellFlowNavGraph
import com.forlks.personal_wellness_routine.ui.theme.PersonalWellnessRoutineTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // 시스템 스플래시는 최소(~300ms)만 표시 — 실제 풍성한 스플래시는 WellFlowSplashScreen(Compose)가 담당
        val splashStartMs = SystemClock.elapsedRealtime()
        val splashMinMs = 300L

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            SystemClock.elapsedRealtime() - splashStartMs < splashMinMs
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = runBlocking {
            when {
                prefs.isOnboardingDone.first() -> Screen.Home.route
                prefs.isLoginChoiceDone.first() -> Screen.Onboarding.route
                else -> Screen.LoginChoice.route  // 최초 설치 시 로그인 선택 화면
            }
        }

        setContent {
            PersonalWellnessRoutineTheme {
                val navController = rememberNavController()
                WellFlowNavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}
