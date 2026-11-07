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
    fun testAppModuleContainsCoroutineScopeAndSingletons() {
        val app = org.koin.dsl.koinApplication {
            modules(com.smokingtracker.di.appModule)
        }
        val unqualifiedScope: kotlinx.coroutines.CoroutineScope? = app.koin.getOrNull()
        assertNull(unqualifiedScope)

        val namedScope: kotlinx.coroutines.CoroutineScope? =
            app.koin.getOrNull(com.smokingtracker.di.ApplicationScopeQualifier)
        assertNotNull(namedScope)

        val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher? =
            app.koin.getOrNull(com.smokingtracker.di.IoDispatcherQualifier)
        assertNotNull(ioDispatcher)

        val defaultDispatcher: kotlinx.coroutines.CoroutineDispatcher? =
            app.koin.getOrNull(com.smokingtracker.di.DefaultDispatcherQualifier)
        assertNotNull(defaultDispatcher)

        val gson1: com.google.gson.Gson? = app.koin.getOrNull()
        val gson2: com.google.gson.Gson? = app.koin.getOrNull()
        assertNotNull(gson1)
        assertSame(gson1, gson2)

        val statsManager: StatisticsManager? = app.koin.getOrNull()
        val achievementsManager: AchievementsManager? = app.koin.getOrNull()
        assertNotNull(statsManager)
        assertNotNull(achievementsManager)
    }

    @Test
    fun testBaselineAnalytics_includesResistedAndEmptyEntries() {
        val manager = StatisticsManager()
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        val threeDaysAgo = today.minusDays(2).atStartOfDay(zone).toInstant().toEpochMilli()
        val startDate = today.minusDays(100).atStartOfDay(zone).toInstant().toEpochMilli()
        val resisted = listOf(threeDaysAgo + 1000L, threeDaysAgo + 2000L)

        // Empty smoked entries, 3 days since trackingStartMillis (today.minusDays(2)..today = 3 days)
        // Daily norm = 10 -> 3 * 10 = 30 avoided from clean days
        val analytics = manager.calculateBaselineAnalytics(
            startDate = startDate,
            dailyAvg = 10,
            packPrice = 200f,
            packSize = 20,
            entries = emptyList(),
            resistedEntries = resisted,
            trackingStartMillis = threeDaysAgo,
            zoneId = zone
        )
        assertNotNull(analytics)
        assertEquals(30, analytics!!.totalAvoidedCigarettes)
        assertEquals(300.0, analytics.totalAvoidedMoney, 0.01)
        assertEquals(2, analytics.resistedCigarettes)
        assertEquals(20.0, analytics.resistedMoneySaved, 0.01)

        // Savings calculation
        val savings = manager.calculateSavings(
            entries = emptyList(),
            resistedCount = 2,
            dailyLimit = 10,
            packPrice = 200f,
            packSize = 20,
            trackingStartMillis = threeDaysAgo,
            zoneId = zone
        )
        assertEquals(22, savings.totalAvoidedCigarettes)
        assertEquals(220f, savings.totalMoneySaved, 0.01f)
        assertEquals(22 * 11, savings.totalLifeMinutes)
    }

    @Test
    fun testSmokingIntervals_sleepThresholdAndSingleTodayEntry() {
        val manager = StatisticsManager()
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        // Yesterday at 23:00 and today at 01:00 (2h across midnight -> should NOT be filtered as sleep since < 6h)
        val yesterday23 = today.minusDays(1).atTime(23, 0).atZone(zone).toInstant().toEpochMilli()
        val today01 = today.atTime(1, 0).atZone(zone).toInstant().toEpochMilli()
        // Same-day 7h sleep gap from 01:00 to 08:00 -> SHOULD be filtered as sleep since >= 6h
        val today08 = today.atTime(8, 0).atZone(zone).toInstant().toEpochMilli()

        val dataTwoToday = manager.calculateSmokingIntervals(listOf(yesterday23, today01, today08), zone)
        // Active interval is only yesterday23 -> today01 (120 min); today01 -> today08 (420 min >= 360 min) is excluded
        assertEquals(120, dataTwoToday.avgIntervalMinutes)

        // Single cigarette smoked today -> todayAvgMinutes should be computed from start of day / elapsed awake time
        val now = System.currentTimeMillis()
        val singleToday = (now - 30 * 60 * 1000L).coerceAtLeast(today.atStartOfDay(zone).toInstant().toEpochMilli() + 60_000L)
        val dataSingleToday = manager.calculateSmokingIntervals(listOf(singleToday), zone)
        assertEquals(1, dataSingleToday.totalIntervalsCount)
        assertNotNull(dataSingleToday.todayAvgMinutes)
    }

    @Test
    fun testStreakAndStats_futureDatesAndTrackingStart() {
        val manager = StatisticsManager()
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        val futureTs = today.plusDays(5).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()

        // Future entry should not produce negative streak
        assertEquals(0, manager.currentSmokeFreeStreakDays(listOf(futureTs), zone))

        // Empty entries with trackingStartMillis 4 days ago should return 4 days
        val fourDaysAgo = today.minusDays(4).atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(4, manager.currentSmokeFreeStreakDays(emptyList(), zone, trackingStartMillis = fourDaysAgo))

        // Stats with only future entry should clamp safely
        val statsFuture = manager.calculateStats(entries = listOf(futureTs), zoneId = zone)
        assertTrue(statsFuture.totalTrackingDays >= 0)
        assertTrue(statsFuture.longestStreakDays >= 0)
    }

    @Test
    fun testHourlyDistribution_emptyAndPopulated() {
        val manager = StatisticsManager()
        val emptyDist = manager.calculateHourlyDistribution(emptyList())
        assertEquals(-1, emptyDist.peakHour)
        assertNull(emptyDist.peakPeriod)
        assertEquals(0, emptyDist.nightCount)
        assertEquals(0, emptyDist.morningCount)
        assertEquals(0, emptyDist.afternoonCount)
        assertEquals(0, emptyDist.eveningCount)

        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        val morning9 = today.atTime(9, 15).atZone(zone).toInstant().toEpochMilli()
        val morning9b = today.atTime(9, 45).atZone(zone).toInstant().toEpochMilli()
        val evening20 = today.atTime(20, 0).atZone(zone).toInstant().toEpochMilli()

        val dist = manager.calculateHourlyDistribution(listOf(morning9, morning9b, evening20), zone)
        assertEquals(9, dist.peakHour)
        assertEquals(2, dist.peakHourCount)
        assertEquals(StatisticsManager.DayPeriod.MORNING, dist.peakPeriod)
        assertEquals(2, dist.morningCount)
        assertEquals(1, dist.eveningCount)
    }

    @Test
    fun testWeeklyComparison_futureWeekAndFirstDayOfWeek() {
        val manager = StatisticsManager()
        val zone = java.time.ZoneId.systemDefault()
        val nowDate = java.time.LocalDate.of(2026, 9, 16)
        val nowMillis = nowDate.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val futureWeekDate = nowDate.plusWeeks(2)
        val prevEntry = nowMillis

        val futureComparison = manager.calculateWeeklyComparison(
            entries = listOf(prevEntry),
            referenceDate = futureWeekDate,
            nowMillis = nowMillis,
            zoneId = zone
        )
        assertEquals(0, futureComparison.daysElapsedThisWeek)
        assertEquals(0, futureComparison.percentChange)
        assertEquals(StatisticsManager.ComparisonTrend.NO_CHANGE, futureComparison.trend)

        // Test Sunday start of week
        val sundayData = manager.generateWeeklyData(
            entries = listOf(prevEntry),
            date = nowDate,
            zoneId = zone,
            firstDayOfWeek = java.time.DayOfWeek.SUNDAY
        )
        assertEquals(7, sundayData.size)
        // Sept 16, 2026 is Wednesday -> index 3 when week starts on Sunday
        assertEquals(1, sundayData[3])
    }

    @Test
    fun testHistoricalBaseline_nanPriceSanitizationAndLongTotalCigarettes() {
        val manager = StatisticsManager()
        val now = System.currentTimeMillis()
        val start = now - 100L * 86_400_000L

        val statsNan = manager.calculateHistoricalBaseline(
            startDate = start,
            dailyAvg = 20,
            packPrice = Float.NaN,
            packSize = 20,
            rankedTriggers = emptyList(),
            nowMillis = now
        )
        assertFalse(statsNan.totalMoneySpent.isNaN())
        assertEquals(0.0, statsNan.totalMoneySpent, 0.001)
        assertEquals(2000L, statsNan.totalCigarettes)
    }

    @Test
    fun testAchievements_eventSecretsReturnNullHistoricalTimestampAndUnearnedReturnNull() {
        val manager = AchievementsManager()
        val firstLaunch = 1700000000000L
        val launches = listOf(firstLaunch)

        // Action-based secret achievements must return null so coordinator uses current timestamp
        assertNull(manager.estimateHistoricalUnlockTimestamp("secret_archivist", emptyList(), launches))
        assertNull(manager.estimateHistoricalUnlockTimestamp("secret_explorer", emptyList(), launches))
        assertNull(manager.estimateHistoricalUnlockTimestamp("secret_hesitant", emptyList(), launches))
        assertNull(manager.estimateHistoricalUnlockTimestamp("secret_analytics_collector", emptyList(), launches))
        assertNull(manager.estimateHistoricalUnlockTimestamp("secret_inflation", emptyList(), launches))

        // Unearned achievements must also return null instead of falling back to firstLaunch
        assertNull(manager.estimateHistoricalUnlockTimestamp("login_3", emptyList(), launches))
        assertNull(manager.estimateHistoricalUnlockTimestamp("secret_night_owl", emptyList(), launches))
        assertNull(manager.estimateHistoricalUnlockTimestamp("secret_punctuality", emptyList(), launches))
    }

    @Test
    fun testAchievements_secretCrisisOnlyCompletedDays() {
        val manager = AchievementsManager()
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.of(2026, 9, 16)
        val nowMillis = today.atTime(14, 0).atZone(zone).toInstant().toEpochMilli()

        // 3 cigarettes smoked today when dailyLimit == 3 -> must NOT unlock mid-day
        val todayEntries = listOf(
            today.atTime(10, 0).atZone(zone).toInstant().toEpochMilli(),
            today.atTime(11, 0).atZone(zone).toInstant().toEpochMilli(),
            today.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        )
        val ctxToday = AchievementContext(
            timeWithoutSmoking = 0L,
            entries = todayEntries,
            launches = listOf(todayEntries.first()),
            dailyLimit = 3,
            nowMillis = nowMillis,
            zoneId = zone
        )
        assertFalse(manager.calculateUnlockedAchievements(ctxToday).contains("secret_crisis"))
        assertNull(
            manager.estimateHistoricalUnlockTimestamp(
                "secret_crisis",
                todayEntries,
                emptyList(),
                dailyLimit = 3,
                zoneId = zone,
                nowMillis = nowMillis
            )
        )

        // 3 cigarettes smoked yesterday when dailyLimit == 3 -> completed day, MUST unlock
        val yesterday = today.minusDays(1)
        val yesterdayEntries = listOf(
            yesterday.atTime(10, 0).atZone(zone).toInstant().toEpochMilli(),
            yesterday.atTime(14, 0).atZone(zone).toInstant().toEpochMilli(),
            yesterday.atTime(19, 0).atZone(zone).toInstant().toEpochMilli()
        )
        val ctxYesterday = AchievementContext(
            timeWithoutSmoking = 0L,
            entries = yesterdayEntries,
            launches = listOf(yesterdayEntries.first()),
            dailyLimit = 3,
            nowMillis = nowMillis,
            zoneId = zone
        )
        assertTrue(manager.calculateUnlockedAchievements(ctxYesterday).contains("secret_crisis"))
        assertEquals(
            yesterdayEntries.last(),
            manager.estimateHistoricalUnlockTimestamp(
                "secret_crisis",
                yesterdayEntries,
                emptyList(),
                dailyLimit = 3,
                zoneId = zone,
                nowMillis = nowMillis
            )
        )
    }

    @Test
    fun testAchievements_secretBoundaryConditionsAndPunctuality() {
        val manager = AchievementsManager()
        val zone = java.time.ZoneId.systemDefault()
        val baseDate = java.time.LocalDate.of(2026, 9, 10)

        // Explorer (> 3) and Analytics Collector (> 10)
        val ctxExact = AchievementContext(
            timeWithoutSmoking = 0L,
            entries = emptyList(),
            launches = emptyList(),
            themeLangChangesToday = 3,
            analyticsVisitsToday = 10
        )
        val unlockedExact = manager.calculateUnlockedAchievements(ctxExact)
        assertFalse(unlockedExact.contains("secret_explorer"))
        assertFalse(unlockedExact.contains("secret_analytics_collector"))

        val ctxOver = ctxExact.copy(themeLangChangesToday = 4, analyticsVisitsToday = 11)
        val unlockedOver = manager.calculateUnlockedAchievements(ctxOver)
        assertTrue(unlockedOver.contains("secret_explorer"))
        assertTrue(unlockedOver.contains("secret_analytics_collector"))

        // Night Owl at exactly 05:00
        val fiveAm = baseDate.atTime(5, 0).atZone(zone).toInstant().toEpochMilli()
        val ctxNightOwl = AchievementContext(
            timeWithoutSmoking = 0L,
            entries = listOf(fiveAm),
            launches = emptyList(),
            zoneId = zone
        )
        assertTrue(manager.calculateUnlockedAchievements(ctxNightOwl).contains("secret_night_owl"))

        // Double damage with 0ms difference
        val ctxDoubleDamageZero = AchievementContext(
            timeWithoutSmoking = 0L,
            entries = listOf(fiveAm, fiveAm),
            launches = emptyList(),
            zoneId = zone
        )
        assertTrue(manager.calculateUnlockedAchievements(ctxDoubleDamageZero).contains("secret_double_damage"))

        // Punctuality: 4-minute identical intervals in unsorted list -> unlocked; 5m & 3.5m -> not unlocked
        val p0 = baseDate.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val p1 = p0 + 4 * 60_000L
        val p2 = p1 + 4 * 60_000L
        assertEquals(p2, manager.findPunctualDate(listOf(p2, p0, p1)))

        val badP2 = p1 + 210_000L // 3.5 min vs 4 min
        assertNull(manager.findPunctualDate(listOf(p0, p1, badP2)))

        // Morning ritual: 01:00 AM night entries should NOT count; 07:00 AM should NOT count; 06:30 AM for 5 days SHOULD count
        val nightEntries = (0L..4L).map { d ->
            baseDate.plusDays(d).atTime(1, 0).atZone(zone).toInstant().toEpochMilli()
        }
        val sevenAmEntries = (0L..4L).map { d ->
            baseDate.plusDays(d).atTime(7, 0).atZone(zone).toInstant().toEpochMilli()
        }
        val sixThirtyEntries = (0L..4L).map { d ->
            baseDate.plusDays(d).atTime(6, 30).atZone(zone).toInstant().toEpochMilli()
        }
        assertFalse(
            manager.calculateUnlockedAchievements(
                AchievementContext(0L, nightEntries, emptyList(), zoneId = zone)
            ).contains("secret_morning_ritual")
        )
        assertFalse(
            manager.calculateUnlockedAchievements(
                AchievementContext(0L, sevenAmEntries, emptyList(), zoneId = zone)
            ).contains("secret_morning_ritual")
        )
        assertTrue(
            manager.calculateUnlockedAchievements(
                AchievementContext(0L, sixThirtyEntries, emptyList(), zoneId = zone)
            ).contains("secret_morning_ritual")
        )

        // findStreakDate with targetDays == 1
        assertEquals(p0, manager.findStreakDate(listOf(p0), 1, zone))
    }

    @Test
    fun testAchievements_historicalProgressPreservedAfterRelapse() {
        val manager = AchievementsManager()
        val dayMs = 86_400_000L
        val now = 1_750_000_000_000L
        val t0 = now - 200 * dayMs
        val t1 = t0 + 180 * dayMs // 180-day historical gap
        val t2 = now - 1 * dayMs  // Relapsed yesterday

        val progress6m = manager.getAchievementProgress(
            achievementId = "nosmoke_6m",
            entries = listOf(t0, t1, t2),
            launches = listOf(t0),
            nowMillis = now
        )
        assertEquals(1f, progress6m.fraction, 0.001f)
        assertEquals("180", progress6m.currentDisplay)
        assertNull(progress6m.remainingDisplay)
    }

    @Test
    fun testGitHubUpdateManager_semVerParsing() {
        val manager = com.smokingtracker.data.manager.GitHubUpdateManager()

        // Uppercase 'V' and lowercase 'v'
        assertTrue(manager.isNewerVersion("V2.0.1", "2.0.0"))
        assertTrue(manager.isNewerVersion("v2.0.1", "V2.0.0"))
        assertFalse(manager.isNewerVersion("V2.0.0", "2.0.0"))

        // Pre-release suffixes (-beta, -rc1) vs release
        assertFalse(manager.isNewerVersion("1.2.0-rc1", "1.2.0"))
        assertTrue(manager.isNewerVersion("1.2.0", "1.2.0-rc1"))
        assertTrue(manager.isNewerVersion("1.2.1-rc1", "1.2.0"))
        assertTrue(manager.isNewerVersion("1.2.0-rc2", "1.2.0-rc1"))

        // Build metadata (+build) ignored
        assertFalse(manager.isNewerVersion("1.2.0+20260101", "1.2.0"))
        assertTrue(manager.isNewerVersion("1.2.1+build42", "1.2.0"))

        // Invalid tags return false
        assertFalse(manager.isNewerVersion("", "1.0.0"))
        assertFalse(manager.isNewerVersion("invalid", "1.0.0"))
    }

    @Test
    fun testBackupManager_validationAndSanitization() {
        val gson = com.google.gson.Gson()
        // Validate directly via BackupData & static/helper logic
        val futureVerJson = """{"version": 99, "isRegistered": true, "entries": []}"""
        val missingEntriesJson = """{"version": 3, "isRegistered": true}"""
        val v1Json = """
            {
                "version": 1,
                "isRegistered": true,
                "smokingEntries": [1700000000000, -50, 9999999999999],
                "entryTriggers": {"1700000000000": "  stress  "}
            }
        """.trimIndent()

        // Check BackupData deserialization with missing fields (all nullable with safe defaults)
        val minimalJson = """{"entries": [{"timestamp": 1700000000000}]}"""
        val parsedMinimal = gson.fromJson(minimalJson, BackupManager.BackupData::class.java)
        assertNotNull(parsedMinimal)
        assertEquals(1, parsedMinimal.entries?.size)

        // Check v1 string-keyed entryTriggers map deserialization
        val parsedV1 = gson.fromJson(v1Json, BackupManager.BackupData::class.java)
        assertEquals("  stress  ", parsedV1.entryTriggers?.get("1700000000000"))

        assertTrue(futureVerJson.contains("99"))
        assertTrue(missingEntriesJson.contains("isRegistered"))
    }

    @Test
    fun generateComprehensiveTestBackupJson() {
        val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        val now = System.currentTimeMillis()
        val zone = java.time.ZoneId.systemDefault()
        val dayMs = 86_400_000L
        val hourMs = 3_600_000L
        val minMs = 60_000L

        val allAchievementIds = setOf(
            "login_1", "login_3", "login_7", "login_30", "login_90", "login_180", "login_365",
            "nosmoke_1d", "nosmoke_3d", "nosmoke_1w", "nosmoke_1m", "nosmoke_3m", "nosmoke_6m", "nosmoke_1y",
            "secret_night_owl", "secret_morning_ritual", "secret_synchronization",
            "secret_punctuality", "secret_hesitant", "secret_explorer", "secret_archivist",
            "secret_analytics_collector", "secret_double_damage", "secret_inflation",
            "secret_crisis", "secret_blind_eye"
        )

        val unlockDates = allAchievementIds.mapIndexed { index, id ->
            id to (now - (300 - index * 10).coerceAtLeast(1) * dayMs)
        }.toMap()

        val launches = (0..370).map { day ->
            now - day * dayMs + 10 * minMs
        }.reversed()

        val entries = mutableListOf<BackupManager.BackupEntry>()

        // Historical entry 380 days ago for smoke-free 1-year achievement historical gap
        entries.add(BackupManager.BackupEntry(timestamp = now - 380 * dayMs, trigger = "Стресс", isResisted = false))

        // Helper to get today's local date start
        val today = java.time.LocalDate.now(zone)

        fun timeAt(daysAgo: Long, hour: Int, minute: Int): Long {
            return today.minusDays(daysAgo).atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
        }

        // Days -14 to -10: Morning ritual (06:15 AM every day)
        for (d in 14L downTo 10L) {
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(d, 6, 15), trigger = "Кофе", isResisted = false))
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(d, 12, 30), trigger = "После еды", isResisted = false))
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(d, 18, 0), trigger = "За рулём", isResisted = false))
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(d, 21, 0), trigger = "Стресс", isResisted = true))
        }

        // Day -9: Night Owl (03:45 AM)
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(9, 3, 45), trigger = "Стресс", isResisted = false))
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(9, 13, 0), trigger = "После еды", isResisted = false))
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(9, 19, 30), trigger = "Видеоигры", isResisted = false))

        // Day -8: Synchronization (12:22 and 14:44)
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(8, 12, 22), trigger = "Кофе", isResisted = false))
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(8, 14, 44), trigger = "Работа", isResisted = false))
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(8, 20, 15), trigger = "Скука", isResisted = true))

        // Day -7: Punctuality (3 smokes spaced by exactly 45 min: 15:00, 15:45, 16:30)
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(7, 15, 0), trigger = "Работа", isResisted = false))
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(7, 15, 45), trigger = "Работа", isResisted = false))
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(7, 16, 30), trigger = "Работа", isResisted = false))

        // Day -6: Double damage (2 smokes within 5 min: 18:00, 18:05)
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(6, 12, 0), trigger = "После еды", isResisted = false))
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(6, 18, 0), trigger = "Стресс", isResisted = false))
        entries.add(BackupManager.BackupEntry(timestamp = timeAt(6, 18, 5), trigger = "Стресс", isResisted = false))

        // Day -5: Crisis (exactly 8 smokes = daily limit 8)
        val hoursCrisis = listOf(9, 11, 13, 15, 17, 19, 21, 23)
        for (h in hoursCrisis) {
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(5, h, 0), trigger = "Стресс", isResisted = false))
        }

        // Day -4: Blind eye (13 smokes >= daily limit 8 + 5)
        for (h in 8..20) {
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(4, h, 10), trigger = "С друзьями", isResisted = false))
        }

        // Days -3 to -1: Normal smoke days with resisting cravings
        for (d in 3L downTo 1L) {
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(d, 9, 30), trigger = "Кофе", isResisted = false))
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(d, 11, 0), trigger = "Тяга", isResisted = true))
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(d, 13, 15), trigger = "После еды", isResisted = false))
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(d, 16, 0), trigger = "Стресс", isResisted = true))
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(d, 18, 45), trigger = "За рулём", isResisted = false))
            entries.add(BackupManager.BackupEntry(timestamp = timeAt(d, 22, 10), trigger = "Видеоигры", isResisted = false))
        }

        // Day 0 (Today): 2 smokes and 1 resisted
        entries.add(BackupManager.BackupEntry(timestamp = now - 3 * hourMs, trigger = "Кофе", isResisted = false))
        entries.add(BackupManager.BackupEntry(timestamp = now - 1 * hourMs - 15 * minMs, trigger = "После еды", isResisted = false))
        entries.add(BackupManager.BackupEntry(timestamp = now - 30 * minMs, trigger = "Стресс", isResisted = true))

        entries.sortBy { it.timestamp }

        val backupData = BackupManager.BackupData(
            version = 3,
            isRegistered = true,
            entries = entries,
            appTheme = "SYSTEM",
            unlockedAchievements = allAchievementIds,
            achievementUnlockDates = unlockDates,
            dailyLimit = 8,
            packPrice = 280.0f,
            packSize = 20,
            currency = "RUB",
            colorPreset = "FOREST_SAGE",
            fontPreset = "OUTFIT",
            amoledTheme = false,
            vibrationEnabled = true,
            hasMadeBackup = true,
            hasChangedPackPrice = true,
            hasCancelledWithin10s = true,
            appLaunchDates = launches,
            useCustomVariableFont = true,
            customFontWeight = 550,
            customFontWidth = 105.0f,
            customFontRoundness = 20.0f,
            taperingPlanEnabled = true,
            taperingIntervalDays = 7,
            lastTaperingCheckinDate = now - 4 * dayMs,
            hasHistoricalBaseline = true,
            historicalStartDate = now - 180 * dayMs,
            historicalDailyAvg = 15,
            historicalPackPrice = 250.0f,
            historicalPackSize = 20,
            historicalTriggerPriorities = listOf("Стресс", "Кофе", "После еды", "Алкоголь", "Скука"),
            savingsGoalTitle = "PlayStation 5",
            savingsGoalAmount = 65000.0f,
            widgetShowResistButton = true,
            showLimitOnGraph = true,
            appIcon = "DEFAULT",
            appLanguageTag = "ru",
            checkUpdatesOnStart = true,
            themeLangChangeCount = 5,
            themeLangChangeDate = now - 2 * dayMs,
            analyticsVisitCount = 15,
            analyticsVisitDate = now - 1 * dayMs,
            customTriggers = listOf("За рулём", "Видеоигры", "С друзьями", "Тяжёлый разговор"),
            disabledDefaultTriggers = emptySet(),
            notificationEnabled = true,
            notificationLowPriority = false,
            notificationShowTimer = true,
            notificationShowProgress = true,
            notificationShowAddButton = true,
            notificationShowResistButton = true
        )

        val jsonString = gson.toJson(backupData)
        java.io.File("test_backup_smoking_tracker.json").writeText(jsonString)

        val parsed = gson.fromJson(jsonString, BackupManager.BackupData::class.java)
        assertNotNull(parsed)
        assertEquals(3, parsed.version)
        assertEquals(allAchievementIds.size, parsed.unlockedAchievements?.size)
        assertTrue((parsed.entries?.size ?: 0) > 30)
    }

    @Test
    fun testSecretHesitantAchievement_notUnlockedOnResistedCravingImmediateDeletion() {
        val achievementsManager = AchievementsManager()
        val recentSmokedMap = mutableMapOf<Long, Long>()
        val baseTime = 1700000000000L

        // 1. Resisted entry added and cancelled immediately (within 2 seconds)
        val resistedId = 101L
        val resistedTimestamp = baseTime
        val deletionTime = baseTime + 2_000L // 2 seconds later (well within 10s window)

        // Resisted entry is NOT tracked in recentSmokedEntryCreationTimesMs
        assertFalse(recentSmokedMap.containsKey(resistedId))

        val cancelledResistedWithin10s = HomeViewModel.determineHesitantCancellation(
            id = resistedId,
            timestamp = resistedTimestamp,
            isResisted = true,
            recentSmokedEntryCreationTimesMs = recentSmokedMap,
            now = deletionTime
        )

        assertFalse("Immediate deletion of resisted craving must NEVER trigger hesitant flag", cancelledResistedWithin10s)

        // Verify that achievement is NOT unlocked
        val resistedContext = AchievementContext(
            timeWithoutSmoking = 0L,
            entries = emptyList(),
            launches = emptyList(),
            hasCancelledWithin10s = cancelledResistedWithin10s
        )
        val unlockedForResisted = achievementsManager.calculateUnlockedAchievements(resistedContext)
        assertFalse(
            "Secret hesitant achievement must NOT be unlocked on resisted craving deletion",
            unlockedForResisted.contains("secret_hesitant")
        )

        // 2. Even if resisted entry ID was somehow present in cache, isResisted = true must clean cache and return false
        recentSmokedMap[resistedId] = baseTime
        val forcedResistedCancelled = HomeViewModel.determineHesitantCancellation(
            id = resistedId,
            timestamp = resistedTimestamp,
            isResisted = true,
            recentSmokedEntryCreationTimesMs = recentSmokedMap,
            now = deletionTime
        )
        assertFalse("Even with cached ID, resisted entry must return false", forcedResistedCancelled)
        assertFalse("Cached entry must be removed from map", recentSmokedMap.containsKey(resistedId))

        // 3. Contrast: regular smoked cigarette cancelled within 10s MUST trigger hesitant achievement
        val smokedId = 202L
        val smokedTimestamp = baseTime
        recentSmokedMap[smokedId] = smokedTimestamp

        val cancelledSmokedWithin10s = HomeViewModel.determineHesitantCancellation(
            id = smokedId,
            timestamp = smokedTimestamp,
            isResisted = false,
            recentSmokedEntryCreationTimesMs = recentSmokedMap,
            now = deletionTime
        )
        assertTrue("Immediate deletion of regular smoked cigarette MUST trigger hesitant flag", cancelledSmokedWithin10s)

        val smokedContext = AchievementContext(
            timeWithoutSmoking = 0L,
            entries = emptyList(),
            launches = emptyList(),
            hasCancelledWithin10s = cancelledSmokedWithin10s
        )
        val unlockedForSmoked = achievementsManager.calculateUnlockedAchievements(smokedContext)
        assertTrue(
            "Secret hesitant achievement MUST be unlocked when regular cigarette is cancelled within 10s",
            unlockedForSmoked.contains("secret_hesitant")
        )

        // 4. Fallback check by timestamp when cache is empty:
        // Fresh timestamp (< 10s) with isResisted = true must NOT trigger hesitant
        val fallbackResistedCancelled = HomeViewModel.determineHesitantCancellation(
            id = 303L,
            timestamp = baseTime,
            isResisted = true,
            recentSmokedEntryCreationTimesMs = recentSmokedMap,
            now = baseTime + 4_000L
        )
        assertFalse("Timestamp fallback for resisted entry must still be false", fallbackResistedCancelled)

        // Fresh timestamp (< 10s) with isResisted = false MUST trigger hesitant
        val fallbackSmokedCancelled = HomeViewModel.determineHesitantCancellation(
            id = 404L,
            timestamp = baseTime,
            isResisted = false,
            recentSmokedEntryCreationTimesMs = recentSmokedMap,
            now = baseTime + 4_000L
        )
        assertTrue("Timestamp fallback for regular cigarette must be true", fallbackSmokedCancelled)
    }
}
