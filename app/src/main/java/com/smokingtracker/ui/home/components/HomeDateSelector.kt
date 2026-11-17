package com.smokingtracker.ui.home.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.rememberBouncyPress

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeDateSelector(
    selectedDateStr: String,
    canGoNext: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onOpenDatePicker: () -> Unit,
    vibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var totalDragX by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .pointerInput(canGoNext) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onDragEnd = {
                        if (totalDragX > 80f) {
                            HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                            onPreviousDay()
                        } else if (totalDragX < -80f && canGoNext) {
                            HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                            onNextDay()
                        }
                        totalDragX = 0f
                    },
                    onDragCancel = { totalDragX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount
                    }
                )
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val prevInteractionSource = remember { MutableInteractionSource() }
        val prevBouncy = rememberBouncyPress(
            interactionSource = prevInteractionSource,
            targetScale = 0.86f,
            targetRotation = -12f
        )

        val nextInteractionSource = remember { MutableInteractionSource() }
        val nextBouncy = rememberBouncyPress(
            interactionSource = nextInteractionSource,
            targetScale = 0.86f,
            targetRotation = 12f
        )

        val datePickerInteractionSource = remember { MutableInteractionSource() }
        val datePickerBouncy = rememberBouncyPress(
            interactionSource = datePickerInteractionSource,
            targetScale = 0.94f
        )

        Surface(
            onClick = {
                prevBouncy.bounce()
                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                onPreviousDay()
            },
            shape = MaterialShapes.Cookie9Sided.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            interactionSource = prevInteractionSource,
            modifier = Modifier
                .size(48.dp)
                .bouncyPress(prevBouncy)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.previous_day)
                )
            }
        }

        Surface(
            onClick = {
                datePickerBouncy.bounce()
                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                onOpenDatePicker()
            },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
            contentColor = MaterialTheme.colorScheme.primary,
            interactionSource = datePickerInteractionSource,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .bouncyPress(datePickerBouncy)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedDateStr,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Surface(
            onClick = {
                if (canGoNext) {
                    nextBouncy.bounce()
                    HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                    onNextDay()
                }
            },
            enabled = canGoNext,
            shape = MaterialShapes.Cookie9Sided.toShape(),
            color = if (canGoNext) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (canGoNext) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            interactionSource = nextInteractionSource,
            modifier = Modifier
                .size(48.dp)
                .bouncyPress(nextBouncy)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next_day)
                )
            }
        }
    }
}
