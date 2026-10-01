package com.smokingtracker

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.FontPreset
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.data.manager.GitHubUpdateManager
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.NotificationPreferences
import com.smokingtracker.data.preferences.ThemePreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.repository.TriggerRepository
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SmokingRepository,
    private val triggerRepository: TriggerRepository,
    private val themePreferences: ThemePreferences,
    private val userPreferences: UserPreferences,
    private val notificationPreferences: NotificationPreferences,
    private val appMetaPreferences: AppMetaPreferences,
    private val updateManager: GitHubUpdateManager,
    private val achievementsCoordinator: AchievementsCoordinator,
    private val backupManager: BackupManager,
    application: Application
) : AndroidViewModel(application) {

    val themePreference: StateFlow<ThemePreference> = themePreferences.appTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreference.SYSTEM
    )

    val fontPreset: StateFlow<FontPreset> = themePreferences.fontPreset.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FontPreset.SYSTEM
    )

    val vibrationEnabled: StateFlow<Boolean> = themePreferences.vibrationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val dailyLimit: StateFlow<Int> = userPreferences.dailyLimit.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
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

    val checkUpdatesOnStart: StateFlow<Boolean> = appMetaPreferences.checkUpdatesOnStart.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val updateCheckState: StateFlow<UpdateCheckState> = updateManager.updateCheckState

    val taperingPlanEnabled: StateFlow<Boolean> = userPreferences.taperingPlanEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val taperingIntervalDays: StateFlow<Int> = userPreferences.taperingIntervalDays.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 7
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

    val customTriggers: StateFlow<List<String>> = triggerRepository.customTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val disabledDefaultTriggers: StateFlow<Set<String>> = triggerRepository.disabledDefaultTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val ongoingNotificationEnabled: StateFlow<Boolean> = notificationPreferences.ongoingNotificationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val notificationLowPriority: StateFlow<Boolean> = notificationPreferences.notificationLowPriority.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notificationShowTimer: StateFlow<Boolean> = notificationPreferences.notificationShowTimer.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notificationShowProgress: StateFlow<Boolean> = notificationPreferences.notificationShowProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notificationShowAddButton: StateFlow<Boolean> = notificationPreferences.notificationShowAddButton.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notificationShowResistButton: StateFlow<Boolean> = notificationPreferences.notificationShowResistButton.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val widgetShowResistButton: StateFlow<Boolean> = notificationPreferences.widgetShowResistButton.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

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

    fun setTaperingPlanSettings(enabled: Boolean, intervalDays: Int) {
        viewModelScope.launch {
            userPreferences.setTaperingPlanEnabled(enabled)
            userPreferences.setTaperingIntervalDays(intervalDays)
            if (enabled && userPreferences.lastTaperingCheckinDate.first() == 0L) {
                userPreferences.updateLastTaperingCheckinDate(System.currentTimeMillis())
            }
        }
    }

    fun updateOngoingNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferences.saveOngoingNotificationEnabled(enabled)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateNotificationLowPriority(lowPriority: Boolean) {
        viewModelScope.launch {
            notificationPreferences.saveNotificationLowPriority(lowPriority)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateNotificationShowTimer(show: Boolean) {
        viewModelScope.launch {
            notificationPreferences.saveNotificationShowTimer(show)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateNotificationShowProgress(show: Boolean) {
        viewModelScope.launch {
            notificationPreferences.saveNotificationShowProgress(show)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateNotificationShowAddButton(show: Boolean) {
        viewModelScope.launch {
            notificationPreferences.saveNotificationShowAddButton(show)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateNotificationShowResistButton(show: Boolean) {
        viewModelScope.launch {
            notificationPreferences.saveNotificationShowResistButton(show)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateWidgetShowResistButton(show: Boolean) {
        viewModelScope.launch {
            notificationPreferences.saveWidgetShowResistButton(show)
            WidgetUpdateManager.updateAll(getApplication())
        }
    }

    fun updateThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            themePreferences.saveThemePreference(theme)
            appMetaPreferences.recordThemeOrLangChange()
            achievementsCoordinator.checkAndUpdate()
        }
    }

    fun setShowLimitOnGraph(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferences.setShowLimitOnGraph(enabled)
        }
    }

    fun setDailyLimit(limit: Int) {
        viewModelScope.launch {
            userPreferences.setDailyLimit(limit)
            WidgetUpdateManager.updateAllAsync(getApplication())
        }
    }

    fun updateFontPreset(preset: FontPreset) {
        viewModelScope.launch {
            themePreferences.saveFontPreset(preset)
        }
    }

    fun updatePackDetails(price: Float, size: Int, curr: String) {
        viewModelScope.launch {
            val oldPrice = userPreferences.packPrice.first()
            userPreferences.savePackDetails(price, size, curr)
            if (oldPrice > 0f && price != oldPrice) {
                appMetaPreferences.setHasChangedPackPrice(true)
            }
            achievementsCoordinator.checkAndUpdate()
        }
    }

    fun updateCheckUpdatesOnStart(enabled: Boolean) {
        viewModelScope.launch {
            appMetaPreferences.saveCheckUpdatesOnStart(enabled)
        }
    }

    fun checkForUpdates(isManual: Boolean) {
        viewModelScope.launch { updateManager.checkForUpdatesWithState(isManual) }
    }

    fun resetUpdateCheckState() {
        updateManager.resetUpdateCheckState()
    }

    fun backupData(uri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                backupManager.backup(uri)
                onSuccess()
            } catch (e: Exception) {
                onError()
            }
        }
    }

    fun restoreData(uri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                backupManager.restore(uri)
                onSuccess()
            } catch (e: Exception) {
                onError()
            }
        }
    }

    fun recordLanguageChange(languageTag: String) {
        viewModelScope.launch {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                themePreferences.saveAppLanguageTag(languageTag)
            }
            appMetaPreferences.recordThemeOrLangChange()
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

    fun clearHistoricalBaseline() {
        viewModelScope.launch {
            userPreferences.clearHistoricalBaseline()
        }
    }

    fun resetAllSmokingData() {
        viewModelScope.launch {
            repository.clearAllEntries()
            userPreferences.resetUserData()
            appMetaPreferences.resetAppMeta()
            notificationPreferences.resetNotificationPreferences()
            triggerRepository.clearTriggers()
            com.smokingtracker.notification.OngoingNotificationManager.updateSuspend(getApplication())
            WidgetUpdateManager.updateAll(getApplication())
        }
    }
}
