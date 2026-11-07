package com.smokingtracker.data.preferences

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Keep
enum class ThemePreference {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromName(name: String?): ThemePreference {
            val trimmed = name?.trim().orEmpty()
            if (trimmed.isEmpty()) return SYSTEM
            return entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) } ?: SYSTEM
        }
    }
}

@Keep
enum class FontPreset {
    ZENITH, NEO, COMPACT, AIRY, SYSTEM, WIDE, OUTFIT;

    companion object {
        fun fromName(name: String?): FontPreset {
            val trimmed = name?.trim().orEmpty()
            if (trimmed.isEmpty()) return SYSTEM
            val preset = entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) } ?: SYSTEM
            return if (preset == WIDE) SYSTEM else preset
        }
    }
}

@Keep
enum class ColorPreset {
    SYSTEM, FOREST_SAGE, SUNSET_ROSE, OCEAN_DEEP, PURPLE_NEBULA, AMBER_GOLD, CRIMSON_BERRY, SLATE_MONO;

    companion object {
        fun fromName(name: String?): ColorPreset {
            val trimmed = name?.trim().orEmpty()
            if (trimmed.isEmpty()) return SYSTEM
            return entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) } ?: SYSTEM
        }
    }
}

@Keep
enum class AppIconPreset {
    DEFAULT, DARK, SUNSET, CREAM, NEON, GREEN, NIGHT, MONOCHROME;

    companion object {
        fun fromName(name: String?): AppIconPreset {
            val trimmed = name?.trim().orEmpty()
            if (trimmed.isEmpty()) return DEFAULT
            return entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) } ?: DEFAULT
        }
    }
}

class ThemePreferences(private val context: Context) {
    private val dataFlow: Flow<Preferences> = context.dataStore.safeData

    companion object {
        val APP_THEME = stringPreferencesKey("app_theme")
        val FONT_PRESET = stringPreferencesKey("font_preset")
        val AMOLED_THEME = booleanPreferencesKey("amoled_theme")
        val COLOR_PRESET = stringPreferencesKey("color_preset")
        val APP_ICON = stringPreferencesKey("app_icon")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val USE_CUSTOM_VARIABLE_FONT = booleanPreferencesKey("use_custom_variable_font")
        val CUSTOM_FONT_WEIGHT = intPreferencesKey("custom_font_weight")
        val CUSTOM_FONT_WIDTH = floatPreferencesKey("custom_font_width")
        val CUSTOM_FONT_ROUNDNESS = floatPreferencesKey("custom_font_roundness")
        val APP_LANGUAGE_TAG = stringPreferencesKey("app_language_tag")
    }

    val appTheme: Flow<ThemePreference> = dataFlow.map { preferences ->
        ThemePreference.fromName(preferences[APP_THEME])
    }.distinctUntilChanged()

    val fontPreset: Flow<FontPreset> = dataFlow.map { preferences ->
        FontPreset.fromName(preferences[FONT_PRESET])
    }.distinctUntilChanged()

    val amoledTheme: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[AMOLED_THEME] ?: false
    }.distinctUntilChanged()

    val colorPreset: Flow<ColorPreset> = dataFlow.map { preferences ->
        ColorPreset.fromName(preferences[COLOR_PRESET])
    }.distinctUntilChanged()

    val appIcon: Flow<AppIconPreset> = dataFlow.map { preferences ->
        AppIconPreset.fromName(preferences[APP_ICON])
    }.distinctUntilChanged()

    val vibrationEnabled: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }.distinctUntilChanged()

    val useCustomVariableFont: Flow<Boolean> = dataFlow.map { preferences ->
        preferences[USE_CUSTOM_VARIABLE_FONT] ?: false
    }.distinctUntilChanged()

    val customFontWeight: Flow<Int> = dataFlow.map { preferences ->
        (preferences[CUSTOM_FONT_WEIGHT] ?: 500).coerceIn(100, 1000)
    }.distinctUntilChanged()

    val customFontWidth: Flow<Float> = dataFlow.map { preferences ->
        val width = preferences[CUSTOM_FONT_WIDTH] ?: 100f
        if (width.isFinite()) width.coerceIn(50f, 150f) else 100f
    }.distinctUntilChanged()

    val customFontRoundness: Flow<Float> = dataFlow.map { preferences ->
        val roundness = preferences[CUSTOM_FONT_ROUNDNESS] ?: 0f
        if (roundness.isFinite()) roundness.coerceIn(0f, 100f) else 0f
    }.distinctUntilChanged()

    val appLanguageTag: Flow<String?> = dataFlow.map { preferences ->
        preferences[APP_LANGUAGE_TAG]
    }.distinctUntilChanged()

    suspend fun saveThemePreference(theme: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme.name
        }
    }

    suspend fun saveFontPreset(preset: FontPreset) {
        context.dataStore.edit { preferences ->
            preferences[FONT_PRESET] = if (preset == FontPreset.WIDE) FontPreset.SYSTEM.name else preset.name
        }
    }

    suspend fun saveAmoledTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AMOLED_THEME] = enabled
        }
    }

    suspend fun saveColorPreset(preset: ColorPreset) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_PRESET] = preset.name
        }
    }

    suspend fun saveAppIcon(preset: AppIconPreset) {
        context.dataStore.edit { preferences ->
            preferences[APP_ICON] = preset.name
        }
    }

    suspend fun saveVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun saveUseCustomVariableFont(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_CUSTOM_VARIABLE_FONT] = enabled
        }
    }

    suspend fun saveCustomFontWeight(weight: Int) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_FONT_WEIGHT] = weight.coerceIn(100, 1000)
        }
    }

    suspend fun saveCustomFontWidth(width: Float) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_FONT_WIDTH] = if (width.isFinite()) width.coerceIn(50f, 150f) else 100f
        }
    }

    suspend fun saveCustomFontRoundness(roundness: Float) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_FONT_ROUNDNESS] = if (roundness.isFinite()) roundness.coerceIn(0f, 100f) else 0f
        }
    }

    suspend fun saveAppLanguageTag(tag: String) {
        context.dataStore.edit { preferences ->
            val trimmed = tag.trim()
            if (trimmed.isNotEmpty()) {
                preferences[APP_LANGUAGE_TAG] = trimmed
            } else {
                preferences.remove(APP_LANGUAGE_TAG)
            }
        }
    }

    suspend fun resetThemePreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(APP_THEME)
            preferences.remove(FONT_PRESET)
            preferences.remove(AMOLED_THEME)
            preferences.remove(COLOR_PRESET)
            preferences.remove(APP_ICON)
            preferences.remove(VIBRATION_ENABLED)
            preferences.remove(USE_CUSTOM_VARIABLE_FONT)
            preferences.remove(CUSTOM_FONT_WEIGHT)
            preferences.remove(CUSTOM_FONT_WIDTH)
            preferences.remove(CUSTOM_FONT_ROUNDNESS)
            preferences.remove(APP_LANGUAGE_TAG)
        }
    }

    suspend fun restoreThemePreferences(
        theme: String,
        colorPresetVal: String,
        fontPresetVal: String,
        amoledThemeVal: Boolean,
        vibrationEnabledVal: Boolean = true,
        useCustomVariableFontVal: Boolean = false,
        customFontWeightVal: Int = 500,
        customFontWidthVal: Float = 100f,
        customFontRoundnessVal: Float = 0f,
        appIconVal: String = AppIconPreset.DEFAULT.name,
        appLanguageTagVal: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = ThemePreference.fromName(theme).name
            preferences[COLOR_PRESET] = ColorPreset.fromName(colorPresetVal).name
            preferences[FONT_PRESET] = FontPreset.fromName(fontPresetVal).name
            preferences[AMOLED_THEME] = amoledThemeVal
            preferences[VIBRATION_ENABLED] = vibrationEnabledVal
            preferences[USE_CUSTOM_VARIABLE_FONT] = useCustomVariableFontVal
            preferences[CUSTOM_FONT_WEIGHT] = customFontWeightVal.coerceIn(100, 1000)
            preferences[CUSTOM_FONT_WIDTH] = if (customFontWidthVal.isFinite()) customFontWidthVal.coerceIn(50f, 150f) else 100f
            preferences[CUSTOM_FONT_ROUNDNESS] = if (customFontRoundnessVal.isFinite()) customFontRoundnessVal.coerceIn(0f, 100f) else 0f
            preferences[APP_ICON] = AppIconPreset.fromName(appIconVal).name
            if (!appLanguageTagVal.isNullOrBlank()) {
                preferences[APP_LANGUAGE_TAG] = appLanguageTagVal.trim()
            } else {
                preferences.remove(APP_LANGUAGE_TAG)
            }
        }
    }
}
