package com.smokingtracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationPreferences(private val context: Context) {

    companion object {
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val NOTIFICATION_LOW_PRIORITY = booleanPreferencesKey("notification_low_priority")
        val NOTIFICATION_SHOW_TIMER = booleanPreferencesKey("notification_show_timer")
        val NOTIFICATION_SHOW_PROGRESS = booleanPreferencesKey("notification_show_progress")
        val NOTIFICATION_SHOW_ADD_BUTTON = booleanPreferencesKey("notification_show_add_button")
        val NOTIFICATION_SHOW_RESIST_BUTTON = booleanPreferencesKey("notification_show_resist_button")
        val WIDGET_SHOW_RESIST_BUTTON = booleanPreferencesKey("widget_show_resist_button")
        val SHOW_LIMIT_ON_GRAPH = booleanPreferencesKey("show_limit_on_graph")
    }

    val ongoingNotificationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ENABLED] ?: false
    }

    val notificationLowPriority: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_LOW_PRIORITY] ?: true
    }

    val notificationShowTimer: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_SHOW_TIMER] ?: true
    }

    val notificationShowProgress: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_SHOW_PROGRESS] ?: true
    }

    val notificationShowAddButton: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_SHOW_ADD_BUTTON] ?: true
    }

    val notificationShowResistButton: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_SHOW_RESIST_BUTTON] ?: false
    }

    val widgetShowResistButton: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[WIDGET_SHOW_RESIST_BUTTON] ?: true
    }

    val showLimitOnGraph: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_LIMIT_ON_GRAPH] ?: true
    }

    suspend fun saveOngoingNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun saveNotificationLowPriority(lowPriority: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_LOW_PRIORITY] = lowPriority
        }
    }

    suspend fun saveNotificationShowTimer(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_SHOW_TIMER] = show
        }
    }

    suspend fun saveNotificationShowProgress(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_SHOW_PROGRESS] = show
        }
    }

    suspend fun saveNotificationShowAddButton(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_SHOW_ADD_BUTTON] = show
        }
    }

    suspend fun saveNotificationShowResistButton(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_SHOW_RESIST_BUTTON] = show
        }
    }

    suspend fun saveWidgetShowResistButton(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WIDGET_SHOW_RESIST_BUTTON] = show
        }
    }

    suspend fun setShowLimitOnGraph(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_LIMIT_ON_GRAPH] = enabled
        }
    }

    suspend fun resetNotificationPreferences() {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = false
        }
    }

    suspend fun restoreNotificationPreferences(
        notificationEnabledVal: Boolean = false,
        notificationLowPriorityVal: Boolean = true,
        notificationShowTimerVal: Boolean = true,
        notificationShowProgressVal: Boolean = true,
        notificationShowAddButtonVal: Boolean = true,
        notificationShowResistButtonVal: Boolean = false
    ) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = notificationEnabledVal
            preferences[NOTIFICATION_LOW_PRIORITY] = notificationLowPriorityVal
            preferences[NOTIFICATION_SHOW_TIMER] = notificationShowTimerVal
            preferences[NOTIFICATION_SHOW_PROGRESS] = notificationShowProgressVal
            preferences[NOTIFICATION_SHOW_ADD_BUTTON] = notificationShowAddButtonVal
            preferences[NOTIFICATION_SHOW_RESIST_BUTTON] = notificationShowResistButtonVal
        }
    }
}
