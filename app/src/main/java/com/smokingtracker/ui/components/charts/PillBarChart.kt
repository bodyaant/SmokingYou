package com.smokingtracker.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.ceil

@Composable
fun PillBarChart(
    dataPoints: List<Int>,
    selectedIndex: Int?,
    onSelectIndex: (Int?) -> Unit,
    dailyLimit: Int,
    showLimit: Boolean,
    xAxisLabels: List<String>,
    modifier: Modifier = Modifier,
    todayIndex: Int? = null,
    cleanDayIndices: Set<Int> = emptySet(),
    vibrationEnabled: Boolean = true,
    chartHeight: Dp = 185.dp
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val density = LocalDensity.current

    val palette = rememberChartColorPalette()
    val primaryColor = palette.primary
    val primaryContainer = palette.primaryContainer
    val tertiaryColor = palette.cleanDay
    val tertiaryContainer = palette.cleanDayContainer
    val errorColor = palette.error
    val errorContainer = palette.errorContainer
    val outlineVariant = palette.outlineVariant
    val onSurfaceVariant = palette.onSurfaceVariant
    val surfaceContainerHighest = palette.surfaceContainerHighest

    val isLimitApplicable = showLimit && dailyLimit > 0
    val rawMax = (dataPoints.maxOrNull() ?: 0).coerceAtLeast(if (isLimitApplicable) dailyLimit else 1)
    val maxY = (rawMax * 1.25f).coerceAtLeast(4f)

    val barAnimations = remember(dataPoints) {
        dataPoints.map { Animatable(0.04f) }
    }

    LaunchedEffect(dataPoints) {
        barAnimations.forEachIndexed { index, anim ->
            launch {
                anim.snapTo(0.04f)
                delay(index * 10L)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = 650f
                    )
                )
            }
        }
    }

    val gridLevels = 3
    val stepValue = ceil(maxY / gridLevels).toInt().coerceAtLeast(1)
    val maxGridValue = stepValue * gridLevels

    val animatedLimitRatio by animateFloatAsState(
        targetValue = if (isLimitApplicable) (dailyLimit.toFloat() / maxGridValue).coerceIn(0f, 1f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "limitLineRatio"
    )

    val isScrollable = dataPoints.size > 12
    val scrollState = rememberScrollState()

    LaunchedEffect(dataPoints, todayIndex) {
        if (isScrollable) {
            snapshotFlow { scrollState.maxValue }
                .filter { it > 0 }
                .first()
            val targetIndex = selectedIndex ?: todayIndex ?: (dataPoints.size - 1)
            if (targetIndex in dataPoints.indices) {
                val slotWidthPx = with(density) { 34.dp.toPx() }
                val spacingPx = with(density) { 8.dp.toPx() }
                val startPaddingPx = with(density) { 8.dp.toPx() }
                val itemCenterPx = startPaddingPx + targetIndex * (slotWidthPx + spacingPx) + slotWidthPx / 2f
                val halfViewport = (scrollState.viewportSize / 2).coerceAtLeast(1)
                val targetScroll = (itemCenterPx - halfViewport).coerceAtLeast(0f).toInt()
                scrollState.animateScrollTo(targetScroll.coerceAtMost(scrollState.maxValue))
            }
        }
    }

    val barAreaHeight = chartHeight - 22.dp
    val gap = 5.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barAreaHeight),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in gridLevels downTo 0) {
                    val labelVal = (maxGridValue * i) / gridLevels
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Canvas(modifier = Modifier.fillMaxWidth()) {
                            drawLine(
                                color = outlineVariant.copy(alpha = 0.22f),
                                start = Offset(24.dp.toPx(), size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = 0.8.dp.toPx()
                            )
                        }
                        Text(
                            text = "$labelVal",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.width(22.dp)
                        )
                    }
                }
            }

            if (isLimitApplicable) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barAreaHeight)
                        .padding(start = 24.dp)
                ) {
                    val y = size.height * (1f - animatedLimitRatio)
                    drawLine(
                        color = errorColor.copy(alpha = 0.65f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.6.dp.toPx(),
                        pathEffect = LimitDashPathEffect
                    )
                }
            }

            val currentSelectedIndex by rememberUpdatedState(selectedIndex)
            val currentTodayIndex by rememberUpdatedState(todayIndex)
            val currentOnSelectIndex by rememberUpdatedState(onSelectIndex)
            val currentVibrationEnabled by rememberUpdatedState(vibrationEnabled)
            val cleanDayLabel = stringResource(R.string.chart_clean_day)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp)
                    .clipToBounds()
            ) {
                if (isScrollable) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dataPoints.forEachIndexed { index, count ->
                            val isSelected = selectedIndex == index
                            val isToday = todayIndex == index
                            val isFuture = todayIndex != null && index > todayIndex
                            val isOverLimit = !isFuture && isLimitApplicable && count > dailyLimit
                            val isCleanDay = index in cleanDayIndices
                            val animFraction = barAnimations.getOrNull(index)?.value ?: 1f

                            val fullHeight = if (count > 0) {
                                barAreaHeight * (count.toFloat() / maxGridValue).coerceIn(0.08f, 1f)
                            } else {
                                barAreaHeight * 0.045f
                            }
                            val animatedHeight = fullHeight * animFraction

                            val baseColor = when {
                                isFuture -> surfaceContainerHighest.copy(alpha = 0.35f)
                                isSelected && isOverLimit -> errorColor.copy(alpha = 0.65f)
                                isSelected -> primaryColor
                                isOverLimit -> errorColor.copy(alpha = 0.45f)
                                isToday -> primaryColor.copy(alpha = 0.9f)
                                count > 0 -> primaryColor.copy(alpha = 0.65f)
                                else -> surfaceContainerHighest.copy(alpha = 0.7f)
                            }
                            val excessColor = if (isSelected) errorColor else errorColor.copy(alpha = 0.9f)
                            val limitHeight = barAreaHeight * animatedLimitRatio
                            val label = xAxisLabels.getOrElse(index) { "$index" }
                            val overLimitText = if (isOverLimit) {
                                ", " + stringResource(R.string.graph_over_limit_badge, count - dailyLimit)
                            } else if (isCleanDay) {
                                ", $cleanDayLabel"
                            } else ""
                            val barDescription = "$label: ${stringResource(R.string.history_cigs_count_format, count.toString())}$overLimitText"

                            Column(
                                modifier = Modifier
                                    .width(34.dp)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = barDescription
                                        selected = isSelected
                                    }
                                    .clickable(
                                        enabled = !isFuture,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        val newSel = if (selectedIndex == index) null else index
                                        onSelectIndex(newSel)
                                        HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(barAreaHeight),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    if (isOverLimit && animatedHeight > limitHeight + gap / 2) {
                                        val baseHeight = (limitHeight - gap / 2).coerceAtLeast(6.dp)
                                        val excessHeight = (animatedHeight - limitHeight - gap / 2).coerceAtLeast(4.dp)

                                        Column(
                                            modifier = Modifier.width(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(excessHeight)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(excessColor)
                                            )
                                            Spacer(modifier = Modifier.height(gap))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(baseHeight)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(baseColor)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(animatedHeight.coerceAtLeast(4.dp))
                                                .clip(RoundedCornerShape(50))
                                                .background(baseColor)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium
                                    ),
                                    color = when {
                                        isFuture -> onSurfaceVariant.copy(alpha = 0.3f)
                                        isSelected -> primaryColor
                                        isToday -> primaryColor.copy(alpha = 0.85f)
                                        else -> onSurfaceVariant.copy(alpha = 0.7f)
                                    },
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Clip,
                                    textAlign = TextAlign.Center
                                )

                                Box(
                                    modifier = Modifier
                                        .padding(top = 3.dp)
                                        .size(width = 12.dp, height = 3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isToday) primaryColor else Color.Transparent)
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(dataPoints) {
                                coroutineScope {
                                    launch {
                                        detectTapGestures(
                                            onTap = { offset ->
                                                val n = dataPoints.size
                                                if (n > 0 && size.width > 0) {
                                                    val idx = ((offset.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                                    val tIdx = currentTodayIndex
                                                    if (tIdx == null || idx <= tIdx) {
                                                        val newSel = if (currentSelectedIndex == idx) null else idx
                                                        currentOnSelectIndex(newSel)
                                                        if (newSel != null) {
                                                            HapticFeedbackHelper.performTick(currentVibrationEnabled, haptic, context)
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    launch {
                                        detectHorizontalDragGestures(
                                            onDragStart = { offset ->
                                                val n = dataPoints.size
                                                if (n > 0 && size.width > 0) {
                                                    val idx = ((offset.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                                    val tIdx = currentTodayIndex
                                                    if (tIdx == null || idx <= tIdx) {
                                                        currentOnSelectIndex(idx)
                                                        HapticFeedbackHelper.performTick(currentVibrationEnabled, haptic, context)
                                                    }
                                                }
                                            },
                                            onHorizontalDrag = { change, _ ->
                                                change.consume()
                                                val n = dataPoints.size
                                                if (n > 0 && size.width > 0) {
                                                    val idx = ((change.position.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                                    val tIdx = currentTodayIndex
                                                    if ((tIdx == null || idx <= tIdx) && idx != currentSelectedIndex) {
                                                        currentOnSelectIndex(idx)
                                                        HapticFeedbackHelper.performTick(currentVibrationEnabled, haptic, context)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                    ) {
                        val barWidth = when {
                            dataPoints.size <= 7 -> 22.dp
                            dataPoints.size <= 12 -> 14.dp
                            else -> 8.dp
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barAreaHeight),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            dataPoints.forEachIndexed { index, count ->
                                val isSelected = selectedIndex == index
                                val isToday = todayIndex == index
                                val isFuture = todayIndex != null && index > todayIndex
                                val isOverLimit = !isFuture && isLimitApplicable && count > dailyLimit
                                val isCleanDay = index in cleanDayIndices
                                val animFraction = barAnimations.getOrNull(index)?.value ?: 1f

                                val fullHeight = if (count > 0) {
                                    barAreaHeight * (count.toFloat() / maxGridValue).coerceIn(0.08f, 1f)
                                } else {
                                    barAreaHeight * 0.045f
                                }
                                val animatedHeight = fullHeight * animFraction

                                val baseColor = when {
                                    isFuture -> surfaceContainerHighest.copy(alpha = 0.35f)
                                    isSelected && isOverLimit -> errorColor.copy(alpha = 0.65f)
                                    isSelected -> primaryColor
                                    isOverLimit -> errorColor.copy(alpha = 0.45f)
                                    isToday -> primaryColor.copy(alpha = 0.9f)
                                    count > 0 -> primaryColor.copy(alpha = 0.65f)
                                    else -> surfaceContainerHighest.copy(alpha = 0.7f)
                                }
                                val excessColor = if (isSelected) errorColor else errorColor.copy(alpha = 0.9f)
                                val limitHeight = barAreaHeight * animatedLimitRatio
                                val label = xAxisLabels.getOrElse(index) { "$index" }
                                val overLimitText = if (isOverLimit) {
                                    ", " + stringResource(R.string.graph_over_limit_badge, count - dailyLimit)
                                } else if (isCleanDay) {
                                    ", $cleanDayLabel"
                                } else ""
                                val barDescription = "$label: ${stringResource(R.string.history_cigs_count_format, count.toString())}$overLimitText"

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = barDescription
                                            selected = isSelected
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    if (isOverLimit && animatedHeight > limitHeight + gap / 2) {
                                        val baseHeight = (limitHeight - gap / 2).coerceAtLeast(6.dp)
                                        val excessHeight = (animatedHeight - limitHeight - gap / 2).coerceAtLeast(4.dp)

                                        Column(
                                            modifier = Modifier.width(barWidth),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(excessHeight)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(excessColor)
                                            )
                                            Spacer(modifier = Modifier.height(gap))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(baseHeight)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(baseColor)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .width(barWidth)
                                                .height(animatedHeight.coerceAtLeast(4.dp))
                                                .clip(RoundedCornerShape(50))
                                                .background(baseColor)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        if (xAxisLabels.size == dataPoints.size) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                xAxisLabels.forEachIndexed { index, label ->
                                    val isHighlighted = selectedIndex == index
                                    val isToday = todayIndex == index
                                    val isFuture = todayIndex != null && index > todayIndex

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = if (xAxisLabels.size > 7) 10.sp else 11.sp,
                                                letterSpacing = if (xAxisLabels.size > 7) (-0.3).sp else 0.sp,
                                                fontWeight = if (isHighlighted || isToday) FontWeight.ExtraBold else FontWeight.Medium
                                            ),
                                            color = when {
                                                isFuture -> onSurfaceVariant.copy(alpha = 0.3f)
                                                isHighlighted -> primaryColor
                                                isToday -> primaryColor.copy(alpha = 0.85f)
                                                else -> onSurfaceVariant.copy(alpha = 0.7f)
                                            },
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                            textAlign = TextAlign.Center
                                        )
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 3.dp)
                                                .size(width = 12.dp, height = 3.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                    if (isToday) primaryColor else Color.Transparent
                                                )
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                xAxisLabels.forEach { label ->
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = onSurfaceVariant.copy(alpha = 0.7f),
                                        maxLines = 1
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
