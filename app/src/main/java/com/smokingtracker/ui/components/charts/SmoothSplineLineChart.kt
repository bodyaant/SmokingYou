package com.smokingtracker.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.ceil

@Composable
fun SmoothSplineLineChart(
    dataPoints: List<Int>,
    selectedIndex: Int?,
    onSelectIndex: (Int?) -> Unit,
    dailyLimit: Int,
    showLimit: Boolean,
    xAxisLabels: List<String>,
    modifier: Modifier = Modifier,
    todayIndex: Int? = null,
    vibrationEnabled: Boolean = true,
    chartHeight: Dp = 185.dp
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val density = LocalDensity.current

    val palette = rememberChartColorPalette()
    val primaryColor = palette.primary
    val errorColor = palette.error
    val outlineVariant = palette.outlineVariant
    val onSurfaceVariant = palette.onSurfaceVariant

    val isLimitApplicable = showLimit && dailyLimit > 0
    val rawMax = (dataPoints.maxOrNull() ?: 0).coerceAtLeast(if (isLimitApplicable) dailyLimit else 1)
    val maxY = (rawMax * 1.25f).coerceAtLeast(4f)

    val gridLevels = 3
    val stepValue = ceil(maxY / gridLevels).toInt().coerceAtLeast(1)
    val maxGridValue = stepValue * gridLevels

    val lineAnimProgress = remember(dataPoints) { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        lineAnimProgress.snapTo(0f)
        lineAnimProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(750, easing = FastOutSlowInEasing)
        )
    }

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

    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentTodayIndex by rememberUpdatedState(todayIndex)
    val currentOnSelectIndex by rememberUpdatedState(onSelectIndex)
    val currentVibrationEnabled by rememberUpdatedState(vibrationEnabled)

    val splinePath = remember { Path() }
    val fillPath = remember { Path() }

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
                                color = outlineVariant.copy(alpha = 0.2f),
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
                    val limitY = size.height * (1f - (dailyLimit.toFloat() / maxGridValue).coerceIn(0f, 1f))
                    drawLine(
                        color = errorColor.copy(alpha = 0.65f),
                        start = Offset(0f, limitY),
                        end = Offset(size.width, limitY),
                        strokeWidth = 1.6.dp.toPx(),
                        pathEffect = LimitDashPathEffect
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp)
                    .clipToBounds()
            ) {
                if (isScrollable) {
                    val n = dataPoints.size
                    val slotWidth = 34.dp
                    val spacing = 8.dp
                    val hPadding = 8.dp
                    val totalContentWidth = hPadding * 2 + slotWidth * n + spacing * (n - 1).coerceAtLeast(0)

                    Column(
                        modifier = Modifier.horizontalScroll(scrollState)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(totalContentWidth)
                                .height(barAreaHeight)
                                .pointerInput(dataPoints) {
                                    detectTapGestures(
                                        onTap = { offset ->
                                            if (n > 0) {
                                                val hPaddingPx = 8.dp.toPx()
                                                val stridePx = (34.dp + 8.dp).toPx()
                                                val idx = ((offset.x - hPaddingPx) / stridePx).toInt().coerceIn(0, n - 1)
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
                        ) {
                            if (n == 0) return@Canvas
                            val height = size.height
                            val progress = lineAnimProgress.value
                            val hPaddingPx = hPadding.toPx()
                            val stridePx = (slotWidth + spacing).toPx()
                            val halfSlotPx = (slotWidth / 2).toPx()

                            val points = dataPoints.mapIndexed { index, value ->
                                val x = hPaddingPx + index * stridePx + halfSlotPx
                                val targetY = height * (1f - (value.toFloat() / maxGridValue).coerceIn(0f, 1f))
                                val animatedY = height - (height - targetY) * progress
                                Offset(x, animatedY)
                            }

                            if (n >= 2) {
                                splinePath.rewind()
                                fillPath.rewind()

                                splinePath.moveTo(points[0].x, points[0].y)
                                fillPath.moveTo(points[0].x, height)
                                fillPath.lineTo(points[0].x, points[0].y)

                                for (i in 0 until n - 1) {
                                    val p0 = if (i > 0) points[i - 1] else points[i]
                                    val p1 = points[i]
                                    val p2 = points[i + 1]
                                    val p3 = if (i + 2 < n) points[i + 2] else p2

                                    val cp1x = p1.x + (p2.x - p0.x) / 6f
                                    val cp1y = (p1.y + (p2.y - p0.y) / 6f).coerceIn(0f, height)
                                    val cp2x = p2.x - (p3.x - p1.x) / 6f
                                    val cp2y = (p2.y - (p3.y - p1.y) / 6f).coerceIn(0f, height)

                                    splinePath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                                    fillPath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                                }

                                fillPath.lineTo(points.last().x, height)
                                fillPath.close()

                                val gradientBrush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.35f * progress),
                                        primaryColor.copy(alpha = 0.02f)
                                    ),
                                    startY = 0f,
                                    endY = height
                                )
                                drawPath(path = fillPath, brush = gradientBrush)

                                drawPath(
                                    path = splinePath,
                                    color = primaryColor,
                                    style = Stroke(
                                        width = 2.8.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            points.forEachIndexed { index, pt ->
                                val isSelected = selectedIndex == index
                                if (isSelected) {
                                    drawLine(
                                        color = primaryColor.copy(alpha = 0.45f),
                                        start = Offset(pt.x, 0f),
                                        end = Offset(pt.x, height),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = SelectionDashPathEffect
                                    )
                                    drawCircle(
                                        color = primaryColor.copy(alpha = 0.25f),
                                        radius = 8.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = primaryColor,
                                        radius = 4.5.dp.toPx(),
                                        center = pt
                                    )
                                } else if (n == 1) {
                                    drawCircle(
                                        color = primaryColor,
                                        radius = 4.5.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .width(totalContentWidth)
                                .padding(horizontal = hPadding),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            xAxisLabels.forEachIndexed { index, label ->
                                val isHighlighted = selectedIndex == index
                                val isToday = todayIndex == index
                                val isFuture = todayIndex != null && index > todayIndex
                                val pointValue = dataPoints.getOrElse(index) { 0 }
                                Column(
                                    modifier = Modifier
                                        .width(slotWidth)
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "$label: $pointValue"
                                            selected = isHighlighted
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
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isHighlighted || isToday) FontWeight.ExtraBold else FontWeight.Medium
                                        ),
                                        color = when {
                                            isHighlighted -> primaryColor
                                            isToday -> primaryColor.copy(alpha = 0.85f)
                                            isFuture -> onSurfaceVariant.copy(alpha = 0.3f)
                                            else -> onSurfaceVariant.copy(alpha = 0.7f)
                                        },
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip
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
                    }
                } else {
                    val n = dataPoints.size
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barAreaHeight)
                                .pointerInput(dataPoints) {
                                    coroutineScope {
                                        launch {
                                            detectTapGestures(
                                                onTap = { offset ->
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
                                                    if (n > 0 && size.width > 0) {
                                                        val idx = ((change.position.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                                        val tIdx = currentTodayIndex
                                                        if ((tIdx == null || idx <= tIdx) && idx != currentSelectedIndex) {
                                                            currentOnSelectIndex(idx)
                                                            HapticFeedbackHelper.performTick(currentVibrationEnabled, haptic, context)
                                                        }
                                                    }
                                                },
                                                onDragEnd = {},
                                                onDragCancel = {}
                                            )
                                        }
                                    }
                                }
                        ) {
                            if (n == 0) return@Canvas
                            val width = size.width
                            val height = size.height
                            val progress = lineAnimProgress.value
                            val slotWidthPx = width / n.toFloat()

                            val points = dataPoints.mapIndexed { index, value ->
                                val x = (index + 0.5f) * slotWidthPx
                                val targetY = height * (1f - (value.toFloat() / maxGridValue).coerceIn(0f, 1f))
                                val animatedY = height - (height - targetY) * progress
                                Offset(x, animatedY)
                            }

                            if (n >= 2) {
                                splinePath.rewind()
                                fillPath.rewind()

                                splinePath.moveTo(points[0].x, points[0].y)
                                fillPath.moveTo(points[0].x, height)
                                fillPath.lineTo(points[0].x, points[0].y)

                                for (i in 0 until n - 1) {
                                    val p0 = if (i > 0) points[i - 1] else points[i]
                                    val p1 = points[i]
                                    val p2 = points[i + 1]
                                    val p3 = if (i + 2 < n) points[i + 2] else p2

                                    val cp1x = p1.x + (p2.x - p0.x) / 6f
                                    val cp1y = (p1.y + (p2.y - p0.y) / 6f).coerceIn(0f, height)
                                    val cp2x = p2.x - (p3.x - p1.x) / 6f
                                    val cp2y = (p2.y - (p3.y - p1.y) / 6f).coerceIn(0f, height)

                                    splinePath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                                    fillPath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                                }

                                fillPath.lineTo(points.last().x, height)
                                fillPath.close()

                                val gradientBrush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.35f * progress),
                                        primaryColor.copy(alpha = 0.02f)
                                    ),
                                    startY = 0f,
                                    endY = height
                                )
                                drawPath(path = fillPath, brush = gradientBrush)

                                drawPath(
                                    path = splinePath,
                                    color = primaryColor,
                                    style = Stroke(
                                        width = 2.8.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            points.forEachIndexed { index, pt ->
                                val isSelected = selectedIndex == index
                                if (isSelected) {
                                    drawLine(
                                        color = primaryColor.copy(alpha = 0.45f),
                                        start = Offset(pt.x, 0f),
                                        end = Offset(pt.x, height),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = SelectionDashPathEffect
                                    )
                                    drawCircle(
                                        color = primaryColor.copy(alpha = 0.25f),
                                        radius = 8.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = primaryColor,
                                        radius = 4.5.dp.toPx(),
                                        center = pt
                                    )
                                } else if (n <= 14) {
                                    val isFuture = todayIndex != null && index > todayIndex
                                    drawCircle(
                                        color = if (isFuture) primaryColor.copy(alpha = 0.25f) else primaryColor,
                                        radius = if (n == 1) 4.5.dp.toPx() else 2.5.dp.toPx(),
                                        center = pt
                                    )
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
                                    val pointValue = dataPoints.getOrElse(index) { 0 }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics(mergeDescendants = true) {
                                                contentDescription = "$label: $pointValue"
                                                selected = isHighlighted
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
