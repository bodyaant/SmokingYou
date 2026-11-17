package com.smokingtracker.ui.graph.sheets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import com.smokingtracker.ui.components.charts.rememberChartColorPalette
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyComparisonBottomSheet(
    comparison: StatisticsManager.WeeklyComparisonData,
    weeklyDate: Calendar,
    vibrationEnabled: Boolean,
    onDismissRequest: () -> Unit
) {
    val chartPalette = rememberChartColorPalette()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val (thisWeekDateRange, prevWeekDateRange) = remember(weeklyDate) {
        val fmt = SimpleDateFormat("d MMM", Locale.getDefault())
        val cal = weeklyDate.clone() as Calendar
        cal.firstDayOfWeek = Calendar.MONDAY
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        val thisStart = cal.time
        cal.add(Calendar.DAY_OF_YEAR, 6)
        val thisEnd = cal.time

        cal.add(Calendar.DAY_OF_YEAR, -13)
        val prevStart = cal.time
        cal.add(Calendar.DAY_OF_YEAR, 6)
        val prevEnd = cal.time

        Pair(
            "${fmt.format(thisStart)} – ${fmt.format(thisEnd)}",
            "${fmt.format(prevStart)} – ${fmt.format(prevEnd)}"
        )
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
        val locale = LocalLocale.current.platformLocale
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weekly_comparison_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
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

            val (badgeBg, badgeFg, badgeText, badgeIcon) = when (comparison.trend) {
                StatisticsManager.ComparisonTrend.DECREASED -> Quadruple(
                    chartPalette.cleanDayContainer,
                    chartPalette.onCleanDayContainer,
                    stringResource(R.string.weekly_comparison_decrease, comparison.percentChange),
                    Icons.AutoMirrored.Filled.TrendingDown
                )
                StatisticsManager.ComparisonTrend.INCREASED -> Quadruple(
                    chartPalette.errorContainer,
                    chartPalette.onErrorContainer,
                    stringResource(R.string.weekly_comparison_increase, comparison.percentChange),
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
                shape = containerShape(RoundedCornerShape(16.dp)),
                color = badgeBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeFg,
                        modifier = Modifier.size(22.dp)
                    )
                    val desc = when (comparison.trend) {
                        StatisticsManager.ComparisonTrend.DECREASED -> stringResource(
                            R.string.weekly_comparison_desc_decrease,
                            String.format(locale, "%.1f", Math.abs(comparison.diffDailyAvg))
                        )
                        StatisticsManager.ComparisonTrend.INCREASED -> stringResource(
                            R.string.weekly_comparison_desc_increase,
                            String.format(locale, "%.1f", Math.abs(comparison.diffDailyAvg))
                        )
                        StatisticsManager.ComparisonTrend.NO_CHANGE -> stringResource(R.string.weekly_comparison_desc_no_change)
                    }
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = badgeFg
                    )
                }
            }

            val maxAvg = maxOf(comparison.thisWeekDailyAvg, comparison.lastWeekDailyAvg).coerceAtLeast(0.1f)
            val thisWeekFraction = (comparison.thisWeekDailyAvg / maxAvg).coerceIn(0f, 1f)
            val lastWeekFraction = (comparison.lastWeekDailyAvg / maxAvg).coerceIn(0f, 1f)

            var isAnimated by remember { mutableStateOf(false) }
            LaunchedEffect(comparison) {
                isAnimated = true
            }

            val animThisWeek by animateFloatAsState(
                targetValue = if (isAnimated) thisWeekFraction else 0.04f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "thisWeekProgress"
            )
            val animLastWeek by animateFloatAsState(
                targetValue = if (isAnimated) lastWeekFraction else 0.04f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "lastWeekProgress"
            )

            val thisWeekLabel = if (comparison.isCurrentWeek) {
                stringResource(R.string.weekly_comparison_this_week_label)
            } else {
                stringResource(R.string.weekly_comparison_selected_week_label)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = thisWeekLabel,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = thisWeekDateRange,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = String.format(locale, "%.1f", comparison.thisWeekDailyAvg),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (comparison.trend == StatisticsManager.ComparisonTrend.INCREASED) {
                                    chartPalette.error
                                } else {
                                    chartPalette.primary
                                }
                            )
                            Text(
                                text = stringResource(R.string.weekly_comparison_per_day_unit),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(chartPalette.surfaceContainerHighest)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animThisWeek)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    if (comparison.trend == StatisticsManager.ComparisonTrend.INCREASED) {
                                        chartPalette.error
                                    } else {
                                        chartPalette.primary
                                    }
                                )
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.weekly_comparison_total_summary,
                            comparison.thisWeekCount,
                            comparison.daysElapsedThisWeek
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.weekly_comparison_last_week_label),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = prevWeekDateRange,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = String.format(locale, "%.1f", comparison.lastWeekDailyAvg),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.weekly_comparison_per_day_unit),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(chartPalette.surfaceContainerHighest)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animLastWeek)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.weekly_comparison_total_summary,
                            comparison.lastWeekCount,
                            7
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun StatisticsManager.DayPeriod?.toDisplayString(): String {
    return when (this) {
        StatisticsManager.DayPeriod.NIGHT -> stringResource(R.string.peak_period_night)
        StatisticsManager.DayPeriod.MORNING -> stringResource(R.string.peak_period_morning)
        StatisticsManager.DayPeriod.AFTERNOON -> stringResource(R.string.peak_period_afternoon)
        StatisticsManager.DayPeriod.EVENING -> stringResource(R.string.peak_period_evening)
        null -> stringResource(R.string.peak_hours_no_data)
    }
}
