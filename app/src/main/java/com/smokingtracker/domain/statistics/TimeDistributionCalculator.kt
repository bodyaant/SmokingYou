package com.smokingtracker.domain.statistics

import com.smokingtracker.daysBetween
import com.smokingtracker.hourOfDay
import com.smokingtracker.startOfDayMillis
import com.smokingtracker.startOfWeek
import com.smokingtracker.toLocalDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

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
            hourlyCounts = List(24) { 0 },
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

object TimeDistributionCalculator {

    const val SLEEP_THRESHOLD_MS = 6 * 60 * 60 * 1000L

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
}
