package com.smokingtracker.ui.components.triggers.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
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
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.ui.components.triggers.calculateQuadrantDominantTriggers
import com.smokingtracker.ui.components.triggers.getTriggerIcon
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress
import com.smokingtracker.ui.theme.subContainer

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TriggerTimeDistributionCard(
    entities: List<SmokingEntryEntity>,
    onTriggerSelected: (String) -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val dominants = remember(entities) { calculateQuadrantDominantTriggers(entities) }

    val quadrantMeta = listOf(
        Triple(stringResource(R.string.day_part_night).substringBefore(" ("), "00–06", Icons.Filled.Bedtime),
        Triple(stringResource(R.string.day_part_morning).substringBefore(" ("), "06–12", Icons.Filled.WbTwilight),
        Triple(stringResource(R.string.day_part_afternoon).substringBefore(" ("), "12–18", Icons.Filled.WbSunny),
        Triple(stringResource(R.string.day_part_evening).substringBefore(" ("), "18–24", Icons.Filled.NightsStay)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.trigger_time_patterns_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.trigger_time_patterns_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dominants.forEach { dominant ->
                    val (qName, qHours, qIcon) = quadrantMeta[dominant.quadrantIndex]
                    val hasData = dominant.triggerKey != null && dominant.count > 0
                    val pct = if (dominant.totalInQuadrant > 0) ((dominant.count.toFloat() / dominant.totalInQuadrant) * 100).toInt() else 0
                    val triggerType = dominant.triggerKey?.let { TriggerType.fromKey(it) }
                    val triggerName = triggerType?.let { stringResource(it.labelResId) } ?: dominant.triggerKey ?: ""

                    val interactionSource = remember { MutableInteractionSource() }
                    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.94f)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bouncyPress(bouncy)
                            .clickable(
                                enabled = hasData,
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (dominant.triggerKey != null) {
                                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    onTriggerSelected(dominant.triggerKey)
                                }
                            },
                        shape = containerShape(RoundedCornerShape(18.dp)),
                        color = MaterialTheme.colorScheme.subContainer
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = qIcon,
                                contentDescription = null,
                                tint = if (hasData) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = qHours,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )

                            if (hasData) {
                                val cookieShape = MaterialShapes.Cookie9Sided.toShape()
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(cookieShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getTriggerIcon(dominant.triggerKey),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = triggerName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.trigger_no_activity_period),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
