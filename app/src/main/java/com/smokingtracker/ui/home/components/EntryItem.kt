package com.smokingtracker.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.data.TriggerItem
import com.smokingtracker.data.TriggerType
import com.smokingtracker.ui.TimePickerDialog
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

fun getTriggerIcon(triggerKey: String?): ImageVector {
    if (triggerKey == null) return Icons.Filled.SmokingRooms
    val trigger = TriggerType.fromKey(triggerKey)
    return when (trigger) {
        TriggerType.STRESS -> Icons.Filled.SentimentDissatisfied
        TriggerType.BOREDOM -> Icons.Filled.HourglassEmpty
        TriggerType.SOCIAL -> Icons.Filled.People
        TriggerType.ROUTINE -> Icons.Filled.Repeat
        TriggerType.FOOD_COFFEE -> Icons.Filled.LocalCafe
        TriggerType.ALCOHOL -> Icons.Filled.LocalBar
        null -> Icons.Filled.Psychology
    }
}

@Preview(showBackground = true)
@Composable
fun EntryItemPreview() {
    EntryItem()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EntryItem(
    entryTime: Long = System.currentTimeMillis(),
    prevEntryTime: Long? = null,
    isResisted: Boolean = false,
    isOverLimit: Boolean = false,
    index: Int = 0,
    trigger: String? = null,
    allTriggers: List<TriggerItem> = remember { TriggerType.allEntries().map { TriggerItem.fromBuiltIn(it) } },
    vibrationEnabled: Boolean = true,
    onDelete: () -> Unit = {},
    onEdit: (Long) -> Unit = {},
    onUpdateTrigger: (String?) -> Unit = {}
) {
    val locale = LocalLocale.current.platformLocale
    val timeStr = remember(entryTime, locale) { SimpleDateFormat("HH:mm", locale).format(Date(entryTime)) }
    var isExpanded by rememberSaveable(entryTime) { mutableStateOf(false) }
    var showTimePicker by rememberSaveable(entryTime) { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val futureDateError = if (!LocalInspectionMode.current) stringResource(R.string.edit_future_time_error) else ""

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "chevron_rotation"
    )

    val timePickerState = remember(entryTime) {
        val cal = Calendar.getInstance().apply { timeInMillis = entryTime }
        TimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true,
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            title = stringResource(R.string.edit_entry),
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newCal = Calendar.getInstance().apply { timeInMillis = entryTime }
                        newCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        newCal.set(Calendar.MINUTE, timePickerState.minute)
                        newCal.set(Calendar.SECOND, 0)
                        newCal.set(Calendar.MILLISECOND, 0)
                        if (newCal.timeInMillis > System.currentTimeMillis()) {
                            android.widget.Toast.makeText(context, futureDateError, android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            onEdit(newCal.timeInMillis)
                            showTimePicker = false
                        }
                    }
                ) { Text(stringResource(R.string.dialog_ok), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false }
                ) { Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold) }
            }
        ) {
            val pastelContainer = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = pastelContainer,
                    timeSelectorContainerColor = pastelContainer,
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    timeSelectorContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f

    val (accentColor, accentContainer, onAccentContainer) = when (index % 3) {
        0 -> Triple(colorScheme.tertiary, colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
        1 -> Triple(colorScheme.secondary, colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)
        else -> Triple(colorScheme.primary, colorScheme.primaryContainer, colorScheme.onPrimaryContainer)
    }

    val cardBgColor = when {
        isResisted -> if (isDark) Color(0xFF142416) else Color(0xFFF0F7ED)
        isOverLimit -> colorScheme.errorContainer.copy(alpha = if (isDark) 0.28f else 0.32f).compositeOver(colorScheme.surfaceContainer)
        else -> colorScheme.surfaceContainer
    }

    val timePillBg = when {
        isResisted -> if (isDark) Color(0xFF224426) else Color(0xFFD6ECCF)
        isOverLimit -> colorScheme.errorContainer
        else -> accentContainer
    }

    val timePillContent = when {
        isResisted -> if (isDark) Color(0xFFAFE1B3) else Color(0xFF184D20)
        isOverLimit -> colorScheme.onErrorContainer
        else -> onAccentContainer
    }

    Card(
        onClick = {
            isExpanded = !isExpanded
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        shape = containerShape(RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = {
                        if (!isResisted) {
                            showTimePicker = true
                        }
                    },
                    shape = CircleShape,
                    color = timePillBg,
                    contentColor = timePillContent
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                val triggerType = trigger?.let { TriggerType.fromKey(it) }
                val triggerLabel = when {
                    isResisted -> stringResource(R.string.mindful_pause_resisted)
                    triggerType != null -> stringResource(triggerType.labelResId)
                    !trigger.isNullOrBlank() -> trigger
                    else -> stringResource(R.string.trigger_none)
                }
                val triggerIcon = when {
                    isResisted -> Icons.Default.Shield
                    else -> getTriggerIcon(trigger)
                }

                val triggerChipBg = when {
                    isResisted -> if (isDark) Color(0xFF224426).copy(alpha = 0.6f) else Color(0xFFD6ECCF).copy(alpha = 0.6f)
                    isOverLimit -> colorScheme.errorContainer.copy(alpha = if (isDark) 0.35f else 0.45f)
                    trigger != null -> accentContainer.copy(alpha = 0.35f)
                    else -> colorScheme.surfaceVariant.copy(alpha = 0.45f)
                }

                val triggerChipContent = when {
                    isResisted -> if (isDark) Color(0xFFAFE1B3) else Color(0xFF1E5E28)
                    isOverLimit -> colorScheme.error
                    trigger != null -> accentColor
                    else -> colorScheme.onSurfaceVariant
                }

                if (!isOverLimit || trigger != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = triggerChipBg,
                        contentColor = triggerChipContent,
                        modifier = Modifier.widthIn(max = if (isOverLimit) 105.dp else 145.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = triggerIcon,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = triggerLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (isOverLimit) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colorScheme.errorContainer.copy(alpha = if (isDark) 0.45f else 0.6f),
                        contentColor = colorScheme.error,
                        modifier = Modifier.widthIn(max = 120.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(R.string.entry_over_limit_badge),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!isResisted) {
                    if (prevEntryTime != null) {
                        val (hours, minutes) = remember(entryTime, prevEntryTime) {
                            val diffMs = entryTime - prevEntryTime
                            val h = TimeUnit.MILLISECONDS.toHours(diffMs)
                            val m = (TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60)
                            h to m
                        }
                        val intervalStr = if (hours > 0) {
                            stringResource(R.string.time_over_limit_hm, hours, minutes)
                        } else {
                            stringResource(R.string.time_over_limit_m, minutes)
                        }
                        Text(
                            text = intervalStr,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isOverLimit) colorScheme.error.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.first_of_the_day),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isOverLimit) colorScheme.error.copy(alpha = 0.85f) else accentColor.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    onClick = {
                        isExpanded = !isExpanded
                    },
                    shape = CircleShape,
                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = chevronRotation }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        color = colorScheme.outlineVariant.copy(alpha = 0.25f)
                    )

                    if (!isResisted) {
                        Surface(
                            onClick = {
                                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                showTimePicker = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            contentColor = colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.edit_time_label),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colorScheme.onSurface
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.edit_reason_label),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp)
                        )

                        val chunkedTriggers = remember(allTriggers) { allTriggers.chunked(2) }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            chunkedTriggers.forEach { rowTriggers ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowTriggers.forEach { triggerItem ->
                                        val isSelected = trigger == triggerItem.key
                                        Surface(
                                            onClick = {
                                                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                                if (isSelected) {
                                                    onUpdateTrigger(null)
                                                } else {
                                                    onUpdateTrigger(triggerItem.key)
                                                }
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) colorScheme.primaryContainer else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            contentColor = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = getTriggerIcon(triggerItem.key),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                val itemLabel = if (triggerItem.labelResId != null) stringResource(triggerItem.labelResId) else (triggerItem.customName ?: triggerItem.key)
                                                Text(
                                                    text = itemLabel,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
                                                    ),
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    if (rowTriggers.size < 2) {
                                        repeat(2 - rowTriggers.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        onClick = onDelete,
                        shape = RoundedCornerShape(16.dp),
                        color = colorScheme.errorContainer.copy(alpha = 0.4f),
                        contentColor = colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.delete_entry),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableEntryItem(
    entryTime: Long,
    prevEntryTime: Long? = null,
    isResisted: Boolean = false,
    isOverLimit: Boolean = false,
    index: Int = 0,
    trigger: String? = null,
    allTriggers: List<TriggerItem>,
    vibrationEnabled: Boolean = true,
    onDelete: () -> Unit,
    onEdit: (Long) -> Unit,
    onUpdateTrigger: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalWindowInfo.current
    val screenWidthPx = remember(windowInfo) {
        windowInfo.containerSize.width.toFloat()
    }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    @Suppress("DEPRECATION")
    lateinit var dismissStateRef: SwipeToDismissBoxState
    @Suppress("DEPRECATION")
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance -> distance * 0.85f },
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                val minThreshold = screenWidthPx * 0.67f
                val currentAbsOffset = kotlin.math.abs(runCatching { dismissStateRef.requireOffset() }.getOrDefault(0f))
                if (currentAbsOffset < minThreshold) {
                    false
                } else {
                    HapticFeedbackHelper.performHeavyThreshold(vibrationEnabled, haptic, context)
                    onDelete()
                    true
                }
            } else {
                true
            }
        }
    ).also { dismissStateRef = it }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isEndToStart = direction == SwipeToDismissBoxValue.EndToStart
            val alignment = if (isEndToStart) Alignment.CenterEnd else Alignment.CenterStart
            val currentOffset = runCatching { dismissState.requireOffset() }.getOrDefault(0f)
            val absOffset = kotlin.math.abs(currentOffset)
            val absOffsetDp = with(LocalDensity.current) { absOffset.toDp() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 5.dp),
                contentAlignment = alignment
            ) {
                if (absOffsetDp > 6.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(absOffsetDp)
                            .clip(containerShape(RoundedCornerShape(24.dp)))
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete_entry),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) {
        EntryItem(
            entryTime = entryTime,
            prevEntryTime = prevEntryTime,
            isResisted = isResisted,
            isOverLimit = isOverLimit,
            index = index,
            trigger = trigger,
            allTriggers = allTriggers,
            vibrationEnabled = vibrationEnabled,
            onDelete = {
                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                onDelete()
            },
            onEdit = onEdit,
            onUpdateTrigger = onUpdateTrigger
        )
    }
}
