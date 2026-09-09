package com.smokingtracker.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BouncyPressState(
    val scale: State<Float>,
    val rotation: State<Float>
)

fun Modifier.bouncyPress(state: BouncyPressState): Modifier = this.graphicsLayer {
    scaleX = state.scale.value
    scaleY = state.scale.value
    rotationZ = state.rotation.value
}

@Composable
fun rememberBouncyPress(
    interactionSource: MutableInteractionSource,
    targetScale: Float = 0.86f,
    targetRotation: Float = 0f,
    minHoldTimeMs: Long = 85L,
    compressStiffness: Float = Spring.StiffnessMedium,
    releaseDampingRatio: Float = Spring.DampingRatioMediumBouncy,
    releaseStiffness: Float = Spring.StiffnessLow
): BouncyPressState {
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(interactionSource, targetScale, targetRotation) {
        var pressStartTime = 0L
        var isCurrentlyPressed = false
        var compressJob: Job? = null
        var releaseJob: Job? = null

        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    releaseJob?.cancel()
                    isCurrentlyPressed = true
                    pressStartTime = System.currentTimeMillis()
                    compressJob?.cancel()
                    compressJob = launch {
                        launch {
                            scale.animateTo(
                                targetValue = targetScale,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = compressStiffness
                                )
                            )
                        }
                        if (targetRotation != 0f) {
                            launch {
                                rotation.animateTo(
                                    targetValue = targetRotation,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = compressStiffness
                                    )
                                )
                            }
                        }
                    }
                }
                is PressInteraction.Release -> {
                    isCurrentlyPressed = false
                    val elapsed = System.currentTimeMillis() - pressStartTime
                    val remainingHold = minHoldTimeMs - elapsed
                    releaseJob?.cancel()
                    releaseJob = launch {
                        if (remainingHold > 0) {
                            delay(remainingHold)
                        }
                        if (!isCurrentlyPressed) {
                            launch {
                                scale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = releaseDampingRatio,
                                        stiffness = releaseStiffness
                                    )
                                )
                            }
                            if (targetRotation != 0f || rotation.value != 0f) {
                                launch {
                                    rotation.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = releaseDampingRatio,
                                            stiffness = releaseStiffness
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                is PressInteraction.Cancel -> {
                    isCurrentlyPressed = false
                    compressJob?.cancel()
                    releaseJob?.cancel()
                    launch {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                    if (targetRotation != 0f || rotation.value != 0f) {
                        launch {
                            rotation.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    return remember(scale, rotation) {
        BouncyPressState(scale.asState(), rotation.asState())
    }
}
