package com.forlks.personal_wellness_routine.util

/**
 * 마음 건강도 계산기 (v0.0.2)
 *
 * 공식: (긍정 단어 수 / 전체 단어 수) × 100
 * - 긍정 단어 / 부정 단어 사전 기반
 * - 전체 단어 수 < 10 이면 데이터 부족 (-1 반환)
 * - 50자 미만 일기는 분석 대상 제외 (호출부에서 필터링)
 */
object MindHealthCalculator {

    val POSITIVE_WORDS = setOf(
        "좋아", "행복", "기쁘", "사랑", "감사", "고마워", "설레", "신나", "재미",
        "즐거", "최고", "멋있", "대단", "훌륭", "완벽", "보람", "뿌듯", "희망",
        "긍정", "웃음", "밝", "활기", "성공", "달성", "건강", "평화", "여유",
        "기대", "신기", "놀라", "감동", "소중", "행운", "축복", "자랑"
    )

    val NEGATIVE_WORDS = setOf(
        "싫어", "힘들", "슬프", "우울", "화나", "짜증", "최악", "실망", "미워",
        "지쳐", "아파", "걱정", "불안", "무서", "후회", "억울", "외로", "피곤",
        "스트레스", "괴롭", "두렵", "실패", "좌절", "포기", "답답", "막막",
        "외롭", "힘내", "그립", "눈물", "절망", "허무", "허전"
    )

    /**
     * 텍스트에서 단어 기반 마음 건강도 계산
     * @return 0~100 점수, -1 = 데이터 부족
     */
    fun calculateFromText(text: String): Int {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size < 10) return -1

        var posCount = 0
        var negCount = 0

        POSITIVE_WORDS.forEach { keyword ->
            if (text.contains(keyword)) posCount++
        }
        NEGATIVE_WORDS.forEach { keyword ->
            if (text.contains(keyword)) negCount++
        }

        val totalSentimentWords = posCount + negCount
        if (totalSentimentWords == 0) {
            // 감정 단어 없으면 중립 (50점)
            return 50
        }

        return ((posCount.toFloat() / totalSentimentWords.toFloat()) * 100)
            .toInt()
            .coerceIn(0, 100)
    }

    /**
     * 여러 일기 텍스트를 합산해 마음 건강도 계산
     * (긍정 단어 누적 수 / 전체 감정단어 누적 수) × 100
     */
    fun calculateFromTexts(texts: List<String>): Int {
        val combined = texts.filter { it.length >= 50 }.joinToString(" ")
        if (combined.isBlank()) return -1

        val words = combined.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size < 10) return -1

        var posCount = 0
        var negCount = 0
        POSITIVE_WORDS.forEach { if (combined.contains(it)) posCount++ }
        NEGATIVE_WORDS.forEach { if (combined.contains(it)) negCount++ }

        val total = posCount + negCount
        if (total == 0) return 50
        return ((posCount.toFloat() / total.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    // ── Legacy compat: diary-count-based (deprecated, kept for MindHealthViewModel) ──
    @Deprecated("Use calculateFromTexts() instead")
    fun calculate(positiveDiaries: Int, totalDiaries: Int): Int {
        if (totalDiaries < 5) return -1
        return ((positiveDiaries.toFloat() / totalDiaries.toFloat()) * 100)
            .toInt()
            .coerceIn(0, 100)
    }

    fun level(score: Int): String = when {
        score < 0   -> "분석중"
        score >= 80 -> "매우 밝음"
        score >= 60 -> "밝음"
        score >= 40 -> "보통"
        score >= 20 -> "흐림"
        else        -> "매우 흐림"
    }

    fun levelEmoji(score: Int): String = when {
        score < 0   -> "🔍"
        score >= 80 -> "☀️"
        score >= 60 -> "⛅"
        score >= 40 -> "🌤"
        score >= 20 -> "🌧"
        else        -> "⛈"
    }

    fun insight(score: Int): String = when {
        score < 0   -> "일기를 조금 더 작성하면 마음 건강도를 분석할 수 있어요."
        score >= 80 -> "이번 달 기쁨·감사 표현이 많아요! 루틴을 꾸준히 유지하세요. 🌟"
        score >= 60 -> "긍정적인 날이 많았어요. 조금 더 꾸준히 기록해봐요!"
        score >= 40 -> "보통 수준이에요. 감사한 일을 찾아 일기에 적어보세요."
        score >= 20 -> "힘든 날이 있었군요. 짧은 산책이나 명상 루틴을 추천드려요."
        else        -> "많이 지치셨나요? 전문가 상담도 좋은 방법이에요. 💙"
    }

    fun shouldShowCounselingBanner(score: Int): Boolean = score in 0..19

    /**
     * 원점수(0~100) → 1~5 레벨 변환 (v0.0.2)
     *  90+ → 5, 70+ → 4, 50+ → 3, 30+ → 2, 미만 → 1
     */
    fun scoreToLevel(rawScore: Int): Int = when {
        rawScore >= 90 -> 5
        rawScore >= 70 -> 4
        rawScore >= 50 -> 3
        rawScore >= 30 -> 2
        else           -> 1
    }

    /**
     * 레벨(1~5) → diarySubScore(0~25) 변환
     *  Lv5→25, Lv4→20, Lv3→15, Lv2→10, Lv1→5
     */
    fun levelToSubScore(level: Int): Float = (level * 5).toFloat().coerceIn(0f, 25f)
}
