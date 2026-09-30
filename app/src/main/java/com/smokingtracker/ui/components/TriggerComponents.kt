package com.smokingtracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.ui.theme.ContainerIcon
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress
import com.smokingtracker.ui.theme.subContainer
import java.util.Calendar
import java.util.Locale

enum class TriggerTrendType {
    INCREASED,
    DECREASED,
    SAME,
    NEW,
    NONE
}

data class TriggerTrendData(
    val type: TriggerTrendType,
    val delta: Int
)

data class QuadrantDominantTrigger(
    val quadrantIndex: Int,
    val triggerKey: String?,
    val count: Int,
    val totalInQuadrant: Int
)

private data class TriggerQuadPart(
    val index: Int,
    val title: String,
    val hours: String,
    val count: Int,
    val icon: ImageVector,
    val color: Color
)

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
    val hourly = IntArray(24) { 0 }
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

@Composable
fun TriggerTrendBadge(
    trend: TriggerTrendData,
    modifier: Modifier = Modifier
) {
    val chartPalette = rememberChartColorPalette()
    when (trend.type) {
        TriggerTrendType.INCREASED -> {
            Surface(
                shape = CircleShape,
                color = chartPalette.errorContainer,
                contentColor = chartPalette.onErrorContainer,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = stringResource(R.string.trigger_trend_increase, trend.delta),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        TriggerTrendType.DECREASED -> {
            Surface(
                shape = CircleShape,
                color = chartPalette.cleanDayContainer,
                contentColor = chartPalette.onCleanDayContainer,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = stringResource(R.string.trigger_trend_decrease, trend.delta),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        TriggerTrendType.NEW -> {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = modifier
            ) {
                Text(
                    text = stringResource(R.string.trigger_trend_new),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        TriggerTrendType.SAME, TriggerTrendType.NONE -> Unit
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun TopTriggerHeroCard(
    triggerKey: String,
    triggerName: String,
    count: Int,
    percent: Int,
    peakPeriodResId: Int?,
    trend: TriggerTrendData,
    onClick: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.98f)
    val tipResId = remember(triggerKey) { getCopingStrategyTip(triggerKey) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bouncyPress(bouncy)
            .clickable(interactionSource = interactionSource, indication = null) {
                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                onClick()
            },
        shape = containerShape(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ContainerIcon(
                        icon = getTriggerIcon(triggerKey),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        backdropColor = MaterialTheme.colorScheme.primaryContainer,
                        size = 48.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.trigger_hero_badge_main),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = triggerName,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.subContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SmokingRooms,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (trend.type != TriggerTrendType.NONE && trend.type != TriggerTrendType.SAME) {
                        TriggerTrendBadge(trend = trend)
                    }

                    if (peakPeriodResId != null) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.subContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = stringResource(R.string.trigger_peak_time_prefix, stringResource(peakPeriodResId)),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = containerShape(RoundedCornerShape(18.dp)),
                    color = MaterialTheme.colorScheme.subContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.trigger_strategy_title),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(tipResId),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TriggerTimeDistributionCard(
    entities: List<SmokingEntryEntity>,
    onTriggerSelected: (String) -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val dominants = remember(entities) { calculateQuadrantDominantTriggers(entities) }

    val quadrantMeta = listOf(
        Triple(stringResource(R.string.day_part_night).substringBefore(" ("), "00–06", Icons.Filled.Bedtime),
        Triple(stringResource(R.string.day_part_morning).substringBefore(" ("), "06–12", Icons.Filled.WbTwilight),
        Triple(stringResource(R.string.day_part_afternoon).substringBefore(" ("), "12–18", Icons.Filled.WbSunny),
        Triple(stringResource(R.string.day_part_evening).substringBefore(" ("), "18–24", Icons.Filled.NightsStay)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.trigger_time_patterns_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.trigger_time_patterns_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dominants.forEach { dominant ->
                    val (qName, qHours, qIcon) = quadrantMeta[dominant.quadrantIndex]
                    val hasData = dominant.triggerKey != null && dominant.count > 0
                    val pct = if (dominant.totalInQuadrant > 0) ((dominant.count.toFloat() / dominant.totalInQuadrant) * 100).toInt() else 0
                    val triggerType = dominant.triggerKey?.let { TriggerType.fromKey(it) }
                    val triggerName = triggerType?.let { stringResource(it.labelResId) } ?: dominant.triggerKey ?: ""

                    val interactionSource = remember { MutableInteractionSource() }
                    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.94f)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bouncyPress(bouncy)
                            .clickable(
                                enabled = hasData,
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (dominant.triggerKey != null) {
                                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    onTriggerSelected(dominant.triggerKey)
                                }
                            },
                        shape = containerShape(RoundedCornerShape(18.dp)),
                        color = MaterialTheme.colorScheme.subContainer
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = qIcon,
                                contentDescription = null,
                                tint = if (hasData) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = qHours,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )

                            if (hasData) {
                                val cookieShape = MaterialShapes.Cookie9Sided.toShape()
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(cookieShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getTriggerIcon(dominant.triggerKey),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = triggerName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.trigger_no_activity_period),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TriggerDetailBottomSheet(
    triggerKey: String,
    filteredEntities: List<SmokingEntryEntity>,
    allFilteredTotalCount: Int,
    trend: TriggerTrendData,
    vibrationEnabled: Boolean,
    onDismissRequest: () -> Unit
) {
    val chartPalette = rememberChartColorPalette()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val triggerType = TriggerType.fromKey(triggerKey)
    val triggerName = triggerType?.let { stringResource(it.labelResId) } ?: triggerKey
    val tipResId = remember(triggerKey) { getCopingStrategyTip(triggerKey) }

    val triggerEntities = remember(filteredEntities, triggerKey) {
        filteredEntities.filter { it.trigger == triggerKey }
    }
    val triggerCount = triggerEntities.size
    val sharePercent = if (allFilteredTotalCount > 0) ((triggerCount.toFloat() / allFilteredTotalCount) * 100).toInt() else 0

    val hourlyCounts = remember(triggerEntities, triggerKey) {
        calculateTriggerHourlyDistribution(triggerEntities, triggerKey)
    }
    val maxHourlyCount = hourlyCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val peakHour = hourlyCounts.indices.maxByOrNull { hourlyCounts[it] } ?: 0
    val peakHourCount = hourlyCounts[peakHour]

    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedQuadrant by remember { mutableStateOf<Int?>(null) }

    val cal = Calendar.getInstance()
    val nightCount = remember(triggerEntities) {
        triggerEntities.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 0..5
        }
    }
    val morningCount = remember(triggerEntities) {
        triggerEntities.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 6..11
        }
    }
    val afternoonCount = remember(triggerEntities) {
        triggerEntities.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 12..17
        }
    }
    val eveningCount = remember(triggerEntities) {
        triggerEntities.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 18..23
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = containerShape(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        containerColor = if (MaterialTheme.colorScheme.surfaceContainerLow == Color.White) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ContainerIcon(
                        icon = getTriggerIcon(triggerKey),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        backdropColor = MaterialTheme.colorScheme.primaryContainer,
                        size = 46.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = triggerName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (trend.type != TriggerTrendType.NONE && trend.type != TriggerTrendType.SAME) {
                                TriggerTrendBadge(trend = trend)
                            }
                        }
                        Text(
                            text = stringResource(R.string.trigger_detail_share_format, triggerCount, sharePercent),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = {
                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        onDismissRequest()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.btn_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = containerShape(RoundedCornerShape(18.dp)),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (selectedHour != null) Icons.Filled.AccessTime else Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    val (qName, qCount) = when (selectedQuadrant) {
                        0 -> stringResource(R.string.day_part_night).substringBefore(" (") to nightCount
                        1 -> stringResource(R.string.day_part_morning).substringBefore(" (") to morningCount
                        2 -> stringResource(R.string.day_part_afternoon).substringBefore(" (") to afternoonCount
                        else -> stringResource(R.string.day_part_evening).substringBefore(" (") to eveningCount
                    }
                    val qPct = if (triggerCount > 0) ((qCount.toFloat() / triggerCount) * 100).toInt() else 0

                    val hourHighlightText = if (selectedHour != null) {
                        val sh = selectedHour!!
                        val eh = (sh + 1) % 24
                        val selCount = hourlyCounts[sh]
                        String.format(Locale.getDefault(), "%02d:00–%02d:00: %d", sh, eh, selCount)
                    } else if (selectedQuadrant != null) {
                        stringResource(R.string.peak_hours_quadrant_format, qName, qCount.toString(), qPct)
                    } else if (peakHourCount > 0) {
                        stringResource(R.string.trigger_detail_peak_hour_format, peakHour, (peakHour + 1) % 24, peakHourCount)
                    } else {
                        stringResource(R.string.trigger_detail_no_peak)
                    }
                    Text(
                        text = hourHighlightText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val quadrants = listOf(
                TriggerQuadPart(0, stringResource(R.string.day_part_night).substringBefore(" ("), "00–06", nightCount, Icons.Filled.Bedtime, chartPalette.tertiary),
                TriggerQuadPart(1, stringResource(R.string.day_part_morning).substringBefore(" ("), "06–12", morningCount, Icons.Filled.WbTwilight, chartPalette.primary.copy(alpha = 0.75f)),
                TriggerQuadPart(2, stringResource(R.string.day_part_afternoon).substringBefore(" ("), "12–18", afternoonCount, Icons.Filled.WbSunny, chartPalette.primary),
                TriggerQuadPart(3, stringResource(R.string.day_part_evening).substringBefore(" ("), "18–24", eveningCount, Icons.Filled.NightsStay, chartPalette.cleanDay)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quadrants.forEach { q ->
                    val isSelected = selectedQuadrant == q.index
                    val pct = if (triggerCount > 0) ((q.count.toFloat() / triggerCount) * 100).toInt() else 0
                    val interactionSource = remember { MutableInteractionSource() }
                    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.94f)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bouncyPress(bouncy)
                            .clickable(interactionSource = interactionSource, indication = null) {
                                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                selectedQuadrant = if (selectedQuadrant == q.index) null else q.index
                                if (selectedQuadrant != null && selectedHour != null && selectedHour !in (selectedQuadrant!! * 6 until (selectedQuadrant!! + 1) * 6)) {
                                    selectedHour = null
                                }
                            },
                        shape = containerShape(RoundedCornerShape(16.dp)),
                        color = if (isSelected) chartPalette.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = q.icon,
                                contentDescription = null,
                                tint = if (isSelected) chartPalette.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = q.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isSelected) chartPalette.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "${q.count}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (isSelected) chartPalette.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) chartPalette.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.trigger_hourly_distribution_title),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .pointerInput(hourlyCounts) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    val hourIndex = ((offset.x / size.width) * 24).toInt().coerceIn(0, 23)
                                    if (selectedHour != hourIndex) {
                                        selectedHour = hourIndex
                                        HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                    }
                                },
                                onHorizontalDrag = { change, _ ->
                                    change.consume()
                                    val hourIndex = ((change.position.x / size.width) * 24).toInt().coerceIn(0, 23)
                                    if (selectedHour != hourIndex) {
                                        selectedHour = hourIndex
                                        HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                    }
                                }
                            )
                        }
                ) {
                    val isGeneralView = selectedQuadrant == null && selectedHour == null
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        hourlyCounts.forEachIndexed { hour, count ->
                            val isPeak = isGeneralView && hour == peakHour && count > 0
                            val isSelected = hour == selectedHour
                            val inSelectedQuadrant = selectedQuadrant == null || hour in (selectedQuadrant!! * 6 until (selectedQuadrant!! + 1) * 6)

                            val animFraction = remember { Animatable(0.04f) }
                            LaunchedEffect(count, maxHourlyCount) {
                                val target = if (count == 0) 0.04f else (count.toFloat() / maxHourlyCount).coerceIn(0.08f, 1.0f)
                                animFraction.animateTo(
                                    targetValue = target,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = 650f
                                    )
                                )
                            }

                            val baseColor = when (hour) {
                                in 0..5 -> chartPalette.tertiary
                                in 6..11 -> chartPalette.primary.copy(alpha = 0.75f)
                                in 12..17 -> chartPalette.primary
                                else -> chartPalette.cleanDay
                            }

                            val barColor = when {
                                isSelected -> chartPalette.primary
                                !inSelectedQuadrant -> baseColor.copy(alpha = 0.15f)
                                isPeak -> chartPalette.primary
                                count > 0 -> if (selectedQuadrant != null) baseColor else baseColor.copy(alpha = 0.75f)
                                else -> chartPalette.surfaceContainerHighest.copy(alpha = 0.4f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedHour = if (selectedHour == hour) null else hour
                                        HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                    },
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    if (isPeak) {
                                        Icon(
                                            imageVector = Icons.Filled.Bolt,
                                            contentDescription = null,
                                            tint = chartPalette.primary,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .padding(bottom = 2.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(108.dp * animFraction.value)
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(barColor)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("00:00", "06:00", "12:00", "18:00", "23:00").forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Surface(
                shape = containerShape(RoundedCornerShape(18.dp)),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.trigger_strategy_title),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(tipResId),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
