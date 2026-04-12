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
        // 스플래시 시작 시각 기록
        val splashStartMs = SystemClock.elapsedRealtime()
        val splashMinMs = 2_000L   // 최소 2초 표시

        val splashScreen = installSplashScreen()

        // keepOnScreen: 2초가 채워질 때까지 스플래시 유지
        splashScreen.setKeepOnScreenCondition {
            SystemClock.elapsedRealtime() - splashStartMs < splashMinMs
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = runBlocking {
            if (prefs.isOnboardingDone.first()) Screen.Home.route
            else Screen.Onboarding.route
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
