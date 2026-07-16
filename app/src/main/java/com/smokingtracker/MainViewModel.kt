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
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Calendar

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
        .map { entities -> entities.map { it.timestamp } }
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
        }
    }

    private suspend fun checkAchievements(updatedEntries: List<Long>? = null, wasEntryRemoved: Boolean = false) = withContext(Dispatchers.Default) {
        val entries = updatedEntries ?: repository.smokingEntries.first().map { it.timestamp }
        val launches = dataStoreManager.appLaunchDates.first()
        val previouslyUnlocked = dataStoreManager.unlockedAchievements.first()

        val newUnlockedSet = AchievementsManager.calculateUnlockedAchievements(entries, launches)

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
        }
    }

    fun removeSmokingEntry(timestamp: Long) {
        viewModelScope.launch {
            repository.removeEntry(timestamp)
            val updated = smokingEntries.value.toMutableList().apply {
                remove(timestamp)
            }
            checkAchievements(updated, wasEntryRemoved = true)
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
        }
    }

    fun updatePackDetails(price: Float, size: Int, curr: String) {
        viewModelScope.launch {
            dataStoreManager.savePackDetails(price, size, curr)
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
    
    fun setDailyLimit(limit: Int) {
        viewModelScope.launch {
            dataStoreManager.setDailyLimit(limit)
        }
    }
    
    data class BackupData(
        val version: Int = 2,
        val isRegistered: Boolean,
        val smokingEntries: List<Long>,
        val appTheme: String,
        val unlockedAchievements: Set<String>,
        val dailyLimit: Int? = 0,
        val packPrice: Float? = 0.0f,
        val packSize: Int? = 20,
        val currency: String? = "USD",
        val colorPreset: String? = "SYSTEM",
        val entryTriggers: Map<Long, String>? = emptyMap(),
        val fontPreset: String? = "WIDE",
        val amoledTheme: Boolean? = false
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
}
