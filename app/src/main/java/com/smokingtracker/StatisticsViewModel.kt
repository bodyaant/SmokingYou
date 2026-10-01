package com.smokingtracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.NotificationPreferences
import com.smokingtracker.data.preferences.ThemePreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.repository.TriggerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val repository: SmokingRepository,
    private val triggerRepository: TriggerRepository,
    private val userPreferences: UserPreferences,
    private val themePreferences: ThemePreferences,
    private val notificationPreferences: NotificationPreferences,
    private val appMetaPreferences: AppMetaPreferences,
    private val achievementsCoordinator: AchievementsCoordinator
) : ViewModel() {

    private val _graphScrollTarget = MutableStateFlow<String?>(null)
    val graphScrollTarget: StateFlow<String?> = _graphScrollTarget

    fun setGraphScrollTarget(target: String?) {
        _graphScrollTarget.value = target
    }

    fun clearGraphScrollTarget() {
        _graphScrollTarget.value = null
    }

    val smokingEntries: StateFlow<List<Long>> = repository.nonResistedTimestamps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val nonResistedEntities: StateFlow<List<SmokingEntryEntity>> = repository.nonResistedEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val resistedEntries: StateFlow<List<SmokingEntryEntity>> = repository.resistedEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dailyLimit: StateFlow<Int> = userPreferences.dailyLimit.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val vibrationEnabled: StateFlow<Boolean> = themePreferences.vibrationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val packPrice: StateFlow<Float> = userPreferences.packPrice.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0f
    )

    val packSize: StateFlow<Int> = userPreferences.packSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 20
    )

    val currency: StateFlow<String> = userPreferences.currency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "USD"
    )

    val savingsGoalTitle: StateFlow<String> = userPreferences.savingsGoalTitle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val savingsGoalAmount: StateFlow<Float> = userPreferences.savingsGoalAmount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    val hasHistoricalBaseline: StateFlow<Boolean> = userPreferences.hasHistoricalBaseline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val historicalStartDate: StateFlow<Long> = userPreferences.historicalStartDate.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val historicalDailyAvg: StateFlow<Int> = userPreferences.historicalDailyAvg.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val historicalPackPrice: StateFlow<Float> = userPreferences.historicalPackPrice.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    val historicalPackSize: StateFlow<Int> = userPreferences.historicalPackSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 20
    )

    val historicalTriggerPriorities: StateFlow<List<String>> = userPreferences.historicalTriggerPriorities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customTriggers: StateFlow<List<String>> = triggerRepository.customTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val disabledDefaultTriggers: StateFlow<Set<String>> = triggerRepository.disabledDefaultTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val showLimitOnGraph: StateFlow<Boolean> = notificationPreferences.showLimitOnGraph.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

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

    fun onAnalyticsTabVisited() {
        viewModelScope.launch {
            appMetaPreferences.recordAnalyticsVisit()
            achievementsCoordinator.checkAndUpdate()
        }
    }

    fun saveBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float = this.packPrice.value,
        packSize: Int = this.packSize.value
    ) {
        viewModelScope.launch {
            userPreferences.saveHistoricalBaseline(
                startDate = startDate,
                dailyAvg = dailyAvg,
                packPrice = packPrice,
                packSize = packSize,
                triggerPriorities = emptyList()
            )
        }
    }

    fun saveHistoricalBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        triggerPriorities: List<String>
    ) {
        viewModelScope.launch {
            userPreferences.saveHistoricalBaseline(
                startDate = startDate,
                dailyAvg = dailyAvg,
                packPrice = packPrice,
                packSize = packSize,
                triggerPriorities = triggerPriorities
            )
        }
    }

    fun clearHistoricalBaseline() {
        viewModelScope.launch {
            userPreferences.clearHistoricalBaseline()
        }
    }

    fun saveSavingsGoal(title: String, amount: Float) {
        viewModelScope.launch {
            userPreferences.saveSavingsGoal(title, amount)
        }
    }
}
