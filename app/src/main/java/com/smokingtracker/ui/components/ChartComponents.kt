package com.smokingtracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

enum class ChartViewStyle {
    BARS,
    LINE,
    CALENDAR
}

data class ChartColorPalette(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val cleanDay: Color,
    val cleanDayContainer: Color,
    val onCleanDayContainer: Color,
    val outlineVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceContainerHighest: Color,
    val tertiary: Color = cleanDay
)

@Composable
fun rememberChartColorPalette(): ChartColorPalette {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val error = MaterialTheme.colorScheme.error
    val errorContainer = MaterialTheme.colorScheme.errorContainer
    val onErrorContainer = MaterialTheme.colorScheme.onErrorContainer
    val tertiary = MaterialTheme.colorScheme.tertiary
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    val onTertiaryContainer = MaterialTheme.colorScheme.onTertiaryContainer
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    return remember(primary, error, tertiary, isDark) {
        val hsvPrimary = FloatArray(3)
        val hsvError = FloatArray(3)
        android.graphics.Color.RGBToHSV(
            (primary.red * 255).toInt(),
            (primary.green * 255).toInt(),
            (primary.blue * 255).toInt(),
            hsvPrimary
        )
        android.graphics.Color.RGBToHSV(
            (error.red * 255).toInt(),
            (error.green * 255).toInt(),
            (error.blue * 255).toInt(),
            hsvError
        )

        val rawDiff = kotlin.math.abs(hsvPrimary[0] - hsvError[0])
        val hueDistance = if (rawDiff > 180f) 360f - rawDiff else rawDiff
        val isRedConflict = hueDistance < 42f

        val effectiveError = if (isRedConflict) {
            if (isDark) Color(0xFFFF9E40) else Color(0xFFE65100)
        } else error

        val effectiveErrorContainer = if (isRedConflict) {
            if (isDark) Color(0xFF5D2800) else Color(0xFFFFCC80)
        } else errorContainer

        val effectiveOnErrorContainer = if (isRedConflict) {
            if (isDark) Color(0xFFFFDCC1) else Color(0xFF431B00)
        } else onErrorContainer

        ChartColorPalette(
            primary = primary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            error = effectiveError,
            errorContainer = effectiveErrorContainer,
            onErrorContainer = effectiveOnErrorContainer,
            cleanDay = tertiary,
            cleanDayContainer = tertiaryContainer,
            onCleanDayContainer = onTertiaryContainer,
            outlineVariant = outlineVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerHighest = surfaceContainerHighest,
            tertiary = tertiary
        )
    }
}

@Composable
fun SealBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    background: Color = MaterialTheme.colorScheme.primaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = background,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(size * 0.62f)
            )
        }
    }
}

@Composable
fun ChartStyleSelector(
    selectedStyle: ChartViewStyle,
    onStyleSelected: (ChartViewStyle) -> Unit,
    showCalendarOption: Boolean = false,
    vibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val activeIndex = when (selectedStyle) {
        ChartViewStyle.BARS -> 0
        ChartViewStyle.LINE -> 1
        ChartViewStyle.CALENDAR -> if (showCalendarOption) 2 else 0
    }
    val animatedTabPosition by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = 450f
        ),
        label = "chartStyleTabIndicator"
    )

    Surface(
        modifier = modifier.height(38.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        val buttonWidth = 36.dp
        Box(
            modifier = Modifier
                .padding(3.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .width(buttonWidth)
                    .fillMaxHeight()
                    .offset(x = buttonWidth * animatedTabPosition)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartStyleIconButton(
                    icon = Icons.Filled.BarChart,
                    contentDescription = stringResource(R.string.chart_view_bars),
                    isSelected = selectedStyle == ChartViewStyle.BARS,
                    buttonWidth = buttonWidth,
                    vibrationEnabled = vibrationEnabled,
                    onClick = { onStyleSelected(ChartViewStyle.BARS) }
                )

                ChartStyleIconButton(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = stringResource(R.string.chart_view_line),
                    isSelected = selectedStyle == ChartViewStyle.LINE,
                    buttonWidth = buttonWidth,
                    vibrationEnabled = vibrationEnabled,
                    onClick = { onStyleSelected(ChartViewStyle.LINE) }
                )

                AnimatedVisibility(
                    visible = showCalendarOption,
                    enter = expandHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = 380f
                        ),
                        expandFrom = Alignment.Start
                    ) + fadeIn(
                        animationSpec = tween(180)
                    ) + scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = 380f
                        ),
                        initialScale = 0.35f,
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    ),
                    exit = shrinkHorizontally(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = 450f
                        ),
                        shrinkTowards = Alignment.Start
                    ) + fadeOut(
                        animationSpec = tween(120)
                    ) + scaleOut(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = 450f
                        ),
                        targetScale = 0.35f,
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    )
                ) {
                    ChartStyleIconButton(
                        icon = Icons.Filled.CalendarMonth,
                        contentDescription = stringResource(R.string.chart_view_calendar),
                        isSelected = selectedStyle == ChartViewStyle.CALENDAR,
                        buttonWidth = buttonWidth,
                        vibrationEnabled = vibrationEnabled,
                        onClick = { onStyleSelected(ChartViewStyle.CALENDAR) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartStyleIconButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    buttonWidth: Dp,
    vibrationEnabled: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.86f)

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        animationSpec = tween(180),
        label = "chartStyleIconTint"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = 500f
        ),
        label = "chartStyleIconScale"
    )

    Box(
        modifier = Modifier
            .size(buttonWidth, 32.dp)
            .clip(CircleShape)
            .bouncyPress(bouncy)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
        )
    }
}

@Composable
fun PillBarChart(
    dataPoints: List<Int>,
    selectedIndex: Int?,
    onSelectIndex: (Int?) -> Unit,
    dailyLimit: Int,
    showLimit: Boolean,
    xAxisLabels: List<String>,
    todayIndex: Int? = null,
    cleanDayIndices: Set<Int> = emptySet(),
    vibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier,
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
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
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

                            Column(
                                modifier = Modifier
                                    .width(34.dp)
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

                                val label = xAxisLabels.getOrElse(index) { "$index" }
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
                                detectTapGestures(
                                    onTap = { offset ->
                                        val n = dataPoints.size
                                        if (n > 0) {
                                            val idx = ((offset.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                            if (todayIndex == null || idx <= todayIndex) {
                                                val newSel = if (selectedIndex == idx) null else idx
                                                onSelectIndex(newSel)
                                                if (newSel != null) {
                                                    HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            .pointerInput(dataPoints) {
                                detectHorizontalDragGestures(
                                    onDragStart = { offset ->
                                        val n = dataPoints.size
                                        if (n > 0) {
                                            val idx = ((offset.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                            if (todayIndex == null || idx <= todayIndex) {
                                                onSelectIndex(idx)
                                                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                            }
                                        }
                                    },
                                    onHorizontalDrag = { change, _ ->
                                        change.consume()
                                        val n = dataPoints.size
                                        if (n > 0) {
                                            val idx = ((change.position.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                            if (todayIndex == null || idx <= todayIndex) {
                                                if (idx != selectedIndex) {
                                                    onSelectIndex(idx)
                                                    HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barAreaHeight),
                            horizontalArrangement = Arrangement.spacedBy(
                                when {
                                    dataPoints.size <= 7 -> 10.dp
                                    dataPoints.size <= 12 -> 6.dp
                                    else -> 2.dp
                                }
                            ),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            dataPoints.forEachIndexed { index, count ->
                                val isSelected = selectedIndex == index
                                val isToday = todayIndex == index
                                val isFuture = todayIndex != null && index > todayIndex
                                val isOverLimit = !isFuture && isLimitApplicable && count > dailyLimit
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

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    if (isOverLimit && animatedHeight > limitHeight + gap / 2) {
                                        val baseHeight = (limitHeight - gap / 2).coerceAtLeast(6.dp)
                                        val excessHeight = (animatedHeight - limitHeight - gap / 2).coerceAtLeast(4.dp)

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

@Composable
fun SmoothSplineLineChart(
    dataPoints: List<Int>,
    selectedIndex: Int?,
    onSelectIndex: (Int?) -> Unit,
    dailyLimit: Int,
    showLimit: Boolean,
    xAxisLabels: List<String>,
    todayIndex: Int? = null,
    vibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier,
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
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
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
                                .pointerInput(dataPoints, todayIndex) {
                                    detectTapGestures(
                                        onTap = { offset ->
                                            if (n > 0) {
                                                val hPaddingPx = 8.dp.toPx()
                                                val stridePx = (34.dp + 8.dp).toPx()
                                                val idx = ((offset.x - hPaddingPx) / stridePx).toInt().coerceIn(0, n - 1)
                                                if (todayIndex == null || idx <= todayIndex) {
                                                    val newSel = if (selectedIndex == idx) null else idx
                                                    onSelectIndex(newSel)
                                                    if (newSel != null) {
                                                        HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                        ) {
                            if (n < 2) return@Canvas
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

                            val splinePath = Path()
                            val fillPath = Path()

                            splinePath.moveTo(points[0].x, points[0].y)
                            fillPath.moveTo(points[0].x, height)
                            fillPath.lineTo(points[0].x, points[0].y)

                            for (i in 0 until n - 1) {
                                val p0 = if (i > 0) points[i - 1] else points[i]
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                val p3 = if (i + 2 < n) points[i + 2] else p2

                                val cp1x = p1.x + (p2.x - p0.x) / 6f
                                val cp1y = p1.y + (p2.y - p0.y) / 6f
                                val cp2x = p2.x - (p3.x - p1.x) / 6f
                                val cp2y = p2.y - (p3.y - p1.y) / 6f

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

                            points.forEachIndexed { index, pt ->
                                val isSelected = selectedIndex == index
                                if (isSelected) {
                                    drawLine(
                                        color = primaryColor.copy(alpha = 0.45f),
                                        start = Offset(pt.x, 0f),
                                        end = Offset(pt.x, height),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
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
                                Column(
                                    modifier = Modifier
                                        .width(slotWidth)
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
                                        softWrap = false
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
                                .pointerInput(dataPoints, todayIndex) {
                                    detectTapGestures(
                                        onTap = { offset ->
                                            if (n > 0) {
                                                val idx = ((offset.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                                if (todayIndex == null || idx <= todayIndex) {
                                                    val newSel = if (selectedIndex == idx) null else idx
                                                    onSelectIndex(newSel)
                                                    if (newSel != null) {
                                                        HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                                .pointerInput(dataPoints, todayIndex) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { offset ->
                                            if (n > 0) {
                                                val idx = ((offset.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                                if (todayIndex == null || idx <= todayIndex) {
                                                    onSelectIndex(idx)
                                                    HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                                }
                                            }
                                        },
                                        onHorizontalDrag = { change, _ ->
                                            change.consume()
                                            if (n > 0) {
                                                val idx = ((change.position.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                                if ((todayIndex == null || idx <= todayIndex) && idx != selectedIndex) {
                                                    onSelectIndex(idx)
                                                    HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                                }
                                            }
                                        },
                                        onDragEnd = {},
                                        onDragCancel = {}
                                    )
                                }
                        ) {
                            if (n < 2) return@Canvas
                            val width = size.width
                            val height = size.height
                            val progress = lineAnimProgress.value

                            val points = dataPoints.mapIndexed { index, value ->
                                val x = (index.toFloat() / (n - 1).toFloat()) * width
                                val targetY = height * (1f - (value.toFloat() / maxGridValue).coerceIn(0f, 1f))
                                val animatedY = height - (height - targetY) * progress
                                Offset(x, animatedY)
                            }

                            val splinePath = Path()
                            val fillPath = Path()

                            splinePath.moveTo(points[0].x, points[0].y)
                            fillPath.moveTo(points[0].x, height)
                            fillPath.lineTo(points[0].x, points[0].y)

                            for (i in 0 until n - 1) {
                                val p0 = if (i > 0) points[i - 1] else points[i]
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                val p3 = if (i + 2 < n) points[i + 2] else p2

                                val cp1x = p1.x + (p2.x - p0.x) / 6f
                                val cp1y = p1.y + (p2.y - p0.y) / 6f
                                val cp2x = p2.x - (p3.x - p1.x) / 6f
                                val cp2y = p2.y - (p3.y - p1.y) / 6f

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

                            points.forEachIndexed { index, pt ->
                                val isSelected = selectedIndex == index
                                if (isSelected) {
                                    drawLine(
                                        color = primaryColor.copy(alpha = 0.45f),
                                        start = Offset(pt.x, 0f),
                                        end = Offset(pt.x, height),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
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
                                        radius = 2.5.dp.toPx(),
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
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
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
                                                fontWeight = if (isHighlighted || isToday) FontWeight.ExtraBold else FontWeight.Medium
                                            ),
                                            color = when {
                                                isHighlighted -> primaryColor
                                                isToday -> primaryColor.copy(alpha = 0.85f)
                                                isFuture -> onSurfaceVariant.copy(alpha = 0.3f)
                                                else -> onSurfaceVariant.copy(alpha = 0.7f)
                                            },
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

@Composable
fun MonthCalendarHeatmap(
    year: Int,
    month: Int,
    countsByDay: Map<Int, Int>,
    dailyLimit: Int,
    selectedDay: Int?,
    onSelectDay: (Int) -> Unit,
    todayDay: Int? = null,
    vibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val palette = rememberChartColorPalette()
    val primaryColor = palette.primary
    val primaryContainer = palette.primaryContainer
    val tertiaryColor = palette.cleanDay
    val tertiaryContainer = palette.cleanDayContainer
    val errorColor = palette.error
    val errorContainer = palette.errorContainer
    val surfaceContainerHighest = palette.surfaceContainerHighest
    val onSurfaceVariant = palette.onSurfaceVariant

    val todayCal = remember { Calendar.getInstance() }
    val isCurrentMonth = year == todayCal.get(Calendar.YEAR) && (month - 1) == todayCal.get(Calendar.MONTH)
    val isFutureMonth = year > todayCal.get(Calendar.YEAR) || (year == todayCal.get(Calendar.YEAR) && (month - 1) > todayCal.get(Calendar.MONTH))
    val currentDayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH)

    val cal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val rawFirstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val leadingBlanks = (rawFirstDayOfWeek + 5) % 7

    val maxCount = remember(countsByDay, dailyLimit) {
        val observed = countsByDay.values.maxOrNull() ?: 1
        maxOf(observed, if (dailyLimit > 0) dailyLimit else 1).coerceAtLeast(1)
    }

    val dayHeaders = remember {
        listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        ).map { it.getDisplayName(JavaTextStyle.NARROW, Locale.getDefault()) }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            dayHeaders.forEach { header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceVariant.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val totalSlots = leadingBlanks + daysInMonth
        val rows = (totalSlots + 6) / 7
        var currentDay = 1

        repeat(rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(7) { colIndex ->
                    val slotIndex = it * 7 + colIndex
                    if (slotIndex < leadingBlanks || currentDay > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val dayNumber = currentDay
                        val isFuture = when {
                            isFutureMonth -> true
                            isCurrentMonth -> dayNumber > currentDayOfMonth
                            todayDay != null -> dayNumber > todayDay
                            else -> false
                        }

                        val count = countsByDay[dayNumber] ?: 0
                        val hasData = countsByDay.containsKey(dayNumber)
                        val isSelected = !isFuture && selectedDay == dayNumber
                        val isToday = todayDay == dayNumber || (isCurrentMonth && dayNumber == currentDayOfMonth)
                        val isOverLimit = !isFuture && dailyLimit > 0 && count > dailyLimit
                        val isCleanDay = !isFuture && hasData && count == 0

                        val intensity = (count.toFloat() / maxCount).coerceIn(0f, 1f)

                        val cellColor = when {
                            isFuture -> surfaceContainerHighest.copy(alpha = 0.18f)
                            !hasData -> surfaceContainerHighest.copy(alpha = 0.35f)
                            isCleanDay -> tertiaryContainer.copy(alpha = 0.6f)
                            isOverLimit -> errorContainer.copy(alpha = 0.75f)
                            intensity <= 0.05f -> surfaceContainerHighest
                            else -> lerp(primaryContainer, primaryColor, intensity * 0.85f)
                        }

                        val textColor = when {
                            isFuture -> onSurfaceVariant.copy(alpha = 0.25f)
                            !hasData -> onSurfaceVariant.copy(alpha = 0.4f)
                            isCleanDay -> tertiaryColor
                            isOverLimit -> errorColor
                            intensity > 0.65f -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        CalendarDayCell(
                            dayNumber = dayNumber,
                            isFuture = isFuture,
                            isSelected = isSelected,
                            isToday = isToday,
                            targetCellColor = cellColor,
                            textColor = textColor,
                            primaryColor = primaryColor,
                            vibrationEnabled = vibrationEnabled,
                            onSelectDay = onSelectDay,
                            modifier = Modifier.weight(1f)
                        )
                        currentDay++
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    isFuture: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    targetCellColor: Color,
    textColor: Color,
    primaryColor: Color,
    vibrationEnabled: Boolean,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.90f)

    val cornerPercent by animateIntAsState(
        targetValue = if (isSelected) 50 else 24,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "calendarCornerPercent"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "calendarCellScale"
    )

    val cellColor by animateColorAsState(
        targetValue = targetCellColor,
        animationSpec = tween(220),
        label = "calendarCellColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else if (isToday) 1.2.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "calendarBorderWidth"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else if (isToday) primaryColor.copy(alpha = 0.6f) else Color.Transparent,
        animationSpec = tween(200),
        label = "calendarBorderColor"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .zIndex(if (isSelected) 1f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(percent = cornerPercent))
            .background(cellColor)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, RoundedCornerShape(percent = cornerPercent))
                } else Modifier
            )
            .bouncyPress(bouncy)
            .clickable(
                enabled = !isFuture,
                interactionSource = interactionSource,
                indication = null
            ) {
                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                onSelectDay(dayNumber)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$dayNumber",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}

@Composable
fun DayPartsStackedBar(
    nightCount: Int,
    morningCount: Int,
    afternoonCount: Int,
    eveningCount: Int,
    selectedQuadrant: Int? = null,
    onSelectQuadrant: (Int?) -> Unit = {},
    vibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val total = (nightCount + morningCount + afternoonCount + eveningCount).coerceAtLeast(1)

    val quadrants = listOf(
        QuadrantInfo(0, stringResource(R.string.day_part_night), "🌙", nightCount, MaterialTheme.colorScheme.tertiary),
        QuadrantInfo(1, stringResource(R.string.day_part_morning), "🌅", morningCount, MaterialTheme.colorScheme.primaryContainer),
        QuadrantInfo(2, stringResource(R.string.day_part_afternoon), "☀️", afternoonCount, MaterialTheme.colorScheme.primary),
        QuadrantInfo(3, stringResource(R.string.day_part_evening), "🌆", eveningCount, MaterialTheme.colorScheme.secondary)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            quadrants.forEach { q ->
                if (q.count > 0) {
                    val isSelected = selectedQuadrant == q.index
                    val weight = (q.count.toFloat() / total).coerceAtLeast(0.04f)

                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(q.color.copy(alpha = if (isSelected) 1f else 0.82f))
                            .clickable {
                                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                onSelectQuadrant(if (selectedQuadrant == q.index) null else q.index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (weight > 0.12f) {
                            Text(
                                text = "${((q.count.toFloat() / total) * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (q.color == MaterialTheme.colorScheme.primaryContainer) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quadrants.forEach { q ->
                val isSelected = selectedQuadrant == q.index
                val percent = ((q.count.toFloat() / total) * 100).roundToInt()

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                            onSelectQuadrant(if (selectedQuadrant == q.index) null else q.index)
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = q.emoji,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${q.count}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private data class QuadrantInfo(
    val index: Int,
    val title: String,
    val emoji: String,
    val count: Int,
    val color: Color
)

@Composable
fun DayDetailCard(
    dateFormatted: String,
    count: Int,
    dailyLimit: Int,
    cost: Float = 0f,
    currency: String = "",
    avgIntervalMinutes: Int? = null,
    topTrigger: String? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCleanDay = count == 0
    val isLimitApplicable = dailyLimit > 0
    val isOverLimit = isLimitApplicable && count > dailyLimit
    val withinLimit = isLimitApplicable && count <= dailyLimit

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(22.dp)),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.chart_selected_day),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.btn_close),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val palette = rememberChartColorPalette()
            val (badgeBg, badgeFg, badgeText, badgeIcon) = when {
                isCleanDay -> Quadruple(
                    palette.cleanDayContainer,
                    palette.onCleanDayContainer,
                    stringResource(R.string.chart_clean_day),
                    Icons.Rounded.Star
                )
                isOverLimit -> Quadruple(
                    palette.errorContainer,
                    palette.onErrorContainer,
                    stringResource(R.string.graph_over_limit_badge, count - dailyLimit),
                    Icons.Default.LocalFireDepartment
                )
                withinLimit -> Quadruple(
                    palette.primaryContainer,
                    palette.onPrimaryContainer,
                    stringResource(R.string.graph_within_limit_badge),
                    Icons.Default.Shield
                )
                else -> Quadruple(
                    palette.surfaceContainerHighest,
                    MaterialTheme.colorScheme.onSurface,
                    stringResource(R.string.history_cigs_count_format, count.toString()),
                    Icons.Default.AccessTime
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = badgeBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeFg,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = badgeFg
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 1.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = stringResource(R.string.day_detail_cost),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (cost > 0f) String.format(Locale.getDefault(), "%.1f %s", cost, currency).trim() else "0 $currency".trim(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 1.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.day_detail_interval),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val intervalText = if (avgIntervalMinutes != null && avgIntervalMinutes > 0) {
                            val h = avgIntervalMinutes / 60
                            val m = avgIntervalMinutes % 60
                            if (h > 0) {
                                stringResource(R.string.format_hours_minutes, h, m)
                            } else {
                                stringResource(R.string.format_minutes_only, m)
                            }
                        } else "–"
                        Text(
                            text = intervalText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 1.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = stringResource(R.string.day_detail_triggers),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = topTrigger ?: stringResource(R.string.day_detail_no_triggers),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
