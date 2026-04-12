package com.forlks.personal_wellness_routine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Light ColorScheme ───────────────────────────────────────────────────────
private val WellFlowLightColorScheme = lightColorScheme(
    primary              = WellGreen,
    onPrimary            = WellSurface,
    primaryContainer     = WellGreenLight,
    onPrimaryContainer   = WellGreenDark,
    secondary            = WellTeal,
    onSecondary          = WellSurface,
    tertiary             = WellGold,
    background           = WellBackground,
    surface              = WellSurface,
    onBackground         = WellOnSurface,
    onSurface            = WellOnSurface,
    onSurfaceVariant     = WellOnSurfaceVariant,
    surfaceVariant       = WellSurfaceVariant
)

// ── Dark ColorScheme ────────────────────────────────────────────────────────
private val WellFlowDarkColorScheme = darkColorScheme(
    primary              = WellGreenLight,          // 더 밝은 그린
    onPrimary            = Color(0xFF003A00),
    primaryContainer     = WellGreenDark,
    onPrimaryContainer   = WellGreenLight,
    secondary            = Color(0xFF80CBC4),        // teal light
    onSecondary          = Color(0xFF003731),
    tertiary             = WellGold,
    background           = Color(0xFF121412),        // 거의 블랙에 가까운 다크그린 배경
    surface              = Color(0xFF1A1E1A),
    onBackground         = Color(0xFFE2E8E2),
    onSurface            = Color(0xFFE2E8E2),
    onSurfaceVariant     = Color(0xFF9EB49E),
    surfaceVariant       = Color(0xFF2A2E2A),
    error                = Color(0xFFCF6679),
    onError              = Color(0xFF690020)
)

@Composable
fun PersonalWellnessRoutineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) WellFlowDarkColorScheme else WellFlowLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}

// Keep old name as alias for compatibility
@Composable
fun PersonalwellnessroutineTheme(content: @Composable () -> Unit) =
    PersonalWellnessRoutineTheme(content = content)
