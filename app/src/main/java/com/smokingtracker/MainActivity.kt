package com.smokingtracker

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.smokingtracker.data.preferences.ThemePreferences
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.ui.MainApp
import com.smokingtracker.ui.theme.AppTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val themePreferences: ThemePreferences by inject()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var initialThemeLoaded by mutableStateOf(false)
        splashScreen.setKeepOnScreenCondition { !initialThemeLoaded }

        lifecycleScope.launch {
            themePreferences.appTheme.first()
            initialThemeLoaded = true
        }

        enableEdgeToEdge()

        setContent {
            if (!initialThemeLoaded) return@setContent

            val viewModel: MainViewModel = koinViewModel()

            val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
            val fontPreset by viewModel.fontPreset.collectAsStateWithLifecycle()
            val amoledTheme by viewModel.amoledTheme.collectAsStateWithLifecycle()
            val colorPreset by viewModel.colorPreset.collectAsStateWithLifecycle()
            val useCustomVariableFont by viewModel.useCustomVariableFont.collectAsStateWithLifecycle()
            val customFontWeight by viewModel.customFontWeight.collectAsStateWithLifecycle()
            val customFontWidth by viewModel.customFontWidth.collectAsStateWithLifecycle()
            val customFontRoundness by viewModel.customFontRoundness.collectAsStateWithLifecycle()
            val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()

            val useDarkTheme = when (themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            AppTheme(
                useDarkTheme = useDarkTheme,
                fontPreset = fontPreset,
                amoledThemeEnabled = amoledTheme,
                colorPreset = colorPreset,
                useCustomVariableFont = useCustomVariableFont,
                customFontWeight = customFontWeight,
                customFontWidth = customFontWidth,
                customFontRoundness = customFontRoundness
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    com.smokingtracker.ui.theme.LocalVibrationEnabled provides vibrationEnabled
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
