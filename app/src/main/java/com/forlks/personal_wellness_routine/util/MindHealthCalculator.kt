package com.forlks.personal_wellness_routine.util

object MindHealthCalculator {

    /**
     * 마음 건강도 계산
     * = (긍정 일기 수 / 전체 일기 수) × 100
     * 50자 미만 일기는 분모·분자 모두 제외
     * 최소 5개 이상 일기부터 유효
     */
    fun calculate(positiveDiaries: Int, totalDiaries: Int): Int {
        if (totalDiaries < 5) return -1   // 데이터 부족 표시용
        return ((positiveDiaries.toFloat() / totalDiaries.toFloat()) * 100)
            .toInt()
            .coerceIn(0, 100)
    }

    /** 구간 레벨 */
    fun level(score: Int): String = when {
        score < 0  -> "분석중"
        score >= 80 -> "매우 밝음"
        score >= 60 -> "밝음"
        score >= 40 -> "보통"
        score >= 20 -> "흐림"
        else        -> "매우 흐림"
    }

    /** 레벨 이모지 */
    fun levelEmoji(score: Int): String = when {
        score < 0  -> "🔍"
        score >= 80 -> "☀️"
        score >= 60 -> "⛅"
        score >= 40 -> "🌤"
        score >= 20 -> "🌧"
        else        -> "⛈"
    }

    /** 인사이트 문구 */
    fun insight(score: Int): String = when {
        score < 0  -> "일기를 5개 이상 작성하면 마음 건강도를 분석할 수 있어요."
        score >= 80 -> "이번 달 기쁨·감사 감정이 많아요! 루틴을 꾸준히 유지하세요. 🌟"
        score >= 60 -> "긍정적인 날이 많았어요. 조금 더 꾸준히 기록해봐요!"
        score >= 40 -> "보통 수준이에요. 감사한 일을 찾아 일기에 적어보세요."
        score >= 20 -> "힘든 날이 있었군요. 짧은 산책이나 명상 루틴을 추천드려요."
        else        -> "많이 지치셨나요? 전문가 상담도 좋은 방법이에요. 💙"
    }

    /** 전문가 상담 안내 필요 여부 */
    fun shouldShowCounselingBanner(score: Int): Boolean = score in 0..19
}
