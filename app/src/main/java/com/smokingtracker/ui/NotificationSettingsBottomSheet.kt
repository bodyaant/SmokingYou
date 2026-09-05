package com.smokingtracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerBorder
import com.smokingtracker.ui.theme.containerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsBottomSheet(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    lowPriority: Boolean,
    onLowPriorityChange: (Boolean) -> Unit,
    showTimer: Boolean,
    onShowTimerChange: (Boolean) -> Unit,
    showProgress: Boolean,
    onShowProgressChange: (Boolean) -> Unit,
    showAddButton: Boolean,
    onShowAddButtonChange: (Boolean) -> Unit,
    showResistButton: Boolean,
    onShowResistButtonChange: (Boolean) -> Unit,
    dailyLimit: Int,
    onDismissRequest: () -> Unit,
    vibrationEnabled: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onEnabledChange(true)
        }
    }

    fun handleToggle(targetState: Boolean) {
        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
        if (targetState && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPerm) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onEnabledChange(true)
            }
        } else {
            onEnabledChange(targetState)
        }
    }

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
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.settings_ongoing_notification_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.settings_ongoing_notification_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Master Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = containerShape(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = containerBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { handleToggle(!enabled) }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.settings_ongoing_notification_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (enabled) {
                                    stringResource(
                                        R.string.notification_status_enabled_format,
                                        stringResource(if (lowPriority) R.string.notification_priority_low else R.string.notification_priority_default)
                                    )
                                } else {
                                    stringResource(R.string.notification_status_disabled)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Switch(
                        checked = enabled,
                        onCheckedChange = { handleToggle(it) },
                        thumbContent = { SwitchThumb(enabled) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Expanded Options & Interactive Live Preview
            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(150))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Live Preview Card
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.notification_preview_title),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        NotificationLivePreviewCard(
                            showTimer = showTimer,
                            showProgress = showProgress,
                            showAddButton = showAddButton,
                            showResistButton = showResistButton,
                            dailyLimit = dailyLimit
                        )
                    }

                    // Priority Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.notification_priority_label),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        val priorityOptions = listOf(
                            Triple(true, stringResource(R.string.notification_priority_low), Icons.Filled.NotificationsOff),
                            Triple(false, stringResource(R.string.notification_priority_default), Icons.Filled.NotificationsActive)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            priorityOptions.forEachIndexed { index, (isLow, title, icon) ->
                                val isSelected = lowPriority == isLow
                                val animatedWeight by animateFloatAsState(
                                    targetValue = if (isSelected) 1.12f else 1.0f,
                                    animationSpec = tween(200),
                                    label = "priority_weight_$index"
                                )
                                val startR by animateDpAsState(
                                    targetValue = if (isSelected || index == 0) 18.dp else 8.dp,
                                    animationSpec = tween(200),
                                    label = "priority_startR_$index"
                                )
                                val endR by animateDpAsState(
                                    targetValue = if (isSelected || index == priorityOptions.size - 1) 18.dp else 8.dp,
                                    animationSpec = tween(200),
                                    label = "priority_endR_$index"
                                )

                                Surface(
                                    onClick = {
                                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                        onLowPriorityChange(isLow)
                                    },
                                    modifier = Modifier
                                        .weight(animatedWeight)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(topStart = startR, bottomStart = startR, topEnd = endR, bottomEnd = endR),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    border = containerBorder(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        ) {
                                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Element toggles container
                    Surface(
                        shape = containerShape(RoundedCornerShape(20.dp)),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = containerBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            NotificationToggleRow(
                                title = stringResource(R.string.notification_show_timer),
                                checked = showTimer,
                                onCheckedChange = {
                                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    onShowTimerChange(it)
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            NotificationToggleRow(
                                title = stringResource(R.string.notification_show_progress),
                                checked = showProgress,
                                onCheckedChange = {
                                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    onShowProgressChange(it)
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            NotificationToggleRow(
                                title = stringResource(R.string.notification_show_add_button),
                                checked = showAddButton,
                                onCheckedChange = {
                                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    onShowAddButtonChange(it)
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            NotificationToggleRow(
                                title = stringResource(R.string.notification_show_resist_button),
                                checked = showResistButton,
                                onCheckedChange = {
                                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    onShowResistButtonChange(it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = { SwitchThumb(checked) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun NotificationLivePreviewCard(
    showTimer: Boolean,
    showProgress: Boolean,
    showAddButton: Boolean,
    showResistButton: Boolean,
    dailyLimit: Int
) {
    val sampleSmoked = if (dailyLimit > 0) (dailyLimit * 0.4f).toInt().coerceAtLeast(4) else 4
    val sampleLimit = if (dailyLimit > 0) dailyLimit else 10
    val samplePercent = if (sampleLimit > 0) (sampleSmoked.toFloat() / sampleLimit * 100).toInt() else 40

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = containerBorder(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Notification System Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cigarettebase),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.notification_title_app_name),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (showTimer) stringResource(R.string.notification_preview_timer_sample) else stringResource(R.string.widget_just_now),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Notification Content text
            val contentText = if (showProgress && sampleLimit > 0) {
                stringResource(R.string.notification_content_with_limit, sampleSmoked, sampleLimit, samplePercent)
            } else {
                stringResource(R.string.notification_content_no_limit, sampleSmoked)
            }
            val resistedPart = if (showResistButton) stringResource(R.string.notification_content_resisted_count, 2) else ""

            Text(
                text = contentText + resistedPart,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Progress Bar
            if (showProgress && sampleLimit > 0) {
                LinearProgressIndicator(
                    progress = { samplePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    drawStopIndicator = {}
                )
            }

            // Action Buttons
            if (showAddButton || showResistButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showAddButton) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.primary,
                            border = containerBorder(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_cigarettebase),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = stringResource(R.string.notification_action_add),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (showResistButton) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.primary,
                            border = containerBorder(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_crosscigarette),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = stringResource(R.string.notification_action_resist),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
