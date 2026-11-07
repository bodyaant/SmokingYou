package com.smokingtracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class UserPreferences(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    private val dataFlow: Flow<Preferences> = context.dataStore.safeData
    private val stringListType = object : TypeToken<List<String?>>() {}.type

    companion object {
        const val DEFAULT_PACK_SIZE = 20
        const val DEFAULT_TAPERING_INTERVAL_DAYS = 7
        const val DEFAULT_CURRENCY = "USD"

        val PACK_PRICE = floatPreferencesKey("pack_price")
        val PACK_SIZE = intPreferencesKey("pack_size")
        val CURRENCY = stringPreferencesKey("currency")
        val DAILY_LIMIT = intPreferencesKey("daily_limit")
        val SAVINGS_GOAL_TITLE = stringPreferencesKey("savings_goal_title")
        val SAVINGS_GOAL_AMOUNT = floatPreferencesKey("savings_goal_amount")
        val HAS_HISTORICAL_BASELINE = booleanPreferencesKey("has_historical_baseline")
        val HISTORICAL_START_DATE = longPreferencesKey("historical_start_date")
        val HISTORICAL_DAILY_AVG = intPreferencesKey("historical_daily_avg")
        val HISTORICAL_PACK_PRICE = floatPreferencesKey("historical_pack_price")
        val HISTORICAL_PACK_SIZE = intPreferencesKey("historical_pack_size")
        val HISTORICAL_TRIGGER_PRIORITIES = stringPreferencesKey("historical_trigger_priorities")
        val TAPERING_PLAN_ENABLED = booleanPreferencesKey("tapering_plan_enabled")
        val TAPERING_INTERVAL_DAYS = intPreferencesKey("tapering_interval_days")
        val LAST_TAPERING_CHECKIN_DATE = longPreferencesKey("last_tapering_checkin_date")
        val WIDGET_SHOW_RESIST_BUTTON = booleanPreferencesKey("widget_show_resist_button")
        val SHOW_LIMIT_ON_GRAPH = booleanPreferencesKey("show_limit_on_graph")
    }

    val packPrice: Flow<Float> = dataFlow.map { preferences ->
        val price = preferences[PACK_PRICE] ?: 0.0f
        if (price.isFinite() && price >= 0f) price else 0.0f
    }.distinctUntilChanged()

    val packSize: Flow<Int> = dataFlow.map { preferences ->
        (preferences[PACK_SIZE] ?: DEFAULT_PACK_SIZE).takeIf { it > 0 } ?: DEFAULT_PACK_SIZE
    }.distinctUntilChanged()

    val currency: Flow<String> = dataFlow.map { preferences ->
        preferences[CURRENCY]?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_CURRENCY
    }.distinctUntilChanged()

    val dailyLimit: Flow<Int> = dataFlow.map { preferences ->
        (preferences[DAILY_LIMIT] ?: 0).coerceAtLeast(0)
    }.distinctUntilChanged()

    val savingsGoalTitle: Flow<String> = dataFlow.map { preferences ->
        preferences[SAVINGS_GOAL_TITLE] ?: ""
    }.distinctUntilChanged()

    val savingsGoalAmount: Flow<Float> = dataFlow.map { preferences ->
        val amount = preferences[SAVINGS_GOAL_AMOUNT] ?: 0f
        if (amount.isFinite() && amount >= 0f) amount else 0f
    }.distinctUntilChanged()

    val hasHistoricalBaseline: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[HAS_HISTORICAL_BASELINE] ?: false
    }.distinctUntilChanged()

    val historicalStartDate: Flow<Long> = dataFlow.map { preferences ->
        (preferences[HISTORICAL_START_DATE] ?: 0L).coerceAtLeast(0L)
    }.distinctUntilChanged()

    val historicalDailyAvg: Flow<Int> = dataFlow.map { preferences ->
        (preferences[HISTORICAL_DAILY_AVG] ?: 0).coerceAtLeast(0)
    }.distinctUntilChanged()

    val historicalPackPrice: Flow<Float> = dataFlow.map { preferences ->
        val price = preferences[HISTORICAL_PACK_PRICE] ?: 0f
        if (price.isFinite() && price >= 0f) price else 0f
    }.distinctUntilChanged()

    val historicalPackSize: Flow<Int> = dataFlow.map { preferences ->
        (preferences[HISTORICAL_PACK_SIZE] ?: DEFAULT_PACK_SIZE).takeIf { it > 0 } ?: DEFAULT_PACK_SIZE
    }.distinctUntilChanged()

    val historicalTriggerPriorities: Flow<List<String>> = dataFlow.map { preferences ->
        val json = preferences[HISTORICAL_TRIGGER_PRIORITIES] ?: "[]"
        gson.fromJsonOrNull<List<String?>>(json, stringListType)
            ?.mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
            ?: emptyList()
    }.distinctUntilChanged()

    val taperingPlanEnabled: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[TAPERING_PLAN_ENABLED] ?: false
    }.distinctUntilChanged()

    val taperingIntervalDays: Flow<Int> = dataFlow.map { preferences ->
        (preferences[TAPERING_INTERVAL_DAYS] ?: DEFAULT_TAPERING_INTERVAL_DAYS).takeIf { it > 0 }
            ?: DEFAULT_TAPERING_INTERVAL_DAYS
    }.distinctUntilChanged()

    val lastTaperingCheckinDate: Flow<Long> = dataFlow.map { preferences ->
        (preferences[LAST_TAPERING_CHECKIN_DATE] ?: 0L).coerceAtLeast(0L)
    }.distinctUntilChanged()

    val widgetShowResistButton: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[WIDGET_SHOW_RESIST_BUTTON] ?: true
    }.distinctUntilChanged()

    val showLimitOnGraph: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[SHOW_LIMIT_ON_GRAPH] ?: true
    }.distinctUntilChanged()

    suspend fun setDailyLimit(limit: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_LIMIT] = limit.coerceAtLeast(0)
        }
    }

    suspend fun savePackDetails(price: Float, size: Int, curr: String) {
        context.dataStore.edit { preferences ->
            preferences[PACK_PRICE] = if (price.isFinite() && price >= 0f) price else 0.0f
            preferences[PACK_SIZE] = size.takeIf { it > 0 } ?: DEFAULT_PACK_SIZE
            preferences[CURRENCY] = curr.trim().ifEmpty { DEFAULT_CURRENCY }
        }
    }

    suspend fun saveSavingsGoal(title: String, amount: Float) {
        context.dataStore.edit { preferences ->
            preferences[SAVINGS_GOAL_TITLE] = title.trim()
            preferences[SAVINGS_GOAL_AMOUNT] = if (amount.isFinite() && amount >= 0f) amount else 0f
        }
    }

    suspend fun saveHistoricalBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        triggerPriorities: List<String>
    ) {
        val sanitizedTriggers = triggerPriorities.mapNotNull { it.trim().takeIf(String::isNotEmpty) }
        context.dataStore.edit { preferences ->
            preferences[HAS_HISTORICAL_BASELINE] = true
            preferences[HISTORICAL_START_DATE] = startDate.coerceAtLeast(0L)
            preferences[HISTORICAL_DAILY_AVG] = dailyAvg.coerceAtLeast(0)
            preferences[HISTORICAL_PACK_PRICE] = if (packPrice.isFinite() && packPrice >= 0f) packPrice else 0f
            preferences[HISTORICAL_PACK_SIZE] = packSize.takeIf { it > 0 } ?: DEFAULT_PACK_SIZE
            preferences[HISTORICAL_TRIGGER_PRIORITIES] = gson.toJson(sanitizedTriggers)
        }
    }

    suspend fun clearHistoricalBaseline() {
        context.dataStore.edit { preferences ->
            preferences[HAS_HISTORICAL_BASELINE] = false
            preferences.remove(HISTORICAL_START_DATE)
            preferences.remove(HISTORICAL_DAILY_AVG)
            preferences.remove(HISTORICAL_PACK_PRICE)
            preferences.remove(HISTORICAL_PACK_SIZE)
            preferences.remove(HISTORICAL_TRIGGER_PRIORITIES)
        }
    }

    suspend fun setTaperingPlanEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TAPERING_PLAN_ENABLED] = enabled
        }
    }

    suspend fun setTaperingIntervalDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[TAPERING_INTERVAL_DAYS] = days.takeIf { it > 0 } ?: DEFAULT_TAPERING_INTERVAL_DAYS
        }
    }

    suspend fun updateLastTaperingCheckinDate(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_TAPERING_CHECKIN_DATE] = timestamp.coerceAtLeast(0L)
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

    suspend fun resetUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(DAILY_LIMIT)
            preferences.remove(TAPERING_PLAN_ENABLED)
            preferences.remove(TAPERING_INTERVAL_DAYS)
            preferences.remove(LAST_TAPERING_CHECKIN_DATE)
            preferences.remove(HAS_HISTORICAL_BASELINE)
            preferences.remove(HISTORICAL_START_DATE)
            preferences.remove(HISTORICAL_DAILY_AVG)
            preferences.remove(HISTORICAL_PACK_PRICE)
            preferences.remove(HISTORICAL_PACK_SIZE)
            preferences.remove(HISTORICAL_TRIGGER_PRIORITIES)
            preferences.remove(SAVINGS_GOAL_TITLE)
            preferences.remove(SAVINGS_GOAL_AMOUNT)
            preferences.remove(PACK_PRICE)
            preferences.remove(PACK_SIZE)
            preferences.remove(CURRENCY)
            preferences.remove(WIDGET_SHOW_RESIST_BUTTON)
            preferences.remove(SHOW_LIMIT_ON_GRAPH)
        }
    }

    suspend fun restoreUserPreferences(
        limit: Int,
        price: Float,
        size: Int,
        curr: String,
        taperingPlanEnabledVal: Boolean = false,
        taperingIntervalDaysVal: Int = DEFAULT_TAPERING_INTERVAL_DAYS,
        lastTaperingCheckinDateVal: Long = 0L,
        hasHistoricalBaselineVal: Boolean = false,
        historicalStartDateVal: Long = 0L,
        historicalDailyAvgVal: Int = 0,
        historicalPackPriceVal: Float = 0f,
        historicalPackSizeVal: Int = DEFAULT_PACK_SIZE,
        historicalTriggerPrioritiesVal: List<String> = emptyList(),
        savingsGoalTitleVal: String = "",
        savingsGoalAmountVal: Float = 0f,
        widgetShowResistButtonVal: Boolean = true,
        showLimitOnGraphVal: Boolean = true
    ) {
        val sanitizedTriggers = historicalTriggerPrioritiesVal.mapNotNull { it.trim().takeIf(String::isNotEmpty) }
        context.dataStore.edit { preferences ->
            preferences[DAILY_LIMIT] = limit.coerceAtLeast(0)
            preferences[PACK_PRICE] = if (price.isFinite() && price >= 0f) price else 0.0f
            preferences[PACK_SIZE] = size.takeIf { it > 0 } ?: DEFAULT_PACK_SIZE
            preferences[CURRENCY] = curr.trim().ifEmpty { DEFAULT_CURRENCY }
            preferences[TAPERING_PLAN_ENABLED] = taperingPlanEnabledVal
            preferences[TAPERING_INTERVAL_DAYS] = taperingIntervalDaysVal.takeIf { it > 0 } ?: DEFAULT_TAPERING_INTERVAL_DAYS
            preferences[LAST_TAPERING_CHECKIN_DATE] = lastTaperingCheckinDateVal.coerceAtLeast(0L)
            preferences[HAS_HISTORICAL_BASELINE] = hasHistoricalBaselineVal
            preferences[HISTORICAL_START_DATE] = historicalStartDateVal.coerceAtLeast(0L)
            preferences[HISTORICAL_DAILY_AVG] = historicalDailyAvgVal.coerceAtLeast(0)
            preferences[HISTORICAL_PACK_PRICE] = if (historicalPackPriceVal.isFinite() && historicalPackPriceVal >= 0f) historicalPackPriceVal else 0f
            preferences[HISTORICAL_PACK_SIZE] = historicalPackSizeVal.takeIf { it > 0 } ?: DEFAULT_PACK_SIZE
            preferences[HISTORICAL_TRIGGER_PRIORITIES] = gson.toJson(sanitizedTriggers)
            preferences[SAVINGS_GOAL_TITLE] = savingsGoalTitleVal.trim()
            preferences[SAVINGS_GOAL_AMOUNT] = if (savingsGoalAmountVal.isFinite() && savingsGoalAmountVal >= 0f) savingsGoalAmountVal else 0f
            preferences[WIDGET_SHOW_RESIST_BUTTON] = widgetShowResistButtonVal
            preferences[SHOW_LIMIT_ON_GRAPH] = showLimitOnGraphVal
        }
    }
}
