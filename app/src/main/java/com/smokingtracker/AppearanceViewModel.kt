package com.smokingtracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.AppIconPreset
import com.smokingtracker.data.ColorPreset
import com.smokingtracker.data.FontPreset
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppearanceViewModel(
    private val themePreferences: ThemePreferences,
    private val appMetaPreferences: AppMetaPreferences,
    private val achievementsCoordinator: AchievementsCoordinator,
    private val appIconManager: AppIconManager
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = themePreferences.appTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreference.SYSTEM
    )

    val fontPreset: StateFlow<FontPreset> = themePreferences.fontPreset.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FontPreset.SYSTEM
    )

    val amoledTheme: StateFlow<Boolean> = themePreferences.amoledTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val vibrationEnabled: StateFlow<Boolean> = themePreferences.vibrationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val colorPreset: StateFlow<ColorPreset> = themePreferences.colorPreset.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ColorPreset.SYSTEM
    )

    val appIcon: StateFlow<AppIconPreset> = themePreferences.appIcon.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppIconPreset.DEFAULT
    )

    val useCustomVariableFont: StateFlow<Boolean> = themePreferences.useCustomVariableFont.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val customFontWeight: StateFlow<Int> = themePreferences.customFontWeight.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 500
    )

    val customFontWidth: StateFlow<Float> = themePreferences.customFontWidth.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 100f
    )

    val customFontRoundness: StateFlow<Float> = themePreferences.customFontRoundness.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    fun updateThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            themePreferences.saveThemePreference(theme)
            appMetaPreferences.recordThemeOrLangChange()
            achievementsCoordinator.checkAndUpdate()
        }
    }

    fun updateAmoledTheme(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.saveAmoledTheme(enabled)
        }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.saveVibrationEnabled(enabled)
        }
    }

    fun updateColorPreset(preset: ColorPreset) {
        viewModelScope.launch {
            themePreferences.saveColorPreset(preset)
        }
    }

    fun updateAppIcon(preset: AppIconPreset) {
        viewModelScope.launch {
            themePreferences.saveAppIcon(preset)
            appIconManager.applyIcon(preset)
        }
    }

    fun updateFontPreset(preset: FontPreset) {
        viewModelScope.launch {
            themePreferences.saveFontPreset(preset)
        }
    }

    fun updateUseCustomVariableFont(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.saveUseCustomVariableFont(enabled)
        }
    }

    fun updateCustomFontWeight(weight: Int) {
        viewModelScope.launch {
            themePreferences.saveCustomFontWeight(weight)
        }
    }

    fun updateCustomFontWidth(width: Float) {
        viewModelScope.launch {
            themePreferences.saveCustomFontWidth(width)
        }
    }

    fun updateCustomFontRoundness(roundness: Float) {
        viewModelScope.launch {
            themePreferences.saveCustomFontRoundness(roundness)
        }
    }
}
