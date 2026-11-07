package com.smokingtracker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.repository.SmokingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

data class AchievementUiItem(
    val achievement: AchievementData,
    val isUnlocked: Boolean,
    val unlockDateMillis: Long?,
    val progress: AchievementsManager.AchievementProgress
)

data class AchievementsUiState(
    val items: List<AchievementUiItem> = emptyList(),
    val progressById: Map<String, AchievementsManager.AchievementProgress> = emptyMap(),
    val smokeFreeStreakDays: Int = 0,
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val unlockedAchievements: Set<String> = emptySet(),
    val achievementUnlockDates: Map<String, Long> = emptyMap()
)

class AchievementsViewModel(
    private val repository: SmokingRepository,
    private val appMetaPreferences: AppMetaPreferences,
    private val achievementsCoordinator: AchievementsCoordinator,
    val achievementsManager: AchievementsManager = AchievementsManager(),
    val statisticsManager: StatisticsManager = StatisticsManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    val smokingEntries: StateFlow<List<Long>> = repository.nonResistedTimestamps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val appLaunchDates: StateFlow<List<Long>> = appMetaPreferences.appLaunchDates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unlockedAchievements: StateFlow<Set<String>> = appMetaPreferences.unlockedAchievements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    val achievementUnlockDates: StateFlow<Map<String, Long>> = appMetaPreferences.achievementUnlockDates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val uiState: StateFlow<AchievementsUiState> = combine(
        repository.nonResistedTimestamps,
        appMetaPreferences.appLaunchDates,
        appMetaPreferences.unlockedAchievements,
        appMetaPreferences.achievementUnlockDates
    ) { entries, launches, unlocked, unlockDates ->
        val zoneId = ZoneId.systemDefault()
        val nowMillis = System.currentTimeMillis()
        val sortedEntries = if (entries.isAscending()) entries else entries.sorted()
        val sortedLaunches = if (launches.isAscending()) launches else launches.sorted()

        val streakDays = statisticsManager.currentSmokeFreeStreakDays(
            entries = sortedEntries,
            zoneId = zoneId,
            trackingStartMillis = sortedLaunches.firstOrNull(),
            nowMillis = nowMillis
        )

        val allAchievements = achievementsManager.achievementsList
        val progressMap = HashMap<String, AchievementsManager.AchievementProgress>(allAchievements.size)
        val uiItems = ArrayList<AchievementUiItem>(allAchievements.size)
        var unlockedCount = 0

        for (achievement in allAchievements) {
            val isUnlocked = unlocked.contains(achievement.id)
            if (isUnlocked) unlockedCount++
            val progress = achievementsManager.getAchievementProgress(
                achievementId = achievement.id,
                entries = sortedEntries,
                launches = sortedLaunches,
                zoneId = zoneId,
                nowMillis = nowMillis
            )
            progressMap[achievement.id] = progress
            uiItems.add(
                AchievementUiItem(
                    achievement = achievement,
                    isUnlocked = isUnlocked,
                    unlockDateMillis = unlockDates[achievement.id],
                    progress = progress
                )
            )
        }

        AchievementsUiState(
            items = uiItems,
            progressById = progressMap,
            smokeFreeStreakDays = streakDays,
            unlockedCount = unlockedCount,
            totalCount = allAchievements.size,
            unlockedAchievements = unlocked,
            achievementUnlockDates = unlockDates
        )
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AchievementsUiState(totalCount = achievementsManager.achievementsList.size)
        )

    private val _achievementPopupQueue = MutableStateFlow<List<AchievementData>>(emptyList())
    val achievementPopupQueue: StateFlow<List<AchievementData>> = _achievementPopupQueue.asStateFlow()

    val pendingAchievementPopup: StateFlow<AchievementData?> = _achievementPopupQueue
        .map { it.firstOrNull() }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun dismissAchievementPopup() {
        var dismissed: AchievementData? = null
        _achievementPopupQueue.update { currentQueue ->
            dismissed = currentQueue.firstOrNull()
            if (currentQueue.isEmpty()) currentQueue else currentQueue.drop(1)
        }
        val dismissedId = dismissed?.id ?: return
        safeLaunch(ioDispatcher) {
            appMetaPreferences.removePendingAchievementPopup(dismissedId)
        }
    }

    fun clearAchievementPopupQueue() {
        _achievementPopupQueue.update { emptyList() }
        safeLaunch(ioDispatcher) {
            appMetaPreferences.clearPendingAchievementPopups()
        }
    }

    fun refreshAchievements() {
        safeLaunch(ioDispatcher) {
            achievementsCoordinator.checkAndUpdateSuspend()
        }
    }

    init {
        safeLaunch(ioDispatcher) {
            appMetaPreferences.pendingAchievementPopups.collect { pendingIds ->
                if (pendingIds.isEmpty()) return@collect
                val pendingItems = pendingIds.mapNotNull { achievementsManager.getAchievementById(it) }
                if (pendingItems.isEmpty()) return@collect
                _achievementPopupQueue.update { currentQueue ->
                    val existingIds = currentQueue.map { it.id }.toSet()
                    val missing = pendingItems.filterNot { it.id in existingIds }
                    if (missing.isEmpty()) currentQueue else currentQueue + missing
                }
            }
        }

        safeLaunch(ioDispatcher) {
            achievementsCoordinator.newAchievements.collect { achievement ->
                _achievementPopupQueue.update { currentQueue ->
                    if (currentQueue.any { it.id == achievement.id }) {
                        currentQueue
                    } else {
                        currentQueue + achievement
                    }
                }
            }
        }
    }

    private fun safeLaunch(
        dispatcher: CoroutineDispatcher = ioDispatcher,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error in AchievementsViewModel coroutine", e)
            }
        }
    }

    companion object {
        private const val TAG = "AchievementsViewModel"
    }
}
