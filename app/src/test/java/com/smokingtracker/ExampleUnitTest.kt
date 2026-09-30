package com.smokingtracker

import org.junit.Test
import org.junit.Assert.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.WavyProgressIndicatorDefaults

class ExampleUnitTest {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Test
    fun addition_isCorrect() {
        println("LinearContainerHeight: ${WavyProgressIndicatorDefaults.LinearContainerHeight}")
        println("LinearDeterminateWavelength: ${WavyProgressIndicatorDefaults.LinearDeterminateWavelength}")
    }

    @Test
    fun testEstimateHistoricalUnlockTimestamp_login() {
        val manager = AchievementsManager()
        val day = 86400000L
        val baseTime = 1700000000000L
        val launches = listOf(
            baseTime,
            baseTime + day,
            baseTime + 2 * day
        )
        val login1Time = manager.estimateHistoricalUnlockTimestamp("login_1", emptyList(), launches)
        assertEquals(baseTime, login1Time)

        val login3Time = manager.estimateHistoricalUnlockTimestamp("login_3", emptyList(), launches)
        assertNotNull(login3Time)
        assertTrue(login3Time!! >= baseTime + 2 * day)
    }

    @Test
    fun testEstimateHistoricalUnlockTimestamp_noSmoke() {
        val manager = AchievementsManager()
        val day = 86400000L
        val t0 = 1700000000000L
        val t1 = t0 + 2 * day
        val entries = listOf(t0, t1)

        val noSmoke1d = manager.estimateHistoricalUnlockTimestamp("nosmoke_1d", entries, emptyList())
        assertEquals(t0 + day, noSmoke1d)
    }

    @Test
    fun testEstimateHistoricalUnlockTimestamp_doubleDamage() {
        val manager = AchievementsManager()
        val t0 = 1700000000000L
        val t1 = t0 + 2 * 60 * 1000L
        val entries = listOf(t0, t1)

        val doubleDamage = manager.estimateHistoricalUnlockTimestamp("secret_double_damage", entries, emptyList())
        assertEquals(t1, doubleDamage)
    }

    @Test
    fun testCalculateWeeklyComparison_pastWeekDoesNotIncludeFutureEntries() {
        val manager = StatisticsManager()
        val cal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 16, 12, 0, 0)
        }
        val prevWeekCal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 9, 12, 0, 0)
        }
        val futureWeekCal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 25, 12, 0, 0)
        }

        val entries = listOf(
            prevWeekCal.timeInMillis,
            prevWeekCal.timeInMillis + 1000L,
            cal.timeInMillis,
            futureWeekCal.timeInMillis,
            futureWeekCal.timeInMillis + 1000L,
            futureWeekCal.timeInMillis + 2000L
        )

        val result = manager.calculateWeeklyComparison(entries, cal)
        assertEquals(1, result.thisWeekCount)
        assertEquals(2, result.lastWeekCount)
        assertEquals(-1, result.difference)
        assertEquals(50, result.percentChange)
        assertEquals(StatisticsManager.ComparisonTrend.DECREASED, result.trend)
    }

    @Test
    fun testCalculateWeeklyComparison_currentWeekPaceCalculation() {
        val manager = StatisticsManager()
        val nowWed = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 16, 15, 0, 0)
        }
        val monThisWeek = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 14, 10, 0, 0)
        }
        val tueThisWeek = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 15, 10, 0, 0)
        }
        val wedThisWeek = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 16, 10, 0, 0)
        }

        val prevMon = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 7, 10, 0, 0)
        }

        val prevEntries = (0..6).flatMap { dayOffset ->
            listOf(
                prevMon.timeInMillis + dayOffset * 86400000L,
                prevMon.timeInMillis + dayOffset * 86400000L + 1000L
            )
        }

        val entriesEqualPace = prevEntries + listOf(
            monThisWeek.timeInMillis, monThisWeek.timeInMillis + 1000L,
            tueThisWeek.timeInMillis, tueThisWeek.timeInMillis + 1000L,
            wedThisWeek.timeInMillis, wedThisWeek.timeInMillis + 1000L
        )

        val resultEqual = manager.calculateWeeklyComparison(entriesEqualPace, nowWed, nowWed)
        assertEquals(6, resultEqual.thisWeekCount)
        assertEquals(14, resultEqual.lastWeekCount)
        assertEquals(3, resultEqual.daysElapsedThisWeek)
        assertTrue(resultEqual.isCurrentWeek)
        assertEquals(2.0f, resultEqual.thisWeekDailyAvg, 0.01f)
        assertEquals(2.0f, resultEqual.lastWeekDailyAvg, 0.01f)
        assertEquals(0, resultEqual.percentChange)
        assertEquals(StatisticsManager.ComparisonTrend.NO_CHANGE, resultEqual.trend)

        val entriesHigherPace = prevEntries + listOf(
            monThisWeek.timeInMillis, monThisWeek.timeInMillis + 1000L, monThisWeek.timeInMillis + 2000L,
            tueThisWeek.timeInMillis, tueThisWeek.timeInMillis + 1000L, tueThisWeek.timeInMillis + 2000L,
            wedThisWeek.timeInMillis, wedThisWeek.timeInMillis + 1000L, wedThisWeek.timeInMillis + 2000L
        )

        val resultHigher = manager.calculateWeeklyComparison(entriesHigherPace, nowWed, nowWed)
        assertEquals(9, resultHigher.thisWeekCount)
        assertEquals(3.0f, resultHigher.thisWeekDailyAvg, 0.01f)
        assertEquals(50, resultHigher.percentChange)
        assertEquals(StatisticsManager.ComparisonTrend.INCREASED, resultHigher.trend)
    }
}
