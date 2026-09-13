package com.smokingtracker.ui

import com.smokingtracker.ui.theme.HapticFeedbackHelper
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.style.TextOverflow
import com.smokingtracker.data.TriggerType
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.ContainerIcon
import com.smokingtracker.ui.theme.rememberBouncyPress
import com.smokingtracker.ui.theme.bouncyPress
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smokingtracker.MainViewModel
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.rememberLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GraphScreen(
    viewModel: MainViewModel,
    initialTarget: String? = null,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val entries by viewModel.smokingEntries.collectAsStateWithLifecycle()
    val nonResistedEntities by viewModel.nonResistedEntities.collectAsStateWithLifecycle()
    val resistedEntries by viewModel.resistedEntries.collectAsStateWithLifecycle()
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val packPrice by viewModel.packPrice.collectAsStateWithLifecycle()
    val packSize by viewModel.packSize.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val hasHistoricalBaseline by viewModel.hasHistoricalBaseline.collectAsStateWithLifecycle()
    val historicalStartDate by viewModel.historicalStartDate.collectAsStateWithLifecycle()
    val historicalDailyAvg by viewModel.historicalDailyAvg.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val showLimitOnGraph by viewModel.showLimitOnGraph.collectAsStateWithLifecycle()
    val scrollTarget by viewModel.graphScrollTarget.collectAsStateWithLifecycle()
    val customTriggers by viewModel.customTriggers.collectAsStateWithLifecycle()
    val disabledDefaultTriggers by viewModel.disabledDefaultTriggers.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAnalyticsTabVisited()
    }

    GraphScreenContent(
        entries = entries,
        resistedCount = resistedEntries.size,
        triggerEntities = nonResistedEntities,
        dailyLimit = dailyLimit,
        showLimitOnGraph = showLimitOnGraph,
        packPrice = packPrice,
        packSize = packSize,
        currency = currency,
        vibrationEnabled = vibrationEnabled,
        scrollTarget = scrollTarget,
        onClearScrollTarget = { viewModel.clearGraphScrollTarget() },
        hasHistoricalBaseline = hasHistoricalBaseline,
        historicalStartDate = historicalStartDate,
        historicalDailyAvg = historicalDailyAvg,
        onSaveBaseline = viewModel::saveBaseline,
        onClearBaseline = viewModel::clearHistoricalBaseline,
        onNavigateToSettings = onNavigateToSettings,
        customTriggers = customTriggers,
        disabledDefaultTriggers = disabledDefaultTriggers,
        onAddCustomTrigger = viewModel::addCustomTrigger,
        onRemoveCustomTrigger = viewModel::removeCustomTrigger,
        onToggleDefaultTrigger = viewModel::toggleDefaultTrigger
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GraphScreenContent(
    entries: List<Long>,
    resistedCount: Int = 0,
    triggerEntities: List<com.smokingtracker.data.local.SmokingEntryEntity> = emptyList(),
    dailyLimit: Int = 0,
    showLimitOnGraph: Boolean = true,
    packPrice: Float = 0f,
    packSize: Int = 20,
    currency: String = "",
    vibrationEnabled: Boolean = true,
    scrollTarget: String? = null,
    onClearScrollTarget: () -> Unit = {},
    hasHistoricalBaseline: Boolean = false,
    historicalStartDate: Long = 0L,
    historicalDailyAvg: Int = 0,
    onSaveBaseline: (startDate: Long, dailyAvg: Int) -> Unit = { _, _ -> },
    onClearBaseline: () -> Unit = {},
    onNavigateToSettings: (() -> Unit)? = null,
    customTriggers: List<String> = emptyList(),
    disabledDefaultTriggers: Set<String> = emptySet(),
    onAddCustomTrigger: (String, (String?) -> Unit) -> Unit = { _, _ -> },
    onRemoveCustomTrigger: (String) -> Unit = {},
    onToggleDefaultTrigger: (String, Boolean) -> Unit = { _, _ -> }
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showBaselineSheet by remember { mutableStateOf(false) }
    var showTriggerManagementSheet by remember { mutableStateOf(false) }

    val baselineAnalytics = remember(hasHistoricalBaseline, historicalStartDate, historicalDailyAvg, packPrice, packSize, entries) {
        if (hasHistoricalBaseline) {
            StatisticsManager().calculateBaselineAnalytics(
                startDate = historicalStartDate,
                dailyAvg = historicalDailyAvg,
                packPrice = packPrice,
                packSize = packSize,
                entries = entries
            )
        } else null
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

    var selectedPeriod by remember { mutableStateOf(ChartPeriod.WEEK) }

    LaunchedEffect(scrollTarget) {
        if (scrollTarget != null) {
            pagerState.animateScrollToPage(0)
            when (scrollTarget) {
                "daily" -> selectedPeriod = ChartPeriod.DAY
                "weekly" -> selectedPeriod = ChartPeriod.WEEK
                "monthly" -> selectedPeriod = ChartPeriod.MONTH
                "yearly" -> selectedPeriod = ChartPeriod.YEAR
            }
            listState.animateScrollToItem(0)
            onClearScrollTarget()
        }
    }

    var dailyDate by remember { mutableStateOf(Calendar.getInstance()) }
    var weeklyDate by remember { mutableStateOf(Calendar.getInstance()) }
    var monthlyDate by remember { mutableStateOf(Calendar.getInstance()) }
    var yearlyDate by remember { mutableStateOf(Calendar.getInstance()) }

    val dailyData = remember(entries, dailyDate) { StatisticsManager().generateDailyData(entries, dailyDate) }
    val weeklyData = remember(entries, weeklyDate) { StatisticsManager().generateWeeklyData(entries, weeklyDate) }
    val monthlyData = remember(entries, monthlyDate) { StatisticsManager().generateMonthlyData(entries, monthlyDate) }
    val monthlyDailyData = remember(entries, monthlyDate) { StatisticsManager().generateMonthlyDailyData(entries, monthlyDate) }
    val yearlyData = remember(entries, yearlyDate) { StatisticsManager().generateYearlyData(entries, yearlyDate) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var activeDatePickerTarget by remember { mutableStateOf<String?>(null) }

    activeDatePickerTarget?.let { target ->
        val targetCalendar = when (target) {
            "daily" -> dailyDate
            "weekly" -> weeklyDate
            "monthly" -> monthlyDate
            else -> yearlyDate
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = targetCalendar.timeInMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { activeDatePickerTarget = null },
            shape = containerShape(RoundedCornerShape(28.dp)),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                            when (target) {
                                "daily" -> dailyDate = cal
                                "weekly" -> weeklyDate = cal
                                "monthly" -> monthlyDate = cal
                                "yearly" -> yearlyDate = cal
                            }
                        }
                        activeDatePickerTarget = null
                    }
                ) {
                    Text(stringResource(R.string.dialog_ok), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    activeDatePickerTarget = null
                }) {
                    Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.analytics_title),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                )
                ExpressiveTabSelector(
                    selectedTab = pagerState.currentPage,
                    onTabSelected = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(it)
                        }
                    },
                    tabs = listOf(
                        stringResource(R.string.tab_graphs),
                        stringResource(R.string.settings_statistics),
                        stringResource(R.string.tab_triggers)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> {
                    val weeklyComparison = remember(entries) { StatisticsManager().calculateWeeklyComparison(entries) }
                    val hourlyDistribution = remember(entries) { StatisticsManager().calculateHourlyDistribution(entries) }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }
                            val today = Calendar.getInstance()

                            val dailyStr = remember(dailyDate) { dateFormat.format(dailyDate.time) }
                            val canGoNextDaily = remember(dailyDate) {
                                dailyDate.get(Calendar.YEAR) < today.get(Calendar.YEAR) ||
                                (dailyDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                 dailyDate.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR))
                            }

                            val weeklyStr = remember(weeklyDate) {
                                val weekStart = weeklyDate.clone() as Calendar
                                weekStart.firstDayOfWeek = Calendar.MONDAY
                                while (weekStart.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                                    weekStart.add(Calendar.DAY_OF_YEAR, -1)
                                }
                                val weekEnd = weekStart.clone() as Calendar
                                weekEnd.add(Calendar.DAY_OF_YEAR, 6)
                                "${SimpleDateFormat("d MMM", Locale.getDefault()).format(weekStart.time)} - ${SimpleDateFormat("d MMM", Locale.getDefault()).format(weekEnd.time)}"
                            }
                            val canGoNextWeekly = remember(weeklyDate) {
                                val todayWeekStart = today.clone() as Calendar
                                todayWeekStart.firstDayOfWeek = Calendar.MONDAY
                                while (todayWeekStart.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                                    todayWeekStart.add(Calendar.DAY_OF_YEAR, -1)
                                }
                                todayWeekStart.set(Calendar.HOUR_OF_DAY, 0)
                                todayWeekStart.set(Calendar.MINUTE, 0)
                                todayWeekStart.set(Calendar.SECOND, 0)
                                todayWeekStart.set(Calendar.MILLISECOND, 0)

                                val selectedWeekStart = weeklyDate.clone() as Calendar
                                selectedWeekStart.firstDayOfWeek = Calendar.MONDAY
                                while (selectedWeekStart.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                                    selectedWeekStart.add(Calendar.DAY_OF_YEAR, -1)
                                }
                                selectedWeekStart.set(Calendar.HOUR_OF_DAY, 0)
                                selectedWeekStart.set(Calendar.MINUTE, 0)
                                selectedWeekStart.set(Calendar.SECOND, 0)
                                selectedWeekStart.set(Calendar.MILLISECOND, 0)

                                selectedWeekStart.timeInMillis < todayWeekStart.timeInMillis
                            }

                            val monthlyStr = remember(monthlyDate) {
                                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthlyDate.time)
                            }
                            val canGoNextMonthly = remember(monthlyDate) {
                                val curCal = Calendar.getInstance()
                                monthlyDate.get(Calendar.YEAR) < curCal.get(Calendar.YEAR) ||
                                (monthlyDate.get(Calendar.YEAR) == curCal.get(Calendar.YEAR) &&
                                 monthlyDate.get(Calendar.MONTH) < curCal.get(Calendar.MONTH))
                            }

                            val yearlyStr = remember(yearlyDate) {
                                SimpleDateFormat("yyyy", Locale.getDefault()).format(yearlyDate.time)
                            }
                            val canGoNextYearly = remember(yearlyDate) {
                                yearlyDate.get(Calendar.YEAR) < today.get(Calendar.YEAR)
                            }

                            HeroAnalyticsCard(
                                selectedPeriod = selectedPeriod,
                                onPeriodSelected = { selectedPeriod = it },
                                dailyDate = dailyDate,
                                weeklyDate = weeklyDate,
                                monthlyDate = monthlyDate,
                                yearlyDate = yearlyDate,
                                dailyData = dailyData,
                                weeklyData = weeklyData,
                                monthlyData = monthlyDailyData,
                                yearlyData = yearlyData,
                                dailyStr = dailyStr,
                                weeklyStr = weeklyStr,
                                monthlyStr = monthlyStr,
                                yearlyStr = yearlyStr,
                                canGoNextDaily = canGoNextDaily,
                                canGoNextWeekly = canGoNextWeekly,
                                canGoNextMonthly = canGoNextMonthly,
                                canGoNextYearly = canGoNextYearly,
                                onPreviousDaily = { dailyDate = dailyDate.clone().apply { (this as Calendar).add(Calendar.DAY_OF_YEAR, -1) } as Calendar },
                                onNextDaily = { dailyDate = dailyDate.clone().apply { (this as Calendar).add(Calendar.DAY_OF_YEAR, 1) } as Calendar },
                                onPreviousWeekly = { weeklyDate = weeklyDate.clone().apply { (this as Calendar).add(Calendar.DAY_OF_YEAR, -7) } as Calendar },
                                onNextWeekly = { weeklyDate = weeklyDate.clone().apply { (this as Calendar).add(Calendar.DAY_OF_YEAR, 7) } as Calendar },
                                onPreviousMonthly = { monthlyDate = monthlyDate.clone().apply { (this as Calendar).add(Calendar.MONTH, -1) } as Calendar },
                                onNextMonthly = { monthlyDate = monthlyDate.clone().apply { (this as Calendar).add(Calendar.MONTH, 1) } as Calendar },
                                onPreviousYearly = { yearlyDate = yearlyDate.clone().apply { (this as Calendar).add(Calendar.YEAR, -1) } as Calendar },
                                onNextYearly = { yearlyDate = yearlyDate.clone().apply { (this as Calendar).add(Calendar.YEAR, 1) } as Calendar },
                                onDateClick = {
                                    activeDatePickerTarget = when (selectedPeriod) {
                                        ChartPeriod.DAY -> "daily"
                                        ChartPeriod.WEEK -> "weekly"
                                        ChartPeriod.MONTH -> "monthly"
                                        ChartPeriod.YEAR -> "yearly"
                                    }
                                },
                                dailyLimit = dailyLimit,
                                showLimitOnGraph = showLimitOnGraph,
                                vibrationEnabled = vibrationEnabled
                            )
                        }

                        item {
                            WeeklyComparisonCard(comparison = weeklyComparison)
                        }

                        item {
                            PeakSmokingHoursSection(distribution = hourlyDistribution, vibrationEnabled = vibrationEnabled)
                        }
                    }
                }
                1 -> {
                    val stats = remember(entries) { StatisticsManager().calculateStats(entries) }
                    if (entries.isEmpty() && resistedCount == 0 && baselineAnalytics == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = MaterialShapes.Cookie9Sided.toShape(),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Analytics,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = stringResource(R.string.stats_no_data),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.stats_no_data_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        StatisticsList(
                            stats = stats,
                            entries = entries,
                            resistedCount = resistedCount,
                            dailyLimit = dailyLimit,
                            packPrice = packPrice,
                            packSize = packSize,
                            currency = currency,
                            baselineAnalytics = baselineAnalytics,
                            hasHistoricalBaseline = hasHistoricalBaseline,
                            onOpenBaselineSheet = { showBaselineSheet = true },
                            onNavigateToSettings = onNavigateToSettings
                        )
                    }
                }
                2 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        TriggersTab(
                            triggerEntities = triggerEntities,
                            onOpenTriggerManagement = { showTriggerManagementSheet = true },
                            vibrationEnabled = vibrationEnabled
                        )
                    }
                }
            }
        }
    }
}

enum class ChartPeriod {
    DAY, WEEK, MONTH, YEAR
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HeroAnalyticsCard(
    selectedPeriod: ChartPeriod,
    onPeriodSelected: (ChartPeriod) -> Unit,
    dailyDate: Calendar,
    weeklyDate: Calendar,
    monthlyDate: Calendar,
    yearlyDate: Calendar,
    dailyData: List<Int>,
    weeklyData: List<Int>,
    monthlyData: List<Int>,
    yearlyData: List<Int>,
    dailyStr: String,
    weeklyStr: String,
    monthlyStr: String,
    yearlyStr: String,
    canGoNextDaily: Boolean,
    canGoNextWeekly: Boolean,
    canGoNextMonthly: Boolean,
    canGoNextYearly: Boolean,
    onPreviousDaily: () -> Unit,
    onNextDaily: () -> Unit,
    onPreviousWeekly: () -> Unit,
    onNextWeekly: () -> Unit,
    onPreviousMonthly: () -> Unit,
    onNextMonthly: () -> Unit,
    onPreviousYearly: () -> Unit,
    onNextYearly: () -> Unit,
    onDateClick: () -> Unit,
    dailyLimit: Int,
    showLimitOnGraph: Boolean,
    vibrationEnabled: Boolean
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current

    val (currentData, currentDateStr, canGoNext, onPrev, onNext) = when (selectedPeriod) {
        ChartPeriod.DAY -> Quintuple(dailyData, dailyStr, canGoNextDaily, onPreviousDaily, onNextDaily)
        ChartPeriod.WEEK -> Quintuple(weeklyData, weeklyStr, canGoNextWeekly, onPreviousWeekly, onNextWeekly)
        ChartPeriod.MONTH -> Quintuple(monthlyData, monthlyStr, canGoNextMonthly, onPreviousMonthly, onNextMonthly)
        ChartPeriod.YEAR -> Quintuple(yearlyData, yearlyStr, canGoNextYearly, onPreviousYearly, onNextYearly)
    }

    val totalCount = currentData.sum()

    val xAxisLabels = remember(selectedPeriod, weeklyDate, monthlyData.size) {
        when (selectedPeriod) {
            ChartPeriod.DAY -> listOf("00:00", "06:00", "12:00", "18:00", "23:00")
            ChartPeriod.WEEK -> {
                val cal = weeklyDate.clone() as Calendar
                cal.firstDayOfWeek = Calendar.MONDAY
                while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }
                val dayFormat = SimpleDateFormat("EE", Locale.getDefault())
                (0..6).map {
                    val name = dayFormat.format(cal.time)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
            }
            ChartPeriod.MONTH -> {
                val daysInMonth = monthlyData.size.coerceAtLeast(1)
                listOf("1", "5", "10", "15", "20", "25", "$daysInMonth")
            }
            ChartPeriod.YEAR -> {
                val cal = Calendar.getInstance()
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                (0..11).map {
                    cal.set(Calendar.MONTH, it)
                    monthFormat.format(cal.time).replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() }
                }
            }
        }
    }

    val tooltipFormatter: (Int) -> String = remember(selectedPeriod, dailyDate, weeklyDate, monthlyDate, yearlyDate) {
        { index ->
            when (selectedPeriod) {
                ChartPeriod.DAY -> {
                    val nextHour = (index + 1) % 24
                    String.format(Locale.getDefault(), "%02d:00 – %02d:00", index, nextHour)
                }
                ChartPeriod.WEEK -> {
                    val cal = weeklyDate.clone() as Calendar
                    cal.firstDayOfWeek = Calendar.MONDAY
                    while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    cal.add(Calendar.DAY_OF_YEAR, index)
                    SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(cal.time)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
                ChartPeriod.MONTH -> {
                    val cal = monthlyDate.clone() as Calendar
                    cal.set(Calendar.DAY_OF_MONTH, index + 1)
                    SimpleDateFormat("d MMMM", Locale.getDefault()).format(cal.time)
                }
                ChartPeriod.YEAR -> {
                    val cal = yearlyDate.clone() as Calendar
                    cal.set(Calendar.MONTH, index)
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroPeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = {
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    onPeriodSelected(it)
                }
            )

            val prevInteractionSource = remember { MutableInteractionSource() }
            val prevBouncy = rememberBouncyPress(interactionSource = prevInteractionSource, targetScale = 0.88f)
            val nextInteractionSource = remember { MutableInteractionSource() }
            val nextBouncy = rememberBouncyPress(interactionSource = nextInteractionSource, targetScale = 0.88f)
            val dateInteractionSource = remember { MutableInteractionSource() }
            val dateBouncy = rememberBouncyPress(interactionSource = dateInteractionSource, targetScale = 0.95f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$totalCount",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.history_cigs_count_format, "").trim(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.stats_total_count),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    shape = containerShape(CircleShape),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                prevBouncy.bounce()
                                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                onPrev()
                            },
                            interactionSource = prevInteractionSource,
                            modifier = Modifier.size(36.dp).bouncyPress(prevBouncy)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                dateBouncy.bounce()
                                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                onDateClick()
                            },
                            interactionSource = dateInteractionSource,
                            shape = CircleShape,
                            color = Color.Transparent,
                            modifier = Modifier.bouncyPress(dateBouncy)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentDateStr,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                nextBouncy.bounce()
                                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                onNext()
                            },
                            enabled = canGoNext,
                            interactionSource = nextInteractionSource,
                            modifier = Modifier.size(36.dp).bouncyPress(nextBouncy)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next",
                                modifier = Modifier.size(18.dp),
                                tint = if (canGoNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            if (totalCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = MaterialShapes.Cookie9Sided.toShape(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.graph_no_data),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.graph_no_data_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                InteractiveAnalyticsChart(
                    dataPoints = currentData,
                    selectedPeriod = selectedPeriod,
                    dateKey = currentDateStr,
                    dailyLimit = dailyLimit,
                    showLimitOnGraph = showLimitOnGraph,
                    vibrationEnabled = vibrationEnabled,
                    xAxisLabels = xAxisLabels,
                    tooltipDateFormatter = tooltipFormatter
                )
            }

            val avgPerUnit = remember(totalCount, currentData.size) {
                if (currentData.isNotEmpty()) totalCount.toFloat() / currentData.size.toFloat() else 0f
            }
            val peakCount = remember(currentData) { currentData.maxOrNull() ?: 0 }
            val isLimitApplicable = showLimitOnGraph && dailyLimit > 0 && (selectedPeriod == ChartPeriod.WEEK || selectedPeriod == ChartPeriod.MONTH)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.graph_metric_avg),
                    value = String.format(Locale.getDefault(), "%.1f", avgPerUnit),
                    unit = stringResource(R.string.history_cigs_count_format, "").trim(),
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    tint = MaterialTheme.colorScheme.secondary
                )

                if (isLimitApplicable) {
                    val inLimitCount = currentData.count { it <= dailyLimit }
                    MetricKpiCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.graph_metric_in_limit),
                        value = "$inLimitCount/${currentData.size}",
                        unit = stringResource(R.string.tapering_plan_pace_days, currentData.size).replace(currentData.size.toString(), "").trim(),
                        icon = Icons.Filled.Shield,
                        tint = if (inLimitCount == currentData.size) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    MetricKpiCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.graph_metric_peak),
                        value = "$peakCount",
                        unit = stringResource(R.string.history_cigs_count_format, "").trim(),
                        icon = Icons.Filled.Bolt,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
fun HeroPeriodSelector(
    selectedPeriod: ChartPeriod,
    onPeriodSelected: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(
        ChartPeriod.DAY to stringResource(R.string.graph_period_day),
        ChartPeriod.WEEK to stringResource(R.string.graph_period_week),
        ChartPeriod.MONTH to stringResource(R.string.graph_period_month),
        ChartPeriod.YEAR to stringResource(R.string.graph_period_year)
    )

    val selectedIndex = selectedPeriod.ordinal
    val animatedSelectedTab by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 400f
        ),
        label = "periodIndicatorOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(3.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val indicatorWidth = maxWidth / periods.size
            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .fillMaxHeight()
                    .offset(x = indicatorWidth * animatedSelectedTab)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            periods.forEachIndexed { index, (period, title) ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "periodTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onPeriodSelected(period)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveAnalyticsChart(
    dataPoints: List<Int>,
    selectedPeriod: ChartPeriod,
    dateKey: String = "",
    dailyLimit: Int,
    showLimitOnGraph: Boolean,
    vibrationEnabled: Boolean,
    xAxisLabels: List<String>,
    tooltipDateFormatter: (Int) -> String,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedIndex by remember(dataPoints, selectedPeriod, dateKey) { mutableStateOf<Int?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val isLimitApplicable = showLimitOnGraph && dailyLimit > 0 && (selectedPeriod == ChartPeriod.WEEK || selectedPeriod == ChartPeriod.MONTH)
    val maxData = (dataPoints.maxOrNull() ?: 0).coerceAtLeast(1)
    val ceilingValue = if (isLimitApplicable) maxOf(maxData, dailyLimit) else maxData
    val maxY = (ceilingValue * 1.25f).coerceAtLeast(4f)

    val animProgress = remember(dataPoints, selectedPeriod, dateKey) { Animatable(0f) }
    LaunchedEffect(dataPoints, selectedPeriod, dateKey) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dataPoints, selectedPeriod, dateKey) {
                        detectTapGestures(
                            onTap = { offset ->
                                val n = dataPoints.size
                                if (n > 0) {
                                    val idx = ((offset.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                    selectedIndex = if (selectedIndex == idx) null else idx
                                    if (selectedIndex != null) {
                                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                    }
                                }
                            }
                        )
                    }
                    .pointerInput(dataPoints, selectedPeriod, dateKey) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                val n = dataPoints.size
                                if (n > 0) {
                                    val idx = ((offset.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                    selectedIndex = idx
                                    com.smokingtracker.ui.theme.HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                }
                            },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                val n = dataPoints.size
                                if (n > 0) {
                                    val idx = ((change.position.x / size.width) * n).toInt().coerceIn(0, n - 1)
                                    if (idx != selectedIndex) {
                                        selectedIndex = idx
                                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                    }
                                }
                            },
                            onDragEnd = {},
                            onDragCancel = {}
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val n = dataPoints.size
                if (n == 0) return@Canvas

                val gridLines = 3
                for (i in 1..gridLines) {
                    val y = canvasHeight - (i * (canvasHeight / (gridLines + 0.5f)))
                    drawLine(
                        color = outlineVariant.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (isLimitApplicable) {
                    val limitY = canvasHeight - (dailyLimit.toFloat() / maxY) * canvasHeight
                    drawLine(
                        color = errorColor.copy(alpha = 0.65f),
                        start = Offset(0f, limitY),
                        end = Offset(canvasWidth, limitY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }

                val slotWidth = canvasWidth / n
                val barWidth = when (selectedPeriod) {
                    ChartPeriod.DAY -> (slotWidth * 0.55f).coerceIn(4.dp.toPx(), 10.dp.toPx())
                    ChartPeriod.WEEK -> (slotWidth * 0.42f).coerceIn(16.dp.toPx(), 28.dp.toPx())
                    ChartPeriod.MONTH -> (slotWidth * 0.6f).coerceIn(3.dp.toPx(), 8.dp.toPx())
                    ChartPeriod.YEAR -> (slotWidth * 0.42f).coerceIn(12.dp.toPx(), 22.dp.toPx())
                }
                val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

                dataPoints.forEachIndexed { index, count ->
                    val cx = (index + 0.5f) * slotWidth
                    val left = cx - barWidth / 2f
                    val right = cx + barWidth / 2f
                    val isSelected = selectedIndex == index
                    val fullHeight = (count.toFloat() / maxY) * canvasHeight * animProgress.value

                    if (count == 0) {
                        val zeroH = 3.dp.toPx()
                        drawRoundRect(
                            color = outlineVariant.copy(alpha = 0.35f),
                            topLeft = Offset(left, canvasHeight - zeroH),
                            size = Size(barWidth, zeroH),
                            cornerRadius = CornerRadius(zeroH / 2f, zeroH / 2f)
                        )
                    } else if (isLimitApplicable && count > dailyLimit) {
                        val excessRatio = ((count - dailyLimit).toFloat() / count.toFloat()).coerceIn(0f, 1f)
                        val baseColor = if (isSelected) tertiaryColor else primaryColor.copy(alpha = 0.85f)
                        val excessColor = if (isSelected) errorColor else errorColor.copy(alpha = 0.85f)

                        val barBrush = Brush.verticalGradient(
                            0.0f to excessColor,
                            excessRatio to excessColor,
                            excessRatio to baseColor,
                            1.0f to baseColor,
                            startY = canvasHeight - fullHeight,
                            endY = canvasHeight
                        )

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(left, canvasHeight - fullHeight),
                            size = Size(barWidth, fullHeight),
                            cornerRadius = cornerRadius
                        )
                    } else {
                        val barColor = if (isSelected) {
                            tertiaryColor
                        } else {
                            primaryColor.copy(alpha = 0.85f)
                        }
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, canvasHeight - fullHeight),
                            size = Size(barWidth, fullHeight),
                            cornerRadius = cornerRadius
                        )
                    }

                    if (isSelected) {
                        drawLine(
                            color = onSurfaceVariantColor.copy(alpha = 0.4f),
                            start = Offset(cx, 0f),
                            end = Offset(cx, canvasHeight),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                        drawCircle(
                            color = onSurfaceColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(cx, (canvasHeight - fullHeight - 6.dp.toPx()).coerceAtLeast(6.dp.toPx()))
                        )
                    }
                }
            }

            val sel = selectedIndex
            if (sel != null && sel in dataPoints.indices) {
                val count = dataPoints[sel]
                val dateInfo = tooltipDateFormatter(sel)
                val isOverLimit = isLimitApplicable && count > dailyLimit
                val overLimitDiff = count - dailyLimit

                Surface(
                    shape = containerShape(RoundedCornerShape(14.dp)),
                    color = if (isOverLimit) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = if (isOverLimit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = dateInfo,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.history_cigs_count_format, count.toString()),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        if (isLimitApplicable && count > 0) {
                            Text(
                                text = if (isOverLimit) stringResource(R.string.graph_over_limit_badge, overLimitDiff)
                                       else stringResource(R.string.graph_within_limit_badge),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (xAxisLabels.size == dataPoints.size) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                xAxisLabels.forEachIndexed { index, label ->
                    val isHighlighted = selectedIndex == index
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                xAxisLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String = "",
    icon: ImageVector,
    tint: Color
) {
    Surface(
        modifier = modifier,
        shape = containerShape(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = tint.copy(alpha = 0.14f),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GraphScreenPreview() {
    MaterialTheme {
        GraphScreenContent(entries = emptyList())
    }
}

enum class TriggerPeriod {
    ALL_TIME, MONTH, WEEK
}

private fun calculatePeakPeriodForTrigger(entities: List<com.smokingtracker.data.local.SmokingEntryEntity>, triggerKey: String): Int? {
    val relevantEntities = entities.filter { it.trigger == triggerKey }
    if (relevantEntities.isEmpty()) return null

    var night = 0
    var morning = 0
    var afternoon = 0
    var evening = 0

    val cal = Calendar.getInstance()
    relevantEntities.forEach { entity ->
        cal.timeInMillis = entity.timestamp
        when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> night++
            in 6..11 -> morning++
            in 12..17 -> afternoon++
            else -> evening++
        }
    }

    val periods = listOf(
        night to R.string.peak_period_night,
        morning to R.string.peak_period_morning,
        afternoon to R.string.peak_period_afternoon,
        evening to R.string.peak_period_evening
    )

    return periods.maxByOrNull { it.first }?.second
}

private fun getCopingStrategyTip(triggerKey: String): Int {
    val type = TriggerType.fromKey(triggerKey)
    return when (type) {
        TriggerType.STRESS -> R.string.trigger_tip_stress
        TriggerType.BOREDOM -> R.string.trigger_tip_boredom
        TriggerType.SOCIAL -> R.string.trigger_tip_social
        TriggerType.ROUTINE -> R.string.trigger_tip_routine
        TriggerType.FOOD_COFFEE -> R.string.trigger_tip_food_coffee
        TriggerType.ALCOHOL -> R.string.trigger_tip_alcohol
        null -> R.string.trigger_tip_custom
    }
}

@Composable
fun TriggerPeriodSelector(
    selectedPeriod: TriggerPeriod,
    onPeriodSelected: (TriggerPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(
        TriggerPeriod.ALL_TIME to stringResource(R.string.trigger_period_all),
        TriggerPeriod.MONTH to stringResource(R.string.trigger_period_month),
        TriggerPeriod.WEEK to stringResource(R.string.trigger_period_week)
    )

    val selectedIndex = selectedPeriod.ordinal
    val animatedSelectedTab by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 400f
        ),
        label = "triggerPeriodIndicatorOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(3.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val indicatorWidth = maxWidth / periods.size
            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .fillMaxHeight()
                    .offset(x = indicatorWidth * animatedSelectedTab)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Row(modifier = Modifier.fillMaxSize()) {
                periods.forEach { (period, title) ->
                    val isSelected = selectedPeriod == period
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200),
                        label = "triggerPeriodTextColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onPeriodSelected(period) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopTriggerHeroCard(
    triggerKey: String,
    triggerName: String,
    count: Int,
    percent: Int,
    peakPeriodResId: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ContainerIcon(
                    icon = getTriggerIcon(triggerKey),
                    tint = MaterialTheme.colorScheme.onTertiary,
                    backdropColor = MaterialTheme.colorScheme.tertiary,
                    size = 48.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.main_trigger),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = triggerName,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Surface(
                    shape = containerShape(CircleShape),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (peakPeriodResId != null) {
                Surface(
                    shape = containerShape(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.trigger_peak_time_prefix, stringResource(peakPeriodResId)),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            val tipResId = remember(triggerKey) { getCopingStrategyTip(triggerKey) }
            Surface(
                shape = containerShape(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.trigger_strategy_title),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(tipResId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ManageTriggersCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = MaterialShapes.Cookie9Sided.toShape(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.trigger_manage_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.trigger_manage_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TriggersTab(
    triggerEntities: List<com.smokingtracker.data.local.SmokingEntryEntity>,
    onOpenTriggerManagement: () -> Unit = {},
    vibrationEnabled: Boolean = true
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedPeriod by remember { mutableStateOf(TriggerPeriod.ALL_TIME) }

    val totalAllTimeTriggers = remember(triggerEntities) {
        triggerEntities.count { it.trigger != null }
    }

    if (totalAllTimeTriggers == 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = MaterialShapes.Cookie9Sided.toShape(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.triggers_no_data_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.triggers_no_data_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                FilledTonalButton(
                    onClick = {
                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        onOpenTriggerManagement()
                    },
                    shape = containerShape(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.trigger_manage_title),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        return
    }

    val filteredEntities = remember(triggerEntities, selectedPeriod) {
        val now = System.currentTimeMillis()
        when (selectedPeriod) {
            TriggerPeriod.ALL_TIME -> triggerEntities.filter { it.trigger != null }
            TriggerPeriod.MONTH -> {
                val cutoff = now - 30L * 24 * 60 * 60 * 1000L
                triggerEntities.filter { it.trigger != null && it.timestamp >= cutoff }
            }
            TriggerPeriod.WEEK -> {
                val cutoff = now - 7L * 24 * 60 * 60 * 1000L
                triggerEntities.filter { it.trigger != null && it.timestamp >= cutoff }
            }
        }
    }

    val periodTriggerCounts = remember(filteredEntities) {
        val counts = mutableMapOf<String, Int>()
        filteredEntities.forEach { entity ->
            val trigger = entity.trigger
            if (trigger != null) {
                counts[trigger] = (counts[trigger] ?: 0) + 1
            }
        }
        counts
    }

    val periodTotalCount = remember(periodTriggerCounts) {
        periodTriggerCounts.values.sum()
    }

    val sortedTriggers = remember(periodTriggerCounts) {
        periodTriggerCounts.toList().sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TriggerPeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { period ->
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    selectedPeriod = period
                }
            )
        }

        if (periodTotalCount == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = MaterialShapes.Cookie9Sided.toShape(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.EventBusy,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.triggers_period_no_data),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val mostFrequent = sortedTriggers.firstOrNull()
            if (mostFrequent != null && mostFrequent.second > 0) {
                item {
                    val topKey = mostFrequent.first
                    val topCount = mostFrequent.second
                    val topPercent = if (periodTotalCount > 0) (topCount.toFloat() / periodTotalCount * 100).toInt() else 0
                    val triggerType = TriggerType.fromKey(topKey)
                    val triggerName = triggerType?.let { stringResource(it.labelResId) } ?: topKey

                    val peakPeriodResId = remember(filteredEntities, topKey) {
                        calculatePeakPeriodForTrigger(filteredEntities, topKey)
                    }

                    TopTriggerHeroCard(
                        triggerKey = topKey,
                        triggerName = triggerName,
                        count = topCount,
                        percent = topPercent,
                        peakPeriodResId = peakPeriodResId
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.triggers_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(28.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        sortedTriggers.forEach { (triggerKey, count) ->
                            val triggerType = TriggerType.fromKey(triggerKey)
                            val triggerName = triggerType?.let { stringResource(it.labelResId) } ?: triggerKey
                            val percent = if (periodTotalCount > 0) count.toFloat() / periodTotalCount.toFloat() else 0f

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        val cookieShape = MaterialShapes.Cookie9Sided.toShape()
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(cookieShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getTriggerIcon(triggerKey),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = triggerName,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Text(
                                        text = stringResource(R.string.trigger_count_pattern, count, (percent * 100).toInt()),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                AnimatedTriggerProgressBar(
                                    targetProgress = percent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            ManageTriggersCard(
                onClick = {
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    onOpenTriggerManagement()
                }
            )
        }
    }
}

@Composable
fun ExpressiveTabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<String>,
    modifier: Modifier = Modifier
) {
    val animatedSelectedTab by animateFloatAsState(
        targetValue = selectedTab.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 400f
        ),
        label = "tabIndicatorOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(4.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val indicatorWidth = maxWidth / tabs.size
            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .fillMaxHeight()
                    .offset(x = indicatorWidth * animatedSelectedTab)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTab
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "tabTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedTriggerProgressBar(
    targetProgress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animProgress = remember { Animatable(0f) }
    val animWaveScale = remember { Animatable(1f) }

    LaunchedEffect(targetProgress) {
        if (targetProgress > 0f) {
            animWaveScale.snapTo(1f)
            launch {
                animProgress.animateTo(
                    targetValue = targetProgress,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                )
            }
            launch {
                animWaveScale.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                )
            }
        } else {
            animProgress.snapTo(0f)
            animWaveScale.snapTo(0f)
        }
    }

    val progressValue = animProgress.value
    val waveScale = animWaveScale.value

    if (waveScale > 0f) {
        LinearWavyProgressIndicator(
            progress = { progressValue },
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            amplitude = {
                1f * waveScale
            }
        )
    } else {
        LinearWavyProgressIndicator(
            progress = { progressValue },
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            amplitude = { 0f },
            waveSpeed = 0.dp
        )
    }
}

private fun getTriggerIcon(triggerKey: String?): ImageVector {
    if (triggerKey == null) return Icons.Filled.SmokingRooms
    val trigger = TriggerType.fromKey(triggerKey)
    return when (trigger) {
        TriggerType.STRESS -> Icons.Filled.Bolt
        TriggerType.BOREDOM -> Icons.Filled.HourglassEmpty
        TriggerType.SOCIAL -> Icons.Filled.People
        TriggerType.ROUTINE -> Icons.Filled.Repeat
        TriggerType.FOOD_COFFEE -> Icons.Filled.LocalCafe
        TriggerType.ALCOHOL -> Icons.Filled.LocalBar
        null -> Icons.Filled.Psychology
    }
}

@Composable
private fun WeeklyComparisonCard(comparison: StatisticsManager.WeeklyComparisonData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weekly_comparison_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val (badgeBg, badgeFg, badgeText, badgeIcon) = when (comparison.trend) {
                    StatisticsManager.ComparisonTrend.DECREASED -> Quadruple(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        stringResource(R.string.weekly_comparison_decrease, comparison.percentChange),
                        Icons.AutoMirrored.Filled.TrendingDown
                    )
                    StatisticsManager.ComparisonTrend.INCREASED -> Quadruple(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer,
                        stringResource(R.string.weekly_comparison_increase, comparison.percentChange),
                        Icons.AutoMirrored.Filled.TrendingUp
                    )
                    StatisticsManager.ComparisonTrend.NO_CHANGE -> Quadruple(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        stringResource(R.string.weekly_comparison_no_change),
                        Icons.Filled.Remove
                    )
                }

                Surface(
                    shape = containerShape(RoundedCornerShape(14.dp)),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeFg,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = badgeFg
                        )
                    }
                }
            }

            val desc = when (comparison.trend) {
                StatisticsManager.ComparisonTrend.DECREASED -> stringResource(R.string.weekly_comparison_desc_decrease, Math.abs(comparison.difference))
                StatisticsManager.ComparisonTrend.INCREASED -> stringResource(R.string.weekly_comparison_desc_increase, Math.abs(comparison.difference))
                StatisticsManager.ComparisonTrend.NO_CHANGE -> stringResource(R.string.weekly_comparison_desc_no_change)
            }

            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            val maxCount = maxOf(comparison.thisWeekCount, comparison.lastWeekCount).coerceAtLeast(1)
            val thisWeekFraction = (comparison.thisWeekCount.toFloat() / maxCount).coerceIn(0f, 1f)
            val lastWeekFraction = (comparison.lastWeekCount.toFloat() / maxCount).coerceIn(0f, 1f)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.weekly_comparison_this_week_label),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "${comparison.thisWeekCount}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.history_cigs_count_format, "").trim(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { thisWeekFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.weekly_comparison_last_week_label),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "${comparison.lastWeekCount}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.history_cigs_count_format, "").trim(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { lastWeekFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun PeakSmokingHoursSection(distribution: StatisticsManager.HourlyDistributionData, vibrationEnabled: Boolean = true) {
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var isAnimated by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        isAnimated = true
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.peak_hours_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.peak_hours_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (distribution.hourlyCounts.all { it == 0 }) {
                Text(
                    text = stringResource(R.string.peak_hours_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val periodText = stringResource(distribution.peakPeriodNameResId)
                val summaryText = if (selectedHour != null) {
                    val selCount = distribution.hourlyCounts[selectedHour!!]
                    val startH = selectedHour!!
                    val endH = (selectedHour!! + 1) % 24
                    val formatTime = String.format(Locale.getDefault(), "%02d:00 - %02d:00", startH, endH)
                    val countStr = stringResource(R.string.history_cigs_count_format, selCount.toString())
                    stringResource(R.string.peak_hours_selected_format, formatTime, countStr)
                } else {
                    stringResource(R.string.peak_hours_summary, periodText, distribution.peakPeriodPercent)
                }

                Surface(
                    shape = containerShape(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                val maxCount = distribution.hourlyCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
                val primaryColor = MaterialTheme.colorScheme.primary

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        distribution.hourlyCounts.forEachIndexed { hour, count ->
                            val targetFraction = if (count == 0) 0.05f else (count.toFloat() / maxCount).coerceIn(0.08f, 1.0f)
                            val animatedFraction by animateFloatAsState(
                                targetValue = if (isAnimated) targetFraction else 0.03f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "bar_$hour"
                            )

                            val isPeak = hour == distribution.peakHour && count > 0
                            val isSelected = hour == selectedHour

                            val barColor = when {
                                isSelected -> MaterialTheme.colorScheme.tertiary
                                isPeak -> primaryColor
                                count > 0 -> primaryColor.copy(alpha = 0.45f)
                                else -> primaryColor.copy(alpha = 0.12f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(animatedFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(barColor)
                                    .clickable {
                                        selectedHour = if (selectedHour == hour) null else hour
                                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("00:00", "06:00", "12:00", "18:00", "23:00").forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

