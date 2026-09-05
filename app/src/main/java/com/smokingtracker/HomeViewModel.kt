package com.smokingtracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: SmokingRepository,
    private val dataStoreManager: DataStoreManager,
    private val achievementsCoordinator: AchievementsCoordinator,
    application: Application
) : AndroidViewModel(application) {

    val smokingEntries: StateFlow<List<Long>> = repository.smokingEntries
        .map { entities -> entities.filter { !it.isResisted }.map { it.timestamp } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSmokingEntities: StateFlow<List<SmokingEntryEntity>> = repository.smokingEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resistedEntries: StateFlow<List<SmokingEntryEntity>> = repository.smokingEntries
        .map { entities -> entities.filter { it.isResisted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyLimit: StateFlow<Int> = dataStoreManager.dailyLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unlockedAchievements: StateFlow<Set<String>> = dataStoreManager.unlockedAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val customTriggers: StateFlow<List<String>> = dataStoreManager.customTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val disabledDefaultTriggers: StateFlow<Set<String>> = dataStoreManager.disabledDefaultTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val activeTriggers: StateFlow<List<com.smokingtracker.data.TriggerItem>> = kotlinx.coroutines.flow.combine(
        dataStoreManager.customTriggers,
        dataStoreManager.disabledDefaultTriggers
    ) { customList, disabledDefaults ->
        val builtIn = com.smokingtracker.data.TriggerType.allEntries()
            .filter { !disabledDefaults.contains(it.key) }
            .map { com.smokingtracker.data.TriggerItem.fromBuiltIn(it, isEnabled = true) }
        val custom = customList.map { com.smokingtracker.data.TriggerItem.fromCustom(it) }
        builtIn + custom
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.smokingtracker.data.TriggerType.allEntries().map { com.smokingtracker.data.TriggerItem.fromBuiltIn(it) }
    )

    private val _showTaperingCheckIn = MutableStateFlow(false)
    val showTaperingCheckIn: StateFlow<Boolean> = _showTaperingCheckIn.asStateFlow()

    val taperingIntervalDays: StateFlow<Int> = dataStoreManager.taperingIntervalDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    fun addCustomTrigger(name: String, onResult: (String?) -> Unit = {}) {
        viewModelScope.launch {
            val added = dataStoreManager.addCustomTrigger(name)
            onResult(added)
        }
    }

    fun removeCustomTrigger(name: String) {
        viewModelScope.launch {
            dataStoreManager.removeCustomTrigger(name)
        }
    }

    fun toggleDefaultTrigger(key: String, isEnabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.toggleDefaultTrigger(key, isEnabled)
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
                dataStoreManager.setHasCancelledWithin10s(true)
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
            val enabled = dataStoreManager.taperingPlanEnabled.first()
            if (!enabled) return@launch
            val intervalDays = dataStoreManager.taperingIntervalDays.first()
            val lastCheckin = dataStoreManager.lastTaperingCheckinDate.first()
            val now = System.currentTimeMillis()
            val limit = dataStoreManager.dailyLimit.first()
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
            val currentLimit = dataStoreManager.dailyLimit.first()
            val newLimit = (currentLimit - 1).coerceAtLeast(0)
            dataStoreManager.setDailyLimit(newLimit)
            dataStoreManager.updateLastTaperingCheckinDate(System.currentTimeMillis())
            _showTaperingCheckIn.value = false
        }
    }

    fun keepTaperingLimit() {
        viewModelScope.launch {
            dataStoreManager.updateLastTaperingCheckinDate(System.currentTimeMillis())
            _showTaperingCheckIn.value = false
        }
    }

    fun snoozeTaperingCheckIn() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val intervalMs = dataStoreManager.taperingIntervalDays.first() * 24L * 60L * 60L * 1000L
            val snoozeMs = 3L * 24L * 60L * 60L * 1000L
            dataStoreManager.updateLastTaperingCheckinDate(now - intervalMs + snoozeMs)
            _showTaperingCheckIn.value = false
        }
    }
}
