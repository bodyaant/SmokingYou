package com.smokingtracker.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smokingtracker.AchievementCategory
import com.smokingtracker.AchievementsManager
import com.smokingtracker.AchievementsViewModel
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import com.smokingtracker.ui.achievements.components.AchievementBentoCard
import com.smokingtracker.ui.achievements.components.AchievementGridTile
import com.smokingtracker.ui.achievements.components.AchievementHeroSection
import com.smokingtracker.ui.achievements.components.CategoryFilterRow
import com.smokingtracker.ui.achievements.sheets.AchievementDetailBottomSheet
import com.smokingtracker.ui.components.entrance
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(viewModel: AchievementsViewModel = koinViewModel(), onBack: () -> Unit) {
    val entries by viewModel.smokingEntries.collectAsStateWithLifecycle()
    val launches by viewModel.appLaunchDates.collectAsStateWithLifecycle()
    val unlockedAchievements by viewModel.unlockedAchievements.collectAsStateWithLifecycle()
    val achievementUnlockDates by viewModel.achievementUnlockDates.collectAsStateWithLifecycle()

    var isGridView by rememberSaveable { mutableStateOf(false) }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        }
    ) { paddingValues ->
        AchievementsTab(
            modifier = Modifier.padding(paddingValues),
            entries = entries,
            launches = launches,
            unlockedAchievements = unlockedAchievements,
            achievementUnlockDates = achievementUnlockDates,
            isGridView = isGridView,
            achievementsManager = viewModel.achievementsManager,
            statisticsManager = viewModel.statisticsManager
        )
    }
}

@Composable
fun AchievementsTab(
    modifier: Modifier = Modifier,
    entries: List<Long>,
    launches: List<Long> = emptyList(),
    unlockedAchievements: Set<String>,
    achievementUnlockDates: Map<String, Long> = emptyMap(),
    isGridView: Boolean = false,
    achievementsManager: AchievementsManager = koinInject(),
    statisticsManager: StatisticsManager = koinInject()
) {
    val manager = achievementsManager
    val allAchievements = remember(manager) { manager.achievementsList }

    val smokeFreeStreakDays by produceState(initialValue = 0, entries, launches, statisticsManager) {
        value = statisticsManager.currentSmokeFreeStreakDaysAsync(
            entries = entries,
            trackingStartMillis = launches.minOrNull()
        )
    }

    var selectedCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedCategory = remember(selectedCategoryName) {
        selectedCategoryName?.let { name -> AchievementCategory.entries.find { it.name == name } }
    }
    var detailAchievementId by rememberSaveable { mutableStateOf<String?>(null) }
    val detailAchievement = remember(detailAchievementId, allAchievements) {
        detailAchievementId?.let { id -> allAchievements.find { it.id == id } }
    }

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

    val listState = rememberLazyListState()
    val rows = remember(displayedAchievements) {
        displayedAchievements.chunked(3)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "hero_section") {
            val heroKey = "hero_section"
            val heroAppeared = appearedItemKeys[heroKey] == true
            AchievementHeroSection(
                smokeFreeStreakDays = smokeFreeStreakDays,
                unlockedCount = unlockedCount,
                totalCount = allAchievements.size,
                modifier = Modifier.entrance(
                    index = 0,
                    alreadyAppeared = heroAppeared,
                    delayMs = 0L,
                    onAppeared = { appearedItemKeys[heroKey] = true }
                )
            )
        }

        item(key = "filter_row") {
            val filterKey = "filter_row"
            val filterAppeared = appearedItemKeys[filterKey] == true
            CategoryFilterRow(
                allAchievements = allAchievements,
                unlockedAchievements = unlockedAchievements,
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategoryName = it?.name },
                modifier = Modifier.entrance(
                    index = 1,
                    alreadyAppeared = filterAppeared,
                    delayMs = if (isInitialCascade) 36L else 0L,
                    onAppeared = { appearedItemKeys[filterKey] = true }
                )
            )
        }

        if (isGridView) {
            itemsIndexed(
                items = rows,
                key = { _, row -> "grid_row_${row.first().id}" }
            ) { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for ((colIndex, achievement) in row.withIndex()) {
                        val itemIndex = rowIndex * 3 + colIndex
                        val isUnlocked = unlockedAchievements.contains(achievement.id)
                        val progress = remember(achievement.id, entries, launches) {
                            manager.getAchievementProgress(achievement.id, entries, launches)
                        }
                        val key = achievement.id
                        val alreadyAppeared = !isInitialCascade || appearedItemKeys[key] == true

                        AchievementGridTile(
                            achievement = achievement,
                            isUnlocked = isUnlocked,
                            progressFraction = progress.fraction,
                            unlockTimestamp = achievementUnlockDates[achievement.id],
                            onClick = { detailAchievementId = achievement.id },
                            modifier = Modifier
                                .weight(1f)
                                .entrance(
                                    index = 2 + itemIndex,
                                    alreadyAppeared = alreadyAppeared,
                                    delayMs = if (isInitialCascade) ((2 + itemIndex) * 36L).coerceAtMost(360L) else 0L,
                                    onAppeared = { appearedItemKeys[key] = true }
                                )
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            itemsIndexed(
                items = displayedAchievements,
                key = { _, achievement -> "bento_${achievement.id}" }
            ) { itemIndex, achievement ->
                val isUnlocked = unlockedAchievements.contains(achievement.id)
                val progress = remember(achievement.id, entries, launches) {
                    manager.getAchievementProgress(achievement.id, entries, launches)
                }
                val key = achievement.id
                val alreadyAppeared = !isInitialCascade || appearedItemKeys[key] == true

                AchievementBentoCard(
                    achievement = achievement,
                    isUnlocked = isUnlocked,
                    progress = progress,
                    unlockTimestamp = achievementUnlockDates[achievement.id],
                    onClick = { detailAchievementId = achievement.id },
                    modifier = Modifier
                        .fillMaxWidth()
                        .entrance(
                            index = 2 + itemIndex,
                            alreadyAppeared = alreadyAppeared,
                            delayMs = if (isInitialCascade) ((2 + itemIndex) * 36L).coerceAtMost(360L) else 0L,
                            onAppeared = { appearedItemKeys[key] = true }
                        )
                )
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
            onDismissRequest = { detailAchievementId = null }
        )
    }
}
