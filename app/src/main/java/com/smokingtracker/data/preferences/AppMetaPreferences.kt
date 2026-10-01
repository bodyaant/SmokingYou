package com.smokingtracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class AppMetaPreferences(private val context: Context) {
    private val gson = Gson()

    companion object {
        val IS_REGISTERED = booleanPreferencesKey("is_registered")
        val SMOKING_ENTRIES = stringPreferencesKey("smoking_entries")
        val ENTRY_TRIGGERS = stringPreferencesKey("entry_triggers")
        val UNLOCKED_ACHIEVEMENTS = stringPreferencesKey("unlocked_achievements")
        val UNLOCKED_ACHIEVEMENTS_SET = stringSetPreferencesKey("unlocked_achievements_set")
        val ACHIEVEMENT_UNLOCK_DATES = stringPreferencesKey("achievement_unlock_dates")
        val APP_LAUNCH_DATES = stringPreferencesKey("app_launch_dates")
        val CHECK_UPDATES_ON_START = booleanPreferencesKey("check_updates_on_start")
        val HAS_MADE_BACKUP = booleanPreferencesKey("has_made_backup")
        val HAS_CHANGED_PACK_PRICE = booleanPreferencesKey("has_changed_pack_price")
        val HAS_CANCELLED_WITHIN_10S = booleanPreferencesKey("has_cancelled_within_10s")
        val THEME_LANG_CHANGE_COUNT = intPreferencesKey("theme_lang_change_count")
        val THEME_LANG_CHANGE_DATE = longPreferencesKey("theme_lang_change_date")
        val ANALYTICS_VISIT_COUNT = intPreferencesKey("analytics_visit_count")
        val ANALYTICS_VISIT_DATE = longPreferencesKey("analytics_visit_date")
    }

    val isRegistered: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_REGISTERED] ?: false
    }

    val unlockedAchievements: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[UNLOCKED_ACHIEVEMENTS_SET]
            ?: run {
                val json = preferences[UNLOCKED_ACHIEVEMENTS]
                if (json != null) {
                    val listType = object : TypeToken<Set<String>>() {}.type
                    gson.fromJson(json, listType) ?: emptySet()
                } else {
                    emptySet()
                }
            }
    }

    val achievementUnlockDates: Flow<Map<String, Long>> = context.dataStore.data.map { preferences ->
        val json = preferences[ACHIEVEMENT_UNLOCK_DATES]
        if (json != null) {
            val mapType = object : TypeToken<Map<String, Long>>() {}.type
            gson.fromJson(json, mapType) ?: emptyMap()
        } else {
            emptyMap()
        }
    }

    val appLaunchDates: Flow<List<Long>> = context.dataStore.data.map { preferences ->
        val json = preferences[APP_LAUNCH_DATES] ?: "[]"
        val listType = object : TypeToken<List<Long>>() {}.type
        gson.fromJson(json, listType) ?: emptyList()
    }

    val checkUpdatesOnStart: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CHECK_UPDATES_ON_START] ?: false
    }

    val hasMadeBackup: Flow<Boolean> = context.dataStore.data.map { it[HAS_MADE_BACKUP] ?: false }
    val hasChangedPackPrice: Flow<Boolean> = context.dataStore.data.map { it[HAS_CHANGED_PACK_PRICE] ?: false }
    val hasCancelledWithin10s: Flow<Boolean> = context.dataStore.data.map { it[HAS_CANCELLED_WITHIN_10S] ?: false }

    val themeLangChangeCount: Flow<Int> = context.dataStore.data.map { prefs ->
        val today = getStartOfToday()
        if ((prefs[THEME_LANG_CHANGE_DATE] ?: 0L) == today) prefs[THEME_LANG_CHANGE_COUNT] ?: 0 else 0
    }

    val analyticsVisitCount: Flow<Int> = context.dataStore.data.map { prefs ->
        val today = getStartOfToday()
        if ((prefs[ANALYTICS_VISIT_DATE] ?: 0L) == today) prefs[ANALYTICS_VISIT_COUNT] ?: 0 else 0
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

    suspend fun saveUnlockedAchievement(achievementId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[UNLOCKED_ACHIEVEMENTS_SET]?.toMutableSet()
                ?: run {
                    val json = preferences[UNLOCKED_ACHIEVEMENTS]
                    val migrated: MutableSet<String> = if (json != null) {
                        val listType = object : TypeToken<Set<String>>() {}.type
                        gson.fromJson<Set<String>>(json, listType)?.toMutableSet() ?: mutableSetOf()
                    } else mutableSetOf()
                    preferences.remove(UNLOCKED_ACHIEVEMENTS)
                    migrated
                }
            current.add(achievementId)
            preferences[UNLOCKED_ACHIEVEMENTS_SET] = current
        }
    }

    suspend fun setUnlockedAchievements(achievements: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[UNLOCKED_ACHIEVEMENTS_SET] = achievements
            preferences.remove(UNLOCKED_ACHIEVEMENTS)
        }
    }

    suspend fun saveAchievementUnlockDates(dates: Map<String, Long>) {
        context.dataStore.edit { preferences ->
            preferences[ACHIEVEMENT_UNLOCK_DATES] = gson.toJson(dates)
        }
    }

    suspend fun recordAchievementUnlockDate(achievementId: String, timestamp: Long) {
        context.dataStore.edit { preferences ->
            val json = preferences[ACHIEVEMENT_UNLOCK_DATES]
            val mapType = object : TypeToken<Map<String, Long>>() {}.type
            val currentMap: MutableMap<String, Long> = if (json != null) {
                (gson.fromJson<Map<String, Long>>(json, mapType) ?: emptyMap()).toMutableMap()
            } else {
                mutableMapOf()
            }
            if (!currentMap.containsKey(achievementId)) {
                currentMap[achievementId] = timestamp
                preferences[ACHIEVEMENT_UNLOCK_DATES] = gson.toJson(currentMap)
            }
        }
    }

    suspend fun setHasMadeBackup(value: Boolean = true) {
        context.dataStore.edit { it[HAS_MADE_BACKUP] = value }
    }

    suspend fun setHasChangedPackPrice(value: Boolean = true) {
        context.dataStore.edit { it[HAS_CHANGED_PACK_PRICE] = value }
    }

    suspend fun setHasCancelledWithin10s(value: Boolean = true) {
        context.dataStore.edit { it[HAS_CANCELLED_WITHIN_10S] = value }
    }

    suspend fun recordThemeOrLangChange(): Int {
        val today = getStartOfToday()
        var newCount = 1
        context.dataStore.edit { prefs ->
            val lastDate = prefs[THEME_LANG_CHANGE_DATE] ?: 0L
            if (lastDate == today) {
                newCount = (prefs[THEME_LANG_CHANGE_COUNT] ?: 0) + 1
            } else {
                newCount = 1
            }
            prefs[THEME_LANG_CHANGE_DATE] = today
            prefs[THEME_LANG_CHANGE_COUNT] = newCount
        }
        return newCount
    }

    suspend fun recordAnalyticsVisit(): Int {
        val today = getStartOfToday()
        var newCount = 1
        context.dataStore.edit { prefs ->
            val lastDate = prefs[ANALYTICS_VISIT_DATE] ?: 0L
            if (lastDate == today) {
                newCount = (prefs[ANALYTICS_VISIT_COUNT] ?: 0) + 1
            } else {
                newCount = 1
            }
            prefs[ANALYTICS_VISIT_DATE] = today
            prefs[ANALYTICS_VISIT_COUNT] = newCount
        }
        return newCount
    }

    suspend fun saveCheckUpdatesOnStart(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHECK_UPDATES_ON_START] = enabled
        }
    }

    private fun getStartOfToday(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    suspend fun resetAppMeta() {
        context.dataStore.edit { preferences ->
            preferences[IS_REGISTERED] = false
            preferences.remove(UNLOCKED_ACHIEVEMENTS)
            preferences.remove(UNLOCKED_ACHIEVEMENTS_SET)
            preferences.remove(ACHIEVEMENT_UNLOCK_DATES)
            preferences.remove(APP_LAUNCH_DATES)
            preferences.remove(HAS_CHANGED_PACK_PRICE)
            preferences.remove(HAS_CANCELLED_WITHIN_10S)
            preferences.remove(THEME_LANG_CHANGE_COUNT)
            preferences.remove(THEME_LANG_CHANGE_DATE)
            preferences.remove(ANALYTICS_VISIT_COUNT)
            preferences.remove(ANALYTICS_VISIT_DATE)
            preferences.remove(ENTRY_TRIGGERS)
            preferences.remove(SMOKING_ENTRIES)
        }
    }

    suspend fun restoreAppMetaPreferences(
        isReg: Boolean,
        achievements: Set<String>,
        achievementUnlockDatesVal: Map<String, Long> = emptyMap(),
        hasBackupVal: Boolean = false,
        hasPriceChangedVal: Boolean = false,
        hasCancelled10sVal: Boolean = false,
        launchesVal: List<Long> = emptyList(),
        checkUpdatesOnStartVal: Boolean = false
    ) {
        context.dataStore.edit { preferences ->
            preferences[IS_REGISTERED] = isReg
            preferences[UNLOCKED_ACHIEVEMENTS_SET] = achievements
            preferences.remove(UNLOCKED_ACHIEVEMENTS)
            if (achievementUnlockDatesVal.isNotEmpty()) {
                preferences[ACHIEVEMENT_UNLOCK_DATES] = gson.toJson(achievementUnlockDatesVal)
            }
            preferences[HAS_MADE_BACKUP] = hasBackupVal
            preferences[HAS_CHANGED_PACK_PRICE] = hasPriceChangedVal
            preferences[HAS_CANCELLED_WITHIN_10S] = hasCancelled10sVal
            preferences[APP_LAUNCH_DATES] = gson.toJson(launchesVal)
            preferences[CHECK_UPDATES_ON_START] = checkUpdatesOnStartVal
        }
    }
}
