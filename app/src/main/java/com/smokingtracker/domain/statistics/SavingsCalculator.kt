package com.smokingtracker.domain.statistics

import com.smokingtracker.groupCountByDay
import com.smokingtracker.toLocalDate
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.roundToLong

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

data class BaselinePreviewData(
    val totalDays: Int,
    val yearsSmokedFloat: Float,
    val yearsInt: Int,
    val monthsInt: Int,
    val packYears: Float,
    val riskLevel: PackYearsRiskLevel,
    val estimatedPastCigarettes: Long,
    val estimatedPastCost: Double
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

object SavingsCalculator {

    const val MINUTES_PER_CIGARETTE = 11L

    fun sanitizePricePerCigarette(packPrice: Float, packSize: Int): Double {
        if (!packPrice.isFinite() || packPrice <= 0f || packSize <= 0) return 0.0
        val pricePerCig = packPrice.toDouble() / packSize.toDouble()
        return if (pricePerCig.isFinite() && pricePerCig >= 0.0) pricePerCig else 0.0
    }

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

        val currentStreakDays = StreakCalculator.currentSmokeFreeStreakDays(
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

        val packYears = HealthRiskCalculator.calculatePackYears(dailyAvg, smokingYears)
        val riskLevel = HealthRiskCalculator.calculateRiskLevel(packYears)

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

    fun calculateBaselinePreview(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): BaselinePreviewData {
        val duration = HealthRiskCalculator.calculateSmokingDuration(startDate, nowMillis)
        val safeDailyAvg = dailyAvg.coerceAtLeast(0)
        val packYears = HealthRiskCalculator.calculatePackYears(safeDailyAvg, duration.yearsFloat)
        val riskLevel = HealthRiskCalculator.calculateRiskLevel(packYears)
        val pricePerCig = sanitizePricePerCigarette(packPrice, packSize)
        val pastCigarettes = (duration.totalDays.toLong() * safeDailyAvg.toLong()).coerceAtLeast(0L)
        val pastCost = pastCigarettes.toDouble() * pricePerCig

        return BaselinePreviewData(
            totalDays = duration.totalDays,
            yearsSmokedFloat = duration.yearsFloat,
            yearsInt = duration.yearsInt,
            monthsInt = duration.monthsInt,
            packYears = packYears,
            riskLevel = riskLevel,
            estimatedPastCigarettes = pastCigarettes,
            estimatedPastCost = pastCost
        )
    }
}
