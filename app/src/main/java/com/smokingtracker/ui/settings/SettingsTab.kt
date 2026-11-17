package com.smokingtracker.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smokingtracker.BackupRestoreUiState
import com.smokingtracker.R
import com.smokingtracker.UpdateCheckState
import com.smokingtracker.data.FontPreset
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.ui.BackupBottomSheet
import com.smokingtracker.ui.BaselineBottomSheet
import com.smokingtracker.ui.DailyLimitBottomSheet
import com.smokingtracker.ui.NotificationSettingsBottomSheet
import com.smokingtracker.ui.PackSettingsBottomSheet
import com.smokingtracker.ui.RestoreBottomSheet
import com.smokingtracker.ui.TriggerManagementBottomSheet
import com.smokingtracker.ui.WidgetSettingsBottomSheet
import com.smokingtracker.ui.settings.dialogs.LanguageBottomSheet
import com.smokingtracker.ui.settings.dialogs.ResetDataDialog
import com.smokingtracker.ui.settings.sections.backupSettingsSection
import com.smokingtracker.ui.settings.sections.generalSettingsSection
import com.smokingtracker.ui.settings.sections.infoSettingsSection
import com.smokingtracker.ui.settings.sections.interfaceSettingsSection
import com.smokingtracker.ui.settings.sections.notificationSettingsSection
import com.smokingtracker.ui.theme.containerGroupGap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    modifier: Modifier = Modifier,
    currentTheme: ThemePreference = ThemePreference.SYSTEM,
    dailyLimit: Int,
    currentFontPreset: FontPreset = FontPreset.SYSTEM,
    packPrice: Float,
    packSize: Int,
    currency: String,
    checkUpdatesOnStart: Boolean,
    updateCheckState: UpdateCheckState,
    taperingPlanEnabled: Boolean = false,
    taperingIntervalDays: Int = 7,
    hasHistoricalBaseline: Boolean = false,
    historicalStartDate: Long = 0L,
    historicalDailyAvg: Int = 0,
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
    onSaveBaseline: (Long, Int) -> Unit = { _, _ -> },
    onClearBaseline: () -> Unit = {},
    onResetAllData: () -> Unit = {}
) {
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showLimitDialog by rememberSaveable { mutableStateOf(false) }
    var showPackDialog by rememberSaveable { mutableStateOf(false) }
    var showBaselineSheet by rememberSaveable { mutableStateOf(false) }
    var showWidgetDialog by rememberSaveable { mutableStateOf(false) }
    var showBackupSheet by rememberSaveable { mutableStateOf(false) }
    var showRestoreSheet by rememberSaveable { mutableStateOf(false) }
    var showTriggerManagementSheet by rememberSaveable { mutableStateOf(false) }
    var showNotificationSettingsSheet by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

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

    if (showBackupSheet) {
        BackupBottomSheet(
            onDismissRequest = {
                onResetBackupState()
                showBackupSheet = false
            },
            operationState = backupOperationState,
            onResetOperationState = onResetBackupState,
            onBackupData = { uri, onSuccess, onError ->
                onBackupData(uri, onSuccess, onError)
            }
        )
    }

    if (showRestoreSheet) {
        RestoreBottomSheet(
            onDismissRequest = {
                onResetRestoreState()
                showRestoreSheet = false
            },
            operationState = restoreOperationState,
            onResetOperationState = onResetRestoreState,
            onRestoreData = { uri, onSuccess, onError ->
                onRestoreData(uri, onSuccess, onError)
            }
        )
    }

    if (showTriggerManagementSheet) {
        TriggerManagementBottomSheet(
            customTriggers = customTriggers,
            disabledDefaultTriggers = disabledDefaultTriggers,
            onAddCustomTrigger = onAddCustomTrigger,
            onRemoveCustomTrigger = onRemoveCustomTrigger,
            onToggleDefaultTrigger = onToggleDefaultTrigger,
            onDismissRequest = { showTriggerManagementSheet = false },
            vibrationEnabled = vibrationEnabled
        )
    }

    if (showNotificationSettingsSheet) {
        NotificationSettingsBottomSheet(
            enabled = ongoingNotificationEnabled,
            onEnabledChange = onOngoingNotificationEnabledChange,
            lowPriority = notificationLowPriority,
            onLowPriorityChange = onNotificationLowPriorityChange,
            showTimer = notificationShowTimer,
            onShowTimerChange = onNotificationShowTimerChange,
            showProgress = notificationShowProgress,
            onShowProgressChange = onNotificationShowProgressChange,
            showAddButton = notificationShowAddButton,
            onShowAddButtonChange = onNotificationShowAddButtonChange,
            showResistButton = notificationShowResistButton,
            onShowResistButtonChange = onNotificationShowResistButtonChange,
            dailyLimit = dailyLimit,
            onDismissRequest = { showNotificationSettingsSheet = false },
            vibrationEnabled = vibrationEnabled
        )
    }

    if (showLanguageDialog) {
        LanguageBottomSheet(
            onDismissRequest = { showLanguageDialog = false },
            onRecordLanguageChange = onRecordLanguageChange
        )
    }

    if (showLimitDialog) {
        DailyLimitBottomSheet(
            dailyLimit = dailyLimit,
            taperingPlanEnabled = taperingPlanEnabled,
            taperingIntervalDays = taperingIntervalDays,
            showLimitOnGraph = showLimitOnGraph,
            onSetDailyLimit = onSetDailyLimit,
            onSetTaperingPlanSettings = onSetTaperingPlanSettings,
            onSetShowLimitOnGraph = onSetShowLimitOnGraph,
            onDismissRequest = { showLimitDialog = false },
            vibrationEnabled = vibrationEnabled
        )
    }

    if (showPackDialog) {
        PackSettingsBottomSheet(
            packPrice = packPrice,
            packSize = packSize,
            currency = currency,
            dailyLimit = dailyLimit,
            onUpdatePackDetails = onUpdatePackDetails,
            onDismissRequest = { showPackDialog = false },
            vibrationEnabled = vibrationEnabled
        )
    }

    if (showBaselineSheet) {
        BaselineBottomSheet(
            hasBaseline = hasHistoricalBaseline,
            historicalStartDate = historicalStartDate,
            historicalDailyAvg = historicalDailyAvg,
            packPrice = packPrice,
            packSize = packSize,
            currency = currency,
            onSaveBaseline = onSaveBaseline,
            onClearBaseline = onClearBaseline,
            onDismissRequest = { showBaselineSheet = false },
            vibrationEnabled = vibrationEnabled
        )
    }

    if (showWidgetDialog) {
        WidgetSettingsBottomSheet(
            widgetShowResistButton = widgetShowResistButton,
            onWidgetShowResistButtonChange = onWidgetShowResistButtonChange,
            dailyLimit = dailyLimit,
            vibrationEnabled = vibrationEnabled,
            onDismissRequest = { showWidgetDialog = false }
        )
    }

    if (showResetDialog) {
        ResetDataDialog(
            onConfirm = onResetAllData,
            onDismissRequest = { showResetDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(containerGroupGap())
    ) {
        interfaceSettingsSection(
            onAppearanceClick = onNavigateToAppearance,
            onLanguageClick = { showLanguageDialog = true },
            onWidgetsClick = { showWidgetDialog = true }
        )
        notificationSettingsSection(
            ongoingNotificationEnabled = ongoingNotificationEnabled,
            notificationLowPriority = notificationLowPriority,
            onNotificationClick = { showNotificationSettingsSheet = true }
        )
        generalSettingsSection(
            dailyLimit = dailyLimit,
            taperingPlanEnabled = taperingPlanEnabled,
            taperingIntervalDays = taperingIntervalDays,
            hasHistoricalBaseline = hasHistoricalBaseline,
            historicalStartDate = historicalStartDate,
            historicalDailyAvg = historicalDailyAvg,
            customTriggers = customTriggers,
            disabledDefaultTriggers = disabledDefaultTriggers,
            packPrice = packPrice,
            packSize = packSize,
            currency = currency,
            onLimitClick = { showLimitDialog = true },
            onBaselineClick = { showBaselineSheet = true },
            onTriggersClick = { showTriggerManagementSheet = true },
            onPackParamsClick = { showPackDialog = true }
        )
        backupSettingsSection(
            onBackupClick = { showBackupSheet = true },
            onRestoreClick = { showRestoreSheet = true },
            onResetDataClick = { showResetDialog = true }
        )
        infoSettingsSection(
            checkUpdatesOnStart = checkUpdatesOnStart,
            onCheckUpdatesOnStartChange = onCheckUpdatesOnStartChange,
            onAboutClick = onNavigateToAbout,
            onCheckForUpdates = onCheckForUpdates
        )
    }
}
