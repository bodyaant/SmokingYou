package com.smokingtracker.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smokingtracker.AchievementsManager
import com.smokingtracker.HomeFabUiAction
import com.smokingtracker.HomeViewModel
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import com.smokingtracker.data.TriggerItem
import com.smokingtracker.data.TriggerType
import com.smokingtracker.startOfDayMillis
import com.smokingtracker.startOfTodayMillis
import com.smokingtracker.ui.home.components.HeroTimerSection
import com.smokingtracker.ui.home.components.HomeDailyLimitSection
import com.smokingtracker.ui.home.components.HomeDateSelector
import com.smokingtracker.ui.home.components.HomeEmptyState
import com.smokingtracker.ui.home.components.HomeSnackbarHost
import com.smokingtracker.ui.home.components.HomeStatsRow
import com.smokingtracker.ui.home.components.SwipeableEntryItem
import com.smokingtracker.ui.home.components.HomeTopAppBar
import com.smokingtracker.ui.home.dialogs.DailyLimitWarningDialog
import com.smokingtracker.ui.home.dialogs.HomeDatePickerDialog
import com.smokingtracker.ui.home.dialogs.HomeTriggerBottomSheet
import com.smokingtracker.ui.home.models.AppSnackbarVisuals
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerPadding
import com.smokingtracker.ui.theme.containerShape
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeScreenPreview() {
    HomeScreenContent(entries = emptyList(), dailyLimit = 10, viewModel = null)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    vibrationEnabled: Boolean = true,
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToGraphs: (String) -> Unit = {}
) {
    val entries by viewModel.smokingEntries.collectAsStateWithLifecycle()
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val unlockedAchievements by viewModel.unlockedAchievements.collectAsStateWithLifecycle()
    val taperingIntervalDays by viewModel.taperingIntervalDays.collectAsStateWithLifecycle()
    HomeScreenContent(
        entries = entries,
        dailyLimit = dailyLimit,
        unlockedAchievements = unlockedAchievements,
        taperingIntervalDays = taperingIntervalDays,
        viewModel = viewModel,
        statisticsManager = viewModel.statisticsManager,
        achievementsManager = viewModel.achievementsManager,
        vibrationEnabled = vibrationEnabled,
        onNavigateToAchievements = onNavigateToAchievements,
        onNavigateToGraphs = onNavigateToGraphs
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeScreenContent(
    entries: List<Long>,
    dailyLimit: Int,
    unlockedAchievements: Set<String> = emptySet(),
    taperingIntervalDays: Int = 7,
    viewModel: HomeViewModel? = null,
    statisticsManager: StatisticsManager = remember(viewModel) { viewModel?.statisticsManager ?: StatisticsManager() },
    achievementsManager: AchievementsManager = remember(viewModel) { viewModel?.achievementsManager ?: AchievementsManager() },
    vibrationEnabled: Boolean = true,
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToGraphs: (String) -> Unit = {}
) {
    var currentDateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    val currentDate = remember(currentDateMillis) {
        Calendar.getInstance().apply { timeInMillis = currentDateMillis }
    }

    var showLimitWarning by rememberSaveable { mutableStateOf(false) }
    var isProcessingAdd by remember { mutableStateOf(false) }
    var lastAddActionTimeMs by remember { mutableLongStateOf(0L) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTriggerDialog by rememberSaveable { mutableStateOf(false) }
    var mindfulPauseTrigger by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddTriggerDialog by rememberSaveable { mutableStateOf(false) }
    val allEntities by viewModel?.allSmokingEntities?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val showTaperingCheckIn by viewModel?.showTaperingCheckIn?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val activeTriggers by viewModel?.activeTriggers?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(TriggerType.allEntries().map { TriggerItem.fromBuiltIn(it) }) }
    val customTriggers by viewModel?.customTriggers?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    var pendingLogTime by rememberSaveable { mutableLongStateOf(0L) }
    var pendingTriggerToLog by rememberSaveable { mutableStateOf<String?>(null) }
    var startInMindfulPause by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var undoSnackbarJob by remember { mutableStateOf<Job?>(null) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val locale = LocalLocale.current.platformLocale

    val resolveSelectedDateTimestamp: () -> Long = {
        val now = Calendar.getInstance()
        val entryDate = Calendar.getInstance().apply { timeInMillis = currentDateMillis }
        entryDate.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
        entryDate.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
        entryDate.set(Calendar.SECOND, now.get(Calendar.SECOND))
        entryDate.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND))
        if (entryDate.timeInMillis <= now.timeInMillis) entryDate.timeInMillis else now.timeInMillis
    }

    val resolveEffectiveLogTime: () -> Long = {
        if (pendingLogTime > 0L) pendingLogTime else resolveSelectedDateTimestamp()
    }

    val resistedSuccessMsg = stringResource(R.string.resisted_craving_success)
    LaunchedEffect(currentDateMillis, viewModel) {
        viewModel?.setSelectedDate(currentDateMillis)
    }
    LaunchedEffect(viewModel) {
        viewModel?.fabActionEvents?.collect { action ->
            when (action) {
                is HomeFabUiAction.OpenSmokedDialog -> {
                    pendingLogTime = resolveSelectedDateTimestamp()
                    startInMindfulPause = false
                    showTriggerDialog = true
                }
                is HomeFabUiAction.OpenMindfulPauseDialog -> {
                    pendingLogTime = resolveSelectedDateTimestamp()
                    mindfulPauseTrigger = null
                    startInMindfulPause = true
                    showTriggerDialog = true
                }
                is HomeFabUiAction.ResistedCraving -> {
                    HapticFeedbackHelper.performSuccess(vibrationEnabled, haptic, context)
                    if (!action.alreadyRecorded) {
                        val logTime = resolveSelectedDateTimestamp()
                        viewModel.addResistedEntry(null, logTime)
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            AppSnackbarVisuals(
                                message = resistedSuccessMsg,
                                duration = SnackbarDuration.Short,
                                icon = Icons.Filled.Shield,
                                isError = false
                            )
                        )
                    }
                }
            }
        }
    }

    val selectedDateEntries = remember(entries, currentDateMillis) {
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentDay = currentDate.get(Calendar.DAY_OF_YEAR)
        val checkCal = Calendar.getInstance()
        entries.filter { timestamp ->
            checkCal.timeInMillis = timestamp
            checkCal.get(Calendar.YEAR) == currentYear &&
            checkCal.get(Calendar.DAY_OF_YEAR) == currentDay
        }.sortedDescending()
    }

    val attemptLogSmoking: (String?) -> Unit = { triggerKey ->
        val nowMs = System.currentTimeMillis()
        if (!isProcessingAdd && (nowMs - lastAddActionTimeMs) >= 400L) {
            if (dailyLimit in 1..selectedDateEntries.size) {
                pendingTriggerToLog = triggerKey
                showLimitWarning = true
            } else {
                isProcessingAdd = true
                lastAddActionTimeMs = nowMs
                HapticFeedbackHelper.performSuccess(vibrationEnabled, haptic, context)
                val targetLogTime = resolveEffectiveLogTime()
                viewModel?.addSmokingEntryWithTrigger(targetLogTime, triggerKey)
                showTriggerDialog = false
                isProcessingAdd = false
            }
        }
    }

    if (showAddTriggerDialog) {
        AddTriggerDialog(
            customTriggers = customTriggers,
            vibrationEnabled = vibrationEnabled,
            onDismiss = { showAddTriggerDialog = false },
            onConfirm = { name ->
                viewModel?.addCustomTrigger(name) { added ->
                    if (added != null) {
                        showAddTriggerDialog = false
                        attemptLogSmoking(added)
                    }
                }
            }
        )
    }

    DailyLimitWarningDialog(
        visible = showLimitWarning,
        isProcessingAdd = isProcessingAdd,
        vibrationEnabled = vibrationEnabled,
        onConfirm = {
            val nowMs = System.currentTimeMillis()
            if (!isProcessingAdd && (nowMs - lastAddActionTimeMs) >= 400L) {
                isProcessingAdd = true
                lastAddActionTimeMs = nowMs
                val targetLogTime = resolveEffectiveLogTime()
                viewModel?.addSmokingEntryWithTrigger(targetLogTime, pendingTriggerToLog)
                showLimitWarning = false
                showTriggerDialog = false
                isProcessingAdd = false
            }
        },
        onDismiss = {
            showLimitWarning = false
            isProcessingAdd = false
        }
    )

    HomeDatePickerDialog(
        visible = showDatePicker,
        currentDateMillis = currentDateMillis,
        vibrationEnabled = vibrationEnabled,
        onDateSelected = { millis ->
            currentDateMillis = millis
        },
        onDismiss = { showDatePicker = false }
    )

    HomeTriggerBottomSheet(
        visible = showTriggerDialog,
        activeTriggers = activeTriggers,
        startInMindfulPause = startInMindfulPause,
        mindfulPauseTrigger = mindfulPauseTrigger,
        vibrationEnabled = vibrationEnabled,
        onDismiss = {
            showTriggerDialog = false
            isProcessingAdd = false
            startInMindfulPause = false
            mindfulPauseTrigger = null
        },
        onSelectTrigger = { triggerKey ->
            attemptLogSmoking(triggerKey)
        },
        onAddCustomTrigger = {
            showAddTriggerDialog = true
        },
        onMindfulPauseSuccess = { trigger ->
            val targetLogTime = resolveEffectiveLogTime()
            viewModel?.addResistedEntry(trigger, targetLogTime)
            showTriggerDialog = false
            isProcessingAdd = false
            startInMindfulPause = false
            mindfulPauseTrigger = null
        },
        onMindfulPauseFailure = { trigger ->
            attemptLogSmoking(trigger)
            startInMindfulPause = false
            mindfulPauseTrigger = null
        }
    )

    if (showTaperingCheckIn) {
        TaperingCheckInBottomSheet(
            currentLimit = dailyLimit,
            intervalDays = taperingIntervalDays,
            onReduceLimit = { viewModel?.acceptTaperingReduction() },
            onKeepLimit = { viewModel?.keepTaperingLimit() },
            onSnooze = { viewModel?.snoozeTaperingCheckIn() },
            onDismiss = { viewModel?.dismissTaperingCheckIn() },
            vibrationEnabled = vibrationEnabled
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            android.widget.Toast.makeText(
                context,
                R.string.notif_permission_rationale,
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val dateFormat = remember(locale) { SimpleDateFormat("d MMMM yyyy", locale) }
    val selectedDateStr = remember(currentDateMillis, dateFormat) { dateFormat.format(Date(currentDateMillis)) }

    val canGoNext = remember(currentDateMillis) {
        startOfDayMillis(currentDateMillis) < startOfTodayMillis()
    }

    val onPreviousDay: () -> Unit = {
        val newDate = Calendar.getInstance().apply { timeInMillis = currentDateMillis }
        newDate.add(Calendar.DAY_OF_YEAR, -1)
        currentDateMillis = newDate.timeInMillis
    }

    val onNextDay: () -> Unit = {
        if (canGoNext) {
            val newDate = Calendar.getInstance().apply { timeInMillis = currentDateMillis }
            newDate.add(Calendar.DAY_OF_YEAR, 1)
            currentDateMillis = newDate.timeInMillis.coerceAtMost(System.currentTimeMillis())
        }
    }

    val undoDeleteStr = stringResource(R.string.undo_delete)
    val undoStr = stringResource(R.string.undo)

    Scaffold(
        snackbarHost = {
            HomeSnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            HomeTopAppBar(
                unlockedAchievements = unlockedAchievements,
                achievementsManager = achievementsManager,
                onNavigateToAchievements = onNavigateToAchievements
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(14.dp))
                val lastSmokedTimestamp = remember(entries) { entries.maxOrNull() }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    shape = containerShape(RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(containerPadding(20.dp, 16.dp, standardHorizontal = 18.dp, standardVertical = 16.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        HeroTimerSection(lastEntryTimestamp = lastSmokedTimestamp)
                        HomeDailyLimitSection(
                            currentCount = selectedDateEntries.size,
                            dailyLimit = dailyLimit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val weeklyCount = remember(entries, currentDate, statisticsManager) {
                    statisticsManager.getWeeklyCount(entries, currentDate)
                }
                val monthlyCount = remember(entries, currentDate, statisticsManager) {
                    statisticsManager.getMonthlyCount(entries, currentDate)
                }

                HomeStatsRow(
                    dailyCount = selectedDateEntries.size,
                    weeklyCount = weeklyCount,
                    monthlyCount = monthlyCount,
                    vibrationEnabled = vibrationEnabled,
                    onNavigateToGraphs = onNavigateToGraphs
                )

                Spacer(modifier = Modifier.height(12.dp))

                HomeDateSelector(
                    selectedDateStr = selectedDateStr,
                    canGoNext = canGoNext,
                    onPreviousDay = onPreviousDay,
                    onNextDay = onNextDay,
                    onOpenDatePicker = { showDatePicker = true },
                    vibrationEnabled = vibrationEnabled
                )

                Spacer(modifier = Modifier.height(16.dp))

                val selectedDateAllEntities = remember(allEntities, currentDateMillis) {
                    val currentYear = currentDate.get(Calendar.YEAR)
                    val currentDay = currentDate.get(Calendar.DAY_OF_YEAR)
                    val checkCal = Calendar.getInstance()
                    allEntities.filter { entity ->
                        checkCal.timeInMillis = entity.timestamp
                        checkCal.get(Calendar.YEAR) == currentYear && checkCal.get(Calendar.DAY_OF_YEAR) == currentDay
                    }.sortedByDescending { it.timestamp }
                }

                val overLimitEntryIds = remember(selectedDateAllEntities, dailyLimit) {
                    if (dailyLimit > 0) {
                        val nonResistedAsc = selectedDateAllEntities.filter { !it.isResisted }.sortedBy { it.timestamp }
                        if (nonResistedAsc.size > dailyLimit) {
                            nonResistedAsc.drop(dailyLimit).map { it.id }.toSet()
                        } else {
                            emptySet()
                        }
                    } else {
                        emptySet()
                    }
                }

                val entriesListState = rememberLazyListState()
                val latestEntryId = selectedDateAllEntities.firstOrNull()?.id
                LaunchedEffect(latestEntryId) {
                    if (selectedDateAllEntities.isNotEmpty()) {
                        entriesListState.animateScrollToItem(0)
                    }
                }

                if (selectedDateAllEntities.isEmpty()) {
                    HomeEmptyState(
                        canGoNext = canGoNext,
                        onPreviousDay = onPreviousDay,
                        onNextDay = onNextDay,
                        vibrationEnabled = vibrationEnabled,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        state = entriesListState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        itemsIndexed(
                            items = selectedDateAllEntities,
                            key = { _, entity -> entity.id }
                        ) { index, entity ->
                            val prevTime = if (index < selectedDateAllEntities.size - 1) selectedDateAllEntities[index + 1].timestamp else null
                            val handleEntryDeletion: () -> Unit = {
                                viewModel?.removeSmokingEntry(entity.id, entity.timestamp, entity.isResisted)
                                undoSnackbarJob?.cancel()
                                snackbarHostState.currentSnackbarData?.dismiss()
                                undoSnackbarJob = scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        AppSnackbarVisuals(
                                            message = undoDeleteStr,
                                            actionLabel = undoStr,
                                            duration = SnackbarDuration.Short,
                                            icon = Icons.Filled.Delete,
                                            isError = true
                                        )
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        if (entity.isResisted) {
                                            viewModel?.addResistedEntry(entity.trigger, entity.timestamp)
                                        } else {
                                            viewModel?.addSmokingEntryWithTrigger(entity.timestamp, entity.trigger)
                                        }
                                    }
                                }
                            }

                            SwipeableEntryItem(
                                entryTime = entity.timestamp,
                                prevEntryTime = prevTime,
                                isResisted = entity.isResisted,
                                isOverLimit = overLimitEntryIds.contains(entity.id),
                                index = index,
                                trigger = entity.trigger,
                                allTriggers = activeTriggers,
                                vibrationEnabled = vibrationEnabled,
                                onDelete = handleEntryDeletion,
                                onEdit = { newTime ->
                                    viewModel?.editSmokingEntry(entity.id, entity.timestamp, newTime)
                                },
                                onUpdateTrigger = { newTrigger ->
                                    viewModel?.updateSmokingEntryTrigger(entity.id, newTrigger)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
