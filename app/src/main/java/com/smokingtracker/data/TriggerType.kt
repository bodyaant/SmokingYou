package com.smokingtracker.data

import com.smokingtracker.R

enum class TriggerType(val key: String, val labelResId: Int) {
    STRESS("Stress", R.string.trigger_stress),
    BOREDOM("Boredom", R.string.trigger_boredom),
    SOCIAL("Social", R.string.trigger_social),
    ROUTINE("Routine", R.string.trigger_routine),
    FOOD_COFFEE("Food/Coffee", R.string.trigger_food_coffee),
    ALCOHOL("Alcohol", R.string.trigger_alcohol);

    companion object {
        fun fromKey(key: String): TriggerType? = entries.find { it.key == key }
        fun allKeys(): List<String> = entries.map { it.key }
        fun allEntries(): List<TriggerType> = entries.toList()
    }
}

data class TriggerItem(
    val key: String,
    val labelResId: Int? = null,
    val customName: String? = null,
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true
) {
    companion object {
        fun fromBuiltIn(type: TriggerType, isEnabled: Boolean = true): TriggerItem {
            return TriggerItem(
                key = type.key,
                labelResId = type.labelResId,
                customName = null,
                isCustom = false,
                isEnabled = isEnabled
            )
        }

        fun fromCustom(name: String): TriggerItem {
            return TriggerItem(
                key = name,
                labelResId = null,
                customName = name,
                isCustom = true,
                isEnabled = true
            )
        }
    }
}
