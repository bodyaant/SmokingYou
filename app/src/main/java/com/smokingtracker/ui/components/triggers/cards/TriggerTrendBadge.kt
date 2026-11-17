package com.smokingtracker.ui.components.triggers.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.components.charts.rememberChartColorPalette
import com.smokingtracker.ui.components.triggers.models.TriggerTrendData
import com.smokingtracker.ui.components.triggers.models.TriggerTrendType

@Composable
fun TriggerTrendBadge(
    trend: TriggerTrendData,
    modifier: Modifier = Modifier
) {
    val chartPalette = rememberChartColorPalette()
    when (trend.type) {
        TriggerTrendType.INCREASED -> {
            Surface(
                shape = CircleShape,
                color = chartPalette.errorContainer,
                contentColor = chartPalette.onErrorContainer,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = stringResource(R.string.trigger_trend_increase, trend.delta),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        TriggerTrendType.DECREASED -> {
            Surface(
                shape = CircleShape,
                color = chartPalette.cleanDayContainer,
                contentColor = chartPalette.onCleanDayContainer,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = stringResource(R.string.trigger_trend_decrease, trend.delta),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        TriggerTrendType.NEW -> {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = modifier
            ) {
                Text(
                    text = stringResource(R.string.trigger_trend_new),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        TriggerTrendType.SAME, TriggerTrendType.NONE -> Unit
    }
}
