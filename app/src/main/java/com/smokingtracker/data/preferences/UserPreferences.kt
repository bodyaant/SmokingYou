package com.smokingtracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {
    private val gson = Gson()

    companion object {
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

    val dailyLimit: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_LIMIT] ?: 0
    }

    val savingsGoalTitle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SAVINGS_GOAL_TITLE] ?: ""
    }

    val savingsGoalAmount: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[SAVINGS_GOAL_AMOUNT] ?: 0f
    }

    val hasHistoricalBaseline: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_HISTORICAL_BASELINE] ?: false
    }

    val historicalStartDate: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[HISTORICAL_START_DATE] ?: 0L
    }

    val historicalDailyAvg: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HISTORICAL_DAILY_AVG] ?: 0
    }

    val historicalPackPrice: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[HISTORICAL_PACK_PRICE] ?: 0f
    }

    val historicalPackSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HISTORICAL_PACK_SIZE] ?: 20
    }

    val historicalTriggerPriorities: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val json = preferences[HISTORICAL_TRIGGER_PRIORITIES] ?: "[]"
        val listType = object : TypeToken<List<String>>() {}.type
        gson.fromJson(json, listType) ?: emptyList()
    }

    val taperingPlanEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TAPERING_PLAN_ENABLED] ?: false
    }

    val taperingIntervalDays: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[TAPERING_INTERVAL_DAYS] ?: 7
    }

    val lastTaperingCheckinDate: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_TAPERING_CHECKIN_DATE] ?: 0L
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

    suspend fun saveSavingsGoal(title: String, amount: Float) {
        context.dataStore.edit { preferences ->
            preferences[SAVINGS_GOAL_TITLE] = title
            preferences[SAVINGS_GOAL_AMOUNT] = amount
        }
    }

    suspend fun saveHistoricalBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        triggerPriorities: List<String>
    ) {
        context.dataStore.edit { preferences ->
            preferences[HAS_HISTORICAL_BASELINE] = true
            preferences[HISTORICAL_START_DATE] = startDate
            preferences[HISTORICAL_DAILY_AVG] = dailyAvg
            preferences[HISTORICAL_PACK_PRICE] = packPrice
            preferences[HISTORICAL_PACK_SIZE] = packSize
            preferences[HISTORICAL_TRIGGER_PRIORITIES] = gson.toJson(triggerPriorities)
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
            preferences[TAPERING_INTERVAL_DAYS] = days
        }
    }

    suspend fun updateLastTaperingCheckinDate(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_TAPERING_CHECKIN_DATE] = timestamp
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
        }
    }

    suspend fun restoreUserPreferences(
        limit: Int,
        price: Float,
        size: Int,
        curr: String,
        taperingPlanEnabledVal: Boolean = false,
        taperingIntervalDaysVal: Int = 7,
        lastTaperingCheckinDateVal: Long = 0L,
        hasHistoricalBaselineVal: Boolean = false,
        historicalStartDateVal: Long = 0L,
        historicalDailyAvgVal: Int = 0,
        historicalPackPriceVal: Float = 0f,
        historicalPackSizeVal: Int = 20,
        historicalTriggerPrioritiesVal: List<String> = emptyList()
    ) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_LIMIT] = limit
            preferences[PACK_PRICE] = price
            preferences[PACK_SIZE] = size
            preferences[CURRENCY] = curr
            preferences[TAPERING_PLAN_ENABLED] = taperingPlanEnabledVal
            preferences[TAPERING_INTERVAL_DAYS] = taperingIntervalDaysVal
            preferences[LAST_TAPERING_CHECKIN_DATE] = lastTaperingCheckinDateVal
            preferences[HAS_HISTORICAL_BASELINE] = hasHistoricalBaselineVal
            preferences[HISTORICAL_START_DATE] = historicalStartDateVal
            preferences[HISTORICAL_DAILY_AVG] = historicalDailyAvgVal
            preferences[HISTORICAL_PACK_PRICE] = historicalPackPriceVal
            preferences[HISTORICAL_PACK_SIZE] = historicalPackSizeVal
            preferences[HISTORICAL_TRIGGER_PRIORITIES] = gson.toJson(historicalTriggerPrioritiesVal)
        }
    }
}
