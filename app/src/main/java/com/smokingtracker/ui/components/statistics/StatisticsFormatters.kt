package com.smokingtracker.ui.components.statistics

import android.content.Context
import com.smokingtracker.R

fun formatMinutesShort(totalMinutes: Int, context: Context): String {
    if (totalMinutes == 0) return context.getString(R.string.minutes_format_zero)
    val days = totalMinutes / (24 * 60)
    val hours = (totalMinutes % (24 * 60)) / 60
    val mins = totalMinutes % 60
    return buildString {
        if (days > 0) {
            append(context.getString(R.string.minutes_format_day, days).trim())
            if (hours > 0) append(" ").append(context.getString(R.string.minutes_format_hour, hours).trim())
        } else if (hours > 0) {
            append(context.getString(R.string.minutes_format_hour, hours).trim())
            if (mins > 0) append(" ").append(context.getString(R.string.minutes_format_min, mins).trim())
        } else {
            append(context.getString(R.string.minutes_format_min, mins).trim())
        }
    }.trim()
}
