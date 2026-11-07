package com.smokingtracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smokingtracker.startOfTodayMillis
import com.smokingtracker.toLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

data class AchievementMetaSnapshot(
    val isRegistered: Boolean,
    val appLaunchDates: List<Long>,
    val hasMadeBackup: Boolean,
    val hasChangedPackPrice: Boolean,
    val hasCancelledWithin10s: Boolean,
    val themeLangChangeCount: Int,
    val analyticsVisitCount: Int,
    val unlockedAchievements: Set<String>,
    val achievementUnlockDates: Map<String, Long>,
    val themeLangChangeDate: Long = 0L,
    val analyticsVisitDate: Long = 0L
)

class AppMetaPreferences(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    private val dataFlow: Flow<Preferences> = context.dataStore.safeData

    private val stringSetType = object : TypeToken<Set<String?>>() {}.type
    private val stringListType = object : TypeToken<List<String?>>() {}.type
    private val unlockDatesMapType = object : TypeToken<Map<String?, Long?>>() {}.type
    private val longListType = object : TypeToken<List<Long?>>() {}.type
    private val stringMapType = object : TypeToken<Map<String?, String?>>() {}.type

    companion object {
        const val MAX_LAUNCH_HISTORY_DAYS = 400

        val IS_REGISTERED = booleanPreferencesKey("is_registered")
        val SMOKING_ENTRIES = stringPreferencesKey("smoking_entries")
        val ENTRY_TRIGGERS = stringPreferencesKey("entry_triggers")
        val UNLOCKED_ACHIEVEMENTS = stringPreferencesKey("unlocked_achievements")
        val UNLOCKED_ACHIEVEMENTS_SET = stringSetPreferencesKey("unlocked_achievements_set")
        val ACHIEVEMENT_UNLOCK_DATES = stringPreferencesKey("achievement_unlock_dates")
        val PENDING_ACHIEVEMENT_POPUPS = stringPreferencesKey("pending_achievement_popups")
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

    val isRegistered: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[IS_REGISTERED] ?: false
    }.distinctUntilChanged()

    val unlockedAchievements: Flow<Set<String>> = dataFlow.map { preferences ->
        preferences[UNLOCKED_ACHIEVEMENTS_SET]
            ?: parseStringSet(preferences[UNLOCKED_ACHIEVEMENTS])
    }.distinctUntilChanged()

    val achievementUnlockDates: Flow<Map<String, Long>> = dataFlow.map { preferences ->
        parseUnlockDatesMap(preferences[ACHIEVEMENT_UNLOCK_DATES])
    }.distinctUntilChanged()

    val pendingAchievementPopups: Flow<List<String>> = dataFlow.map { preferences ->
        parseStringList(preferences[PENDING_ACHIEVEMENT_POPUPS])
    }.distinctUntilChanged()

    val appLaunchDates: Flow<List<Long>> = dataFlow.map { preferences ->
        parseLongList(preferences[APP_LAUNCH_DATES])
    }.distinctUntilChanged()

    val checkUpdatesOnStart: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[CHECK_UPDATES_ON_START] ?: false
    }.distinctUntilChanged()

    val hasMadeBackup: Flow<Boolean> = dataFlow.map { it[HAS_MADE_BACKUP] ?: false }
        .distinctUntilChanged()

    val hasChangedPackPrice: Flow<Boolean> = dataFlow.map { it[HAS_CHANGED_PACK_PRICE] ?: false }
        .distinctUntilChanged()

    val hasCancelledWithin10s: Flow<Boolean> = dataFlow.map { it[HAS_CANCELLED_WITHIN_10S] ?: false }
        .distinctUntilChanged()

    val themeLangChangeCount: Flow<Int> = dataFlow.map { prefs ->
        val lastDate = prefs[THEME_LANG_CHANGE_DATE] ?: 0L
        if (isSameDayAsToday(lastDate)) (prefs[THEME_LANG_CHANGE_COUNT] ?: 0).coerceAtLeast(0) else 0
    }.distinctUntilChanged()

    val themeLangChangeDate: Flow<Long> = dataFlow.map { prefs ->
        (prefs[THEME_LANG_CHANGE_DATE] ?: 0L).coerceAtLeast(0L)
    }.distinctUntilChanged()

    val analyticsVisitCount: Flow<Int> = dataFlow.map { prefs ->
        val lastDate = prefs[ANALYTICS_VISIT_DATE] ?: 0L
        if (isSameDayAsToday(lastDate)) (prefs[ANALYTICS_VISIT_COUNT] ?: 0).coerceAtLeast(0) else 0
    }.distinctUntilChanged()

    val analyticsVisitDate: Flow<Long> = dataFlow.map { prefs ->
        (prefs[ANALYTICS_VISIT_DATE] ?: 0L).coerceAtLeast(0L)
    }.distinctUntilChanged()

    val hasOldData: Flow<Boolean> = dataFlow.map { preferences ->
        preferences.contains(SMOKING_ENTRIES)
    }.distinctUntilChanged()

    suspend fun getOldEntries(): Pair<List<Long>, Map<Long, String>> {
        val preferences = dataFlow.first()
        val oldEntries = parseLongList(preferences[SMOKING_ENTRIES])
        val oldTriggers = parseOldTriggersMap(preferences[ENTRY_TRIGGERS])
        return Pair(oldEntries, oldTriggers)
    }

    suspend fun clearOldEntries() {
        context.dataStore.edit { preferences ->
            preferences.remove(SMOKING_ENTRIES)
            preferences.remove(ENTRY_TRIGGERS)
        }
    }

    suspend fun getOldEntriesAndClear(): Pair<List<Long>, Map<Long, String>> {
        var oldEntries: List<Long> = emptyList()
        var oldTriggers: Map<Long, String> = emptyMap()

        context.dataStore.edit { preferences ->
            oldEntries = parseLongList(preferences[SMOKING_ENTRIES])
            oldTriggers = parseOldTriggersMap(preferences[ENTRY_TRIGGERS])
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
        if (timestamp <= 0L) return
        context.dataStore.edit { preferences ->
            val current = parseLongList(preferences[APP_LAUNCH_DATES])
            val normalized = normalizeLaunchDates(current, timestamp)
            if (normalized != current) {
                preferences[APP_LAUNCH_DATES] = gson.toJson(normalized)
            }
        }
    }

    suspend fun saveUnlockedAchievement(achievementId: String) {
        val trimmedId = achievementId.trim()
        if (trimmedId.isEmpty()) return
        context.dataStore.edit { preferences ->
            val current = preferences[UNLOCKED_ACHIEVEMENTS_SET]?.toMutableSet()
                ?: run {
                    val migrated = parseStringSet(preferences[UNLOCKED_ACHIEVEMENTS]).toMutableSet()
                    preferences.remove(UNLOCKED_ACHIEVEMENTS)
                    migrated
                }
            current.add(trimmedId)
            preferences[UNLOCKED_ACHIEVEMENTS_SET] = current
        }
    }

    suspend fun getAchievementMetaSnapshot(): AchievementMetaSnapshot {
        val prefs = dataFlow.first()
        val lastThemeDate = prefs[THEME_LANG_CHANGE_DATE] ?: 0L
        val themeCount = if (isSameDayAsToday(lastThemeDate)) {
            (prefs[THEME_LANG_CHANGE_COUNT] ?: 0).coerceAtLeast(0)
        } else {
            0
        }
        val lastAnalyticsDate = prefs[ANALYTICS_VISIT_DATE] ?: 0L
        val analyticsCount = if (isSameDayAsToday(lastAnalyticsDate)) {
            (prefs[ANALYTICS_VISIT_COUNT] ?: 0).coerceAtLeast(0)
        } else {
            0
        }
        val unlocked = prefs[UNLOCKED_ACHIEVEMENTS_SET]
            ?: parseStringSet(prefs[UNLOCKED_ACHIEVEMENTS])
        return AchievementMetaSnapshot(
            isRegistered = prefs[IS_REGISTERED] ?: false,
            appLaunchDates = parseLongList(prefs[APP_LAUNCH_DATES]),
            hasMadeBackup = prefs[HAS_MADE_BACKUP] ?: false,
            hasChangedPackPrice = prefs[HAS_CHANGED_PACK_PRICE] ?: false,
            hasCancelledWithin10s = prefs[HAS_CANCELLED_WITHIN_10S] ?: false,
            themeLangChangeCount = themeCount,
            analyticsVisitCount = analyticsCount,
            unlockedAchievements = unlocked,
            achievementUnlockDates = parseUnlockDatesMap(prefs[ACHIEVEMENT_UNLOCK_DATES]),
            themeLangChangeDate = lastThemeDate.coerceAtLeast(0L),
            analyticsVisitDate = lastAnalyticsDate.coerceAtLeast(0L)
        )
    }

    suspend fun saveAchievementsState(
        achievements: Set<String>,
        dates: Map<String, Long>,
        newlyUnlockedIds: List<String> = emptyList()
    ) {
        val sanitizedAchievements = achievements.mapNotNull { it.trim().takeIf(String::isNotEmpty) }.toSet()
        val sanitizedDates = dates.entries
            .mapNotNull { (k, v) ->
                val key = k.trim()
                if (key.isNotEmpty() && v > 0L) key to v else null
            }
            .toMap()
        val sanitizedNewIds = newlyUnlockedIds.mapNotNull { it.trim().takeIf(String::isNotEmpty) }
        context.dataStore.edit { preferences ->
            preferences[UNLOCKED_ACHIEVEMENTS_SET] = sanitizedAchievements
            preferences.remove(UNLOCKED_ACHIEVEMENTS)
            if (sanitizedDates.isNotEmpty()) {
                preferences[ACHIEVEMENT_UNLOCK_DATES] = gson.toJson(sanitizedDates)
            } else {
                preferences.remove(ACHIEVEMENT_UNLOCK_DATES)
            }
            if (sanitizedNewIds.isNotEmpty()) {
                val existingQueue = parseStringList(preferences[PENDING_ACHIEVEMENT_POPUPS])
                val mergedQueue = (existingQueue + sanitizedNewIds).distinct()
                preferences[PENDING_ACHIEVEMENT_POPUPS] = gson.toJson(mergedQueue)
            }
        }
    }

    suspend fun removePendingAchievementPopup(achievementId: String) {
        val cleanId = achievementId.trim()
        if (cleanId.isEmpty()) return
        context.dataStore.edit { preferences ->
            val current = parseStringList(preferences[PENDING_ACHIEVEMENT_POPUPS])
            val updated = current.filterNot { it == cleanId }
            if (updated.isNotEmpty()) {
                preferences[PENDING_ACHIEVEMENT_POPUPS] = gson.toJson(updated)
            } else {
                preferences.remove(PENDING_ACHIEVEMENT_POPUPS)
            }
        }
    }

    suspend fun clearPendingAchievementPopups() {
        context.dataStore.edit { preferences ->
            preferences.remove(PENDING_ACHIEVEMENT_POPUPS)
        }
    }

    suspend fun setUnlockedAchievements(achievements: Set<String>) {
        val sanitized = achievements.mapNotNull { it.trim().takeIf(String::isNotEmpty) }.toSet()
        context.dataStore.edit { preferences ->
            preferences[UNLOCKED_ACHIEVEMENTS_SET] = sanitized
            preferences.remove(UNLOCKED_ACHIEVEMENTS)
        }
    }

    suspend fun saveAchievementUnlockDates(dates: Map<String, Long>) {
        val sanitized = dates.entries
            .mapNotNull { (k, v) ->
                val key = k.trim()
                if (key.isNotEmpty() && v > 0L) key to v else null
            }
            .toMap()
        context.dataStore.edit { preferences ->
            preferences[ACHIEVEMENT_UNLOCK_DATES] = gson.toJson(sanitized)
        }
    }

    suspend fun recordAchievementUnlockDate(achievementId: String, timestamp: Long) {
        val trimmedId = achievementId.trim()
        if (trimmedId.isEmpty() || timestamp <= 0L) return
        context.dataStore.edit { preferences ->
            val currentMap = parseUnlockDatesMap(preferences[ACHIEVEMENT_UNLOCK_DATES]).toMutableMap()
            if (!currentMap.containsKey(trimmedId)) {
                currentMap[trimmedId] = timestamp
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
        val today = startOfTodayMillis()
        var newCount = 1
        context.dataStore.edit { prefs ->
            val lastDate = prefs[THEME_LANG_CHANGE_DATE] ?: 0L
            newCount = if (isSameDayAsToday(lastDate)) {
                (prefs[THEME_LANG_CHANGE_COUNT] ?: 0).coerceAtLeast(0) + 1
            } else {
                1
            }
            prefs[THEME_LANG_CHANGE_DATE] = today
            prefs[THEME_LANG_CHANGE_COUNT] = newCount
        }
        return newCount
    }

    suspend fun recordAnalyticsVisit(): Int {
        val today = startOfTodayMillis()
        var newCount = 1
        context.dataStore.edit { prefs ->
            val lastDate = prefs[ANALYTICS_VISIT_DATE] ?: 0L
            newCount = if (isSameDayAsToday(lastDate)) {
                (prefs[ANALYTICS_VISIT_COUNT] ?: 0).coerceAtLeast(0) + 1
            } else {
                1
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

    private fun isSameDayAsToday(epochMillis: Long): Boolean {
        if (epochMillis <= 0L) return false
        return toLocalDate(epochMillis) == LocalDate.now()
    }

    private fun parseStringSet(json: String?): Set<String> {
        return gson.fromJsonOrNull<Set<String?>>(json, stringSetType)
            ?.mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
            ?.toSet()
            ?: emptySet()
    }

    private fun parseStringList(json: String?): List<String> {
        return gson.fromJsonOrNull<List<String?>>(json, stringListType)
            ?.mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
            ?.distinct()
            ?: emptyList()
    }

    private fun parseUnlockDatesMap(json: String?): Map<String, Long> {
        val raw = gson.fromJsonOrNull<Map<String?, Long?>>(json, unlockDatesMapType) ?: return emptyMap()
        return raw.entries
            .mapNotNull { (k, v) ->
                val key = k?.trim()
                if (!key.isNullOrEmpty() && v != null && v > 0L) key to v else null
            }
            .toMap()
    }

    private fun parseLongList(json: String?): List<Long> {
        return gson.fromJsonOrNull<List<Long?>>(json, longListType)
            ?.mapNotNull { ts -> ts?.takeIf { it > 0L } }
            ?: emptyList()
    }

    private fun parseOldTriggersMap(json: String?): Map<Long, String> {
        val raw = gson.fromJsonOrNull<Map<String?, String?>>(json, stringMapType) ?: return emptyMap()
        return raw.entries
            .mapNotNull { (k, v) ->
                val ts = k?.trim()?.toLongOrNull()
                val trigger = v?.trim()?.takeIf(String::isNotEmpty)
                if (ts != null && ts > 0L && trigger != null) ts to trigger else null
            }
            .toMap()
    }

    private fun normalizeLaunchDates(existing: List<Long>, newTimestamp: Long? = null): List<Long> {
        val combined = buildList {
            for (ts in existing) {
                if (ts > 0L) add(ts)
            }
            if (newTimestamp != null && newTimestamp > 0L) {
                add(newTimestamp)
            }
        }
        if (combined.isEmpty()) return emptyList()

        val sorted = combined.sorted()
        val dedupedByDay = ArrayList<Long>(sorted.size)
        var lastDay: LocalDate? = null
        for (ts in sorted) {
            val day = toLocalDate(ts)
            if (day != lastDay) {
                dedupedByDay.add(ts)
                lastDay = day
            }
        }

        if (dedupedByDay.size <= MAX_LAUNCH_HISTORY_DAYS) {
            return dedupedByDay
        }

        val firstLaunch = dedupedByDay.first()
        val recentTail = dedupedByDay.takeLast(MAX_LAUNCH_HISTORY_DAYS - 1)
        return buildList(MAX_LAUNCH_HISTORY_DAYS) {
            add(firstLaunch)
            addAll(recentTail)
        }
    }

    suspend fun resetAppMeta() {
        context.dataStore.edit { preferences ->
            preferences[IS_REGISTERED] = false
            preferences.remove(UNLOCKED_ACHIEVEMENTS)
            preferences.remove(UNLOCKED_ACHIEVEMENTS_SET)
            preferences.remove(ACHIEVEMENT_UNLOCK_DATES)
            preferences.remove(PENDING_ACHIEVEMENT_POPUPS)
            preferences.remove(APP_LAUNCH_DATES)
            preferences.remove(CHECK_UPDATES_ON_START)
            preferences.remove(HAS_MADE_BACKUP)
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
        checkUpdatesOnStartVal: Boolean = false,
        themeLangChangeCountVal: Int = 0,
        themeLangChangeDateVal: Long = 0L,
        analyticsVisitCountVal: Int = 0,
        analyticsVisitDateVal: Long = 0L
    ) {
        val sanitizedAchievements = achievements.mapNotNull { it.trim().takeIf(String::isNotEmpty) }.toSet()
        val sanitizedDates = achievementUnlockDatesVal.entries
            .mapNotNull { (k, v) ->
                val key = k.trim()
                if (key.isNotEmpty() && v > 0L) key to v else null
            }
            .toMap()
        val normalizedLaunches = normalizeLaunchDates(launchesVal)

        context.dataStore.edit { preferences ->
            preferences[IS_REGISTERED] = isReg
            preferences[UNLOCKED_ACHIEVEMENTS_SET] = sanitizedAchievements
            preferences.remove(UNLOCKED_ACHIEVEMENTS)
            if (sanitizedDates.isNotEmpty()) {
                preferences[ACHIEVEMENT_UNLOCK_DATES] = gson.toJson(sanitizedDates)
            } else {
                preferences.remove(ACHIEVEMENT_UNLOCK_DATES)
            }
            preferences[HAS_MADE_BACKUP] = hasBackupVal
            preferences[HAS_CHANGED_PACK_PRICE] = hasPriceChangedVal
            preferences[HAS_CANCELLED_WITHIN_10S] = hasCancelled10sVal
            preferences[APP_LAUNCH_DATES] = gson.toJson(normalizedLaunches)
            preferences[CHECK_UPDATES_ON_START] = checkUpdatesOnStartVal
            if (themeLangChangeCountVal > 0 && themeLangChangeDateVal > 0L) {
                preferences[THEME_LANG_CHANGE_COUNT] = themeLangChangeCountVal.coerceAtLeast(0)
                preferences[THEME_LANG_CHANGE_DATE] = themeLangChangeDateVal.coerceAtLeast(0L)
            } else {
                preferences.remove(THEME_LANG_CHANGE_COUNT)
                preferences.remove(THEME_LANG_CHANGE_DATE)
            }
            if (analyticsVisitCountVal > 0 && analyticsVisitDateVal > 0L) {
                preferences[ANALYTICS_VISIT_COUNT] = analyticsVisitCountVal.coerceAtLeast(0)
                preferences[ANALYTICS_VISIT_DATE] = analyticsVisitDateVal.coerceAtLeast(0L)
            } else {
                preferences.remove(ANALYTICS_VISIT_COUNT)
                preferences.remove(ANALYTICS_VISIT_DATE)
            }
        }
    }
}
