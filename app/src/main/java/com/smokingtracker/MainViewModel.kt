package com.smokingtracker

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.AppIconPreset
import com.smokingtracker.data.ColorPreset
import com.smokingtracker.data.ContainerStyle
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.FontPreset
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.data.repository.SmokingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.smokingtracker.data.manager.GitHubUpdateManager
import com.smokingtracker.UpdateCheckState
import com.smokingtracker.data.local.SmokingEntryEntity
import java.util.Calendar
import com.smokingtracker.widget.WidgetUpdateManager

class MainViewModel(
    private val repository: SmokingRepository,
    private val dataStoreManager: DataStoreManager,
    private val updateManager: GitHubUpdateManager,
    private val achievementsCoordinator: AchievementsCoordinator,
    private val appIconManager: AppIconManager,
    private val backupManager: BackupManager,
    application: Application
) : AndroidViewModel(application) {


    val isRegistered: StateFlow<Boolean?> = dataStoreManager.isRegistered.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _graphScrollTarget = MutableStateFlow<String?>(null)
    val graphScrollTarget: StateFlow<String?> = _graphScrollTarget

    fun setGraphScrollTarget(target: String?) {
        _graphScrollTarget.value = target
    }

    fun clearGraphScrollTarget() {
        _graphScrollTarget.value = null
    }

    val smokingEntries: StateFlow<List<Long>> = repository.smokingEntries
        .map { entities -> entities.filter { !it.isResisted }.map { it.timestamp } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val nonResistedEntities: StateFlow<List<SmokingEntryEntity>> = repository.smokingEntries
        .map { entities -> entities.filter { !it.isResisted } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val resistedEntries: StateFlow<List<SmokingEntryEntity>> = repository.smokingEntries
        .map { entities -> entities.filter { it.isResisted } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val themePreference: StateFlow<ThemePreference> = dataStoreManager.appTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreference.SYSTEM
    )

    val unlockedAchievements: StateFlow<Set<String>> = dataStoreManager.unlockedAchievements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )
    
    val dailyLimit: StateFlow<Int> = dataStoreManager.dailyLimit.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val fontPreset: StateFlow<FontPreset> = dataStoreManager.fontPreset.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FontPreset.ZENITH
    )

    val amoledTheme: StateFlow<Boolean> = dataStoreManager.amoledTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val vibrationEnabled: StateFlow<Boolean> = dataStoreManager.vibrationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val appLaunchDates: StateFlow<List<Long>> = dataStoreManager.appLaunchDates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val packPrice: StateFlow<Float> = dataStoreManager.packPrice.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0f
    )

    val packSize: StateFlow<Int> = dataStoreManager.packSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 20
    )

    val currency: StateFlow<String> = dataStoreManager.currency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "USD"
    )

    val colorPreset: StateFlow<ColorPreset> = dataStoreManager.colorPreset.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ColorPreset.SYSTEM
    )

    val checkUpdatesOnStart: StateFlow<Boolean> = dataStoreManager.checkUpdatesOnStart.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val appIcon: StateFlow<AppIconPreset> = dataStoreManager.appIcon.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppIconPreset.DEFAULT
    )

    val containerBorderEnabled: StateFlow<Boolean> = dataStoreManager.containerBorderEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val containerStyle: StateFlow<ContainerStyle> = dataStoreManager.containerStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ContainerStyle.EXPRESSIVE
    )

    val useCustomVariableFont: StateFlow<Boolean> = dataStoreManager.useCustomVariableFont.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val customFontWeight: StateFlow<Int> = dataStoreManager.customFontWeight.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 500
    )

    val customFontWidth: StateFlow<Float> = dataStoreManager.customFontWidth.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 100f
    )

    val customFontRoundness: StateFlow<Float> = dataStoreManager.customFontRoundness.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    val hasHistoricalBaseline: StateFlow<Boolean> = dataStoreManager.hasHistoricalBaseline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val historicalStartDate: StateFlow<Long> = dataStoreManager.historicalStartDate.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val historicalDailyAvg: StateFlow<Int> = dataStoreManager.historicalDailyAvg.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val historicalPackPrice: StateFlow<Float> = dataStoreManager.historicalPackPrice.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    val historicalPackSize: StateFlow<Int> = dataStoreManager.historicalPackSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 20
    )

    val historicalTriggerPriorities: StateFlow<List<String>> = dataStoreManager.historicalTriggerPriorities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customTriggers: StateFlow<List<String>> = dataStoreManager.customTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val disabledDefaultTriggers: StateFlow<Set<String>> = dataStoreManager.disabledDefaultTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val ongoingNotificationEnabled: StateFlow<Boolean> = dataStoreManager.ongoingNotificationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val notificationLowPriority: StateFlow<Boolean> = dataStoreManager.notificationLowPriority.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notificationShowTimer: StateFlow<Boolean> = dataStoreManager.notificationShowTimer.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notificationShowProgress: StateFlow<Boolean> = dataStoreManager.notificationShowProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notificationShowAddButton: StateFlow<Boolean> = dataStoreManager.notificationShowAddButton.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notificationShowResistButton: StateFlow<Boolean> = dataStoreManager.notificationShowResistButton.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

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

    val updateCheckState: StateFlow<UpdateCheckState> = updateManager.updateCheckState

    val taperingPlanEnabled: StateFlow<Boolean> = dataStoreManager.taperingPlanEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val taperingIntervalDays: StateFlow<Int> = dataStoreManager.taperingIntervalDays.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 7
    )


    private val _achievementPopupQueue = MutableStateFlow<List<com.smokingtracker.Achievement>>(emptyList())
    val pendingAchievementPopup: StateFlow<com.smokingtracker.Achievement?> = _achievementPopupQueue
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
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val launches = dataStoreManager.appLaunchDates.first()
            val cal = Calendar.getInstance().apply { timeInMillis = now }
            val todayYear = cal.get(Calendar.YEAR)
            val todayDay = cal.get(Calendar.DAY_OF_YEAR)
            
            val checkCal = Calendar.getInstance()
            val alreadyLoggedToday = launches.any {
                checkCal.timeInMillis = it
                checkCal.get(Calendar.YEAR) == todayYear && checkCal.get(Calendar.DAY_OF_YEAR) == todayDay
            }

            if (!alreadyLoggedToday) {
                dataStoreManager.recordAppLaunch(now)
            }
            val isReg = dataStoreManager.isRegistered.first()
            if (isReg == true) {
                achievementsCoordinator.checkAndUpdate()
            }
        }
    }


    fun registerUser() {
        viewModelScope.launch {
            dataStoreManager.saveUserProfile()
            achievementsCoordinator.checkAndUpdate()
        }
    }

    fun updateCheckUpdatesOnStart(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveCheckUpdatesOnStart(enabled)
        }
    }

    fun updateContainerBorderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveContainerBorderEnabled(enabled)
        }
    }

    fun updateContainerStyle(style: ContainerStyle) {
        viewModelScope.launch {
            dataStoreManager.saveContainerStyle(style)
        }
    }

    fun checkForUpdates(isManual: Boolean) {
        viewModelScope.launch { updateManager.checkForUpdatesWithState(isManual) }
    }

    fun resetUpdateCheckState() {
        updateManager.resetUpdateCheckState()
    }


    fun setTaperingPlanSettings(enabled: Boolean, intervalDays: Int) {
        viewModelScope.launch {
            dataStoreManager.setTaperingPlanEnabled(enabled)
            dataStoreManager.setTaperingIntervalDays(intervalDays)
            if (enabled && dataStoreManager.lastTaperingCheckinDate.first() == 0L) {
                dataStoreManager.updateLastTaperingCheckinDate(System.currentTimeMillis())
            }
        }
    }


    fun updatePackDetails(price: Float, size: Int, curr: String) {
        viewModelScope.launch {
            val oldPrice = dataStoreManager.packPrice.first()
            dataStoreManager.savePackDetails(price, size, curr)
            if (oldPrice > 0f && price != oldPrice) {
                dataStoreManager.setHasChangedPackPrice(true)
            }
            achievementsCoordinator.checkAndUpdate()
        }
    }

    fun updateColorPreset(preset: ColorPreset) {
        viewModelScope.launch {
            dataStoreManager.saveColorPreset(preset)
        }
    }

    fun updateThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            dataStoreManager.saveThemePreference(theme)
            dataStoreManager.recordThemeOrLangChange()
            achievementsCoordinator.checkAndUpdate()
        }
    }

    fun recordLanguageChange(languageTag: String) {
        viewModelScope.launch {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                dataStoreManager.saveAppLanguageTag(languageTag)
            }
            dataStoreManager.recordThemeOrLangChange()
            achievementsCoordinator.checkAndUpdate()
        }
    }

    fun onAnalyticsTabVisited() {
        viewModelScope.launch {
            dataStoreManager.recordAnalyticsVisit()
            achievementsCoordinator.checkAndUpdate()
        }
    }

    fun updateFontPreset(preset: FontPreset) {
        viewModelScope.launch {
            dataStoreManager.saveFontPreset(preset)
        }
    }

    fun updateUseCustomVariableFont(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveUseCustomVariableFont(enabled)
        }
    }

    fun updateCustomFontWeight(weight: Int) {
        viewModelScope.launch {
            dataStoreManager.saveCustomFontWeight(weight)
        }
    }

    fun updateCustomFontWidth(width: Float) {
        viewModelScope.launch {
            dataStoreManager.saveCustomFontWidth(width)
        }
    }

    fun updateCustomFontRoundness(roundness: Float) {
        viewModelScope.launch {
            dataStoreManager.saveCustomFontRoundness(roundness)
        }
    }

    fun updateAmoledTheme(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveAmoledTheme(enabled)
        }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveVibrationEnabled(enabled)
        }
    }

    fun updateAppIcon(preset: AppIconPreset) {
        viewModelScope.launch {
            dataStoreManager.saveAppIcon(preset)
            appIconManager.applyIcon(preset)
        }
    }
    
    fun setDailyLimit(limit: Int) {
        viewModelScope.launch {
            dataStoreManager.setDailyLimit(limit)
            WidgetUpdateManager.updateAllAsync(getApplication())
        }
    }

    fun updateOngoingNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveOngoingNotificationEnabled(enabled)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateNotificationLowPriority(lowPriority: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveNotificationLowPriority(lowPriority)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateNotificationShowTimer(show: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveNotificationShowTimer(show)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateNotificationShowProgress(show: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveNotificationShowProgress(show)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateNotificationShowAddButton(show: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveNotificationShowAddButton(show)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }

    fun updateNotificationShowResistButton(show: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveNotificationShowResistButton(show)
            com.smokingtracker.notification.OngoingNotificationManager.update(getApplication())
        }
    }
    
    fun backupData(uri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try { backupManager.backup(uri); onSuccess() } catch (e: Exception) { onError() }
        }
    }

    fun restoreData(uri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try { backupManager.restore(uri); onSuccess() } catch (e: Exception) { onError() }
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
            dataStoreManager.saveHistoricalBaseline(
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
            dataStoreManager.clearHistoricalBaseline()
        }
    }
}
