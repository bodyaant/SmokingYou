package com.smokingtracker.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeDailyLimitSection(
    currentCount: Int,
    dailyLimit: Int,
    modifier: Modifier = Modifier
) {
    if (dailyLimit <= 0) return

    val progress = (currentCount.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)
    val isLimitReached = progress >= 1f

    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isLimitReached) Icons.Filled.Warning else Icons.Filled.SmokingRooms,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (isLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.home_daily_limit),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$currentCount / $dailyLimit",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    LinearWavyProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth(),
        amplitude = { if (isLimitReached) 0f else 1f },
        waveSpeed = if (isLimitReached) 0.dp else 16.dp,
        color = if (isLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}
