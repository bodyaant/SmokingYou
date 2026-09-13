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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BouncyPressState(
    val scale: State<Float>,
    val rotation: State<Float>,
    private val onBounce: () -> Unit = {}
) {
    fun bounce() {
        onBounce()
    }
}

fun Modifier.bouncyPress(state: BouncyPressState): Modifier = this.graphicsLayer {
    scaleX = state.scale.value
    scaleY = state.scale.value
    rotationZ = state.rotation.value
}

private class BouncyPressTracker {
    var isCurrentlyPressed = false
    var compressJob: Job? = null
    var releaseJob: Job? = null
}

@Composable
fun rememberBouncyPress(
    interactionSource: MutableInteractionSource,
    targetScale: Float = 0.86f,
    targetRotation: Float = 0f,
    minHoldTimeMs: Long = 35L,
    compressStiffness: Float = Spring.StiffnessMedium,
    releaseDampingRatio: Float = Spring.DampingRatioMediumBouncy,
    releaseStiffness: Float = Spring.StiffnessLow
): BouncyPressState {
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val tracker = remember { BouncyPressTracker() }

    val onBounce: () -> Unit = remember(
        tracker,
        targetScale,
        targetRotation,
        compressStiffness,
        releaseDampingRatio,
        releaseStiffness,
        minHoldTimeMs
    ) {
        {
            val isBusy = tracker.isCurrentlyPressed || tracker.releaseJob?.isActive == true
            if (!isBusy) {
                tracker.compressJob?.cancel()
                tracker.releaseJob?.cancel()
                tracker.releaseJob = scope.launch {
                    coroutineScope {
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
                    delay(minHoldTimeMs)
                    coroutineScope {
                        launch {
                            scale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = releaseDampingRatio,
                                    stiffness = releaseStiffness
                                )
                            )
                        }
                        if (targetRotation != 0f) {
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
        }
    }

    LaunchedEffect(interactionSource, targetScale, targetRotation) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    tracker.releaseJob?.cancel()
                    tracker.isCurrentlyPressed = true
                    tracker.compressJob?.cancel()
                    tracker.compressJob = launch {
                        coroutineScope {
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
                }
                is PressInteraction.Release -> {
                    tracker.isCurrentlyPressed = false
                    tracker.releaseJob?.cancel()
                    tracker.releaseJob = launch {
                        tracker.compressJob?.join()
                        delay(minHoldTimeMs)
                        coroutineScope {
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
                    tracker.isCurrentlyPressed = false
                    tracker.compressJob?.cancel()
                    tracker.releaseJob?.cancel()
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

    return remember(scale, rotation, onBounce) {
        BouncyPressState(scale.asState(), rotation.asState(), onBounce)
    }
}
