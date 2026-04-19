package com.forlks.personal_wellness_routine.util

import org.junit.Assert.*
import org.junit.Test

class DailyHealthCalculatorTest {

    // ── calculate ─────────────────────────────────────────────────────────────

    @Test
    fun `calculate - 모든 항목 null이면 0 반환`() {
        val result = DailyHealthCalculator.calculate(null, null, null, null, null)
        assertEquals(0f, result, 0f)
    }

    @Test
    fun `calculate - 모든 항목 최대값이면 100 반환`() {
        val result = DailyHealthCalculator.calculate(
            moodScore = 20f,
            routineScore = 25f,
            diaryScore = 25f,
            chatTempScore = 20f,
            relationScore = 10f
        )
        assertEquals(100f, result, 0.01f)
    }

    @Test
    fun `calculate - 모든 항목 0이면 0 반환`() {
        val result = DailyHealthCalculator.calculate(0f, 0f, 0f, 0f, 0f)
        assertEquals(0f, result, 0.01f)
    }

    @Test
    fun `calculate - 기분만 최대값이면 100점 반환(항목 비율 재계산)`() {
        // moodScore만 제공 → 가중치 20/20 = 100%
        val result = DailyHealthCalculator.calculate(
            moodScore = 20f,
            routineScore = null,
            diaryScore = null,
            chatTempScore = null,
            relationScore = null
        )
        assertEquals(100f, result, 0.01f)
    }

    @Test
    fun `calculate - 기분 절반이면 50점 반환`() {
        val result = DailyHealthCalculator.calculate(
            moodScore = 10f,   // 20점 중 10점
            routineScore = null,
            diaryScore = null,
            chatTempScore = null,
            relationScore = null
        )
        assertEquals(50f, result, 0.01f)
    }

    @Test
    fun `calculate - 결과는 항상 0~100 범위`() {
        val result = DailyHealthCalculator.calculate(20f, 25f, 25f, 20f, 10f)
        assertTrue(result in 0f..100f)
    }

    // ── levelFrom ────────────────────────────────────────────────────────────

    @Test
    fun `levelFrom - 90점 이상 Lv5`() {
        assertEquals(5, DailyHealthCalculator.levelFrom(90f))
        assertEquals(5, DailyHealthCalculator.levelFrom(100f))
    }

    @Test
    fun `levelFrom - 75~89점 Lv4`() {
        assertEquals(4, DailyHealthCalculator.levelFrom(75f))
        assertEquals(4, DailyHealthCalculator.levelFrom(89.9f))
    }

    @Test
    fun `levelFrom - 60~74점 Lv3`() {
        assertEquals(3, DailyHealthCalculator.levelFrom(60f))
        assertEquals(3, DailyHealthCalculator.levelFrom(74.9f))
    }

    @Test
    fun `levelFrom - 40~59점 Lv2`() {
        assertEquals(2, DailyHealthCalculator.levelFrom(40f))
        assertEquals(2, DailyHealthCalculator.levelFrom(59.9f))
    }

    @Test
    fun `levelFrom - 39점 이하 Lv1`() {
        assertEquals(1, DailyHealthCalculator.levelFrom(0f))
        assertEquals(1, DailyHealthCalculator.levelFrom(39.9f))
    }

    // ── levelLabel / levelEmoji ───────────────────────────────────────────────

    @Test
    fun `levelLabel - 5개 레벨 모두 비어있지 않음`() {
        (1..5).forEach { level ->
            assertTrue("Lv$level 레이블이 비어있음",
                DailyHealthCalculator.levelLabel(level).isNotBlank())
        }
    }

    @Test
    fun `levelEmoji - 5개 레벨 모두 이모지 포함`() {
        (1..5).forEach { level ->
            assertTrue("Lv$level 이모지가 비어있음",
                DailyHealthCalculator.levelEmoji(level).isNotBlank())
        }
    }

    // ── 변환 함수들 ──────────────────────────────────────────────────────────

    @Test
    fun `moodEmojiToScore - 😊 는 최대 20점`() {
        assertEquals(20f, DailyHealthCalculator.moodEmojiToScore("😊"), 0f)
    }

    @Test
    fun `moodEmojiToScore - 🙂 는 16점`() {
        assertEquals(16f, DailyHealthCalculator.moodEmojiToScore("🙂"), 0f)
    }

    @Test
    fun `moodEmojiToScore - 😐 는 12점`() {
        assertEquals(12f, DailyHealthCalculator.moodEmojiToScore("😐"), 0f)
    }

    @Test
    fun `moodEmojiToScore - 😔 는 8점`() {
        assertEquals(8f, DailyHealthCalculator.moodEmojiToScore("😔"), 0f)
    }

    @Test
    fun `moodEmojiToScore - 😢 는 최소 4점`() {
        assertEquals(4f, DailyHealthCalculator.moodEmojiToScore("😢"), 0f)
    }

    @Test
    fun `moodEmojiToScore - 알 수 없는 이모지는 기본값 12점`() {
        assertEquals(12f, DailyHealthCalculator.moodEmojiToScore("❓"), 0f)
    }

    @Test
    fun `routineRatioToScore - 비율 최대(1)이면 25점`() {
        assertEquals(25f, DailyHealthCalculator.routineRatioToScore(1.0f), 0f)
    }

    @Test
    fun `routineRatioToScore - 비율 절반(0_5)이면 12점5`() {
        assertEquals(12.5f, DailyHealthCalculator.routineRatioToScore(0.5f), 0.01f)
    }

    @Test
    fun `routineRatioToScore - 비율 0이면 0점`() {
        assertEquals(0f, DailyHealthCalculator.routineRatioToScore(0f), 0f)
    }

    @Test
    fun `chatTempToScore - 100도이면 최대 20점`() {
        assertEquals(20f, DailyHealthCalculator.chatTempToScore(100f), 0f)
    }

    @Test
    fun `relationToScore - 100점이면 최대 10점`() {
        assertEquals(10f, DailyHealthCalculator.relationToScore(100f), 0f)
    }

    // ── buildEntity ───────────────────────────────────────────────────────────

    @Test
    fun `buildEntity - totalScore와 level 자동 계산`() {
        val entity = DailyHealthCalculator.buildEntity(
            date = "2026-04-14",
            moodScore = 20f,
            routineScore = 25f
        )
        assertEquals("2026-04-14", entity.date)
        assertTrue("totalScore가 0보다 커야 함", entity.totalScore > 0f)
        assertTrue("level이 1~5 범위여야 함", entity.level in 1..5)
    }
}
