package com.smokingtracker.ui.graph.cards

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.ui.components.charts.ChartStyleSelector
import com.smokingtracker.ui.components.charts.ChartViewStyle
import com.smokingtracker.ui.components.charts.DayDetailCard
import com.smokingtracker.ui.components.charts.MonthCalendarHeatmap
import com.smokingtracker.ui.components.charts.PillBarChart
import com.smokingtracker.ui.components.charts.SmoothSplineLineChart
import com.smokingtracker.ui.components.charts.rememberChartColorPalette
import com.smokingtracker.ui.graph.components.HeroPeriodSelector
import com.smokingtracker.ui.graph.models.ChartPeriod
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress
import com.smokingtracker.ui.theme.subContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private data class SelectedSliceData(
    val dateFormatted: String,
    val count: Int,
    val cost: Float,
    val avgInterval: Int?,
    val topTriggerKey: String?
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HeroAnalyticsCard(
    selectedPeriod: ChartPeriod,
    onPeriodSelected: (ChartPeriod) -> Unit,
    dailyDate: Calendar,
    weeklyDate: Calendar,
    monthlyDate: Calendar,
    yearlyDate: Calendar,
    dailyData: List<Int>,
    weeklyData: List<Int>,
    monthlyData: List<Int>,
    yearlyData: List<Int>,
    dailyStr: String,
    weeklyStr: String,
    monthlyStr: String,
    yearlyStr: String,
    canGoNextDaily: Boolean,
    canGoNextWeekly: Boolean,
    canGoNextMonthly: Boolean,
    canGoNextYearly: Boolean,
    onPreviousDaily: () -> Unit,
    onNextDaily: () -> Unit,
    onPreviousWeekly: () -> Unit,
    onNextWeekly: () -> Unit,
    onPreviousMonthly: () -> Unit,
    onNextMonthly: () -> Unit,
    onPreviousYearly: () -> Unit,
    onNextYearly: () -> Unit,
    onDateClick: () -> Unit,
    dailyLimit: Int,
    showLimitOnGraph: Boolean,
    vibrationEnabled: Boolean,
    packPrice: Float = 0f,
    packSize: Int = 20,
    currency: String = "",
    triggerEntities: List<SmokingEntryEntity> = emptyList(),
    weeklyComparison: StatisticsManager.WeeklyComparisonData? = null,
    onShowWeeklyComparison: (() -> Unit)? = null,
    hourlyDistribution: StatisticsManager.HourlyDistributionData? = null,
    onShowPeakHours: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val locale = LocalLocale.current.platformLocale
    val chartPalette = rememberChartColorPalette()

    val monthJan = stringResource(R.string.month_jan_short)
    val monthFeb = stringResource(R.string.month_feb_short)
    val monthMar = stringResource(R.string.month_mar_short)
    val monthApr = stringResource(R.string.month_apr_short)
    val monthMay = stringResource(R.string.month_may_short)
    val monthJun = stringResource(R.string.month_jun_short)
    val monthJul = stringResource(R.string.month_jul_short)
    val monthAug = stringResource(R.string.month_aug_short)
    val monthSep = stringResource(R.string.month_sep_short)
    val monthOct = stringResource(R.string.month_oct_short)
    val monthNov = stringResource(R.string.month_nov_short)
    val monthDec = stringResource(R.string.month_dec_short)
    val yearMonthLabels = remember(monthJan, monthFeb, monthMar, monthApr, monthMay, monthJun, monthJul, monthAug, monthSep, monthOct, monthNov, monthDec) {
        listOf(monthJan, monthFeb, monthMar, monthApr, monthMay, monthJun, monthJul, monthAug, monthSep, monthOct, monthNov, monthDec)
    }

    var chartViewStyle by rememberSaveable(selectedPeriod) { mutableStateOf(ChartViewStyle.BARS) }

    val (currentData, currentDateStr, canGoNext, onPrev, onNext) = when (selectedPeriod) {
        ChartPeriod.DAY -> Quintuple(dailyData, dailyStr, canGoNextDaily, onPreviousDaily, onNextDaily)
        ChartPeriod.WEEK -> Quintuple(weeklyData, weeklyStr, canGoNextWeekly, onPreviousWeekly, onNextWeekly)
        ChartPeriod.MONTH -> Quintuple(monthlyData, monthlyStr, canGoNextMonthly, onPreviousMonthly, onNextMonthly)
        ChartPeriod.YEAR -> Quintuple(yearlyData, yearlyStr, canGoNextYearly, onPreviousYearly, onNextYearly)
    }

    var selectedIndex by rememberSaveable(currentData, selectedPeriod, currentDateStr) { mutableStateOf<Int?>(null) }

    val totalCount = currentData.sum()
    val isLimitApplicable = showLimitOnGraph && dailyLimit > 0 && (selectedPeriod == ChartPeriod.WEEK || selectedPeriod == ChartPeriod.MONTH)

    val today = Calendar.getInstance()
    val todayIndex = remember(selectedPeriod, dailyDate, weeklyDate, monthlyDate, yearlyDate) {
        when (selectedPeriod) {
            ChartPeriod.DAY -> {
                if (dailyDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    dailyDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                ) today.get(Calendar.HOUR_OF_DAY) else null
            }
            ChartPeriod.WEEK -> {
                val weekStart = weeklyDate.clone() as Calendar
                weekStart.firstDayOfWeek = Calendar.MONDAY
                while (weekStart.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    weekStart.add(Calendar.DAY_OF_YEAR, -1)
                }
                val weekEnd = weekStart.clone() as Calendar
                weekEnd.add(Calendar.DAY_OF_YEAR, 7)
                if (today.timeInMillis in weekStart.timeInMillis until weekEnd.timeInMillis) {
                    (today.get(Calendar.DAY_OF_WEEK) + 5) % 7
                } else null
            }
            ChartPeriod.MONTH -> {
                if (monthlyDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    monthlyDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                ) today.get(Calendar.DAY_OF_MONTH) - 1 else null
            }
            ChartPeriod.YEAR -> {
                if (yearlyDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                    today.get(Calendar.MONTH)
                } else null
            }
        }
    }

    val cleanDayIndices = remember(currentData, selectedPeriod, todayIndex, dailyDate, weeklyDate, monthlyDate, yearlyDate) {
        if (selectedPeriod == ChartPeriod.WEEK || selectedPeriod == ChartPeriod.MONTH) {
            currentData.indices.filter { idx ->
                val isZero = currentData[idx] == 0
                val notFuture = if (todayIndex != null) idx <= todayIndex else {
                    val cal = when (selectedPeriod) {
                        ChartPeriod.WEEK -> weeklyDate
                        else -> monthlyDate
                    }
                    cal.before(today)
                }
                isZero && notFuture
            }.toSet()
        } else emptySet()
    }

    val xAxisLabels = remember(selectedPeriod, weeklyDate, monthlyData.size, locale, yearMonthLabels) {
        when (selectedPeriod) {
            ChartPeriod.DAY -> (0..23).map { String.format(locale, "%02d:00", it) }
            ChartPeriod.WEEK -> {
                val cal = weeklyDate.clone() as Calendar
                cal.firstDayOfWeek = Calendar.MONDAY
                while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }
                val dayFormat = SimpleDateFormat("EE", locale)
                (0..6).map {
                    val name = dayFormat.format(cal.time)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                }
            }
            ChartPeriod.MONTH -> {
                val daysInMonth = monthlyData.size.coerceAtLeast(1)
                (1..daysInMonth).map { "$it" }
            }
            ChartPeriod.YEAR -> yearMonthLabels
        }
    }

    val tooltipFormatter: (Int) -> String = remember(selectedPeriod, dailyDate, weeklyDate, monthlyDate, yearlyDate) {
        { index ->
            when (selectedPeriod) {
                ChartPeriod.DAY -> {
                    val nextHour = (index + 1) % 24
                    String.format(Locale.getDefault(), "%02d:00 – %02d:00", index, nextHour)
                }
                ChartPeriod.WEEK -> {
                    val cal = weeklyDate.clone() as Calendar
                    cal.firstDayOfWeek = Calendar.MONDAY
                    while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    cal.add(Calendar.DAY_OF_YEAR, index)
                    SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(cal.time)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
                ChartPeriod.MONTH -> {
                    val cal = monthlyDate.clone() as Calendar
                    cal.set(Calendar.DAY_OF_MONTH, index + 1)
                    SimpleDateFormat("d MMMM", Locale.getDefault()).format(cal.time)
                }
                ChartPeriod.YEAR -> {
                    val cal = yearlyDate.clone() as Calendar
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.MONTH, index)
                    SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(cal.time)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
            }
        }
    }

    val selectedDayDetail = remember(selectedIndex, todayIndex, selectedPeriod, currentData, triggerEntities, dailyDate, weeklyDate, monthlyDate, yearlyDate, packPrice, packSize) {
        val sel = selectedIndex
        val isFuture = todayIndex != null && sel != null && sel > todayIndex
        if (sel != null && sel in currentData.indices && !isFuture) {
            val count = currentData[sel]
            val dateLabel = tooltipFormatter(sel)
            val cost = if (packSize > 0) (count.toFloat() / packSize) * packPrice else 0f

            val sliceStartCal = when (selectedPeriod) {
                ChartPeriod.DAY -> {
                    (dailyDate.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, sel)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                }
                ChartPeriod.WEEK -> {
                    val cal = weeklyDate.clone() as Calendar
                    cal.firstDayOfWeek = Calendar.MONDAY
                    while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    cal.add(Calendar.DAY_OF_YEAR, sel)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal
                }
                ChartPeriod.MONTH -> {
                    (monthlyDate.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, sel + 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                }
                ChartPeriod.YEAR -> {
                    (yearlyDate.clone() as Calendar).apply {
                        set(Calendar.MONTH, sel)
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                }
            }

            val sliceEndCal = (sliceStartCal.clone() as Calendar).apply {
                when (selectedPeriod) {
                    ChartPeriod.DAY -> add(Calendar.HOUR_OF_DAY, 1)
                    ChartPeriod.WEEK, ChartPeriod.MONTH -> add(Calendar.DAY_OF_YEAR, 1)
                    ChartPeriod.YEAR -> add(Calendar.MONTH, 1)
                }
            }

            val sliceEntries = triggerEntities.filter {
                it.timestamp in sliceStartCal.timeInMillis until sliceEndCal.timeInMillis && !it.isResisted
            }

            val timestamps = sliceEntries.map { it.timestamp }.sorted()
            val avgInterval = if (timestamps.size > 1) {
                val totalDiff = timestamps.last() - timestamps.first()
                ((totalDiff / (1000 * 60)) / (timestamps.size - 1)).toInt()
            } else null

            val topTrigKey = sliceEntries.mapNotNull { it.trigger }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

            SelectedSliceData(
                dateFormatted = dateLabel,
                count = count,
                cost = cost,
                avgInterval = avgInterval,
                topTriggerKey = topTrigKey
            )
        } else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroPeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = {
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    selectedIndex = null
                    onPeriodSelected(it)
                }
            )

            val prevInteractionSource = remember { MutableInteractionSource() }
            val prevBouncy = rememberBouncyPress(interactionSource = prevInteractionSource, targetScale = 0.88f)
            val nextInteractionSource = remember { MutableInteractionSource() }
            val nextBouncy = rememberBouncyPress(interactionSource = nextInteractionSource, targetScale = 0.88f)
            val dateInteractionSource = remember { MutableInteractionSource() }
            val dateBouncy = rememberBouncyPress(interactionSource = dateInteractionSource, targetScale = 0.95f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = containerShape(CircleShape),
                    color = MaterialTheme.colorScheme.subContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                prevBouncy.bounce()
                                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                selectedIndex = null
                                onPrev()
                            },
                            interactionSource = prevInteractionSource,
                            modifier = Modifier.size(36.dp).bouncyPress(prevBouncy)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = stringResource(R.string.btn_previous),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                dateBouncy.bounce()
                                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                onDateClick()
                            },
                            interactionSource = dateInteractionSource,
                            shape = CircleShape,
                            color = Color.Transparent,
                            modifier = Modifier.bouncyPress(dateBouncy)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentDateStr,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                nextBouncy.bounce()
                                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                selectedIndex = null
                                onNext()
                            },
                            enabled = canGoNext,
                            interactionSource = nextInteractionSource,
                            modifier = Modifier.size(36.dp).bouncyPress(nextBouncy)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.btn_next),
                                modifier = Modifier.size(18.dp),
                                tint = if (canGoNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                ChartStyleSelector(
                    selectedStyle = chartViewStyle,
                    onStyleSelected = {
                        selectedIndex = null
                        chartViewStyle = it
                    },
                    showCalendarOption = selectedPeriod == ChartPeriod.MONTH,
                    vibrationEnabled = vibrationEnabled
                )
            }

            val isItemHighlighted = selectedIndex != null && selectedIndex in currentData.indices
            val displayCount = if (isItemHighlighted) currentData[selectedIndex!!] else totalCount
            val displaySubtitle = if (isItemHighlighted) {
                tooltipFormatter(selectedIndex!!)
            } else {
                stringResource(R.string.stats_total_count)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    AnimatedContent(
                        targetState = displayCount,
                        transitionSpec = {
                            (fadeIn(tween(200)) + slideInVertically { it / 3 })
                                .togetherWith(fadeOut(tween(140)) + slideOutVertically { -it / 3 })
                        },
                        label = "headerCountAnim"
                    ) { countVal ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$countVal",
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                                maxLines = 1,
                                softWrap = false,
                                color = when {
                                    isItemHighlighted && countVal == 0 -> chartPalette.cleanDay
                                    isItemHighlighted && isLimitApplicable && countVal > dailyLimit -> chartPalette.error
                                    isItemHighlighted -> chartPalette.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = stringResource(R.string.history_cigs_count_format, "").trim(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = displaySubtitle,
                        transitionSpec = {
                            (fadeIn(tween(180)) + slideInVertically { it / 3 })
                                .togetherWith(fadeOut(tween(120)))
                        },
                        label = "headerSubtitleAnim"
                    ) { sub ->
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isItemHighlighted) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isItemHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val currentBadge = when {
                    isItemHighlighted -> null
                    selectedPeriod == ChartPeriod.WEEK && weeklyComparison != null && onShowWeeklyComparison != null && (weeklyComparison.thisWeekCount > 0 || weeklyComparison.lastWeekCount > 0) -> "WEEK"
                    selectedPeriod == ChartPeriod.DAY && hourlyDistribution != null && onShowPeakHours != null && hourlyDistribution.peakHourCount > 0 -> "PEAK"
                    else -> null
                }

                AnimatedContent(
                    targetState = currentBadge,
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(spring(Spring.DampingRatioLowBouncy, 400f)))
                            .togetherWith(fadeOut(tween(120)) + scaleOut(targetScale = 0.85f))
                    },
                    label = "headerBadgeAnim"
                ) { badgeKey ->
                    when (badgeKey) {
                        "WEEK" -> {
                            val interactionSource = remember { MutableInteractionSource() }
                            val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.90f)
                            val (badgeBg, badgeFg, badgeText, badgeIcon) = when (weeklyComparison!!.trend) {
                                StatisticsManager.ComparisonTrend.DECREASED -> Quadruple(
                                    chartPalette.cleanDayContainer,
                                    chartPalette.onCleanDayContainer,
                                    stringResource(R.string.weekly_comparison_decrease, weeklyComparison.percentChange),
                                    Icons.AutoMirrored.Filled.TrendingDown
                                )
                                StatisticsManager.ComparisonTrend.INCREASED -> Quadruple(
                                    chartPalette.errorContainer,
                                    chartPalette.onErrorContainer,
                                    stringResource(R.string.weekly_comparison_increase, weeklyComparison.percentChange),
                                    Icons.AutoMirrored.Filled.TrendingUp
                                )
                                StatisticsManager.ComparisonTrend.NO_CHANGE -> Quadruple(
                                    chartPalette.surfaceContainerHighest,
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                    stringResource(R.string.weekly_comparison_no_change),
                                    Icons.Filled.Remove
                                )
                            }
                            Surface(
                                shape = containerShape(CircleShape),
                                color = badgeBg,
                                modifier = Modifier
                                    .bouncyPress(bouncy)
                                    .clickable(interactionSource = interactionSource, indication = null) {
                                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                        onShowWeeklyComparison?.invoke()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = badgeIcon,
                                        contentDescription = null,
                                        tint = badgeFg,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = badgeFg,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = badgeFg.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        "PEAK" -> {
                            val interactionSource = remember { MutableInteractionSource() }
                            val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.90f)
                            val startH = hourlyDistribution!!.peakHour
                            val endH = (startH + 1) % 24
                            val peakTimeStr = String.format(locale, "%02d:00–%02d:00", startH, endH)
                            Surface(
                                shape = containerShape(CircleShape),
                                color = chartPalette.primaryContainer,
                                modifier = Modifier
                                    .bouncyPress(bouncy)
                                    .clickable(interactionSource = interactionSource, indication = null) {
                                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                        onShowPeakHours?.invoke()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = null,
                                        tint = chartPalette.onPrimaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${stringResource(R.string.graph_metric_peak)}: $peakTimeStr",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = chartPalette.onPrimaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = chartPalette.onPrimaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            if (totalCount == 0 && cleanDayIndices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = MaterialShapes.Cookie9Sided.toShape(),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.graph_no_data),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.graph_no_data_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                AnimatedContent(
                    targetState = chartViewStyle,
                    transitionSpec = {
                        (fadeIn(tween(240)) + slideInVertically { it / 20 })
                            .togetherWith(fadeOut(tween(140)))
                    },
                    label = "chartViewStyleAnimation"
                ) { style ->
                    when (style) {
                        ChartViewStyle.BARS -> {
                            PillBarChart(
                                dataPoints = currentData,
                                selectedIndex = selectedIndex,
                                onSelectIndex = { selectedIndex = it },
                                dailyLimit = if (isLimitApplicable) dailyLimit else 0,
                                showLimit = isLimitApplicable,
                                xAxisLabels = xAxisLabels,
                                todayIndex = todayIndex,
                                cleanDayIndices = cleanDayIndices,
                                vibrationEnabled = vibrationEnabled
                            )
                        }
                        ChartViewStyle.LINE -> {
                            SmoothSplineLineChart(
                                dataPoints = currentData,
                                selectedIndex = selectedIndex,
                                onSelectIndex = { selectedIndex = it },
                                dailyLimit = if (isLimitApplicable) dailyLimit else 0,
                                showLimit = isLimitApplicable,
                                xAxisLabels = xAxisLabels,
                                todayIndex = todayIndex,
                                vibrationEnabled = vibrationEnabled
                            )
                        }
                        ChartViewStyle.CALENDAR -> {
                            if (selectedPeriod == ChartPeriod.MONTH) {
                                val countsMap = remember(monthlyData) {
                                    monthlyData.mapIndexed { idx, cnt -> (idx + 1) to cnt }.toMap()
                                }
                                val isCurrentMonth = monthlyDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                        monthlyDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)

                                MonthCalendarHeatmap(
                                    year = monthlyDate.get(Calendar.YEAR),
                                    month = monthlyDate.get(Calendar.MONTH) + 1,
                                    countsByDay = countsMap,
                                    dailyLimit = if (isLimitApplicable) dailyLimit else 0,
                                    selectedDay = if (selectedIndex != null) selectedIndex!! + 1 else null,
                                    onSelectDay = { day ->
                                        selectedIndex = day - 1
                                    },
                                    todayDay = if (isCurrentMonth) today.get(Calendar.DAY_OF_MONTH) else null,
                                    vibrationEnabled = vibrationEnabled
                                )
                            } else {
                                PillBarChart(
                                    dataPoints = currentData,
                                    selectedIndex = selectedIndex,
                                    onSelectIndex = { selectedIndex = it },
                                    dailyLimit = if (isLimitApplicable) dailyLimit else 0,
                                    showLimit = isLimitApplicable,
                                    xAxisLabels = xAxisLabels,
                                    todayIndex = todayIndex,
                                    cleanDayIndices = cleanDayIndices,
                                    vibrationEnabled = vibrationEnabled
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedDayDetail != null,
                enter = fadeIn(tween(220)) + expandVertically(spring(stiffness = Spring.StiffnessLow)),
                exit = fadeOut(tween(150)) + shrinkVertically(spring(stiffness = Spring.StiffnessLow))
            ) {
                if (selectedDayDetail != null) {
                    val trigName = selectedDayDetail.topTriggerKey?.let { k ->
                        TriggerType.fromKey(k)?.let { type ->
                            stringResource(when (type) {
                                TriggerType.STRESS -> R.string.trigger_stress
                                TriggerType.BOREDOM -> R.string.trigger_boredom
                                TriggerType.SOCIAL -> R.string.trigger_social
                                TriggerType.ROUTINE -> R.string.trigger_routine
                                TriggerType.FOOD_COFFEE -> R.string.trigger_food_coffee
                                TriggerType.ALCOHOL -> R.string.trigger_alcohol
                            })
                        } ?: k
                    }

                    val detailTitle = when (selectedPeriod) {
                        ChartPeriod.DAY -> stringResource(R.string.chart_selected_hour)
                        ChartPeriod.WEEK, ChartPeriod.MONTH -> stringResource(R.string.chart_selected_day)
                        ChartPeriod.YEAR -> stringResource(R.string.chart_selected_month)
                    }

                    DayDetailCard(
                        title = detailTitle,
                        dateFormatted = selectedDayDetail.dateFormatted,
                        count = selectedDayDetail.count,
                        dailyLimit = if (isLimitApplicable) dailyLimit else 0,
                        cost = selectedDayDetail.cost,
                        currency = currency,
                        avgIntervalMinutes = selectedDayDetail.avgInterval,
                        topTrigger = trigName,
                        onClose = { selectedIndex = null }
                    )
                }
            }

            val avgPerUnit = remember(totalCount, currentData.size) {
                if (currentData.isNotEmpty()) totalCount.toFloat() / currentData.size.toFloat() else 0f
            }
            val peakCount = remember(currentData) { currentData.maxOrNull() ?: 0 }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.graph_metric_avg),
                    value = String.format(locale, "%.1f", avgPerUnit),
                    unit = stringResource(R.string.history_cigs_count_format, "").trim(),
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    tint = MaterialTheme.colorScheme.secondary
                )

                val inLimitCount = remember(currentData, dailyLimit) { currentData.count { it <= dailyLimit } }
                val secondTitle = if (isLimitApplicable) stringResource(R.string.graph_metric_in_limit) else stringResource(R.string.graph_metric_peak)
                val secondValue = if (isLimitApplicable) "$inLimitCount/${currentData.size}" else "$peakCount"
                val secondUnit = if (isLimitApplicable) {
                    stringResource(R.string.tapering_plan_pace_days, currentData.size).replace(currentData.size.toString(), "").trim()
                } else {
                    stringResource(R.string.history_cigs_count_format, "").trim()
                }
                val secondIcon = if (isLimitApplicable) Icons.Filled.Shield else Icons.Filled.Bolt
                val secondTint = if (isLimitApplicable) {
                    if (inLimitCount == currentData.size) chartPalette.primary else chartPalette.tertiary
                } else {
                    chartPalette.tertiary
                }

                MetricKpiCard(
                    modifier = Modifier.weight(1f),
                    title = secondTitle,
                    value = secondValue,
                    unit = secondUnit,
                    icon = secondIcon,
                    tint = secondTint
                )
            }
        }
    }
}

@Composable
private fun MetricKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String = "",
    icon: ImageVector,
    tint: Color
) {
    val animatedTint by animateColorAsState(
        targetValue = tint,
        animationSpec = tween(durationMillis = 250),
        label = "kpiTint"
    )

    Surface(
        modifier = modifier,
        shape = containerShape(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.subContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = animatedTint.copy(alpha = 0.14f),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AnimatedContent(
                            targetState = icon,
                            transitionSpec = {
                                (fadeIn(tween(180)) + scaleIn(spring(Spring.DampingRatioLowBouncy, 400f)))
                                    .togetherWith(fadeOut(tween(120)) + scaleOut(targetScale = 0.8f))
                            },
                            label = "kpiIconAnim"
                        ) { animIcon ->
                            Icon(
                                imageVector = animIcon,
                                contentDescription = null,
                                tint = animatedTint,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
                AnimatedContent(
                    targetState = title,
                    transitionSpec = {
                        (fadeIn(tween(180)) + slideInVertically { it / 3 })
                            .togetherWith(fadeOut(tween(120)) + slideOutVertically { -it / 3 })
                    },
                    label = "kpiTitleAnim"
                ) { animTitle ->
                    Text(
                        text = animTitle,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            AnimatedContent(
                targetState = Pair(value, unit),
                contentAlignment = Alignment.BottomStart,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 30)) +
                     slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) { it / 2 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(140)) +
                            slideOutVertically(animationSpec = tween(140)) { -it / 2 }
                        )
                },
                label = "kpiValueUnitAnim"
            ) { (animValue, animUnit) ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = animValue,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (animUnit.isNotEmpty()) {
                        Text(
                            text = animUnit,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 3.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
