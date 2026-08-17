package com.smokingtracker.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.foundation.BorderStroke
import com.smokingtracker.ui.theme.containerBorder
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.ContainerIcon
import com.smokingtracker.ui.theme.LocalContainerStyle
import com.smokingtracker.data.ContainerStyle
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
fun GraphScreen(viewModel: MainViewModel, initialTarget: String? = null) {
    val entries by viewModel.smokingEntries.collectAsStateWithLifecycle()
    val entryTriggers by viewModel.entryTriggers.collectAsStateWithLifecycle()
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val packPrice by viewModel.packPrice.collectAsStateWithLifecycle()
    val packSize by viewModel.packSize.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val scrollTarget by viewModel.graphScrollTarget.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAnalyticsTabVisited()
    }

    GraphScreenContent(
        entries = entries,
        entryTriggers = entryTriggers,
        dailyLimit = dailyLimit,
        packPrice = packPrice,
        packSize = packSize,
        currency = currency,
        vibrationEnabled = vibrationEnabled,
        scrollTarget = scrollTarget,
        onClearScrollTarget = { viewModel.clearGraphScrollTarget() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GraphScreenContent(
    entries: List<Long>,
    entryTriggers: Map<Long, String>,
    dailyLimit: Int = 0,
    packPrice: Float = 0f,
    packSize: Int = 20,
    currency: String = "",
    vibrationEnabled: Boolean = false,
    scrollTarget: String? = null,
    onClearScrollTarget: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(scrollTarget) {
        if (scrollTarget != null) {
            pagerState.animateScrollToPage(0)
            kotlinx.coroutines.delay(100L)
            when (scrollTarget) {
                "daily" -> listState.animateScrollToItem(2)
                "weekly" -> listState.animateScrollToItem(3)
                "monthly" -> listState.animateScrollToItem(4)
            }
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
                    com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
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
                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
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
                        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

                        item {
                            WeeklyComparisonCard(comparison = weeklyComparison)
                        }

                        item {
                            PeakSmokingHoursSection(distribution = hourlyDistribution, vibrationEnabled = vibrationEnabled)
                        }

                        item {
                            val dailyStr = remember(dailyDate) { dateFormat.format(dailyDate.time) }
                            val today = Calendar.getInstance()
                            val canGoNextDaily = remember(dailyDate) {
                                dailyDate.get(Calendar.YEAR) < today.get(Calendar.YEAR) ||
                                (dailyDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                 dailyDate.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR))
                            }
                            GraphSection(
                                title = stringResource(R.string.daily_overview),
                                totalCount = dailyData.sum(),
                                dateLabel = dailyStr,
                                dataPoints = dailyData,
                                canGoNext = canGoNextDaily,
                                onPrevious = { dailyDate = dailyDate.clone().apply { (this as Calendar).add(Calendar.DAY_OF_YEAR, -1) } as Calendar },
                                onNext = { dailyDate = dailyDate.clone().apply { (this as Calendar).add(Calendar.DAY_OF_YEAR, 1) } as Calendar },
                                onDateClick = { activeDatePickerTarget = "daily" },
                                vibrationEnabled = vibrationEnabled
                            )
                        }

                        item {
                            val weeklyStr = remember(weeklyDate) {
                                val weekStart = weeklyDate.clone() as Calendar
                                weekStart.set(Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
                                val weekEnd = weekStart.clone() as Calendar
                                weekEnd.add(Calendar.DAY_OF_YEAR, 6)
                                
                                val shortFormat = SimpleDateFormat("d MMM", Locale.getDefault())
                                "${shortFormat.format(weekStart.time)} - ${shortFormat.format(weekEnd.time)}"
                            }
                            val today = Calendar.getInstance()
                            val canGoNextWeekly = remember(weeklyDate) {
                                val todayWeekStart = today.clone() as Calendar
                                todayWeekStart.set(Calendar.DAY_OF_WEEK, todayWeekStart.firstDayOfWeek)
                                val selectedWeekStart = weeklyDate.clone() as Calendar
                                selectedWeekStart.set(Calendar.DAY_OF_WEEK, selectedWeekStart.firstDayOfWeek)
                                selectedWeekStart.before(todayWeekStart)
                            }
                            GraphSection(
                                title = stringResource(R.string.weekly_overview),
                                totalCount = weeklyData.sum(),
                                dateLabel = weeklyStr,
                                dataPoints = weeklyData,
                                canGoNext = canGoNextWeekly,
                                onPrevious = { weeklyDate = weeklyDate.clone().apply { (this as Calendar).add(Calendar.WEEK_OF_YEAR, -1) } as Calendar },
                                onNext = { weeklyDate = weeklyDate.clone().apply { (this as Calendar).add(Calendar.WEEK_OF_YEAR, 1) } as Calendar },
                                onDateClick = { activeDatePickerTarget = "weekly" },
                                vibrationEnabled = vibrationEnabled
                            )
                        }

                        item {
                            val monthlyStr = remember(monthlyDate) {
                                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthlyDate.time)
                            }
                            val today = Calendar.getInstance()
                            val canGoNextMonthly = remember(monthlyDate) {
                                monthlyDate.get(Calendar.YEAR) < today.get(Calendar.YEAR) ||
                                (monthlyDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                 monthlyDate.get(Calendar.MONTH) < today.get(Calendar.MONTH))
                            }
                            GraphSection(
                                title = stringResource(R.string.monthly_overview),
                                totalCount = monthlyData.sum(),
                                dateLabel = monthlyStr,
                                dataPoints = monthlyData,
                                canGoNext = canGoNextMonthly,
                                onPrevious = { monthlyDate = monthlyDate.clone().apply { (this as Calendar).add(Calendar.MONTH, -1) } as Calendar },
                                onNext = { monthlyDate = monthlyDate.clone().apply { (this as Calendar).add(Calendar.MONTH, 1) } as Calendar },
                                onDateClick = { activeDatePickerTarget = "monthly" },
                                vibrationEnabled = vibrationEnabled
                            )
                        }

                        item {
                            val yearlyStr = remember(yearlyDate) {
                                SimpleDateFormat("yyyy", Locale.getDefault()).format(yearlyDate.time)
                            }
                            val today = Calendar.getInstance()
                            val canGoNextYearly = remember(yearlyDate) {
                                yearlyDate.get(Calendar.YEAR) < today.get(Calendar.YEAR)
                            }
                            GraphSection(
                                title = stringResource(R.string.yearly_overview),
                                totalCount = yearlyData.sum(),
                                dateLabel = yearlyStr,
                                dataPoints = yearlyData,
                                canGoNext = canGoNextYearly,
                                onPrevious = { yearlyDate = yearlyDate.clone().apply { (this as Calendar).add(Calendar.YEAR, -1) } as Calendar },
                                onNext = { yearlyDate = yearlyDate.clone().apply { (this as Calendar).add(Calendar.YEAR, 1) } as Calendar },
                                onDateClick = { activeDatePickerTarget = "yearly" },
                                vibrationEnabled = vibrationEnabled
                            )
                        }
                    }
                }
                1 -> {
                    val stats = remember(entries) { StatisticsManager().calculateStats(entries) }
                    if (entries.isEmpty()) {
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
                            dailyLimit = dailyLimit,
                            packPrice = packPrice,
                            packSize = packSize,
                            currency = currency,
                            onNavigateToSettings = null
                        )
                    }
                }
                2 -> {
                    val triggerCounts = remember(entries, entryTriggers) {
                        val counts = com.smokingtracker.data.TriggerType.allKeys()
                            .associateWith { 0 }.toMutableMap()
                        val entrySet = entries.toSet()
                        entryTriggers.forEach { (timestamp, trigger) ->
                            if (entrySet.contains(timestamp)) {
                                counts[trigger] = (counts[trigger] ?: 0) + 1
                            }
                        }
                        counts
                    }
                    
                    val totalTriggersLogged = triggerCounts.values.sum()
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        TriggersTab(triggerCounts = triggerCounts, totalCount = totalTriggersLogged)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GraphSection(
    title: String,
    totalCount: Int,
    dateLabel: String,
    dataPoints: List<Int>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    canGoNext: Boolean = true,
    onDateClick: (() -> Unit)? = null,
    vibrationEnabled: Boolean = false
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val cookieShape = MaterialShapes.Cookie9Sided.toShape()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = containerShape(RoundedCornerShape(32.dp)),
        border = containerBorder()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = containerShape(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "$totalCount",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (totalCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
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
                                    imageVector = Icons.Filled.BarChart,
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
                LineGraph(dataPoints = dataPoints, modifier = Modifier.fillMaxWidth().height(160.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        onPrevious()
                    },
                    shape = cookieShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
                    }
                }

                Surface(
                    onClick = {
                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        onDateClick?.invoke()
                    },
                    enabled = onDateClick != null,
                    shape = RoundedCornerShape(24.dp),
                    color = if (onDateClick != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = if (onDateClick != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    border = containerBorder(1.dp, if (onDateClick != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (onDateClick != null) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (onDateClick != null) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Surface(
                    onClick = {
                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        onNext()
                    },
                    enabled = canGoNext,
                    shape = cookieShape,
                    color = if (canGoNext) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (canGoNext) MaterialTheme.colorScheme.onSecondaryContainer
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
                    }
                }
            }
        }
    }
}

@Composable
fun LineGraph(dataPoints: List<Int>, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val surfaceColor = MaterialTheme.colorScheme.surface

    val progress = remember(dataPoints) { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Canvas(modifier = modifier.padding(horizontal = 8.dp, vertical = 16.dp)) {
        val maxPoint = dataPoints.maxOrNull()?.toFloat()?.takeIf { it > 0 } ?: 1f
        val yFactor = size.height / (maxPoint * 1.2f)
        val xFactor = if (dataPoints.size > 1) size.width / (dataPoints.size - 1) else size.width

        val gridLines = 4
        for (i in 0..gridLines) {
            val y = size.height - (i * (size.height / gridLines))
            drawLine(
                color = surfaceVariant,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (dataPoints.isEmpty()) return@Canvas

        val path = Path()
        var prevX = 0f
        var prevY = size.height - (dataPoints[0] * yFactor)
        path.moveTo(prevX, prevY)

        for (index in 1 until dataPoints.size) {
            val x = index * xFactor
            val y = size.height - (dataPoints[index] * yFactor)

            val controlX1 = (prevX + x) / 2f
            val controlY1 = prevY
            val controlX2 = (prevX + x) / 2f
            val controlY2 = y
            
            path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            
            prevX = x
            prevY = y
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo((dataPoints.size - 1) * xFactor, size.height)
            lineTo(0f, size.height)
            close()
        }

        clipRect(right = size.width * progress.value) {
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.3f),
                        primaryColor.copy(alpha = 0.0f)
                    )
                )
            )

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(
                    width = 4.dp.toPx(), 
                    cap = StrokeCap.Round, 
                    join = StrokeJoin.Round
                )
            )

            dataPoints.forEachIndexed { index, value ->
                val x = index * xFactor
                val y = size.height - (value * yFactor)

                drawCircle(
                    color = primaryColor.copy(alpha = 0.15f),
                    radius = 9.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = surfaceColor,
                    radius = 2.5.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
private fun GraphScreenPreview() {
    MaterialTheme {
        GraphScreenContent(entries = emptyList(), entryTriggers = emptyMap())
    }
}

@Preview(showBackground = true)
@Composable
private fun GraphSectionPreview() {
    MaterialTheme {
        GraphSection(
            title = "Daily Overview",
            totalCount = 12,
            dateLabel = "2023-10-25",
            dataPoints = listOf(1, 4, 2, 5, 0),
            onPrevious = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LineGraphPreview() {
    MaterialTheme {
        LineGraph(dataPoints = listOf(1, 4, 2, 5, 0), modifier = Modifier.fillMaxWidth().height(150.dp))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TriggersTab(triggerCounts: Map<String, Int>, totalCount: Int) {
    if (totalCount == 0) {
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
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.triggers_no_data_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.triggers_no_data_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val sortedTriggers = remember(triggerCounts) {
        triggerCounts.toList().sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val mostFrequent = sortedTriggers.firstOrNull()
            if (mostFrequent != null && mostFrequent.second > 0) {
                val triggerType = com.smokingtracker.data.TriggerType.fromKey(mostFrequent.first)
                val triggerName = triggerType?.let { stringResource(it.labelResId) } ?: mostFrequent.first
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    border = containerBorder(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ContainerIcon(
                            icon = Icons.Filled.BarChart,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            backdropColor = MaterialTheme.colorScheme.tertiary,
                            size = 44.dp
                        )
                        Column {
                            Text(
                                stringResource(R.string.main_trigger),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                triggerName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                stringResource(R.string.triggers_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = containerShape(RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = containerBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    val isStandardStyle = LocalContainerStyle.current == ContainerStyle.STANDARD
                    sortedTriggers.forEach { (triggerKey, count) ->
                        val triggerType = com.smokingtracker.data.TriggerType.fromKey(triggerKey)
                        val triggerName = triggerType?.let { stringResource(it.labelResId) } ?: triggerKey
                        val percent = if (totalCount > 0) count.toFloat() / totalCount.toFloat() else 0f

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
                                            .clip(if (isStandardStyle) CircleShape else cookieShape)
                                            .background(if (isStandardStyle) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
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
            .border(
                border = containerBorder(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)) ?: BorderStroke(0.dp, Color.Transparent),
                shape = CircleShape
            )
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

@Composable
private fun WeeklyComparisonCard(comparison: StatisticsManager.WeeklyComparisonData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = containerBorder()
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
                    shape = containerShape(RoundedCornerShape(12.dp)),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeFg,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.weekly_comparison_this_week, comparison.thisWeekCount),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.weekly_comparison_last_week, comparison.lastWeekCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun PeakSmokingHoursSection(distribution: StatisticsManager.HourlyDistributionData, vibrationEnabled: Boolean = false) {
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
        ),
        border = containerBorder()
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
                                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
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

