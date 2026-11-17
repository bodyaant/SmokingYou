package com.smokingtracker.ui.components.triggers.sheets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.ui.components.charts.rememberChartColorPalette
import com.smokingtracker.ui.components.triggers.calculateTriggerHourlyDistribution
import com.smokingtracker.ui.components.triggers.cards.TriggerTrendBadge
import com.smokingtracker.ui.components.triggers.getCopingStrategyTip
import com.smokingtracker.ui.components.triggers.getTriggerIcon
import com.smokingtracker.ui.components.triggers.models.TriggerQuadPart
import com.smokingtracker.ui.components.triggers.models.TriggerTrendData
import com.smokingtracker.ui.components.triggers.models.TriggerTrendType
import com.smokingtracker.ui.theme.ContainerIcon
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TriggerDetailBottomSheet(
    triggerKey: String,
    filteredEntities: List<SmokingEntryEntity>,
    allFilteredTotalCount: Int,
    trend: TriggerTrendData,
    vibrationEnabled: Boolean,
    onDismissRequest: () -> Unit
) {
    val chartPalette = rememberChartColorPalette()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val locale = LocalLocale.current.platformLocale
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val triggerType = TriggerType.fromKey(triggerKey)
    val triggerName = triggerType?.let { stringResource(it.labelResId) } ?: triggerKey
    val tipResId = remember(triggerKey) { getCopingStrategyTip(triggerKey) }

    val triggerEntities = remember(filteredEntities, triggerKey) {
        filteredEntities.filter { it.trigger == triggerKey }
    }
    val triggerCount = triggerEntities.size
    val sharePercent = if (allFilteredTotalCount > 0) ((triggerCount.toFloat() / allFilteredTotalCount) * 100).toInt() else 0

    val hourlyCounts = remember(triggerEntities, triggerKey) {
        calculateTriggerHourlyDistribution(triggerEntities, triggerKey)
    }
    val maxHourlyCount = hourlyCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val peakHour = hourlyCounts.indices.maxByOrNull { hourlyCounts[it] } ?: 0
    val peakHourCount = hourlyCounts[peakHour]

    var selectedHour by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedQuadrant by rememberSaveable { mutableStateOf<Int?>(null) }

    val cal = Calendar.getInstance()
    val nightCount = remember(triggerEntities) {
        triggerEntities.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 0..5
        }
    }
    val morningCount = remember(triggerEntities) {
        triggerEntities.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 6..11
        }
    }
    val afternoonCount = remember(triggerEntities) {
        triggerEntities.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 12..17
        }
    }
    val eveningCount = remember(triggerEntities) {
        triggerEntities.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 18..23
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = containerShape(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        containerColor = if (MaterialTheme.colorScheme.surfaceContainerLow == Color.White) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ContainerIcon(
                        icon = getTriggerIcon(triggerKey),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        backdropColor = MaterialTheme.colorScheme.primaryContainer,
                        size = 46.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = triggerName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (trend.type != TriggerTrendType.NONE && trend.type != TriggerTrendType.SAME) {
                                TriggerTrendBadge(trend = trend)
                            }
                        }
                        Text(
                            text = stringResource(R.string.trigger_detail_share_format, triggerCount, sharePercent),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = {
                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        onDismissRequest()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.btn_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = containerShape(RoundedCornerShape(18.dp)),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (selectedHour != null) Icons.Filled.AccessTime else Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    val (qName, qCount) = when (selectedQuadrant) {
                        0 -> stringResource(R.string.day_part_night).substringBefore(" (") to nightCount
                        1 -> stringResource(R.string.day_part_morning).substringBefore(" (") to morningCount
                        2 -> stringResource(R.string.day_part_afternoon).substringBefore(" (") to afternoonCount
                        else -> stringResource(R.string.day_part_evening).substringBefore(" (") to eveningCount
                    }
                    val qPct = if (triggerCount > 0) ((qCount.toFloat() / triggerCount) * 100).toInt() else 0

                    val hourHighlightText = if (selectedHour != null) {
                        val sh = selectedHour!!
                        val eh = (sh + 1) % 24
                        val selCount = hourlyCounts[sh]
                        String.format(locale, "%02d:00–%02d:00: %d", sh, eh, selCount)
                    } else if (selectedQuadrant != null) {
                        stringResource(R.string.peak_hours_quadrant_format, qName, qCount.toString(), qPct)
                    } else if (peakHourCount > 0) {
                        stringResource(R.string.trigger_detail_peak_hour_format, peakHour, (peakHour + 1) % 24, peakHourCount)
                    } else {
                        stringResource(R.string.trigger_detail_no_peak)
                    }
                    Text(
                        text = hourHighlightText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val quadrants = listOf(
                TriggerQuadPart(0, stringResource(R.string.day_part_night).substringBefore(" ("), "00–06", nightCount, Icons.Filled.Bedtime, chartPalette.tertiary),
                TriggerQuadPart(1, stringResource(R.string.day_part_morning).substringBefore(" ("), "06–12", morningCount, Icons.Filled.WbTwilight, chartPalette.primary.copy(alpha = 0.75f)),
                TriggerQuadPart(2, stringResource(R.string.day_part_afternoon).substringBefore(" ("), "12–18", afternoonCount, Icons.Filled.WbSunny, chartPalette.primary),
                TriggerQuadPart(3, stringResource(R.string.day_part_evening).substringBefore(" ("), "18–24", eveningCount, Icons.Filled.NightsStay, chartPalette.cleanDay)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quadrants.forEach { q ->
                    val isSelected = selectedQuadrant == q.index
                    val pct = if (triggerCount > 0) ((q.count.toFloat() / triggerCount) * 100).toInt() else 0
                    val interactionSource = remember { MutableInteractionSource() }
                    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.94f)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bouncyPress(bouncy)
                            .clickable(interactionSource = interactionSource, indication = null) {
                                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                selectedQuadrant = if (selectedQuadrant == q.index) null else q.index
                                if (selectedQuadrant != null && selectedHour != null && selectedHour !in (selectedQuadrant!! * 6 until (selectedQuadrant!! + 1) * 6)) {
                                    selectedHour = null
                                }
                            },
                        shape = containerShape(RoundedCornerShape(16.dp)),
                        color = if (isSelected) chartPalette.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = q.icon,
                                contentDescription = null,
                                tint = if (isSelected) chartPalette.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = q.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isSelected) chartPalette.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "${q.count}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (isSelected) chartPalette.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) chartPalette.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.trigger_hourly_distribution_title),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .pointerInput(hourlyCounts) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    val hourIndex = ((offset.x / size.width) * 24).toInt().coerceIn(0, 23)
                                    if (selectedHour != hourIndex) {
                                        selectedHour = hourIndex
                                        HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                    }
                                },
                                onHorizontalDrag = { change, _ ->
                                    change.consume()
                                    val hourIndex = ((change.position.x / size.width) * 24).toInt().coerceIn(0, 23)
                                    if (selectedHour != hourIndex) {
                                        selectedHour = hourIndex
                                        HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                    }
                                }
                            )
                        }
                ) {
                    val isGeneralView = selectedQuadrant == null && selectedHour == null
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        hourlyCounts.forEachIndexed { hour, count ->
                            val isPeak = isGeneralView && hour == peakHour && count > 0
                            val isSelected = hour == selectedHour
                            val inSelectedQuadrant = selectedQuadrant == null || hour in (selectedQuadrant!! * 6 until (selectedQuadrant!! + 1) * 6)

                            val animFraction = remember { Animatable(0.04f) }
                            LaunchedEffect(count, maxHourlyCount) {
                                val target = if (count == 0) 0.04f else (count.toFloat() / maxHourlyCount).coerceIn(0.08f, 1.0f)
                                animFraction.animateTo(
                                    targetValue = target,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = 650f
                                    )
                                )
                            }

                            val baseColor = when (hour) {
                                in 0..5 -> chartPalette.tertiary
                                in 6..11 -> chartPalette.primary.copy(alpha = 0.75f)
                                in 12..17 -> chartPalette.primary
                                else -> chartPalette.cleanDay
                            }

                            val barColor = when {
                                isSelected -> chartPalette.primary
                                !inSelectedQuadrant -> baseColor.copy(alpha = 0.15f)
                                isPeak -> chartPalette.primary
                                count > 0 -> if (selectedQuadrant != null) baseColor else baseColor.copy(alpha = 0.75f)
                                else -> chartPalette.surfaceContainerHighest.copy(alpha = 0.4f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = String.format(locale, "%02d:00–%02d:00: %d", hour, (hour + 1) % 24, count)
                                        selected = isSelected
                                    }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedHour = if (selectedHour == hour) null else hour
                                        HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                    },
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    if (isPeak) {
                                        Icon(
                                            imageVector = Icons.Filled.Bolt,
                                            contentDescription = null,
                                            tint = chartPalette.primary,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .padding(bottom = 2.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(108.dp * animFraction.value)
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(barColor)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("00:00", "06:00", "12:00", "18:00", "23:00").forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Surface(
                shape = containerShape(RoundedCornerShape(18.dp)),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.trigger_strategy_title),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(tipResId),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
