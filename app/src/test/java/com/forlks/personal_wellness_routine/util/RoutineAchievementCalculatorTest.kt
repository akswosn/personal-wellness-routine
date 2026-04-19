package com.forlks.personal_wellness_routine.util

import org.junit.Assert.*
import org.junit.Test

class RoutineAchievementCalculatorTest {

    // ── dailyScore ────────────────────────────────────────────────────────────

    @Test
    fun `dailyScore - 루틴 없을 때 0 반환`() {
        assertEquals(0, RoutineAchievementCalculator.dailyScore(0, 0))
    }

    @Test
    fun `dailyScore - 전부 완료 시 100 반환`() {
        assertEquals(100, RoutineAchievementCalculator.dailyScore(5, 5))
    }

    @Test
    fun `dailyScore - 절반 완료 시 50 반환`() {
        assertEquals(50, RoutineAchievementCalculator.dailyScore(2, 4))
    }

    @Test
    fun `dailyScore - 4개 중 3개 완료 시 75 반환`() {
        assertEquals(75, RoutineAchievementCalculator.dailyScore(3, 4))
    }

    @Test
    fun `dailyScore - 완료 0개일 때 0 반환`() {
        assertEquals(0, RoutineAchievementCalculator.dailyScore(0, 5))
    }

    // ── weeklyScore / monthlyScore ─────────────────────────────────────────────

    @Test
    fun `weeklyScore - 빈 리스트는 0 반환`() {
        assertEquals(0, RoutineAchievementCalculator.weeklyScore(emptyList()))
    }

    @Test
    fun `weeklyScore - 7일 점수 평균 계산`() {
        val scores = listOf(80, 60, 70, 90, 50, 40, 100)
        val expected = scores.average().toInt()   // 70
        assertEquals(expected, RoutineAchievementCalculator.weeklyScore(scores))
    }

    @Test
    fun `monthlyScore - weeklyScore와 동일한 로직`() {
        val scores = listOf(100, 80, 60, 40)
        assertEquals(
            RoutineAchievementCalculator.weeklyScore(scores),
            RoutineAchievementCalculator.monthlyScore(scores)
        )
    }

    // ── gradeFrom ────────────────────────────────────────────────────────────

    @Test
    fun `gradeFrom - 90점 이상 S 등급`() {
        assertEquals("S", RoutineAchievementCalculator.gradeFrom(90))
        assertEquals("S", RoutineAchievementCalculator.gradeFrom(100))
    }

    @Test
    fun `gradeFrom - 75~89점 A 등급`() {
        assertEquals("A", RoutineAchievementCalculator.gradeFrom(75))
        assertEquals("A", RoutineAchievementCalculator.gradeFrom(89))
    }

    @Test
    fun `gradeFrom - 60~74점 B 등급`() {
        assertEquals("B", RoutineAchievementCalculator.gradeFrom(60))
        assertEquals("B", RoutineAchievementCalculator.gradeFrom(74))
    }

    @Test
    fun `gradeFrom - 40~59점 C 등급`() {
        assertEquals("C", RoutineAchievementCalculator.gradeFrom(40))
        assertEquals("C", RoutineAchievementCalculator.gradeFrom(59))
    }

    @Test
    fun `gradeFrom - 39점 이하 D 등급`() {
        assertEquals("D", RoutineAchievementCalculator.gradeFrom(39))
        assertEquals("D", RoutineAchievementCalculator.gradeFrom(0))
    }

    // ── WP 보너스 ─────────────────────────────────────────────────────────────

    @Test
    fun `weeklyWpBonus - S등급 15WP`() {
        assertEquals(15, RoutineAchievementCalculator.weeklyWpBonus("S"))
    }

    @Test
    fun `weeklyWpBonus - A등급 10WP`() {
        assertEquals(10, RoutineAchievementCalculator.weeklyWpBonus("A"))
    }

    @Test
    fun `weeklyWpBonus - B등급 이하 0WP`() {
        assertEquals(0, RoutineAchievementCalculator.weeklyWpBonus("B"))
        assertEquals(0, RoutineAchievementCalculator.weeklyWpBonus("D"))
    }

    @Test
    fun `monthlyWpBonus - S등급 50WP`() {
        assertEquals(50, RoutineAchievementCalculator.monthlyWpBonus("S"))
    }

    @Test
    fun `monthlyWpBonus - S 외 0WP`() {
        assertEquals(0, RoutineAchievementCalculator.monthlyWpBonus("A"))
    }

    // ── barColorLevel ─────────────────────────────────────────────────────────

    @Test
    fun `barColorLevel - 75점 이상 2(녹색)`() {
        assertEquals(2, RoutineAchievementCalculator.barColorLevel(75))
        assertEquals(2, RoutineAchievementCalculator.barColorLevel(100))
    }

    @Test
    fun `barColorLevel - 60~74점 1(연녹색)`() {
        assertEquals(1, RoutineAchievementCalculator.barColorLevel(60))
        assertEquals(1, RoutineAchievementCalculator.barColorLevel(74))
    }

    @Test
    fun `barColorLevel - 59점 이하 0(빨강)`() {
        assertEquals(0, RoutineAchievementCalculator.barColorLevel(59))
        assertEquals(0, RoutineAchievementCalculator.barColorLevel(0))
    }

    // ── gradeDesc / gradeEmoji ────────────────────────────────────────────────

    @Test
    fun `gradeDesc - 모든 등급 빈 문자열 아님`() {
        listOf("S", "A", "B", "C", "D").forEach { grade ->
            assertTrue("등급 $grade 설명이 비어있음",
                RoutineAchievementCalculator.gradeDesc(grade).isNotBlank())
        }
    }

    @Test
    fun `gradeEmoji - 모든 등급 이모지 포함`() {
        listOf("S", "A", "B", "C", "D").forEach { grade ->
            assertTrue("등급 $grade 이모지가 비어있음",
                RoutineAchievementCalculator.gradeEmoji(grade).isNotBlank())
        }
    }
}
