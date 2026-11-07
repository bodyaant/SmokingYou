package com.smokingtracker

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
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
import com.smokingtracker.notification.OngoingNotificationManager
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface BackupRestoreUiState {
    data object Idle : BackupRestoreUiState
    data object Loading : BackupRestoreUiState
    data object Done : BackupRestoreUiState
    data class Error(val message: String? = null) : BackupRestoreUiState
}

sealed interface BackupRestoreEvent {
    data object BackupSuccess : BackupRestoreEvent
    data class BackupError(val message: String? = null) : BackupRestoreEvent
    data object RestoreSuccess : BackupRestoreEvent
    data class RestoreError(val message: String? = null) : BackupRestoreEvent
}

@Immutable
data class SettingsUiState(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val fontPreset: FontPreset = FontPreset.SYSTEM,
    val vibrationEnabled: Boolean = true,
    val dailyLimit: Int = 0,
    val packPrice: Float = 0.0f,
    val packSize: Int = UserPreferences.DEFAULT_PACK_SIZE,
    val currency: String = UserPreferences.DEFAULT_CURRENCY,
    val checkUpdatesOnStart: Boolean = false,
    val updateCheckState: UpdateCheckState = UpdateCheckState.Idle,
    val taperingPlanEnabled: Boolean = false,
    val taperingIntervalDays: Int = UserPreferences.DEFAULT_TAPERING_INTERVAL_DAYS,
    val hasHistoricalBaseline: Boolean = false,
    val historicalStartDate: Long = 0L,
    val historicalDailyAvg: Int = 0,
    val customTriggers: List<String> = emptyList(),
    val disabledDefaultTriggers: Set<String> = emptySet(),
    val ongoingNotificationEnabled: Boolean = false,
    val notificationLowPriority: Boolean = true,
    val notificationShowTimer: Boolean = true,
    val notificationShowProgress: Boolean = true,
    val notificationShowAddButton: Boolean = true,
    val notificationShowResistButton: Boolean = false,
    val widgetShowResistButton: Boolean = true,
    val showLimitOnGraph: Boolean = true,
    val backupOperationState: BackupRestoreUiState = BackupRestoreUiState.Idle,
    val restoreOperationState: BackupRestoreUiState = BackupRestoreUiState.Idle
)

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
    private val ongoingNotificationManager: OngoingNotificationManager,
    private val widgetUpdateManager: WidgetUpdateManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val backupRestoreMutex = Mutex()
    private val resetMutex = Mutex()
    private val languageChangeMutex = Mutex()

    @Volatile
    private var lastRecordedLanguageTag: String? = null

    private val _backupOperationState = MutableStateFlow<BackupRestoreUiState>(BackupRestoreUiState.Idle)
    val backupOperationState: StateFlow<BackupRestoreUiState> = _backupOperationState.asStateFlow()

    private val _restoreOperationState = MutableStateFlow<BackupRestoreUiState>(BackupRestoreUiState.Idle)
    val restoreOperationState: StateFlow<BackupRestoreUiState> = _restoreOperationState.asStateFlow()

    private val _backupRestoreEvents = Channel<BackupRestoreEvent>(Channel.BUFFERED)
    val backupRestoreEvents: Flow<BackupRestoreEvent> = _backupRestoreEvents.receiveAsFlow()

    private val _customTriggerAddedChannel = Channel<String?>(Channel.BUFFERED)
    val customTriggerAddedEvents: Flow<String?> = _customTriggerAddedChannel.receiveAsFlow()

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
        initialValue = UserPreferences.DEFAULT_PACK_SIZE
    )

    val currency: StateFlow<String> = userPreferences.currency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences.DEFAULT_CURRENCY
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
        initialValue = UserPreferences.DEFAULT_TAPERING_INTERVAL_DAYS
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

    val widgetShowResistButton: StateFlow<Boolean> = userPreferences.widgetShowResistButton.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val showLimitOnGraph: StateFlow<Boolean> = userPreferences.showLimitOnGraph.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val uiState: StateFlow<SettingsUiState> = combine<Any?, SettingsUiState>(
        listOf(
            themePreference,
            fontPreset,
            vibrationEnabled,
            dailyLimit,
            packPrice,
            packSize,
            currency,
            checkUpdatesOnStart,
            updateCheckState,
            taperingPlanEnabled,
            taperingIntervalDays,
            hasHistoricalBaseline,
            historicalStartDate,
            historicalDailyAvg,
            customTriggers,
            disabledDefaultTriggers,
            ongoingNotificationEnabled,
            notificationLowPriority,
            notificationShowTimer,
            notificationShowProgress,
            notificationShowAddButton,
            notificationShowResistButton,
            widgetShowResistButton,
            showLimitOnGraph,
            _backupOperationState,
            _restoreOperationState
        )
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        SettingsUiState(
            themePreference = values[0] as ThemePreference,
            fontPreset = values[1] as FontPreset,
            vibrationEnabled = values[2] as Boolean,
            dailyLimit = values[3] as Int,
            packPrice = values[4] as Float,
            packSize = values[5] as Int,
            currency = values[6] as String,
            checkUpdatesOnStart = values[7] as Boolean,
            updateCheckState = values[8] as UpdateCheckState,
            taperingPlanEnabled = values[9] as Boolean,
            taperingIntervalDays = values[10] as Int,
            hasHistoricalBaseline = values[11] as Boolean,
            historicalStartDate = values[12] as Long,
            historicalDailyAvg = values[13] as Int,
            customTriggers = values[14] as List<String>,
            disabledDefaultTriggers = values[15] as Set<String>,
            ongoingNotificationEnabled = values[16] as Boolean,
            notificationLowPriority = values[17] as Boolean,
            notificationShowTimer = values[18] as Boolean,
            notificationShowProgress = values[19] as Boolean,
            notificationShowAddButton = values[20] as Boolean,
            notificationShowResistButton = values[21] as Boolean,
            widgetShowResistButton = values[22] as Boolean,
            showLimitOnGraph = values[23] as Boolean,
            backupOperationState = values[24] as BackupRestoreUiState,
            restoreOperationState = values[25] as BackupRestoreUiState
        )
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    private fun safeLaunch(
        dispatcher: CoroutineDispatcher = ioDispatcher,
        block: suspend CoroutineScope.() -> Unit
    ): Job = viewModelScope.launch(dispatcher) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in SettingsViewModel coroutine", e)
        }
    }

    suspend fun addCustomTriggerSuspend(name: String): String? = withContext(ioDispatcher) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return@withContext null
        val added = triggerRepository.addCustomTrigger(cleanName)
        _customTriggerAddedChannel.trySend(added)
        added
    }

    fun addCustomTrigger(name: String, onResult: (String?) -> Unit = {}) {
        safeLaunch(ioDispatcher) {
            val cleanName = name.trim()
            if (cleanName.isEmpty()) {
                _customTriggerAddedChannel.trySend(null)
                withContext(mainDispatcher) {
                    onResult(null)
                }
                return@safeLaunch
            }
            val added = triggerRepository.addCustomTrigger(cleanName)
            _customTriggerAddedChannel.trySend(added)
            withContext(mainDispatcher) {
                onResult(added)
            }
        }
    }

    fun removeCustomTrigger(name: String) {
        safeLaunch(ioDispatcher) {
            triggerRepository.removeCustomTrigger(name)
        }
    }

    fun toggleDefaultTrigger(key: String, isEnabled: Boolean) {
        safeLaunch(ioDispatcher) {
            triggerRepository.toggleDefaultTrigger(key, isEnabled)
        }
    }

    fun setTaperingPlanSettings(enabled: Boolean, intervalDays: Int) {
        val safeIntervalDays = intervalDays.coerceIn(1, 365)
        safeLaunch(ioDispatcher) {
            userPreferences.setTaperingPlanEnabled(enabled)
            userPreferences.setTaperingIntervalDays(safeIntervalDays)
            val now = System.currentTimeMillis()
            val lastCheckin = userPreferences.lastTaperingCheckinDate.first()
            if (enabled && (lastCheckin <= 0L || lastCheckin > now)) {
                userPreferences.updateLastTaperingCheckinDate(now)
            }
        }
    }

    fun updateOngoingNotificationEnabled(enabled: Boolean) {
        safeLaunch(ioDispatcher) {
            notificationPreferences.saveOngoingNotificationEnabled(enabled)
            ongoingNotificationManager.update()
        }
    }

    fun updateNotificationLowPriority(lowPriority: Boolean) {
        safeLaunch(ioDispatcher) {
            notificationPreferences.saveNotificationLowPriority(lowPriority)
            ongoingNotificationManager.update()
        }
    }

    fun updateNotificationShowTimer(show: Boolean) {
        safeLaunch(ioDispatcher) {
            notificationPreferences.saveNotificationShowTimer(show)
            ongoingNotificationManager.update()
        }
    }

    fun updateNotificationShowProgress(show: Boolean) {
        safeLaunch(ioDispatcher) {
            notificationPreferences.saveNotificationShowProgress(show)
            ongoingNotificationManager.update()
        }
    }

    fun updateNotificationShowAddButton(show: Boolean) {
        safeLaunch(ioDispatcher) {
            notificationPreferences.saveNotificationShowAddButton(show)
            ongoingNotificationManager.update()
        }
    }

    fun updateNotificationShowResistButton(show: Boolean) {
        safeLaunch(ioDispatcher) {
            notificationPreferences.saveNotificationShowResistButton(show)
            ongoingNotificationManager.update()
        }
    }

    fun updateWidgetShowResistButton(show: Boolean) {
        safeLaunch(ioDispatcher) {
            userPreferences.saveWidgetShowResistButton(show)
            widgetUpdateManager.updateAll()
        }
    }

    fun updateThemePreference(theme: ThemePreference) {
        safeLaunch(ioDispatcher) {
            val currentTheme = themePreferences.appTheme.first()
            if (currentTheme == theme) return@safeLaunch
            themePreferences.saveThemePreference(theme)
            appMetaPreferences.recordThemeOrLangChange()
            achievementsCoordinator.checkAndUpdateSuspend()
        }
    }

    fun setShowLimitOnGraph(enabled: Boolean) {
        safeLaunch(ioDispatcher) {
            userPreferences.setShowLimitOnGraph(enabled)
        }
    }

    fun setDailyLimit(limit: Int) {
        if (limit < 0) {
            Log.w(TAG, "Ignoring negative daily limit: $limit")
            return
        }
        safeLaunch(ioDispatcher) {
            userPreferences.setDailyLimit(limit)
            ongoingNotificationManager.update()
            widgetUpdateManager.updateAllAsync()
            achievementsCoordinator.checkAndUpdateSuspend()
        }
    }

    fun updateFontPreset(preset: FontPreset) {
        safeLaunch(ioDispatcher) {
            themePreferences.saveFontPreset(preset)
        }
    }

    fun updatePackDetails(price: Float, size: Int, curr: String) {
        if (!price.isFinite() || price < 0f || size <= 0) {
            Log.w(TAG, "Ignoring invalid pack details: price=$price, size=$size")
            return
        }
        val safeCurrency = curr.trim().ifEmpty { UserPreferences.DEFAULT_CURRENCY }
        safeLaunch(ioDispatcher) {
            val oldPrice = userPreferences.packPrice.first()
            userPreferences.savePackDetails(price, size, safeCurrency)
            if (oldPrice > 0f && price != oldPrice) {
                appMetaPreferences.setHasChangedPackPrice(true)
            }
            achievementsCoordinator.checkAndUpdateSuspend()
        }
    }

    fun updateCheckUpdatesOnStart(enabled: Boolean) {
        safeLaunch(ioDispatcher) {
            appMetaPreferences.saveCheckUpdatesOnStart(enabled)
        }
    }

    fun checkForUpdates(isManual: Boolean) {
        safeLaunch(ioDispatcher) {
            updateManager.checkForUpdatesWithState(isManual)
        }
    }

    fun resetUpdateCheckState() {
        updateManager.resetUpdateCheckState()
    }

    fun resetBackupOperationState() {
        _backupOperationState.value = BackupRestoreUiState.Idle
    }

    fun resetRestoreOperationState() {
        _restoreOperationState.value = BackupRestoreUiState.Idle
    }

    fun backupData(
        uri: Uri,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        if (!backupRestoreMutex.tryLock()) {
            Log.w(TAG, "Backup or restore operation already in progress")
            return
        }
        _backupOperationState.value = BackupRestoreUiState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                backupManager.backup(uri)
                _backupOperationState.value = BackupRestoreUiState.Done
                _backupRestoreEvents.trySend(BackupRestoreEvent.BackupSuccess)
                withContext(mainDispatcher) {
                    onSuccess()
                }
            } catch (e: CancellationException) {
                _backupOperationState.value = BackupRestoreUiState.Idle
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Backup failed for uri=$uri", e)
                val errorMessage = e.localizedMessage ?: e.message
                _backupOperationState.value = BackupRestoreUiState.Error(errorMessage)
                _backupRestoreEvents.trySend(BackupRestoreEvent.BackupError(errorMessage))
                withContext(mainDispatcher) {
                    onError()
                }
            } finally {
                backupRestoreMutex.unlock()
            }
        }
    }

    fun restoreData(
        uri: Uri,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        if (!backupRestoreMutex.tryLock()) {
            Log.w(TAG, "Backup or restore operation already in progress")
            return
        }
        _restoreOperationState.value = BackupRestoreUiState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                backupManager.restore(uri)
                _restoreOperationState.value = BackupRestoreUiState.Done
                _backupRestoreEvents.trySend(BackupRestoreEvent.RestoreSuccess)
                withContext(mainDispatcher) {
                    onSuccess()
                }
            } catch (e: CancellationException) {
                _restoreOperationState.value = BackupRestoreUiState.Idle
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed for uri=$uri", e)
                val errorMessage = e.localizedMessage ?: e.message
                _restoreOperationState.value = BackupRestoreUiState.Error(errorMessage)
                _backupRestoreEvents.trySend(BackupRestoreEvent.RestoreError(errorMessage))
                withContext(mainDispatcher) {
                    onError()
                }
            } finally {
                backupRestoreMutex.unlock()
            }
        }
    }

    fun recordLanguageChange(languageTag: String) {
        val cleanTag = languageTag.trim()
        if (cleanTag.isEmpty()) return
        safeLaunch(ioDispatcher) {
            languageChangeMutex.withLock {
                val previousStoredTag = themePreferences.appLanguageTag.first()?.trim().orEmpty()
                val previousInMemoryTag = lastRecordedLanguageTag
                if (cleanTag.equals(previousStoredTag, ignoreCase = true) ||
                    (previousInMemoryTag != null && cleanTag.equals(previousInMemoryTag, ignoreCase = true))
                ) {
                    return@withLock
                }
                lastRecordedLanguageTag = cleanTag
                themePreferences.saveAppLanguageTag(cleanTag)
                appMetaPreferences.recordThemeOrLangChange()
                achievementsCoordinator.checkAndUpdateSuspend()
            }
        }
    }

    fun saveBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float = this.packPrice.value,
        packSize: Int = this.packSize.value
    ) {
        val now = System.currentTimeMillis()
        if (startDate <= 0L || startDate > now || dailyAvg <= 0) {
            Log.w(TAG, "Ignoring invalid baseline: startDate=$startDate, dailyAvg=$dailyAvg")
            return
        }
        val safePrice = if (packPrice.isFinite() && packPrice >= 0f) packPrice else 0f
        val safeSize = if (packSize > 0) packSize else UserPreferences.DEFAULT_PACK_SIZE
        safeLaunch(ioDispatcher) {
            val existingPriorities = userPreferences.historicalTriggerPriorities.first()
            userPreferences.saveHistoricalBaseline(
                startDate = startDate,
                dailyAvg = dailyAvg,
                packPrice = safePrice,
                packSize = safeSize,
                triggerPriorities = existingPriorities
            )
            achievementsCoordinator.checkAndUpdateSuspend()
        }
    }

    fun clearHistoricalBaseline() {
        safeLaunch(ioDispatcher) {
            userPreferences.clearHistoricalBaseline()
            achievementsCoordinator.checkAndUpdateSuspend()
        }
    }

    fun resetAllSmokingData() {
        safeLaunch(ioDispatcher) {
            resetMutex.withLock {
                val cleared = repository.clearAllEntries()
                if (!cleared) {
                    Log.e(TAG, "Aborting resetAllSmokingData because repository.clearAllEntries() failed")
                    return@withLock
                }
                userPreferences.resetUserData()
                appMetaPreferences.resetAppMeta()
                notificationPreferences.resetNotificationPreferences()
                triggerRepository.clearTriggers()
                ongoingNotificationManager.update()
                widgetUpdateManager.updateAllAsync()
            }
        }
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
