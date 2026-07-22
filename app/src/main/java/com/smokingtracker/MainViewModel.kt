package com.smokingtracker

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.data.repository.SmokingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.smokingtracker.data.manager.GitHubUpdateManager
import com.smokingtracker.data.manager.GitHubRelease
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Calendar
import com.smokingtracker.widget.WidgetUpdateManager

class MainViewModel(
    private val repository: SmokingRepository,
    private val dataStoreManager: DataStoreManager,
    private val updateManager: GitHubUpdateManager,
    private val context: Context
) : ViewModel() {

    private val gson = Gson()

    val isRegistered: StateFlow<Boolean?> = dataStoreManager.isRegistered.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val smokingEntries: StateFlow<List<Long>> = repository.smokingEntries
        .map { entities -> entities.filter { !it.isResisted }.map { it.timestamp } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSmokingEntities: StateFlow<List<SmokingEntryEntity>> = repository.smokingEntries
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

    val fontPreset: StateFlow<String> = dataStoreManager.fontPreset.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "WIDE"
    )

    val amoledTheme: StateFlow<Boolean> = dataStoreManager.amoledTheme.stateIn(
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

    val colorPreset: StateFlow<String> = dataStoreManager.colorPreset.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "SYSTEM"
    )

    val checkUpdatesOnStart: StateFlow<Boolean> = dataStoreManager.checkUpdatesOnStart.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val appIcon: StateFlow<String> = dataStoreManager.appIcon.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "DEFAULT"
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

    private val _updateCheckState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    val updateCheckState: StateFlow<UpdateCheckState> = _updateCheckState.asStateFlow()

    val entryTriggers: StateFlow<Map<Long, String>> = repository.smokingEntries
        .map { entities ->
            entities.filter { it.trigger != null }.associate { it.timestamp to it.trigger!! }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

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

    private val _showTaperingCheckIn = MutableStateFlow(false)
    val showTaperingCheckIn: StateFlow<Boolean> = _showTaperingCheckIn.asStateFlow()

    init {
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
            checkAchievements()
            checkTaperingPlanEligibility()
        }
    }

    fun checkAchievements(updatedEntries: List<Long>? = null, wasEntryRemoved: Boolean = false) = viewModelScope.launch(Dispatchers.Default) {
        val entries = updatedEntries ?: repository.smokingEntries.first().map { it.timestamp }
        val launches = dataStoreManager.appLaunchDates.first()
        val dailyLimit = dataStoreManager.dailyLimit.first()
        val hasBackup = dataStoreManager.hasMadeBackup.first()
        val hasPriceChanged = dataStoreManager.hasChangedPackPrice.first()
        val hasCancelled10s = dataStoreManager.hasCancelledWithin10s.first()
        val themeLangCount = dataStoreManager.themeLangChangeCount.first()
        val analyticsCount = dataStoreManager.analyticsVisitCount.first()

        val lastEntry = entries.maxOrNull()
        val now = System.currentTimeMillis()
        val timeWithoutSmoking = if (lastEntry != null) (now - lastEntry).coerceAtLeast(0L) else 0L

        val ctx = AchievementContext(
            timeWithoutSmoking = timeWithoutSmoking,
            entries = entries,
            launches = launches,
            dailyLimit = dailyLimit,
            hasMadeBackup = hasBackup,
            hasChangedPackPrice = hasPriceChanged,
            hasCancelledWithin10s = hasCancelled10s,
            themeLangChangesToday = themeLangCount,
            analyticsVisitsToday = analyticsCount
        )

        val previouslyUnlocked = dataStoreManager.unlockedAchievements.first()
        val newUnlockedSet = AchievementsManager.calculateUnlockedAchievements(ctx)

        val effectiveUnlockedSet = if (wasEntryRemoved) {
            val noSmokeIds = AchievementsManager.achievementsList
                .filter { it.category == AchievementCategory.NO_SMOKE }
                .map { it.id }.toSet()
            val preservedNoSmoke = previouslyUnlocked.intersect(noSmokeIds)
            newUnlockedSet + preservedNoSmoke
        } else {
            newUnlockedSet
        }

        val newlyUnlocked = effectiveUnlockedSet - previouslyUnlocked
        newlyUnlocked.forEach { achievementId ->
            AchievementsManager.sendNotificationForAchievement(context, achievementId)
        }

        dataStoreManager.setUnlockedAchievements(effectiveUnlockedSet)
    }

    fun registerUser() {
        viewModelScope.launch {
            dataStoreManager.saveUserProfile()
        }
    }

    fun updateCheckUpdatesOnStart(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveCheckUpdatesOnStart(enabled)
        }
    }

    fun checkForUpdates(isManual: Boolean) {
        viewModelScope.launch {
            if (isManual) {
                _updateCheckState.value = UpdateCheckState.Checking
            }
            when (val result = updateManager.checkForUpdates()) {
                is GitHubUpdateManager.UpdateResult.NewUpdate -> {
                    _updateCheckState.value = UpdateCheckState.NewUpdate(result.release)
                }
                is GitHubUpdateManager.UpdateResult.NoUpdate -> {
                    if (isManual) {
                        _updateCheckState.value = UpdateCheckState.NoUpdate
                    } else {
                        _updateCheckState.value = UpdateCheckState.Idle
                    }
                }
                is GitHubUpdateManager.UpdateResult.Error -> {
                    if (isManual) {
                        _updateCheckState.value = UpdateCheckState.Error(result.message)
                    } else {
                        _updateCheckState.value = UpdateCheckState.Idle
                    }
                }
            }
        }
    }

    fun resetUpdateCheckState() {
        _updateCheckState.value = UpdateCheckState.Idle
    }

    sealed class UpdateCheckState {
        object Idle : UpdateCheckState()
        object Checking : UpdateCheckState()
        data class NewUpdate(val release: GitHubRelease) : UpdateCheckState()
        object NoUpdate : UpdateCheckState()
        data class Error(val message: String) : UpdateCheckState()
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
            checkAchievements(updated)
            WidgetUpdateManager.updateAllAsync(context)
        }
    }

    fun addResistedEntry(trigger: String?, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.addResistedEntry(timestamp, trigger)
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

    fun setTaperingPlanSettings(enabled: Boolean, intervalDays: Int) {
        viewModelScope.launch {
            dataStoreManager.setTaperingPlanEnabled(enabled)
            dataStoreManager.setTaperingIntervalDays(intervalDays)
            if (enabled && dataStoreManager.lastTaperingCheckinDate.first() == 0L) {
                dataStoreManager.updateLastTaperingCheckinDate(System.currentTimeMillis())
            }
        }
    }

    fun removeSmokingEntry(timestamp: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (now - timestamp <= 10_000L) {
                dataStoreManager.setHasCancelledWithin10s(true)
            }
            repository.removeEntry(timestamp)
            val updated = smokingEntries.value.toMutableList().apply {
                remove(timestamp)
            }
            checkAchievements(updated, wasEntryRemoved = true)
            WidgetUpdateManager.updateAllAsync(context)
        }
    }

    fun editSmokingEntry(oldTimestamp: Long, newTimestamp: Long) {
        viewModelScope.launch {
            val trigger = entryTriggers.value[oldTimestamp]
            repository.removeEntry(oldTimestamp)
            repository.addEntry(newTimestamp, trigger)
            val updated = smokingEntries.value.toMutableList().apply {
                remove(oldTimestamp)
                add(newTimestamp)
                sort()
            }
            checkAchievements(updated)
            WidgetUpdateManager.updateAllAsync(context)
        }
    }

    fun updateSmokingEntryTrigger(timestamp: Long, trigger: String?) {
        viewModelScope.launch {
            repository.updateEntryTrigger(timestamp, trigger)
        }
    }

    fun updatePackDetails(price: Float, size: Int, curr: String) {
        viewModelScope.launch {
            val oldPrice = dataStoreManager.packPrice.first()
            dataStoreManager.savePackDetails(price, size, curr)
            if (oldPrice > 0f && price != oldPrice) {
                dataStoreManager.setHasChangedPackPrice(true)
            }
            checkAchievements()
        }
    }

    fun updateColorPreset(preset: String) {
        viewModelScope.launch {
            dataStoreManager.saveColorPreset(preset)
        }
    }

    fun updateThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            dataStoreManager.saveThemePreference(theme)
            dataStoreManager.recordThemeOrLangChange()
            checkAchievements()
        }
    }

    fun recordLanguageChange() {
        viewModelScope.launch {
            dataStoreManager.recordThemeOrLangChange()
            checkAchievements()
        }
    }

    fun onAnalyticsTabVisited() {
        viewModelScope.launch {
            dataStoreManager.recordAnalyticsVisit()
            checkAchievements()
        }
    }

    fun updateFontPreset(preset: String) {
        viewModelScope.launch {
            dataStoreManager.saveFontPreset(preset)
        }
    }

    fun updateAmoledTheme(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveAmoledTheme(enabled)
        }
    }

    fun updateAppIcon(iconKey: String) {
        viewModelScope.launch {
            dataStoreManager.saveAppIcon(iconKey)
            val pm = context.packageManager
            val packageName = context.packageName
            val targetAlias = when (iconKey) {
                "DEFAULT" -> "$packageName.MainActivityDefault"
                "DARK" -> "$packageName.MainActivityDark"
                "SUNSET" -> "$packageName.MainActivitySunset"
                "CREAM" -> "$packageName.MainActivityCream"
                "NEON" -> "$packageName.MainActivityNeon"
                "GREEN" -> "$packageName.MainActivityGreen"
                "NIGHT" -> "$packageName.MainActivityNight"
                "MONOCHROME" -> "$packageName.MainActivityMonochrome"
                else -> "$packageName.MainActivityDefault"
            }
            val aliases = listOf(
                "$packageName.MainActivityDefault",
                "$packageName.MainActivityDark",
                "$packageName.MainActivitySunset",
                "$packageName.MainActivityCream",
                "$packageName.MainActivityNeon",
                "$packageName.MainActivityGreen",
                "$packageName.MainActivityNight",
                "$packageName.MainActivityMonochrome"
            )
            aliases.forEach { alias ->
                val state = if (alias == targetAlias) {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                try {
                    pm.setComponentEnabledSetting(
                        android.content.ComponentName(context, alias),
                        state,
                        android.content.pm.PackageManager.DONT_KILL_APP
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun setDailyLimit(limit: Int) {
        viewModelScope.launch {
            dataStoreManager.setDailyLimit(limit)
        }
    }
    
    @Keep
    data class BackupData(
        @SerializedName("version") val version: Int = 2,
        @SerializedName("isRegistered") val isRegistered: Boolean,
        @SerializedName("smokingEntries") val smokingEntries: List<Long>,
        @SerializedName("appTheme") val appTheme: String,
        @SerializedName("unlockedAchievements") val unlockedAchievements: Set<String>,
        @SerializedName("dailyLimit") val dailyLimit: Int? = 0,
        @SerializedName("packPrice") val packPrice: Float? = 0.0f,
        @SerializedName("packSize") val packSize: Int? = 20,
        @SerializedName("currency") val currency: String? = "USD",
        @SerializedName("colorPreset") val colorPreset: String? = "SYSTEM",
        @SerializedName("entryTriggers") val entryTriggers: Map<Long, String>? = emptyMap(),
        @SerializedName("fontPreset") val fontPreset: String? = "WIDE",
        @SerializedName("amoledTheme") val amoledTheme: Boolean? = false
    )

    fun backupData(uri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                val currentEntries = repository.smokingEntries.first()
                val data = BackupData(
                    isRegistered = dataStoreManager.isRegistered.first(),
                    smokingEntries = currentEntries.map { it.timestamp },
                    appTheme = dataStoreManager.appTheme.first().name,
                    unlockedAchievements = dataStoreManager.unlockedAchievements.first(),
                    dailyLimit = dataStoreManager.dailyLimit.first(),
                    packPrice = dataStoreManager.packPrice.first(),
                    packSize = dataStoreManager.packSize.first(),
                    currency = dataStoreManager.currency.first(),
                    colorPreset = dataStoreManager.colorPreset.first(),
                    entryTriggers = currentEntries.filter { it.trigger != null }.associate { it.timestamp to it.trigger!! },
                    fontPreset = dataStoreManager.fontPreset.first(),
                    amoledTheme = dataStoreManager.amoledTheme.first()
                )
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        gson.toJson(data, writer)
                    }
                }
                dataStoreManager.setHasMadeBackup(true)
                checkAchievements()
                onSuccess()
            } catch (e: Exception) {
                onError()
            }
        }
    }

    fun restoreData(uri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val data = gson.fromJson(reader, BackupData::class.java)
                        if (data != null) {
                            dataStoreManager.restoreFromBackup(
                                isReg = data.isRegistered,
                                theme = data.appTheme,
                                achievements = data.unlockedAchievements,
                                limit = data.dailyLimit ?: 0,
                                price = data.packPrice ?: 0.0f,
                                size = data.packSize ?: 20,
                                curr = data.currency ?: "USD",
                                colorPresetVal = data.colorPreset ?: "SYSTEM",
                                fontPresetVal = data.fontPreset ?: "WIDE",
                                amoledThemeVal = data.amoledTheme ?: false
                            )
                            val newEntities = data.smokingEntries.map { ts ->
                                SmokingEntryEntity(
                                    timestamp = ts,
                                    trigger = data.entryTriggers?.get(ts)
                                )
                            }
                            repository.clearAndInsertEntries(newEntities)
                            WidgetUpdateManager.updateAllAsync(context)
                            onSuccess()
                        } else {
                            onError()
                        }
                    }
                } ?: onError()
            } catch (e: Exception) {
                onError()
            }
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
