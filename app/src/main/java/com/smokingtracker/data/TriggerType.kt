package com.smokingtracker.data

import com.smokingtracker.R

enum class TriggerType(val key: String, val labelResId: Int) {
    STRESS("Stress", R.string.trigger_stress),
    BOREDOM("Boredom", R.string.trigger_boredom),
    SOCIAL("Social", R.string.trigger_social),
    ROUTINE("Routine", R.string.trigger_routine),
    FOOD_COFFEE("Food/Coffee", R.string.trigger_food_coffee);

    companion object {
        fun fromKey(key: String): TriggerType? = entries.find { it.key == key }
        fun allKeys(): List<String> = entries.map { it.key }
        fun allEntries(): List<TriggerType> = entries.toList()
    }
}
