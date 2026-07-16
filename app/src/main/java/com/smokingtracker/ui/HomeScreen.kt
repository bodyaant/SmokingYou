package com.smokingtracker.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smokingtracker.MainViewModel
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeScreenPreview() {
    HomeScreenContent(entries = emptyList(), dailyLimit = 10, viewModel = null)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val entries by viewModel.smokingEntries.collectAsState()
    val dailyLimit by viewModel.dailyLimit.collectAsState()
    HomeScreenContent(entries = entries, dailyLimit = dailyLimit, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeScreenContent(entries: List<Long>, dailyLimit: Int, viewModel: MainViewModel? = null) {
    val cookieShape = MaterialShapes.Cookie12Sided.toShape()

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
    var pendingLogTime by remember { mutableLongStateOf(0L) }
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val rotationAngle = remember { Animatable(0f) }
    var isAnimating by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

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
            title = { Text(stringResource(R.string.limit_warning_title)) },
            text = { Text(stringResource(R.string.limit_warning_message)) },
            confirmButton = {
                TextButton(
                    enabled = !isProcessingAdd,
                    onClick = {
                        if (!isProcessingAdd) {
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
                    }
                ) {
                    Text(stringResource(R.string.add_anyway))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitWarning = false }) {
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
            confirmButton = {
                TextButton(
                    onClick = {
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
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTriggerDialog) {
        BasicAlertDialog(onDismissRequest = { showTriggerDialog = false; isProcessingAdd = false }) {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.trigger_dialog_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        com.smokingtracker.data.TriggerType.allEntries().forEach { trigger ->
                            Button(
                                onClick = {
                                    if (pendingLogTime > 0L) {
                                        viewModel?.addSmokingEntryWithTrigger(pendingLogTime, trigger.key)
                                    }
                                    showTriggerDialog = false
                                    isProcessingAdd = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(stringResource(trigger.labelResId), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showTriggerDialog = false; isProcessingAdd = false }) {
                            Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (pendingLogTime > 0L) {
                                    viewModel?.addSmokingEntryWithTrigger(pendingLogTime, null)
                                }
                                showTriggerDialog = false
                                isProcessingAdd = false
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(stringResource(R.string.trigger_skip), fontWeight = FontWeight.Bold)
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    border = BorderStroke(
                        width = 1.dp,
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
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
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
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = timePassedText,
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
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
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    LinearWavyProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                val today = remember { Calendar.getInstance() }
                val weeklyCount = remember(entries) { StatisticsManager.getWeeklyCount(entries, today) }
                val monthlyCount = remember(entries) { StatisticsManager.getMonthlyCount(entries, today) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatItem(stringResource(R.string.stat_daily), selectedDateEntries.size.toString(), Modifier.weight(1f))
                    StatItem(stringResource(R.string.stat_weekly), weeklyCount.toString(), Modifier.weight(1f)) 
                    StatItem(stringResource(R.string.stat_monthly), monthlyCount.toString(), Modifier.weight(1f)) 
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        onClick = {
                            val newDate = currentDate.clone() as Calendar
                            newDate.add(Calendar.DAY_OF_YEAR, -1)
                            currentDate = newDate
                        },
                        shape = cookieShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_day))
                        }
                    }

                    Surface(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
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
                        }
                    }

                    Surface(
                        onClick = {
                            val newDate = currentDate.clone() as Calendar
                            newDate.add(Calendar.DAY_OF_YEAR, 1)
                            currentDate = newDate
                        },
                        enabled = !isToday,
                        shape = cookieShape,
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

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 190.dp)
                ) {
                    itemsIndexed(
                        items = selectedDateEntries,
                        key = { _, entryTime -> entryTime }
                    ) { index, entryTime ->
                        val prevTime = if (index < selectedDateEntries.size - 1) selectedDateEntries[index + 1] else null
                        EntryItem(
                            entryTime = entryTime,
                            prevEntryTime = prevTime,
                            index = index,
                            onDelete = {
                                scope.launch {
                                    val trigger = viewModel?.entryTriggers?.value?.get(entryTime)
                                    viewModel?.removeSmokingEntry(entryTime)
                                    val result = snackbarHostState.showSnackbar(
                                        message = undoDeleteStr,
                                        actionLabel = undoStr,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel?.addSmokingEntryWithTrigger(entryTime, trigger)
                                    }
                                }
                            },
                            onEdit = { newTime ->
                                viewModel?.editSmokingEntry(entryTime, newTime)
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 110.dp, end = 24.dp)
                    .graphicsLayer {
                        rotationZ = rotationAngle.value
                    },
                onClick = {
                    if (isAnimating) return@FloatingActionButton

                    scope.launch {
                        isAnimating = true
                        rotationAngle.animateTo(
                            targetValue = 90f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                        rotationAngle.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                        isAnimating = false
                    }

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
                shape = MaterialShapes.Cookie9Sided.toShape(),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_entry),
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            rotationZ = -rotationAngle.value
                        }
                )
            }
        }
    }
}


@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
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
    index: Int = 0,
    onDelete: () -> Unit = {},
    onEdit: (Long) -> Unit = {}
) {
    val cookieShape = MaterialShapes.Cookie9Sided.toShape()
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entryTime))
    var showTimePicker by remember { mutableStateOf(false) }
    var editErrorToast by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val futureDateError = if (!LocalInspectionMode.current) stringResource(R.string.edit_future_time_error) else ""

    val cal = Calendar.getInstance().apply { timeInMillis = entryTime }
    val timePickerState = rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
        is24Hour = true,
    )

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
                ) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false }
                ) { Text(stringResource(R.string.dialog_cancel)) }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val (accentColor, accentContainer, onAccentContainer) = when (index % 3) {
        0 -> Triple(colorScheme.tertiary, colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
        1 -> Triple(colorScheme.secondary, colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)
        else -> Triple(colorScheme.primary, colorScheme.primaryContainer, colorScheme.onPrimaryContainer)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accentContainer,
                contentColor = onAccentContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            Surface(
                onClick = { showTimePicker = true },
                shape = cookieShape,
                color = accentContainer.copy(alpha = 0.25f),
                contentColor = accentColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit_entry),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                onClick = onDelete,
                shape = cookieShape,
                color = colorScheme.errorContainer.copy(alpha = 0.25f),
                contentColor = colorScheme.error,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete_entry),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (prevEntryTime != null) {
                val intervalStr = remember(entryTime, prevEntryTime) {
                    val diffMs = entryTime - prevEntryTime
                    val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diffMs)
                    val minutes = (java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60)
                    if (hours > 0) {
                        context.getString(R.string.time_over_limit_hm, hours, minutes)
                    } else {
                        context.getString(R.string.time_over_limit_m, minutes)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = intervalStr,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentContainer.copy(alpha = 0.15f),
                    contentColor = accentColor.copy(alpha = 0.8f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.first_of_the_day),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}


