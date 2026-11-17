package com.smokingtracker.ui.components.statistics.cards

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
import androidx.compose.material.icons.filled.MedicalServices
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.rememberBouncyPress
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WhoRecoveryBentoCard(
    completedCount: Int,
    totalCount: Int,
    nextMilestoneTimeStr: String?,
    onClick: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.95f)

    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val inactiveDotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)
    val progress = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()
    val recoveryWhoDesc = stringResource(R.string.stats_body_recovery_who)

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .bouncyPress(bouncy)
            .clickable(interactionSource = interactionSource, indication = null) {
                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                onClick()
            }
            .semantics {
                contentDescription = recoveryWhoDesc
            },
        shape = MaterialShapes.Cookie12Sided.toShape(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val radius = min(size.width, size.height) / 2f
                val dotDistance = radius * 0.87f
                val dotRadius = 2.75.dp.toPx()
                val glowRadius = 5.dp.toPx()

                for (i in 0 until 12) {
                    val angleRad = (-Math.PI / 2.0 + i * (2.0 * Math.PI / 12.0)).toFloat()
                    val x = centerX + dotDistance * cos(angleRad)
                    val y = centerY + dotDistance * sin(angleRad)

                    if (i < completedCount) {
                        drawCircle(
                            color = tertiaryColor.copy(alpha = 0.28f),
                            radius = glowRadius,
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = tertiaryColor,
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                    } else {
                        drawCircle(
                            color = inactiveDotColor,
                            radius = dotRadius * 0.85f,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MedicalServices,
                    contentDescription = null,
                    tint = tertiaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, lineHeight = 36.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    shape = CircleShape,
                    color = tertiaryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$completedCount / $totalCount",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = tertiaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
