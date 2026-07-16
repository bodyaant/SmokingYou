package com.smokingtracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smoking_tracker_prefs")

enum class ThemePreference {
    SYSTEM, LIGHT, DARK
}

class DataStoreManager(private val context: Context) {
    private val gson = Gson()

    companion object {
        val IS_REGISTERED = booleanPreferencesKey("is_registered")
        val SMOKING_ENTRIES = stringPreferencesKey("smoking_entries")
        val APP_THEME = stringPreferencesKey("app_theme")
        val UNLOCKED_ACHIEVEMENTS = stringPreferencesKey("unlocked_achievements")
        val DAILY_LIMIT = intPreferencesKey("daily_limit")
        val APP_LAUNCH_DATES = stringPreferencesKey("app_launch_dates")
        val FONT_PRESET = stringPreferencesKey("font_preset")
        val AMOLED_THEME = booleanPreferencesKey("amoled_theme")
        val PACK_PRICE = floatPreferencesKey("pack_price")
        val PACK_SIZE = intPreferencesKey("pack_size")
        val CURRENCY = stringPreferencesKey("currency")
        val COLOR_PRESET = stringPreferencesKey("color_preset")
        val ENTRY_TRIGGERS = stringPreferencesKey("entry_triggers")
        val CHECK_UPDATES_ON_START = booleanPreferencesKey("check_updates_on_start")
        val APP_ICON = stringPreferencesKey("app_icon")
    }

    val isRegistered: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_REGISTERED] ?: false
    }

    val appTheme: Flow<ThemePreference> = context.dataStore.data.map { preferences ->
        val themeName = preferences[APP_THEME] ?: ThemePreference.SYSTEM.name
        try {
            ThemePreference.valueOf(themeName)
        } catch (e: Exception) {
            ThemePreference.SYSTEM
        }
    }

    val unlockedAchievements: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        val json = preferences[UNLOCKED_ACHIEVEMENTS] ?: "[]"
        val listType = object : TypeToken<Set<String>>() {}.type
        gson.fromJson(json, listType) ?: emptySet()
    }
    
    val dailyLimit: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_LIMIT] ?: 0
    }

    val appLaunchDates: Flow<List<Long>> = context.dataStore.data.map { preferences ->
        val json = preferences[APP_LAUNCH_DATES] ?: "[]"
        val listType = object : TypeToken<List<Long>>() {}.type
        gson.fromJson(json, listType) ?: emptyList()
    }

    val fontPreset: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FONT_PRESET] ?: "WIDE"
    }

    val amoledTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AMOLED_THEME] ?: false
    }

    val packPrice: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PACK_PRICE] ?: 0.0f
    }

    val packSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PACK_SIZE] ?: 20
    }

    val currency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY] ?: "USD"
    }

    val colorPreset: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[COLOR_PRESET] ?: "SYSTEM"
    }

    val checkUpdatesOnStart: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CHECK_UPDATES_ON_START] ?: true
    }

    val appIcon: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_ICON] ?: "DEFAULT"
    }

    val hasOldData: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences.contains(SMOKING_ENTRIES)
    }

    suspend fun getOldEntriesAndClear(): Pair<List<Long>, Map<Long, String>> {
        var oldEntries: List<Long> = emptyList()
        var oldTriggers: Map<Long, String> = emptyMap()

        context.dataStore.edit { preferences ->
            val entriesJson = preferences[SMOKING_ENTRIES]
            if (entriesJson != null) {
                val listType = object : TypeToken<List<Long>>() {}.type
                oldEntries = gson.fromJson(entriesJson, listType) ?: emptyList()
            }
            val triggersJson = preferences[ENTRY_TRIGGERS]
            if (triggersJson != null) {
                val type = object : TypeToken<Map<Long, String>>() {}.type
                oldTriggers = gson.fromJson(triggersJson, type) ?: emptyMap()
            }
            preferences.remove(SMOKING_ENTRIES)
            preferences.remove(ENTRY_TRIGGERS)
        }
        return Pair(oldEntries, oldTriggers)
    }

    suspend fun saveAmoledTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AMOLED_THEME] = enabled
        }
    }

    suspend fun saveFontPreset(preset: String) {
        context.dataStore.edit { preferences ->
            preferences[FONT_PRESET] = preset
        }
    }

    suspend fun saveUserProfile() {
        context.dataStore.edit { preferences ->
            preferences[IS_REGISTERED] = true
        }
    }

    suspend fun recordAppLaunch(timestamp: Long) {
        context.dataStore.edit { preferences ->
            val json = preferences[APP_LAUNCH_DATES] ?: "[]"
            val listType = object : TypeToken<List<Long>>() {}.type
            val current: MutableList<Long> = gson.fromJson(json, listType) ?: mutableListOf()
            current.add(timestamp)
            current.sort()
            preferences[APP_LAUNCH_DATES] = gson.toJson(current)
        }
    }

    suspend fun saveThemePreference(theme: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme.name
        }
    }

    suspend fun saveUnlockedAchievement(achievementId: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[UNLOCKED_ACHIEVEMENTS] ?: "[]"
            val listType = object : TypeToken<MutableSet<String>>() {}.type
            val current: MutableSet<String> = gson.fromJson(json, listType) ?: mutableSetOf()
            current.add(achievementId)
            preferences[UNLOCKED_ACHIEVEMENTS] = gson.toJson(current)
        }
    }

    suspend fun setUnlockedAchievements(achievements: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[UNLOCKED_ACHIEVEMENTS] = gson.toJson(achievements)
        }
    }
    
    suspend fun setDailyLimit(limit: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_LIMIT] = limit
        }
    }

    suspend fun savePackDetails(price: Float, size: Int, curr: String) {
        context.dataStore.edit { preferences ->
            preferences[PACK_PRICE] = price
            preferences[PACK_SIZE] = size
            preferences[CURRENCY] = curr
        }
    }

    suspend fun saveColorPreset(preset: String) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_PRESET] = preset
        }
    }

    suspend fun saveCheckUpdatesOnStart(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHECK_UPDATES_ON_START] = enabled
        }
    }

    suspend fun saveAppIcon(iconKey: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_ICON] = iconKey
        }
    }

    suspend fun restoreFromBackup(
        isReg: Boolean,
        theme: String,
        achievements: Set<String>,
        limit: Int,
        price: Float,
        size: Int,
        curr: String,
        colorPresetVal: String,
        fontPresetVal: String,
        amoledThemeVal: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[IS_REGISTERED] = isReg
            preferences[APP_THEME] = theme
            preferences[UNLOCKED_ACHIEVEMENTS] = gson.toJson(achievements)
            preferences[DAILY_LIMIT] = limit
            preferences[PACK_PRICE] = price
            preferences[PACK_SIZE] = size
            preferences[CURRENCY] = curr
            preferences[COLOR_PRESET] = colorPresetVal
            preferences[FONT_PRESET] = fontPresetVal
            preferences[AMOLED_THEME] = amoledThemeVal
        }
    }
}
