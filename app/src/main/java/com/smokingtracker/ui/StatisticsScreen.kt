package com.smokingtracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smokingtracker.R
import com.smokingtracker.StatisticsData
import com.smokingtracker.StatisticsManager
import com.smokingtracker.StatisticsViewModel
import org.koin.androidx.compose.koinViewModel
import com.smokingtracker.ui.components.AverageIntervalBentoCard
import com.smokingtracker.ui.components.AverageIntervalBottomSheet
import com.smokingtracker.ui.components.ConsumptionDetailBottomSheet
import com.smokingtracker.ui.components.LifeReclaimedBentoCard
import com.smokingtracker.ui.components.LifeReclaimedBottomSheet
import com.smokingtracker.ui.components.MoneySavedBentoCard
import com.smokingtracker.ui.components.MoneySavedBottomSheet
import com.smokingtracker.ui.components.PackYearsBottomSheet
import com.smokingtracker.ui.components.ResistedBentoCard
import com.smokingtracker.ui.components.ResistedCravingsBottomSheet
import com.smokingtracker.ui.components.StreakBentoCard
import com.smokingtracker.ui.components.StreakDetailBottomSheet
import com.smokingtracker.ui.components.StatisticsOverviewCard
import com.smokingtracker.ui.components.WhoRecoveryBentoCard
import com.smokingtracker.ui.components.WhoRecoveryBottomSheet
import com.smokingtracker.ui.components.WhoRecoveryMilestone
import com.smokingtracker.ui.theme.ContainerIcon
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress
import com.smokingtracker.ui.theme.subContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = koinViewModel(), onBack: () -> Unit, onNavigateToSettings: (() -> Unit)? = null) {
    val entries by viewModel.smokingEntries.collectAsStateWithLifecycle()
    val resistedEntries by viewModel.resistedEntries.collectAsStateWithLifecycle()
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val packPrice by viewModel.packPrice.collectAsStateWithLifecycle()
    val packSize by viewModel.packSize.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val hasHistoricalBaseline by viewModel.hasHistoricalBaseline.collectAsStateWithLifecycle()
    val historicalStartDate by viewModel.historicalStartDate.collectAsStateWithLifecycle()
    val historicalDailyAvg by viewModel.historicalDailyAvg.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val savingsGoalTitle by viewModel.savingsGoalTitle.collectAsStateWithLifecycle()
    val savingsGoalAmount by viewModel.savingsGoalAmount.collectAsStateWithLifecycle()

    var showBaselineSheet by remember { mutableStateOf(false) }

    val stats = remember(entries) { StatisticsManager().calculateStats(entries) }

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
            onSaveBaseline = viewModel::saveBaseline,
            onClearBaseline = viewModel::clearHistoricalBaseline,
            onDismissRequest = { showBaselineSheet = false },
            vibrationEnabled = vibrationEnabled
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        }
    ) { paddingValues ->
        if (entries.isEmpty() && resistedEntries.isEmpty() && baselineAnalytics == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.stats_no_data), style = MaterialTheme.typography.titleMedium)
            }
        } else {
            StatisticsList(
                modifier = Modifier.padding(paddingValues),
                stats = stats,
                entries = entries,
                resistedCount = resistedEntries.size,
                dailyLimit = dailyLimit,
                packPrice = packPrice,
                packSize = packSize,
                currency = currency,
                baselineAnalytics = baselineAnalytics,
                hasHistoricalBaseline = hasHistoricalBaseline,
                vibrationEnabled = vibrationEnabled,
                savingsGoalTitle = savingsGoalTitle,
                savingsGoalAmount = savingsGoalAmount,
                onSaveSavingsGoal = viewModel::saveSavingsGoal,
                onOpenBaselineSheet = { showBaselineSheet = true },
                onNavigateToSettings = onNavigateToSettings
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
    dailyLimit: Int,
    packPrice: Float,
    packSize: Int,
    currency: String,
    baselineAnalytics: StatisticsManager.SmokingBaselineAnalytics? = null,
    hasHistoricalBaseline: Boolean = false,
    vibrationEnabled: Boolean = true,
    savingsGoalTitle: String = "",
    savingsGoalAmount: Float = 0f,
    onSaveSavingsGoal: (String, Float) -> Unit = { _, _ -> },
    onOpenBaselineSheet: () -> Unit = {},
    onNavigateToSettings: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(top = 16.dp, bottom = 120.dp)
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }
    val trackingSinceStr = remember(stats.trackingSince) {
        stats.trackingSince?.let { dateFormat.format(Date(it)) } ?: context.getString(R.string.stats_no_data)
    }

    val lastCigaretteTime = entries.maxOrNull() ?: 0L
    val timeElapsedMs = if (lastCigaretteTime > 0L) System.currentTimeMillis() - lastCigaretteTime else 0L
    val timeElapsedMinutes = (timeElapsedMs / (1000 * 60)).toFloat()

    val currentStreakDays = remember(entries) { StatisticsManager().currentSmokeFreeStreakDays(entries) }
    val streakCigarettesSaved = (currentStreakDays * dailyLimit).coerceAtLeast(0)
    val streakMoneySaved = if (packSize > 0) streakCigarettesSaved.toFloat() * (packPrice / packSize.toFloat()) else 0f
    val streakLifeMinutesSaved = streakCigarettesSaved * 11

    val resistedMoneySaved = if (packSize > 0 && packPrice > 0f) {
        resistedCount * (packPrice / packSize.toFloat())
    } else 0f
    val resistedLifeMinutes = resistedCount * 11

    val totalMoneySaved = streakMoneySaved + resistedMoneySaved
    val totalLifeMinutes = streakLifeMinutesSaved + resistedLifeMinutes
    val totalAvoidedCigarettes = streakCigarettesSaved + resistedCount

    val currencySymbol = remember(currency) {
        when (currency) {
            "RUB" -> "₽"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "TRY" -> "₺"
            "KZT" -> "₸"
            "UAH" -> "₴"
            else -> currency
        }
    }

    val whoMilestones = remember {
        listOf(
            WhoRecoveryMilestone(R.string.who_bp_desc, R.string.who_bp_time, 20f),
            WhoRecoveryMilestone(R.string.who_oxygen_desc, R.string.who_oxygen_time, 480f),
            WhoRecoveryMilestone(R.string.who_co_desc, R.string.who_co_time, 720f),
            WhoRecoveryMilestone(R.string.who_heart_attack_desc, R.string.who_heart_attack_time, 1440f),
            WhoRecoveryMilestone(R.string.who_taste_smell_desc, R.string.who_taste_smell_time, 2880f),
            WhoRecoveryMilestone(R.string.who_nicotine_desc, R.string.who_nicotine_time, 4320f),
            WhoRecoveryMilestone(R.string.who_lung_desc, R.string.who_lung_time, 20160f),
            WhoRecoveryMilestone(R.string.who_cough_desc, R.string.who_cough_time, 43200f),
            WhoRecoveryMilestone(R.string.who_circulation_desc, R.string.who_circulation_time, 129600f),
            WhoRecoveryMilestone(R.string.who_cilia_desc, R.string.who_cilia_time, 259200f),
            WhoRecoveryMilestone(R.string.who_bronchi_desc, R.string.who_bronchi_time, 388800f),
            WhoRecoveryMilestone(R.string.who_coronary_desc, R.string.who_coronary_time, 525600f)
        )
    }

    val completedMilestonesCount = remember(timeElapsedMinutes, lastCigaretteTime) {
        if (lastCigaretteTime == 0L) 0
        else whoMilestones.count { timeElapsedMinutes >= it.targetMinutes }
    }
    val nextMilestone = remember(whoMilestones, timeElapsedMinutes) {
        whoMilestones.firstOrNull { timeElapsedMinutes < it.targetMinutes }
    }

    val intervalData = remember(entries) { StatisticsManager().calculateSmokingIntervals(entries) }

    var showLifeSheet by remember { mutableStateOf(false) }
    var showMoneySheet by remember { mutableStateOf(false) }
    var showStreakSheet by remember { mutableStateOf(false) }
    var showWhoSheet by remember { mutableStateOf(false) }
    var showConsumptionSheet by remember { mutableStateOf(false) }
    var showResistedSheet by remember { mutableStateOf(false) }
    var showIntervalSheet by remember { mutableStateOf(false) }
    var showPackYearsSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StatisticsOverviewCard(
                totalCount = stats.totalCount,
                avgPerDay = stats.avgPerDay,
                trackingDays = stats.totalTrackingDays,
                trackingSince = trackingSinceStr,
                onClick = { showConsumptionSheet = true },
                vibrationEnabled = vibrationEnabled
            )
        }

        if (baselineAnalytics != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ContainerIcon(
                                    icon = Icons.Filled.AutoGraph,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    backdropColor = MaterialTheme.colorScheme.primaryContainer,
                                    size = 38.dp
                                )
                                Text(
                                    text = stringResource(R.string.baseline_stats_card_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            FilledTonalIconButton(
                                onClick = onOpenBaselineSheet,
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.baseline_edit),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.subContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.baseline_stats_was_label),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.history_cigs_count_format, baselineAnalytics.baselineDailyAvg.toString()),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.subContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.baseline_stats_now_label),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (baselineAnalytics.reductionPercentage > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.baseline_stats_pcs_day, baselineAnalytics.currentAvg7Days),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                                        color = if (baselineAnalytics.reductionPercentage > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (baselineAnalytics.reductionPercentage > 0) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (baselineAnalytics.reductionPercentage > 0) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.baseline_stats_reduction_badge, baselineAnalytics.reductionPercentage),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.baseline_stats_no_reduction_badge),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (baselineAnalytics.totalAvoidedCigarettes > 0) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.subContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    ContainerIcon(
                                        icon = Icons.Filled.Paid,
                                        tint = MaterialTheme.colorScheme.primary,
                                        backdropColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        size = 40.dp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.baseline_stats_avoided_title),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(
                                                R.string.baseline_stats_avoided_desc,
                                                baselineAnalytics.totalAvoidedCigarettes,
                                                String.format(Locale.getDefault(), "%,.0f %s", baselineAnalytics.totalAvoidedMoney, currencySymbol)
                                            ),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        val riskColor = when (baselineAnalytics.riskLevel) {
                            StatisticsManager.PackYearsRiskLevel.LOW -> MaterialTheme.colorScheme.primary
                            StatisticsManager.PackYearsRiskLevel.MODERATE -> MaterialTheme.colorScheme.tertiary
                            StatisticsManager.PackYearsRiskLevel.HIGH -> MaterialTheme.colorScheme.error
                        }
                        val riskText = when (baselineAnalytics.riskLevel) {
                            StatisticsManager.PackYearsRiskLevel.LOW -> stringResource(R.string.baseline_risk_low)
                            StatisticsManager.PackYearsRiskLevel.MODERATE -> stringResource(R.string.baseline_risk_moderate)
                            StatisticsManager.PackYearsRiskLevel.HIGH -> stringResource(R.string.baseline_risk_high)
                        }

                        val packYearsInteractionSource = remember { MutableInteractionSource() }
                        val packYearsBouncy = rememberBouncyPress(interactionSource = packYearsInteractionSource, targetScale = 0.97f)

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.subContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bouncyPress(packYearsBouncy)
                                .clickable(interactionSource = packYearsInteractionSource, indication = null) {
                                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                    showPackYearsSheet = true
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        ContainerIcon(
                                            icon = Icons.Filled.MedicalServices,
                                            tint = riskColor,
                                            backdropColor = riskColor.copy(alpha = 0.15f),
                                            size = 36.dp
                                        )
                                        Column {
                                            Text(
                                                text = stringResource(R.string.baseline_pack_years_title),
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${String.format(Locale.getDefault(), "%.1f", baselineAnalytics.packYears)} • $riskText",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = riskColor
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = stringResource(
                                        R.string.baseline_stats_past_summary,
                                        stringResource(R.string.baseline_years_format, baselineAnalytics.totalYearsInt),
                                        String.format(Locale.getDefault(), "%,d", baselineAnalytics.pastCigarettes),
                                        String.format(Locale.getDefault(), "%,.0f %s", baselineAnalytics.pastMoneySpent, currencySymbol)
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else if (!hasHistoricalBaseline) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ContainerIcon(
                                icon = Icons.Filled.AutoGraph,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                backdropColor = MaterialTheme.colorScheme.primaryContainer,
                                size = 40.dp
                            )
                            Text(
                                text = stringResource(R.string.baseline_banner_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = stringResource(R.string.baseline_banner_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onOpenBaselineSheet,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.baseline_banner_button),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        if (dailyLimit <= 0 || packPrice <= 0f) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = MaterialShapes.Flower.toShape(),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.stats_savings_setup_warning),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (onNavigateToSettings != null) {
                            Button(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.stats_go_to_settings),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StreakBentoCard(
                    currentStreakDays = currentStreakDays,
                    longestStreakDays = stats.longestStreakDays,
                    onClick = { showStreakSheet = true },
                    vibrationEnabled = vibrationEnabled,
                    modifier = Modifier.weight(1f)
                )
                WhoRecoveryBentoCard(
                    completedCount = completedMilestonesCount,
                    totalCount = whoMilestones.size,
                    nextMilestoneTimeStr = nextMilestone?.let { stringResource(it.timeResId) },
                    onClick = { showWhoSheet = true },
                    vibrationEnabled = vibrationEnabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoneySavedBentoCard(
                    moneySaved = totalMoneySaved,
                    currencySymbol = currencySymbol,
                    packPrice = packPrice,
                    savingsGoalTitle = savingsGoalTitle,
                    savingsGoalAmount = savingsGoalAmount,
                    onClick = { showMoneySheet = true },
                    vibrationEnabled = vibrationEnabled,
                    modifier = Modifier.weight(1f)
                )
                LifeReclaimedBentoCard(
                    lifeMinutes = totalLifeMinutes,
                    onClick = { showLifeSheet = true },
                    vibrationEnabled = vibrationEnabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AverageIntervalBentoCard(
                    intervalData = intervalData,
                    onClick = { showIntervalSheet = true },
                    vibrationEnabled = vibrationEnabled,
                    modifier = Modifier.weight(1f)
                )
                ResistedBentoCard(
                    resistedCount = resistedCount,
                    onClick = { showResistedSheet = true },
                    vibrationEnabled = vibrationEnabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.subContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.stats_tracking_since, trackingSinceStr),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showLifeSheet) {
        LifeReclaimedBottomSheet(
            lifeMinutes = totalLifeMinutes,
            avoidedCigarettes = totalAvoidedCigarettes,
            onDismissRequest = { showLifeSheet = false }
        )
    }

    if (showMoneySheet) {
        MoneySavedBottomSheet(
            moneySaved = totalMoneySaved,
            currencySymbol = currencySymbol,
            packPrice = packPrice,
            packSize = packSize,
            dailyAvg = stats.avgPerDay,
            savingsGoalTitle = savingsGoalTitle,
            savingsGoalAmount = savingsGoalAmount,
            onSaveSavingsGoal = onSaveSavingsGoal,
            onDismissRequest = { showMoneySheet = false }
        )
    }

    if (showStreakSheet) {
        StreakDetailBottomSheet(
            currentStreakDays = currentStreakDays,
            longestStreakDays = stats.longestStreakDays,
            onDismissRequest = { showStreakSheet = false }
        )
    }

    if (showWhoSheet) {
        WhoRecoveryBottomSheet(
            milestones = whoMilestones,
            timeElapsedMinutes = timeElapsedMinutes,
            onDismissRequest = { showWhoSheet = false }
        )
    }

    if (showConsumptionSheet) {
        ConsumptionDetailBottomSheet(
            avgPerDay = stats.avgPerDay,
            totalCount = stats.totalCount,
            minPerDay = stats.minPerDay,
            maxPerDay = stats.maxPerDay,
            totalTrackingDays = stats.totalTrackingDays,
            onDismissRequest = { showConsumptionSheet = false }
        )
    }

    if (showResistedSheet) {
        ResistedCravingsBottomSheet(
            resistedCount = resistedCount,
            avoidedMoney = resistedMoneySaved,
            currencySymbol = currencySymbol,
            onDismissRequest = { showResistedSheet = false }
        )
    }

    if (showIntervalSheet) {
        AverageIntervalBottomSheet(
            intervalData = intervalData,
            onDismissRequest = { showIntervalSheet = false }
        )
    }

    if (showPackYearsSheet && baselineAnalytics != null) {
        PackYearsBottomSheet(
            packYears = baselineAnalytics.packYears,
            riskLevel = baselineAnalytics.riskLevel,
            onDismissRequest = { showPackYearsSheet = false }
        )
    }
}
