package com.smokingtracker.ui

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Speed
import com.smokingtracker.ui.theme.containerBorder
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.containerPadding
import com.smokingtracker.ui.theme.containerGroupGap
import com.smokingtracker.ui.theme.LocalContainerStyle
import com.smokingtracker.ui.theme.ContainerGroupPosition
import com.smokingtracker.data.ContainerStyle
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import com.smokingtracker.widget.QuickAddWidgetProvider
import com.smokingtracker.widget.TimerWidgetProvider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smokingtracker.MainViewModel
import com.smokingtracker.UpdateCheckState
import com.smokingtracker.R
import com.smokingtracker.data.FontPreset
import com.smokingtracker.data.ThemePreference
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.SystemUpdate
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreen(
    viewModel: MainViewModel,
    onNavigateToAbout: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToHistoryGenerator: () -> Unit = {}
) {
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val fontPreset by viewModel.fontPreset.collectAsStateWithLifecycle()
    val packPrice by viewModel.packPrice.collectAsStateWithLifecycle()
    val packSize by viewModel.packSize.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val checkUpdatesOnStart by viewModel.checkUpdatesOnStart.collectAsStateWithLifecycle()
    val updateCheckState by viewModel.updateCheckState.collectAsStateWithLifecycle()
    val taperingPlanEnabled by viewModel.taperingPlanEnabled.collectAsStateWithLifecycle()
    val taperingIntervalDays by viewModel.taperingIntervalDays.collectAsStateWithLifecycle()
    val hasHistoricalBaseline by viewModel.hasHistoricalBaseline.collectAsStateWithLifecycle()

    PersonalScreenContent(
        themePreference = themePreference,
        dailyLimit = dailyLimit,
        fontPreset = fontPreset,
        packPrice = packPrice,
        packSize = packSize,
        currency = currency,
        checkUpdatesOnStart = checkUpdatesOnStart,
        updateCheckState = updateCheckState,
        taperingPlanEnabled = taperingPlanEnabled,
        taperingIntervalDays = taperingIntervalDays,
        onSetTaperingPlanSettings = viewModel::setTaperingPlanSettings,
        onThemeChange = viewModel::updateThemePreference,
        onSetDailyLimit = viewModel::setDailyLimit,
        onFontPresetChange = viewModel::updateFontPreset,
        onUpdatePackDetails = viewModel::updatePackDetails,
        onCheckUpdatesOnStartChange = viewModel::updateCheckUpdatesOnStart,
        onCheckForUpdates = { viewModel.checkForUpdates(isManual = true) },
        onResetUpdateCheckState = viewModel::resetUpdateCheckState,
        onBackupData = { uri, onSuccess, onError -> viewModel.backupData(uri, onSuccess, onError) },
        onRestoreData = { uri, onSuccess, onError -> viewModel.restoreData(uri, onSuccess, onError) },
        onRecordLanguageChange = { tag -> viewModel.recordLanguageChange(tag) },
        onNavigateToAbout = onNavigateToAbout,
        onNavigateToAchievements = onNavigateToAchievements,
        onNavigateToStatistics = onNavigateToStatistics,
        onNavigateToAppearance = onNavigateToAppearance,
        hasHistoricalBaseline = hasHistoricalBaseline,
        onNavigateToHistoryGenerator = onNavigateToHistoryGenerator,
        onClearHistoricalBaseline = viewModel::clearHistoricalBaseline
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreenContent(
    themePreference: ThemePreference,
    dailyLimit: Int,
    fontPreset: FontPreset,
    packPrice: Float,
    packSize: Int,
    currency: String,
    checkUpdatesOnStart: Boolean,
    updateCheckState: UpdateCheckState,
    taperingPlanEnabled: Boolean = false,
    taperingIntervalDays: Int = 7,
    hasHistoricalBaseline: Boolean = false,
    onSetTaperingPlanSettings: (Boolean, Int) -> Unit = { _, _ -> },
    onThemeChange: (ThemePreference) -> Unit,
    onSetDailyLimit: (Int) -> Unit,
    onFontPresetChange: (FontPreset) -> Unit,
    onUpdatePackDetails: (Float, Int, String) -> Unit,
    onCheckUpdatesOnStartChange: (Boolean) -> Unit,
    onCheckForUpdates: () -> Unit,
    onResetUpdateCheckState: () -> Unit,
    onBackupData: (android.net.Uri, () -> Unit, () -> Unit) -> Unit,
    onRestoreData: (android.net.Uri, () -> Unit, () -> Unit) -> Unit,
    onRecordLanguageChange: (String) -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToHistoryGenerator: () -> Unit = {},
    onClearHistoricalBaseline: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.personal_title),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
            )
        }
    ) { paddingValues ->
        SettingsTab(
            modifier = Modifier.padding(paddingValues),
            currentTheme = themePreference,
            dailyLimit = dailyLimit,
            currentFontPreset = fontPreset,
            packPrice = packPrice,
            packSize = packSize,
            currency = currency,
            checkUpdatesOnStart = checkUpdatesOnStart,
            updateCheckState = updateCheckState,
            taperingPlanEnabled = taperingPlanEnabled,
            taperingIntervalDays = taperingIntervalDays,
            onSetTaperingPlanSettings = onSetTaperingPlanSettings,
            onThemeChange = onThemeChange,
            onSetDailyLimit = onSetDailyLimit,
            onFontPresetChange = onFontPresetChange,
            onUpdatePackDetails = onUpdatePackDetails,
            onCheckUpdatesOnStartChange = onCheckUpdatesOnStartChange,
            onCheckForUpdates = onCheckForUpdates,
            onResetUpdateCheckState = onResetUpdateCheckState,
            onBackupData = onBackupData,
            onRestoreData = onRestoreData,
            onRecordLanguageChange = onRecordLanguageChange,
            onNavigateToAbout = onNavigateToAbout,
            onNavigateToAchievements = onNavigateToAchievements,
            onNavigateToStatistics = onNavigateToStatistics,
            onNavigateToAppearance = onNavigateToAppearance,
            hasHistoricalBaseline = hasHistoricalBaseline,
            onNavigateToHistoryGenerator = onNavigateToHistoryGenerator,
            onClearHistoricalBaseline = onClearHistoricalBaseline
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    modifier: Modifier = Modifier,
    currentTheme: ThemePreference,
    dailyLimit: Int,
    currentFontPreset: FontPreset,
    packPrice: Float,
    packSize: Int,
    currency: String,
    checkUpdatesOnStart: Boolean,
    updateCheckState: UpdateCheckState,
    taperingPlanEnabled: Boolean = false,
    taperingIntervalDays: Int = 7,
    hasHistoricalBaseline: Boolean = false,
    onSetTaperingPlanSettings: (Boolean, Int) -> Unit = { _, _ -> },
    onThemeChange: (ThemePreference) -> Unit,
    onSetDailyLimit: (Int) -> Unit,
    onFontPresetChange: (FontPreset) -> Unit,
    onUpdatePackDetails: (Float, Int, String) -> Unit,
    onCheckUpdatesOnStartChange: (Boolean) -> Unit,
    onCheckForUpdates: () -> Unit,
    onResetUpdateCheckState: () -> Unit,
    onBackupData: (android.net.Uri, () -> Unit, () -> Unit) -> Unit,
    onRestoreData: (android.net.Uri, () -> Unit, () -> Unit) -> Unit,
    onRecordLanguageChange: (String) -> Unit = {},
    onNavigateToAbout: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToHistoryGenerator: () -> Unit = {},
    onClearHistoricalBaseline: () -> Unit = {}
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var showPackDialog by remember { mutableStateOf(false) }
    var showWidgetDialog by remember { mutableStateOf(false) }
    var showBackupSheet by remember { mutableStateOf(false) }
    var showRestoreSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val checkingStr = stringResource(R.string.update_checking)
    val noUpdatesStr = stringResource(R.string.update_no_updates)
    val errorStr = stringResource(R.string.update_error)

    LaunchedEffect(updateCheckState) {
        when (val state = updateCheckState) {
            is UpdateCheckState.Checking -> {
                Toast.makeText(context, checkingStr, Toast.LENGTH_SHORT).show()
            }
            is UpdateCheckState.NoUpdate -> {
                Toast.makeText(context, noUpdatesStr, Toast.LENGTH_SHORT).show()
                onResetUpdateCheckState()
            }
            is UpdateCheckState.Error -> {
                Toast.makeText(context, String.format(errorStr, state.message), Toast.LENGTH_LONG).show()
                onResetUpdateCheckState()
            }
            else -> {}
        }
    }

    val currentLocale = LocalConfiguration.current.locales.get(0)?.language ?: Locale.getDefault().language

    val backupSuccessStr = stringResource(R.string.backup_success)
    val backupErrorStr = stringResource(R.string.backup_error)
    val restoreSuccessStr = stringResource(R.string.restore_success)
    val restoreErrorStr = stringResource(R.string.restore_error)
    val savedStr = stringResource(R.string.settings_saved)

    if (showBackupSheet) {
        BackupBottomSheet(
            onDismissRequest = { showBackupSheet = false },
            onBackupData = { uri, onSuccess, onError ->
                onBackupData(
                    uri,
                    {
                        Toast.makeText(context, backupSuccessStr, Toast.LENGTH_SHORT).show()
                        onSuccess()
                    },
                    {
                        Toast.makeText(context, backupErrorStr, Toast.LENGTH_SHORT).show()
                        onError()
                    }
                )
            }
        )
    }

    if (showRestoreSheet) {
        RestoreBottomSheet(
            onDismissRequest = { showRestoreSheet = false },
            onRestoreData = { uri, onSuccess, onError ->
                onRestoreData(
                    uri,
                    {
                        Toast.makeText(context, restoreSuccessStr, Toast.LENGTH_SHORT).show()
                        onSuccess()
                    },
                    {
                        Toast.makeText(context, restoreErrorStr, Toast.LENGTH_SHORT).show()
                        onError()
                    }
                )
            }
        )
    }

    if (showLanguageDialog) {
        val languages = listOf(
            "en" to "English",
            "ru" to "Русский",
            "es" to "Español",
            "de" to "Deutsch",
            "fr" to "Français",
            "tr" to "Türkçe",
            "it" to "Italiano",
            "pt" to "Português",
            "uk" to "Українська"
        )
        ModalBottomSheet(
            onDismissRequest = { showLanguageDialog = false },
            sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.language_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                languages.forEachIndexed { index, (langCode, langName) ->
                    val groupPosition = when (index) {
                        0 -> ContainerGroupPosition.FIRST
                        languages.lastIndex -> ContainerGroupPosition.LAST
                        else -> ContainerGroupPosition.MIDDLE
                    }
                    val shape = containerShape(
                        when (index) {
                            0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                            languages.lastIndex -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                            else -> RoundedCornerShape(8.dp)
                        },
                        groupPosition
                    )
                    val isSelected = currentLocale == langCode

                    Surface(
                        onClick = {
                            changeLanguage(context, langCode)
                            onRecordLanguageChange(langCode)
                            showLanguageDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = shape,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = langName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (index < languages.lastIndex) {
                        Spacer(modifier = Modifier.height(containerGroupGap()))
                    }
                }
            }
        }
    }

    if (showLimitDialog) {
        var newLimit by remember { mutableStateOf(dailyLimit.toString()) }
        val limitValid = newLimit.isEmpty() || newLimit.toIntOrNull() != null

        BasicAlertDialog(onDismissRequest = { showLimitDialog = false }) {
            Surface(shape = containerShape(RoundedCornerShape(28.dp)), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        stringResource(R.string.set_limit_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newLimit,
                        onValueChange = { newLimit = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.set_limit_title)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = !limitValid,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        supportingText = {
                            Text(stringResource(R.string.limit_dialog_helper))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(0, 5, 10, 15, 20).forEach { preset ->
                            val isSelected = newLimit.toIntOrNull() == preset
                            Surface(
                                onClick = { newLimit = preset.toString() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = containerShape(RoundedCornerShape(12.dp)),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                border = containerBorder(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = preset.toString(),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showLimitDialog = false }) {
                            Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSetDailyLimit(newLimit.toIntOrNull() ?: 0)
                                Toast.makeText(context, savedStr, Toast.LENGTH_SHORT).show()
                                showLimitDialog = false
                            },
                            enabled = limitValid
                        ) {
                            Text(stringResource(R.string.dialog_ok), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showPackDialog) {
        var priceInput by remember { mutableStateOf(if (packPrice > 0f) packPrice.toString() else "") }
        var sizeInput by remember { mutableStateOf(packSize.toString()) }
        var selectedCurrency by remember { mutableStateOf(currency) }
        val priceValid = priceInput.isEmpty() || priceInput.toFloatOrNull() != null

        BasicAlertDialog(onDismissRequest = { showPackDialog = false }) {
            Surface(shape = containerShape(RoundedCornerShape(28.dp)), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        stringResource(R.string.settings_pack_params),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_pack_price)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = !priceValid,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        supportingText = if (!priceValid) {
                            { Text(stringResource(R.string.error_invalid_price)) }
                        } else null
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = sizeInput,
                        onValueChange = { sizeInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_pack_size)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                                )
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.settings_currency), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf("USD", "EUR", "RUB", "GBP", "TRY", "KZT", "UAH")) { curr ->
                            val isSelected = selectedCurrency == curr
                            Surface(
                                onClick = { selectedCurrency = curr },
                                modifier = Modifier.height(36.dp),
                                shape = containerShape(RoundedCornerShape(12.dp)),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                border = containerBorder(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = curr,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showPackDialog = false }) {
                            Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = priceValid,
                            onClick = {
                                val price = priceInput.toFloatOrNull() ?: 0.0f
                                val size = sizeInput.toIntOrNull() ?: 20
                                onUpdatePackDetails(price, size, selectedCurrency)
                                Toast.makeText(context, savedStr, Toast.LENGTH_SHORT).show()
                                showPackDialog = false
                            }
                        ) {
                            Text(stringResource(R.string.dialog_ok), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showWidgetDialog) {
        WidgetPinDialog(onDismiss = { showWidgetDialog = false })
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(containerGroupGap())
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_section_interface),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, start = 8.dp)
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.Brightness4,
                title = stringResource(R.string.settings_appearance),
                subtitle = stringResource(R.string.settings_appearance_desc),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                groupPosition = ContainerGroupPosition.FIRST,
                onClick = onNavigateToAppearance
            )
        }
        item {
            val locale = LocalConfiguration.current.locales.get(0) ?: java.util.Locale.getDefault()
            val langDisplay = locale.getDisplayLanguage(locale).replaceFirstChar { it.uppercase() }
            SettingItem(
                icon = Icons.Filled.Language,
                title = stringResource(R.string.settings_language),
                subtitle = langDisplay,
                shape = RoundedCornerShape(8.dp),
                groupPosition = ContainerGroupPosition.MIDDLE,
                onClick = { showLanguageDialog = true }
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.Widgets,
                title = stringResource(R.string.settings_widgets_title),
                subtitle = stringResource(R.string.settings_widgets_desc),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                groupPosition = ContainerGroupPosition.LAST,
                onClick = { showWidgetDialog = true }
            )
        }

        item {
            Text(
                text = stringResource(R.string.settings_section_goals),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.Warning,
                title = stringResource(R.string.settings_daily_limit),
                subtitle = if (dailyLimit > 0) dailyLimit.toString() else stringResource(R.string.no_limit),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                groupPosition = ContainerGroupPosition.FIRST,
                onClick = { showLimitDialog = true }
            )
        }
        item {
            var showTaperingDialog by remember { mutableStateOf(false) }

            if (showTaperingDialog) {
                var enabled by remember(taperingPlanEnabled) { mutableStateOf(taperingPlanEnabled) }
                var interval by remember(taperingIntervalDays) { mutableIntStateOf(taperingIntervalDays) }

                ModalBottomSheet(
                    onDismissRequest = { showTaperingDialog = false },
                    sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.tapering_plan_dialog_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.tapering_plan_switch_label),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f).padding(end = 12.dp)
                            )
                            Switch(
                                checked = enabled,
                                onCheckedChange = { enabled = it },
                                thumbContent = { SwitchThumb(enabled) }
                            )
                        }

                        AnimatedVisibility(
                            visible = enabled,
                            enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeIn(),
                            exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    stringResource(R.string.tapering_plan_slider_label, interval),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Slider(
                                    value = interval.toFloat(),
                                    onValueChange = { interval = it.toInt() },
                                    valueRange = 3f..14f,
                                    steps = 10
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { showTaperingDialog = false }) {
                                Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onSetTaperingPlanSettings(enabled, interval)
                                    showTaperingDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.dialog_ok), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            SettingItem(
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                title = stringResource(R.string.tapering_plan_title),
                subtitle = if (taperingPlanEnabled) stringResource(R.string.tapering_plan_subtitle_active, taperingIntervalDays) else stringResource(R.string.tapering_plan_subtitle_off),
                shape = RoundedCornerShape(8.dp),
                groupPosition = ContainerGroupPosition.MIDDLE,
                onClick = { showTaperingDialog = true }
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.AttachMoney,
                title = stringResource(R.string.settings_pack_params),
                subtitle = if (packPrice > 0f) {
                    stringResource(R.string.settings_pack_subtitle_pattern, packPrice.toString(), currency, packSize)
                } else {
                    stringResource(R.string.settings_tap_configure)
                },
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                groupPosition = ContainerGroupPosition.LAST,
                onClick = { showPackDialog = true }
            )
        }

        item {
            Text(
                text = stringResource(R.string.settings_data),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.Backup,
                title = stringResource(R.string.settings_backup),
                subtitle = stringResource(R.string.backup_desc),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                groupPosition = ContainerGroupPosition.FIRST,
                onClick = { showBackupSheet = true }
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.Restore,
                title = stringResource(R.string.settings_restore),
                subtitle = stringResource(R.string.restore_desc),
                shape = RoundedCornerShape(8.dp),
                groupPosition = ContainerGroupPosition.MIDDLE,
                onClick = { showRestoreSheet = true }
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.AutoAwesome,
                title = stringResource(R.string.history_generator_title),
                subtitle = if (hasHistoricalBaseline) stringResource(R.string.history_preview_title) else stringResource(R.string.history_banner_desc),
                shape = if (hasHistoricalBaseline) RoundedCornerShape(8.dp) else RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                groupPosition = if (hasHistoricalBaseline) ContainerGroupPosition.MIDDLE else ContainerGroupPosition.LAST,
                onClick = onNavigateToHistoryGenerator
            )
        }
        if (hasHistoricalBaseline) {
            item {
                var showResetDialog by remember { mutableStateOf(false) }

                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text(stringResource(R.string.history_reset_dialog_title)) },
                        text = { Text(stringResource(R.string.history_reset_dialog_desc)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    onClearHistoricalBaseline()
                                    showResetDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(stringResource(R.string.history_reset_button))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetDialog = false }) {
                                Text(stringResource(R.string.dialog_cancel))
                            }
                        }
                    )
                }

                SettingItem(
                    icon = Icons.Filled.Delete,
                    title = stringResource(R.string.history_reset_button),
                    subtitle = stringResource(R.string.history_reset_dialog_desc),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                    groupPosition = ContainerGroupPosition.LAST,
                    onClick = { showResetDialog = true }
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.settings_info),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.about_app),
                subtitle = stringResource(R.string.about_app_desc),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                groupPosition = ContainerGroupPosition.FIRST,
                onClick = onNavigateToAbout
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.SystemUpdate,
                title = stringResource(R.string.settings_check_updates),
                subtitle = stringResource(R.string.settings_check_updates_desc),
                shape = RoundedCornerShape(8.dp),
                groupPosition = ContainerGroupPosition.MIDDLE,
                onClick = onCheckForUpdates
            )
        }
        item {
            SettingItemWithSwitch(
                icon = Icons.Filled.SystemUpdate,
                title = stringResource(R.string.settings_check_updates_on_start),
                subtitle = stringResource(R.string.settings_check_updates_on_start_desc),
                checked = checkUpdatesOnStart,
                onCheckedChange = onCheckUpdatesOnStartChange,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                groupPosition = ContainerGroupPosition.LAST
            )
        }
    }
}

@Composable
fun SettingItemWithSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(28.dp),
    groupPosition: ContainerGroupPosition = ContainerGroupPosition.SINGLE
) {
    val isStandardStyle = LocalContainerStyle.current == ContainerStyle.STANDARD
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape(shape, groupPosition),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = containerBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(containerPadding(20.dp, 18.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isStandardStyle) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = { SwitchThumb(checked) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun SwitchThumb(checked: Boolean) {
    AnimatedContent(
        targetState = checked,
        transitionSpec = {
            scaleIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) togetherWith
                    scaleOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
        },
        label = "switch_thumb_icon"
    ) { isChecked ->
        Icon(
            imageVector = if (isChecked) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            modifier = Modifier.size(SwitchDefaults.IconSize)
        )
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(28.dp),
    groupPosition: ContainerGroupPosition = ContainerGroupPosition.SINGLE,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape(shape, groupPosition),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = containerBorder()
    ) {
        SettingItemContent(icon = icon, title = title, subtitle = subtitle, onClick = onClick)
    }
}

@Composable
fun SettingItemContent(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    val isStandardStyle = LocalContainerStyle.current == ContainerStyle.STANDARD
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(containerPadding(20.dp, 18.dp))

    Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
        if (!isStandardStyle) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

fun changeLanguage(context: android.content.Context, languageTag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java)
            .applicationLocales = LocaleList.forLanguageTags(languageTag)
    } else {
        val locale = java.util.Locale.forLanguageTag(languageTag)
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        val activity = context as? android.app.Activity
        activity?.recreate()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPinDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    fun requestPin(providerClass: Class<*>) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            val myProvider = ComponentName(context, providerClass)
            appWidgetManager.requestPinAppWidget(myProvider, null, null)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.widget_pin_unsupported_toast),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_widgets_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.settings_widgets_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = containerShape(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.widget_quick_add_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                requestPin(QuickAddWidgetProvider::class.java)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.widget_pin_button_1x1),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.widget_quick_add_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = containerShape(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.widget_timer_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                requestPin(TimerWidgetProvider::class.java)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.widget_pin_button_3x1),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.widget_timer_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
