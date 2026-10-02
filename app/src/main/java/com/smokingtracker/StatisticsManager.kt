package com.smokingtracker

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class StatisticsData(
    val maxPerDay: Int,
    val minPerDay: Int,
    val avgPerDay: Float,
    val totalCount: Int,
    val trackingSince: Long?,
    val longestStreakDays: Int,
    val totalTrackingDays: Int
)

class StatisticsManager {

    fun calculateStats(entries: List<Long>): StatisticsData {
        if (entries.isEmpty()) {
            return StatisticsData(0, 0, 0f, 0, null, 0, 0)
        }

        val sortedEntries = entries.sorted()
        val totalCount = entries.size
        val trackingSince = sortedEntries.first()

        val zoneId = ZoneId.systemDefault()
        val dailyCounts = groupCountByDay(entries, zoneId)

        val maxPerDay = dailyCounts.values.maxOrNull() ?: 0
        val minPerDay = dailyCounts.values.minOrNull() ?: 0

        val firstDate = toLocalDate(trackingSince, zoneId)
        val today = LocalDate.now(zoneId)

        val totalTrackingDays = (daysBetween(firstDate, today) + 1).toInt()

        val avgPerDay = totalCount.toFloat() / totalTrackingDays.coerceAtLeast(1)

        val longestStreak = calculateLongestStreak(sortedEntries, zoneId)

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

    private fun calculateLongestStreak(sortedEntries: List<Long>, zoneId: ZoneId = ZoneId.systemDefault()): Int {
        if (sortedEntries.isEmpty()) return 0

        val entryDates = sortedEntries.map { toLocalDate(it, zoneId) }.distinct().sorted()

        var maxStreak = 0

        for (i in 0 until entryDates.size - 1) {
            val gapDays = daysBetween(entryDates[i], entryDates[i + 1]).toInt() - 1
            if (gapDays > maxStreak) {
                maxStreak = gapDays
            }
        }

        val lastEntry = entryDates.last()
        val today = LocalDate.now(zoneId)

        val currentGap = daysBetween(lastEntry, today).toInt()
        if (currentGap > maxStreak) {
            maxStreak = currentGap
        }

        return maxStreak
    }

    fun currentSmokeFreeStreakDays(entries: List<Long>, zoneId: ZoneId = ZoneId.systemDefault()): Int {
        if (entries.isEmpty()) return 0
        val lastEntryMs = entries.maxOrNull() ?: return 0
        val lastDay = toLocalDate(lastEntryMs, zoneId)
        val today = LocalDate.now(zoneId)
        return daysBetween(lastDay, today).toInt()
    }

    fun getWeeklyCount(entries: List<Long>, date: Calendar): Int {
        val zoneId = date.timeZone.toZoneId()
        val localDate = Instant.ofEpochMilli(date.timeInMillis).atZone(zoneId).toLocalDate()
        val monday = localDate.with(DayOfWeek.MONDAY)
        val weekStartMillis = monday.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val weekEndMillis = monday.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return entries.count { it >= weekStartMillis && it < weekEndMillis }
    }

    fun getMonthlyCount(entries: List<Long>, date: Calendar): Int {
        val zoneId = date.timeZone.toZoneId()
        val localDate = Instant.ofEpochMilli(date.timeInMillis).atZone(zoneId).toLocalDate()
        val monthStart = localDate.withDayOfMonth(1)
        val monthStartMillis = monthStart.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthEndMillis = monthStart.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return entries.count { it >= monthStartMillis && it < monthEndMillis }
    }

    fun generateDailyData(entries: List<Long>, date: Calendar): List<Int> {
        val zoneId = date.timeZone.toZoneId()
        val localDate = Instant.ofEpochMilli(date.timeInMillis).atZone(zoneId).toLocalDate()
        val dayStartMillis = localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val dayEndMillis = localDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val hourlyCounts = IntArray(24)
        entries.forEach { time ->
            if (time >= dayStartMillis && time < dayEndMillis) {
                val hour = hourOfDay(time, zoneId)
                if (hour in 0..23) hourlyCounts[hour]++
            }
        }
        return hourlyCounts.toList()
    }

    fun generateWeeklyData(entries: List<Long>, date: Calendar): List<Int> {
        val zoneId = date.timeZone.toZoneId()
        val localDate = Instant.ofEpochMilli(date.timeInMillis).atZone(zoneId).toLocalDate()
        val monday = localDate.with(DayOfWeek.MONDAY)
        val weekStartMillis = monday.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val weekEndMillis = monday.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val dailyCounts = IntArray(7)
        entries.forEach { time ->
            if (time >= weekStartMillis && time < weekEndMillis) {
                val entryDate = toLocalDate(time, zoneId)
                val diffDays = daysBetween(monday, entryDate).toInt().coerceIn(0, 6)
                dailyCounts[diffDays]++
            }
        }
        return dailyCounts.toList()
    }

    fun generateMonthlyData(entries: List<Long>, date: Calendar): List<Int> {
        val zoneId = date.timeZone.toZoneId()
        val localDate = Instant.ofEpochMilli(date.timeInMillis).atZone(zoneId).toLocalDate()
        val monthStart = localDate.withDayOfMonth(1)
        val monthStartMillis = monthStart.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthEndMillis = monthStart.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val daysInMonth = monthStart.lengthOfMonth()
        val dailyCounts = IntArray(daysInMonth)
        entries.forEach { time ->
            if (time >= monthStartMillis && time < monthEndMillis) {
                val dayIndex = toLocalDate(time, zoneId).dayOfMonth - 1
                if (dayIndex in 0 until daysInMonth) dailyCounts[dayIndex]++
            }
        }
        val chunkSize = kotlin.math.ceil(daysInMonth / 4.0).toInt()
        val weeklyChunks = mutableListOf<Int>()
        for (i in 0 until 4) {
            var sum = 0
            for (j in 0 until chunkSize) {
                val index = i * chunkSize + j
                if (index < daysInMonth) sum += dailyCounts[index]
            }
            weeklyChunks.add(sum)
        }
        return weeklyChunks
    }

    fun generateMonthlyDailyData(entries: List<Long>, date: Calendar): List<Int> {
        val zoneId = date.timeZone.toZoneId()
        val localDate = Instant.ofEpochMilli(date.timeInMillis).atZone(zoneId).toLocalDate()
        val monthStart = localDate.withDayOfMonth(1)
        val monthStartMillis = monthStart.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthEndMillis = monthStart.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val daysInMonth = monthStart.lengthOfMonth()
        val dailyCounts = IntArray(daysInMonth)
        entries.forEach { time ->
            if (time >= monthStartMillis && time < monthEndMillis) {
                val dayIndex = toLocalDate(time, zoneId).dayOfMonth - 1
                if (dayIndex in 0 until daysInMonth) dailyCounts[dayIndex]++
            }
        }
        return dailyCounts.toList()
    }

    fun generateYearlyData(entries: List<Long>, date: Calendar): List<Int> {
        val zoneId = date.timeZone.toZoneId()
        val localDate = Instant.ofEpochMilli(date.timeInMillis).atZone(zoneId).toLocalDate()
        val yearStart = localDate.withDayOfYear(1)
        val yearStartMillis = yearStart.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val yearEndMillis = yearStart.plusYears(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthlyCounts = IntArray(12)
        entries.forEach { time ->
            if (time >= yearStartMillis && time < yearEndMillis) {
                val monthIndex = toLocalDate(time, zoneId).monthValue - 1
                if (monthIndex in 0..11) monthlyCounts[monthIndex]++
            }
        }
        return monthlyCounts.toList()
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
        val totalAvoidedMoney: Double
    )

    data class HistoricalBaselineStats(
        val totalCigarettes: Int,
        val totalMoneySpent: Double,
        val totalDays: Int,
        val estimatedTriggerCounts: Map<String, Int>
    )

    fun calculateBaselineAnalytics(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        entries: List<Long>
    ): SmokingBaselineAnalytics? {
        if (startDate <= 0L || dailyAvg <= 0) return null

        val now = System.currentTimeMillis()
        if (startDate >= now) return null

        val diffMs = now - startDate
        val totalDaysUntilNow = TimeUnit.MILLISECONDS.toDays(diffMs).coerceAtLeast(1)
        val smokingYears = (totalDaysUntilNow / 365.25f).coerceAtLeast(0.1f)
        val totalYearsInt = (totalDaysUntilNow / 365).toInt()
        val totalMonthsInt = ((totalDaysUntilNow % 365) / 30).toInt()

        val packYears = (dailyAvg.toFloat() / 20f) * smokingYears
        val riskLevel = when {
            packYears < 10f -> PackYearsRiskLevel.LOW
            packYears < 20f -> PackYearsRiskLevel.MODERATE
            else -> PackYearsRiskLevel.HIGH
        }

        val pricePerCig = if (packSize > 0) (packPrice / packSize).toDouble() else 0.0

        val firstEntryTime = entries.minOrNull() ?: now
        val priorDurationMs = (firstEntryTime - startDate).coerceAtLeast(0L)
        val priorDays = TimeUnit.MILLISECONDS.toDays(priorDurationMs).coerceAtLeast(0)
        val pastCigarettes = priorDays * dailyAvg
        val pastMoneySpent = pastCigarettes * pricePerCig

        val sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7)
        val last7DaysCount = entries.count { it in sevenDaysAgo..now }
        val currentAvg7Days = last7DaysCount / 7.0f

        val reductionDiff = dailyAvg - currentAvg7Days
        val reductionPercentage = if (dailyAvg > 0) {
            ((reductionDiff / dailyAvg.toFloat()) * 100f).roundToInt().coerceIn(-100, 100)
        } else 0

        var avoidedSum = 0
        if (entries.isNotEmpty()) {
            val zoneId = ZoneId.systemDefault()
            val countsByDay = groupCountByDay(entries, zoneId)
            var currentDay = toLocalDate(firstEntryTime, zoneId)
            val today = LocalDate.now(zoneId)

            while (!currentDay.isAfter(today)) {
                val actual = countsByDay[currentDay] ?: 0
                if (actual < dailyAvg) {
                    avoidedSum += (dailyAvg - actual)
                }
                currentDay = currentDay.plusDays(1)
            }
        }
        val totalAvoidedCigarettes = avoidedSum
        val totalAvoidedMoney = totalAvoidedCigarettes * pricePerCig

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
            totalAvoidedMoney = totalAvoidedMoney
        )
    }

    fun calculateHistoricalBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        rankedTriggers: List<String>
    ): HistoricalBaselineStats {
        if (startDate <= 0L || dailyAvg <= 0) {
            return HistoricalBaselineStats(0, 0.0, 0, emptyMap())
        }

        val now = System.currentTimeMillis()
        if (startDate >= now) {
            return HistoricalBaselineStats(0, 0.0, 0, emptyMap())
        }

        val diffMs = now - startDate
        val totalDays = (TimeUnit.MILLISECONDS.toDays(diffMs)).toInt().coerceAtLeast(1)
        val totalCigarettes = totalDays * dailyAvg
        val pricePerCigarette = if (packSize > 0) packPrice / packSize.toDouble() else 0.0
        val totalMoneySpent = totalCigarettes * pricePerCigarette

        val triggerCounts = mutableMapOf<String, Int>()
        if (rankedTriggers.isNotEmpty()) {
            val n = rankedTriggers.size
            val totalWeight = (n * (n + 1)) / 2
            var remaining = totalCigarettes

            rankedTriggers.forEachIndexed { index, triggerKey ->
                val weight = n - index
                val count = Math.round((weight.toDouble() / totalWeight) * totalCigarettes).toInt()
                triggerCounts[triggerKey] = count
                remaining -= count
            }
            if (remaining != 0 && rankedTriggers.isNotEmpty()) {
                val firstKey = rankedTriggers.first()
                triggerCounts[firstKey] = (triggerCounts[firstKey] ?: 0) + remaining
            }
        }

        return HistoricalBaselineStats(
            totalCigarettes = totalCigarettes,
            totalMoneySpent = totalMoneySpent,
            totalDays = totalDays,
            estimatedTriggerCounts = triggerCounts
        )
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
    )

    fun calculateWeeklyComparison(
        entries: List<Long>,
        referenceDate: Calendar = Calendar.getInstance(),
        now: Calendar = Calendar.getInstance()
    ): WeeklyComparisonData {
        val zoneId = referenceDate.timeZone.toZoneId()
        val refDate = Instant.ofEpochMilli(referenceDate.timeInMillis).atZone(zoneId).toLocalDate()
        val thisWeekMonday = refDate.with(DayOfWeek.MONDAY)
        val thisWeekStart = thisWeekMonday.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val thisWeekEnd = thisWeekMonday.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val prevWeekStart = thisWeekMonday.minusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val prevWeekEnd = thisWeekStart

        val thisWeekEntries = entries.filter { it >= thisWeekStart && it < thisWeekEnd }
        val prevWeekEntries = entries.filter { it >= prevWeekStart && it < prevWeekEnd }

        val thisWeekCount = thisWeekEntries.size
        val lastWeekCount = prevWeekEntries.size
        val diff = thisWeekCount - lastWeekCount

        val nowMs = now.timeInMillis
        val isCurrentWeek = nowMs in thisWeekStart until thisWeekEnd
        val daysElapsedThisWeek = if (isCurrentWeek) {
            val nowZoneId = now.timeZone.toZoneId()
            val nowLocalDate = Instant.ofEpochMilli(nowMs).atZone(nowZoneId).toLocalDate()
            nowLocalDate.dayOfWeek.value.coerceIn(1, 7)
        } else {
            7
        }

        val thisWeekDailyAvg = if (daysElapsedThisWeek > 0) {
            thisWeekCount.toFloat() / daysElapsedThisWeek
        } else {
            0f
        }
        val lastWeekDailyAvg = lastWeekCount.toFloat() / 7f
        val diffDailyAvg = thisWeekDailyAvg - lastWeekDailyAvg

        val percentChange = if (lastWeekDailyAvg == 0f) {
            if (thisWeekDailyAvg > 0f) 100 else 0
        } else {
            Math.round(Math.abs(diffDailyAvg.toDouble()) * 100.0 / lastWeekDailyAvg).toInt()
        }

        val trend = when {
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

    data class HourlyDistributionData(
        val hourlyCounts: List<Int>,
        val peakHour: Int,
        val peakHourCount: Int,
        val peakPeriodNameResId: Int,
        val peakPeriodPercent: Int
    )

    fun calculateHourlyDistribution(entries: List<Long>): HourlyDistributionData {
        val hourly = IntArray(24)
        val zoneId = ZoneId.systemDefault()
        entries.forEach { ts ->
            val hour = hourOfDay(ts, zoneId)
            if (hour in 0..23) hourly[hour]++
        }

        val total = entries.size
        val peakHour = hourly.indices.maxByOrNull { hourly[it] } ?: 0
        val peakHourCount = hourly[peakHour]

        val nightSum = hourly.slice(0..5).sum()
        val morningSum = hourly.slice(6..11).sum()
        val afternoonSum = hourly.slice(12..17).sum()
        val eveningSum = hourly.slice(18..23).sum()

        val periods = listOf(
            nightSum to R.string.peak_period_night,
            morningSum to R.string.peak_period_morning,
            afternoonSum to R.string.peak_period_afternoon,
            eveningSum to R.string.peak_period_evening
        )

        val bestPeriod = periods.maxByOrNull { it.first } ?: (0 to R.string.peak_period_afternoon)
        val peakPeriodPercent = if (total > 0) Math.round((bestPeriod.first.toDouble() * 100.0) / total).toInt() else 0

        return HourlyDistributionData(
            hourlyCounts = hourly.toList(),
            peakHour = peakHour,
            peakHourCount = peakHourCount,
            peakPeriodNameResId = bestPeriod.second,
            peakPeriodPercent = peakPeriodPercent
        )
    }

    data class SmokingIntervalData(
        val avgIntervalMinutes: Int,
        val minIntervalMinutes: Int,
        val maxIntervalMinutes: Int,
        val todayAvgMinutes: Int?,
        val totalIntervalsCount: Int
    )

    fun calculateSmokingIntervals(entries: List<Long>): SmokingIntervalData {
        if (entries.size < 2) {
            return SmokingIntervalData(0, 0, 0, null, 0)
        }

        val sorted = entries.sorted()
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val todayStartMs = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val todayEndMs = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val allIntervals = mutableListOf<Int>()
        val todayIntervals = mutableListOf<Int>()

        for (i in 0 until sorted.size - 1) {
            val t1 = sorted[i]
            val t2 = sorted[i + 1]
            val diffMs = t2 - t1
            if (diffMs <= 0) continue

            val sameDay = toLocalDate(t1, zoneId) == toLocalDate(t2, zoneId)
            val isOvernight = !sameDay && diffMs > 4 * 60 * 60 * 1000L

            if (!isOvernight) {
                val minutes = (diffMs / (60 * 1000L)).toInt()
                allIntervals.add(minutes)

                if (t2 in todayStartMs until todayEndMs) {
                    todayIntervals.add(minutes)
                }
            }
        }

        if (allIntervals.isEmpty()) {
            val totalDiffMs = sorted.last() - sorted.first()
            val count = sorted.size - 1
            val fallbackMins = if (count > 0) (totalDiffMs / (count * 60 * 1000L)).toInt() else 0
            return SmokingIntervalData(
                avgIntervalMinutes = fallbackMins,
                minIntervalMinutes = fallbackMins,
                maxIntervalMinutes = fallbackMins,
                todayAvgMinutes = null,
                totalIntervalsCount = 0
            )
        }

        val avgMinutes = (allIntervals.sum().toDouble() / allIntervals.size).roundToInt()
        val minMinutes = allIntervals.minOrNull() ?: 0
        val maxMinutes = allIntervals.maxOrNull() ?: 0
        val todayAvg = if (todayIntervals.isNotEmpty()) {
            (todayIntervals.sum().toDouble() / todayIntervals.size).roundToInt()
        } else null

        return SmokingIntervalData(
            avgIntervalMinutes = avgMinutes,
            minIntervalMinutes = minMinutes,
            maxIntervalMinutes = maxMinutes,
            todayAvgMinutes = todayAvg,
            totalIntervalsCount = allIntervals.size
        )
    }
}

