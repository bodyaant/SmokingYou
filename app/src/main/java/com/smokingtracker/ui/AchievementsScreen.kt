package com.smokingtracker.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smokingtracker.Achievement
import com.smokingtracker.AchievementCategory
import com.smokingtracker.AchievementsManager
import com.smokingtracker.MainViewModel
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import com.smokingtracker.ui.components.BadgeMedallion
import com.smokingtracker.ui.components.ConfettiBurst
import com.smokingtracker.ui.components.entrance
import com.smokingtracker.ui.components.pulse
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val entries by viewModel.smokingEntries.collectAsStateWithLifecycle()
    val launches by viewModel.appLaunchDates.collectAsStateWithLifecycle()
    val unlockedAchievements by viewModel.unlockedAchievements.collectAsStateWithLifecycle()
    val achievementUnlockDates by viewModel.achievementUnlockDates.collectAsStateWithLifecycle()

    var isGridView by remember { mutableStateOf(false) }

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
                        modifier = Modifier.padding(start = 12.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = { isGridView = !isGridView },
                        modifier = Modifier.padding(end = 12.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        AnimatedContent(
                            targetState = isGridView,
                            transitionSpec = {
                                (fadeIn(tween(180)) + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 0.75f))
                                    .togetherWith(fadeOut(tween(140)) + scaleOut(animationSpec = tween(140), targetScale = 0.75f))
                            },
                            label = "viewModeIconTransition"
                        ) { grid ->
                            Icon(
                                imageVector = if (grid) Icons.Filled.ViewAgenda else Icons.Filled.GridView,
                                contentDescription = stringResource(if (grid) R.string.ach_view_list else R.string.ach_view_grid)
                            )
                        }
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
            unlockedAchievements = unlockedAchievements,
            achievementUnlockDates = achievementUnlockDates,
            isGridView = isGridView
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AchievementsTab(
    modifier: Modifier = Modifier,
    entries: List<Long>,
    launches: List<Long> = emptyList(),
    unlockedAchievements: Set<String>,
    achievementUnlockDates: Map<String, Long> = emptyMap(),
    isGridView: Boolean = false
) {
    val manager = remember { AchievementsManager() }
    val allAchievements = remember { manager.achievementsList }

    val smokeFreeStreakDays = remember(entries) {
        StatisticsManager().currentSmokeFreeStreakDays(entries)
    }

    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }
    var detailAchievement by remember { mutableStateOf<Achievement?>(null) }

    val displayedAchievements = remember(selectedCategory, allAchievements) {
        if (selectedCategory == null) {
            allAchievements
        } else {
            allAchievements.filter { it.category == selectedCategory }
        }
    }

    val appearedItemKeys = remember { mutableStateMapOf<String, Boolean>() }
    var isInitialCascade by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(600L)
        isInitialCascade = false
    }

    val unlockedCount = remember(unlockedAchievements, allAchievements) {
        allAchievements.count { unlockedAchievements.contains(it.id) }
    }

    val gridState = rememberLazyGridState()
    val listState = rememberLazyGridState()

    LaunchedEffect(isGridView) {
        if (isGridView) {
            val listIndex = listState.firstVisibleItemIndex
            val listOffset = listState.firstVisibleItemScrollOffset
            gridState.scrollToItem(listIndex, listOffset)
        } else {
            val gridIndex = gridState.firstVisibleItemIndex
            val gridOffset = gridState.firstVisibleItemScrollOffset
            listState.scrollToItem(gridIndex, gridOffset)
        }
    }

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = isGridView,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) togetherWith
                fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
            },
            label = "viewModeSharedTransition"
        ) { targetIsGrid ->
            val columns = if (targetIsGrid) GridCells.Fixed(3) else GridCells.Fixed(1)
            val currentState = if (targetIsGrid) gridState else listState

            LazyVerticalGrid(
                columns = columns,
                state = currentState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(if (targetIsGrid) 3 else 1) }) {
                    val key = "hero_section"
                    val alreadyAppeared = appearedItemKeys[key] == true
                    AchievementHeroSection(
                        smokeFreeStreakDays = smokeFreeStreakDays,
                        unlockedCount = unlockedCount,
                        totalCount = allAchievements.size,
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "hero_section"),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = { _, _ -> spring(dampingRatio = 0.82f, stiffness = 380f) }
                            )
                            .entrance(
                                index = 0,
                                alreadyAppeared = alreadyAppeared,
                                delayMs = if (isInitialCascade) 0L else 0L,
                                onAppeared = { appearedItemKeys[key] = true }
                            )
                    )
                }

                item(span = { GridItemSpan(if (targetIsGrid) 3 else 1) }) {
                    val key = "filter_row"
                    val alreadyAppeared = appearedItemKeys[key] == true
                    CategoryFilterRow(
                        allAchievements = allAchievements,
                        unlockedAchievements = unlockedAchievements,
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it },
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "filter_row"),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = { _, _ -> spring(dampingRatio = 0.82f, stiffness = 380f) }
                            )
                            .entrance(
                                index = 1,
                                alreadyAppeared = alreadyAppeared,
                                delayMs = if (isInitialCascade) 36L else 0L,
                                onAppeared = { appearedItemKeys[key] = true }
                            )
                    )
                }

                itemsIndexed(
                    items = displayedAchievements,
                    key = { _, ach -> ach.id }
                ) { index, achievement ->
                    val isUnlocked = unlockedAchievements.contains(achievement.id)
                    val progress = remember(achievement.id, entries, launches) {
                        manager.getAchievementProgress(achievement.id, entries, launches)
                    }
                    val key = achievement.id
                    val alreadyAppeared = appearedItemKeys[key] == true
                    val itemModifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "card_${achievement.id}"),
                            animatedVisibilityScope = this@AnimatedContent,
                            boundsTransform = { _, _ -> spring(dampingRatio = 0.82f, stiffness = 380f) }
                        )
                        .entrance(
                            index = 2 + index,
                            alreadyAppeared = alreadyAppeared,
                            delayMs = if (isInitialCascade) ((2 + index) * 36L).coerceAtMost(360L) else 0L,
                            onAppeared = { appearedItemKeys[key] = true }
                        )

                    if (targetIsGrid) {
                        AchievementGridTile(
                            achievement = achievement,
                            isUnlocked = isUnlocked,
                            progressFraction = progress.fraction,
                            onClick = { detailAchievement = achievement },
                            modifier = itemModifier
                        )
                    } else {
                        AchievementBentoCard(
                            achievement = achievement,
                            isUnlocked = isUnlocked,
                            progress = progress,
                            unlockTimestamp = achievementUnlockDates[achievement.id],
                            onClick = { detailAchievement = achievement },
                            modifier = itemModifier
                        )
                    }
                }
            }
        }
    }

    detailAchievement?.let { achievement ->
        val isUnlocked = unlockedAchievements.contains(achievement.id)
        val progress = remember(achievement.id, entries, launches) {
            manager.getAchievementProgress(achievement.id, entries, launches)
        }

        AchievementDetailBottomSheet(
            achievement = achievement,
            isUnlocked = isUnlocked,
            progress = progress,
            unlockTimestamp = achievementUnlockDates[achievement.id],
            onDismissRequest = { detailAchievement = null }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AchievementHeroSection(
    smokeFreeStreakDays: Int,
    unlockedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (totalCount > 0) (unlockedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
    val percentage = (progressFraction * 100).toInt()

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
                    modifier = Modifier.pulse(from = 0.94f, to = 1.05f, durationMs = 1500)
                ) {
                    BadgeMedallion(
                        id = if (smokeFreeStreakDays > 0) "login_30" else "nosmoke_1y",
                        category = if (smokeFreeStreakDays > 0) AchievementCategory.LOGIN else AchievementCategory.NO_SMOKE,
                        isUnlocked = true,
                        size = 64.dp
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

                val animProgress = remember { Animatable(0f) }
                LaunchedEffect(progressFraction) {
                    animProgress.animateTo(
                        targetValue = progressFraction,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing)
                    )
                }

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

@Composable
private fun CategoryFilterRow(
    allAchievements: List<Achievement>,
    unlockedAchievements: Set<String>,
    selectedCategory: AchievementCategory?,
    onSelectCategory: (AchievementCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember {
        listOf(
            null,
            AchievementCategory.NO_SMOKE,
            AchievementCategory.LOGIN,
            AchievementCategory.SECRET
        )
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(categories, key = { it?.name ?: "ALL" }) { category ->
            val isSelected = selectedCategory == category
            val title = if (category == null) {
                stringResource(R.string.ach_category_all)
            } else {
                stringResource(category.titleResId)
            }

            val matchingAchievements = remember(category, allAchievements) {
                if (category == null) allAchievements else allAchievements.filter { it.category == category }
            }
            val countUnlocked = remember(matchingAchievements, unlockedAchievements) {
                matchingAchievements.count { unlockedAchievements.contains(it.id) }
            }

            val interactionSource = remember { MutableInteractionSource() }
            val bouncyState = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.86f)

            FilterChip(
                selected = isSelected,
                onClick = {
                    bouncyState.bounce()
                    onSelectCategory(category)
                },
                interactionSource = interactionSource,
                label = {
                    Text(
                        text = "$title ($countUnlocked/${matchingAchievements.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = null,
                shape = RoundedCornerShape(100),
                modifier = Modifier.bouncyPress(bouncyState)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AchievementGridTile(
    achievement: Achievement,
    isUnlocked: Boolean,
    progressFraction: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bouncyState = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.88f)

    Surface(
        onClick = {
            bouncyState.bounce()
            onClick()
        },
        interactionSource = interactionSource,
        shape = containerShape(RoundedCornerShape(22.dp)),
        color = if (isUnlocked) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        modifier = modifier
            .bouncyPress(bouncyState)
            .fillMaxWidth()
            .height(136.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 14.dp, bottom = 12.dp, start = 8.dp, end = 8.dp),
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
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isUnlocked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isUnlocked && !achievement.isSecret) {
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AchievementBentoCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    progress: AchievementsManager.AchievementProgress,
    unlockTimestamp: Long? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bouncyState = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.90f)

    Surface(
        onClick = {
            bouncyState.bounce()
            onClick()
        },
        interactionSource = interactionSource,
        shape = containerShape(RoundedCornerShape(24.dp)),
        color = if (isUnlocked) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
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
                    Spacer(modifier = Modifier.height(4.dp))
                    val formattedDate = remember(unlockTimestamp) {
                        SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(unlockTimestamp))
                    }
                    Text(
                        text = stringResource(R.string.ach_unlocked_date, formattedDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                }

                if (!isUnlocked && !achievement.isSecret) {
                    Spacer(modifier = Modifier.height(10.dp))

                    val animProgress = remember { Animatable(0f) }
                    LaunchedEffect(progress.fraction) {
                        animProgress.animateTo(
                            progress.fraction,
                            animationSpec = tween(800, easing = FastOutSlowInEasing)
                        )
                    }

                    LinearWavyProgressIndicator(
                        progress = { animProgress.value },
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AchievementDetailBottomSheet(
    achievement: Achievement,
    isUnlocked: Boolean,
    progress: AchievementsManager.AchievementProgress,
    unlockTimestamp: Long? = null,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confettiKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(achievement.id, isUnlocked) {
        if (isUnlocked) {
            confettiKey++
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = if (MaterialTheme.colorScheme.surfaceContainerLow == Color.White) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ConfettiBurst(burstKey = confettiKey, modifier = Modifier.fillMaxWidth().height(260.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = if (isUnlocked) Modifier.pulse(0.96f, 1.04f, 1600) else Modifier
                ) {
                    BadgeMedallion(
                        id = achievement.id,
                        category = achievement.category,
                        isUnlocked = isUnlocked,
                        isSecret = achievement.isSecret,
                        size = 84.dp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    contentColor = if (isUnlocked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ) {
                    Text(
                        text = stringResource(achievement.category.titleResId),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (achievement.isSecret && !isUnlocked) {
                            stringResource(R.string.ach_status_secret)
                        } else {
                            stringResource(achievement.titleResId)
                        },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (achievement.isSecret && !isUnlocked) {
                            stringResource(R.string.ach_secret_locked_hint)
                        } else {
                            stringResource(achievement.descResId)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                if (isUnlocked && unlockTimestamp != null && unlockTimestamp > 0L) {
                    val formattedDate = remember(unlockTimestamp) {
                        SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(unlockTimestamp))
                    }
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.ach_unlocked_date, formattedDate),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                if (!isUnlocked && !achievement.isSecret) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = containerShape(RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.ach_status_in_progress),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(progress.fraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            LinearWavyProgressIndicator(
                                progress = { progress.fraction },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                amplitude = { 1f }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${progress.currentDisplay} / ${progress.targetDisplay}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (progress.remainingDisplay != null) {
                                    Text(
                                        text = stringResource(R.string.ach_progress_remaining, progress.remainingDisplay),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(R.string.ach_close),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
