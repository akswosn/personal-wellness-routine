package com.forlks.personal_wellness_routine.util

import com.forlks.personal_wellness_routine.data.db.entity.DailyHealthScoreEntity

/**
 * 일 건강도 계산기
 *
 * 원본 가중치: 기분(20) + 루틴(25) + 일기(25) + 카카오(20) + 관계(10) = 100pt
 * null 항목은 제외 후 나머지 항목 가중치 합으로 비율 재계산 → 0~100 점수 반환
 *
 * Level 기준:
 *  Lv1  0~39   지친날   😩
 *  Lv2  40~59  보통날   🤔
 *  Lv3  60~74  좋은날   😄
 *  Lv4  75~89  엄청좋은날 😁
 *  Lv5  90~100 완전코럭  ❤️
 */
object DailyHealthCalculator {

    private const val WEIGHT_MOOD     = 20f
    private const val WEIGHT_ROUTINE  = 25f
    private const val WEIGHT_DIARY    = 25f
    private const val WEIGHT_CHAT     = 20f
    private const val WEIGHT_RELATION = 10f

    /**
     * 각 항목의 점수(0~각 최대치)와 최대치 쌍을 받아 totalScore(0~100) 계산
     * null → 해당 항목 제외
     */
    fun calculate(
        moodScore: Float?,      // 0~20
        routineScore: Float?,   // 0~25
        diaryScore: Float?,     // 0~25
        chatTempScore: Float?,  // 0~20
        relationScore: Float?   // 0~10
    ): Float {
        data class Item(val score: Float, val maxWeight: Float)

        val items = listOfNotNull(
            moodScore?.let     { Item(it, WEIGHT_MOOD) },
            routineScore?.let  { Item(it, WEIGHT_ROUTINE) },
            diaryScore?.let    { Item(it, WEIGHT_DIARY) },
            chatTempScore?.let { Item(it, WEIGHT_CHAT) },
            relationScore?.let { Item(it, WEIGHT_RELATION) }
        )

        if (items.isEmpty()) return 0f

        val totalWeight = items.sumOf { it.maxWeight.toDouble() }.toFloat()
        val weightedSum = items.sumOf { (it.score / it.maxWeight * 100.0) * (it.maxWeight / totalWeight) }

        return weightedSum.toFloat().coerceIn(0f, 100f)
    }

    fun levelFrom(totalScore: Float): Int = when {
        totalScore >= 90f -> 5
        totalScore >= 75f -> 4
        totalScore >= 60f -> 3
        totalScore >= 40f -> 2
        else              -> 1
    }

    fun levelLabel(level: Int): String = when (level) {
        5    -> "완전코럭"
        4    -> "엄청좋은날"
        3    -> "좋은날"
        2    -> "보통날"
        else -> "지친날"
    }

    fun levelEmoji(level: Int): String = when (level) {
        5    -> "❤️"
        4    -> "😁"
        3    -> "😄"
        2    -> "🤔"
        else -> "😩"
    }

    /** 오늘기분 이모지 → moodScore(0~20) 변환 */
    fun moodEmojiToScore(emoji: String): Float = when (emoji) {
        "😊" -> 20f
        "🙂" -> 16f
        "😐" -> 10f
        "😔" -> 5f
        "😢" -> 0f
        else  -> 10f  // 기본값
    }

    /** 루틴달성률(0~1) → routineScore(0~25) 변환 */
    fun routineRatioToScore(ratio: Float): Float = (ratio * WEIGHT_ROUTINE).coerceIn(0f, WEIGHT_ROUTINE)

    /** 마음건강도(0~100) → diaryScore(0~25) 변환 */
    fun mindHealthToScore(mindHealth: Float): Float = (mindHealth / 100f * WEIGHT_DIARY).coerceIn(0f, WEIGHT_DIARY)

    /** 카카오온도(0~100) → chatTempScore(0~20) 변환 */
    fun chatTempToScore(chatTemp: Float): Float = (chatTemp / 100f * WEIGHT_CHAT).coerceIn(0f, WEIGHT_CHAT)

    /** 관계건강도(0~100) → relationScore(0~10) 변환 */
    fun relationToScore(relationScore: Float): Float = (relationScore / 100f * WEIGHT_RELATION).coerceIn(0f, WEIGHT_RELATION)

    /** Entity 생성 헬퍼 */
    fun buildEntity(
        date: String,
        moodScore: Float? = null,
        routineScore: Float? = null,
        diaryScore: Float? = null,
        chatTempScore: Float? = null,
        relationScore: Float? = null
    ): DailyHealthScoreEntity {
        val total = calculate(moodScore, routineScore, diaryScore, chatTempScore, relationScore)
        return DailyHealthScoreEntity(
            date          = date,
            moodScore     = moodScore,
            routineScore  = routineScore,
            diaryScore    = diaryScore,
            chatTempScore = chatTempScore,
            relationScore = relationScore,
            totalScore    = total,
            level         = levelFrom(total)
        )
    }
}
