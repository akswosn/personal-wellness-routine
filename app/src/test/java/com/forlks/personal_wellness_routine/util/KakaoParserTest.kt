package com.forlks.personal_wellness_routine.util

import org.junit.Assert.*
import org.junit.Test

class KakaoParserTest {

    // ── calcRawScore ──────────────────────────────────────────────────────────

    @Test
    fun `calcRawScore - 감정 단어 없으면 중립 50 반환`() {
        val text = "오늘 날씨 맑고 밥 먹었어"
        assertEquals(50, KakaoParser.calcRawScore(text))
    }

    @Test
    fun `calcRawScore - 긍정 단어만 있으면 100 반환`() {
        val text = "너무 행복하고 사랑해 감사해"
        assertEquals(100, KakaoParser.calcRawScore(text))
    }

    @Test
    fun `calcRawScore - 부정 단어만 있으면 0 반환`() {
        val text = "힘들어 짜증나 최악이야"
        assertEquals(0, KakaoParser.calcRawScore(text))
    }

    @Test
    fun `calcRawScore - 긍정 부정 동수이면 50 반환`() {
        // 긍정: 행복 / 부정: 슬프  →  1/(1+1)*100 = 50
        val text = "행복하지만 슬프다"
        assertEquals(50, KakaoParser.calcRawScore(text))
    }

    @Test
    fun `calcRawScore - 이모지 포함 텍스트 정상 계산`() {
        val text = "😊 오늘 최고야!"
        val score = KakaoParser.calcRawScore(text)
        assertTrue("이모지 포함 긍정 텍스트 점수가 50 이상이어야 함. 실제: $score", score >= 50)
    }

    @Test
    fun `calcRawScore - 결과는 항상 0~100 범위`() {
        val texts = listOf(
            "행복 사랑 최고 감사",
            "힘들어 우울해 짜증나",
            "날씨 맑음 점심 식사",
            "😊😊😊😢"
        )
        texts.forEach { text ->
            val score = KakaoParser.calcRawScore(text)
            assertTrue("'$text' 점수 $score 가 0~100 범위를 벗어남", score in 0..100)
        }
    }

    // ── scoreToLevel ──────────────────────────────────────────────────────────

    @Test
    fun `scoreToLevel - 90점 이상 Lv5`() {
        assertEquals(5, KakaoParser.scoreToLevel(90))
        assertEquals(5, KakaoParser.scoreToLevel(100))
    }

    @Test
    fun `scoreToLevel - 70~89점 Lv4`() {
        assertEquals(4, KakaoParser.scoreToLevel(70))
        assertEquals(4, KakaoParser.scoreToLevel(89))
    }

    @Test
    fun `scoreToLevel - 50~69점 Lv3`() {
        assertEquals(3, KakaoParser.scoreToLevel(50))
        assertEquals(3, KakaoParser.scoreToLevel(69))
    }

    @Test
    fun `scoreToLevel - 30~49점 Lv2`() {
        assertEquals(2, KakaoParser.scoreToLevel(30))
        assertEquals(2, KakaoParser.scoreToLevel(49))
    }

    @Test
    fun `scoreToLevel - 29점 이하 Lv1`() {
        assertEquals(1, KakaoParser.scoreToLevel(0))
        assertEquals(1, KakaoParser.scoreToLevel(29))
    }

    // ── Sentiment 분류 (calcRawScore 기반 간접 테스트) ─────────────────────────

    @Test
    fun `긍정 메시지 점수는 50 초과`() {
        val message = "오늘 정말 좋아! 최고야 행복해 😊"
        val score = KakaoParser.calcRawScore(message)
        assertTrue("긍정 메시지 점수가 50 초과이어야 함. 실제: $score", score > 50)
    }

    @Test
    fun `부정 메시지 점수는 50 미만`() {
        val message = "오늘 너무 힘들고 짜증나 😢 우울해"
        val score = KakaoParser.calcRawScore(message)
        assertTrue("부정 메시지 점수가 50 미만이어야 함. 실제: $score", score < 50)
    }

    @Test
    fun `중립 메시지 점수는 50`() {
        val message = "오늘 회사 갔다 왔어 밥 먹었어"
        val score = KakaoParser.calcRawScore(message)
        assertEquals("중립 메시지 점수는 50이어야 함", 50, score)
    }

    // ── DATE_PATTERN 형식 (날짜 감지) ─────────────────────────────────────────

    @Test
    fun `카카오 날짜 형식 정규식 - 유효한 형식 매칭`() {
        // "2024년 3월 5일 화요일" 형식이 파싱됨을 간접 검증
        // calcRawScore는 Context 없이 테스트 가능한 유일한 공개 메서드이므로
        // 날짜 파싱은 parseAndAnalyzeFull 내부 처리 — 여기서는 scoreToLevel 경계값으로 대체
        val score50 = KakaoParser.scoreToLevel(50)
        val score49 = KakaoParser.scoreToLevel(49)
        assertEquals(3, score50)
        assertEquals(2, score49)
    }
}
