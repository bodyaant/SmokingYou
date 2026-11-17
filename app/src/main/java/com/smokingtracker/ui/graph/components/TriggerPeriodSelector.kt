package com.smokingtracker.ui.graph.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.graph.models.TriggerPeriod

@Composable
fun TriggerPeriodSelector(
    selectedPeriod: TriggerPeriod,
    onPeriodSelected: (TriggerPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(
        TriggerPeriod.ALL_TIME to stringResource(R.string.trigger_period_all),
        TriggerPeriod.MONTH to stringResource(R.string.trigger_period_month),
        TriggerPeriod.WEEK to stringResource(R.string.trigger_period_week)
    )

    val selectedIndex = selectedPeriod.ordinal
    val animatedSelectedTab by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 400f
        ),
        label = "triggerPeriodIndicatorOffset"
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    val indicatorBrush = remember(primaryColor) {
        Brush.horizontalGradient(
            colors = listOf(
                primaryColor,
                primaryColor.copy(alpha = 0.85f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(1f / periods.size)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = size.width * animatedSelectedTab
                }
                .clip(CircleShape)
                .background(
                    brush = indicatorBrush,
                    shape = CircleShape
                )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            periods.forEach { (period, title) ->
                val isSelected = selectedPeriod == period
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 200),
                    label = "triggerPeriodTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onPeriodSelected(period) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = textColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
