package com.smokingtracker

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun toLocalDate(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
    return Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
}

fun startOfDayMillis(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Long {
    return date.atStartOfDay(zoneId).toInstant().toEpochMilli()
}

fun startOfDayMillis(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Long {
    return toLocalDate(epochMillis, zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()
}

fun startOfTodayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
    return LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()
}

fun daysBetween(startDate: LocalDate, endDate: LocalDate): Long {
    return ChronoUnit.DAYS.between(startDate, endDate)
}

fun daysBetween(startMillis: Long, endMillis: Long): Long {
    val startDate = toLocalDate(startMillis)
    val endDate = toLocalDate(endMillis)
    return ChronoUnit.DAYS.between(startDate, endDate)
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
    return Instant.ofEpochMilli(epochMillis).atZone(zoneId).hour
}
