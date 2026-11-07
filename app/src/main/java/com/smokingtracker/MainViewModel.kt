package com.smokingtracker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.ColorPreset
import com.smokingtracker.data.FontPreset
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.data.manager.GitHubUpdateManager
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.ThemePreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

data class MainUiState(
    val isRegistered: Boolean? = null,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val fontPreset: FontPreset = FontPreset.SYSTEM,
    val amoledTheme: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val colorPreset: ColorPreset = ColorPreset.SYSTEM,
    val useCustomVariableFont: Boolean = false,
    val customFontWeight: Int = 500,
    val customFontWidth: Float = 100f,
    val customFontRoundness: Float = 0f,
    val checkUpdatesOnStart: Boolean = false
)

class MainViewModel(
    private val themePreferences: ThemePreferences,
    private val appMetaPreferences: AppMetaPreferences,
    private val updateManager: GitHubUpdateManager,
    private val achievementsCoordinator: AchievementsCoordinator,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val registerMutex = Mutex()
    private val lastRegisterTimestampNs = AtomicLong(0L)

    val isRegistered: StateFlow<Boolean?> = appMetaPreferences.isRegistered.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
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

    val checkUpdatesOnStart: StateFlow<Boolean> = appMetaPreferences.checkUpdatesOnStart.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val updateCheckState: StateFlow<UpdateCheckState> = updateManager.updateCheckState

    val uiState: StateFlow<MainUiState> = combine(
        combine(
            appMetaPreferences.isRegistered,
            themePreferences.appTheme,
            themePreferences.fontPreset,
            themePreferences.amoledTheme,
            themePreferences.vibrationEnabled
        ) { isReg, theme, font, amoled, vibration ->
            MainThemeBasePart(
                isRegistered = isReg,
                themePreference = theme,
                fontPreset = font,
                amoledTheme = amoled,
                vibrationEnabled = vibration
            )
        },
        combine(
            themePreferences.colorPreset,
            themePreferences.useCustomVariableFont,
            themePreferences.customFontWeight,
            themePreferences.customFontWidth,
            themePreferences.customFontRoundness
        ) { color, useCustom, weight, width, roundness ->
            MainFontCustomPart(
                colorPreset = color,
                useCustomVariableFont = useCustom,
                customFontWeight = weight,
                customFontWidth = width,
                customFontRoundness = roundness
            )
        },
        appMetaPreferences.checkUpdatesOnStart
    ) { base, custom, checkUpdates ->
        MainUiState(
            isRegistered = base.isRegistered,
            themePreference = base.themePreference,
            fontPreset = base.fontPreset,
            amoledTheme = base.amoledTheme,
            vibrationEnabled = base.vibrationEnabled,
            colorPreset = custom.colorPreset,
            useCustomVariableFont = custom.useCustomVariableFont,
            customFontWeight = custom.customFontWeight,
            customFontWidth = custom.customFontWidth,
            customFontRoundness = custom.customFontRoundness,
            checkUpdatesOnStart = checkUpdates
        )
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState()
        )

    init {
        safeLaunch(ioDispatcher) {
            val now = System.currentTimeMillis()
            val snapshot = appMetaPreferences.getAchievementMetaSnapshot()
            val today = toLocalDate(now)
            val alreadyLoggedToday = snapshot.appLaunchDates.any { launchTs ->
                toLocalDate(launchTs) == today
            }

            if (!alreadyLoggedToday) {
                appMetaPreferences.recordAppLaunch(now)
            }
            if (snapshot.isRegistered) {
                achievementsCoordinator.checkAndUpdateSuspend()
            }
        }
    }

    fun registerUser() {
        val nowNs = System.nanoTime()
        val prevNs = lastRegisterTimestampNs.get()
        if (prevNs != 0L && (nowNs - prevNs) in 0L until REGISTER_DEBOUNCE_NS) {
            return
        }
        if (!lastRegisterTimestampNs.compareAndSet(prevNs, nowNs)) {
            return
        }

        safeLaunch(ioDispatcher) {
            registerMutex.withLock {
                if (appMetaPreferences.isRegistered.first()) {
                    return@withLock
                }
                val now = System.currentTimeMillis()
                appMetaPreferences.saveUserProfile()
                appMetaPreferences.recordAppLaunch(now)
                achievementsCoordinator.checkAndUpdateSuspend()
            }
        }
    }

    fun checkForUpdates(isManual: Boolean) {
        safeLaunch(ioDispatcher) {
            updateManager.checkForUpdatesWithState(isManual)
        }
    }

    fun resetUpdateCheckState() {
        updateManager.resetUpdateCheckState()
    }

    private fun safeLaunch(
        dispatcher: CoroutineDispatcher = ioDispatcher,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error in MainViewModel coroutine", e)
            }
        }
    }

    private data class MainThemeBasePart(
        val isRegistered: Boolean,
        val themePreference: ThemePreference,
        val fontPreset: FontPreset,
        val amoledTheme: Boolean,
        val vibrationEnabled: Boolean
    )

    private data class MainFontCustomPart(
        val colorPreset: ColorPreset,
        val useCustomVariableFont: Boolean,
        val customFontWeight: Int,
        val customFontWidth: Float,
        val customFontRoundness: Float
    )

    companion object {
        private const val TAG = "MainViewModel"
        private const val REGISTER_DEBOUNCE_NS = 750_000_000L // 750ms
    }
}
