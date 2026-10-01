package com.smokingtracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smokingtracker.data.TriggerItem
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.preferences.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Locale

class TriggerRepository(
    private val context: Context
) {
    private val gson = Gson()

    companion object {
        val CUSTOM_TRIGGERS = stringPreferencesKey("custom_triggers_list")
        val DISABLED_DEFAULT_TRIGGERS = stringSetPreferencesKey("disabled_default_triggers_set")
    }

    val customTriggers: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val json = preferences[CUSTOM_TRIGGERS] ?: "[]"
        val listType = object : TypeToken<List<String>>() {}.type
        gson.fromJson(json, listType) ?: emptyList()
    }

    val disabledDefaultTriggers: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[DISABLED_DEFAULT_TRIGGERS] ?: emptySet()
    }

    val activeTriggers: Flow<List<TriggerItem>> = combine(
        customTriggers,
        disabledDefaultTriggers
    ) { customList, disabledDefaults ->
        val builtIn = TriggerType.allEntries()
            .filter { !disabledDefaults.contains(it.key) }
            .map { TriggerItem.fromBuiltIn(it, isEnabled = true) }
        val custom = customList.map { TriggerItem.fromCustom(it) }
        builtIn + custom
    }

    fun formatTriggerName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return ""
        return trimmed.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    suspend fun addCustomTrigger(name: String): String? {
        val formatted = formatTriggerName(name)
        if (formatted.isBlank()) return null
        var added: String? = null
        context.dataStore.edit { preferences ->
            val json = preferences[CUSTOM_TRIGGERS] ?: "[]"
            val listType = object : TypeToken<List<String>>() {}.type
            val current: MutableList<String> = gson.fromJson(json, listType) ?: mutableListOf()
            val exists = current.any { it.equals(formatted, ignoreCase = true) }
            if (!exists) {
                current.add(formatted)
                preferences[CUSTOM_TRIGGERS] = gson.toJson(current)
                added = formatted
            }
        }
        return added
    }

    suspend fun removeCustomTrigger(name: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[CUSTOM_TRIGGERS] ?: "[]"
            val listType = object : TypeToken<List<String>>() {}.type
            val current: MutableList<String> = gson.fromJson(json, listType) ?: mutableListOf()
            current.removeAll { it.equals(name, ignoreCase = true) }
            preferences[CUSTOM_TRIGGERS] = gson.toJson(current)
        }
    }

    suspend fun toggleDefaultTrigger(key: String, isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[DISABLED_DEFAULT_TRIGGERS]?.toMutableSet() ?: mutableSetOf()
            if (isEnabled) {
                current.remove(key)
            } else {
                current.add(key)
            }
            preferences[DISABLED_DEFAULT_TRIGGERS] = current
        }
    }

    suspend fun restoreTriggers(customTriggersVal: List<String>, disabledDefaultTriggersVal: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_TRIGGERS] = gson.toJson(customTriggersVal)
            preferences[DISABLED_DEFAULT_TRIGGERS] = disabledDefaultTriggersVal
        }
    }

    suspend fun clearTriggers() {
        context.dataStore.edit { preferences ->
            preferences.remove(CUSTOM_TRIGGERS)
            preferences.remove(DISABLED_DEFAULT_TRIGGERS)
        }
    }
}
