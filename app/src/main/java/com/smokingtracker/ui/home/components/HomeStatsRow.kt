package com.smokingtracker.ui.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.containerShape

@Composable
fun HomeStatsRow(
    dailyCount: Int,
    weeklyCount: Int,
    monthlyCount: Int,
    vibrationEnabled: Boolean = true,
    onNavigateToGraphs: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = stringResource(R.string.stat_daily),
                value = dailyCount.toString(),
                onClick = { onNavigateToGraphs("daily") },
                vibrationEnabled = vibrationEnabled,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(28.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            StatItem(
                label = stringResource(R.string.stat_weekly),
                value = weeklyCount.toString(),
                onClick = { onNavigateToGraphs("weekly") },
                vibrationEnabled = vibrationEnabled,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(28.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            StatItem(
                label = stringResource(R.string.stat_monthly),
                value = monthlyCount.toString(),
                onClick = { onNavigateToGraphs("monthly") },
                vibrationEnabled = vibrationEnabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
