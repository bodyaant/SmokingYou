package com.smokingtracker

import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Locale

fun toLocalDate(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
    return try {
        Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
    } catch (e: DateTimeException) {
        LocalDate.now(zoneId)
    }
}

fun startOfDayMillis(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Long {
    return try {
        date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    } catch (e: DateTimeException) {
        0L
    } catch (e: ArithmeticException) {
        0L
    }
}

fun startOfDayMillis(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Long {
    return startOfDayMillis(toLocalDate(epochMillis, zoneId), zoneId)
}

fun startOfTodayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
    return startOfDayMillis(LocalDate.now(zoneId), zoneId)
}

fun firstDayOfWeek(locale: Locale = Locale.getDefault()): DayOfWeek {
    return WeekFields.of(locale).firstDayOfWeek
}

fun startOfWeek(
    date: LocalDate,
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
): LocalDate {
    return date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
}

fun startOfWeekMillis(
    date: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
): Long {
    return startOfDayMillis(startOfWeek(date, firstDayOfWeek), zoneId)
}

fun calendarDayOfWeekToJavaTime(calendarDayOfWeek: Int): DayOfWeek {
    return when (calendarDayOfWeek) {
        Calendar.MONDAY -> DayOfWeek.MONDAY
        Calendar.TUESDAY -> DayOfWeek.TUESDAY
        Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
        Calendar.THURSDAY -> DayOfWeek.THURSDAY
        Calendar.FRIDAY -> DayOfWeek.FRIDAY
        Calendar.SATURDAY -> DayOfWeek.SATURDAY
        Calendar.SUNDAY -> DayOfWeek.SUNDAY
        else -> DayOfWeek.MONDAY
    }
}

fun daysBetween(startDate: LocalDate, endDate: LocalDate): Long {
    return try {
        ChronoUnit.DAYS.between(startDate, endDate).coerceAtLeast(0L)
    } catch (e: DateTimeException) {
        0L
    } catch (e: ArithmeticException) {
        0L
    }
}

fun daysBetween(
    startMillis: Long,
    endMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): Long {
    val startDate = toLocalDate(startMillis, zoneId)
    val endDate = toLocalDate(endMillis, zoneId)
    return daysBetween(startDate, endDate)
}

fun groupCountByDay(entries: Iterable<Long>, zoneId: ZoneId = ZoneId.systemDefault()): Map<LocalDate, Int> {
    val counts = mutableMapOf<LocalDate, Int>()
    for (ts in entries) {
        val date = toLocalDate(ts, zoneId)
        counts[date] = (counts[date] ?: 0) + 1
    }
    return counts
}

fun hourOfDay(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Int {
    return try {
        Instant.ofEpochMilli(epochMillis).atZone(zoneId).hour
    } catch (e: DateTimeException) {
        0
    }
}

fun minuteOfDay(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Int {
    return try {
        val localTime = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalTime()
        localTime.hour * 60 + localTime.minute
    } catch (e: DateTimeException) {
        0
    }
}
