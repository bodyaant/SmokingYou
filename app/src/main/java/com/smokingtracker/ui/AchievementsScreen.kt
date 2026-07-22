package com.smokingtracker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.smokingtracker.AchievementsManager
import com.smokingtracker.MainViewModel
import com.smokingtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val entries by viewModel.smokingEntries.collectAsState()
    val launches by viewModel.appLaunchDates.collectAsState()
    val unlockedAchievements by viewModel.unlockedAchievements.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.my_achievements),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        }
    ) { paddingValues ->
        AchievementsTab(
            modifier = Modifier.padding(paddingValues),
            entries = entries,
            launches = launches,
            unlockedAchievements = unlockedAchievements
        )
    }
}

@Composable
fun AchievementsTab(
    modifier: Modifier = Modifier,
    entries: List<Long>,
    launches: List<Long> = emptyList(),
    unlockedAchievements: Set<String>
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val groupedAchievements = AchievementsManager.achievementsList.groupBy { it.category }

        groupedAchievements.forEach { (category, achievements) ->
            item {
                Text(
                    text = stringResource(category.titleResId),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        achievements.forEach { achievement ->
                            val isUnlocked = unlockedAchievements.contains(achievement.id)
                            val isSecretHidden = achievement.isSecret && !isUnlocked

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isUnlocked) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                },
                                border = if (isUnlocked) null else BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isUnlocked) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        contentColor = if (isUnlocked) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isUnlocked) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(achievement.titleResId),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isUnlocked) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isSecretHidden) {
                                                stringResource(R.string.ach_secret_hidden_desc)
                                            } else {
                                                stringResource(achievement.descResId)
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isUnlocked) {
                                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            }
                                        )
                                        if (!isUnlocked && !achievement.isSecret) {
                                            val progress = remember(entries, launches) {
                                                AchievementsManager.progressFraction(achievement.id, entries, launches)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            AnimatedAchievementProgressBar(
                                                targetProgress = progress,
                                                modifier = Modifier.fillMaxWidth(),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                            )
                                            Text(
                                                text = "${(progress * 100).toInt()}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedAchievementProgressBar(
    targetProgress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    trackColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
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

    val progressValue = animProgress.value
    val waveScale = animWaveScale.value

    if (waveScale > 0f) {
        LinearWavyProgressIndicator(
            progress = { progressValue },
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            amplitude = { progressFraction ->
                WavyProgressIndicatorDefaults.indicatorAmplitude(progressFraction) * waveScale
            }
        )
    } else {
        LinearWavyProgressIndicator(
            progress = { progressValue },
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            amplitude = { 0f },
            waveSpeed = 0.dp
        )
    }
}
