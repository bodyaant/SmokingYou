package com.smokingtracker

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.ui.MainApp
import com.smokingtracker.ui.theme.AppTheme
import com.smokingtracker.ui.theme.LocalVibrationEnabled
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun attachBaseContext(base: Context) {
        val localizedContext = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val prefs = base.getSharedPreferences(LOCALE_PREFS_NAME, Context.MODE_PRIVATE)
            val savedLanguageTag = prefs.getString(KEY_LANGUAGE_TAG, null)
            if (!savedLanguageTag.isNullOrBlank()) {
                val locale = Locale.forLanguageTag(savedLanguageTag)
                Locale.setDefault(locale)
                val config = Configuration(base.resources.configuration)
                config.setLocale(locale)
                base.createConfigurationContext(config)
            } else {
                base
            }
        } else {
            base
        }
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.isRegistered == null
        }

        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val useDarkTheme = when (uiState.themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            AppTheme(
                useDarkTheme = useDarkTheme,
                fontPreset = uiState.fontPreset,
                amoledThemeEnabled = uiState.amoledTheme,
                colorPreset = uiState.colorPreset,
                useCustomVariableFont = uiState.useCustomVariableFont,
                customFontWeight = uiState.customFontWeight,
                customFontWidth = uiState.customFontWidth,
                customFontRoundness = uiState.customFontRoundness
            ) {
                CompositionLocalProvider(
                    LocalVibrationEnabled provides uiState.vibrationEnabled
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

    companion object {
        const val LOCALE_PREFS_NAME = "app_locale_prefs"
        const val KEY_LANGUAGE_TAG = "language_tag"
    }
}
