package com.forlks.personal_wellness_routine.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.forlks.personal_wellness_routine.ui.screen.character.CharacterProfileScreen
import com.forlks.personal_wellness_routine.ui.screen.character.CharacterSelectScreen
import com.forlks.personal_wellness_routine.ui.screen.diary.DiaryScreen
import com.forlks.personal_wellness_routine.ui.screen.diary.MindHealthScreen
import com.forlks.personal_wellness_routine.ui.screen.home.HomeScreen
import com.forlks.personal_wellness_routine.ui.screen.stats.RoutineAchievementScreen
import com.forlks.personal_wellness_routine.ui.screen.kakao.*
import com.forlks.personal_wellness_routine.ui.screen.onboarding.LoginChoiceScreen
import com.forlks.personal_wellness_routine.ui.screen.onboarding.OnboardingScreen
import com.forlks.personal_wellness_routine.ui.screen.routine.RoutineCreateScreen
import com.forlks.personal_wellness_routine.ui.screen.routine.RoutineEditScreen
import com.forlks.personal_wellness_routine.ui.screen.routine.RoutineListScreen
import com.forlks.personal_wellness_routine.ui.screen.settings.SettingsScreen
import com.forlks.personal_wellness_routine.ui.screen.stats.StatsScreen

@Composable
fun WellFlowNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.LoginChoice.route) {
            LoginChoiceScreen(
                onGoogleLogin = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.LoginChoice.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.LoginChoice.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = { navController.navigate(Screen.CharacterSelect.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }}
            )
        }

        composable(Screen.CharacterSelect.route) {
            CharacterSelectScreen(
                onFinish = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.CharacterSelect.route) { inclusive = true }
                }}
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRoutines = { navController.navigate(Screen.RoutineList.route) },
                onNavigateToDiary = { navController.navigate(Screen.Diary.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToKakao = { navController.navigate(Screen.KakaoImport.route) },
                onNavigateToCharacter = { navController.navigate(Screen.CharacterProfile.route) }
            )
        }

        composable(Screen.RoutineList.route) {
            RoutineListScreen(
                onBack = { navController.popBackStack() },
                onCreateRoutine = { navController.navigate(Screen.RoutineCreate.route) },
                onEditRoutine = { id -> navController.navigate(Screen.RoutineEdit.createRoute(id)) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.RoutineCreate.route) {
            RoutineCreateScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.RoutineEdit.route,
            arguments = listOf(navArgument("routineId") { type = NavType.LongType })
        ) { backStack ->
            RoutineEditScreen(
                routineId = backStack.arguments?.getLong("routineId") ?: 0L,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Diary.route) {
            DiaryScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToCharacter = { navController.navigate(Screen.CharacterSelect.route) },
                onNavigateToGoogleLogin = { navController.navigate(Screen.LoginChoice.route) }
            )
        }

        composable(Screen.CharacterProfile.route) {
            CharacterProfileScreen(onBack = { navController.popBackStack() })
        }

        // SCR-AN1 루틴 달성도 분석
        composable(Screen.RoutineAchievement.route) {
            RoutineAchievementScreen(onBack = { navController.popBackStack() })
        }

        // SCR-AN3 마음 건강도
        composable(Screen.MindHealth.route) {
            MindHealthScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.KakaoImport.route) {
            KakaoImportScreen(
                onBack = { navController.popBackStack() },
                onFileSelected = { uri ->
                    navController.navigate(Screen.KakaoAnalyzing.createRoute(uri))
                },
                onCalendar = { navController.navigate(Screen.KakaoCalendar.route) }
            )
        }

        composable(Screen.KakaoCalendar.route) {
            KakaoCalendarScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.KakaoAnalyzing.route,
            arguments = listOf(navArgument("fileUri") { type = NavType.StringType })
        ) { backStack ->
            val encoded = backStack.arguments?.getString("fileUri") ?: ""
            val fileUri = java.net.URLDecoder.decode(encoded, "UTF-8")
            KakaoAnalyzingScreen(
                fileUri = fileUri,
                onAnalysisDone = { id ->
                    navController.navigate(Screen.KakaoTemperature.createRoute(id)) {
                        popUpTo(Screen.KakaoAnalyzing.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.KakaoTemperature.route,
            arguments = listOf(navArgument("analysisId") { type = NavType.LongType })
        ) { backStack ->
            KakaoTemperatureScreen(
                analysisId = backStack.arguments?.getLong("analysisId") ?: 0L,
                onNext = { id -> navController.navigate(Screen.KakaoRelationship.createRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.KakaoRelationship.route,
            arguments = listOf(navArgument("analysisId") { type = NavType.LongType })
        ) { backStack ->
            KakaoRelationshipScreen(
                analysisId = backStack.arguments?.getLong("analysisId") ?: 0L,
                onNext = { id -> navController.navigate(Screen.KakaoDiaryDraft.createRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.KakaoDiaryDraft.route,
            arguments = listOf(navArgument("analysisId") { type = NavType.LongType })
        ) { backStack ->
            KakaoDiaryDraftScreen(
                analysisId = backStack.arguments?.getLong("analysisId") ?: 0L,
                onSaved = { navController.navigate(Screen.Diary.route) {
                    popUpTo(Screen.Home.route)
                }},
                onBack = { navController.popBackStack() }
            )
        }
    }
}
