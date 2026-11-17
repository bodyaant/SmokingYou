package com.smokingtracker.ui.graph.triggers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.ui.components.triggers.calculatePeakPeriodForTrigger
import com.smokingtracker.ui.components.triggers.calculateTriggerTrend
import com.smokingtracker.ui.components.triggers.cards.TopTriggerHeroCard
import com.smokingtracker.ui.components.triggers.cards.TriggerTimeDistributionCard
import com.smokingtracker.ui.components.triggers.cards.TriggerTrendBadge
import com.smokingtracker.ui.components.triggers.getTriggerIcon
import com.smokingtracker.ui.components.triggers.models.TriggerTrendType
import com.smokingtracker.ui.components.triggers.sheets.TriggerDetailBottomSheet
import com.smokingtracker.ui.graph.components.TriggerPeriodSelector
import com.smokingtracker.ui.graph.models.TriggerPeriod
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ManageTriggersCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = MaterialShapes.Cookie9Sided.toShape(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.trigger_manage_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.trigger_manage_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TriggersTab(
    triggerEntities: List<SmokingEntryEntity>,
    onOpenTriggerManagement: () -> Unit = {},
    vibrationEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var selectedPeriod by rememberSaveable { mutableStateOf(TriggerPeriod.ALL_TIME) }
    var selectedDetailTriggerKey by rememberSaveable { mutableStateOf<String?>(null) }

    val totalAllTimeTriggers = remember(triggerEntities) {
        triggerEntities.count { it.trigger != null }
    }

    if (totalAllTimeTriggers == 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = MaterialShapes.Cookie9Sided.toShape(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.triggers_no_data_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.triggers_no_data_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                FilledTonalButton(
                    onClick = {
                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        onOpenTriggerManagement()
                    },
                    shape = containerShape(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.trigger_manage_title),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        return
    }

    val filteredEntities = remember(triggerEntities, selectedPeriod) {
        val now = System.currentTimeMillis()
        when (selectedPeriod) {
            TriggerPeriod.ALL_TIME -> triggerEntities.filter { it.trigger != null }
            TriggerPeriod.MONTH -> {
                val cutoff = now - 30L * 24 * 60 * 60 * 1000L
                triggerEntities.filter { it.trigger != null && it.timestamp >= cutoff }
            }
            TriggerPeriod.WEEK -> {
                val cutoff = now - 7L * 24 * 60 * 60 * 1000L
                triggerEntities.filter { it.trigger != null && it.timestamp >= cutoff }
            }
        }
    }

    val periodTriggerCounts = remember(filteredEntities) {
        val counts = mutableMapOf<String, Int>()
        filteredEntities.forEach { entity ->
            val trigger = entity.trigger
            if (trigger != null) {
                counts[trigger] = (counts[trigger] ?: 0) + 1
            }
        }
        counts
    }

    val previousPeriodTriggerCounts = remember(triggerEntities, selectedPeriod) {
        val now = System.currentTimeMillis()
        val previousEntities = when (selectedPeriod) {
            TriggerPeriod.WEEK -> {
                val start = now - 14L * 24 * 60 * 60 * 1000L
                val end = now - 7L * 24 * 60 * 60 * 1000L
                triggerEntities.filter { it.trigger != null && it.timestamp in start until end }
            }
            TriggerPeriod.MONTH -> {
                val start = now - 60L * 24 * 60 * 60 * 1000L
                val end = now - 30L * 24 * 60 * 60 * 1000L
                triggerEntities.filter { it.trigger != null && it.timestamp in start until end }
            }
            TriggerPeriod.ALL_TIME -> emptyList()
        }
        val counts = mutableMapOf<String, Int>()
        previousEntities.forEach { entity ->
            val trigger = entity.trigger
            if (trigger != null) {
                counts[trigger] = (counts[trigger] ?: 0) + 1
            }
        }
        counts
    }

    val periodTotalCount = remember(periodTriggerCounts) {
        periodTriggerCounts.values.sum()
    }

    val sortedTriggers = remember(periodTriggerCounts) {
        periodTriggerCounts.toList().sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "trigger_period_selector") {
            TriggerPeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { period ->
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    selectedPeriod = period
                }
            )
        }

        if (periodTotalCount == 0) {
            item(key = "trigger_no_data_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = MaterialShapes.Cookie9Sided.toShape(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.EventBusy,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.triggers_period_no_data),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val mostFrequent = sortedTriggers.firstOrNull()
            if (mostFrequent != null && mostFrequent.second > 0) {
                item(key = "trigger_top_hero_${mostFrequent.first}") {
                    val topKey = mostFrequent.first
                    val topCount = mostFrequent.second
                    val topPercent = if (periodTotalCount > 0) (topCount.toFloat() / periodTotalCount * 100).toInt() else 0
                    val triggerType = TriggerType.fromKey(topKey)
                    val triggerName = triggerType?.let { stringResource(it.labelResId) } ?: topKey

                    val peakPeriodResId = remember(filteredEntities, topKey) {
                        calculatePeakPeriodForTrigger(filteredEntities, topKey)
                    }

                    val topTrend = remember(topKey, periodTriggerCounts, previousPeriodTriggerCounts, selectedPeriod) {
                        val cur = periodTriggerCounts[topKey] ?: 0
                        val prev = previousPeriodTriggerCounts[topKey] ?: 0
                        calculateTriggerTrend(cur, prev, selectedPeriod != TriggerPeriod.ALL_TIME)
                    }

                    TopTriggerHeroCard(
                        triggerKey = topKey,
                        triggerName = triggerName,
                        count = topCount,
                        percent = topPercent,
                        peakPeriodResId = peakPeriodResId,
                        trend = topTrend,
                        onClick = { selectedDetailTriggerKey = topKey },
                        vibrationEnabled = vibrationEnabled
                    )
                }
            }

            item(key = "trigger_time_distribution") {
                TriggerTimeDistributionCard(
                    entities = filteredEntities,
                    onTriggerSelected = { selectedDetailTriggerKey = it },
                    vibrationEnabled = vibrationEnabled
                )
            }

            item(key = "trigger_list_header") {
                Text(
                    text = stringResource(R.string.triggers_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            itemsIndexed(
                items = sortedTriggers,
                key = { _, (triggerKey, _) -> "trigger_row_$triggerKey" }
            ) { index, (triggerKey, count) ->
                val isFirst = index == 0
                val isLast = index == sortedTriggers.lastIndex
                val itemShape = containerShape(
                    RoundedCornerShape(
                        topStart = if (isFirst) 28.dp else 10.dp,
                        topEnd = if (isFirst) 28.dp else 10.dp,
                        bottomStart = if (isLast) 28.dp else 10.dp,
                        bottomEnd = if (isLast) 28.dp else 10.dp
                    )
                )

                val triggerType = TriggerType.fromKey(triggerKey)
                val triggerName = triggerType?.let { stringResource(it.labelResId) } ?: triggerKey
                val percent = if (periodTotalCount > 0) count.toFloat() / periodTotalCount.toFloat() else 0f

                val rowTrend = remember(triggerKey, periodTriggerCounts, previousPeriodTriggerCounts, selectedPeriod) {
                    val cur = periodTriggerCounts[triggerKey] ?: 0
                    val prev = previousPeriodTriggerCounts[triggerKey] ?: 0
                    calculateTriggerTrend(cur, prev, selectedPeriod != TriggerPeriod.ALL_TIME)
                }

                val rowInteraction = remember { MutableInteractionSource() }
                val rowBouncy = rememberBouncyPress(interactionSource = rowInteraction, targetScale = 0.98f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bouncyPress(rowBouncy)
                        .clickable(
                            interactionSource = rowInteraction,
                            indication = null
                        ) {
                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            selectedDetailTriggerKey = triggerKey
                        },
                    shape = itemShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                val cookieShape = MaterialShapes.Cookie9Sided.toShape()
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(cookieShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getTriggerIcon(triggerKey),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = triggerName,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (rowTrend.type != TriggerTrendType.NONE && rowTrend.type != TriggerTrendType.SAME) {
                                            TriggerTrendBadge(trend = rowTrend)
                                        }
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.trigger_count_pattern, count, (percent * 100).toInt()),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        AnimatedTriggerProgressBar(
                            targetProgress = percent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        item(key = "trigger_manage_card") {
            ManageTriggersCard(
                onClick = {
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    onOpenTriggerManagement()
                }
            )
        }
    }

    selectedDetailTriggerKey?.let { detailKey ->
        val curCount = periodTriggerCounts[detailKey] ?: 0
        val prevCount = previousPeriodTriggerCounts[detailKey] ?: 0
        val detailTrend = remember(detailKey, curCount, prevCount, selectedPeriod) {
            calculateTriggerTrend(curCount, prevCount, selectedPeriod != TriggerPeriod.ALL_TIME)
        }
        TriggerDetailBottomSheet(
            triggerKey = detailKey,
            filteredEntities = filteredEntities,
            allFilteredTotalCount = periodTotalCount,
            trend = detailTrend,
            vibrationEnabled = vibrationEnabled,
            onDismissRequest = { selectedDetailTriggerKey = null }
        )
    }
}
