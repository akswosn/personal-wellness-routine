package com.forlks.personal_wellness_routine.domain

import com.forlks.personal_wellness_routine.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class WpCalculationTest {

    // ── calculateCharacterLevel ───────────────────────────────────────────────

    @Test
    fun `calculateCharacterLevel - 0 WP는 Lv1 알`() {
        val (level, name) = calculateCharacterLevel(0)
        assertEquals(1, level)
        assertEquals("알", name)
    }

    @Test
    fun `calculateCharacterLevel - 99 WP는 Lv1 알`() {
        val (level, name) = calculateCharacterLevel(99)
        assertEquals(1, level)
        assertEquals("알", name)
    }

    @Test
    fun `calculateCharacterLevel - 100 WP는 Lv2 아기`() {
        val (level, name) = calculateCharacterLevel(100)
        assertEquals(2, level)
        assertEquals("아기", name)
    }

    @Test
    fun `calculateCharacterLevel - 249 WP는 Lv2 아기`() {
        val (level, name) = calculateCharacterLevel(249)
        assertEquals(2, level)
        assertEquals("아기", name)
    }

    @Test
    fun `calculateCharacterLevel - 250 WP는 Lv3 성장`() {
        val (level, name) = calculateCharacterLevel(250)
        assertEquals(3, level)
        assertEquals("성장", name)
    }

    @Test
    fun `calculateCharacterLevel - 499 WP는 Lv3 성장`() {
        val (level, name) = calculateCharacterLevel(499)
        assertEquals(3, level)
        assertEquals("성장", name)
    }

    @Test
    fun `calculateCharacterLevel - 500 WP는 Lv4 성숙`() {
        val (level, name) = calculateCharacterLevel(500)
        assertEquals(4, level)
        assertEquals("성숙", name)
    }

    @Test
    fun `calculateCharacterLevel - 749 WP는 Lv4 성숙`() {
        val (level, name) = calculateCharacterLevel(749)
        assertEquals(4, level)
        assertEquals("성숙", name)
    }

    @Test
    fun `calculateCharacterLevel - 750 WP 이상 Lv5 레전드`() {
        val (level, name) = calculateCharacterLevel(750)
        assertEquals(5, level)
        assertEquals("레전드", name)
    }

    @Test
    fun `calculateCharacterLevel - 9999 WP도 Lv5 레전드`() {
        val (level, name) = calculateCharacterLevel(9999)
        assertEquals(5, level)
        assertEquals("레전드", name)
    }

    // ── nextLevelWp ───────────────────────────────────────────────────────────

    @Test
    fun `nextLevelWp - 0 WP 다음 목표 100`() {
        assertEquals(100, nextLevelWp(0))
    }

    @Test
    fun `nextLevelWp - 99 WP 다음 목표 100`() {
        assertEquals(100, nextLevelWp(99))
    }

    @Test
    fun `nextLevelWp - 100 WP 다음 목표 250`() {
        assertEquals(250, nextLevelWp(100))
    }

    @Test
    fun `nextLevelWp - 250 WP 다음 목표 500`() {
        assertEquals(500, nextLevelWp(250))
    }

    @Test
    fun `nextLevelWp - 500 WP 다음 목표 750`() {
        assertEquals(750, nextLevelWp(500))
    }

    @Test
    fun `nextLevelWp - 750 WP 이상 다음 목표 1000`() {
        assertEquals(1000, nextLevelWp(750))
        assertEquals(1000, nextLevelWp(999))
    }

    // ── WpEvent.points ────────────────────────────────────────────────────────

    @Test
    fun `WpEvent ATTENDANCE 10점`() {
        assertEquals(10, WpEvent.points(WpEvent.ATTENDANCE))
    }

    @Test
    fun `WpEvent ROUTINE 2점`() {
        assertEquals(2, WpEvent.points(WpEvent.ROUTINE))
    }

    @Test
    fun `WpEvent DIARY 3점`() {
        assertEquals(3, WpEvent.points(WpEvent.DIARY))
    }

    @Test
    fun `WpEvent CHAT_ANALYSIS 5점`() {
        assertEquals(5, WpEvent.points(WpEvent.CHAT_ANALYSIS))
    }

    @Test
    fun `WpEvent STREAK_7 10점`() {
        assertEquals(10, WpEvent.points(WpEvent.STREAK_7))
    }

    @Test
    fun `WpEvent STREAK_30 30점`() {
        assertEquals(30, WpEvent.points(WpEvent.STREAK_30))
    }

    @Test
    fun `WpEvent 알 수 없는 이벤트 0점`() {
        assertEquals(0, WpEvent.points("UNKNOWN_EVENT"))
    }

    // ── CharacterState.progressRatio ─────────────────────────────────────────

    @Test
    fun `progressRatio - Lv1 0 WP 시 0f`() {
        val state = CharacterState(
            type = CharacterType.CAT,
            name = "솔이",
            totalWp = 0,
            level = 1,
            levelName = "알",
            nextLevelWp = 100,
            todayWp = 0
        )
        assertEquals(0f, state.progressRatio, 0.01f)
    }

    @Test
    fun `progressRatio - Lv1 50 WP 시 0_5f`() {
        val state = CharacterState(
            type = CharacterType.CAT,
            name = "솔이",
            totalWp = 50,
            level = 1,
            levelName = "알",
            nextLevelWp = 100,
            todayWp = 5
        )
        assertEquals(0.5f, state.progressRatio, 0.01f)
    }

    @Test
    fun `progressRatio - Lv2 100 WP 시 0f(레벨 시작)`() {
        val state = CharacterState(
            type = CharacterType.DOG,
            name = "뭉치",
            totalWp = 100,
            level = 2,
            levelName = "아기",
            nextLevelWp = 250,
            todayWp = 0
        )
        assertEquals(0f, state.progressRatio, 0.01f)
    }

    // ── DailyChatResult.temperatureLevel ─────────────────────────────────────

    @Test
    fun `temperatureLevel - 70도 이상 WARM`() {
        val result = DailyChatResult(
            id = 1L, chatAnalysisId = 1L, date = "2026-04-14",
            totalMessages = 10, positiveCount = 7, negativeCount = 1, neutralCount = 2,
            temperature = 70f, relationshipScore = 80f
        )
        assertEquals(TemperatureLevel.WARM, result.temperatureLevel)
    }

    @Test
    fun `temperatureLevel - 50~69도 NORMAL`() {
        val result = DailyChatResult(
            id = 1L, chatAnalysisId = 1L, date = "2026-04-14",
            totalMessages = 10, positiveCount = 5, negativeCount = 2, neutralCount = 3,
            temperature = 55f, relationshipScore = 60f
        )
        assertEquals(TemperatureLevel.NORMAL, result.temperatureLevel)
    }

    @Test
    fun `temperatureLevel - 30~49도 COOL`() {
        val result = DailyChatResult(
            id = 1L, chatAnalysisId = 1L, date = "2026-04-14",
            totalMessages = 10, positiveCount = 3, negativeCount = 5, neutralCount = 2,
            temperature = 35f, relationshipScore = 40f
        )
        assertEquals(TemperatureLevel.COOL, result.temperatureLevel)
    }

    @Test
    fun `temperatureLevel - 29도 이하 COLD`() {
        val result = DailyChatResult(
            id = 1L, chatAnalysisId = 1L, date = "2026-04-14",
            totalMessages = 10, positiveCount = 1, negativeCount = 8, neutralCount = 1,
            temperature = 20f, relationshipScore = 20f
        )
        assertEquals(TemperatureLevel.COLD, result.temperatureLevel)
    }
}
