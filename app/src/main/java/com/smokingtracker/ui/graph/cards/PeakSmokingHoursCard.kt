package com.smokingtracker.ui.graph.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import com.smokingtracker.ui.components.charts.rememberChartColorPalette
import com.smokingtracker.ui.graph.sheets.toDisplayString
import com.smokingtracker.ui.theme.ContainerIcon
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress

@Composable
fun PeakSmokingHoursCard(
    distribution: StatisticsManager.HourlyDistributionData,
    vibrationEnabled: Boolean,
    onClick: () -> Unit
) {
    val chartPalette = rememberChartColorPalette()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.96f)

    val nightCount = distribution.nightCount
    val morningCount = distribution.morningCount
    val afternoonCount = distribution.afternoonCount
    val eveningCount = distribution.eveningCount
    val total = nightCount + morningCount + afternoonCount + eveningCount
    val periodText = distribution.peakPeriod.toDisplayString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyPress(bouncy)
            .clickable(interactionSource = interactionSource, indication = null) {
                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                onClick()
            },
        shape = containerShape(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ContainerIcon(
                icon = Icons.Filled.Bolt,
                tint = chartPalette.onPrimaryContainer,
                backdropColor = chartPalette.primaryContainer
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.peak_hours_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (total == 0 || distribution.peakPeriod == null) {
                    Text(
                        text = stringResource(R.string.peak_hours_no_data),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.peak_hours_summary, periodText, distribution.peakPeriodPercent),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(chartPalette.surfaceContainerHighest),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val safeTotal = total.coerceAtLeast(1).toFloat()
                        val qCounts = listOf(
                            nightCount to chartPalette.tertiary,
                            morningCount to chartPalette.primary.copy(alpha = 0.65f),
                            afternoonCount to chartPalette.primary,
                            eveningCount to chartPalette.cleanDay
                        )
                        qCounts.forEach { (count, color) ->
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight((count / safeTotal).coerceAtLeast(0.05f))
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
