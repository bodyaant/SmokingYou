package com.smokingtracker.ui.components.statistics.cards

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.components.statistics.formatMinutesShort
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LifeReclaimedBentoCard(
    lifeMinutes: Int,
    onClick: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.95f)

    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val formattedLife = remember(lifeMinutes, context) {
        formatMinutesShort(lifeMinutes, context)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ekgTransition")
    val heartScaleState = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartPulse"
    )
    val ekgPhaseState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ekgPhase"
    )
    val ekgPath = remember { Path() }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .bouncyPress(bouncy)
            .clickable(interactionSource = interactionSource, indication = null) {
                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                onClick()
            },
        shape = containerShape(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        val scale = heartScaleState.value
                        scaleX = scale
                        scaleY = scale
                    }
                ) {
                    Surface(
                        shape = MaterialShapes.Sunny.toShape(),
                        color = tertiaryColor.copy(alpha = 0.18f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = tertiaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.stats_life_saved),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedLife,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                val width = size.width
                val height = size.height
                if (width <= 0f || height <= 0f) return@Canvas
                val midY = height * 0.5f

                ekgPath.rewind()
                ekgPath.moveTo(0f, midY)

                val cycleCount = 3
                val cycleWidth = (width / cycleCount.toFloat()).coerceAtLeast(1f)

                for (i in 0 until cycleCount) {
                    val startX = i * cycleWidth
                    ekgPath.lineTo(startX + cycleWidth * 0.20f, midY)
                    ekgPath.lineTo(startX + cycleWidth * 0.28f, midY - height * 0.22f)
                    ekgPath.lineTo(startX + cycleWidth * 0.36f, midY)
                    ekgPath.lineTo(startX + cycleWidth * 0.44f, midY + height * 0.12f)
                    ekgPath.lineTo(startX + cycleWidth * 0.52f, midY - height * 0.48f)
                    ekgPath.lineTo(startX + cycleWidth * 0.60f, midY + height * 0.28f)
                    ekgPath.lineTo(startX + cycleWidth * 0.68f, midY)
                    ekgPath.lineTo(startX + cycleWidth * 0.78f, midY - height * 0.18f)
                    ekgPath.lineTo(startX + cycleWidth * 0.88f, midY)
                    ekgPath.lineTo(startX + cycleWidth, midY)
                }

                drawPath(
                    path = ekgPath,
                    color = tertiaryColor.copy(alpha = 0.35f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                val dotX = width * ekgPhaseState.value
                val cycleIndex = (dotX / cycleWidth).toInt().coerceIn(0, cycleCount - 1)
                val localX = dotX - (cycleIndex * cycleWidth)
                val normX = localX / cycleWidth

                val dotY = when {
                    normX < 0.20f -> midY
                    normX < 0.28f -> midY + ((normX - 0.20f) / 0.08f) * (-height * 0.22f)
                    normX < 0.36f -> (midY - height * 0.22f) + ((normX - 0.28f) / 0.08f) * (height * 0.22f)
                    normX < 0.44f -> midY + ((normX - 0.36f) / 0.08f) * (height * 0.12f)
                    normX < 0.52f -> (midY + height * 0.12f) + ((normX - 0.44f) / 0.08f) * (-height * 0.60f)
                    normX < 0.60f -> (midY - height * 0.48f) + ((normX - 0.52f) / 0.08f) * (height * 0.76f)
                    normX < 0.68f -> (midY + height * 0.28f) + ((normX - 0.60f) / 0.08f) * (-height * 0.28f)
                    normX < 0.78f -> midY + ((normX - 0.68f) / 0.10f) * (-height * 0.18f)
                    normX < 0.88f -> (midY - height * 0.18f) + ((normX - 0.78f) / 0.10f) * (height * 0.18f)
                    else -> midY
                }

                drawCircle(
                    color = tertiaryColor.copy(alpha = 0.3f),
                    radius = 7.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
                drawCircle(
                    color = tertiaryColor,
                    radius = 3.5.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }
    }
}
