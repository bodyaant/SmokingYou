package com.smokingtracker.ui.components.statistics.cards

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.rememberBouncyPress
import com.smokingtracker.ui.theme.subContainer

@Composable
fun StreakBentoCard(
    currentStreakDays: Int,
    longestStreakDays: Int,
    onClick: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.95f)

    val primaryColor = MaterialTheme.colorScheme.primary

    val milestoneTiers = remember { listOf(1, 3, 7, 14, 30, 90, 365) }
    val currentTier = remember(currentStreakDays) {
        milestoneTiers.lastOrNull { currentStreakDays >= it } ?: 0
    }
    val nextMilestone = remember(currentStreakDays) {
        milestoneTiers.firstOrNull { currentStreakDays < it } ?: (currentStreakDays + 30)
    }

    val tierProgress = remember(currentStreakDays, currentTier, nextMilestone) {
        val span = (nextMilestone - currentTier).coerceAtLeast(1)
        ((currentStreakDays - currentTier).toFloat() / span.toFloat()).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = tierProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "streakProgress"
    )

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .bouncyPress(bouncy)
            .clickable(interactionSource = interactionSource, indication = null) {
                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                onClick()
            },
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 5.dp.toPx()
                val radius = ((size.minDimension - strokeWidth - 16.dp.toPx()) / 2f).coerceAtLeast(0f)
                if (radius <= 0f) return@Canvas
                val arcCenter = Offset(size.width / 2f, size.height / 2f)

                drawCircle(
                    color = primaryColor.copy(alpha = 0.12f),
                    radius = radius,
                    center = arcCenter,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val sweepAngle = animatedProgress * 360f
                if (sweepAngle > 0f) {
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(arcCenter.x - radius, arcCenter.y - radius),
                        size = Size(radius * 2f, radius * 2f),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$currentStreakDays",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, lineHeight = 36.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = pluralStringResource(R.plurals.stats_days_plural, currentStreakDays, currentStreakDays),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.subContainer
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.stats_days_plural,
                            longestStreakDays,
                            longestStreakDays
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
