package com.smokingtracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.ui.MainApp
import com.smokingtracker.ui.theme.AppTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val dataStoreManager: DataStoreManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var initialThemeLoaded by mutableStateOf(false)
        splashScreen.setKeepOnScreenCondition { !initialThemeLoaded }

        lifecycleScope.launch {
            dataStoreManager.appTheme.first() 
            initialThemeLoaded = true
        }

        enableEdgeToEdge()

        setContent {
            if (!initialThemeLoaded) return@setContent

            val viewModel: MainViewModel = koinViewModel()

            val themePreference by viewModel.themePreference.collectAsState()
            val fontPreset by viewModel.fontPreset.collectAsState()
            val amoledTheme by viewModel.amoledTheme.collectAsState()
            val colorPreset by viewModel.colorPreset.collectAsState()
            val useDarkTheme = when (themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            AppTheme(
                useDarkTheme = useDarkTheme,
                fontPreset = fontPreset,
                amoledThemeEnabled = amoledTheme,
                colorPreset = colorPreset
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
