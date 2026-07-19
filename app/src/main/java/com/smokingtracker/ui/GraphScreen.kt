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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.MainViewModel
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(viewModel: MainViewModel, onNavigateToStatistics: () -> Unit) {
    val entries by viewModel.smokingEntries.collectAsState()
    val entryTriggers by viewModel.entryTriggers.collectAsState()
    GraphScreenContent(
        entries = entries,
        entryTriggers = entryTriggers,
        onNavigateToStatistics = onNavigateToStatistics
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreenContent(
    entries: List<Long>,
    entryTriggers: Map<Long, String>,
    onNavigateToStatistics: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    var dailyDate by remember { mutableStateOf(Calendar.getInstance()) }
    var weeklyDate by remember { mutableStateOf(Calendar.getInstance()) }
    var monthlyDate by remember { mutableStateOf(Calendar.getInstance()) }
    var yearlyDate by remember { mutableStateOf(Calendar.getInstance()) }

    val dailyData = remember(entries, dailyDate) { StatisticsManager.generateDailyData(entries, dailyDate) }
    val weeklyData = remember(entries, weeklyDate) { StatisticsManager.generateWeeklyData(entries, weeklyDate) }
    val monthlyData = remember(entries, monthlyDate) { StatisticsManager.generateMonthlyData(entries, monthlyDate) }
    val yearlyData = remember(entries, yearlyDate) { StatisticsManager.generateYearlyData(entries, yearlyDate) }

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
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = Calendar.getInstance().apply { timeInMillis = millis }
                            when (target) {
                                "daily" -> dailyDate = newDate
                                "weekly" -> weeklyDate = newDate
                                "monthly" -> monthlyDate = newDate
                                "yearly" -> yearlyDate = newDate
                            }
                        }
                        activeDatePickerTarget = null
                    }
                ) {
                    Text(stringResource(R.string.dialog_ok), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDatePickerTarget = null }) {
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
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    tabs = listOf(
                        stringResource(R.string.tab_graphs),
                        stringResource(R.string.tab_triggers)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                        onClick = onNavigateToStatistics,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.BarChart, contentDescription = null, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_statistics),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.statistics_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
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
                        onDateClick = { activeDatePickerTarget = "daily" }
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
                        onDateClick = { activeDatePickerTarget = "weekly" }
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
                        onDateClick = { activeDatePickerTarget = "monthly" }
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
                        onDateClick = { activeDatePickerTarget = "yearly" }
                    )
                }
            }
        } else {
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
            
            Box(modifier = Modifier.padding(paddingValues)) {
                TriggersTab(triggerCounts = triggerCounts, totalCount = totalTriggersLogged)
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
    onDateClick: (() -> Unit)? = null
) {
    val cookieShape = MaterialShapes.Cookie12Sided.toShape()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
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
                    shape = CircleShape,
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
                    Text(
                        text = stringResource(R.string.graph_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
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
                    onClick = onPrevious,
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
                    onClick = { onDateClick?.invoke() },
                    enabled = onDateClick != null,
                    shape = RoundedCornerShape(24.dp),
                    color = if (onDateClick != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = if (onDateClick != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, if (onDateClick != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
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
                    }
                }

                Surface(
                    onClick = onNext,
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

@Composable
fun TriggersTab(triggerCounts: Map<String, Int>, totalCount: Int) {
    if (totalCount == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.triggers_no_data),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp)
            )
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
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.BarChart, contentDescription = null, modifier = Modifier.size(24.dp))
                            }
                        }
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
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
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
                                Text(
                                    text = triggerName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = stringResource(R.string.trigger_count_pattern, count, (percent * 100).toInt()),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            LinearProgressIndicator(
                                progress = { percent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape),
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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
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
