package com.smokingtracker.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smokingtracker.data.TriggerItem
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.preferences.dataStore
import com.smokingtracker.data.preferences.fromJsonOrNull
import com.smokingtracker.data.preferences.safeData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

interface TriggerRepository {
    val customTriggers: Flow<List<String>>
    val disabledDefaultTriggers: Flow<Set<String>>
    val activeTriggers: Flow<List<TriggerItem>>

    fun formatTriggerName(name: String): String
    suspend fun addCustomTrigger(name: String): String?
    suspend fun removeCustomTrigger(name: String): Boolean
    suspend fun toggleDefaultTrigger(key: String, isEnabled: Boolean): Boolean
    suspend fun restoreTriggers(
        customTriggersVal: List<String>,
        disabledDefaultTriggersVal: Set<String>
    ): Boolean
    suspend fun clearTriggers(): Boolean

    companion object {
        val CUSTOM_TRIGGERS = stringPreferencesKey("custom_triggers_list")
        val DISABLED_DEFAULT_TRIGGERS = stringSetPreferencesKey("disabled_default_triggers_set")

        operator fun invoke(
            dataStore: DataStore<Preferences>,
            gson: Gson = Gson(),
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
            defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
        ): TriggerRepository = DataStoreTriggerRepository(
            dataStore = dataStore,
            gson = gson,
            ioDispatcher = ioDispatcher,
            defaultDispatcher = defaultDispatcher
        )

        operator fun invoke(
            context: Context,
            gson: Gson = Gson(),
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
            defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
        ): TriggerRepository = DataStoreTriggerRepository(
            dataStore = context.dataStore,
            gson = gson,
            ioDispatcher = ioDispatcher,
            defaultDispatcher = defaultDispatcher
        )
    }
}

class DataStoreTriggerRepository(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson = Gson(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : TriggerRepository {

    constructor(context: Context, gson: Gson = Gson()) : this(dataStore = context.dataStore, gson = gson)

    private val stringListType = object : TypeToken<List<String?>>() {}.type
    private val dataFlow: Flow<Preferences> = dataStore.safeData

    override val customTriggers: Flow<List<String>> = dataFlow.map { preferences ->
        val json = preferences[TriggerRepository.CUSTOM_TRIGGERS] ?: "[]"
        sanitizeCustomTriggersList(gson.fromJsonOrNull<List<String?>>(json, stringListType))
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override val disabledDefaultTriggers: Flow<Set<String>> = dataFlow.map { preferences ->
        sanitizeDisabledDefaultsSet(preferences[TriggerRepository.DISABLED_DEFAULT_TRIGGERS])
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override val activeTriggers: Flow<List<TriggerItem>> = combine(
        customTriggers,
        disabledDefaultTriggers
    ) { customList, disabledDefaults ->
        val builtIn = TriggerType.allEntries()
            .filter { !disabledDefaults.contains(it.key) }
            .map { TriggerItem.fromBuiltIn(it, isEnabled = true) }
        val custom = customList
            .filter { !TriggerType.isBuiltIn(it) }
            .mapNotNull { TriggerItem.fromCustomOrNull(it) }
        (builtIn + custom).distinctBy { it.key.lowercase(Locale.ROOT) }
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override fun formatTriggerName(name: String): String {
        val normalized = TriggerItem.normalizeCustomName(name)
        if (normalized.isEmpty()) return ""
        return normalized.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    override suspend fun addCustomTrigger(name: String): String? {
        val formatted = formatTriggerName(name)
        if (formatted.isBlank() || TriggerType.isBuiltIn(formatted)) return null
        return withContext(ioDispatcher) {
            var added: String? = null
            try {
                dataStore.edit { preferences ->
                    val json = preferences[TriggerRepository.CUSTOM_TRIGGERS] ?: "[]"
                    val current = sanitizeCustomTriggersList(
                        gson.fromJsonOrNull<List<String?>>(json, stringListType)
                    ).toMutableList()
                    val exists = current.any { it.equals(formatted, ignoreCase = true) }
                    if (!exists) {
                        current.add(formatted)
                        preferences[TriggerRepository.CUSTOM_TRIGGERS] = gson.toJson(current)
                        added = formatted
                    }
                }
                added
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "Failed to add custom trigger '$formatted'", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error adding custom trigger '$formatted'", e)
                null
            }
        }
    }

    override suspend fun removeCustomTrigger(name: String): Boolean {
        val formatted = formatTriggerName(name)
        if (formatted.isBlank()) return false
        return withContext(ioDispatcher) {
            var removed = false
            try {
                dataStore.edit { preferences ->
                    val json = preferences[TriggerRepository.CUSTOM_TRIGGERS] ?: "[]"
                    val current = sanitizeCustomTriggersList(
                        gson.fromJsonOrNull<List<String?>>(json, stringListType)
                    ).toMutableList()
                    removed = current.removeAll {
                        formatTriggerName(it).equals(formatted, ignoreCase = true) ||
                            it.trim().equals(formatted, ignoreCase = true)
                    }
                    if (removed) {
                        preferences[TriggerRepository.CUSTOM_TRIGGERS] = gson.toJson(current)
                    }
                }
                removed
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "Failed to remove custom trigger '$formatted'", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error removing custom trigger '$formatted'", e)
                false
            }
        }
    }

    override suspend fun toggleDefaultTrigger(key: String, isEnabled: Boolean): Boolean {
        val triggerType = TriggerType.fromKey(key) ?: return false
        return withContext(ioDispatcher) {
            try {
                dataStore.edit { preferences ->
                    val current = sanitizeDisabledDefaultsSet(
                        preferences[TriggerRepository.DISABLED_DEFAULT_TRIGGERS]
                    ).toMutableSet()
                    if (isEnabled) {
                        current.remove(triggerType.key)
                    } else {
                        current.add(triggerType.key)
                    }
                    preferences[TriggerRepository.DISABLED_DEFAULT_TRIGGERS] = current
                }
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "Failed to toggle default trigger '${triggerType.key}'", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error toggling default trigger '${triggerType.key}'", e)
                false
            }
        }
    }

    override suspend fun restoreTriggers(
        customTriggersVal: List<String>,
        disabledDefaultTriggersVal: Set<String>
    ): Boolean = withContext(ioDispatcher) {
        val sanitizedCustom = sanitizeCustomTriggersList(customTriggersVal)
        val sanitizedDisabled = sanitizeDisabledDefaultsSet(disabledDefaultTriggersVal)
        try {
            dataStore.edit { preferences ->
                preferences[TriggerRepository.CUSTOM_TRIGGERS] = gson.toJson(sanitizedCustom)
                preferences[TriggerRepository.DISABLED_DEFAULT_TRIGGERS] = sanitizedDisabled
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Failed to restore triggers", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error restoring triggers", e)
            false
        }
    }

    override suspend fun clearTriggers(): Boolean = withContext(ioDispatcher) {
        try {
            dataStore.edit { preferences ->
                preferences.remove(TriggerRepository.CUSTOM_TRIGGERS)
                preferences.remove(TriggerRepository.DISABLED_DEFAULT_TRIGGERS)
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Failed to clear triggers", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error clearing triggers", e)
            false
        }
    }

    private fun sanitizeCustomTriggersList(raw: List<String?>?): List<String> {
        if (raw.isNullOrEmpty()) return emptyList()
        return raw.asSequence()
            .mapNotNull { it?.let(::formatTriggerName)?.takeIf(String::isNotBlank) }
            .filter { !TriggerType.isBuiltIn(it) }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .toList()
    }

    private fun sanitizeDisabledDefaultsSet(raw: Set<String?>?): Set<String> {
        if (raw.isNullOrEmpty()) return emptySet()
        return raw.mapNotNull { TriggerType.fromKey(it)?.key }.toSet()
    }

    companion object {
        private const val TAG = "TriggerRepository"
    }
}
