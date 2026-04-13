package com.forlks.personal_wellness_routine.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.forlks.personal_wellness_routine.ui.navigation.Screen

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("홈", Icons.Filled.Home, Screen.Home.route),
    BottomNavItem("루틴", Icons.Filled.List, Screen.RoutineList.route),
    BottomNavItem("일기", Icons.Filled.Book, Screen.Diary.route),
    BottomNavItem("대화", Icons.Filled.Chat, Screen.KakaoImport.route),
    BottomNavItem("통계", Icons.Filled.BarChart, Screen.Stats.route)
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) onNavigate(item.route)
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
