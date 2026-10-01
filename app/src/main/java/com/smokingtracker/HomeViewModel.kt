package com.smokingtracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.TriggerItem
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.repository.TriggerRepository
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeFabUiAction {
    data object OpenSmokedDialog : HomeFabUiAction
    data object OpenMindfulPauseDialog : HomeFabUiAction
    data object ResistedCraving : HomeFabUiAction
}

class HomeViewModel(
    private val repository: SmokingRepository,
    private val triggerRepository: TriggerRepository,
    private val userPreferences: UserPreferences,
    private val appMetaPreferences: AppMetaPreferences,
    private val achievementsCoordinator: AchievementsCoordinator,
    application: Application
) : AndroidViewModel(application) {

    private val _fabActionEvents = MutableSharedFlow<HomeFabUiAction>(extraBufferCapacity = 1)
    val fabActionEvents = _fabActionEvents.asSharedFlow()

    fun triggerFabSmoked() {
        viewModelScope.launch {
            _fabActionEvents.emit(HomeFabUiAction.OpenSmokedDialog)
        }
    }

    fun triggerFabMindfulPause() {
        viewModelScope.launch {
            _fabActionEvents.emit(HomeFabUiAction.OpenMindfulPauseDialog)
        }
    }

    fun triggerFabResisted() {
        viewModelScope.launch {
            _fabActionEvents.emit(HomeFabUiAction.ResistedCraving)
        }
    }

    val smokingEntries: StateFlow<List<Long>> = repository.nonResistedTimestamps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSmokingEntities: StateFlow<List<SmokingEntryEntity>> = repository.smokingEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resistedEntries: StateFlow<List<SmokingEntryEntity>> = repository.resistedEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyLimit: StateFlow<Int> = userPreferences.dailyLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unlockedAchievements: StateFlow<Set<String>> = appMetaPreferences.unlockedAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val customTriggers: StateFlow<List<String>> = triggerRepository.customTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val disabledDefaultTriggers: StateFlow<Set<String>> = triggerRepository.disabledDefaultTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val activeTriggers: StateFlow<List<TriggerItem>> = triggerRepository.activeTriggers
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            TriggerType.allEntries().map { TriggerItem.fromBuiltIn(it) }
        )

    private val _showTaperingCheckIn = MutableStateFlow(false)
    val showTaperingCheckIn: StateFlow<Boolean> = _showTaperingCheckIn.asStateFlow()

    val taperingIntervalDays: StateFlow<Int> = userPreferences.taperingIntervalDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    fun addCustomTrigger(name: String, onResult: (String?) -> Unit = {}) {
        viewModelScope.launch {
            val added = triggerRepository.addCustomTrigger(name)
            onResult(added)
        }
    }

    fun removeCustomTrigger(name: String) {
        viewModelScope.launch {
            triggerRepository.removeCustomTrigger(name)
        }
    }

    fun toggleDefaultTrigger(key: String, isEnabled: Boolean) {
        viewModelScope.launch {
            triggerRepository.toggleDefaultTrigger(key, isEnabled)
        }
    }

    init {
        viewModelScope.launch {
            checkTaperingPlanEligibility()
        }
    }

    fun addSmokingEntry(timestamp: Long = System.currentTimeMillis()) {
        addSmokingEntryWithTrigger(timestamp, null)
    }

    fun addSmokingEntryWithTrigger(timestamp: Long = System.currentTimeMillis(), trigger: String?) {
        viewModelScope.launch {
            repository.addEntry(timestamp, trigger)
            val updated = smokingEntries.value.toMutableList().apply {
                add(timestamp)
                sort()
            }
            achievementsCoordinator.checkAndUpdate(updated)
            WidgetUpdateManager.updateAllAsync(getApplication())
        }
    }

    fun addResistedEntry(trigger: String?, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.addResistedEntry(timestamp, trigger)
            WidgetUpdateManager.updateAllAsync(getApplication())
        }
    }

    fun removeSmokingEntry(id: Long, timestamp: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (now - timestamp <= 10_000L) {
                appMetaPreferences.setHasCancelledWithin10s(true)
            }
            repository.removeEntryById(id)
            val updated = smokingEntries.value.toMutableList().apply {
                remove(timestamp)
            }
            achievementsCoordinator.checkAndUpdate(updated, wasEntryRemoved = true)
            WidgetUpdateManager.updateAllAsync(getApplication())
        }
    }

    fun editSmokingEntry(id: Long, oldTimestamp: Long, newTimestamp: Long) {
        viewModelScope.launch {
            repository.updateEntryTimestampById(id, newTimestamp)
            val updated = smokingEntries.value.toMutableList().apply {
                remove(oldTimestamp)
                add(newTimestamp)
                sort()
            }
            achievementsCoordinator.checkAndUpdate(updated)
            WidgetUpdateManager.updateAllAsync(getApplication())
        }
    }

    fun updateSmokingEntryTrigger(id: Long, trigger: String?) {
        viewModelScope.launch {
            repository.updateEntryTriggerById(id, trigger)
            WidgetUpdateManager.updateAllAsync(getApplication())
        }
    }

    fun checkTaperingPlanEligibility() {
        viewModelScope.launch {
            val enabled = userPreferences.taperingPlanEnabled.first()
            if (!enabled) return@launch
            val intervalDays = userPreferences.taperingIntervalDays.first()
            val lastCheckin = userPreferences.lastTaperingCheckinDate.first()
            val now = System.currentTimeMillis()
            val limit = userPreferences.dailyLimit.first()
            if (limit <= 0) return@launch

            val daysPassed = if (lastCheckin > 0) {
                ((now - lastCheckin) / (1000 * 60 * 60 * 24)).toInt()
            } else {
                intervalDays
            }

            if (daysPassed >= intervalDays) {
                _showTaperingCheckIn.value = true
            }
        }
    }

    fun dismissTaperingCheckIn() {
        _showTaperingCheckIn.value = false
    }

    fun acceptTaperingReduction() {
        viewModelScope.launch {
            val currentLimit = userPreferences.dailyLimit.first()
            val newLimit = (currentLimit - 1).coerceAtLeast(0)
            userPreferences.setDailyLimit(newLimit)
            userPreferences.updateLastTaperingCheckinDate(System.currentTimeMillis())
            _showTaperingCheckIn.value = false
        }
    }

    fun keepTaperingLimit() {
        viewModelScope.launch {
            userPreferences.updateLastTaperingCheckinDate(System.currentTimeMillis())
            _showTaperingCheckIn.value = false
        }
    }

    fun snoozeTaperingCheckIn() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val intervalMs = userPreferences.taperingIntervalDays.first() * 24L * 60L * 60L * 1000L
            val snoozeMs = 3L * 24L * 60L * 60L * 1000L
            userPreferences.updateLastTaperingCheckinDate(now - intervalMs + snoozeMs)
            _showTaperingCheckIn.value = false
        }
    }
}
