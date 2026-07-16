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
import com.smokingtracker.MainViewModel.UpdateCheckState
import com.smokingtracker.R
import com.smokingtracker.data.ThemePreference
import androidx.compose.material.icons.filled.SystemUpdate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreen(
    viewModel: MainViewModel,
    onNavigateToAbout: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAppearance: () -> Unit
) {
    val themePreference by viewModel.themePreference.collectAsState()
    val dailyLimit by viewModel.dailyLimit.collectAsState()
    val fontPreset by viewModel.fontPreset.collectAsState()
    val packPrice by viewModel.packPrice.collectAsState()
    val packSize by viewModel.packSize.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val checkUpdatesOnStart by viewModel.checkUpdatesOnStart.collectAsState()
    val updateCheckState by viewModel.updateCheckState.collectAsState()

    PersonalScreenContent(
        themePreference = themePreference,
        dailyLimit = dailyLimit,
        fontPreset = fontPreset,
        packPrice = packPrice,
        packSize = packSize,
        currency = currency,
        checkUpdatesOnStart = checkUpdatesOnStart,
        updateCheckState = updateCheckState,
        onThemeChange = viewModel::updateThemePreference,
        onSetDailyLimit = viewModel::setDailyLimit,
        onFontPresetChange = viewModel::updateFontPreset,
        onUpdatePackDetails = viewModel::updatePackDetails,
        onCheckUpdatesOnStartChange = viewModel::updateCheckUpdatesOnStart,
        onCheckForUpdates = { viewModel.checkForUpdates(isManual = true) },
        onResetUpdateCheckState = viewModel::resetUpdateCheckState,
        onBackupData = { uri, onSuccess, onError -> viewModel.backupData(uri, onSuccess, onError) },
        onRestoreData = { uri, onSuccess, onError -> viewModel.restoreData(uri, onSuccess, onError) },
        onNavigateToAbout = onNavigateToAbout,
        onNavigateToAchievements = onNavigateToAchievements,
        onNavigateToStatistics = onNavigateToStatistics,
        onNavigateToAppearance = onNavigateToAppearance
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreenContent(
    themePreference: ThemePreference,
    dailyLimit: Int,
    fontPreset: String,
    packPrice: Float,
    packSize: Int,
    currency: String,
    checkUpdatesOnStart: Boolean,
    updateCheckState: UpdateCheckState,
    onThemeChange: (ThemePreference) -> Unit,
    onSetDailyLimit: (Int) -> Unit,
    onFontPresetChange: (String) -> Unit,
    onUpdatePackDetails: (Float, Int, String) -> Unit,
    onCheckUpdatesOnStartChange: (Boolean) -> Unit,
    onCheckForUpdates: () -> Unit,
    onResetUpdateCheckState: () -> Unit,
    onBackupData: (android.net.Uri, () -> Unit, () -> Unit) -> Unit,
    onRestoreData: (android.net.Uri, () -> Unit, () -> Unit) -> Unit,
    onNavigateToAbout: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {}
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            onThemeChange = onThemeChange,
            onSetDailyLimit = onSetDailyLimit,
            onFontPresetChange = onFontPresetChange,
            onUpdatePackDetails = onUpdatePackDetails,
            onCheckUpdatesOnStartChange = onCheckUpdatesOnStartChange,
            onCheckForUpdates = onCheckForUpdates,
            onResetUpdateCheckState = onResetUpdateCheckState,
            onBackupData = onBackupData,
            onRestoreData = onRestoreData,
            onNavigateToAbout = onNavigateToAbout,
            onNavigateToAchievements = onNavigateToAchievements,
            onNavigateToStatistics = onNavigateToStatistics,
            onNavigateToAppearance = onNavigateToAppearance
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    modifier: Modifier = Modifier,
    currentTheme: ThemePreference,
    dailyLimit: Int,
    currentFontPreset: String,
    packPrice: Float,
    packSize: Int,
    currency: String,
    checkUpdatesOnStart: Boolean,
    updateCheckState: UpdateCheckState,
    onThemeChange: (ThemePreference) -> Unit,
    onSetDailyLimit: (Int) -> Unit,
    onFontPresetChange: (String) -> Unit,
    onUpdatePackDetails: (Float, Int, String) -> Unit,
    onCheckUpdatesOnStartChange: (Boolean) -> Unit,
    onCheckForUpdates: () -> Unit,
    onResetUpdateCheckState: () -> Unit,
    onBackupData: (android.net.Uri, () -> Unit, () -> Unit) -> Unit,
    onRestoreData: (android.net.Uri, () -> Unit, () -> Unit) -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAppearance: () -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var showPackDialog by remember { mutableStateOf(false) }

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

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            onBackupData(
                it,
                { Toast.makeText(context, backupSuccessStr, Toast.LENGTH_SHORT).show() },
                { Toast.makeText(context, backupErrorStr, Toast.LENGTH_SHORT).show() }
            )
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onRestoreData(
                it,
                { Toast.makeText(context, restoreSuccessStr, Toast.LENGTH_SHORT).show() },
                { Toast.makeText(context, restoreErrorStr, Toast.LENGTH_SHORT).show() }
            )
        }
    }

    if (showLanguageDialog) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageDialog = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.language_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Surface(
                    onClick = {
                        changeLanguage(context, "en")
                        showLanguageDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = if (currentLocale == "en") MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "English",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f),
                            color = if (currentLocale == "en") MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        if (currentLocale == "en") {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    onClick = {
                        changeLanguage(context, "ru")
                        showLanguageDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                    color = if (currentLocale == "ru") MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Русский",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f),
                            color = if (currentLocale == "ru") MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        if (currentLocale == "ru") {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showLimitDialog) {
        var newLimit by remember { mutableStateOf(dailyLimit.toString()) }
        BasicAlertDialog(onDismissRequest = { showLimitDialog = false }) {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
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
                        label = { Text(stringResource(R.string.no_limit)) },
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showLimitDialog = false }) {
                            Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            onSetDailyLimit(newLimit.toIntOrNull() ?: 0)
                            Toast.makeText(context, savedStr, Toast.LENGTH_SHORT).show()
                            showLimitDialog = false
                        }) {
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
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
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
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = !priceValid,
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
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.settings_currency), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("USD", "EUR", "RUB", "GBP", "TRY", "KZT", "UAH")) { curr ->
                            FilterChip(
                                selected = selectedCurrency == curr,
                                onClick = { selectedCurrency = curr },
                                label = { Text(curr) }
                            )
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_general),
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
                onClick = { showLanguageDialog = true }
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.EmojiEvents,
                title = stringResource(R.string.settings_achievements),
                subtitle = stringResource(R.string.achievements_desc),
                shape = RoundedCornerShape(8.dp),
                onClick = onNavigateToAchievements
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.Warning,
                title = stringResource(R.string.settings_daily_limit),
                subtitle = if (dailyLimit > 0) dailyLimit.toString() else stringResource(R.string.no_limit),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                onClick = { showLimitDialog = true }
            )
        }

        item {
            Text(
                text = stringResource(R.string.settings_cigarette_params),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
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
                shape = RoundedCornerShape(24.dp),
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
                onClick = { backupLauncher.launch("smoking_tracker_backup.json") }
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.Restore,
                title = stringResource(R.string.settings_restore),
                subtitle = stringResource(R.string.restore_desc),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                onClick = { restoreLauncher.launch(arrayOf("application/json")) }
            )
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
                onClick = onNavigateToAbout
            )
        }
        item {
            SettingItem(
                icon = Icons.Filled.SystemUpdate,
                title = stringResource(R.string.settings_check_updates),
                subtitle = stringResource(R.string.settings_check_updates_desc),
                shape = RoundedCornerShape(8.dp),
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
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
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
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(28.dp)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(28.dp),
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
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
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 20.dp, vertical = 18.dp)

    Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
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
