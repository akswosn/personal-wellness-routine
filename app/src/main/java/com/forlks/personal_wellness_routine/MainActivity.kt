package com.forlks.personal_wellness_routine

import android.os.Bundle
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
        installSplashScreen()
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