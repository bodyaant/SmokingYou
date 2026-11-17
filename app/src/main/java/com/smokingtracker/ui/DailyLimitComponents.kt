package com.smokingtracker.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokingtracker.R
import com.smokingtracker.domain.statistics.TaperingCalculator
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerShape
import kotlin.math.roundToInt

@Composable
fun DailyLimitCounterCard(
    limitValue: Int,
    onLimitChanged: (Int) -> Unit,
    vibrationEnabled: Boolean,
    haptic: HapticFeedback,
    context: Context,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = {
                        if (limitValue > TaperingCalculator.MIN_DAILY_LIMIT) {
                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            onLimitChanged(limitValue - 1)
                        }
                    },
                    enabled = limitValue > TaperingCalculator.MIN_DAILY_LIMIT,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = limitValue.toString(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.daily_limit_cigs_per_day),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        if (limitValue < TaperingCalculator.MAX_DAILY_LIMIT) {
                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            onLimitChanged(limitValue + 1)
                        }
                    },
                    enabled = limitValue < TaperingCalculator.MAX_DAILY_LIMIT,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }

            Slider(
                value = limitValue.toFloat().coerceIn(
                    TaperingCalculator.MIN_DAILY_LIMIT.toFloat(),
                    TaperingCalculator.MAX_DAILY_LIMIT.toFloat()
                ),
                onValueChange = {
                    val rounded = it.roundToInt().coerceIn(
                        TaperingCalculator.MIN_DAILY_LIMIT,
                        TaperingCalculator.MAX_DAILY_LIMIT
                    )
                    if (rounded != limitValue) {
                        onLimitChanged(rounded)
                        HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                    }
                },
                valueRange = TaperingCalculator.MIN_DAILY_LIMIT.toFloat()..TaperingCalculator.MAX_DAILY_LIMIT.toFloat(),
                steps = TaperingCalculator.MAX_DAILY_LIMIT - TaperingCalculator.MIN_DAILY_LIMIT - 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DailyLimitPresetsRow(
    currentLimit: Int,
    onPresetSelected: (Int) -> Unit,
    vibrationEnabled: Boolean,
    haptic: HapticFeedback,
    context: Context,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.daily_limit_presets),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TaperingCalculator.PRESET_LIMITS.forEach { preset ->
                val isSelected = currentLimit == preset
                Surface(
                    onClick = {
                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        onPresetSelected(preset)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = containerShape(RoundedCornerShape(12.dp)),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = preset.toString(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaperingIntervalSelector(
    taperingInterval: Int,
    onIntervalChanged: (Int) -> Unit,
    vibrationEnabled: Boolean,
    haptic: HapticFeedback,
    context: Context,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.tapering_plan_pace_label),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TaperingCalculator.PRESET_INTERVALS.forEach { intervalDaysOption ->
                val isSelected = taperingInterval == intervalDaysOption
                Surface(
                    onClick = {
                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        onIntervalChanged(intervalDaysOption)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = containerShape(RoundedCornerShape(10.dp)),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.tapering_plan_pace_days, intervalDaysOption),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        Slider(
            value = taperingInterval.toFloat().coerceIn(
                TaperingCalculator.MIN_INTERVAL_DAYS.toFloat(),
                TaperingCalculator.MAX_INTERVAL_DAYS.toFloat()
            ),
            onValueChange = {
                val rounded = it.roundToInt().coerceIn(
                    TaperingCalculator.MIN_INTERVAL_DAYS,
                    TaperingCalculator.MAX_INTERVAL_DAYS
                )
                if (rounded != taperingInterval) {
                    onIntervalChanged(rounded)
                    HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                }
            },
            valueRange = TaperingCalculator.MIN_INTERVAL_DAYS.toFloat()..TaperingCalculator.MAX_INTERVAL_DAYS.toFloat(),
            steps = TaperingCalculator.MAX_INTERVAL_DAYS - TaperingCalculator.MIN_INTERVAL_DAYS - 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
