package com.smokingtracker.domain.statistics

import java.util.concurrent.TimeUnit

enum class PackYearsRiskLevel {
    LOW,
    MODERATE,
    HIGH
}

data class SmokingDuration(
    val totalDays: Int,
    val yearsFloat: Float,
    val yearsInt: Int,
    val monthsInt: Int
)

object HealthRiskCalculator {

    fun calculateSmokingDuration(
        startDateMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): SmokingDuration {
        val diffMs = (nowMillis - startDateMillis).coerceAtLeast(0L)
        val totalDays = TimeUnit.MILLISECONDS.toDays(diffMs).toInt().coerceAtLeast(1)
        val yearsFloat = (totalDays / 365.25f).coerceAtLeast(0.1f)
        val yearsInt = totalDays / 365
        val monthsInt = (totalDays % 365) / 30
        return SmokingDuration(totalDays, yearsFloat, yearsInt, monthsInt)
    }

    fun calculatePackYears(dailyAvg: Int, smokingYears: Float): Float {
        return (dailyAvg.toFloat() / 20f) * smokingYears
    }

    fun calculateRiskLevel(packYears: Float): PackYearsRiskLevel {
        return when {
            packYears < 10f -> PackYearsRiskLevel.LOW
            packYears < 20f -> PackYearsRiskLevel.MODERATE
            else -> PackYearsRiskLevel.HIGH
        }
    }
}
