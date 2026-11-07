package com.smokingtracker.data

import androidx.annotation.Keep
import com.smokingtracker.R

@Keep
enum class TriggerType(val key: String, val labelResId: Int) {
    STRESS("Stress", R.string.trigger_stress),
    BOREDOM("Boredom", R.string.trigger_boredom),
    SOCIAL("Social", R.string.trigger_social),
    ROUTINE("Routine", R.string.trigger_routine),
    FOOD_COFFEE("Food/Coffee", R.string.trigger_food_coffee),
    ALCOHOL("Alcohol", R.string.trigger_alcohol);

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")

        fun fromKey(key: String?): TriggerType? {
            if (key.isNullOrBlank()) return null
            val normalized = key.trim().replace(WHITESPACE_REGEX, " ")
            return entries.find {
                it.key.equals(normalized, ignoreCase = true) || it.name.equals(normalized, ignoreCase = true)
            }
        }

        fun isBuiltIn(nameOrKey: String?): Boolean = fromKey(nameOrKey) != null

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
        const val CUSTOM_KEY_PREFIX = "custom:"
        private val WHITESPACE_REGEX = Regex("\\s+")

        fun normalizeCustomName(name: String): String {
            return name.trim().replace(WHITESPACE_REGEX, " ")
        }

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
            val trimmed = normalizeCustomName(name)
            require(trimmed.isNotBlank()) { "Custom trigger name must not be blank" }
            val resolvedKey = if (TriggerType.fromKey(trimmed) != null) {
                "$CUSTOM_KEY_PREFIX$trimmed"
            } else {
                trimmed
            }
            return TriggerItem(
                key = resolvedKey,
                labelResId = null,
                customName = trimmed,
                isCustom = true,
                isEnabled = true
            )
        }

        fun fromCustomOrNull(name: String?): TriggerItem? {
            val trimmed = name?.let(::normalizeCustomName) ?: return null
            if (trimmed.isBlank()) return null
            return fromCustom(trimmed)
        }
    }
}
