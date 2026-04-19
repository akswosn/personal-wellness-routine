package com.forlks.personal_wellness_routine.util

object RoutineAchievementCalculator {

    /** 일간 달성도 (0~100) */
    fun dailyScore(completed: Int, total: Int): Int {
        if (total == 0) return 0
        return ((completed.toFloat() / total.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    /** 주간 달성도: 최근 7일 일간 점수 평균 */
    fun weeklyScore(dailyScores: List<Int>): Int {
        if (dailyScores.isEmpty()) return 0
        return dailyScores.average().toInt()
    }

    /** 월간 달성도: 최근 30일 일간 점수 평균 */
    fun monthlyScore(dailyScores: List<Int>): Int = weeklyScore(dailyScores)

    /** 등급 반환: S/A/B/C/D */
    fun gradeFrom(score: Int): String = when {
        score >= 90 -> "S"
        score >= 75 -> "A"
        score >= 60 -> "B"
        score >= 40 -> "C"
        else        -> "D"
    }

    /** 등급 이모지 */
    fun gradeEmoji(grade: String): String = when (grade) {
        "S"  -> "🏆"
        "A"  -> "⭐"
        "B"  -> "✅"
        "C"  -> "📈"
        else -> "💪"
    }

    /** 등급 설명 */
    fun gradeDesc(grade: String): String = when (grade) {
        "S"  -> "완벽한 달성!"
        "A"  -> "우수한 달성"
        "B"  -> "양호한 달성"
        "C"  -> "조금 더 노력해요"
        else -> "다시 도전해봐요"
    }

    /** 주간 WP 보너스 */
    fun weeklyWpBonus(grade: String): Int = when (grade) {
        "S"  -> 15
        "A"  -> 10
        else -> 0
    }

    /** 월간 WP 보너스 */
    fun monthlyWpBonus(grade: String): Int = when (grade) {
        "S"  -> 50
        else -> 0
    }

    /** 바 차트 색상 결정 (0~100 score) */
    fun barColorLevel(score: Int): Int = when {
        score >= 75 -> 2   // 녹색
        score >= 60 -> 1   // 연녹색
        else        -> 0   // 빨강
    }
}
