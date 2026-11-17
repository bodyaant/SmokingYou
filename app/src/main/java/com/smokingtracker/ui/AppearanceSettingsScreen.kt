package com.smokingtracker.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smokingtracker.AppearanceViewModel
import com.smokingtracker.R
import com.smokingtracker.data.ColorPreset
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.ui.appearance.AppIconSelector
import com.smokingtracker.ui.appearance.ColorPresetSelector
import com.smokingtracker.ui.appearance.FontSelectionSection
import com.smokingtracker.ui.appearance.ThemeSegmentedButton
import com.smokingtracker.ui.theme.ContainerGroupPosition
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerGroupGap
import com.smokingtracker.ui.theme.containerPadding
import com.smokingtracker.ui.theme.containerShape
import org.koin.androidx.compose.koinViewModel

typealias AppIconOption = com.smokingtracker.ui.appearance.AppIconOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceSettingsScreen(
    viewModel: AppearanceViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    val fontPreset by viewModel.fontPreset.collectAsStateWithLifecycle()
    val amoledTheme by viewModel.amoledTheme.collectAsStateWithLifecycle()
    val colorPreset by viewModel.colorPreset.collectAsStateWithLifecycle()
    val appIcon by viewModel.appIcon.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val useCustomVariableFont by viewModel.useCustomVariableFont.collectAsStateWithLifecycle()
    val customFontWeight by viewModel.customFontWeight.collectAsStateWithLifecycle()
    val customFontWidth by viewModel.customFontWidth.collectAsStateWithLifecycle()
    val customFontRoundness by viewModel.customFontRoundness.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val useDarkTheme = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_appearance),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 12.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(containerGroupGap())
        ) {
            item(key = "header_theme") {
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }

            item(key = "card_theme") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp), ContainerGroupPosition.FIRST),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(containerPadding(20.dp, 20.dp))) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val themeIcon = when (themePreference) {
                                        ThemePreference.SYSTEM -> Icons.Filled.BrightnessAuto
                                        ThemePreference.LIGHT -> Icons.Filled.LightMode
                                        ThemePreference.DARK -> Icons.Filled.DarkMode
                                    }
                                    AnimatedContent(
                                        targetState = themeIcon,
                                        label = "theme_header_icon"
                                    ) { icon ->
                                        Icon(icon, contentDescription = null)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.settings_theme),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    stringResource(R.string.settings_theme_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        ThemeSegmentedButton(
                            currentTheme = themePreference,
                            onThemeChange = viewModel::updateThemePreference
                        )
                    }
                }
            }

            item(key = "card_amoled") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(8.dp), ContainerGroupPosition.MIDDLE),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    val isAmoledSwitchEnabled = useDarkTheme
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(containerPadding(20.dp, 18.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isAmoledSwitchEnabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.38f),
                            contentColor = if (isAmoledSwitchEnabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.38f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Brightness4, contentDescription = null)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_amoled_theme),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isAmoledSwitchEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                            Text(
                                text = stringResource(R.string.settings_amoled_theme_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isAmoledSwitchEnabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        }
                        Switch(
                            checked = amoledTheme && isAmoledSwitchEnabled,
                            enabled = isAmoledSwitchEnabled,
                            onCheckedChange = viewModel::updateAmoledTheme,
                            thumbContent = {
                                SwitchThumb(amoledTheme && isAmoledSwitchEnabled)
                            }
                        )
                    }
                }
            }

            item(key = "card_vibration") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp), ContainerGroupPosition.LAST),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(containerPadding(20.dp, 18.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.TouchApp, contentDescription = null)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_vibration),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.settings_vibration_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateVibrationEnabled(enabled)
                                if (enabled) {
                                    HapticFeedbackHelper.performClick(true, null, context)
                                }
                            },
                            thumbContent = {
                                SwitchThumb(vibrationEnabled)
                            }
                        )
                    }
                }
            }

            item(key = "header_color_preset") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_color_preset),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }

            item(key = "card_color_preset") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(containerPadding(20.dp, 20.dp))) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Palette, contentDescription = null)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.settings_color_preset),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                val currentPresetTitle = when (colorPreset) {
                                    ColorPreset.SYSTEM -> stringResource(R.string.color_preset_system)
                                    ColorPreset.FOREST_SAGE -> stringResource(R.string.color_preset_sage)
                                    ColorPreset.SUNSET_ROSE -> stringResource(R.string.color_preset_rose)
                                    ColorPreset.OCEAN_DEEP -> stringResource(R.string.color_preset_ocean)
                                    ColorPreset.PURPLE_NEBULA -> stringResource(R.string.color_preset_purple)
                                    ColorPreset.AMBER_GOLD -> stringResource(R.string.color_preset_amber)
                                    ColorPreset.CRIMSON_BERRY -> stringResource(R.string.color_preset_crimson)
                                    ColorPreset.SLATE_MONO -> stringResource(R.string.color_preset_slate)
                                }
                                AnimatedContent(
                                    targetState = currentPresetTitle,
                                    label = "color_preset_subtitle"
                                ) { titleText ->
                                    Text(
                                        text = titleText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        ColorPresetSelector(
                            currentPreset = colorPreset,
                            useDarkTheme = useDarkTheme,
                            onPresetChange = viewModel::updateColorPreset,
                            vibrationEnabled = vibrationEnabled
                        )
                    }
                }
            }

            item(key = "header_app_icon") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_app_icon),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }

            item(key = "card_app_icon") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(containerPadding(20.dp, 20.dp))) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Palette,
                                        contentDescription = null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.settings_app_icon),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    stringResource(R.string.settings_app_icon_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        AppIconSelector(
                            currentIcon = appIcon,
                            onIconChange = viewModel::updateAppIcon
                        )
                    }
                }
            }

            item(key = "header_font") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_font),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }

            item(key = "card_font") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(containerPadding(20.dp, 20.dp))) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.TextFields, contentDescription = null)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.settings_font),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    stringResource(R.string.settings_font_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        FontSelectionSection(
                            currentPreset = fontPreset,
                            onPresetChange = viewModel::updateFontPreset,
                            useCustomVariableFont = useCustomVariableFont,
                            onUseCustomVariableFontChange = viewModel::updateUseCustomVariableFont,
                            customFontWeight = customFontWeight,
                            onCustomFontWeightChange = viewModel::updateCustomFontWeight,
                            customFontWidth = customFontWidth,
                            onCustomFontWidthChange = viewModel::updateCustomFontWidth,
                            customFontRoundness = customFontRoundness,
                            onCustomFontRoundnessChange = viewModel::updateCustomFontRoundness,
                            vibrationEnabled = vibrationEnabled
                        )
                    }
                }
            }
        }
    }
}
