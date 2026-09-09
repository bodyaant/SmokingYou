package com.smokingtracker.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TouchApp
import com.smokingtracker.ui.theme.containerBorder
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.containerPadding
import com.smokingtracker.ui.theme.containerGroupGap
import com.smokingtracker.ui.theme.ContainerGroupPosition
import androidx.compose.material3.*
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.MainViewModel
import com.smokingtracker.R
import com.smokingtracker.data.AppIconPreset
import com.smokingtracker.data.ColorPreset
import com.smokingtracker.data.FontPreset
import com.smokingtracker.data.ThemePreference
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics



import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    val fontPreset by viewModel.fontPreset.collectAsStateWithLifecycle()
    val amoledTheme by viewModel.amoledTheme.collectAsStateWithLifecycle()
    val colorPreset by viewModel.colorPreset.collectAsStateWithLifecycle()
    val appIcon by viewModel.appIcon.collectAsStateWithLifecycle()
    val containerBorderEnabled by viewModel.containerBorderEnabled.collectAsStateWithLifecycle()
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            item {
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp), ContainerGroupPosition.FIRST),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = containerBorder()
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

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(8.dp), ContainerGroupPosition.MIDDLE),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = containerBorder()
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

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(8.dp), ContainerGroupPosition.MIDDLE),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = containerBorder()
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
                                Icon(Icons.Filled.CropSquare, contentDescription = null)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_container_border),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.settings_container_border_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        Switch(
                            checked = containerBorderEnabled,
                            onCheckedChange = viewModel::updateContainerBorderEnabled,
                            thumbContent = {
                                SwitchThumb(containerBorderEnabled)
                            }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp), ContainerGroupPosition.LAST),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = containerBorder()
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
                                    com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(true, null, context)
                                }
                            },
                            thumbContent = {
                                SwitchThumb(vibrationEnabled)
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_color_preset),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = containerBorder()
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

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_app_icon),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = containerBorder()
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
                                        imageVector = androidx.compose.material.icons.Icons.Filled.Palette,
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

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_font),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = containerBorder()
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

@Composable
fun ThemeSegmentedButton(
    currentTheme: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit
) {
    val options = listOf(
        Triple(ThemePreference.SYSTEM, stringResource(R.string.theme_system), Icons.Filled.BrightnessAuto),
        Triple(ThemePreference.LIGHT, stringResource(R.string.theme_light), Icons.Filled.LightMode),
        Triple(ThemePreference.DARK, stringResource(R.string.theme_dark), Icons.Filled.DarkMode)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, (theme, title, icon) ->
            val isSelected = currentTheme == theme

            val animatedWeight by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "theme_weight_$index"
            )

            val startR by animateDpAsState(
                targetValue = if (isSelected || index == 0) 24.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "theme_startR_$index"
            )
            val endR by animateDpAsState(
                targetValue = if (isSelected || index == options.size - 1) 24.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "theme_endR_$index"
            )

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                label = "theme_container_$index"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                label = "theme_content_$index"
            )

            Surface(
                onClick = { onThemeChange(theme) },
                modifier = Modifier
                    .weight(animatedWeight)
                    .height(48.dp),
                shape = RoundedCornerShape(topStart = startR, bottomStart = startR, topEnd = endR, bottomEnd = endR),
                color = containerColor,
                contentColor = contentColor
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FontSelectionSection(
    currentPreset: FontPreset,
    onPresetChange: (FontPreset) -> Unit,
    useCustomVariableFont: Boolean,
    onUseCustomVariableFontChange: (Boolean) -> Unit,
    customFontWeight: Int,
    onCustomFontWeightChange: (Int) -> Unit,
    customFontWidth: Float,
    onCustomFontWidthChange: (Float) -> Unit,
    customFontRoundness: Float,
    onCustomFontRoundnessChange: (Float) -> Unit,
    vibrationEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var lastGsFlexPreset by remember {
        mutableStateOf(
            if (currentPreset != FontPreset.SYSTEM && currentPreset != FontPreset.OUTFIT) {
                if (currentPreset == FontPreset.WIDE) FontPreset.ZENITH else currentPreset
            } else {
                FontPreset.ZENITH
            }
        )
    }

    LaunchedEffect(currentPreset) {
        if (currentPreset != FontPreset.SYSTEM && currentPreset != FontPreset.OUTFIT) {
            lastGsFlexPreset = if (currentPreset == FontPreset.WIDE) FontPreset.ZENITH else currentPreset
        }
    }

    val isGsFlexSelected = currentPreset != FontPreset.SYSTEM

    val fontTypeOptions = listOf(
        Triple(true, stringResource(R.string.font_family_gs_flex), Icons.Filled.TextFields),
        Triple(false, stringResource(R.string.font_preset_system), Icons.Filled.Smartphone)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            fontTypeOptions.forEachIndexed { index, (isGsFlex, title, icon) ->
                val isSelected = isGsFlex == isGsFlexSelected

                val animatedWeight by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "font_type_weight_$index"
                )

                val startR by animateDpAsState(
                    targetValue = if (isSelected || index == 0) 24.dp else 8.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "font_type_startR_$index"
                )
                val endR by animateDpAsState(
                    targetValue = if (isSelected || index == fontTypeOptions.size - 1) 24.dp else 8.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "font_type_endR_$index"
                )

                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    label = "font_type_container_$index"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                    label = "font_type_content_$index"
                )

                Surface(
                    onClick = {
                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        if (isGsFlex) {
                            onPresetChange(lastGsFlexPreset)
                        } else {
                            onPresetChange(FontPreset.SYSTEM)
                        }
                    },
                    modifier = Modifier
                        .weight(animatedWeight)
                        .height(48.dp),
                    shape = RoundedCornerShape(topStart = startR, bottomStart = startR, topEnd = endR, bottomEnd = endR),
                    color = containerColor,
                    contentColor = contentColor
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isGsFlexSelected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = containerShape(RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                    border = containerBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = stringResource(R.string.settings_custom_variable_font),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.settings_custom_variable_font_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        Switch(
                            checked = useCustomVariableFont,
                            onCheckedChange = {
                                com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                onUseCustomVariableFontChange(it)
                            },
                            thumbContent = { SwitchThumb(useCustomVariableFont) }
                        )
                    }
                }

                AnimatedContent(
                    targetState = useCustomVariableFont,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                    label = "customFontContent"
                ) { isCustom ->
                    if (!isCustom) {
                        val presetOptions = listOf(
                            FontPreset.ZENITH to stringResource(R.string.font_preset_zenith),
                            FontPreset.NEO to stringResource(R.string.font_preset_neo),
                            FontPreset.COMPACT to stringResource(R.string.font_preset_impact),
                            FontPreset.AIRY to stringResource(R.string.font_preset_airy)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            presetOptions.forEachIndexed { index, (preset, title) ->
                                val isSelected = isGsFlexSelected && (currentPreset == preset || (currentPreset == FontPreset.WIDE && preset == FontPreset.ZENITH))

                                val animatedWeight by androidx.compose.animation.core.animateFloatAsState(
                                    targetValue = if (isSelected) 1.25f else 1.0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                                    label = "gsflex_preset_weight_$index"
                                )

                                val startR by animateDpAsState(
                                    targetValue = if (isSelected || index == 0) 24.dp else 8.dp,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                                    label = "gsflex_preset_startR_$index"
                                )
                                val endR by animateDpAsState(
                                    targetValue = if (isSelected || index == presetOptions.size - 1) 24.dp else 8.dp,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                                    label = "gsflex_preset_endR_$index"
                                )

                                val containerColor by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    label = "gsflex_preset_container_$index"
                                )
                                val contentColor by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                    label = "gsflex_preset_content_$index"
                                )

                                Surface(
                                    onClick = {
                                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                        lastGsFlexPreset = preset
                                        onPresetChange(preset)
                                    },
                                    modifier = Modifier
                                        .weight(animatedWeight)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(topStart = startR, bottomStart = startR, topEnd = endR, bottomEnd = endR),
                                    color = containerColor,
                                    contentColor = contentColor
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Card(
                                shape = containerShape(RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                ),
                                border = containerBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.font_preview_sample),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "12:45 • 20 pcs • $5.50 • 98%",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.font_weight_label),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$customFontWeight",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = customFontWeight.toFloat(),
                                    onValueChange = { onCustomFontWeightChange(it.toInt()) },
                                    valueRange = 100f..1000f,
                                    steps = 17
                                )
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.font_width_label),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${customFontWidth.toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = customFontWidth,
                                    onValueChange = { onCustomFontWidthChange(it) },
                                    valueRange = 25f..150f,
                                    steps = 24
                                )
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.font_roundness_label),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${customFontRoundness.toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = customFontRoundness,
                                    onValueChange = { onCustomFontRoundnessChange(it) },
                                    valueRange = 0f..100f,
                                    steps = 19
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
private data class ColorPresetOption(
    val preset: ColorPreset,
    val nameRes: Int,
    val containerColor: Color,
    val contentColor: Color,
    val checkedContainerColor: Color,
    val checkedContentColor: Color
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun toggleButtonColors(
    containerColor: Color,
    contentColor: Color,
    checkedContainerColor: Color,
    checkedContentColor: Color
): ToggleButtonColors = ToggleButtonColors(
    containerColor = containerColor,
    contentColor = contentColor,
    disabledContainerColor = containerColor.copy(alpha = 0.38f),
    disabledContentColor = contentColor.copy(alpha = 0.38f),
    checkedContainerColor = checkedContainerColor,
    checkedContentColor = checkedContentColor
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorPresetSelector(
    currentPreset: ColorPreset,
    useDarkTheme: Boolean,
    onPresetChange: (ColorPreset) -> Unit,
    vibrationEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val colorPresets = remember(useDarkTheme) {
        listOf(
            ColorPresetOption(
                preset = ColorPreset.FOREST_SAGE,
                nameRes = R.string.color_preset_sage,
                containerColor = if (useDarkTheme) Color(0xFF354E16) else Color(0xFFCDEDA3),
                contentColor = if (useDarkTheme) Color(0xFFCDEDA3) else Color(0xFF101F00),
                checkedContainerColor = if (useDarkTheme) Color(0xFFB1D18A) else Color(0xFF4C662B),
                checkedContentColor = if (useDarkTheme) Color(0xFF1F3701) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.SUNSET_ROSE,
                nameRes = R.string.color_preset_rose,
                containerColor = if (useDarkTheme) Color(0xFF723523) else Color(0xFFFFDBD1),
                contentColor = if (useDarkTheme) Color(0xFFFFDBD1) else Color(0xFF3A0B01),
                checkedContainerColor = if (useDarkTheme) Color(0xFFF5B5A1) else Color(0xFF8F4C38),
                checkedContentColor = if (useDarkTheme) Color(0xFF561F0F) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.OCEAN_DEEP,
                nameRes = R.string.color_preset_ocean,
                containerColor = if (useDarkTheme) Color(0xFF004C69) else Color(0xFFC0E8FF),
                contentColor = if (useDarkTheme) Color(0xFFC0E8FF) else Color(0xFF001E2C),
                checkedContainerColor = if (useDarkTheme) Color(0xFF76D1FF) else Color(0xFF006689),
                checkedContentColor = if (useDarkTheme) Color(0xFF003549) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.PURPLE_NEBULA,
                nameRes = R.string.color_preset_purple,
                containerColor = if (useDarkTheme) Color(0xFF533588) else Color(0xFFEBDCFF),
                contentColor = if (useDarkTheme) Color(0xFFEBDCFF) else Color(0xFF250059),
                checkedContainerColor = if (useDarkTheme) Color(0xFFD4BBFF) else Color(0xFF6B4EA2),
                checkedContentColor = if (useDarkTheme) Color(0xFF3B1D71) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.AMBER_GOLD,
                nameRes = R.string.color_preset_amber,
                containerColor = if (useDarkTheme) Color(0xFF633F00) else Color(0xFFFFDDB3),
                contentColor = if (useDarkTheme) Color(0xFFFFDDB3) else Color(0xFF291800),
                checkedContainerColor = if (useDarkTheme) Color(0xFFFFB95B) else Color(0xFF825500),
                checkedContentColor = if (useDarkTheme) Color(0xFF452B00) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.CRIMSON_BERRY,
                nameRes = R.string.color_preset_crimson,
                containerColor = if (useDarkTheme) Color(0xFF7C002C) else Color(0xFFFFD9DD),
                contentColor = if (useDarkTheme) Color(0xFFFFD9DD) else Color(0xFF3A0010),
                checkedContainerColor = if (useDarkTheme) Color(0xFFFFB2BE) else Color(0xFF980038),
                checkedContentColor = if (useDarkTheme) Color(0xFF5F0021) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.SLATE_MONO,
                nameRes = R.string.color_preset_slate,
                containerColor = if (useDarkTheme) Color(0xFF383838) else Color(0xFFE2E2E2),
                contentColor = if (useDarkTheme) Color(0xFFE2E2E2) else Color(0xFF1A1A1A),
                checkedContainerColor = if (useDarkTheme) Color(0xFFC6C6C6) else Color(0xFF474747),
                checkedContentColor = if (useDarkTheme) Color(0xFF202020) else Color(0xFFFFFFFF)
            )
        )
    }

    val isSystemSelected = currentPreset == ColorPreset.SYSTEM
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                val systemEndR by animateDpAsState(
                    targetValue = if (isSystemSelected) 20.dp else 6.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "color_system_endR"
                )
                val systemShape = RoundedCornerShape(
                    topStart = 20.dp,
                    bottomStart = 20.dp,
                    topEnd = systemEndR,
                    bottomEnd = systemEndR
                )

                ToggleButton(
                    checked = isSystemSelected,
                    onCheckedChange = {
                        if (!isSystemSelected) {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            onPresetChange(ColorPreset.SYSTEM)
                        }
                    },
                    shapes = ToggleButtonShapes(systemShape, systemShape, systemShape),
                    colors = toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        checkedContainerColor = MaterialTheme.colorScheme.primary,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 40.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedContent(
                            targetState = isSystemSelected,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(140)) +
                                 scaleIn(initialScale = 0.75f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(70)) +
                                        scaleOut(targetScale = 0.75f, animationSpec = tween(70))
                                    )
                            },
                            label = "color_system_icon"
                        ) { checked ->
                            Icon(
                                imageVector = if (checked) Icons.Filled.Check else Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.color_preset_system),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                }
            }

            itemsIndexed(colorPresets, key = { _, item -> item.preset }) { index, item ->
                val isChecked = currentPreset == item.preset
                val isLast = index == colorPresets.size - 1

                val startR by animateDpAsState(
                    targetValue = if (isChecked) 20.dp else 6.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "color_startR_$index"
                )
                val endR by animateDpAsState(
                    targetValue = if (isChecked || isLast) 20.dp else 6.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "color_endR_$index"
                )
                val itemShape = RoundedCornerShape(
                    topStart = startR,
                    bottomStart = startR,
                    topEnd = endR,
                    bottomEnd = endR
                )

                ToggleButton(
                    checked = isChecked,
                    onCheckedChange = {
                        if (!isChecked) {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            onPresetChange(item.preset)
                        }
                    },
                    shapes = ToggleButtonShapes(itemShape, itemShape, itemShape),
                    colors = toggleButtonColors(
                        containerColor = item.containerColor,
                        contentColor = item.contentColor,
                        checkedContainerColor = item.checkedContainerColor,
                        checkedContentColor = item.checkedContentColor
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 40.dp)
                ) {
                    AnimatedContent(
                        targetState = isChecked,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(150, delayMillis = 50)) +
                             scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(70)) +
                                    scaleOut(targetScale = 0.85f, animationSpec = tween(70))
                                )
                                .using(
                                    SizeTransform(clip = false) { _, _ ->
                                        spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    }
                                )
                        },
                        label = "color_preset_content_${item.preset.name}"
                    ) { checked ->
                        if (checked) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(item.nameRes),
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class AppIconOption(
    val preset: AppIconPreset,
    val nameRes: Int,
    val drawableRes: Int
)

@Composable
fun AppIconSelector(
    currentIcon: AppIconPreset,
    onIconChange: (AppIconPreset) -> Unit
) {
    val options = listOf(
        AppIconOption(AppIconPreset.DEFAULT, R.string.app_icon_classic, R.drawable.ic_launcher_classic),
        AppIconOption(AppIconPreset.DARK, R.string.app_icon_dark, R.drawable.ic_launcher_dark),
        AppIconOption(AppIconPreset.SUNSET, R.string.app_icon_sunset, R.drawable.ic_launcher_sunset),
        AppIconOption(AppIconPreset.CREAM, R.string.app_icon_cream, R.drawable.ic_launcher_cream),
        AppIconOption(AppIconPreset.NEON, R.string.app_icon_neon, R.drawable.ic_launcher_neon),
        AppIconOption(AppIconPreset.GREEN, R.string.app_icon_organic, R.drawable.ic_launcher_green),
        AppIconOption(AppIconPreset.NIGHT, R.string.app_icon_midnight, R.drawable.ic_launcher_night),
        AppIconOption(AppIconPreset.MONOCHROME, R.string.app_icon_monochrome, R.drawable.ic_launcher_monochrome_variant)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val chunkedOptions = options.chunked(4)
        chunkedOptions.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowItems.forEach { option ->
                    val isSelected = currentIcon == option.preset
                    
                    val scale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                        label = "icon_scale_${option.preset.name}"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .scale(scale)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onIconChange(option.preset) }
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .border(
                                    border = BorderStroke(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    ),
                                    shape = CircleShape
                                )
                                .padding(if (isSelected) 4.dp else 0.dp)
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = option.drawableRes),
                                contentDescription = stringResource(id = option.nameRes),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(id = option.nameRes),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
                if (rowItems.size < 4) {
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

