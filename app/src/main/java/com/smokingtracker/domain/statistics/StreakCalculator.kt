package com.smokingtracker.domain.statistics

import com.smokingtracker.StatisticsData
import com.smokingtracker.daysBetween
import com.smokingtracker.groupCountByDay
import com.smokingtracker.toLocalDate
import java.time.LocalDate
import java.time.ZoneId

object StreakCalculator {

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

    fun calculateLongestStreak(
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
}
