package com.smokingtracker

import java.util.*
import java.util.concurrent.TimeUnit

data class StatisticsData(
    val maxPerDay: Int,
    val minPerDay: Int,
    val avgPerDay: Int,
    val totalCount: Int,
    val trackingSince: Long?,
    val longestStreakDays: Int,
    val totalTrackingDays: Int
)

object StatisticsManager {

    fun calculateStats(entries: List<Long>): StatisticsData {
        if (entries.isEmpty()) {
            return StatisticsData(0, 0, 0, 0, null, 0, 0)
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

        val diffMillis = today.timeInMillis - firstDay.timeInMillis
        val totalTrackingDays = (TimeUnit.MILLISECONDS.toDays(diffMillis) + 1).toInt()

        val avgPerDay = Math.round(totalCount.toDouble() / totalTrackingDays.coerceAtLeast(1)).toInt()

        val longestStreak = calculateLongestStreak(sortedEntries)

        return StatisticsData(
            maxPerDay = maxPerDay,
            minPerDay = minPerDay,
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
            val gapDays = TimeUnit.MILLISECONDS.toDays(entryDays[i + 1] - entryDays[i]).toInt() - 1
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

        val currentGap = TimeUnit.MILLISECONDS.toDays(today - lastEntry).toInt()
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

        return TimeUnit.MILLISECONDS.toDays(today - lastDay).toInt()
    }

    fun getWeeklyCount(entries: List<Long>, date: Calendar): Int {
        val weekStart = date.clone() as Calendar
        weekStart.set(Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
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
        weekStart.set(Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
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
}
