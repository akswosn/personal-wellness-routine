package com.forlks.personal_wellness_routine.ui.navigation

sealed class Screen(val route: String) {
    // 전체화면 Compose 스플래시 (항상 최초 진입)
    object Splash : Screen("splash")

    // 최초 진입 선택
    object LoginChoice : Screen("login_choice")

    // Onboarding
    object Onboarding : Screen("onboarding")
    object CharacterSelect : Screen("character_select")

    // Main tabs
    object Home : Screen("home")
    object RoutineList : Screen("routine_list")
    object Diary : Screen("diary")
    object Stats : Screen("stats")
    object Settings : Screen("settings")

    // Routine sub-screens
    object RoutineCreate : Screen("routine_create")
    object RoutineEdit : Screen("routine_edit/{routineId}") {
        fun createRoute(routineId: Long) = "routine_edit/$routineId"
    }

    // Character
    object CharacterProfile : Screen("character_profile")

    // Analysis screens (SCR-AN1~3)
    object RoutineAchievement : Screen("routine_achievement")
    object MindHealth : Screen("mind_health")

    // KakaoTalk analysis
    object KakaoImport : Screen("kakao_import")
    object KakaoCalendar : Screen("kakao_calendar")
    object KakaoAnalyzing : Screen("kakao_analyzing/{fileUri}") {
        fun createRoute(fileUri: String) = "kakao_analyzing/${java.net.URLEncoder.encode(fileUri, "UTF-8")}"
    }
    object KakaoTemperature : Screen("kakao_temperature/{analysisId}") {
        fun createRoute(analysisId: Long) = "kakao_temperature/$analysisId"
    }
    object KakaoRelationship : Screen("kakao_relationship/{analysisId}") {
        fun createRoute(analysisId: Long) = "kakao_relationship/$analysisId"
    }
    object KakaoDiaryDraft : Screen("kakao_diary_draft/{analysisId}") {
        fun createRoute(analysisId: Long) = "kakao_diary_draft/$analysisId"
    }
}
