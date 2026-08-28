package com.smokingtracker

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun daysBetween(startMillis: Long, endMillis: Long): Long {
    val startDate = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val endDate = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return ChronoUnit.DAYS.between(startDate, endDate)
}
