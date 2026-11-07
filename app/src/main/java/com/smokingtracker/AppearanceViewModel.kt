package com.smokingtracker

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.AppIconPreset
import com.smokingtracker.data.ColorPreset
import com.smokingtracker.data.FontPreset
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.ThemePreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Immutable
data class AppearanceUiState(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val fontPreset: FontPreset = FontPreset.SYSTEM,
    val amoledTheme: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val colorPreset: ColorPreset = ColorPreset.SYSTEM,
    val appIcon: AppIconPreset = AppIconPreset.DEFAULT,
    val useCustomVariableFont: Boolean = false,
    val customFontWeight: Int = AppearanceViewModel.DEFAULT_FONT_WEIGHT,
    val customFontWidth: Float = AppearanceViewModel.DEFAULT_FONT_WIDTH,
    val customFontRoundness: Float = AppearanceViewModel.DEFAULT_FONT_ROUNDNESS
)

@OptIn(FlowPreview::class)
class AppearanceViewModel(
    private val themePreferences: ThemePreferences,
    private val appMetaPreferences: AppMetaPreferences,
    private val achievementsCoordinator: AchievementsCoordinator,
    private val appIconManager: AppIconManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val iconMutex = Mutex()

    private val pendingFontWeight = MutableStateFlow<Int?>(null)
    private val pendingFontWidth = MutableStateFlow<Float?>(null)
    private val pendingFontRoundness = MutableStateFlow<Float?>(null)

    private val fontWeightWriteRequests = MutableSharedFlow<Int>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val fontWidthWriteRequests = MutableSharedFlow<Float>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val fontRoundnessWriteRequests = MutableSharedFlow<Float>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

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

    val customFontWeight: StateFlow<Int> = combine(
        themePreferences.customFontWeight,
        pendingFontWeight
    ) { saved, pending ->
        pending ?: saved
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_FONT_WEIGHT
        )

    val customFontWidth: StateFlow<Float> = combine(
        themePreferences.customFontWidth,
        pendingFontWidth
    ) { saved, pending ->
        pending ?: saved
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_FONT_WIDTH
        )

    val customFontRoundness: StateFlow<Float> = combine(
        themePreferences.customFontRoundness,
        pendingFontRoundness
    ) { saved, pending ->
        pending ?: saved
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_FONT_ROUNDNESS
        )

    val uiState: StateFlow<AppearanceUiState> = combine<Any?, AppearanceUiState>(
        listOf(
            themePreference,
            fontPreset,
            amoledTheme,
            vibrationEnabled,
            colorPreset,
            appIcon,
            useCustomVariableFont,
            customFontWeight,
            customFontWidth,
            customFontRoundness
        )
    ) { values ->
        AppearanceUiState(
            themePreference = values[0] as ThemePreference,
            fontPreset = values[1] as FontPreset,
            amoledTheme = values[2] as Boolean,
            vibrationEnabled = values[3] as Boolean,
            colorPreset = values[4] as ColorPreset,
            appIcon = values[5] as AppIconPreset,
            useCustomVariableFont = values[6] as Boolean,
            customFontWeight = values[7] as Int,
            customFontWidth = values[8] as Float,
            customFontRoundness = values[9] as Float
        )
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppearanceUiState()
        )

    init {
        safeLaunch(ioDispatcher) {
            fontWeightWriteRequests
                .debounce(FONT_SLIDER_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { weight ->
                    themePreferences.saveCustomFontWeight(weight)
                    if (pendingFontWeight.value == weight) {
                        pendingFontWeight.value = null
                    }
                }
        }
        safeLaunch(ioDispatcher) {
            fontWidthWriteRequests
                .debounce(FONT_SLIDER_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { width ->
                    themePreferences.saveCustomFontWidth(width)
                    if (pendingFontWidth.value == width) {
                        pendingFontWidth.value = null
                    }
                }
        }
        safeLaunch(ioDispatcher) {
            fontRoundnessWriteRequests
                .debounce(FONT_SLIDER_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { roundness ->
                    themePreferences.saveCustomFontRoundness(roundness)
                    if (pendingFontRoundness.value == roundness) {
                        pendingFontRoundness.value = null
                    }
                }
        }
    }

    private fun safeLaunch(
        dispatcher: CoroutineDispatcher = ioDispatcher,
        block: suspend CoroutineScope.() -> Unit
    ): Job = viewModelScope.launch(dispatcher) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in AppearanceViewModel coroutine", e)
        }
    }

    fun updateThemePreference(theme: ThemePreference) {
        safeLaunch(ioDispatcher) {
            val currentTheme = themePreferences.appTheme.first()
            if (currentTheme == theme) return@safeLaunch
            themePreferences.saveThemePreference(theme)
            appMetaPreferences.recordThemeOrLangChange()
            achievementsCoordinator.checkAndUpdateSuspend()
        }
    }

    fun updateAmoledTheme(enabled: Boolean) {
        safeLaunch(ioDispatcher) {
            themePreferences.saveAmoledTheme(enabled)
        }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        safeLaunch(ioDispatcher) {
            themePreferences.saveVibrationEnabled(enabled)
        }
    }

    fun updateColorPreset(preset: ColorPreset) {
        safeLaunch(ioDispatcher) {
            themePreferences.saveColorPreset(preset)
        }
    }

    fun updateAppIcon(preset: AppIconPreset) {
        safeLaunch(ioDispatcher) {
            iconMutex.withLock {
                val currentIcon = themePreferences.appIcon.first()
                if (currentIcon == preset) return@withLock
                themePreferences.saveAppIcon(preset)
                appIconManager.applyIcon(preset)
            }
        }
    }

    fun updateFontPreset(preset: FontPreset) {
        safeLaunch(ioDispatcher) {
            themePreferences.saveFontPreset(preset)
        }
    }

    fun updateUseCustomVariableFont(enabled: Boolean) {
        safeLaunch(ioDispatcher) {
            themePreferences.saveUseCustomVariableFont(enabled)
        }
    }

    fun updateCustomFontWeight(weight: Int) {
        val safeWeight = weight.coerceIn(MIN_FONT_WEIGHT, MAX_FONT_WEIGHT)
        pendingFontWeight.value = safeWeight
        fontWeightWriteRequests.tryEmit(safeWeight)
    }

    fun updateCustomFontWidth(width: Float) {
        if (!width.isFinite()) return
        val safeWidth = width.coerceIn(MIN_FONT_WIDTH, MAX_FONT_WIDTH)
        pendingFontWidth.value = safeWidth
        fontWidthWriteRequests.tryEmit(safeWidth)
    }

    fun updateCustomFontRoundness(roundness: Float) {
        if (!roundness.isFinite()) return
        val safeRoundness = roundness.coerceIn(MIN_FONT_ROUNDNESS, MAX_FONT_ROUNDNESS)
        pendingFontRoundness.value = safeRoundness
        fontRoundnessWriteRequests.tryEmit(safeRoundness)
    }

    companion object {
        private const val TAG = "AppearanceViewModel"
        private const val FONT_SLIDER_DEBOUNCE_MS = 200L
        const val DEFAULT_FONT_WEIGHT = 500
        const val DEFAULT_FONT_WIDTH = 100f
        const val DEFAULT_FONT_ROUNDNESS = 0f
        private const val MIN_FONT_WEIGHT = 100
        private const val MAX_FONT_WEIGHT = 1000
        private const val MIN_FONT_WIDTH = 50f
        private const val MAX_FONT_WIDTH = 150f
        private const val MIN_FONT_ROUNDNESS = 0f
        private const val MAX_FONT_ROUNDNESS = 100f
    }
}
