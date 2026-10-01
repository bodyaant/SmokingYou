package com.smokingtracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemePreference {
    SYSTEM, LIGHT, DARK
}

enum class FontPreset { ZENITH, NEO, COMPACT, AIRY, SYSTEM, WIDE, OUTFIT }
enum class ColorPreset { SYSTEM, FOREST_SAGE, SUNSET_ROSE, OCEAN_DEEP, PURPLE_NEBULA, AMBER_GOLD, CRIMSON_BERRY, SLATE_MONO }
enum class AppIconPreset { DEFAULT, DARK, SUNSET, CREAM, NEON, GREEN, NIGHT, MONOCHROME }

class ThemePreferences(private val context: Context) {

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

    val appTheme: Flow<ThemePreference> = context.dataStore.data.map { preferences ->
        val themeName = preferences[APP_THEME] ?: ThemePreference.SYSTEM.name
        try {
            ThemePreference.valueOf(themeName)
        } catch (_: Exception) {
            ThemePreference.SYSTEM
        }
    }

    val fontPreset: Flow<FontPreset> = context.dataStore.data.map { preferences ->
        val name = preferences[FONT_PRESET] ?: FontPreset.SYSTEM.name
        try {
            val preset = FontPreset.valueOf(name)
            if (preset == FontPreset.WIDE) FontPreset.SYSTEM else preset
        } catch (_: Exception) {
            FontPreset.SYSTEM
        }
    }

    val amoledTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AMOLED_THEME] ?: false
    }

    val colorPreset: Flow<ColorPreset> = context.dataStore.data.map { preferences ->
        val name = preferences[COLOR_PRESET] ?: ColorPreset.SYSTEM.name
        try {
            ColorPreset.valueOf(name)
        } catch (_: Exception) {
            ColorPreset.SYSTEM
        }
    }

    val appIcon: Flow<AppIconPreset> = context.dataStore.data.map { preferences ->
        val name = preferences[APP_ICON] ?: AppIconPreset.DEFAULT.name
        try {
            AppIconPreset.valueOf(name)
        } catch (_: Exception) {
            AppIconPreset.DEFAULT
        }
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    val useCustomVariableFont: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_CUSTOM_VARIABLE_FONT] ?: false
    }

    val customFontWeight: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_FONT_WEIGHT] ?: 500
    }

    val customFontWidth: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_FONT_WIDTH] ?: 100f
    }

    val customFontRoundness: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_FONT_ROUNDNESS] ?: 0f
    }

    val appLanguageTag: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_LANGUAGE_TAG]
    }

    suspend fun saveThemePreference(theme: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme.name
        }
    }

    suspend fun saveFontPreset(preset: FontPreset) {
        context.dataStore.edit { preferences ->
            preferences[FONT_PRESET] = preset.name
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
            preferences[CUSTOM_FONT_WEIGHT] = weight
        }
    }

    suspend fun saveCustomFontWidth(width: Float) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_FONT_WIDTH] = width
        }
    }

    suspend fun saveCustomFontRoundness(roundness: Float) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_FONT_ROUNDNESS] = roundness
        }
    }

    suspend fun saveAppLanguageTag(tag: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE_TAG] = tag
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
        appIconVal: String = AppIconPreset.DEFAULT.name
    ) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme
            preferences[COLOR_PRESET] = colorPresetVal
            preferences[FONT_PRESET] = fontPresetVal
            preferences[AMOLED_THEME] = amoledThemeVal
            preferences[VIBRATION_ENABLED] = vibrationEnabledVal
            preferences[USE_CUSTOM_VARIABLE_FONT] = useCustomVariableFontVal
            preferences[CUSTOM_FONT_WEIGHT] = customFontWeightVal
            preferences[CUSTOM_FONT_WIDTH] = customFontWidthVal
            preferences[CUSTOM_FONT_ROUNDNESS] = customFontRoundnessVal
            preferences[APP_ICON] = appIconVal
        }
    }
}
