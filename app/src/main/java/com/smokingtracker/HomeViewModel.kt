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

    val entryTriggers: StateFlow<Map<Long, String>> = repository.smokingEntries
        .map { entities ->
            entities.filter { it.trigger != null }.associate { it.timestamp to it.trigger!! }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val dailyLimit: StateFlow<Int> = dataStoreManager.dailyLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unlockedAchievements: StateFlow<Set<String>> = dataStoreManager.unlockedAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _showTaperingCheckIn = MutableStateFlow(false)
    val showTaperingCheckIn: StateFlow<Boolean> = _showTaperingCheckIn.asStateFlow()

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
            val trigger = entryTriggers.value[oldTimestamp]
            repository.removeEntryById(id) 
            repository.addEntry(newTimestamp, trigger)
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
