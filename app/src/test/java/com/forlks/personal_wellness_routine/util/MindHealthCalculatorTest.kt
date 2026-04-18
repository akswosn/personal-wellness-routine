package com.forlks.personal_wellness_routine.util

import org.junit.Assert.*
import org.junit.Test

class MindHealthCalculatorTest {

    // ── calculateFromText ─────────────────────────────────────────────────────

    @Test
    fun `calculateFromText - 단어 10개 미만은 -1(데이터부족) 반환`() {
        val shortText = "오늘 기분이 좋다"   // 단어 4개 미만
        assertEquals(-1, MindHealthCalculator.calculateFromText(shortText))
    }

    @Test
    fun `calculateFromText - 긍정 단어만 있으면 100 반환`() {
        // 단어 수 >= 10 이고 긍정 단어 다수 포함
        val positiveText = "오늘 정말 행복하고 즐거운 하루였어 기뻐서 웃음이 나왔고 최고의 날이었어 감사해 사랑해"
        val score = MindHealthCalculator.calculateFromText(positiveText)
        assertTrue("긍정 텍스트 점수가 50 이상이어야 함. 실제: $score", score >= 50)
    }

    @Test
    fun `calculateFromText - 부정 단어만 있으면 낮은 점수 반환`() {
        val negativeText = "오늘 너무 힘들고 슬프고 우울한 하루였어 짜증나고 피곤하고 스트레스 받아서 최악이야"
        val score = MindHealthCalculator.calculateFromText(negativeText)
        assertTrue("부정 텍스트 점수가 50 미만이어야 함. 실제: $score", score < 50)
    }

    @Test
    fun `calculateFromText - 감정 단어 없으면 중립 50 반환`() {
        val neutralText = "오늘 날씨는 맑았고 점심은 밥을 먹었고 저녁에는 책을 읽었어 그냥 평범한 하루였어"
        val score = MindHealthCalculator.calculateFromText(neutralText)
        assertEquals("감정 단어 없을 때 중립 50점", 50, score)
    }

    @Test
    fun `calculateFromText - 결과는 0~100 범위 내`() {
        val text = "오늘 정말 행복하고 즐거운 하루였어 기뻐서 웃음이 나왔고 최고의 날이었어"
        val score = MindHealthCalculator.calculateFromText(text)
        assertTrue(score in 0..100)
    }

    // ── calculateFromTexts ────────────────────────────────────────────────────

    @Test
    fun `calculateFromTexts - 빈 리스트 -1 반환`() {
        assertEquals(-1, MindHealthCalculator.calculateFromTexts(emptyList()))
    }

    @Test
    fun `calculateFromTexts - 50자 미만 일기는 필터링`() {
        val shortDiaries = listOf("짧은 일기", "또 짧아")
        assertEquals(-1, MindHealthCalculator.calculateFromTexts(shortDiaries))
    }

    @Test
    fun `calculateFromTexts - 50자 이상 일기 합산 분석`() {
        val diaries = listOf(
            "오늘 정말 행복하고 즐거운 하루였어. 아침부터 기분이 좋아서 감사한 마음이 들었고 즐거운 시간을 보냈다.",
            "오늘도 힘들고 지쳐서 우울한 하루였어. 짜증나는 일이 많아서 스트레스를 많이 받았고 피곤한 하루였다."
        )
        val score = MindHealthCalculator.calculateFromTexts(diaries)
        assertTrue("여러 일기 합산 점수가 0~100 범위여야 함. 실제: $score", score in 0..100)
    }

    // ── level / levelEmoji ────────────────────────────────────────────────────

    @Test
    fun `level - 80점 이상 매우 밝음`() {
        assertEquals("매우 밝음", MindHealthCalculator.level(80))
        assertEquals("매우 밝음", MindHealthCalculator.level(100))
    }

    @Test
    fun `level - 60~79점 밝음`() {
        assertEquals("밝음", MindHealthCalculator.level(60))
        assertEquals("밝음", MindHealthCalculator.level(79))
    }

    @Test
    fun `level - 40~59점 보통`() {
        assertEquals("보통", MindHealthCalculator.level(40))
        assertEquals("보통", MindHealthCalculator.level(59))
    }

    @Test
    fun `level - 20~39점 흐림`() {
        assertEquals("흐림", MindHealthCalculator.level(20))
        assertEquals("흐림", MindHealthCalculator.level(39))
    }

    @Test
    fun `level - 0~19점 매우 흐림`() {
        assertEquals("매우 흐림", MindHealthCalculator.level(0))
        assertEquals("매우 흐림", MindHealthCalculator.level(19))
    }

    @Test
    fun `level - -1점(데이터부족) 분석중`() {
        assertEquals("분석중", MindHealthCalculator.level(-1))
    }

    @Test
    fun `levelEmoji - 각 구간 이모지 매핑`() {
        assertEquals("☀️", MindHealthCalculator.levelEmoji(80))
        assertEquals("⛅", MindHealthCalculator.levelEmoji(60))
        assertEquals("🌤", MindHealthCalculator.levelEmoji(40))
        assertEquals("🌧", MindHealthCalculator.levelEmoji(20))
        assertEquals("⛈", MindHealthCalculator.levelEmoji(0))
        assertEquals("🔍", MindHealthCalculator.levelEmoji(-1))
    }

    // ── scoreToLevel / levelToSubScore ───────────────────────────────────────

    @Test
    fun `scoreToLevel - 90점 이상 Lv5`() {
        assertEquals(5, MindHealthCalculator.scoreToLevel(90))
        assertEquals(5, MindHealthCalculator.scoreToLevel(100))
    }

    @Test
    fun `scoreToLevel - 70~89점 Lv4`() {
        assertEquals(4, MindHealthCalculator.scoreToLevel(70))
        assertEquals(4, MindHealthCalculator.scoreToLevel(89))
    }

    @Test
    fun `scoreToLevel - 50~69점 Lv3`() {
        assertEquals(3, MindHealthCalculator.scoreToLevel(50))
        assertEquals(3, MindHealthCalculator.scoreToLevel(69))
    }

    @Test
    fun `scoreToLevel - 30~49점 Lv2`() {
        assertEquals(2, MindHealthCalculator.scoreToLevel(30))
        assertEquals(2, MindHealthCalculator.scoreToLevel(49))
    }

    @Test
    fun `scoreToLevel - 29점 이하 Lv1`() {
        assertEquals(1, MindHealthCalculator.scoreToLevel(0))
        assertEquals(1, MindHealthCalculator.scoreToLevel(29))
    }

    @Test
    fun `levelToSubScore - Lv5는 25점`() {
        assertEquals(25f, MindHealthCalculator.levelToSubScore(5), 0f)
    }

    @Test
    fun `levelToSubScore - Lv1은 5점`() {
        assertEquals(5f, MindHealthCalculator.levelToSubScore(1), 0f)
    }

    // ── shouldShowCounselingBanner ─────────────────────────────────────────────

    @Test
    fun `shouldShowCounselingBanner - 0~19점 배너 표시`() {
        assertTrue(MindHealthCalculator.shouldShowCounselingBanner(0))
        assertTrue(MindHealthCalculator.shouldShowCounselingBanner(15))
        assertTrue(MindHealthCalculator.shouldShowCounselingBanner(19))
    }

    @Test
    fun `shouldShowCounselingBanner - 20점 이상 배너 미표시`() {
        assertFalse(MindHealthCalculator.shouldShowCounselingBanner(20))
        assertFalse(MindHealthCalculator.shouldShowCounselingBanner(50))
    }

    @Test
    fun `shouldShowCounselingBanner - -1(데이터없음) 배너 미표시`() {
        assertFalse(MindHealthCalculator.shouldShowCounselingBanner(-1))
    }
}
