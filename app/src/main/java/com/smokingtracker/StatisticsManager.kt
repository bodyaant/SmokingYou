package com.smokingtracker

import java.util.*
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

        val dailyCounts = mutableMapOf<String, Int>()

        entries.forEach { timestamp ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = timestamp
            val dayKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            dailyCounts[dayKey] = (dailyCounts[dayKey] ?: 0) + 1
        }

        val maxPerDay = dailyCounts.values.maxOrNull() ?: 0
        val minPerDay = dailyCounts.values.minOrNull() ?: 0

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val firstDay = Calendar.getInstance().apply {
            timeInMillis = trackingSince
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val totalTrackingDays = (daysBetween(firstDay.timeInMillis, today.timeInMillis) + 1).toInt()

        val avgPerDay = totalCount.toFloat() / totalTrackingDays.coerceAtLeast(1)

        val longestStreak = calculateLongestStreak(sortedEntries)

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

    private fun calculateLongestStreak(sortedEntries: List<Long>): Int {
        if (sortedEntries.isEmpty()) return 0

        val entryDays = sortedEntries.map { ts ->
            Calendar.getInstance().apply {
                timeInMillis = ts
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.distinct().sorted()

        var maxStreak = 0

        for (i in 0 until entryDays.size - 1) {
            val gapDays = daysBetween(entryDays[i], entryDays[i + 1]).toInt() - 1
            if (gapDays > maxStreak) {
                maxStreak = gapDays
            }
        }

        val lastEntry = entryDays.last()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val currentGap = daysBetween(lastEntry, today).toInt()
        if (currentGap > maxStreak) {
            maxStreak = currentGap
        }

        return maxStreak
    }

    fun currentSmokeFreeStreakDays(entries: List<Long>): Int {
        if (entries.isEmpty()) return 0
        val lastEntryMs = entries.maxOrNull() ?: return 0

        val lastDay = Calendar.getInstance().apply {
            timeInMillis = lastEntryMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return daysBetween(lastDay, today).toInt()
    }

    fun getWeeklyCount(entries: List<Long>, date: Calendar): Int {
        val weekStart = date.clone() as Calendar
        weekStart.firstDayOfWeek = Calendar.MONDAY
        while (weekStart.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            weekStart.add(Calendar.DAY_OF_YEAR, -1)
        }
        weekStart.set(Calendar.HOUR_OF_DAY, 0)
        weekStart.set(Calendar.MINUTE, 0)
        weekStart.set(Calendar.SECOND, 0)
        weekStart.set(Calendar.MILLISECOND, 0)
        val weekEnd = weekStart.clone() as Calendar
        weekEnd.add(Calendar.DAY_OF_YEAR, 7)
        return entries.count { it >= weekStart.timeInMillis && it < weekEnd.timeInMillis }
    }

    fun getMonthlyCount(entries: List<Long>, date: Calendar): Int {
        val monthStart = date.clone() as Calendar
        monthStart.set(Calendar.DAY_OF_MONTH, 1)
        monthStart.set(Calendar.HOUR_OF_DAY, 0)
        monthStart.set(Calendar.MINUTE, 0)
        monthStart.set(Calendar.SECOND, 0)
        monthStart.set(Calendar.MILLISECOND, 0)
        val monthEnd = monthStart.clone() as Calendar
        monthEnd.add(Calendar.MONTH, 1)
        return entries.count { it >= monthStart.timeInMillis && it < monthEnd.timeInMillis }
    }

    fun generateDailyData(entries: List<Long>, date: Calendar): List<Int> {
        val dayStart = date.clone() as Calendar
        dayStart.set(Calendar.HOUR_OF_DAY, 0)
        dayStart.set(Calendar.MINUTE, 0)
        dayStart.set(Calendar.SECOND, 0)
        dayStart.set(Calendar.MILLISECOND, 0)
        val dayStartMillis = dayStart.timeInMillis
        val dayEnd = dayStart.clone() as Calendar
        dayEnd.add(Calendar.DAY_OF_YEAR, 1)
        val dayEndMillis = dayEnd.timeInMillis
        val dayEntries = entries.filter { it >= dayStartMillis && it < dayEndMillis }
        val hourlyCounts = IntArray(24) { 0 }
        val cal = Calendar.getInstance()
        dayEntries.forEach { time ->
            cal.timeInMillis = time
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            if (hour in 0..23) hourlyCounts[hour]++
        }
        return hourlyCounts.toList()
    }

    fun generateWeeklyData(entries: List<Long>, date: Calendar): List<Int> {
        val weekStart = date.clone() as Calendar
        weekStart.firstDayOfWeek = Calendar.MONDAY
        while (weekStart.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            weekStart.add(Calendar.DAY_OF_YEAR, -1)
        }
        weekStart.set(Calendar.HOUR_OF_DAY, 0)
        weekStart.set(Calendar.MINUTE, 0)
        weekStart.set(Calendar.SECOND, 0)
        weekStart.set(Calendar.MILLISECOND, 0)
        val weekStartMillis = weekStart.timeInMillis
        val weekEnd = weekStart.clone() as Calendar
        weekEnd.add(Calendar.DAY_OF_YEAR, 7)
        val weekEndMillis = weekEnd.timeInMillis
        val weekEntries = entries.filter { it >= weekStartMillis && it < weekEndMillis }
        val dailyCounts = IntArray(7) { 0 }
        val cal = Calendar.getInstance()
        weekEntries.forEach { time ->
            cal.timeInMillis = time
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val diffDays = ((cal.timeInMillis - weekStartMillis) / (24 * 60 * 60 * 1000L)).toInt().coerceIn(0, 6)
            dailyCounts[diffDays]++
        }
        return dailyCounts.toList()
    }

    fun generateMonthlyData(entries: List<Long>, date: Calendar): List<Int> {
        val monthStart = date.clone() as Calendar
        monthStart.set(Calendar.DAY_OF_MONTH, 1)
        monthStart.set(Calendar.HOUR_OF_DAY, 0)
        monthStart.set(Calendar.MINUTE, 0)
        monthStart.set(Calendar.SECOND, 0)
        monthStart.set(Calendar.MILLISECOND, 0)
        val monthStartMillis = monthStart.timeInMillis
        val monthEnd = monthStart.clone() as Calendar
        monthEnd.add(Calendar.MONTH, 1)
        val monthEndMillis = monthEnd.timeInMillis
        val monthEntries = entries.filter { it >= monthStartMillis && it < monthEndMillis }
        val daysInMonth = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dailyCounts = IntArray(daysInMonth) { 0 }
        val cal = Calendar.getInstance()
        monthEntries.forEach { time ->
            cal.timeInMillis = time
            val dayIndex = cal.get(Calendar.DAY_OF_MONTH) - 1
            if (dayIndex in 0 until daysInMonth) dailyCounts[dayIndex]++
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
        val monthStart = date.clone() as Calendar
        monthStart.set(Calendar.DAY_OF_MONTH, 1)
        monthStart.set(Calendar.HOUR_OF_DAY, 0)
        monthStart.set(Calendar.MINUTE, 0)
        monthStart.set(Calendar.SECOND, 0)
        monthStart.set(Calendar.MILLISECOND, 0)
        val monthStartMillis = monthStart.timeInMillis
        val monthEnd = monthStart.clone() as Calendar
        monthEnd.add(Calendar.MONTH, 1)
        val monthEndMillis = monthEnd.timeInMillis
        val monthEntries = entries.filter { it >= monthStartMillis && it < monthEndMillis }
        val daysInMonth = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dailyCounts = IntArray(daysInMonth) { 0 }
        val cal = Calendar.getInstance()
        monthEntries.forEach { time ->
            cal.timeInMillis = time
            val dayIndex = cal.get(Calendar.DAY_OF_MONTH) - 1
            if (dayIndex in 0 until daysInMonth) dailyCounts[dayIndex]++
        }
        return dailyCounts.toList()
    }

    fun generateYearlyData(entries: List<Long>, date: Calendar): List<Int> {
        val yearStart = date.clone() as Calendar
        yearStart.set(Calendar.DAY_OF_YEAR, 1)
        yearStart.set(Calendar.HOUR_OF_DAY, 0)
        yearStart.set(Calendar.MINUTE, 0)
        yearStart.set(Calendar.SECOND, 0)
        yearStart.set(Calendar.MILLISECOND, 0)
        val yearStartMillis = yearStart.timeInMillis
        val yearEnd = yearStart.clone() as Calendar
        yearEnd.add(Calendar.YEAR, 1)
        val yearEndMillis = yearEnd.timeInMillis
        val yearEntries = entries.filter { it >= yearStartMillis && it < yearEndMillis }
        val monthlyCounts = IntArray(12) { 0 }
        val cal = Calendar.getInstance()
        yearEntries.forEach { time ->
            cal.timeInMillis = time
            val monthIndex = cal.get(Calendar.MONTH)
            if (monthIndex in 0..11) monthlyCounts[monthIndex]++
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
            val cal = Calendar.getInstance()
            val countsByDay = mutableMapOf<Long, Int>()
            entries.forEach { timestamp ->
                cal.timeInMillis = timestamp
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val dayKey = cal.timeInMillis
                countsByDay[dayKey] = (countsByDay[dayKey] ?: 0) + 1
            }

            cal.timeInMillis = firstEntryTime
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            var currentDay = cal.timeInMillis

            val todayCal = Calendar.getInstance()
            todayCal.set(Calendar.HOUR_OF_DAY, 0)
            todayCal.set(Calendar.MINUTE, 0)
            todayCal.set(Calendar.SECOND, 0)
            todayCal.set(Calendar.MILLISECOND, 0)
            val todayMidnight = todayCal.timeInMillis

            while (currentDay <= todayMidnight) {
                val actual = countsByDay[currentDay] ?: 0
                if (actual < dailyAvg) {
                    avoidedSum += (dailyAvg - actual)
                }
                cal.timeInMillis = currentDay
                cal.add(Calendar.DAY_OF_YEAR, 1)
                currentDay = cal.timeInMillis
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
        val cal = referenceDate.clone() as Calendar
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        val thisWeekStart = cal.timeInMillis

        val thisWeekEndCal = cal.clone() as Calendar
        thisWeekEndCal.add(Calendar.DAY_OF_YEAR, 7)
        val thisWeekEnd = thisWeekEndCal.timeInMillis

        val prevWeekStartCal = cal.clone() as Calendar
        prevWeekStartCal.add(Calendar.DAY_OF_YEAR, -7)
        val prevWeekStart = prevWeekStartCal.timeInMillis

        val prevWeekEnd = thisWeekStart

        val thisWeekEntries = entries.filter { it >= thisWeekStart && it < thisWeekEnd }
        val prevWeekEntries = entries.filter { it >= prevWeekStart && it < prevWeekEnd }

        val thisWeekCount = thisWeekEntries.size
        val lastWeekCount = prevWeekEntries.size
        val diff = thisWeekCount - lastWeekCount

        val nowMs = now.timeInMillis
        val isCurrentWeek = nowMs in thisWeekStart until thisWeekEnd
        val daysElapsedThisWeek = if (isCurrentWeek) {
            ((now.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1).coerceIn(1, 7)
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
        val hourly = IntArray(24) { 0 }
        val cal = Calendar.getInstance()
        entries.forEach { ts ->
            cal.timeInMillis = ts
            val hour = cal.get(Calendar.HOUR_OF_DAY)
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
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStartMs = todayCal.timeInMillis
        val todayEndMs = todayStartMs + 24L * 60 * 60 * 1000L

        val allIntervals = mutableListOf<Int>()
        val todayIntervals = mutableListOf<Int>()

        for (i in 0 until sorted.size - 1) {
            val t1 = sorted[i]
            val t2 = sorted[i + 1]
            val diffMs = t2 - t1
            if (diffMs <= 0) continue

            cal1.timeInMillis = t1
            cal2.timeInMillis = t2

            val sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

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

