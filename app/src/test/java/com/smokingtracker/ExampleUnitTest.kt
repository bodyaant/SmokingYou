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

    @Test
    fun testDateUtils_conversionsAndGrouping() {
        val zone = java.time.ZoneId.systemDefault()
        val localDate = java.time.LocalDate.of(2026, 9, 15)
        val millis = localDate.atStartOfDay(zone).toInstant().toEpochMilli()

        assertEquals(localDate, toLocalDate(millis, zone))
        assertEquals(millis, startOfDayMillis(localDate, zone))
        assertEquals(millis, startOfDayMillis(millis + 3600000L, zone))
        assertEquals(1, hourOfDay(millis + 3600000L, zone))

        val nextDay = localDate.plusDays(3)
        assertEquals(3L, daysBetween(localDate, nextDay))
        assertEquals(3L, daysBetween(millis, nextDay.atStartOfDay(zone).toInstant().toEpochMilli()))

        val entries = listOf(
            millis + 1000L,
            millis + 2000L,
            nextDay.atStartOfDay(zone).toInstant().toEpochMilli() + 500L
        )
        val counts = groupCountByDay(entries, zone)
        assertEquals(2, counts.size)
        assertEquals(2, counts[localDate])
        assertEquals(1, counts[nextDay])
    }

    @Test
    fun testStatisticsManager_calculateStats_empty() {
        val manager = StatisticsManager()
        val stats = manager.calculateStats(emptyList())
        assertEquals(0, stats.totalCount)
        assertEquals(0, stats.maxPerDay)
        assertEquals(0, stats.minPerDay)
        assertEquals(0f, stats.avgPerDay, 0.001f)
        assertNull(stats.trackingSince)
        assertEquals(0, stats.longestStreakDays)
        assertEquals(0, stats.totalTrackingDays)
    }

    @Test
    fun testStatisticsManager_calculateStats_singleDay() {
        val manager = StatisticsManager()
        val now = System.currentTimeMillis()
        val stats = manager.calculateStats(listOf(now, now + 1000L, now + 2000L))
        assertEquals(3, stats.totalCount)
        assertEquals(3, stats.maxPerDay)
        assertEquals(3, stats.minPerDay)
        assertEquals(3.0f, stats.avgPerDay, 0.001f)
        assertEquals(1, stats.totalTrackingDays)
        assertEquals(0, stats.longestStreakDays)
    }

    @Test
    fun testStatisticsManager_calculateStats_multipleDaysWithGap() {
        val manager = StatisticsManager()
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        val day0 = today.minusDays(5)
        val day1 = today.minusDays(4)
        val day4 = today.minusDays(1)

        val t0 = day0.atStartOfDay(zone).toInstant().toEpochMilli() + 3600000L
        val t1 = day1.atStartOfDay(zone).toInstant().toEpochMilli() + 3600000L
        val t4 = day4.atStartOfDay(zone).toInstant().toEpochMilli() + 3600000L

        val stats = manager.calculateStats(listOf(t0, t0 + 1000L, t1, t4))
        assertEquals(4, stats.totalCount)
        assertEquals(2, stats.maxPerDay)
        assertEquals(0, stats.minPerDay)
        assertEquals(6, stats.totalTrackingDays)
        assertEquals(4f / 6f, stats.avgPerDay, 0.001f)
        assertEquals(2, stats.longestStreakDays)
    }

    @Test
    fun testStatisticsManager_currentSmokeFreeStreakDays() {
        val manager = StatisticsManager()
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        val day2Ago = today.minusDays(2)
        val ts = day2Ago.atStartOfDay(zone).toInstant().toEpochMilli() + 7200000L

        assertEquals(2, manager.currentSmokeFreeStreakDays(listOf(ts), zone))
        assertEquals(0, manager.currentSmokeFreeStreakDays(emptyList(), zone))
    }

    @Test
    fun testStatisticsManager_generateDailyData() {
        val manager = StatisticsManager()
        val cal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 16, 12, 0, 0)
        }
        val zone = cal.timeZone.toZoneId()
        val baseDate = java.time.LocalDate.of(2026, 9, 16)
        val h1 = baseDate.atTime(1, 15).atZone(zone).toInstant().toEpochMilli()
        val h1_2 = baseDate.atTime(1, 45).atZone(zone).toInstant().toEpochMilli()
        val h10 = baseDate.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val otherDay = baseDate.plusDays(1).atTime(1, 0).atZone(zone).toInstant().toEpochMilli()

        val daily = manager.generateDailyData(listOf(h1, h1_2, h10, otherDay), cal)
        assertEquals(24, daily.size)
        assertEquals(2, daily[1])
        assertEquals(1, daily[10])
        assertEquals(0, daily[0])
    }

    @Test
    fun testCalculateUnlockedAchievements_historicalInterval() {
        val manager = AchievementsManager()
        val day = 86400000L
        val now = System.currentTimeMillis()
        val t0 = now - 8 * day
        val t1 = t0 + 4 * day
        val t2 = now - 1000L
        val entries = listOf(t0, t1, t2)

        val unlocked = manager.calculateUnlockedAchievements(entries, listOf(t0))
        assertTrue(unlocked.contains("nosmoke_1d"))
        assertTrue(unlocked.contains("nosmoke_3d"))
        assertFalse(unlocked.contains("nosmoke_1w"))
    }

    @Test
    fun testGetAchievementProgress_resetsOnNewSmokingEntry() {
        val manager = AchievementsManager()
        val now = System.currentTimeMillis()
        val entries = listOf(now - 1000L)

        val progress1m = manager.getAchievementProgress("nosmoke_1m", entries, emptyList())
        assertEquals(0f, progress1m.fraction, 0.001f)
        assertEquals("0", progress1m.currentDisplay)
        assertEquals("30", progress1m.targetDisplay)
        assertEquals("30", progress1m.remainingDisplay)
    }

    @Test
    fun testCalculateUnlockedAchievements_emptyEntriesUsesLaunches() {
        val manager = AchievementsManager()
        val day = 86400000L
        val now = System.currentTimeMillis()
        val launchTwoDaysAgo = now - 2 * day

        val unlocked = manager.calculateUnlockedAchievements(emptyList(), listOf(launchTwoDaysAgo))
        assertTrue(unlocked.contains("nosmoke_1d"))
        assertFalse(unlocked.contains("nosmoke_3d"))
    }

    @Test
    fun testAppModuleContainsCoroutineScope() {
        val app = org.koin.dsl.koinApplication {
            modules(com.smokingtracker.di.appModule)
        }
        val scope: kotlinx.coroutines.CoroutineScope? = app.koin.getOrNull()
        assertNotNull(scope)
    }
}
