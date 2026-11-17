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
import kotlin.math.ceil

object ChartDatasetGenerator {

    val EMPTY_DAILY_DATA: List<Int> = List(24) { 0 }
    val EMPTY_WEEKLY_DATA: List<Int> = List(7) { 0 }
    val EMPTY_MONTHLY_CHUNKS: List<Int> = List(4) { 0 }
    val EMPTY_YEARLY_DATA: List<Int> = List(12) { 0 }

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

    fun generateMonthlyDailyDataArray(
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
}
