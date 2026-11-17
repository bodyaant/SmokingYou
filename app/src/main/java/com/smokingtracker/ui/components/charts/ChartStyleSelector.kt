package com.smokingtracker.ui.components.charts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.rememberBouncyPress
import com.smokingtracker.ui.theme.subContainer

@Composable
fun ChartStyleSelector(
    selectedStyle: ChartViewStyle,
    onStyleSelected: (ChartViewStyle) -> Unit,
    modifier: Modifier = Modifier,
    showCalendarOption: Boolean = false,
    vibrationEnabled: Boolean = true
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
        color = MaterialTheme.colorScheme.subContainer
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
                    .offset { IntOffset((buttonWidth * animatedTabPosition).roundToPx(), 0) }
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
