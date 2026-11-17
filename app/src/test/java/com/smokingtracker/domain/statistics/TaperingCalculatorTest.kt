package com.smokingtracker.domain.statistics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class TaperingCalculatorTest {

    @Test
    fun testValidateDailyLimit() {
        assertEquals(1, TaperingCalculator.validateDailyLimit(0))
        assertEquals(1, TaperingCalculator.validateDailyLimit(-5))
        assertEquals(10, TaperingCalculator.validateDailyLimit(10))
        assertEquals(60, TaperingCalculator.validateDailyLimit(60))
        assertEquals(60, TaperingCalculator.validateDailyLimit(100))
    }

    @Test
    fun testValidateIntervalDays() {
        assertEquals(3, TaperingCalculator.validateIntervalDays(1))
        assertEquals(3, TaperingCalculator.validateIntervalDays(3))
        assertEquals(7, TaperingCalculator.validateIntervalDays(7))
        assertEquals(14, TaperingCalculator.validateIntervalDays(14))
        assertEquals(14, TaperingCalculator.validateIntervalDays(30))
    }

    @Test
    fun testCalculateTotalDaysToZero() {
        assertEquals(0, TaperingCalculator.calculateTotalDaysToZero(0, 7))
        assertEquals(70, TaperingCalculator.calculateTotalDaysToZero(10, 7))
        assertEquals(15, TaperingCalculator.calculateTotalDaysToZero(5, 3))
    }

    @Test
    fun testCalculateTargetDate() {
        val baseDate = LocalDate.of(2026, 1, 1)
        val target = TaperingCalculator.calculateTargetDate(70, baseDate)
        assertEquals(LocalDate.of(2026, 3, 12), target)
    }

    @Test
    fun testFormatTargetDate() {
        val target = LocalDate.of(2026, 5, 15)
        val formatted = TaperingCalculator.formatTargetDate(target, Locale.ENGLISH)
        assertEquals("15 May", formatted)
    }

    @Test
    fun testGenerateReductionPoints() {
        val baseDate = LocalDate.of(2026, 1, 1)
        val points = TaperingCalculator.generateReductionPoints(3, 7, baseDate)

        assertEquals(4, points.size)
        assertEquals(TaperingPoint(0, 3, baseDate), points[0])
        assertEquals(TaperingPoint(7, 2, baseDate.plusDays(7)), points[1])
        assertEquals(TaperingPoint(14, 1, baseDate.plusDays(14)), points[2])
        assertEquals(TaperingPoint(21, 0, baseDate.plusDays(21)), points[3])
    }

    @Test
    fun testGenerateScheduleSteps() {
        val baseDate = LocalDate.of(2026, 1, 1)
        val steps = TaperingCalculator.generateScheduleSteps(3, 7, baseDate)

        assertEquals(3, steps.size)
        assertEquals(1, steps[0].stepIndex)
        assertEquals(3, steps[0].dailyLimit)
        assertEquals(0, steps[0].startDayOffset)
        assertEquals(6, steps[0].endDayOffset)

        assertEquals(2, steps[1].stepIndex)
        assertEquals(2, steps[1].dailyLimit)
        assertEquals(7, steps[1].startDayOffset)
        assertEquals(13, steps[1].endDayOffset)

        assertEquals(3, steps[2].stepIndex)
        assertEquals(1, steps[2].dailyLimit)
        assertEquals(14, steps[2].startDayOffset)
        assertEquals(20, steps[2].endDayOffset)
    }

    @Test
    fun testCalculatePlan() {
        val baseDate = LocalDate.of(2026, 1, 1)
        val plan = TaperingCalculator.calculatePlan(5, 7, Locale.ENGLISH, baseDate)

        assertEquals(5, plan.initialLimit)
        assertEquals(7, plan.intervalDays)
        assertEquals(35, plan.totalDaysToZero)
        assertEquals(LocalDate.of(2026, 2, 5), plan.targetDate)
        assertEquals("5 February", plan.formattedTargetDate)
        assertEquals(6, plan.points.size)
        assertEquals(5, plan.steps.size)
    }

    @Test
    fun testIsEligibleForCheckin() {
        val now = 1_000_000_000_000L
        val oneDayMs = 86_400_000L
        val sixDaysAgo = now - 6 * oneDayMs
        val eightDaysAgo = now - 8 * oneDayMs

        assertFalse(TaperingCalculator.isEligibleForCheckin(0L, 7, now))
        assertFalse(TaperingCalculator.isEligibleForCheckin(now + 1000L, 7, now))
        assertFalse(TaperingCalculator.isEligibleForCheckin(sixDaysAgo, 7, now))
        assertTrue(TaperingCalculator.isEligibleForCheckin(eightDaysAgo, 7, now))
    }

    @Test
    fun testHealthRiskDurationAndBaselinePreview() {
        val now = 1_700_000_000_000L
        val oneYearMs = (365.25 * 86_400_000L).toLong()
        val threeYearsAgo = now - (3 * oneYearMs)

        val duration = HealthRiskCalculator.calculateSmokingDuration(threeYearsAgo, now)
        assertTrue(duration.totalDays >= 1095)
        assertEquals(3, duration.yearsInt)

        val preview = SavingsCalculator.calculateBaselinePreview(
            startDate = threeYearsAgo,
            dailyAvg = 20,
            packPrice = 200f,
            packSize = 20,
            nowMillis = now
        )

        assertEquals(3, preview.yearsInt)
        assertTrue(preview.packYears >= 2.9f && preview.packYears <= 3.1f)
        assertEquals(PackYearsRiskLevel.LOW, preview.riskLevel)
        assertTrue(preview.estimatedPastCigarettes >= 21900L)
        assertTrue(preview.estimatedPastCost >= 219000.0)
    }
}
