package com.smokingtracker.ui.home.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.data.TriggerItem
import com.smokingtracker.ui.MindfulPauseContent
import com.smokingtracker.ui.home.components.getTriggerIcon
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeTriggerBottomSheet(
    visible: Boolean,
    activeTriggers: List<TriggerItem>,
    startInMindfulPause: Boolean,
    mindfulPauseTrigger: String?,
    vibrationEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onSelectTrigger: (String?) -> Unit,
    onAddCustomTrigger: () -> Unit,
    onMindfulPauseSuccess: (String?) -> Unit,
    onMindfulPauseFailure: (String?) -> Unit
) {
    if (!visible) return

    val triggerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isMindfulPauseActive by rememberSaveable(visible) { mutableStateOf(startInMindfulPause) }
    var currentMindfulTrigger by rememberSaveable(visible) { mutableStateOf(mindfulPauseTrigger) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = {
            isMindfulPauseActive = false
            onDismiss()
        },
        sheetState = triggerSheetState,
        containerColor = if (MaterialTheme.colorScheme.surfaceContainerLow == Color.White) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        AnimatedContent(
            targetState = isMindfulPauseActive,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "sheet_step_anim"
        ) { inPauseMode ->
            if (inPauseMode) {
                MindfulPauseContent(
                    selectedTrigger = currentMindfulTrigger,
                    vibrationEnabled = vibrationEnabled,
                    onDismiss = {
                        isMindfulPauseActive = false
                        onDismiss()
                    },
                    onSuccess = { trigger ->
                        isMindfulPauseActive = false
                        onMindfulPauseSuccess(trigger)
                    },
                    onFailure = { trigger ->
                        isMindfulPauseActive = false
                        onMindfulPauseFailure(trigger)
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.trigger_dialog_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val chunkedTriggers = remember(activeTriggers) { activeTriggers.chunked(2) }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        chunkedTriggers.forEach { rowTriggers ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowTriggers.forEach { triggerItem ->
                                    Card(
                                        onClick = {
                                            onSelectTrigger(triggerItem.key)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(78.dp),
                                        shape = containerShape(RoundedCornerShape(20.dp)),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxSize().padding(8.dp)
                                        ) {
                                            val cShape = MaterialShapes.Cookie9Sided.toShape()
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(cShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = getTriggerIcon(triggerItem.key),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val labelText = if (triggerItem.labelResId != null) stringResource(triggerItem.labelResId) else (triggerItem.customName ?: triggerItem.key)
                                            Text(
                                                text = labelText,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                if (rowTriggers.size == 1) {
                                    Card(
                                        onClick = {
                                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                            onAddCustomTrigger()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(78.dp),
                                        shape = containerShape(RoundedCornerShape(20.dp)),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                            contentColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxSize().padding(8.dp)
                                        ) {
                                            val cShape = MaterialShapes.Cookie9Sided.toShape()
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(cShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stringResource(R.string.trigger_add_custom),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (activeTriggers.size % 2 == 0) {
                            Surface(
                                onClick = {
                                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    onAddCustomTrigger()
                                },
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                contentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.trigger_add_custom),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        onClick = {
                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            currentMindfulTrigger = null
                            isMindfulPauseActive = true
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.mindful_pause_button),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onSelectTrigger(null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SmokingRooms,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(stringResource(R.string.trigger_skip), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
