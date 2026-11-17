package com.smokingtracker.ui.achievements.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.AchievementData
import com.smokingtracker.AchievementsManager
import com.smokingtracker.R
import com.smokingtracker.ui.components.BadgeMedallion
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.rememberBouncyPress
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val achievementBentoCardShape = RoundedCornerShape(24.dp)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AchievementBentoCard(
    achievement: AchievementData,
    isUnlocked: Boolean,
    progress: AchievementsManager.AchievementProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    unlockTimestamp: Long? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bouncyState = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.90f)

    Surface(
        onClick = {
            bouncyState.bounce()
            onClick()
        },
        interactionSource = interactionSource,
        shape = achievementBentoCardShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .bouncyPress(bouncyState)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeMedallion(
                id = achievement.id,
                category = achievement.category,
                isUnlocked = isUnlocked,
                isSecret = achievement.isSecret,
                size = 56.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (achievement.isSecret && !isUnlocked) {
                            stringResource(R.string.ach_category_secret)
                        } else {
                            stringResource(achievement.titleResId)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isUnlocked) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ach_status_unlocked),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (achievement.isSecret && !isUnlocked) {
                        stringResource(R.string.ach_secret_hidden_desc)
                    } else {
                        stringResource(achievement.descResId)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                if (isUnlocked && unlockTimestamp != null && unlockTimestamp > 0L) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val locale = LocalLocale.current.platformLocale
                    val formattedDate = remember(unlockTimestamp, locale) {
                        val unlockDate = Instant.ofEpochMilli(unlockTimestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        DateTimeFormatter.ofPattern("d MMMM yyyy", locale).format(unlockDate)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    }
                }

                if (!isUnlocked && !achievement.isSecret) {
                    Spacer(modifier = Modifier.height(10.dp))

                    LinearWavyProgressIndicator(
                        progress = { progress.fraction.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        amplitude = { 1f }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${progress.currentDisplay} / ${progress.targetDisplay}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(progress.fraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
