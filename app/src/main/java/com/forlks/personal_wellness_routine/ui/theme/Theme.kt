package com.forlks.personal_wellness_routine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Light ColorScheme — Sage & Mint ────────────────────────────────────────
private val WellFlowLightColorScheme = lightColorScheme(
    primary              = WellGreen,           // 세이지 그린
    onPrimary            = WellSurface,
    primaryContainer     = WellGreenLight,       // 소프트 세이지 컨테이너
    onPrimaryContainer   = WellGreenDark,
    secondary            = WellTeal,             // 소프트 티얼
    onSecondary          = WellSurface,
    tertiary             = WellGold,             // 따뜻한 골드
    background           = WellBackground,       // 연한 세이지 배경
    surface              = WellSurface,
    onBackground         = WellOnSurface,
    onSurface            = WellOnSurface,
    onSurfaceVariant     = WellOnSurfaceVariant,
    surfaceVariant       = WellSurfaceVariant
)

// ── Dark ColorScheme ────────────────────────────────────────────────────────
private val WellFlowDarkColorScheme = darkColorScheme(
    primary              = WellGreenLight,        // 다크모드: 밝은 세이지
    onPrimary            = Color(0xFF0D2E12),
    primaryContainer     = WellGreenDark,
    onPrimaryContainer   = WellGreenLight,
    secondary            = Color(0xFF7ABDB4),     // 밝은 티얼
    onSecondary          = Color(0xFF003730),
    tertiary             = WellGold,
    background           = Color(0xFF111811),     // 거의 블랙에 가까운 다크 배경
    surface              = Color(0xFF181D18),     // 다크 서피스
    onBackground         = Color(0xFFE0E7E0),
    onSurface            = Color(0xFFE0E7E0),
    onSurfaceVariant     = Color(0xFF9DB4A0),
    surfaceVariant       = Color(0xFF282E28),
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

// 하위 호환성 별칭
@Composable
fun PersonalwellnessroutineTheme(content: @Composable () -> Unit) =
    PersonalWellnessRoutineTheme(content = content)
