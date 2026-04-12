package com.forlks.personal_wellness_routine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val WellFlowColorScheme = lightColorScheme(
    primary = WellGreen,
    onPrimary = WellSurface,
    primaryContainer = WellGreenLight,
    onPrimaryContainer = WellGreenDark,
    secondary = WellTeal,
    onSecondary = WellSurface,
    tertiary = WellGold,
    background = WellBackground,
    surface = WellSurface,
    onBackground = WellOnSurface,
    onSurface = WellOnSurface,
    onSurfaceVariant = WellOnSurfaceVariant,
    surfaceVariant = WellSurfaceVariant
)

@Composable
fun PersonalWellnessRoutineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WellFlowColorScheme,
        typography = Typography,
        content = content
    )
}

// Keep old name as alias for compatibility
@Composable
fun PersonalwellnessroutineTheme(content: @Composable () -> Unit) =
    PersonalWellnessRoutineTheme(content = content)