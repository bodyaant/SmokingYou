package com.smokingtracker.ui.achievements.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.AchievementCategory
import com.smokingtracker.R
import com.smokingtracker.ui.components.BadgeMedallion
import com.smokingtracker.ui.components.pulse
import com.smokingtracker.ui.theme.containerShape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AchievementHeroSection(
    smokeFreeStreakDays: Int,
    unlockedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (totalCount > 0) (unlockedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
    val percentage = (progressFraction * 100).toInt()
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(progressFraction) {
        animProgress.animateTo(
            targetValue = progressFraction,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.pulse(from = 0.94f, to = 1.05f, durationMs = 1500)
                ) {
                    CircularProgressIndicator(
                        progress = { animProgress.value },
                        modifier = Modifier.size(76.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        strokeWidth = 3.5.dp
                    )
                    BadgeMedallion(
                        id = if (smokeFreeStreakDays > 0) "login_30" else "nosmoke_1y",
                        category = if (smokeFreeStreakDays > 0) AchievementCategory.LOGIN else AchievementCategory.NO_SMOKE,
                        isUnlocked = true,
                        size = 58.dp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (smokeFreeStreakDays > 0) {
                        Text(
                            text = stringResource(R.string.ach_streak_days, smokeFreeStreakDays),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.ach_streak_smokefree),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.ach_hero_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.achievements_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.ach_unlocked_count, unlockedCount, totalCount, percentage),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LinearWavyProgressIndicator(
                    progress = { animProgress.value },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    amplitude = { 1f }
                )
            }
        }
    }
}
