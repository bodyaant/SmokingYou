package com.smokingtracker

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.roundToLong

data class StatisticsData(
    val maxPerDay: Int,
    val minPerDay: Int,
    val avgPerDay: Float,
    val totalCount: Int,
    val trackingSince: Long?,
    val longestStreakDays: Int,
    val totalTrackingDays: Int
) {
    companion object {
        val EMPTY = StatisticsData(
            maxPerDay = 0,
            minPerDay = 0,
            avgPerDay = 0f,
            totalCount = 0,
            trackingSince = null,
            longestStreakDays = 0,
            totalTrackingDays = 0
        )
    }
}

class StatisticsManager(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    fun calculateStats(
        entries: List<Long>,
        trackingStartMillis: Long? = null,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): StatisticsData {
        val today = toLocalDate(nowMillis, zoneId)
        val validEntries = entries.filter { it > 0L }

        if (validEntries.isEmpty()) {
            val validTrackingStart = trackingStartMillis?.takeIf { it > 0L } ?: return StatisticsData.EMPTY
            val startDay = toLocalDate(validTrackingStart, zoneId).let { if (it.isAfter(today)) today else it }
            val streakDays = daysBetween(startDay, today).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
            val trackingDays = (streakDays + 1L).coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
            return StatisticsData(
                maxPerDay = 0,
                minPerDay = 0,
                avgPerDay = 0f,
                totalCount = 0,
                trackingSince = validTrackingStart.coerceAtMost(nowMillis),
                longestStreakDays = streakDays,
                totalTrackingDays = trackingDays
            )
        }

        val nonFutureEntries = validEntries.filter { !toLocalDate(it, zoneId).isAfter(today) }
            .ifEmpty { validEntries }
        val sortedEntries = nonFutureEntries.sorted()
        val totalCount = nonFutureEntries.size
        val earliestEntry = sortedEntries.first()
        val trackingSince = earliestEntry

        val effectiveStartMillis = if (trackingStartMillis != null && trackingStartMillis in 1..earliestEntry) {
            trackingStartMillis
        } else {
            earliestEntry
        }

        val dailyCounts = groupCountByDay(nonFutureEntries, zoneId)

        val maxPerDay = dailyCounts.values.maxOrNull() ?: 0
        val minPerDay = dailyCounts.values.minOrNull() ?: 0

        val rawFirstDate = toLocalDate(effectiveStartMillis, zoneId)
        val firstDate = if (rawFirstDate.isAfter(today)) today else rawFirstDate

        val totalTrackingDays = (daysBetween(firstDate, today) + 1L)
            .coerceIn(1L, Int.MAX_VALUE.toLong())
            .toInt()

        val avgPerDay = totalCount.toFloat() / totalTrackingDays.coerceAtLeast(1)

        val longestStreak = calculateLongestStreak(
            sortedEntries = sortedEntries,
            zoneId = zoneId,
            trackingStartMillis = trackingStartMillis,
            today = today
        )

        val effectiveMinPerDay = if (totalTrackingDays > dailyCounts.size) 0 else minPerDay

        return StatisticsData(
            maxPerDay = maxPerDay,
            minPerDay = effectiveMinPerDay,
            avgPerDay = avgPerDay,
            totalCount = totalCount,
            trackingSince = trackingSince,
            longestStreakDays = longestStreak,
            totalTrackingDays = totalTrackingDays
        )
    }

    suspend fun calculateStatsAsync(
        entries: List<Long>,
        trackingStartMillis: Long? = null,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): StatisticsData = withContext(defaultDispatcher) {
        calculateStats(entries, trackingStartMillis, zoneId, nowMillis)
    }

    private fun calculateLongestStreak(
        sortedEntries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        trackingStartMillis: Long? = null,
        today: LocalDate = LocalDate.now(zoneId)
    ): Int {
        val entryDates = sortedEntries
            .map { toLocalDate(it, zoneId) }
            .filter { !it.isAfter(today) }
            .distinct()
            .sorted()

        if (entryDates.isEmpty()) {
            val validStart = trackingStartMillis?.takeIf { it > 0L } ?: return 0
            val startDay = toLocalDate(validStart, zoneId)
            if (startDay.isAfter(today)) return 0
            return daysBetween(startDay, today).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        }

        var maxStreak = 0

        if (trackingStartMillis != null && trackingStartMillis > 0L) {
            val startDay = toLocalDate(trackingStartMillis, zoneId)
            if (startDay.isBefore(entryDates.first())) {
                val initialGap = daysBetween(startDay, entryDates.first())
                    .coerceIn(0L, Int.MAX_VALUE.toLong())
                    .toInt()
                if (initialGap > maxStreak) {
                    maxStreak = initialGap
                }
            }
        }

        for (i in 0 until entryDates.size - 1) {
            val gapDays = (daysBetween(entryDates[i], entryDates[i + 1]) - 1L)
                .coerceIn(0L, Int.MAX_VALUE.toLong())
                .toInt()
            if (gapDays > maxStreak) {
                maxStreak = gapDays
            }
        }

        val lastEntry = entryDates.last()
        val currentGap = if (lastEntry.isAfter(today)) {
            0
        } else {
            daysBetween(lastEntry, today).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        }
        if (currentGap > maxStreak) {
            maxStreak = currentGap
        }

        return maxStreak.coerceAtLeast(0)
    }

    fun currentSmokeFreeStreakDays(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        trackingStartMillis: Long? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): Int {
        val today = toLocalDate(nowMillis, zoneId)
        val validEntries = entries.filter { it > 0L }

        if (validEntries.isEmpty()) {
            val validStart = trackingStartMillis?.takeIf { it > 0L } ?: return 0
            val startDay = toLocalDate(validStart, zoneId)
            if (startDay.isAfter(today)) return 0
            return daysBetween(startDay, today).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        }

        val nonFutureDates = validEntries
            .map { toLocalDate(it, zoneId) }
            .filter { !it.isAfter(today) }
        val lastDay = nonFutureDates.maxOrNull() ?: return 0

        return daysBetween(lastDay, today).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    }

    suspend fun currentSmokeFreeStreakDaysAsync(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        trackingStartMillis: Long? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): Int = withContext(defaultDispatcher) {
        currentSmokeFreeStreakDays(entries, zoneId, trackingStartMillis, nowMillis)
    }

    fun getWeeklyCount(
        entries: List<Long>,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): Int {
        val weekStart = startOfWeek(date, firstDayOfWeek)
        val weekStartMillis = startOfDayMillis(weekStart, zoneId)
        val weekEndMillis = startOfDayMillis(weekStart.plusDays(7), zoneId)
        return entries.count { it in weekStartMillis until weekEndMillis }
    }

    fun getWeeklyCount(
        entries: List<Long>,
        date: Calendar,
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): Int {
        val zoneId = date.timeZone.toZoneId()
        val localDate = toLocalDate(date.timeInMillis, zoneId)
        return getWeeklyCount(entries, localDate, zoneId, firstDayOfWeek)
    }

    suspend fun getWeeklyCountAsync(
        entries: List<Long>,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): Int = withContext(defaultDispatcher) {
        getWeeklyCount(entries, date, zoneId, firstDayOfWeek)
    }

    fun getMonthlyCount(
        entries: List<Long>,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        val monthStart = date.withDayOfMonth(1)
        val monthStartMillis = startOfDayMillis(monthStart, zoneId)
        val monthEndMillis = startOfDayMillis(monthStart.plusMonths(1), zoneId)
        return entries.count { it in monthStartMillis until monthEndMillis }
    }

    fun getMonthlyCount(entries: List<Long>, date: Calendar): Int {
        val zoneId = date.timeZone.toZoneId()
        val localDate = toLocalDate(date.timeInMillis, zoneId)
        return getMonthlyCount(entries, localDate, zoneId)
    }

    suspend fun getMonthlyCountAsync(
        entries: List<Long>,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int = withContext(defaultDispatcher) {
        getMonthlyCount(entries, date, zoneId)
    }

    fun generateDailyData(
        entries: List<Long>,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<Int> {
        val dayStartMillis = startOfDayMillis(date, zoneId)
        val dayEndMillis = startOfDayMillis(date.plusDays(1), zoneId)
        val hourlyCounts = IntArray(24)
        for (time in entries) {
            if (time in dayStartMillis until dayEndMillis) {
                val hour = hourOfDay(time, zoneId)
                if (hour in 0..23) hourlyCounts[hour]++
            }
        }
        return hourlyCounts.toList()
    }

    fun generateDailyData(entries: List<Long>, date: Calendar): List<Int> {
        val zoneId = date.timeZone.toZoneId()
        val localDate = toLocalDate(date.timeInMillis, zoneId)
        return generateDailyData(entries, localDate, zoneId)
    }

    suspend fun generateDailyDataAsync(
        entries: List<Long>,
        date: Calendar
    ): List<Int> = withContext(defaultDispatcher) {
        generateDailyData(entries, date)
    }

    fun generateWeeklyData(
        entries: List<Long>,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): List<Int> {
        val weekStart = startOfWeek(date, firstDayOfWeek)
        val weekStartMillis = startOfDayMillis(weekStart, zoneId)
        val weekEndMillis = startOfDayMillis(weekStart.plusDays(7), zoneId)
        val dailyCounts = IntArray(7)
        for (time in entries) {
            if (time in weekStartMillis until weekEndMillis) {
                val entryDate = toLocalDate(time, zoneId)
                val diffDays = daysBetween(weekStart, entryDate).toInt().coerceIn(0, 6)
                dailyCounts[diffDays]++
            }
        }
        return dailyCounts.toList()
    }

    fun generateWeeklyData(
        entries: List<Long>,
        date: Calendar,
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): List<Int> {
        val zoneId = date.timeZone.toZoneId()
        val localDate = toLocalDate(date.timeInMillis, zoneId)
        return generateWeeklyData(entries, localDate, zoneId, firstDayOfWeek)
    }

    suspend fun generateWeeklyDataAsync(
        entries: List<Long>,
        date: Calendar,
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): List<Int> = withContext(defaultDispatcher) {
        generateWeeklyData(entries, date, firstDayOfWeek)
    }

    fun generateMonthlyData(
        entries: List<Long>,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<Int> {
        val dailyCounts = generateMonthlyDailyDataArray(entries, date, zoneId)
        val daysInMonth = dailyCounts.size
        val chunkSize = ceil(daysInMonth / 4.0).toInt().coerceAtLeast(1)
        val weeklyChunks = IntArray(4)
        for (i in 0 until 4) {
            var sum = 0
            for (j in 0 until chunkSize) {
                val index = i * chunkSize + j
                if (index < daysInMonth) sum += dailyCounts[index]
            }
            weeklyChunks[i] = sum
        }
        return weeklyChunks.toList()
    }

    fun generateMonthlyData(entries: List<Long>, date: Calendar): List<Int> {
        val zoneId = date.timeZone.toZoneId()
        val localDate = toLocalDate(date.timeInMillis, zoneId)
        return generateMonthlyData(entries, localDate, zoneId)
    }

    suspend fun generateMonthlyDataAsync(
        entries: List<Long>,
        date: Calendar
    ): List<Int> = withContext(defaultDispatcher) {
        generateMonthlyData(entries, date)
    }

    fun generateMonthlyDailyData(
        entries: List<Long>,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<Int> {
        return generateMonthlyDailyDataArray(entries, date, zoneId).toList()
    }

    fun generateMonthlyDailyData(entries: List<Long>, date: Calendar): List<Int> {
        val zoneId = date.timeZone.toZoneId()
        val localDate = toLocalDate(date.timeInMillis, zoneId)
        return generateMonthlyDailyData(entries, localDate, zoneId)
    }

    suspend fun generateMonthlyDailyDataAsync(
        entries: List<Long>,
        date: Calendar
    ): List<Int> = withContext(defaultDispatcher) {
        generateMonthlyDailyData(entries, date)
    }

    private fun generateMonthlyDailyDataArray(
        entries: List<Long>,
        date: LocalDate,
        zoneId: ZoneId
    ): IntArray {
        val monthStart = date.withDayOfMonth(1)
        val monthStartMillis = startOfDayMillis(monthStart, zoneId)
        val monthEndMillis = startOfDayMillis(monthStart.plusMonths(1), zoneId)
        val daysInMonth = monthStart.lengthOfMonth()
        val dailyCounts = IntArray(daysInMonth)
        for (time in entries) {
            if (time in monthStartMillis until monthEndMillis) {
                val dayIndex = toLocalDate(time, zoneId).dayOfMonth - 1
                if (dayIndex in 0 until daysInMonth) dailyCounts[dayIndex]++
            }
        }
        return dailyCounts
    }

    fun generateYearlyData(
        entries: List<Long>,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<Int> {
        val yearStart = date.withDayOfYear(1)
        val yearStartMillis = startOfDayMillis(yearStart, zoneId)
        val yearEndMillis = startOfDayMillis(yearStart.plusYears(1), zoneId)
        val monthlyCounts = IntArray(12)
        for (time in entries) {
            if (time in yearStartMillis until yearEndMillis) {
                val monthIndex = toLocalDate(time, zoneId).monthValue - 1
                if (monthIndex in 0..11) monthlyCounts[monthIndex]++
            }
        }
        return monthlyCounts.toList()
    }

    fun generateYearlyData(entries: List<Long>, date: Calendar): List<Int> {
        val zoneId = date.timeZone.toZoneId()
        val localDate = toLocalDate(date.timeInMillis, zoneId)
        return generateYearlyData(entries, localDate, zoneId)
    }

    suspend fun generateYearlyDataAsync(
        entries: List<Long>,
        date: Calendar
    ): List<Int> = withContext(defaultDispatcher) {
        generateYearlyData(entries, date)
    }

    enum class PackYearsRiskLevel {
        LOW,
        MODERATE,
        HIGH
    }

    data class SmokingBaselineAnalytics(
        val baselineDailyAvg: Int,
        val smokingYears: Float,
        val totalYearsInt: Int,
        val totalMonthsInt: Int,
        val packYears: Float,
        val riskLevel: PackYearsRiskLevel,
        val pastCigarettes: Long,
        val pastMoneySpent: Double,
        val currentAvg7Days: Float,
        val reductionPercentage: Int,
        val totalAvoidedCigarettes: Int,
        val totalAvoidedMoney: Double,
        val resistedCigarettes: Int = 0,
        val resistedMoneySaved: Double = 0.0,
        val resistedLifeMinutes: Int = 0
    )

    data class HistoricalBaselineStats(
        val totalCigarettes: Long,
        val totalMoneySpent: Double,
        val totalDays: Int,
        val estimatedTriggerCounts: Map<String, Int>
    )

    data class SavingsData(
        val currentStreakDays: Int = 0,
        val streakCigarettesSaved: Int = 0,
        val streakMoneySaved: Float = 0f,
        val streakLifeMinutesSaved: Int = 0,
        val resistedCount: Int = 0,
        val resistedMoneySaved: Float = 0f,
        val resistedLifeMinutes: Int = 0,
        val totalMoneySaved: Float = 0f,
        val totalLifeMinutes: Int = 0,
        val totalAvoidedCigarettes: Int = 0
    )

    fun calculateSavings(
        entries: List<Long>,
        resistedCount: Int,
        dailyLimit: Int,
        packPrice: Float,
        packSize: Int,
        trackingStartMillis: Long? = null,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): SavingsData {
        val pricePerCig = sanitizePricePerCigarette(packPrice, packSize).toFloat()
        val safeLimit = dailyLimit.coerceAtLeast(0)
        val safeResisted = resistedCount.coerceAtLeast(0)

        val currentStreakDays = currentSmokeFreeStreakDays(
            entries = entries,
            zoneId = zoneId,
            trackingStartMillis = trackingStartMillis,
            nowMillis = nowMillis
        )
        val streakCigarettesSaved = (currentStreakDays.toLong() * safeLimit.toLong())
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()
        val streakMoneySaved = streakCigarettesSaved.toFloat() * pricePerCig
        val streakLifeMinutesSaved = (streakCigarettesSaved.toLong() * MINUTES_PER_CIGARETTE)
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()

        val resistedMoneySaved = safeResisted.toFloat() * pricePerCig
        val resistedLifeMinutes = (safeResisted.toLong() * MINUTES_PER_CIGARETTE)
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()

        val totalMoneySaved = streakMoneySaved + resistedMoneySaved
        val totalLifeMinutes = (streakLifeMinutesSaved.toLong() + resistedLifeMinutes.toLong())
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()
        val totalAvoidedCigarettes = (streakCigarettesSaved.toLong() + safeResisted.toLong())
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()

        return SavingsData(
            currentStreakDays = currentStreakDays,
            streakCigarettesSaved = streakCigarettesSaved,
            streakMoneySaved = streakMoneySaved,
            streakLifeMinutesSaved = streakLifeMinutesSaved,
            resistedCount = safeResisted,
            resistedMoneySaved = resistedMoneySaved,
            resistedLifeMinutes = resistedLifeMinutes,
            totalMoneySaved = totalMoneySaved,
            totalLifeMinutes = totalLifeMinutes,
            totalAvoidedCigarettes = totalAvoidedCigarettes
        )
    }

    suspend fun calculateSavingsAsync(
        entries: List<Long>,
        resistedCount: Int,
        dailyLimit: Int,
        packPrice: Float,
        packSize: Int,
        trackingStartMillis: Long? = null,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): SavingsData = withContext(defaultDispatcher) {
        calculateSavings(
            entries = entries,
            resistedCount = resistedCount,
            dailyLimit = dailyLimit,
            packPrice = packPrice,
            packSize = packSize,
            trackingStartMillis = trackingStartMillis,
            zoneId = zoneId,
            nowMillis = nowMillis
        )
    }

    fun calculateBaselineAnalytics(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        entries: List<Long>,
        resistedEntries: List<Long> = emptyList(),
        trackingStartMillis: Long? = null,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): SmokingBaselineAnalytics? {
        if (startDate <= 0L || dailyAvg <= 0) return null
        if (startDate >= nowMillis) return null

        val diffMs = nowMillis - startDate
        val totalDaysUntilNow = TimeUnit.MILLISECONDS.toDays(diffMs).coerceAtLeast(1L)
        val smokingYears = (totalDaysUntilNow / 365.25f).coerceAtLeast(0.1f)
        val totalYearsInt = (totalDaysUntilNow / 365L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val totalMonthsInt = ((totalDaysUntilNow % 365L) / 30L).toInt()

        val packYears = (dailyAvg.toFloat() / 20f) * smokingYears
        val riskLevel = when {
            packYears < 10f -> PackYearsRiskLevel.LOW
            packYears < 20f -> PackYearsRiskLevel.MODERATE
            else -> PackYearsRiskLevel.HIGH
        }

        val pricePerCig = sanitizePricePerCigarette(packPrice, packSize)

        val validEntries = entries.filter { it in 1..nowMillis }
        val validResisted = resistedEntries.filter { it in 1..nowMillis }

        val earliestAppTimestamp = listOfNotNull(
            validEntries.minOrNull(),
            validResisted.minOrNull(),
            trackingStartMillis?.takeIf { it in 1..nowMillis }
        ).minOrNull() ?: nowMillis

        val effectiveTrackingStart = earliestAppTimestamp.coerceIn(startDate, nowMillis)
        val priorDurationMs = (effectiveTrackingStart - startDate).coerceAtLeast(0L)
        val priorDays = TimeUnit.MILLISECONDS.toDays(priorDurationMs).coerceAtLeast(0L)
        val pastCigarettes = priorDays * dailyAvg.toLong()
        val pastMoneySpent = pastCigarettes.toDouble() * pricePerCig

        val sevenDaysAgo = nowMillis - TimeUnit.DAYS.toMillis(7)
        val last7DaysCount = validEntries.count { it in sevenDaysAgo..nowMillis }
        val currentAvg7Days = last7DaysCount / 7.0f

        val reductionDiff = dailyAvg - currentAvg7Days
        val reductionPercentage = ((reductionDiff / dailyAvg.toFloat()) * 100f)
            .roundToInt()
            .coerceIn(-100, 100)

        val countsByDay = groupCountByDay(validEntries, zoneId)
        val resistedByDay = groupCountByDay(validResisted, zoneId)
        var currentDay = toLocalDate(effectiveTrackingStart, zoneId)
        val today = toLocalDate(nowMillis, zoneId)
        if (currentDay.isAfter(today)) {
            currentDay = today
        }

        var avoidedSum = 0L
        while (!currentDay.isAfter(today)) {
            val actual = countsByDay[currentDay] ?: 0
            val resistedOnDay = resistedByDay[currentDay] ?: 0
            val baselineDiff = (dailyAvg - actual).coerceAtLeast(0)
            avoidedSum += maxOf(baselineDiff, resistedOnDay).toLong()
            currentDay = currentDay.plusDays(1)
        }

        val resistedCount = validResisted.size
        val totalAvoidedCigarettes = maxOf(avoidedSum, resistedCount.toLong())
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()
        val totalAvoidedMoney = totalAvoidedCigarettes.toDouble() * pricePerCig
        val resistedMoneySaved = resistedCount.toDouble() * pricePerCig
        val resistedLifeMinutes = (resistedCount.toLong() * MINUTES_PER_CIGARETTE)
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()

        return SmokingBaselineAnalytics(
            baselineDailyAvg = dailyAvg,
            smokingYears = smokingYears,
            totalYearsInt = totalYearsInt,
            totalMonthsInt = totalMonthsInt,
            packYears = packYears,
            riskLevel = riskLevel,
            pastCigarettes = pastCigarettes,
            pastMoneySpent = pastMoneySpent,
            currentAvg7Days = currentAvg7Days,
            reductionPercentage = reductionPercentage,
            totalAvoidedCigarettes = totalAvoidedCigarettes,
            totalAvoidedMoney = totalAvoidedMoney,
            resistedCigarettes = resistedCount,
            resistedMoneySaved = resistedMoneySaved,
            resistedLifeMinutes = resistedLifeMinutes
        )
    }

    suspend fun calculateBaselineAnalyticsAsync(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        entries: List<Long>,
        resistedEntries: List<Long> = emptyList(),
        trackingStartMillis: Long? = null,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): SmokingBaselineAnalytics? = withContext(defaultDispatcher) {
        calculateBaselineAnalytics(
            startDate = startDate,
            dailyAvg = dailyAvg,
            packPrice = packPrice,
            packSize = packSize,
            entries = entries,
            resistedEntries = resistedEntries,
            trackingStartMillis = trackingStartMillis,
            zoneId = zoneId,
            nowMillis = nowMillis
        )
    }

    fun calculateHistoricalBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        rankedTriggers: List<String>,
        nowMillis: Long = System.currentTimeMillis()
    ): HistoricalBaselineStats {
        if (startDate <= 0L || dailyAvg <= 0 || startDate >= nowMillis) {
            return HistoricalBaselineStats(0L, 0.0, 0, emptyMap())
        }

        val diffMs = nowMillis - startDate
        val totalDaysLong = TimeUnit.MILLISECONDS.toDays(diffMs).coerceAtLeast(1L)
        val totalDays = totalDaysLong.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val totalCigarettes = totalDaysLong * dailyAvg.toLong()
        val pricePerCigarette = sanitizePricePerCigarette(packPrice, packSize)
        val totalMoneySpent = totalCigarettes.toDouble() * pricePerCigarette

        val triggerCounts = mutableMapOf<String, Int>()
        if (rankedTriggers.isNotEmpty()) {
            val n = rankedTriggers.size
            val totalWeight = (n.toLong() * (n + 1L)) / 2L
            val clampedTotalForDistribution = totalCigarettes.coerceAtMost(Int.MAX_VALUE.toLong())
            var remaining = clampedTotalForDistribution

            rankedTriggers.forEachIndexed { index, triggerKey ->
                val weight = n - index
                val count = ((weight.toDouble() / totalWeight.toDouble()) * clampedTotalForDistribution.toDouble())
                    .roundToLong()
                    .coerceIn(0L, Int.MAX_VALUE.toLong())
                triggerCounts[triggerKey] = count.toInt()
                remaining -= count
            }
            if (remaining != 0L) {
                val firstKey = rankedTriggers.first()
                val adjusted = ((triggerCounts[firstKey]?.toLong() ?: 0L) + remaining)
                    .coerceIn(0L, Int.MAX_VALUE.toLong())
                    .toInt()
                triggerCounts[firstKey] = adjusted
            }
        }

        return HistoricalBaselineStats(
            totalCigarettes = totalCigarettes,
            totalMoneySpent = totalMoneySpent,
            totalDays = totalDays,
            estimatedTriggerCounts = triggerCounts
        )
    }

    suspend fun calculateHistoricalBaselineAsync(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        rankedTriggers: List<String>,
        nowMillis: Long = System.currentTimeMillis()
    ): HistoricalBaselineStats = withContext(defaultDispatcher) {
        calculateHistoricalBaseline(startDate, dailyAvg, packPrice, packSize, rankedTriggers, nowMillis)
    }

    enum class ComparisonTrend {
        DECREASED, INCREASED, NO_CHANGE
    }

    data class WeeklyComparisonData(
        val thisWeekCount: Int,
        val lastWeekCount: Int,
        val difference: Int,
        val percentChange: Int,
        val trend: ComparisonTrend,
        val thisWeekDailyAvg: Float,
        val lastWeekDailyAvg: Float,
        val diffDailyAvg: Float,
        val isCurrentWeek: Boolean,
        val daysElapsedThisWeek: Int
    ) {
        companion object {
            val EMPTY = WeeklyComparisonData(
                thisWeekCount = 0,
                lastWeekCount = 0,
                difference = 0,
                percentChange = 0,
                trend = ComparisonTrend.NO_CHANGE,
                thisWeekDailyAvg = 0f,
                lastWeekDailyAvg = 0f,
                diffDailyAvg = 0f,
                isCurrentWeek = true,
                daysElapsedThisWeek = 1
            )
        }
    }

    fun calculateWeeklyComparison(
        entries: List<Long>,
        referenceDate: LocalDate = LocalDate.now(),
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): WeeklyComparisonData {
        val thisWeekStartLocalDate = startOfWeek(referenceDate, firstDayOfWeek)
        val thisWeekStart = startOfDayMillis(thisWeekStartLocalDate, zoneId)
        val thisWeekEnd = startOfDayMillis(thisWeekStartLocalDate.plusDays(7), zoneId)

        val prevWeekStart = startOfDayMillis(thisWeekStartLocalDate.minusDays(7), zoneId)
        val prevWeekEnd = thisWeekStart

        var thisWeekCount = 0
        var lastWeekCount = 0
        for (ts in entries) {
            if (ts in thisWeekStart until thisWeekEnd) {
                thisWeekCount++
            } else if (ts in prevWeekStart until prevWeekEnd) {
                lastWeekCount++
            }
        }
        val diff = thisWeekCount - lastWeekCount

        val isFutureWeek = thisWeekStart > nowMillis
        val isCurrentWeek = nowMillis in thisWeekStart until thisWeekEnd
        val daysElapsedThisWeek = when {
            isFutureWeek -> 0
            isCurrentWeek -> {
                val nowLocalDate = toLocalDate(nowMillis, zoneId)
                (daysBetween(thisWeekStartLocalDate, nowLocalDate).toInt() + 1).coerceIn(1, 7)
            }
            else -> 7
        }

        val thisWeekDailyAvg = if (daysElapsedThisWeek > 0) {
            thisWeekCount.toFloat() / daysElapsedThisWeek
        } else {
            0f
        }
        val lastWeekDailyAvg = lastWeekCount.toFloat() / 7f
        val diffDailyAvg = if (isFutureWeek) 0f else thisWeekDailyAvg - lastWeekDailyAvg

        val percentChange = when {
            isFutureWeek -> 0
            lastWeekDailyAvg == 0f -> if (thisWeekDailyAvg > 0f) 100 else 0
            else -> ((abs(diffDailyAvg.toDouble()) * 100.0) / lastWeekDailyAvg).roundToInt()
        }

        val trend = when {
            isFutureWeek -> ComparisonTrend.NO_CHANGE
            diffDailyAvg < -0.01f -> ComparisonTrend.DECREASED
            diffDailyAvg > 0.01f -> ComparisonTrend.INCREASED
            else -> ComparisonTrend.NO_CHANGE
        }

        return WeeklyComparisonData(
            thisWeekCount = thisWeekCount,
            lastWeekCount = lastWeekCount,
            difference = diff,
            percentChange = percentChange,
            trend = trend,
            thisWeekDailyAvg = thisWeekDailyAvg,
            lastWeekDailyAvg = lastWeekDailyAvg,
            diffDailyAvg = diffDailyAvg,
            isCurrentWeek = isCurrentWeek,
            daysElapsedThisWeek = daysElapsedThisWeek
        )
    }

    fun calculateWeeklyComparison(
        entries: List<Long>,
        referenceDate: Calendar = Calendar.getInstance(),
        now: Calendar = Calendar.getInstance(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): WeeklyComparisonData {
        val zoneId = referenceDate.timeZone.toZoneId()
        val refDate = toLocalDate(referenceDate.timeInMillis, zoneId)
        return calculateWeeklyComparison(
            entries = entries,
            referenceDate = refDate,
            nowMillis = now.timeInMillis,
            zoneId = zoneId,
            firstDayOfWeek = firstDayOfWeek
        )
    }

    suspend fun calculateWeeklyComparisonAsync(
        entries: List<Long>,
        referenceDate: Calendar = Calendar.getInstance(),
        now: Calendar = Calendar.getInstance(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): WeeklyComparisonData = withContext(defaultDispatcher) {
        calculateWeeklyComparison(entries, referenceDate, now, firstDayOfWeek)
    }

    enum class DayPeriod {
        NIGHT,
        MORNING,
        AFTERNOON,
        EVENING
    }

    data class HourlyDistributionData(
        val hourlyCounts: List<Int>,
        val peakHour: Int,
        val peakHourCount: Int,
        val peakPeriod: DayPeriod?,
        val peakPeriodPercent: Int,
        val nightCount: Int = 0,
        val morningCount: Int = 0,
        val afternoonCount: Int = 0,
        val eveningCount: Int = 0
    ) {
        companion object {
            val EMPTY = HourlyDistributionData(
                hourlyCounts = EMPTY_DAILY_DATA,
                peakHour = -1,
                peakHourCount = 0,
                peakPeriod = null,
                peakPeriodPercent = 0,
                nightCount = 0,
                morningCount = 0,
                afternoonCount = 0,
                eveningCount = 0
            )
        }
    }

    fun calculateHourlyDistribution(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): HourlyDistributionData {
        val hourly = IntArray(24)
        var total = 0
        for (ts in entries) {
            if (ts <= 0L) continue
            val hour = hourOfDay(ts, zoneId)
            if (hour in 0..23) {
                hourly[hour]++
                total++
            }
        }

        if (total == 0) {
            return HourlyDistributionData(
                hourlyCounts = hourly.toList(),
                peakHour = -1,
                peakHourCount = 0,
                peakPeriod = null,
                peakPeriodPercent = 0,
                nightCount = 0,
                morningCount = 0,
                afternoonCount = 0,
                eveningCount = 0
            )
        }

        var peakHour = 0
        var peakHourCount = hourly[0]
        var nightSum = 0
        var morningSum = 0
        var afternoonSum = 0
        var eveningSum = 0

        for (h in 0..23) {
            val count = hourly[h]
            if (count > peakHourCount) {
                peakHourCount = count
                peakHour = h
            }
            when (h) {
                in 0..5 -> nightSum += count
                in 6..11 -> morningSum += count
                in 12..17 -> afternoonSum += count
                else -> eveningSum += count
            }
        }

        var bestPeriod = DayPeriod.NIGHT
        var bestPeriodCount = nightSum
        if (morningSum > bestPeriodCount) {
            bestPeriod = DayPeriod.MORNING
            bestPeriodCount = morningSum
        }
        if (afternoonSum > bestPeriodCount) {
            bestPeriod = DayPeriod.AFTERNOON
            bestPeriodCount = afternoonSum
        }
        if (eveningSum > bestPeriodCount) {
            bestPeriod = DayPeriod.EVENING
            bestPeriodCount = eveningSum
        }

        val peakPeriodPercent = ((bestPeriodCount.toDouble() * 100.0) / total).roundToInt()

        return HourlyDistributionData(
            hourlyCounts = hourly.toList(),
            peakHour = peakHour,
            peakHourCount = peakHourCount,
            peakPeriod = bestPeriod,
            peakPeriodPercent = peakPeriodPercent,
            nightCount = nightSum,
            morningCount = morningSum,
            afternoonCount = afternoonSum,
            eveningCount = eveningSum
        )
    }

    suspend fun calculateHourlyDistributionAsync(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): HourlyDistributionData = withContext(defaultDispatcher) {
        calculateHourlyDistribution(entries, zoneId)
    }

    data class SmokingIntervalData(
        val avgIntervalMinutes: Int,
        val minIntervalMinutes: Int,
        val maxIntervalMinutes: Int,
        val todayAvgMinutes: Int?,
        val totalIntervalsCount: Int
    ) {
        companion object {
            val EMPTY = SmokingIntervalData(
                avgIntervalMinutes = 0,
                minIntervalMinutes = 0,
                maxIntervalMinutes = 0,
                todayAvgMinutes = null,
                totalIntervalsCount = 0
            )
        }
    }

    fun calculateSmokingIntervals(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): SmokingIntervalData {
        val validEntries = entries.filter { it > 0L }
        if (validEntries.isEmpty()) {
            return SmokingIntervalData.EMPTY
        }

        val sorted = validEntries.sorted()
        val today = toLocalDate(nowMillis, zoneId)
        val todayStartMs = startOfDayMillis(today, zoneId)
        val todayEndMs = startOfDayMillis(today.plusDays(1), zoneId)

        var lastTodayEntry: Long? = null
        for (ts in sorted) {
            if (ts in todayStartMs until todayEndMs) {
                lastTodayEntry = ts
            }
        }

        if (sorted.size < 2) {
            val singleTodayAvg = lastTodayEntry?.let { todayTs ->
                val elapsedMs = (nowMillis - todayTs).coerceAtLeast(0L)
                if (elapsedMs in 1L until SLEEP_THRESHOLD_MS) {
                    (elapsedMs / 60_000L).coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
                } else null
            }
            return SmokingIntervalData(
                avgIntervalMinutes = singleTodayAvg ?: 0,
                minIntervalMinutes = singleTodayAvg ?: 0,
                maxIntervalMinutes = singleTodayAvg ?: 0,
                todayAvgMinutes = singleTodayAvg,
                totalIntervalsCount = if (singleTodayAvg != null) 1 else 0
            )
        }

        var allSumMinutes = 0L
        var allCount = 0
        var minMinutes = Int.MAX_VALUE
        var maxMinutes = 0

        var todaySumMinutes = 0L
        var todayCount = 0

        for (i in 0 until sorted.size - 1) {
            val t1 = sorted[i]
            val t2 = sorted[i + 1]
            val diffMs = t2 - t1
            if (diffMs <= 0L) continue

            val isSleepGap = diffMs >= SLEEP_THRESHOLD_MS
            if (!isSleepGap) {
                val minutesLong = (diffMs / 60_000L).coerceAtMost(Int.MAX_VALUE.toLong())
                val minutes = minutesLong.toInt()
                allSumMinutes += minutesLong
                allCount++
                if (minutes < minMinutes) minMinutes = minutes
                if (minutes > maxMinutes) maxMinutes = minutes

                if (t2 in todayStartMs until todayEndMs) {
                    todaySumMinutes += minutesLong
                    todayCount++
                }
            }
        }

        val todayAvg = when {
            todayCount > 0 -> (todaySumMinutes.toDouble() / todayCount).roundToInt()
            lastTodayEntry != null -> {
                val elapsedMs = (nowMillis - lastTodayEntry).coerceAtLeast(0L)
                if (elapsedMs in 1L until SLEEP_THRESHOLD_MS) {
                    (elapsedMs / 60_000L).coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
                } else null
            }
            else -> null
        }

        if (allCount == 0) {
            val totalDiffMs = (sorted.last() - sorted.first()).coerceAtLeast(0L)
            val count = sorted.size - 1
            val fallbackMins = if (count > 0) {
                (totalDiffMs / (count.toLong() * 60_000L))
                    .coerceIn(0L, Int.MAX_VALUE.toLong())
                    .toInt()
            } else 0
            return SmokingIntervalData(
                avgIntervalMinutes = fallbackMins,
                minIntervalMinutes = fallbackMins,
                maxIntervalMinutes = fallbackMins,
                todayAvgMinutes = todayAvg,
                totalIntervalsCount = 0
            )
        }

        val avgMinutes = (allSumMinutes.toDouble() / allCount)
            .roundToLong()
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()

        return SmokingIntervalData(
            avgIntervalMinutes = avgMinutes,
            minIntervalMinutes = if (minMinutes == Int.MAX_VALUE) 0 else minMinutes,
            maxIntervalMinutes = maxMinutes,
            todayAvgMinutes = todayAvg,
            totalIntervalsCount = allCount
        )
    }

    suspend fun calculateSmokingIntervalsAsync(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): SmokingIntervalData = withContext(defaultDispatcher) {
        calculateSmokingIntervals(entries, zoneId, nowMillis)
    }

    private fun sanitizePricePerCigarette(packPrice: Float, packSize: Int): Double {
        if (!packPrice.isFinite() || packPrice <= 0f || packSize <= 0) return 0.0
        val pricePerCig = packPrice.toDouble() / packSize.toDouble()
        return if (pricePerCig.isFinite() && pricePerCig >= 0.0) pricePerCig else 0.0
    }

    companion object {
        const val MINUTES_PER_CIGARETTE = 11L
        const val SLEEP_THRESHOLD_MS = 6 * 60 * 60 * 1000L

        val EMPTY_DAILY_DATA: List<Int> = List(24) { 0 }
        val EMPTY_WEEKLY_DATA: List<Int> = List(7) { 0 }
        val EMPTY_MONTHLY_CHUNKS: List<Int> = List(4) { 0 }
        val EMPTY_YEARLY_DATA: List<Int> = List(12) { 0 }
    }
}
