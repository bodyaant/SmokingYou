package com.smokingtracker.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smokingtracker.BackupRestoreEvent
import com.smokingtracker.BackupRestoreUiState
import com.smokingtracker.R
import com.smokingtracker.SettingsViewModel
import com.smokingtracker.UpdateCheckState
import com.smokingtracker.data.FontPreset
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.ui.settings.SettingsTab
import com.smokingtracker.ui.settings.components.SettingItem as NewSettingItem
import com.smokingtracker.ui.settings.components.SettingItemContent as NewSettingItemContent
import com.smokingtracker.ui.settings.components.SettingItemWithSwitch as NewSettingItemWithSwitch
import com.smokingtracker.ui.settings.components.SwitchThumb as NewSwitchThumb
import com.smokingtracker.ui.theme.ContainerGroupPosition
import com.smokingtracker.util.LocaleHelper
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onNavigateToAbout: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAppearance: () -> Unit
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
    val historicalStartDate by viewModel.historicalStartDate.collectAsStateWithLifecycle()
    val historicalDailyAvg by viewModel.historicalDailyAvg.collectAsStateWithLifecycle()
    val customTriggers by viewModel.customTriggers.collectAsStateWithLifecycle()
    val disabledDefaultTriggers by viewModel.disabledDefaultTriggers.collectAsStateWithLifecycle()
    val ongoingNotificationEnabled by viewModel.ongoingNotificationEnabled.collectAsStateWithLifecycle()
    val notificationLowPriority by viewModel.notificationLowPriority.collectAsStateWithLifecycle()
    val notificationShowTimer by viewModel.notificationShowTimer.collectAsStateWithLifecycle()
    val notificationShowProgress by viewModel.notificationShowProgress.collectAsStateWithLifecycle()
    val notificationShowAddButton by viewModel.notificationShowAddButton.collectAsStateWithLifecycle()
    val notificationShowResistButton by viewModel.notificationShowResistButton.collectAsStateWithLifecycle()
    val widgetShowResistButton by viewModel.widgetShowResistButton.collectAsStateWithLifecycle()
    val showLimitOnGraph by viewModel.showLimitOnGraph.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val backupOperationState by viewModel.backupOperationState.collectAsStateWithLifecycle()
    val restoreOperationState by viewModel.restoreOperationState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val backupSuccessStr = stringResource(R.string.backup_success)
    val backupErrorStr = stringResource(R.string.backup_error)
    val restoreSuccessStr = stringResource(R.string.restore_success)
    val restoreErrorStr = stringResource(R.string.restore_error)

    LaunchedEffect(viewModel) {
        viewModel.backupRestoreEvents.collect { event ->
            when (event) {
                is BackupRestoreEvent.BackupSuccess -> {
                    Toast.makeText(context, backupSuccessStr, Toast.LENGTH_SHORT).show()
                }
                is BackupRestoreEvent.BackupError -> {
                    val detail = event.message?.takeIf { it.isNotBlank() }
                    val msg = if (detail != null) "$backupErrorStr: $detail" else backupErrorStr
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
                is BackupRestoreEvent.RestoreSuccess -> {
                    Toast.makeText(context, restoreSuccessStr, Toast.LENGTH_SHORT).show()
                }
                is BackupRestoreEvent.RestoreError -> {
                    val detail = event.message?.takeIf { it.isNotBlank() }
                    val msg = if (detail != null) "$restoreErrorStr: $detail" else restoreErrorStr
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
        customTriggers = customTriggers,
        disabledDefaultTriggers = disabledDefaultTriggers,
        ongoingNotificationEnabled = ongoingNotificationEnabled,
        notificationLowPriority = notificationLowPriority,
        notificationShowTimer = notificationShowTimer,
        notificationShowProgress = notificationShowProgress,
        notificationShowAddButton = notificationShowAddButton,
        notificationShowResistButton = notificationShowResistButton,
        widgetShowResistButton = widgetShowResistButton,
        vibrationEnabled = vibrationEnabled,
        backupOperationState = backupOperationState,
        restoreOperationState = restoreOperationState,
        onResetBackupState = viewModel::resetBackupOperationState,
        onResetRestoreState = viewModel::resetRestoreOperationState,
        onAddCustomTrigger = viewModel::addCustomTrigger,
        onRemoveCustomTrigger = viewModel::removeCustomTrigger,
        onToggleDefaultTrigger = viewModel::toggleDefaultTrigger,
        onSetTaperingPlanSettings = viewModel::setTaperingPlanSettings,
        onOngoingNotificationEnabledChange = viewModel::updateOngoingNotificationEnabled,
        onNotificationLowPriorityChange = viewModel::updateNotificationLowPriority,
        onNotificationShowTimerChange = viewModel::updateNotificationShowTimer,
        onNotificationShowProgressChange = viewModel::updateNotificationShowProgress,
        onNotificationShowAddButtonChange = viewModel::updateNotificationShowAddButton,
        onNotificationShowResistButtonChange = viewModel::updateNotificationShowResistButton,
        onWidgetShowResistButtonChange = viewModel::updateWidgetShowResistButton,
        onThemeChange = viewModel::updateThemePreference,
        showLimitOnGraph = showLimitOnGraph,
        onSetShowLimitOnGraph = viewModel::setShowLimitOnGraph,
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
        historicalStartDate = historicalStartDate,
        historicalDailyAvg = historicalDailyAvg,
        onSaveBaseline = viewModel::saveBaseline,
        onClearBaseline = viewModel::clearHistoricalBaseline,
        onResetAllData = viewModel::resetAllSmokingData
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
    customTriggers: List<String> = emptyList(),
    disabledDefaultTriggers: Set<String> = emptySet(),
    ongoingNotificationEnabled: Boolean = false,
    notificationLowPriority: Boolean = true,
    notificationShowTimer: Boolean = true,
    notificationShowProgress: Boolean = true,
    notificationShowAddButton: Boolean = true,
    notificationShowResistButton: Boolean = false,
    widgetShowResistButton: Boolean = true,
    showLimitOnGraph: Boolean = true,
    vibrationEnabled: Boolean = true,
    backupOperationState: BackupRestoreUiState = BackupRestoreUiState.Idle,
    restoreOperationState: BackupRestoreUiState = BackupRestoreUiState.Idle,
    onResetBackupState: () -> Unit = {},
    onResetRestoreState: () -> Unit = {},
    onAddCustomTrigger: (String, (String?) -> Unit) -> Unit = { _, _ -> },
    onRemoveCustomTrigger: (String) -> Unit = {},
    onToggleDefaultTrigger: (String, Boolean) -> Unit = { _, _ -> },
    onSetTaperingPlanSettings: (Boolean, Int) -> Unit = { _, _ -> },
    onSetShowLimitOnGraph: (Boolean) -> Unit = {},
    onOngoingNotificationEnabledChange: (Boolean) -> Unit = {},
    onNotificationLowPriorityChange: (Boolean) -> Unit = {},
    onNotificationShowTimerChange: (Boolean) -> Unit = {},
    onNotificationShowProgressChange: (Boolean) -> Unit = {},
    onNotificationShowAddButtonChange: (Boolean) -> Unit = {},
    onNotificationShowResistButtonChange: (Boolean) -> Unit = {},
    onWidgetShowResistButtonChange: (Boolean) -> Unit = {},
    onThemeChange: (ThemePreference) -> Unit = {},
    onSetDailyLimit: (Int) -> Unit = {},
    onFontPresetChange: (FontPreset) -> Unit = {},
    onUpdatePackDetails: (Float, Int, String) -> Unit = { _, _, _ -> },
    onCheckUpdatesOnStartChange: (Boolean) -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    onResetUpdateCheckState: () -> Unit = {},
    onBackupData: (Uri, () -> Unit, () -> Unit) -> Unit = { _, _, _ -> },
    onRestoreData: (Uri, () -> Unit, () -> Unit) -> Unit = { _, _, _ -> },
    onRecordLanguageChange: (String) -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    hasHistoricalBaseline: Boolean = false,
    historicalStartDate: Long = 0L,
    historicalDailyAvg: Int = 0,
    onSaveBaseline: (Long, Int) -> Unit = { _, _ -> },
    onClearBaseline: () -> Unit = {},
    onResetAllData: () -> Unit = {}
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
            customTriggers = customTriggers,
            disabledDefaultTriggers = disabledDefaultTriggers,
            ongoingNotificationEnabled = ongoingNotificationEnabled,
            notificationLowPriority = notificationLowPriority,
            notificationShowTimer = notificationShowTimer,
            notificationShowProgress = notificationShowProgress,
            notificationShowAddButton = notificationShowAddButton,
            notificationShowResistButton = notificationShowResistButton,
            widgetShowResistButton = widgetShowResistButton,
            showLimitOnGraph = showLimitOnGraph,
            vibrationEnabled = vibrationEnabled,
            backupOperationState = backupOperationState,
            restoreOperationState = restoreOperationState,
            onResetBackupState = onResetBackupState,
            onResetRestoreState = onResetRestoreState,
            onAddCustomTrigger = onAddCustomTrigger,
            onRemoveCustomTrigger = onRemoveCustomTrigger,
            onToggleDefaultTrigger = onToggleDefaultTrigger,
            onSetTaperingPlanSettings = onSetTaperingPlanSettings,
            onSetShowLimitOnGraph = onSetShowLimitOnGraph,
            onOngoingNotificationEnabledChange = onOngoingNotificationEnabledChange,
            onNotificationLowPriorityChange = onNotificationLowPriorityChange,
            onNotificationShowTimerChange = onNotificationShowTimerChange,
            onNotificationShowProgressChange = onNotificationShowProgressChange,
            onNotificationShowAddButtonChange = onNotificationShowAddButtonChange,
            onNotificationShowResistButtonChange = onNotificationShowResistButtonChange,
            onWidgetShowResistButtonChange = onWidgetShowResistButtonChange,
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
            historicalStartDate = historicalStartDate,
            historicalDailyAvg = historicalDailyAvg,
            onSaveBaseline = onSaveBaseline,
            onClearBaseline = onClearBaseline,
            onResetAllData = onResetAllData
        )
    }
}

fun changeLanguage(context: Context, languageTag: String) {
    LocaleHelper.changeLanguage(context, languageTag)
}

@Composable
fun SettingItemWithSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape = RoundedCornerShape(28.dp),
    groupPosition: ContainerGroupPosition = ContainerGroupPosition.SINGLE
) {
    NewSettingItemWithSwitch(
        icon = icon,
        title = title,
        subtitle = subtitle,
        checked = checked,
        onCheckedChange = onCheckedChange,
        shape = shape,
        groupPosition = groupPosition
    )
}

@Composable
fun SwitchThumb(checked: Boolean) {
    NewSwitchThumb(checked)
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    shape: Shape = RoundedCornerShape(28.dp),
    groupPosition: ContainerGroupPosition = ContainerGroupPosition.SINGLE,
    onClick: (() -> Unit)? = null
) {
    NewSettingItem(
        icon = icon,
        title = title,
        subtitle = subtitle,
        shape = shape,
        groupPosition = groupPosition,
        onClick = onClick
    )
}

@Composable
fun SettingItemContent(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    NewSettingItemContent(
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = onClick
    )
}
