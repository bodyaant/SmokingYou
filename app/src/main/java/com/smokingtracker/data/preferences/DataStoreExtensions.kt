package com.smokingtracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import java.lang.reflect.Type

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "smoking_tracker_prefs",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

val DataStore<Preferences>.safeData: Flow<Preferences>
    get() = data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }

internal fun <T> Gson.fromJsonOrNull(json: String?, type: Type): T? {
    if (json.isNullOrBlank()) return null
    return try {
        fromJson<T>(json, type)
    } catch (_: JsonParseException) {
        null
    } catch (_: IllegalStateException) {
        null
    } catch (_: NumberFormatException) {
        null
    } catch (_: Exception) {
        null
    }
}
