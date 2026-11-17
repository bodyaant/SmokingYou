package com.smokingtracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smokingtracker.R
import com.smokingtracker.StatisticsData
import com.smokingtracker.StatisticsManager
import com.smokingtracker.StatisticsViewModel
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.ui.graph.cards.HeroAnalyticsCard
import com.smokingtracker.ui.graph.cards.PeakSmokingHoursCard
import com.smokingtracker.ui.graph.components.ExpressiveTabSelector
import com.smokingtracker.ui.graph.models.ChartPeriod
import com.smokingtracker.ui.graph.sheets.PeakSmokingHoursBottomSheet
import com.smokingtracker.ui.graph.sheets.WeeklyComparisonBottomSheet
import com.smokingtracker.ui.graph.triggers.TriggersTab
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerShape
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val CalendarSaver: Saver<Calendar, Long> = Saver(
    save = { it.timeInMillis },
    restore = { Calendar.getInstance().apply { timeInMillis = it } }
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GraphScreen(
    viewModel: StatisticsViewModel = koinViewModel(),
    initialTarget: String? = null,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val entries by viewModel.smokingEntries.collectAsStateWithLifecycle()
    val nonResistedEntities by viewModel.nonResistedEntities.collectAsStateWithLifecycle()
    val resistedEntries by viewModel.resistedEntries.collectAsStateWithLifecycle()
    val resistedTimestamps by viewModel.resistedTimestamps.collectAsStateWithLifecycle()
    val firstAppLaunchDate by viewModel.firstAppLaunchDate.collectAsStateWithLifecycle()
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
    val savingsGoalTitle by viewModel.savingsGoalTitle.collectAsStateWithLifecycle()
    val savingsGoalAmount by viewModel.savingsGoalAmount.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAnalyticsTabVisited()
    }

    GraphScreenContent(
        entries = entries,
        resistedCount = resistedEntries.size,
        resistedTimestamps = resistedTimestamps,
        trackingStartMillis = firstAppLaunchDate,
        triggerEntities = nonResistedEntities,
        dailyLimit = dailyLimit,
        showLimitOnGraph = showLimitOnGraph,
        packPrice = packPrice,
        packSize = packSize,
        currency = currency,
        vibrationEnabled = vibrationEnabled,
        savingsGoalTitle = savingsGoalTitle,
        savingsGoalAmount = savingsGoalAmount,
        onSaveSavingsGoal = viewModel::saveSavingsGoal,
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
        onToggleDefaultTrigger = viewModel::toggleDefaultTrigger,
        statisticsManager = viewModel.statisticsManager
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GraphScreenContent(
    entries: List<Long>,
    resistedCount: Int = 0,
    resistedTimestamps: List<Long> = emptyList(),
    trackingStartMillis: Long? = null,
    triggerEntities: List<SmokingEntryEntity> = emptyList(),
    dailyLimit: Int = 0,
    showLimitOnGraph: Boolean = true,
    packPrice: Float = 0f,
    packSize: Int = 20,
    currency: String = "",
    vibrationEnabled: Boolean = true,
    savingsGoalTitle: String = "",
    savingsGoalAmount: Float = 0f,
    onSaveSavingsGoal: (String, Float) -> Unit = { _, _ -> },
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
    onToggleDefaultTrigger: (String, Boolean) -> Unit = { _, _ -> },
    statisticsManager: StatisticsManager = koinInject()
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showBaselineSheet by rememberSaveable { mutableStateOf(false) }
    var showTriggerManagementSheet by rememberSaveable { mutableStateOf(false) }

    val baselineAnalytics by produceState<StatisticsManager.SmokingBaselineAnalytics?>(
        initialValue = null,
        hasHistoricalBaseline,
        historicalStartDate,
        historicalDailyAvg,
        packPrice,
        packSize,
        entries,
        resistedTimestamps,
        trackingStartMillis,
        statisticsManager
    ) {
        value = if (hasHistoricalBaseline) {
            statisticsManager.calculateBaselineAnalyticsAsync(
                startDate = historicalStartDate,
                dailyAvg = historicalDailyAvg,
                packPrice = packPrice,
                packSize = packSize,
                entries = entries,
                resistedEntries = resistedTimestamps,
                trackingStartMillis = trackingStartMillis
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

    var selectedPeriod by rememberSaveable { mutableStateOf(ChartPeriod.WEEK) }

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

    var dailyDate by rememberSaveable(stateSaver = CalendarSaver) { mutableStateOf(Calendar.getInstance()) }
    var weeklyDate by rememberSaveable(stateSaver = CalendarSaver) { mutableStateOf(Calendar.getInstance()) }
    var monthlyDate by rememberSaveable(stateSaver = CalendarSaver) { mutableStateOf(Calendar.getInstance()) }
    var yearlyDate by rememberSaveable(stateSaver = CalendarSaver) { mutableStateOf(Calendar.getInstance()) }

    val dailyData by produceState(
        initialValue = StatisticsManager.EMPTY_DAILY_DATA,
        entries,
        dailyDate,
        statisticsManager
    ) {
        value = statisticsManager.generateDailyDataAsync(entries, dailyDate)
    }
    val weeklyData by produceState(
        initialValue = StatisticsManager.EMPTY_WEEKLY_DATA,
        entries,
        weeklyDate,
        statisticsManager
    ) {
        value = statisticsManager.generateWeeklyDataAsync(entries, weeklyDate)
    }
    val monthlyData by produceState(
        initialValue = StatisticsManager.EMPTY_MONTHLY_CHUNKS,
        entries,
        monthlyDate,
        statisticsManager
    ) {
        value = statisticsManager.generateMonthlyDataAsync(entries, monthlyDate)
    }
    val monthlyDailyData by produceState(
        initialValue = List(30) { 0 },
        entries,
        monthlyDate,
        statisticsManager
    ) {
        value = statisticsManager.generateMonthlyDailyDataAsync(entries, monthlyDate)
    }
    val yearlyData by produceState(
        initialValue = StatisticsManager.EMPTY_YEARLY_DATA,
        entries,
        yearlyDate,
        statisticsManager
    ) {
        value = statisticsManager.generateYearlyDataAsync(entries, yearlyDate)
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var activeDatePickerTarget by rememberSaveable { mutableStateOf<String?>(null) }
    val weeklyComparison by produceState(
        initialValue = StatisticsManager.WeeklyComparisonData.EMPTY,
        entries,
        weeklyDate,
        statisticsManager
    ) {
        value = statisticsManager.calculateWeeklyComparisonAsync(entries, weeklyDate)
    }
    var showWeeklyComparisonSheet by rememberSaveable { mutableStateOf(false) }
    val hourlyDistribution by produceState(
        initialValue = StatisticsManager.HourlyDistributionData.EMPTY,
        entries,
        statisticsManager
    ) {
        value = statisticsManager.calculateHourlyDistributionAsync(entries)
    }
    var showPeakHoursSheet by rememberSaveable { mutableStateOf(false) }

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
                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
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

    if (showWeeklyComparisonSheet && (weeklyComparison.thisWeekCount > 0 || weeklyComparison.lastWeekCount > 0)) {
        WeeklyComparisonBottomSheet(
            comparison = weeklyComparison,
            weeklyDate = weeklyDate,
            vibrationEnabled = vibrationEnabled,
            onDismissRequest = { showWeeklyComparisonSheet = false }
        )
    }

    if (showPeakHoursSheet) {
        PeakSmokingHoursBottomSheet(
            distribution = hourlyDistribution,
            vibrationEnabled = vibrationEnabled,
            onDismissRequest = { showPeakHoursSheet = false }
        )
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
                    colors = TopAppBarDefaults.topAppBarColors(
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
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item(key = "hero_analytics_card") {
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
                                SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(monthlyDate.time)
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
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
                                vibrationEnabled = vibrationEnabled,
                                packPrice = packPrice,
                                packSize = packSize,
                                currency = currency,
                                triggerEntities = triggerEntities,
                                weeklyComparison = weeklyComparison,
                                onShowWeeklyComparison = { showWeeklyComparisonSheet = true },
                                hourlyDistribution = hourlyDistribution,
                                onShowPeakHours = { showPeakHoursSheet = true }
                            )
                        }

                        item(key = "peak_smoking_hours_card") {
                            AnimatedVisibility(
                                visible = selectedPeriod != ChartPeriod.DAY,
                                enter = expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(200)),
                                exit = shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)) + fadeOut(tween(150))
                            ) {
                                PeakSmokingHoursCard(
                                    distribution = hourlyDistribution,
                                    vibrationEnabled = vibrationEnabled,
                                    onClick = { showPeakHoursSheet = true }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    val stats by produceState(
                        initialValue = StatisticsData.EMPTY,
                        entries,
                        trackingStartMillis,
                        statisticsManager
                    ) {
                        value = statisticsManager.calculateStatsAsync(
                            entries = entries,
                            trackingStartMillis = trackingStartMillis
                        )
                    }
                    if (entries.isEmpty() && resistedCount == 0 && !hasHistoricalBaseline && baselineAnalytics == null) {
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
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
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
                            trackingStartMillis = trackingStartMillis,
                            dailyLimit = dailyLimit,
                            packPrice = packPrice,
                            packSize = packSize,
                            currency = currency,
                            baselineAnalytics = baselineAnalytics,
                            hasHistoricalBaseline = hasHistoricalBaseline,
                            vibrationEnabled = vibrationEnabled,
                            savingsGoalTitle = savingsGoalTitle,
                            savingsGoalAmount = savingsGoalAmount,
                            onSaveSavingsGoal = onSaveSavingsGoal,
                            onOpenBaselineSheet = { showBaselineSheet = true },
                            onNavigateToSettings = onNavigateToSettings,
                            statisticsManager = statisticsManager
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

@Preview(showBackground = true)
@Composable
private fun GraphScreenPreview() {
    MaterialTheme {
        GraphScreenContent(entries = emptyList())
    }
}
