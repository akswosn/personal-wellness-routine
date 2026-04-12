package com.forlks.personal_wellness_routine.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.forlks.personal_wellness_routine.ui.theme.*

/**
 * 일 건강도 5단계 컬러 시스템
 *
 * Light:
 *   Lv1 (지친날)    → 연한 빨강/핑크 계열
 *   Lv2 (보통날)    → 노란/오렌지 계열
 *   Lv3 (좋은날)    → 연한 그린 계열
 *   Lv4 (엄청좋은날) → 진한 그린 계열
 *   Lv5 (완전코럭)  → 티얼/민트 계열
 *
 * Dark:
 *   동일 톤이지만 어둡게 처리
 */
object LevelColorProvider {

    data class LevelColors(
        val container: Color,   // 배경 컬러
        val onContainer: Color, // 텍스트 컬러
        val accent: Color       // 강조 컬러
    )

    // Light theme level colors
    private val Lv1Light = LevelColors(
        container   = Color(0xFFFFEBEE),
        onContainer = Color(0xFFB71C1C),
        accent      = Color(0xFFEF5350)
    )
    private val Lv2Light = LevelColors(
        container   = Color(0xFFFFF8E1),
        onContainer = Color(0xFFE65100),
        accent      = Color(0xFFFFB300)
    )
    private val Lv3Light = LevelColors(
        container   = Color(0xFFE8F5E9),
        onContainer = Color(0xFF2E7D32),
        accent      = Color(0xFF4CAF50)
    )
    private val Lv4Light = LevelColors(
        container   = Color(0xFFC8E6C9),
        onContainer = Color(0xFF1B5E20),
        accent      = Color(0xFF388E3C)
    )
    private val Lv5Light = LevelColors(
        container   = Color(0xFFB2EBF2),
        onContainer = Color(0xFF006064),
        accent      = Color(0xFF00ACC1)
    )

    // Dark theme level colors
    private val Lv1Dark = LevelColors(
        container   = Color(0xFF4E1212),
        onContainer = Color(0xFFFFCDD2),
        accent      = Color(0xFFEF9A9A)
    )
    private val Lv2Dark = LevelColors(
        container   = Color(0xFF3E2800),
        onContainer = Color(0xFFFFE0B2),
        accent      = Color(0xFFFFCC02)
    )
    private val Lv3Dark = LevelColors(
        container   = Color(0xFF1B5E20),
        onContainer = Color(0xFFC8E6C9),
        accent      = Color(0xFF81C784)
    )
    private val Lv4Dark = LevelColors(
        container   = Color(0xFF1A3A1A),
        onContainer = Color(0xFFA5D6A7),
        accent      = Color(0xFF66BB6A)
    )
    private val Lv5Dark = LevelColors(
        container   = Color(0xFF00363A),
        onContainer = Color(0xFFB2EBF2),
        accent      = Color(0xFF4DD0E1)
    )

    fun get(level: Int, isDark: Boolean): LevelColors {
        return if (isDark) {
            when (level) {
                5    -> Lv5Dark
                4    -> Lv4Dark
                3    -> Lv3Dark
                2    -> Lv2Dark
                else -> Lv1Dark
            }
        } else {
            when (level) {
                5    -> Lv5Light
                4    -> Lv4Light
                3    -> Lv3Light
                2    -> Lv2Light
                else -> Lv1Light
            }
        }
    }
}
