package com.smokingtracker

import com.smokingtracker.domain.statistics.ChartDatasetGenerator
import com.smokingtracker.domain.statistics.ComparisonTrend
import com.smokingtracker.domain.statistics.DayPeriod
import com.smokingtracker.domain.statistics.HistoricalBaselineStats
import com.smokingtracker.domain.statistics.HourlyDistributionData
import com.smokingtracker.domain.statistics.PackYearsRiskLevel
import com.smokingtracker.domain.statistics.SavingsCalculator
import com.smokingtracker.domain.statistics.SavingsData
import com.smokingtracker.domain.statistics.SmokingBaselineAnalytics
import com.smokingtracker.domain.statistics.SmokingIntervalData
import com.smokingtracker.domain.statistics.StreakCalculator
import com.smokingtracker.domain.statistics.TimeDistributionCalculator
import com.smokingtracker.domain.statistics.WeeklyComparisonData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

typealias PackYearsRiskLevel = com.smokingtracker.domain.statistics.PackYearsRiskLevel
typealias SmokingBaselineAnalytics = com.smokingtracker.domain.statistics.SmokingBaselineAnalytics
typealias HistoricalBaselineStats = com.smokingtracker.domain.statistics.HistoricalBaselineStats
typealias SavingsData = com.smokingtracker.domain.statistics.SavingsData
typealias ComparisonTrend = com.smokingtracker.domain.statistics.ComparisonTrend
typealias WeeklyComparisonData = com.smokingtracker.domain.statistics.WeeklyComparisonData
typealias DayPeriod = com.smokingtracker.domain.statistics.DayPeriod
typealias HourlyDistributionData = com.smokingtracker.domain.statistics.HourlyDistributionData
typealias SmokingIntervalData = com.smokingtracker.domain.statistics.SmokingIntervalData

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
    typealias PackYearsRiskLevel = com.smokingtracker.domain.statistics.PackYearsRiskLevel
    typealias SmokingBaselineAnalytics = com.smokingtracker.domain.statistics.SmokingBaselineAnalytics
    typealias HistoricalBaselineStats = com.smokingtracker.domain.statistics.HistoricalBaselineStats
    typealias SavingsData = com.smokingtracker.domain.statistics.SavingsData
    typealias ComparisonTrend = com.smokingtracker.domain.statistics.ComparisonTrend
    typealias WeeklyComparisonData = com.smokingtracker.domain.statistics.WeeklyComparisonData
    typealias DayPeriod = com.smokingtracker.domain.statistics.DayPeriod
    typealias HourlyDistributionData = com.smokingtracker.domain.statistics.HourlyDistributionData
    typealias SmokingIntervalData = com.smokingtracker.domain.statistics.SmokingIntervalData

    fun calculateStats(
        entries: List<Long>,
        trackingStartMillis: Long? = null,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): StatisticsData = StreakCalculator.calculateStats(entries, trackingStartMillis, zoneId, nowMillis)

    suspend fun calculateStatsAsync(
        entries: List<Long>,
        trackingStartMillis: Long? = null,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): StatisticsData = withContext(defaultDispatcher) {
        calculateStats(entries, trackingStartMillis, zoneId, nowMillis)
    }

    fun currentSmokeFreeStreakDays(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        trackingStartMillis: Long? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): Int = StreakCalculator.currentSmokeFreeStreakDays(entries, zoneId, trackingStartMillis, nowMillis)

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
    ): Int = ChartDatasetGenerator.getWeeklyCount(entries, date, zoneId, firstDayOfWeek)

    fun getWeeklyCount(
        entries: List<Long>,
        date: Calendar,
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): Int = ChartDatasetGenerator.getWeeklyCount(entries, date, firstDayOfWeek)

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
    ): Int = ChartDatasetGenerator.getMonthlyCount(entries, date, zoneId)

    fun getMonthlyCount(entries: List<Long>, date: Calendar): Int =
        ChartDatasetGenerator.getMonthlyCount(entries, date)

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
    ): List<Int> = ChartDatasetGenerator.generateDailyData(entries, date, zoneId)

    fun generateDailyData(entries: List<Long>, date: Calendar): List<Int> =
        ChartDatasetGenerator.generateDailyData(entries, date)

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
    ): List<Int> = ChartDatasetGenerator.generateWeeklyData(entries, date, zoneId, firstDayOfWeek)

    fun generateWeeklyData(
        entries: List<Long>,
        date: Calendar,
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): List<Int> = ChartDatasetGenerator.generateWeeklyData(entries, date, firstDayOfWeek)

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
    ): List<Int> = ChartDatasetGenerator.generateMonthlyData(entries, date, zoneId)

    fun generateMonthlyData(entries: List<Long>, date: Calendar): List<Int> =
        ChartDatasetGenerator.generateMonthlyData(entries, date)

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
    ): List<Int> = ChartDatasetGenerator.generateMonthlyDailyData(entries, date, zoneId)

    fun generateMonthlyDailyData(entries: List<Long>, date: Calendar): List<Int> =
        ChartDatasetGenerator.generateMonthlyDailyData(entries, date)

    suspend fun generateMonthlyDailyDataAsync(
        entries: List<Long>,
        date: Calendar
    ): List<Int> = withContext(defaultDispatcher) {
        generateMonthlyDailyData(entries, date)
    }

    fun generateYearlyData(
        entries: List<Long>,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<Int> = ChartDatasetGenerator.generateYearlyData(entries, date, zoneId)

    fun generateYearlyData(entries: List<Long>, date: Calendar): List<Int> =
        ChartDatasetGenerator.generateYearlyData(entries, date)

    suspend fun generateYearlyDataAsync(
        entries: List<Long>,
        date: Calendar
    ): List<Int> = withContext(defaultDispatcher) {
        generateYearlyData(entries, date)
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
    ): SavingsData = SavingsCalculator.calculateSavings(
        entries, resistedCount, dailyLimit, packPrice, packSize, trackingStartMillis, zoneId, nowMillis
    )

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
        calculateSavings(entries, resistedCount, dailyLimit, packPrice, packSize, trackingStartMillis, zoneId, nowMillis)
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
    ): SmokingBaselineAnalytics? = SavingsCalculator.calculateBaselineAnalytics(
        startDate, dailyAvg, packPrice, packSize, entries, resistedEntries, trackingStartMillis, zoneId, nowMillis
    )

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
            startDate, dailyAvg, packPrice, packSize, entries, resistedEntries, trackingStartMillis, zoneId, nowMillis
        )
    }

    fun calculateHistoricalBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        rankedTriggers: List<String>,
        nowMillis: Long = System.currentTimeMillis()
    ): HistoricalBaselineStats = SavingsCalculator.calculateHistoricalBaseline(
        startDate, dailyAvg, packPrice, packSize, rankedTriggers, nowMillis
    )

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

    fun calculateWeeklyComparison(
        entries: List<Long>,
        referenceDate: LocalDate = LocalDate.now(),
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): WeeklyComparisonData = TimeDistributionCalculator.calculateWeeklyComparison(
        entries, referenceDate, nowMillis, zoneId, firstDayOfWeek
    )

    fun calculateWeeklyComparison(
        entries: List<Long>,
        referenceDate: Calendar = Calendar.getInstance(),
        now: Calendar = Calendar.getInstance(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): WeeklyComparisonData = TimeDistributionCalculator.calculateWeeklyComparison(
        entries, referenceDate, now, firstDayOfWeek
    )

    suspend fun calculateWeeklyComparisonAsync(
        entries: List<Long>,
        referenceDate: Calendar = Calendar.getInstance(),
        now: Calendar = Calendar.getInstance(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ): WeeklyComparisonData = withContext(defaultDispatcher) {
        calculateWeeklyComparison(entries, referenceDate, now, firstDayOfWeek)
    }

    fun calculateHourlyDistribution(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): HourlyDistributionData = TimeDistributionCalculator.calculateHourlyDistribution(entries, zoneId)

    suspend fun calculateHourlyDistributionAsync(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): HourlyDistributionData = withContext(defaultDispatcher) {
        calculateHourlyDistribution(entries, zoneId)
    }

    fun calculateSmokingIntervals(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): SmokingIntervalData = TimeDistributionCalculator.calculateSmokingIntervals(entries, zoneId, nowMillis)

    suspend fun calculateSmokingIntervalsAsync(
        entries: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): SmokingIntervalData = withContext(defaultDispatcher) {
        calculateSmokingIntervals(entries, zoneId, nowMillis)
    }

    companion object {
        const val MINUTES_PER_CIGARETTE = SavingsCalculator.MINUTES_PER_CIGARETTE
        const val SLEEP_THRESHOLD_MS = TimeDistributionCalculator.SLEEP_THRESHOLD_MS

        val EMPTY_DAILY_DATA: List<Int> = ChartDatasetGenerator.EMPTY_DAILY_DATA
        val EMPTY_WEEKLY_DATA: List<Int> = ChartDatasetGenerator.EMPTY_WEEKLY_DATA
        val EMPTY_MONTHLY_CHUNKS: List<Int> = ChartDatasetGenerator.EMPTY_MONTHLY_CHUNKS
        val EMPTY_YEARLY_DATA: List<Int> = ChartDatasetGenerator.EMPTY_YEARLY_DATA
    }
}
