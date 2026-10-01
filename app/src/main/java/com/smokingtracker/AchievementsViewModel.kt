package com.smokingtracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.repository.SmokingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AchievementsViewModel(
    private val repository: SmokingRepository,
    private val appMetaPreferences: AppMetaPreferences,
    private val achievementsCoordinator: AchievementsCoordinator
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

    private val _achievementPopupQueue = MutableStateFlow<List<Achievement>>(emptyList())
    val pendingAchievementPopup: StateFlow<Achievement?> = _achievementPopupQueue
        .map { it.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun dismissAchievementPopup() {
        _achievementPopupQueue.value = _achievementPopupQueue.value.drop(1)
    }

    init {
        viewModelScope.launch {
            achievementsCoordinator.newAchievements.collect { achievement ->
                _achievementPopupQueue.value = _achievementPopupQueue.value + achievement
            }
        }
    }
}
