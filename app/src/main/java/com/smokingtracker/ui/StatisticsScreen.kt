package com.smokingtracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smokingtracker.R
import com.smokingtracker.StatisticsData
import com.smokingtracker.StatisticsManager
import com.smokingtracker.StatisticsViewModel
import com.smokingtracker.ui.components.statistics.cards.StatisticsOverviewCard
import com.smokingtracker.ui.statistics.sections.BaselineBannerCard
import com.smokingtracker.ui.statistics.sections.BaselineStatisticsCard
import com.smokingtracker.ui.statistics.sections.DEFAULT_WHO_MILESTONES
import com.smokingtracker.ui.statistics.sections.HealthAndStreakSection
import com.smokingtracker.ui.statistics.sections.IntervalsAndResistedSection
import com.smokingtracker.ui.statistics.sections.SavingsAndLifeSection
import com.smokingtracker.ui.statistics.sections.SavingsSetupWarningCard
import com.smokingtracker.ui.statistics.sections.StatisticsDetailSheets
import com.smokingtracker.ui.statistics.sections.TrackingSinceFooter
import com.smokingtracker.ui.statistics.sections.getCurrencySymbol
import com.smokingtracker.ui.statistics.sections.rememberStatisticsSheetState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val uiState by viewModel.statisticsUiState.collectAsStateWithLifecycle()
    var showBaselineSheet by rememberSaveable { mutableStateOf(false) }

    if (showBaselineSheet) {
        BaselineBottomSheet(
            hasBaseline = uiState.hasHistoricalBaseline,
            historicalStartDate = uiState.historicalStartDate,
            historicalDailyAvg = uiState.historicalDailyAvg,
            packPrice = uiState.packPrice,
            packSize = uiState.packSize,
            currency = uiState.currency,
            onSaveBaseline = viewModel::saveBaseline,
            onClearBaseline = viewModel::clearHistoricalBaseline,
            onDismissRequest = { showBaselineSheet = false },
            vibrationEnabled = uiState.vibrationEnabled
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_statistics),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (uiState.entries.isEmpty() && uiState.resistedCount == 0 && !uiState.hasHistoricalBaseline && uiState.baselineAnalytics == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.stats_no_data), style = MaterialTheme.typography.titleMedium)
            }
        } else {
            StatisticsList(
                modifier = Modifier.padding(paddingValues),
                stats = uiState.stats,
                entries = uiState.entries,
                resistedCount = uiState.resistedCount,
                trackingStartMillis = uiState.firstAppLaunchDate,
                dailyLimit = uiState.dailyLimit,
                packPrice = uiState.packPrice,
                packSize = uiState.packSize,
                currency = uiState.currency,
                baselineAnalytics = uiState.baselineAnalytics,
                hasHistoricalBaseline = uiState.hasHistoricalBaseline,
                vibrationEnabled = uiState.vibrationEnabled,
                savingsGoalTitle = uiState.savingsGoalTitle,
                savingsGoalAmount = uiState.savingsGoalAmount,
                savingsData = uiState.savingsData,
                intervalData = uiState.smokingIntervals,
                onSaveSavingsGoal = viewModel::saveSavingsGoal,
                onOpenBaselineSheet = { showBaselineSheet = true },
                onNavigateToSettings = onNavigateToSettings,
                statisticsManager = viewModel.statisticsManager
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatisticsList(
    modifier: Modifier = Modifier,
    stats: StatisticsData,
    entries: List<Long>,
    resistedCount: Int = 0,
    trackingStartMillis: Long? = null,
    dailyLimit: Int,
    packPrice: Float,
    packSize: Int,
    currency: String,
    baselineAnalytics: StatisticsManager.SmokingBaselineAnalytics? = null,
    hasHistoricalBaseline: Boolean = false,
    vibrationEnabled: Boolean = true,
    savingsGoalTitle: String = "",
    savingsGoalAmount: Float = 0f,
    savingsData: StatisticsManager.SavingsData? = null,
    intervalData: StatisticsManager.SmokingIntervalData? = null,
    onSaveSavingsGoal: (String, Float) -> Unit = { _, _ -> },
    onOpenBaselineSheet: () -> Unit = {},
    onNavigateToSettings: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(top = 16.dp, bottom = 120.dp),
    statisticsManager: StatisticsManager = koinInject()
) {
    val locale = LocalLocale.current.platformLocale
    val noDataStr = stringResource(R.string.stats_no_data)

    val dateFormat = remember(locale) { SimpleDateFormat("d MMMM yyyy", locale) }
    val trackingSinceStr = remember(stats.trackingSince, dateFormat, noDataStr) {
        stats.trackingSince?.let { dateFormat.format(Date(it)) } ?: noDataStr
    }

    val lastCigaretteTime = remember(entries, trackingStartMillis) {
        entries.maxOrNull() ?: trackingStartMillis ?: 0L
    }
    val timeElapsedMs = if (lastCigaretteTime > 0L) (System.currentTimeMillis() - lastCigaretteTime).coerceAtLeast(0L) else 0L
    val timeElapsedMinutes = (timeElapsedMs / (1000 * 60)).toFloat()

    val computedSavings by produceState(
        initialValue = savingsData ?: StatisticsManager.SavingsData(resistedCount = resistedCount),
        entries,
        resistedCount,
        dailyLimit,
        packPrice,
        packSize,
        trackingStartMillis,
        savingsData,
        statisticsManager
    ) {
        value = savingsData ?: statisticsManager.calculateSavingsAsync(
            entries = entries,
            resistedCount = resistedCount,
            dailyLimit = dailyLimit,
            packPrice = packPrice,
            packSize = packSize,
            trackingStartMillis = trackingStartMillis
        )
    }

    val currencySymbol = remember(currency) { getCurrencySymbol(currency) }
    val whoMilestones = DEFAULT_WHO_MILESTONES

    val completedMilestonesCount = remember(timeElapsedMinutes, lastCigaretteTime) {
        if (lastCigaretteTime == 0L) 0
        else whoMilestones.count { timeElapsedMinutes >= it.targetMinutes }
    }
    val nextMilestone = remember(whoMilestones, timeElapsedMinutes) {
        whoMilestones.firstOrNull { timeElapsedMinutes < it.targetMinutes }
    }

    val computedIntervals by produceState(
        initialValue = intervalData ?: StatisticsManager.SmokingIntervalData.EMPTY,
        entries,
        intervalData,
        statisticsManager
    ) {
        value = intervalData ?: statisticsManager.calculateSmokingIntervalsAsync(entries)
    }

    val sheetState = rememberStatisticsSheetState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "stats_overview") {
            StatisticsOverviewCard(
                totalCount = stats.totalCount,
                avgPerDay = stats.avgPerDay,
                trackingDays = stats.totalTrackingDays,
                trackingSince = trackingSinceStr,
                onClick = { sheetState.showConsumptionSheet = true },
                vibrationEnabled = vibrationEnabled
            )
        }

        if (baselineAnalytics != null) {
            item(key = "stats_baseline_card") {
                BaselineStatisticsCard(
                    baselineAnalytics = baselineAnalytics,
                    currencySymbol = currencySymbol,
                    onOpenBaselineSheet = onOpenBaselineSheet,
                    onOpenPackYearsSheet = { sheetState.showPackYearsSheet = true },
                    vibrationEnabled = vibrationEnabled
                )
            }
        } else if (!hasHistoricalBaseline) {
            item(key = "stats_baseline_banner") {
                BaselineBannerCard(onOpenBaselineSheet = onOpenBaselineSheet)
            }
        }

        if (dailyLimit <= 0 || packPrice <= 0f) {
            item(key = "stats_savings_warning") {
                SavingsSetupWarningCard(onNavigateToSettings = onNavigateToSettings)
            }
        }

        item(key = "stats_bento_row_1") {
            HealthAndStreakSection(
                currentStreakDays = computedSavings.currentStreakDays,
                longestStreakDays = stats.longestStreakDays,
                completedMilestonesCount = completedMilestonesCount,
                totalMilestonesCount = whoMilestones.size,
                nextMilestoneTimeStr = nextMilestone?.let { stringResource(it.timeResId) },
                onStreakClick = { sheetState.showStreakSheet = true },
                onWhoClick = { sheetState.showWhoSheet = true },
                vibrationEnabled = vibrationEnabled
            )
        }

        item(key = "stats_bento_row_2") {
            SavingsAndLifeSection(
                totalMoneySaved = computedSavings.totalMoneySaved,
                currencySymbol = currencySymbol,
                packPrice = packPrice,
                savingsGoalTitle = savingsGoalTitle,
                savingsGoalAmount = savingsGoalAmount,
                totalLifeMinutes = computedSavings.totalLifeMinutes,
                onMoneyClick = { sheetState.showMoneySheet = true },
                onLifeClick = { sheetState.showLifeSheet = true },
                vibrationEnabled = vibrationEnabled,
                onGoalClick = { sheetState.showSavingsGoalDialog = true }
            )
        }

        item(key = "stats_bento_row_3") {
            IntervalsAndResistedSection(
                intervalData = computedIntervals,
                resistedCount = resistedCount,
                onIntervalClick = { sheetState.showIntervalSheet = true },
                onResistedClick = { sheetState.showResistedSheet = true },
                vibrationEnabled = vibrationEnabled
            )
        }

        item(key = "stats_tracking_since") {
            TrackingSinceFooter(trackingSinceStr = trackingSinceStr)
        }
    }

    StatisticsDetailSheets(
        sheetState = sheetState,
        savingsGoalTitle = savingsGoalTitle,
        savingsGoalAmount = savingsGoalAmount,
        currencySymbol = currencySymbol,
        computedSavings = computedSavings,
        onSaveSavingsGoal = onSaveSavingsGoal,
        stats = stats,
        dailyLimit = dailyLimit,
        packPrice = packPrice,
        packSize = packSize,
        baselineAnalytics = baselineAnalytics,
        whoMilestones = whoMilestones,
        timeElapsedMinutes = timeElapsedMinutes,
        resistedCount = resistedCount,
        computedIntervals = computedIntervals
    )
}
