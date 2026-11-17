package com.smokingtracker.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.domain.statistics.TaperingCalculator
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLimitBottomSheet(
    dailyLimit: Int,
    taperingPlanEnabled: Boolean = false,
    taperingIntervalDays: Int = TaperingCalculator.DEFAULT_INTERVAL_DAYS,
    showLimitOnGraph: Boolean = true,
    onSetDailyLimit: (Int) -> Unit,
    onSetTaperingPlanSettings: (Boolean, Int) -> Unit = { _, _ -> },
    onSetShowLimitOnGraph: (Boolean) -> Unit = {},
    onDismissRequest: () -> Unit,
    vibrationEnabled: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val currentLocale = LocalLocale.current.platformLocale

    var limitEnabled by rememberSaveable { mutableStateOf(dailyLimit > 0) }
    var limitValue by rememberSaveable {
        mutableIntStateOf(if (dailyLimit > 0) dailyLimit else TaperingCalculator.DEFAULT_DAILY_LIMIT)
    }
    var taperingEnabled by rememberSaveable { mutableStateOf(taperingPlanEnabled && dailyLimit > 0) }
    var taperingInterval by rememberSaveable {
        mutableIntStateOf(
            if (taperingIntervalDays in TaperingCalculator.MIN_INTERVAL_DAYS..TaperingCalculator.MAX_INTERVAL_DAYS) {
                taperingIntervalDays
            } else {
                TaperingCalculator.DEFAULT_INTERVAL_DAYS
            }
        )
    }
    var limitOnGraphEnabled by rememberSaveable { mutableStateOf(showLimitOnGraph) }

    val savedStr = stringResource(R.string.settings_saved)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = if (MaterialTheme.colorScheme.surfaceContainerLow == Color.White) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_daily_limit),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.daily_limit_bottom_sheet_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = containerShape(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            limitEnabled = !limitEnabled
                        }
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.daily_limit_switch_label),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (limitEnabled) {
                                stringResource(R.string.daily_limit_active_subtitle, limitValue)
                            } else {
                                stringResource(R.string.daily_limit_off_subtitle)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (limitEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = limitEnabled,
                        onCheckedChange = {
                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            limitEnabled = it
                        },
                        thumbContent = { SwitchThumb(limitEnabled) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = limitEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DailyLimitCounterCard(
                        limitValue = limitValue,
                        onLimitChanged = { limitValue = it },
                        vibrationEnabled = vibrationEnabled,
                        haptic = haptic,
                        context = context
                    )

                    DailyLimitPresetsRow(
                        currentLimit = limitValue,
                        onPresetSelected = { limitValue = it },
                        vibrationEnabled = vibrationEnabled,
                        haptic = haptic,
                        context = context
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = containerShape(RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    limitOnGraphEnabled = !limitOnGraphEnabled
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (limitOnGraphEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = if (limitOnGraphEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.BarChart,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.graph_setting_show_limit),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.graph_setting_show_limit_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = limitOnGraphEnabled,
                                onCheckedChange = {
                                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    limitOnGraphEnabled = it
                                },
                                thumbContent = { SwitchThumb(limitOnGraphEnabled) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = containerShape(RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                        taperingEnabled = !taperingEnabled
                                    }
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (taperingEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        contentColor = if (taperingEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = stringResource(R.string.tapering_plan_dialog_title),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (taperingEnabled) {
                                                stringResource(R.string.tapering_plan_subtitle_active, taperingInterval)
                                            } else {
                                                stringResource(R.string.tapering_plan_subtitle_off)
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (taperingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = taperingEnabled,
                                    onCheckedChange = {
                                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                        taperingEnabled = it
                                    },
                                    thumbContent = { SwitchThumb(taperingEnabled) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            AnimatedVisibility(
                                visible = taperingEnabled,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                        thickness = 1.dp
                                    )

                                    TaperingIntervalSelector(
                                        taperingInterval = taperingInterval,
                                        onIntervalChanged = { taperingInterval = it },
                                        vibrationEnabled = vibrationEnabled,
                                        haptic = haptic,
                                        context = context
                                    )

                                    val taperingPlan = remember(limitValue, taperingInterval, currentLocale) {
                                        TaperingCalculator.calculatePlan(
                                            initialLimit = limitValue,
                                            intervalDays = taperingInterval,
                                            locale = currentLocale
                                        )
                                    }

                                    TaperingChartPreview(plan = taperingPlan)
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = containerShape(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.daily_limit_info_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val finalLimit = if (limitEnabled) limitValue else 0
                    val finalTapering = limitEnabled && taperingEnabled
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    onSetDailyLimit(finalLimit)
                    onSetTaperingPlanSettings(finalTapering, taperingInterval)
                    onSetShowLimitOnGraph(limitOnGraphEnabled)
                    Toast.makeText(context, savedStr, Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.pack_save_button),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
