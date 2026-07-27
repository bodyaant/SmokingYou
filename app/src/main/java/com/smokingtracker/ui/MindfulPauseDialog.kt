package com.smokingtracker.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.smokingtracker.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MindfulPauseDialog(
    selectedTrigger: String?,
    onDismiss: () -> Unit,
    onSuccess: (String?) -> Unit,
    onFailure: (String?) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var totalSeconds by remember { mutableIntStateOf(15) }
    var secondsRemaining by remember { mutableIntStateOf(15) }
    var elapsedTimeMs by remember { mutableLongStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var isTimerFinished by remember { mutableStateOf(false) }

    val motivationalTips = remember {
        listOf(
            R.string.mindful_pause_tip_1,
            R.string.mindful_pause_tip_2,
            R.string.mindful_pause_tip_3,
            R.string.mindful_pause_tip_4,
            R.string.mindful_pause_tip_5
        )
    }

    var tipIndex by remember { mutableIntStateOf(0) }

    val breathingPhaseText = when ((totalSeconds - secondsRemaining) % 12) {
        in 0..3 -> stringResource(R.string.mindful_pause_breathe_in)
        in 4..7 -> stringResource(R.string.mindful_pause_hold_breath)
        else -> stringResource(R.string.mindful_pause_breathe_out)
    }

    LaunchedEffect(isTimerRunning, totalSeconds) {
        if (isTimerRunning) {
            val startTime = System.currentTimeMillis() - elapsedTimeMs
            while (elapsedTimeMs < totalSeconds * 1000L) {
                delay(16L)
                elapsedTimeMs = System.currentTimeMillis() - startTime
                val currentSecondsRemaining = (totalSeconds - (elapsedTimeMs / 1000L)).toInt().coerceAtLeast(0)
                if (currentSecondsRemaining != secondsRemaining) {
                    secondsRemaining = currentSecondsRemaining
                    if (secondsRemaining % 5 == 0 && secondsRemaining > 0) {
                        tipIndex = (tipIndex + 1) % motivationalTips.size
                    }
                }
            }
            elapsedTimeMs = totalSeconds * 1000L
            secondsRemaining = 0
            isTimerRunning = false
            isTimerFinished = true
            try {
                com.smokingtracker.ui.theme.HapticFeedbackHelper.performSuccess(haptic, context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val progress = (elapsedTimeMs.toFloat() / (totalSeconds * 1000f)).coerceIn(0f, 1f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.mindful_pause_header),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.dialog_cancel))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            ) {
                CircularWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!isTimerFinished) {
                        Text(
                            text = "$secondsRemaining",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.mindful_pause_sec),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isTimerFinished) stringResource(R.string.mindful_pause_well_done) else breathingPhaseText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(motivationalTips[tipIndex]),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isTimerFinished) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(haptic, context)
                            totalSeconds += 15
                            secondsRemaining += 15
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MoreTime, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.mindful_pause_add_15s))
                    }

                    Button(
                        onClick = {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performSuccess(haptic, context)
                            isTimerRunning = false
                            isTimerFinished = true
                            elapsedTimeMs = totalSeconds * 1000L
                            secondsRemaining = 0
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.mindful_pause_already_held))
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performSuccess(haptic, context)
                            onSuccess(selectedTrigger)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.mindful_pause_resisted),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    TextButton(
                        onClick = {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(haptic, context)
                            onFailure(selectedTrigger)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmokingRooms,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.mindful_pause_smoked_after_all),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
