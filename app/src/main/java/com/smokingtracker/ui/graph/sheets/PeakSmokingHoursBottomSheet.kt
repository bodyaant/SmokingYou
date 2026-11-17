package com.smokingtracker.ui.graph.sheets

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import com.smokingtracker.ui.components.charts.rememberChartColorPalette
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeakSmokingHoursBottomSheet(
    distribution: StatisticsManager.HourlyDistributionData,
    vibrationEnabled: Boolean,
    onDismissRequest: () -> Unit
) {
    val chartPalette = rememberChartColorPalette()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedHour by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedQuadrant by rememberSaveable { mutableStateOf<Int?>(null) }

    val nightCount = distribution.nightCount
    val morningCount = distribution.morningCount
    val afternoonCount = distribution.afternoonCount
    val eveningCount = distribution.eveningCount
    val rawTotal = nightCount + morningCount + afternoonCount + eveningCount
    val total = rawTotal.coerceAtLeast(1)

    val periodText = distribution.peakPeriod.toDisplayString()
    val maxCount = distribution.hourlyCounts.maxOrNull()?.coerceAtLeast(1) ?: 1

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
                .verticalScroll(rememberScrollState())
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
                    text = stringResource(R.string.peak_hours_title),
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

            val summaryText = if (selectedHour != null) {
                val selCount = distribution.hourlyCounts[selectedHour!!]
                val startH = selectedHour!!
                val endH = (selectedHour!! + 1) % 24
                val formatTime = String.format(locale, "%02d:00–%02d:00", startH, endH)
                val countStr = stringResource(R.string.history_cigs_count_format, selCount.toString())
                stringResource(R.string.peak_hours_selected_format, formatTime, countStr)
            } else if (selectedQuadrant != null) {
                val (qName, qCount) = when (selectedQuadrant) {
                    0 -> stringResource(R.string.day_part_night).substringBefore(" (") to nightCount
                    1 -> stringResource(R.string.day_part_morning).substringBefore(" (") to morningCount
                    2 -> stringResource(R.string.day_part_afternoon).substringBefore(" (") to afternoonCount
                    else -> stringResource(R.string.day_part_evening).substringBefore(" (") to eveningCount
                }
                val qPct = if (rawTotal > 0) ((qCount.toFloat() / total) * 100).toInt() else 0
                val countFormatted = stringResource(R.string.history_cigs_count_format, qCount.toString())
                stringResource(R.string.peak_hours_quadrant_format, qName, countFormatted, qPct)
            } else if (rawTotal == 0 || distribution.peakPeriod == null) {
                stringResource(R.string.peak_hours_no_data)
            } else {
                stringResource(R.string.peak_hours_summary, periodText, distribution.peakPeriodPercent)
            }

            Surface(
                shape = containerShape(RoundedCornerShape(16.dp)),
                color = chartPalette.primaryContainer,
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
                        tint = chartPalette.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = chartPalette.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val quadrants = listOf(
                QuadPart(0, stringResource(R.string.day_part_night).substringBefore(" ("), "00-06", nightCount, Icons.Filled.Bedtime, chartPalette.tertiary),
                QuadPart(1, stringResource(R.string.day_part_morning).substringBefore(" ("), "06-12", morningCount, Icons.Filled.WbTwilight, chartPalette.primary.copy(alpha = 0.75f)),
                QuadPart(2, stringResource(R.string.day_part_afternoon).substringBefore(" ("), "12-18", afternoonCount, Icons.Filled.WbSunny, chartPalette.primary),
                QuadPart(3, stringResource(R.string.day_part_evening).substringBefore(" ("), "18-24", eveningCount, Icons.Filled.NightsStay, chartPalette.cleanDay)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quadrants.forEach { q ->
                    val isSelected = selectedQuadrant == q.index
                    val percent = ((q.count.toFloat() / total) * 100).toInt()
                    val interactionSource = remember { MutableInteractionSource() }
                    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.92f)

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
                        color = if (isSelected) chartPalette.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, chartPalette.primary) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = q.icon,
                                contentDescription = null,
                                tint = if (isSelected) chartPalette.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = q.title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) chartPalette.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${q.count}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (isSelected) chartPalette.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) chartPalette.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .pointerInput(distribution.hourlyCounts) {
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
                        distribution.hourlyCounts.forEachIndexed { hour, count ->
                            val isPeak = isGeneralView && hour == distribution.peakHour && count > 0
                            val isSelected = hour == selectedHour
                            val inSelectedQuadrant = selectedQuadrant == null || hour in (selectedQuadrant!! * 6 until (selectedQuadrant!! + 1) * 6)

                            val animFraction = remember { Animatable(0.04f) }
                            LaunchedEffect(count, maxCount) {
                                kotlinx.coroutines.delay(hour * 6L)
                                val target = if (count == 0) 0.04f else (count.toFloat() / maxCount).coerceIn(0.08f, 1.0f)
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
                                else -> chartPalette.surfaceContainerHighest.copy(alpha = 0.6f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = String.format(locale, "%02d:00–%02d:00: %d", hour, (hour + 1) % 24, count)
                                        selected = isSelected
                                    }
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
                                            .height(136.dp * animFraction.value)
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
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

data class QuadPart(
    val index: Int,
    val title: String,
    val hours: String,
    val count: Int,
    val icon: ImageVector,
    val color: Color
)
