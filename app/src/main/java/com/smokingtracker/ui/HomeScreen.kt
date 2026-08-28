package com.smokingtracker.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import com.smokingtracker.ui.theme.containerBorder
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.containerPadding
import com.smokingtracker.ui.theme.ContainerIcon
import com.smokingtracker.ui.theme.LocalContainerStyle
import com.smokingtracker.data.ContainerStyle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.smokingtracker.data.TriggerType
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smokingtracker.HomeViewModel
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput

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
    vibrationEnabled: Boolean = false,
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToGraphs: (String) -> Unit = {}
) {
    val entries by viewModel.smokingEntries.collectAsStateWithLifecycle()
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val unlockedAchievements by viewModel.unlockedAchievements.collectAsStateWithLifecycle()
    HomeScreenContent(
        entries = entries,
        dailyLimit = dailyLimit,
        unlockedAchievements = unlockedAchievements,
        viewModel = viewModel,
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
    viewModel: HomeViewModel? = null,
    vibrationEnabled: Boolean = false,
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToGraphs: (String) -> Unit = {}
) {
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }

    var timePassedText by remember { mutableStateOf("") }
    val calculatingText = stringResource(R.string.time_calculating)
    val invalidEntryText = stringResource(R.string.invalid_future_entry)
    val startTrackingText = stringResource(R.string.start_tracking)
    
    val formatHm = stringResource(R.string.duration_hm)
    val formatHms = stringResource(R.string.duration_hms)
    val formatMs = stringResource(R.string.duration_ms)
    
    var showLimitWarning by remember { mutableStateOf(false) }
    var isProcessingAdd by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTriggerDialog by remember { mutableStateOf(false) }
    var showMindfulPauseDialog by remember { mutableStateOf(false) }
    var mindfulPauseTrigger by remember { mutableStateOf<String?>(null) }
    val allEntities by viewModel?.allSmokingEntities?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val showTaperingCheckIn by viewModel?.showTaperingCheckIn?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    var pendingLogTime by remember { mutableLongStateOf(0L) }
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.notif_permission_rationale),
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

    LaunchedEffect(entries, formatHm, formatHms, formatMs) {
        timePassedText = calculatingText
        while (true) {
            val lastEntry = entries.maxOrNull()
            if (lastEntry != null) {
                val now = System.currentTimeMillis()
                val diff = now - lastEntry
                
              if (diff < 0) {
                    timePassedText = invalidEntryText
                } else {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
                    
                    timePassedText = when {
                        hours >= 24 -> String.format(Locale.getDefault(), formatHm, hours, minutes)
                        hours > 0 -> String.format(Locale.getDefault(), formatHms, hours, minutes, seconds)
                        else -> String.format(Locale.getDefault(), formatMs, minutes, seconds)
                    }
                }
            } else {
                timePassedText = startTrackingText
            }
            delay(1000)
        }
    }

    val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    val selectedDateStr = dateFormat.format(currentDate.time)
    
    val selectedDateEntries = remember(entries, currentDate) {
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentDay = currentDate.get(Calendar.DAY_OF_YEAR)
        val checkCal = Calendar.getInstance()
        entries.filter { timestamp ->
            checkCal.timeInMillis = timestamp
            checkCal.get(Calendar.YEAR) == currentYear &&
            checkCal.get(Calendar.DAY_OF_YEAR) == currentDay
        }.sortedDescending()
    }

    val isToday = remember(currentDate) {
        val now = Calendar.getInstance()
        currentDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        currentDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    }
                  
    if (showLimitWarning) {
        AlertDialog(
            onDismissRequest = { showLimitWarning = false },
            shape = containerShape(RoundedCornerShape(28.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Surface(
                    shape = MaterialShapes.Cookie9Sided.toShape(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.limit_warning_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.limit_warning_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    enabled = !isProcessingAdd,
                    onClick = {
                        if (!isProcessingAdd) {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            isProcessingAdd = true
                            val now = Calendar.getInstance()
                            val entryDate = currentDate.clone() as Calendar
                            entryDate.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                            entryDate.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                            entryDate.set(Calendar.SECOND, now.get(Calendar.SECOND))
                            entryDate.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND))
                            if (entryDate.timeInMillis <= now.timeInMillis) {
                                pendingLogTime = entryDate.timeInMillis
                                showTriggerDialog = true
                            }
                            showLimitWarning = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.add_anyway), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        showLimitWarning = false
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDate.timeInMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            shape = containerShape(RoundedCornerShape(28.dp)),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newCal = Calendar.getInstance().apply { timeInMillis = millis }
                            currentDate = newCal
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_ok), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTriggerDialog) {
        val triggerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var isMindfulPauseActive by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = {
                showTriggerDialog = false
                isProcessingAdd = false
                isMindfulPauseActive = false
            },
            sheetState = triggerSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            AnimatedContent(
                targetState = isMindfulPauseActive,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "sheet_step_anim"
            ) { inPauseMode ->
                if (inPauseMode) {
                    MindfulPauseContent(
                        selectedTrigger = mindfulPauseTrigger,
                        vibrationEnabled = vibrationEnabled,
                        onDismiss = {
                            showTriggerDialog = false
                            isProcessingAdd = false
                            isMindfulPauseActive = false
                        },
                        onSuccess = { trigger ->
                            viewModel?.addResistedEntry(trigger)
                            showTriggerDialog = false
                            isProcessingAdd = false
                            isMindfulPauseActive = false
                        },
                        onFailure = { trigger ->
                            viewModel?.addSmokingEntryWithTrigger(System.currentTimeMillis(), trigger)
                            showTriggerDialog = false
                            isProcessingAdd = false
                            isMindfulPauseActive = false
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.trigger_dialog_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            modifier = Modifier.padding(bottom = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val triggers = com.smokingtracker.data.TriggerType.allEntries()
                        val chunkedTriggers = triggers.chunked(2)

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            chunkedTriggers.forEach { rowTriggers ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowTriggers.forEach { trigger ->
                                        Card(
                                            onClick = {
                                                com.smokingtracker.ui.theme.HapticFeedbackHelper.performSuccess(vibrationEnabled, haptic, context)
                                                if (pendingLogTime > 0L) {
                                                    viewModel?.addSmokingEntryWithTrigger(pendingLogTime, trigger.key)
                                                }
                                                showTriggerDialog = false
                                                isProcessingAdd = false
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(78.dp),
                                            shape = containerShape(RoundedCornerShape(20.dp)),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                            border = containerBorder(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxSize().padding(8.dp)
                                            ) {
                                                val cShape = MaterialShapes.Cookie9Sided.toShape()
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(cShape)
                                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = getTriggerIcon(trigger.key),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = stringResource(trigger.labelResId),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    if (rowTriggers.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mindful Pause button
                        Surface(
                            onClick = {
                                com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                mindfulPauseTrigger = null
                                isMindfulPauseActive = true
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            border = containerBorder(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.mindful_pause_button),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Skip / Without trigger Button
                        Button(
                            onClick = {
                                com.smokingtracker.ui.theme.HapticFeedbackHelper.performSuccess(vibrationEnabled, haptic, context)
                                if (pendingLogTime > 0L) {
                                    viewModel?.addSmokingEntryWithTrigger(pendingLogTime, null)
                                }
                                showTriggerDialog = false
                                isProcessingAdd = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SmokingRooms,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(stringResource(R.string.trigger_skip), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMindfulPauseDialog) {
        MindfulPauseDialog(
            selectedTrigger = mindfulPauseTrigger,
            vibrationEnabled = vibrationEnabled,
            onDismiss = { showMindfulPauseDialog = false },
            onSuccess = { trigger ->
                viewModel?.addResistedEntry(trigger)
                showMindfulPauseDialog = false
            },
            onFailure = { trigger ->
                val logTime = if (pendingLogTime > 0L) pendingLogTime else System.currentTimeMillis()
                viewModel?.addSmokingEntryWithTrigger(logTime, trigger)
                showMindfulPauseDialog = false
            }
        )
    }

    if (showTaperingCheckIn) {
        TaperingCheckInBottomSheet(
            currentLimit = dailyLimit,
            onReduceLimit = { viewModel?.acceptTaperingReduction() },
            onKeepLimit = { viewModel?.keepTaperingLimit() },
            onSnooze = { viewModel?.snoozeTaperingCheckIn() },
            onDismiss = { viewModel?.dismissTaperingCheckIn() }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val undoDeleteStr = stringResource(R.string.undo_delete)
    val undoStr = stringResource(R.string.undo)

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(bottom = 96.dp)
                    .padding(horizontal = 16.dp)
            ) { snackbarData ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = containerShape(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    border = containerBorder(
                        strokeWidth = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            ContainerIcon(
                                icon = Icons.Filled.Delete,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                backdropColor = MaterialTheme.colorScheme.errorContainer,
                                size = 36.dp
                            )
                            Text(
                                text = snackbarData.visuals.message,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        snackbarData.visuals.actionLabel?.let { actionLabel ->
                            TextButton(
                                onClick = { snackbarData.performAction() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = actionLabel,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }
                    }
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    Surface(
                        onClick = onNavigateToAchievements,
                        shape = RoundedCornerShape(100),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = stringResource(R.string.settings_achievements),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${unlockedAchievements.size}/${com.smokingtracker.AchievementsManager().achievementsList.size}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
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
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    shape = containerShape(RoundedCornerShape(32.dp)),
                    border = containerBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(containerPadding(32.dp, 32.dp, standardHorizontal = 20.dp, standardVertical = 20.dp)).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val heroTextStyle = when {
                            timePassedText.length > 16 -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                            timePassedText.length > 10 -> MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                            else -> MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold)
                        }
                        Text(
                            text = timePassedText,
                            style = heroTextStyle,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.time_past_label),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                if (dailyLimit > 0) {
                    val progress = (selectedDateEntries.size.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)
                    val isLimitReached = progress >= 1f
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    LinearWavyProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        amplitude = { if (isLimitReached) 0f else 1f },
                        waveSpeed = if (isLimitReached) 0.dp else 16.dp,
                        color = if (isLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                val weeklyCount = remember(entries, currentDate) { StatisticsManager().getWeeklyCount(entries, currentDate) }
                val monthlyCount = remember(entries, currentDate) { StatisticsManager().getMonthlyCount(entries, currentDate) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatItem(
                        label = stringResource(R.string.stat_daily),
                        value = selectedDateEntries.size.toString(),
                        onClick = { onNavigateToGraphs("daily") },
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = stringResource(R.string.stat_weekly),
                        value = weeklyCount.toString(),
                        onClick = { onNavigateToGraphs("weekly") },
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = stringResource(R.string.stat_monthly),
                        value = monthlyCount.toString(),
                        onClick = { onNavigateToGraphs("monthly") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                var totalDragX by remember { mutableFloatStateOf(0f) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .pointerInput(currentDate, isToday) {
                            detectHorizontalDragGestures(
                                onDragStart = { totalDragX = 0f },
                                onDragEnd = {
                                    if (totalDragX > 80f) {
                                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                        val newDate = currentDate.clone() as Calendar
                                        newDate.add(Calendar.DAY_OF_YEAR, -1)
                                        currentDate = newDate
                                    } else if (totalDragX < -80f && !isToday) {
                                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                        val newDate = currentDate.clone() as Calendar
                                        newDate.add(Calendar.DAY_OF_YEAR, 1)
                                        currentDate = newDate
                                    }
                                    totalDragX = 0f
                                },
                                onDragCancel = { totalDragX = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    totalDragX += dragAmount
                                }
                            )
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        onClick = {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            val newDate = currentDate.clone() as Calendar
                            newDate.add(Calendar.DAY_OF_YEAR, -1)
                            currentDate = newDate
                        },
                        shape = MaterialShapes.Cookie9Sided.toShape(),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_day))
                        }
                    }

                    Surface(
                        onClick = {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            showDatePicker = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        border = containerBorder(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = selectedDateStr,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            val newDate = currentDate.clone() as Calendar
                            newDate.add(Calendar.DAY_OF_YEAR, 1)
                            currentDate = newDate
                        },
                        enabled = !isToday,
                        shape = MaterialShapes.Cookie9Sided.toShape(),
                        color = if (!isToday) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!isToday) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_day))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val selectedDateAllEntities = remember(allEntities, currentDate) {
                    val currentYear = currentDate.get(Calendar.YEAR)
                    val currentDay = currentDate.get(Calendar.DAY_OF_YEAR)
                    val checkCal = Calendar.getInstance()
                    allEntities.filter { entity ->
                        checkCal.timeInMillis = entity.timestamp
                        checkCal.get(Calendar.YEAR) == currentYear && checkCal.get(Calendar.DAY_OF_YEAR) == currentDay
                    }.sortedByDescending { it.timestamp }
                }

                val entriesListState = androidx.compose.foundation.lazy.rememberLazyListState()
                val latestEntryId = selectedDateAllEntities.firstOrNull()?.id
                LaunchedEffect(latestEntryId) {
                    if (selectedDateAllEntities.isNotEmpty()) {
                        entriesListState.animateScrollToItem(0)
                    }
                }

                LazyColumn(
                    state = entriesListState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 190.dp)
                ) {
                    if (selectedDateAllEntities.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 4.dp),
                                shape = containerShape(RoundedCornerShape(24.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
                                ),
                                border = containerBorder(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = MaterialShapes.Cookie9Sided.toShape(),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        contentColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.History,
                                                contentDescription = null,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = stringResource(R.string.home_no_entries_title),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stringResource(R.string.home_no_entries_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = selectedDateAllEntities,
                            key = { _, entity -> entity.id }
                        ) { index, entity ->
                            val prevTime = if (index < selectedDateAllEntities.size - 1) selectedDateAllEntities[index + 1].timestamp else null
                            val density = LocalDensity.current
                            val screenWidthPx = remember(density) {
                                context.resources.displayMetrics.widthPixels.toFloat()
                            }
                            var latestDismissOffset by remember { mutableFloatStateOf(0f) }
                            val dismissState = rememberSwipeToDismissBoxState(
                                positionalThreshold = { distance -> distance * 0.85f },
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        val minThreshold = screenWidthPx * 0.67f
                                        if (latestDismissOffset < minThreshold) {
                                            false
                                        } else {
                                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                            scope.launch {
                                                viewModel?.removeSmokingEntry(entity.id, entity.timestamp)
                                                val result = snackbarHostState.showSnackbar(
                                                    message = undoDeleteStr,
                                                    actionLabel = undoStr,
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    if (entity.isResisted) {
                                                        viewModel?.addResistedEntry(entity.trigger, entity.timestamp)
                                                    } else {
                                                        viewModel?.addSmokingEntryWithTrigger(entity.timestamp, entity.trigger)
                                                    }
                                                }
                                            }
                                            true
                                        }
                                    } else {
                                        true
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    val direction = dismissState.dismissDirection
                                    val isEndToStart = direction == SwipeToDismissBoxValue.EndToStart
                                    val alignment = if (isEndToStart) Alignment.CenterEnd else Alignment.CenterStart
                                    val currentOffset = runCatching { dismissState.requireOffset() }.getOrDefault(0f)
                                    val absOffset = kotlin.math.abs(currentOffset)
                                    latestDismissOffset = absOffset
                                    val absOffsetDp = with(LocalDensity.current) { absOffset.toDp() }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 5.dp),
                                        contentAlignment = alignment
                                    ) {
                                        if (absOffsetDp > 6.dp) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .width(absOffsetDp)
                                                    .clip(containerShape(RoundedCornerShape(24.dp)))
                                                    .background(MaterialTheme.colorScheme.errorContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = stringResource(R.string.delete_entry),
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            ) {
                            EntryItem(
                                entryTime = entity.timestamp,
                                prevEntryTime = prevTime,
                                isResisted = entity.isResisted,
                                index = index,
                                trigger = entity.trigger,
                                onDelete = {
                                    com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    scope.launch {
                                        viewModel?.removeSmokingEntry(entity.id, entity.timestamp)
                                        val result = snackbarHostState.showSnackbar(
                                            message = undoDeleteStr,
                                            actionLabel = undoStr,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            if (entity.isResisted) {
                                                viewModel?.addResistedEntry(entity.trigger, entity.timestamp)
                                            } else {
                                                viewModel?.addSmokingEntryWithTrigger(entity.timestamp, entity.trigger)
                                            }
                                        }
                                    }
                                },
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

        val fabInteractionSource = remember { MutableInteractionSource() }
        val isFabPressed by fabInteractionSource.collectIsPressedAsState()
        val fabScale by animateFloatAsState(
            targetValue = if (isFabPressed) 0.88f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "fab_scale"
        )

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 110.dp, end = 24.dp)
                .scale(fabScale),
            onClick = {
                com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)

                if (dailyLimit in 1..selectedDateEntries.size) {
                    showLimitWarning = true
                } else {
                    val now = Calendar.getInstance()
                    val entryDate = currentDate.clone() as Calendar
                    entryDate.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                    entryDate.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                    entryDate.set(Calendar.SECOND, now.get(Calendar.SECOND))
                    entryDate.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND))

                    if (entryDate.timeInMillis <= now.timeInMillis) {
                        pendingLogTime = entryDate.timeInMillis
                        showTriggerDialog = true
                    }
                }
            },
            interactionSource = fabInteractionSource,
            shape = containerShape(RoundedCornerShape(18.dp)),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 1.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_entry),
                modifier = Modifier.size(28.dp)
            )
        }
        }
    }
}


@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "stat_scale"
    )

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale),
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = containerShape(RoundedCornerShape(24.dp)),
        border = containerBorder()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

private fun getTriggerIcon(triggerKey: String?): ImageVector {
    val trigger = triggerKey?.let { TriggerType.fromKey(it) }
    return when (trigger) {
        TriggerType.STRESS -> Icons.Filled.Bolt
        TriggerType.BOREDOM -> Icons.Filled.HourglassEmpty
        TriggerType.SOCIAL -> Icons.Filled.People
        TriggerType.ROUTINE -> Icons.Filled.Repeat
        TriggerType.FOOD_COFFEE -> Icons.Filled.LocalCafe
        TriggerType.ALCOHOL -> Icons.Filled.LocalBar
        else -> Icons.Filled.SmokingRooms
    }
}

@Preview(showBackground = true)
@Composable
fun EntryItemPreview() {
    EntryItem()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EntryItem(
    entryTime: Long = System.currentTimeMillis(),
    prevEntryTime: Long? = null,
    isResisted: Boolean = false,
    index: Int = 0,
    trigger: String? = null,
    onDelete: () -> Unit = {},
    onEdit: (Long) -> Unit = {},
    onUpdateTrigger: (String?) -> Unit = {}
) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entryTime))
    var isExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val futureDateError = if (!LocalInspectionMode.current) stringResource(R.string.edit_future_time_error) else ""

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "chevron_rotation"
    )

    val timePickerState = remember(entryTime) {
        val cal = Calendar.getInstance().apply { timeInMillis = entryTime }
        TimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true,
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            title = stringResource(R.string.edit_entry),
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newCal = Calendar.getInstance().apply { timeInMillis = entryTime }
                        newCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        newCal.set(Calendar.MINUTE, timePickerState.minute)
                        if (newCal.timeInMillis > System.currentTimeMillis()) {
                            android.widget.Toast.makeText(context, futureDateError, android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            onEdit(newCal.timeInMillis)
                            showTimePicker = false
                        }
                    }
                ) { Text(stringResource(R.string.dialog_ok), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false }
                ) { Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold) }
            }
        ) {
            val pastelContainer = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = pastelContainer,
                    timeSelectorUnselectedContainerColor = pastelContainer,
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    val (accentColor, accentContainer, onAccentContainer) = when (index % 3) {
        0 -> Triple(colorScheme.tertiary, colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
        1 -> Triple(colorScheme.secondary, colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)
        else -> Triple(colorScheme.primary, colorScheme.primaryContainer, colorScheme.onPrimaryContainer)
    }

    Card(
        onClick = {
            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(true, haptic, context)
            isExpanded = !isExpanded
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainer
        ),
        shape = containerShape(RoundedCornerShape(24.dp)),
        border = containerBorder(
            if (isResisted) 1.5.dp else 1.dp,
            if (isResisted) colorScheme.primary else colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // --- COLLAPSED HEADER ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Time pill
                Surface(
                    onClick = {
                        if (!isResisted) {
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(true, haptic, context)
                            showTimePicker = true
                        }
                    },
                    shape = CircleShape,
                    color = if (isResisted) colorScheme.primaryContainer else accentContainer,
                    contentColor = if (isResisted) colorScheme.onPrimaryContainer else onAccentContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                // 2. Reason / Trigger info chip (Icon + Text)
                val triggerType = trigger?.let { TriggerType.fromKey(it) }
                val triggerLabel = when {
                    isResisted -> stringResource(R.string.mindful_pause_resisted)
                    triggerType != null -> stringResource(triggerType.labelResId)
                    else -> stringResource(R.string.trigger_none)
                }
                val triggerIcon = when {
                    isResisted -> Icons.Default.Shield
                    else -> getTriggerIcon(trigger)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isResisted) colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else if (triggerType != null) accentContainer.copy(alpha = 0.35f)
                            else colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    contentColor = if (isResisted) colorScheme.primary
                                   else if (triggerType != null) accentColor
                                   else colorScheme.onSurfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = triggerIcon,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = triggerLabel,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 3. Interval / First of the day info
                if (!isResisted) {
                    if (prevEntryTime != null) {
                        val intervalStr = remember(entryTime, prevEntryTime) {
                            val diffMs = entryTime - prevEntryTime
                            val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
                            val minutes = (TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60)
                            if (hours > 0) {
                                context.getString(R.string.time_over_limit_hm, hours, minutes)
                            } else {
                                context.getString(R.string.time_over_limit_m, minutes)
                            }
                        }
                        Text(
                            text = intervalStr,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.first_of_the_day),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = accentColor.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                }

                // 4. Android notification-style expand chevron button
                Surface(
                    onClick = {
                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(true, haptic, context)
                        isExpanded = !isExpanded
                    },
                    shape = CircleShape,
                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = chevronRotation }
                        )
                    }
                }
            }

            // --- EXPANDABLE EDIT PANEL ---
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        color = colorScheme.outlineVariant.copy(alpha = 0.25f)
                    )

                    if (!isResisted) {
                        // Time Editor Button
                        Surface(
                            onClick = {
                                com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(true, haptic, context)
                                showTimePicker = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            contentColor = colorScheme.onSurfaceVariant,
                            border = containerBorder(1.dp, colorScheme.outlineVariant.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.edit_time_label),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colorScheme.onSurface
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Reason / Trigger selector section
                        Text(
                            text = stringResource(R.string.edit_reason_label),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp)
                        )

                        val allTriggers = remember { TriggerType.allEntries() }
                        val chunkedTriggers = remember { allTriggers.chunked(2) }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            chunkedTriggers.forEach { rowTriggers ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowTriggers.forEach { type ->
                                        val isSelected = trigger == type.key
                                        Surface(
                                            onClick = {
                                                com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(true, haptic, context)
                                                if (isSelected) {
                                                    onUpdateTrigger(null)
                                                } else {
                                                    onUpdateTrigger(type.key)
                                                }
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) colorScheme.primaryContainer else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            contentColor = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                                            border = containerBorder(
                                                1.dp,
                                                if (isSelected) colorScheme.primary else colorScheme.outlineVariant.copy(alpha = 0.25f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = getTriggerIcon(type.key),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = stringResource(type.labelResId),
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
                                                    ),
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    if (rowTriggers.size < 2) {
                                        repeat(2 - rowTriggers.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Delete Button
                    Surface(
                        onClick = onDelete,
                        shape = RoundedCornerShape(16.dp),
                        color = colorScheme.errorContainer.copy(alpha = 0.4f),
                        contentColor = colorScheme.error,
                        border = containerBorder(1.dp, colorScheme.error.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.delete_entry),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}


