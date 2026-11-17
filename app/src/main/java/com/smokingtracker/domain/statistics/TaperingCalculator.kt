package com.smokingtracker.domain.statistics

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

data class TaperingPoint(
    val dayOffset: Int,
    val limit: Int,
    val date: LocalDate
)

data class TaperingStep(
    val stepIndex: Int,
    val dailyLimit: Int,
    val startDayOffset: Int,
    val endDayOffset: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class TaperingPlan(
    val initialLimit: Int,
    val intervalDays: Int,
    val totalDaysToZero: Int,
    val targetDate: LocalDate,
    val formattedTargetDate: String,
    val points: List<TaperingPoint>,
    val steps: List<TaperingStep>
)

object TaperingCalculator {

    const val MIN_DAILY_LIMIT = 1
    const val MAX_DAILY_LIMIT = 60
    const val DEFAULT_DAILY_LIMIT = 10

    const val MIN_INTERVAL_DAYS = 3
    const val MAX_INTERVAL_DAYS = 14
    const val DEFAULT_INTERVAL_DAYS = 7

    val PRESET_LIMITS: List<Int> = listOf(5, 10, 15, 20, 25)
    val PRESET_INTERVALS: List<Int> = listOf(3, 5, 7, 10, 14)

    fun validateDailyLimit(limit: Int): Int {
        return limit.coerceIn(MIN_DAILY_LIMIT, MAX_DAILY_LIMIT)
    }

    fun validateIntervalDays(intervalDays: Int): Int {
        return intervalDays.coerceIn(MIN_INTERVAL_DAYS, MAX_INTERVAL_DAYS)
    }

    fun calculateTotalDaysToZero(initialLimit: Int, intervalDays: Int): Int {
        if (initialLimit <= 0) return 0
        val safeLimit = validateDailyLimit(initialLimit)
        val safeInterval = validateIntervalDays(intervalDays)
        return (safeLimit * safeInterval).coerceAtLeast(safeInterval)
    }

    fun calculateTargetDate(
        totalDaysToZero: Int,
        fromDate: LocalDate = LocalDate.now()
    ): LocalDate {
        val safeDays = totalDaysToZero.coerceAtLeast(0)
        return fromDate.plusDays(safeDays.toLong())
    }

    fun formatTargetDate(
        targetDate: LocalDate,
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM", locale)
        return formatter.format(targetDate)
    }

    fun generateReductionPoints(
        initialLimit: Int,
        intervalDays: Int,
        fromDate: LocalDate = LocalDate.now()
    ): List<TaperingPoint> {
        if (initialLimit <= 0) return emptyList()

        val safeLimit = validateDailyLimit(initialLimit)
        val safeInterval = validateIntervalDays(intervalDays)
        val points = ArrayList<TaperingPoint>(safeLimit + 1)

        var currentDayOffset = 0
        var currentLimit = safeLimit

        points.add(TaperingPoint(currentDayOffset, currentLimit, fromDate))

        while (currentLimit > 0) {
            currentDayOffset += safeInterval
            currentLimit--
            points.add(
                TaperingPoint(
                    dayOffset = currentDayOffset,
                    limit = currentLimit,
                    date = fromDate.plusDays(currentDayOffset.toLong())
                )
            )
        }

        return points
    }

    fun generateScheduleSteps(
        initialLimit: Int,
        intervalDays: Int,
        fromDate: LocalDate = LocalDate.now()
    ): List<TaperingStep> {
        if (initialLimit <= 0) return emptyList()

        val safeLimit = validateDailyLimit(initialLimit)
        val safeInterval = validateIntervalDays(intervalDays)
        val steps = ArrayList<TaperingStep>(safeLimit)

        for (step in 0 until safeLimit) {
            val stepLimit = safeLimit - step
            val startOffset = step * safeInterval
            val endOffset = (step + 1) * safeInterval - 1
            steps.add(
                TaperingStep(
                    stepIndex = step + 1,
                    dailyLimit = stepLimit,
                    startDayOffset = startOffset,
                    endDayOffset = endOffset,
                    startDate = fromDate.plusDays(startOffset.toLong()),
                    endDate = fromDate.plusDays(endOffset.toLong())
                )
            )
        }

        return steps
    }

    fun calculatePlan(
        initialLimit: Int,
        intervalDays: Int,
        locale: Locale = Locale.getDefault(),
        fromDate: LocalDate = LocalDate.now()
    ): TaperingPlan {
        val safeLimit = validateDailyLimit(initialLimit)
        val safeInterval = validateIntervalDays(intervalDays)
        val totalDays = calculateTotalDaysToZero(safeLimit, safeInterval)
        val targetDate = calculateTargetDate(totalDays, fromDate)
        val formattedDate = formatTargetDate(targetDate, locale)
        val points = generateReductionPoints(safeLimit, safeInterval, fromDate)
        val steps = generateScheduleSteps(safeLimit, safeInterval, fromDate)

        return TaperingPlan(
            initialLimit = safeLimit,
            intervalDays = safeInterval,
            totalDaysToZero = totalDays,
            targetDate = targetDate,
            formattedTargetDate = formattedDate,
            points = points,
            steps = steps
        )
    }

    fun isEligibleForCheckin(
        lastCheckinMillis: Long,
        intervalDays: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (lastCheckinMillis <= 0L || lastCheckinMillis > nowMillis) return false
        val safeInterval = validateIntervalDays(intervalDays)
        val diffMs = nowMillis - lastCheckinMillis
        val daysPassed = TimeUnit.MILLISECONDS.toDays(diffMs)
        return daysPassed >= safeInterval.toLong()
    }
}
