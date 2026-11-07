package com.smokingtracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class NotificationSettingsSnapshot(
    val enabled: Boolean = false,
    val lowPriority: Boolean = true,
    val showTimer: Boolean = true,
    val showProgress: Boolean = true,
    val showAddButton: Boolean = true,
    val showResistButton: Boolean = false
)

class NotificationPreferences(private val context: Context) {
    private val dataFlow: Flow<Preferences> = context.dataStore.safeData

    companion object {
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val NOTIFICATION_LOW_PRIORITY = booleanPreferencesKey("notification_low_priority")
        val NOTIFICATION_SHOW_TIMER = booleanPreferencesKey("notification_show_timer")
        val NOTIFICATION_SHOW_PROGRESS = booleanPreferencesKey("notification_show_progress")
        val NOTIFICATION_SHOW_ADD_BUTTON = booleanPreferencesKey("notification_show_add_button")
        val NOTIFICATION_SHOW_RESIST_BUTTON = booleanPreferencesKey("notification_show_resist_button")

        // Compatibility aliases; canonical ownership moved to UserPreferences (BUG-22)
        val WIDGET_SHOW_RESIST_BUTTON = UserPreferences.WIDGET_SHOW_RESIST_BUTTON
        val SHOW_LIMIT_ON_GRAPH = UserPreferences.SHOW_LIMIT_ON_GRAPH
    }

    val ongoingNotificationEnabled: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[NOTIFICATION_ENABLED] ?: false
    }.distinctUntilChanged()

    val notificationLowPriority: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[NOTIFICATION_LOW_PRIORITY] ?: true
    }.distinctUntilChanged()

    val notificationShowTimer: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[NOTIFICATION_SHOW_TIMER] ?: true
    }.distinctUntilChanged()

    val notificationShowProgress: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[NOTIFICATION_SHOW_PROGRESS] ?: true
    }.distinctUntilChanged()

    val notificationShowAddButton: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[NOTIFICATION_SHOW_ADD_BUTTON] ?: true
    }.distinctUntilChanged()

    val notificationShowResistButton: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[NOTIFICATION_SHOW_RESIST_BUTTON] ?: false
    }.distinctUntilChanged()

    val widgetShowResistButton: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[UserPreferences.WIDGET_SHOW_RESIST_BUTTON] ?: true
    }.distinctUntilChanged()

    val showLimitOnGraph: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[UserPreferences.SHOW_LIMIT_ON_GRAPH] ?: true
    }.distinctUntilChanged()

    suspend fun getSnapshot(): NotificationSettingsSnapshot {
        val preferences = dataFlow.first()
        return NotificationSettingsSnapshot(
            enabled = preferences[NOTIFICATION_ENABLED] ?: false,
            lowPriority = preferences[NOTIFICATION_LOW_PRIORITY] ?: true,
            showTimer = preferences[NOTIFICATION_SHOW_TIMER] ?: true,
            showProgress = preferences[NOTIFICATION_SHOW_PROGRESS] ?: true,
            showAddButton = preferences[NOTIFICATION_SHOW_ADD_BUTTON] ?: true,
            showResistButton = preferences[NOTIFICATION_SHOW_RESIST_BUTTON] ?: false
        )
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
            preferences[UserPreferences.WIDGET_SHOW_RESIST_BUTTON] = show
        }
    }

    suspend fun setShowLimitOnGraph(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[UserPreferences.SHOW_LIMIT_ON_GRAPH] = enabled
        }
    }

    suspend fun resetNotificationPreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(NOTIFICATION_ENABLED)
            preferences.remove(NOTIFICATION_LOW_PRIORITY)
            preferences.remove(NOTIFICATION_SHOW_TIMER)
            preferences.remove(NOTIFICATION_SHOW_PROGRESS)
            preferences.remove(NOTIFICATION_SHOW_ADD_BUTTON)
            preferences.remove(NOTIFICATION_SHOW_RESIST_BUTTON)
            preferences.remove(UserPreferences.WIDGET_SHOW_RESIST_BUTTON)
            preferences.remove(UserPreferences.SHOW_LIMIT_ON_GRAPH)
        }
    }

    suspend fun restoreNotificationPreferences(
        notificationEnabledVal: Boolean = false,
        notificationLowPriorityVal: Boolean = true,
        notificationShowTimerVal: Boolean = true,
        notificationShowProgressVal: Boolean = true,
        notificationShowAddButtonVal: Boolean = true,
        notificationShowResistButtonVal: Boolean = false,
        widgetShowResistButtonVal: Boolean? = null,
        showLimitOnGraphVal: Boolean? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = notificationEnabledVal
            preferences[NOTIFICATION_LOW_PRIORITY] = notificationLowPriorityVal
            preferences[NOTIFICATION_SHOW_TIMER] = notificationShowTimerVal
            preferences[NOTIFICATION_SHOW_PROGRESS] = notificationShowProgressVal
            preferences[NOTIFICATION_SHOW_ADD_BUTTON] = notificationShowAddButtonVal
            preferences[NOTIFICATION_SHOW_RESIST_BUTTON] = notificationShowResistButtonVal
            if (widgetShowResistButtonVal != null) {
                preferences[UserPreferences.WIDGET_SHOW_RESIST_BUTTON] = widgetShowResistButtonVal
            }
            if (showLimitOnGraphVal != null) {
                preferences[UserPreferences.SHOW_LIMIT_ON_GRAPH] = showLimitOnGraphVal
            }
        }
    }
}
