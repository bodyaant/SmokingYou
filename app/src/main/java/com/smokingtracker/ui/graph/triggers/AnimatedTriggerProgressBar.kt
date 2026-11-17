package com.smokingtracker.ui.graph.triggers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedTriggerProgressBar(
    targetProgress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animProgress = remember { Animatable(0f) }
    val animWaveScale = remember { Animatable(1f) }

    LaunchedEffect(targetProgress) {
        if (targetProgress > 0f) {
            animWaveScale.snapTo(1f)
            launch {
                animProgress.animateTo(
                    targetValue = targetProgress,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                )
            }
            launch {
                animWaveScale.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                )
            }
        } else {
            animProgress.snapTo(0f)
            animWaveScale.snapTo(0f)
        }
    }

    LinearWavyProgressIndicator(
        progress = { animProgress.value },
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        amplitude = { animWaveScale.value }
    )
}
