package com.smokingtracker.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import kotlinx.coroutines.launch

@Composable
fun BoxScope.HomeFabMenuOverlay(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    rootSize: IntSize,
    fabRightPx: Float,
    fabTopPx: Float,
    vibrationEnabled: Boolean,
    onMindfulPause: () -> Unit,
    onResisted: () -> Unit,
    onSmoked: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isExpanded) {
        BackHandler {
            onDismiss()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onDismiss()
                }
        )
    }

    if (rootSize.width > 0 && fabRightPx > 0f) {
        val density = LocalDensity.current
        val context = LocalContext.current
        val haptic = LocalHapticFeedback.current

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(150)) + scaleIn(
                initialScale = 0.8f,
                transformOrigin = TransformOrigin(1f, 1f),
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow)
            ) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow)
            ),
            exit = fadeOut(tween(100)) + scaleOut(
                targetScale = 0.8f,
                transformOrigin = TransformOrigin(1f, 1f),
                animationSpec = tween(100)
            ) + slideOutVertically(
                targetOffsetY = { it / 3 },
                animationSpec = tween(100)
            ),
            modifier = modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = with(density) { (rootSize.width - fabRightPx).coerceAtLeast(0f).toDp() },
                    bottom = with(density) { (rootSize.height - fabTopPx + 12.dp.toPx()).toDp() }
                )
        ) {
            FabMenuItemsColumn(
                onMindfulPause = {
                    onDismiss()
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    onMindfulPause()
                },
                onResisted = {
                    onDismiss()
                    onResisted()
                },
                onSmoked = {
                    onDismiss()
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    onSmoked()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeFabButton(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val squishProgress = remember { Animatable(1f) }

    val triggerSquish: () -> Unit = {
        scope.launch {
            squishProgress.animateTo(
                targetValue = 0.82f,
                animationSpec = tween(70, easing = LinearEasing)
            )
            squishProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    ToggleFloatingActionButton(
        checked = isExpanded,
        onCheckedChange = {
            onToggle()
            triggerSquish()
            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
        },
        modifier = modifier
            .size(68.dp)
            .graphicsLayer {
                scaleX = 2f - squishProgress.value
                scaleY = squishProgress.value
            },
        containerColor = ToggleFloatingActionButtonDefaults.containerColor(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary
        ),
        containerCornerRadius = ToggleFloatingActionButtonDefaults.containerCornerRadius(
            24.dp,
            36.dp
        ),
        containerSize = ToggleFloatingActionButtonDefaults.containerSize(
            68.dp,
            64.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    (scaleIn(spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)) + fadeIn())
                        .togetherWith(scaleOut(tween(100)) + fadeOut())
                },
                label = "fab_icon_morph"
            ) { expanded ->
                if (expanded) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close_menu),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_entry),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FabMenuItemsColumn(
    onMindfulPause: () -> Unit,
    onResisted: () -> Unit,
    onSmoked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FabMenuItem(
            onClick = onMindfulPause,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SelfImprovement,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            },
            label = stringResource(R.string.action_mindful_pause)
        )

        FabMenuItem(
            onClick = onResisted,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            },
            label = stringResource(R.string.action_resisted_craving)
        )

        FabMenuItem(
            onClick = onSmoked,
            icon = {
                Icon(
                    imageVector = Icons.Filled.SmokingRooms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            },
            label = stringResource(R.string.action_smoked_cigarette)
        )
    }
}

@Composable
fun FabMenuItem(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 6.dp,
        tonalElevation = 3.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}
