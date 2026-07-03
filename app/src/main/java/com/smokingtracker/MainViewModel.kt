package com.smokingtracker

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Calendar

class MainViewModel(private val dataStoreManager: DataStoreManager, private val context: Context) : ViewModel() {

    private val gson = Gson()

    val isRegistered: StateFlow<Boolean?> = dataStoreManager.isRegistered.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val smokingEntries: StateFlow<List<Long>> = dataStoreManager.smokingEntries.stateIn(
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

    private suspend fun checkAchievements(updatedEntries: List<Long>? = null) = withContext(Dispatchers.Default) {
        val entries = updatedEntries ?: smokingEntries.value
        val launches = appLaunchDates.value
        val previouslyUnlocked = unlockedAchievements.value
        
        val newUnlockedSet = AchievementsManager.calculateUnlockedAchievements(entries, launches)

        val newlyUnlocked = newUnlockedSet - previouslyUnlocked
        newlyUnlocked.forEach { achievementId ->
            AchievementsManager.sendNotificationForAchievement(context, achievementId)
        }

        dataStoreManager.setUnlockedAchievements(newUnlockedSet)
    }

    fun registerUser() {
        viewModelScope.launch {
            dataStoreManager.saveUserProfile()
        }
    }

    fun addSmokingEntry(timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            dataStoreManager.addSmokingEntry(timestamp)
            val updated = smokingEntries.value.toMutableList().apply { 
                add(timestamp)
                sort()
            }
            checkAchievements(updated)
        }
    }

    fun removeSmokingEntry(timestamp: Long) {
        viewModelScope.launch {
            dataStoreManager.removeSmokingEntry(timestamp)
            val updated = smokingEntries.value.toMutableList().apply { 
                remove(timestamp)
            }
            checkAchievements(updated)
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
        val isRegistered: Boolean,
        val smokingEntries: List<Long>,
        val appTheme: String,
        val unlockedAchievements: Set<String>,
        val dailyLimit: Int? = 0
    )

    fun backupData(uri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                val data = BackupData(
                    isRegistered = dataStoreManager.isRegistered.first() ?: false,
                    smokingEntries = dataStoreManager.smokingEntries.first(),
                    appTheme = dataStoreManager.appTheme.first().name,
                    unlockedAchievements = dataStoreManager.unlockedAchievements.first(),
                    dailyLimit = dataStoreManager.dailyLimit.first()
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
                                entries = data.smokingEntries,
                                theme = data.appTheme,
                                achievements = data.unlockedAchievements,
                                limit = data.dailyLimit ?: 0
                            )
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
