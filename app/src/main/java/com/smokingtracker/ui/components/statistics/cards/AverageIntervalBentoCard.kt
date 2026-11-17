package com.smokingtracker.ui.components.statistics.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import com.smokingtracker.ui.components.statistics.formatMinutesShort
import com.smokingtracker.ui.theme.ContainerIcon
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress
import com.smokingtracker.ui.theme.subContainer

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AverageIntervalBentoCard(
    intervalData: StatisticsManager.SmokingIntervalData,
    onClick: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.95f)

    val primaryColor = MaterialTheme.colorScheme.primary
    val noDataStr = stringResource(R.string.stats_no_data)
    val avgIntervalText = remember(intervalData.avgIntervalMinutes, noDataStr) {
        if (intervalData.avgIntervalMinutes > 0) formatMinutesShort(intervalData.avgIntervalMinutes, context)
        else noDataStr
    }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .bouncyPress(bouncy)
            .clickable(interactionSource = interactionSource, indication = null) {
                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                onClick()
            },
        shape = containerShape(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContainerIcon(
                    icon = Icons.Filled.HourglassEmpty,
                    tint = primaryColor,
                    backdropColor = primaryColor.copy(alpha = 0.18f),
                    size = 36.dp
                )
                if (intervalData.todayAvgMinutes != null && intervalData.todayAvgMinutes > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.subContainer
                    ) {
                        Text(
                            text = formatMinutesShort(intervalData.todayAvgMinutes, context),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else if (intervalData.totalIntervalsCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.subContainer
                    ) {
                        Text(
                            text = "${intervalData.totalIntervalsCount}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.stats_avg_interval_label),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = avgIntervalText,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            ) {
                val trackHeight = 4.dp.toPx()
                val midY = size.height / 2f
                drawLine(
                    color = primaryColor.copy(alpha = 0.2f),
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round
                )
                val spread = (intervalData.maxIntervalMinutes - intervalData.minIntervalMinutes).coerceAtLeast(1).toFloat()
                val dotFraction = if (intervalData.maxIntervalMinutes > intervalData.minIntervalMinutes) {
                    ((intervalData.avgIntervalMinutes - intervalData.minIntervalMinutes) / spread).coerceIn(0f, 1f)
                } else 0.5f
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(size.width * dotFraction, midY)
                )
            }
        }
    }
}
