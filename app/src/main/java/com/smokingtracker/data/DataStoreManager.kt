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

enum class FontPreset { WIDE, AIRY, SYSTEM, OUTFIT }
enum class ColorPreset { SYSTEM, FOREST_SAGE, SUNSET_ROSE, OCEAN_DEEP, PURPLE_NEBULA, AMBER_GOLD, CRIMSON_BERRY, SLATE_MONO }
enum class AppIconPreset { DEFAULT, DARK, SUNSET, CREAM, NEON, GREEN, NIGHT, MONOCHROME }
enum class ContainerStyle { EXPRESSIVE, STANDARD }

class DataStoreManager(private val context: Context) {
    private val gson = Gson()

    companion object {
        val IS_REGISTERED = booleanPreferencesKey("is_registered")
        val SMOKING_ENTRIES = stringPreferencesKey("smoking_entries")
        val APP_THEME = stringPreferencesKey("app_theme")
        val UNLOCKED_ACHIEVEMENTS = stringPreferencesKey("unlocked_achievements")
        val UNLOCKED_ACHIEVEMENTS_SET = stringSetPreferencesKey("unlocked_achievements_set")
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
        val HAS_MADE_BACKUP = booleanPreferencesKey("has_made_backup")
        val HAS_CHANGED_PACK_PRICE = booleanPreferencesKey("has_changed_pack_price")
        val HAS_CANCELLED_WITHIN_10S = booleanPreferencesKey("has_cancelled_within_10s")
        val THEME_LANG_CHANGE_COUNT = intPreferencesKey("theme_lang_change_count")
        val THEME_LANG_CHANGE_DATE = longPreferencesKey("theme_lang_change_date")
        val ANALYTICS_VISIT_COUNT = intPreferencesKey("analytics_visit_count")
        val ANALYTICS_VISIT_DATE = longPreferencesKey("analytics_visit_date")
        val TAPERING_PLAN_ENABLED = booleanPreferencesKey("tapering_plan_enabled")
        val TAPERING_INTERVAL_DAYS = intPreferencesKey("tapering_interval_days")
        val LAST_TAPERING_CHECKIN_DATE = longPreferencesKey("last_tapering_checkin_date")
        val HAS_HISTORICAL_BASELINE = booleanPreferencesKey("has_historical_baseline")
        val HISTORICAL_START_DATE = longPreferencesKey("historical_start_date")
        val HISTORICAL_DAILY_AVG = intPreferencesKey("historical_daily_avg")
        val HISTORICAL_PACK_PRICE = floatPreferencesKey("historical_pack_price")
        val HISTORICAL_PACK_SIZE = intPreferencesKey("historical_pack_size")
        val HISTORICAL_TRIGGER_PRIORITIES = stringPreferencesKey("historical_trigger_priorities")
        val CONTAINER_BORDER_ENABLED = booleanPreferencesKey("container_border_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val CONTAINER_STYLE = stringPreferencesKey("container_style")
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
    
    val dailyLimit: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_LIMIT] ?: 0
    }

    val appLaunchDates: Flow<List<Long>> = context.dataStore.data.map { preferences ->
        val json = preferences[APP_LAUNCH_DATES] ?: "[]"
        val listType = object : TypeToken<List<Long>>() {}.type
        gson.fromJson(json, listType) ?: emptyList()
    }

    val fontPreset: Flow<FontPreset> = context.dataStore.data.map { preferences ->
        val name = preferences[FONT_PRESET] ?: FontPreset.WIDE.name
        try { FontPreset.valueOf(name) } catch (_: Exception) { FontPreset.WIDE }
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

    val colorPreset: Flow<ColorPreset> = context.dataStore.data.map { preferences ->
        val name = preferences[COLOR_PRESET] ?: ColorPreset.SYSTEM.name
        try { ColorPreset.valueOf(name) } catch (_: Exception) { ColorPreset.SYSTEM }
    }

    val checkUpdatesOnStart: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CHECK_UPDATES_ON_START] ?: true
    }

    val appIcon: Flow<AppIconPreset> = context.dataStore.data.map { preferences ->
        val name = preferences[APP_ICON] ?: AppIconPreset.DEFAULT.name
        try { AppIconPreset.valueOf(name) } catch (_: Exception) { AppIconPreset.DEFAULT }
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

    suspend fun saveFontPreset(preset: FontPreset) {
        context.dataStore.edit { preferences ->
            preferences[FONT_PRESET] = preset.name
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

    suspend fun saveColorPreset(preset: ColorPreset) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_PRESET] = preset.name
        }
    }

    suspend fun saveCheckUpdatesOnStart(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHECK_UPDATES_ON_START] = enabled
        }
    }

    suspend fun saveAppIcon(preset: AppIconPreset) {
        context.dataStore.edit { preferences ->
            preferences[APP_ICON] = preset.name
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
        amoledThemeVal: Boolean,
        vibrationEnabledVal: Boolean = false,
        hasBackupVal: Boolean = false,
        hasPriceChangedVal: Boolean = false,
        hasCancelled10sVal: Boolean = false,
        launchesVal: List<Long> = emptyList()
    ) {
        context.dataStore.edit { preferences ->
            preferences[IS_REGISTERED] = isReg
            preferences[APP_THEME] = theme
            preferences[UNLOCKED_ACHIEVEMENTS_SET] = achievements
            preferences.remove(UNLOCKED_ACHIEVEMENTS)
            preferences[DAILY_LIMIT] = limit
            preferences[PACK_PRICE] = price
            preferences[PACK_SIZE] = size
            preferences[CURRENCY] = curr
            preferences[COLOR_PRESET] = colorPresetVal
            preferences[FONT_PRESET] = fontPresetVal
            preferences[AMOLED_THEME] = amoledThemeVal
            preferences[VIBRATION_ENABLED] = vibrationEnabledVal
            preferences[HAS_MADE_BACKUP] = hasBackupVal
            preferences[HAS_CHANGED_PACK_PRICE] = hasPriceChangedVal
            preferences[HAS_CANCELLED_WITHIN_10S] = hasCancelled10sVal
            preferences[APP_LAUNCH_DATES] = gson.toJson(launchesVal)
        }
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

    val taperingPlanEnabled: Flow<Boolean> = context.dataStore.data.map { it[TAPERING_PLAN_ENABLED] ?: false }
    val taperingIntervalDays: Flow<Int> = context.dataStore.data.map { it[TAPERING_INTERVAL_DAYS] ?: 7 }
    val lastTaperingCheckinDate: Flow<Long> = context.dataStore.data.map { it[LAST_TAPERING_CHECKIN_DATE] ?: 0L }

    suspend fun setTaperingPlanEnabled(enabled: Boolean) {
        context.dataStore.edit { it[TAPERING_PLAN_ENABLED] = enabled }
    }

    suspend fun setTaperingIntervalDays(days: Int) {
        context.dataStore.edit { it[TAPERING_INTERVAL_DAYS] = days }
    }

    suspend fun updateLastTaperingCheckinDate(timestamp: Long) {
        context.dataStore.edit { it[LAST_TAPERING_CHECKIN_DATE] = timestamp }
    }

    val hasHistoricalBaseline: Flow<Boolean> = context.dataStore.data.map { it[HAS_HISTORICAL_BASELINE] ?: false }
    val historicalStartDate: Flow<Long> = context.dataStore.data.map { it[HISTORICAL_START_DATE] ?: 0L }
    val historicalDailyAvg: Flow<Int> = context.dataStore.data.map { it[HISTORICAL_DAILY_AVG] ?: 0 }
    val historicalPackPrice: Flow<Float> = context.dataStore.data.map { it[HISTORICAL_PACK_PRICE] ?: 0f }
    val historicalPackSize: Flow<Int> = context.dataStore.data.map { it[HISTORICAL_PACK_SIZE] ?: 20 }
    val historicalTriggerPriorities: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[HISTORICAL_TRIGGER_PRIORITIES] ?: "[]"
        val listType = object : TypeToken<List<String>>() {}.type
        gson.fromJson(json, listType) ?: emptyList()
    }

    suspend fun saveHistoricalBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        triggerPriorities: List<String>
    ) {
        context.dataStore.edit { prefs ->
            prefs[HAS_HISTORICAL_BASELINE] = true
            prefs[HISTORICAL_START_DATE] = startDate
            prefs[HISTORICAL_DAILY_AVG] = dailyAvg
            prefs[HISTORICAL_PACK_PRICE] = packPrice
            prefs[HISTORICAL_PACK_SIZE] = packSize
            prefs[HISTORICAL_TRIGGER_PRIORITIES] = gson.toJson(triggerPriorities)
        }
    }

    suspend fun clearHistoricalBaseline() {
        context.dataStore.edit { prefs ->
            prefs[HAS_HISTORICAL_BASELINE] = false
            prefs.remove(HISTORICAL_START_DATE)
            prefs.remove(HISTORICAL_DAILY_AVG)
            prefs.remove(HISTORICAL_PACK_PRICE)
            prefs.remove(HISTORICAL_PACK_SIZE)
            prefs.remove(HISTORICAL_TRIGGER_PRIORITIES)
        }
    }

    val containerBorderEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CONTAINER_BORDER_ENABLED] ?: true
    }

    suspend fun saveContainerBorderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CONTAINER_BORDER_ENABLED] = enabled
        }
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: false
    }

    suspend fun saveVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    val containerStyle: Flow<ContainerStyle> = context.dataStore.data.map { preferences ->
        val name = preferences[CONTAINER_STYLE] ?: ContainerStyle.EXPRESSIVE.name
        try { ContainerStyle.valueOf(name) } catch (_: Exception) { ContainerStyle.EXPRESSIVE }
    }

    suspend fun saveContainerStyle(style: ContainerStyle) {
        context.dataStore.edit { preferences ->
            preferences[CONTAINER_STYLE] = style.name
        }
    }

    private fun getStartOfToday(): Long {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
