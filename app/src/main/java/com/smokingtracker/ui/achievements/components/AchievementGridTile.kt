package com.smokingtracker.ui.achievements.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokingtracker.AchievementData
import com.smokingtracker.R
import com.smokingtracker.ui.components.BadgeMedallion
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.rememberBouncyPress
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val achievementGridCardShape = RoundedCornerShape(22.dp)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AchievementGridTile(
    achievement: AchievementData,
    isUnlocked: Boolean,
    progressFraction: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    unlockTimestamp: Long? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bouncyState = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.88f)
    val secretLockedLabel = stringResource(R.string.ach_status_secret)
    val locale = LocalLocale.current.platformLocale

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val neonGlowBorder = remember(isUnlocked, primaryColor, tertiaryColor) {
        if (isUnlocked) {
            Modifier.border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.65f),
                        tertiaryColor.copy(alpha = 0.45f),
                        primaryColor.copy(alpha = 0.25f)
                    )
                ),
                shape = achievementGridCardShape
            )
        } else {
            Modifier
        }
    }

    Surface(
        onClick = {
            bouncyState.bounce()
            onClick()
        },
        interactionSource = interactionSource,
        shape = achievementGridCardShape,
        color = if (isUnlocked) {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        modifier = modifier
            .bouncyPress(bouncyState)
            .then(neonGlowBorder)
            .fillMaxWidth()
            .heightIn(min = 148.dp)
            .then(
                if (achievement.isSecret && !isUnlocked) {
                    Modifier.semantics(mergeDescendants = true) {
                        contentDescription = secretLockedLabel
                    }
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 13.dp, bottom = 11.dp, start = 6.dp, end = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            BadgeMedallion(
                id = achievement.id,
                category = achievement.category,
                isUnlocked = isUnlocked,
                isSecret = achievement.isSecret,
                size = 54.dp
            )

            Text(
                text = if (achievement.isSecret && !isUnlocked) {
                    "???"
                } else {
                    stringResource(achievement.titleResId)
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp
                ),
                color = if (isUnlocked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    val formattedDate = remember(unlockTimestamp, locale) {
                        if (unlockTimestamp != null && unlockTimestamp > 0L) {
                            val nowYear = LocalDate.now().year
                            val unlockDate = Instant.ofEpochMilli(unlockTimestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val pattern = if (nowYear == unlockDate.year) "d MMM" else "d MMM yyyy"
                            DateTimeFormatter.ofPattern(pattern, locale).format(unlockDate)
                        } else {
                            null
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = formattedDate ?: stringResource(R.string.ach_status_unlocked),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else if (achievement.isSecret) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(12.dp)
                    )
                } else {
                    LinearWavyProgressIndicator(
                        progress = { progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(0.88f),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        amplitude = { 1f }
                    )
                }
            }
        }
    }
}
