package com.smokingtracker.ui.components.triggers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.ui.graphics.vector.ImageVector
import com.smokingtracker.R
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.ui.components.triggers.models.QuadrantDominantTrigger
import com.smokingtracker.ui.components.triggers.models.TriggerTrendData
import com.smokingtracker.ui.components.triggers.models.TriggerTrendType
import java.util.Calendar

fun getTriggerIcon(triggerKey: String?): ImageVector {
    if (triggerKey == null) return Icons.Filled.SmokingRooms
    val trigger = TriggerType.fromKey(triggerKey)
    return when (trigger) {
        TriggerType.STRESS -> Icons.Filled.SentimentDissatisfied
        TriggerType.BOREDOM -> Icons.Filled.HourglassEmpty
        TriggerType.SOCIAL -> Icons.Filled.People
        TriggerType.ROUTINE -> Icons.Filled.Repeat
        TriggerType.FOOD_COFFEE -> Icons.Filled.LocalCafe
        TriggerType.ALCOHOL -> Icons.Filled.LocalBar
        null -> Icons.Filled.Psychology
    }
}

fun getCopingStrategyTip(triggerKey: String): Int {
    val type = TriggerType.fromKey(triggerKey)
    return when (type) {
        TriggerType.STRESS -> R.string.trigger_tip_stress
        TriggerType.BOREDOM -> R.string.trigger_tip_boredom
        TriggerType.SOCIAL -> R.string.trigger_tip_social
        TriggerType.ROUTINE -> R.string.trigger_tip_routine
        TriggerType.FOOD_COFFEE -> R.string.trigger_tip_food_coffee
        TriggerType.ALCOHOL -> R.string.trigger_tip_alcohol
        null -> R.string.trigger_tip_custom
    }
}

fun calculatePeakPeriodForTrigger(entities: List<SmokingEntryEntity>, triggerKey: String): Int? {
    val relevantEntities = entities.filter { it.trigger == triggerKey }
    if (relevantEntities.isEmpty()) return null

    var night = 0
    var morning = 0
    var afternoon = 0
    var evening = 0

    val cal = Calendar.getInstance()
    relevantEntities.forEach { entity ->
        cal.timeInMillis = entity.timestamp
        when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> night++
            in 6..11 -> morning++
            in 12..17 -> afternoon++
            else -> evening++
        }
    }

    val periods = listOf(
        night to R.string.peak_period_night,
        morning to R.string.peak_period_morning,
        afternoon to R.string.peak_period_afternoon,
        evening to R.string.peak_period_evening
    )

    return periods.maxByOrNull { it.first }?.second
}

fun calculateTriggerTrend(currentCount: Int, previousCount: Int, isPeriodApplicable: Boolean): TriggerTrendData {
    if (!isPeriodApplicable) return TriggerTrendData(TriggerTrendType.NONE, 0)
    if (previousCount == 0 && currentCount > 0) return TriggerTrendData(TriggerTrendType.NEW, currentCount)
    if (currentCount > previousCount) return TriggerTrendData(TriggerTrendType.INCREASED, currentCount - previousCount)
    if (currentCount < previousCount) return TriggerTrendData(TriggerTrendType.DECREASED, previousCount - currentCount)
    return TriggerTrendData(TriggerTrendType.SAME, 0)
}

fun calculateTriggerHourlyDistribution(entities: List<SmokingEntryEntity>, triggerKey: String): List<Int> {
    val hourly = IntArray(24)
    val cal = Calendar.getInstance()
    entities.forEach { entity ->
        if (entity.trigger == triggerKey) {
            cal.timeInMillis = entity.timestamp
            val h = cal.get(Calendar.HOUR_OF_DAY)
            if (h in 0..23) hourly[h]++
        }
    }
    return hourly.toList()
}

fun calculateQuadrantDominantTriggers(entities: List<SmokingEntryEntity>): List<QuadrantDominantTrigger> {
    val cal = Calendar.getInstance()
    val quadrantBuckets = List(4) { mutableListOf<String>() }

    entities.forEach { entity ->
        val trigger = entity.trigger
        if (trigger != null) {
            cal.timeInMillis = entity.timestamp
            val q = when (cal.get(Calendar.HOUR_OF_DAY)) {
                in 0..5 -> 0
                in 6..11 -> 1
                in 12..17 -> 2
                else -> 3
            }
            quadrantBuckets[q].add(trigger)
        }
    }

    return quadrantBuckets.mapIndexed { index, triggersInQuadrant ->
        if (triggersInQuadrant.isEmpty()) {
            QuadrantDominantTrigger(index, null, 0, 0)
        } else {
            val counts = triggersInQuadrant.groupingBy { it }.eachCount()
            val maxEntry = counts.maxByOrNull { it.value }
            QuadrantDominantTrigger(
                quadrantIndex = index,
                triggerKey = maxEntry?.key,
                count = maxEntry?.value ?: 0,
                totalInQuadrant = triggersInQuadrant.size
            )
        }
    }
}
