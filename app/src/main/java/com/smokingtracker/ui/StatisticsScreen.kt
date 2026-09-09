package com.smokingtracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.smokingtracker.ui.theme.containerBorder
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.containerPadding
import com.smokingtracker.ui.theme.ContainerIcon
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.MainViewModel
import com.smokingtracker.R
import com.smokingtracker.StatisticsData
import com.smokingtracker.StatisticsManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: MainViewModel, onBack: () -> Unit, onNavigateToSettings: (() -> Unit)? = null) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                onOpenBaselineSheet = { showBaselineSheet = true },
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

private data class WhoMilestone(
    val labelResId: Int,
    val timeResId: Int,
    val targetMinutes: Float
)

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
    onOpenBaselineSheet: () -> Unit = {},
    onNavigateToSettings: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(top = 16.dp, bottom = 120.dp)
) {
    val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault())
    val trackingSinceStr = stats.trackingSince?.let { dateFormat.format(Date(it)) }
        ?: stringResource(R.string.stats_no_data)
    val context = LocalContext.current

    val lastCigaretteTime = entries.maxOrNull() ?: 0L
    val timeElapsedMs = if (lastCigaretteTime > 0L) System.currentTimeMillis() - lastCigaretteTime else 0L
    val timeElapsedMinutes = (timeElapsedMs / (1000 * 60)).toFloat()

    val currentStreakDays = remember(entries) { StatisticsManager().currentSmokeFreeStreakDays(entries) }
    val streakCigarettesSaved = (currentStreakDays * dailyLimit).coerceAtLeast(0)
    val streakMoneySaved = if (packSize > 0) streakCigarettesSaved.toFloat() * (packPrice / packSize.toFloat()) else 0f
    val streakLifeMinutesSaved = streakCigarettesSaved * 11

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
            WhoMilestone(R.string.who_bp_desc, R.string.who_bp_time, 20f),
            WhoMilestone(R.string.who_oxygen_desc, R.string.who_oxygen_time, 480f),
            WhoMilestone(R.string.who_co_desc, R.string.who_co_time, 720f),
            WhoMilestone(R.string.who_heart_attack_desc, R.string.who_heart_attack_time, 1440f),
            WhoMilestone(R.string.who_taste_smell_desc, R.string.who_taste_smell_time, 2880f),
            WhoMilestone(R.string.who_nicotine_desc, R.string.who_nicotine_time, 4320f),
            WhoMilestone(R.string.who_lung_desc, R.string.who_lung_time, 20160f),
            WhoMilestone(R.string.who_cough_desc, R.string.who_cough_time, 43200f),
            WhoMilestone(R.string.who_circulation_desc, R.string.who_circulation_time, 129600f),
            WhoMilestone(R.string.who_cilia_desc, R.string.who_cilia_time, 259200f),
            WhoMilestone(R.string.who_bronchi_desc, R.string.who_bronchi_time, 388800f),
            WhoMilestone(R.string.who_coronary_desc, R.string.who_coronary_time, 525600f)
        )
    }

    var isRecoveryExpanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isRecoveryExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "who_chevron_rotation"
    )

    val completedMilestonesCount = remember(timeElapsedMinutes, lastCigaretteTime) {
        if (lastCigaretteTime == 0L) 0
        else whoMilestones.count { timeElapsedMinutes >= it.targetMinutes }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (baselineAnalytics != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    modifier = Modifier.fillMaxWidth(),
                    border = containerBorder(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.baseline_stats_card_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onOpenBaselineSheet,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Baseline",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.baseline_stats_was_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${baselineAnalytics.baselineDailyAvg} " + stringResource(R.string.history_cigs_count_format, "").trim(),
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.baseline_stats_now_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.baseline_stats_pcs_day, baselineAnalytics.currentAvg7Days),
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = CircleShape,
                            color = if (baselineAnalytics.reductionPercentage > 0) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (baselineAnalytics.reductionPercentage > 0) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.baseline_stats_reduction_badge, baselineAnalytics.reductionPercentage),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = containerBorder(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.baseline_stats_avoided_title),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
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

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = riskColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.baseline_pack_years_title),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", baselineAnalytics.packYears)} • $riskText",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = riskColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(
                                R.string.baseline_stats_past_summary,
                                stringResource(R.string.baseline_years_format, baselineAnalytics.totalYearsInt),
                                String.format(Locale.getDefault(), "%,d", baselineAnalytics.pastCigarettes),
                                String.format(Locale.getDefault(), "%,.0f %s", baselineAnalytics.pastMoneySpent, currencySymbol)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (!hasHistoricalBaseline) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    modifier = Modifier.fillMaxWidth(),
                    border = containerBorder(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.baseline_banner_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.baseline_banner_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenBaselineSheet,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.baseline_banner_button),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                onClick = { isRecoveryExpanded = !isRecoveryExpanded },
                modifier = Modifier.fillMaxWidth(),
                shape = containerShape(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = containerBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(containerPadding(16.dp, 16.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ContainerIcon(
                            icon = Icons.Filled.Favorite,
                            tint = MaterialTheme.colorScheme.primary,
                            backdropColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            size = 40.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.stats_body_recovery_who),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(100),
                                    color = if (completedMilestonesCount == whoMilestones.size) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = if (completedMilestonesCount == whoMilestones.size) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Text(
                                        text = "$completedMilestonesCount/${whoMilestones.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                val percent = if (whoMilestones.isNotEmpty()) (completedMilestonesCount * 100) / whoMilestones.size else 0
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            onClick = { isRecoveryExpanded = !isRecoveryExpanded },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = if (isRecoveryExpanded) "Collapse" else "Expand",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer { rotationZ = chevronRotation }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isRecoveryExpanded,
                        enter = expandVertically(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                        ) + fadeIn(),
                        exit = shrinkVertically(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                        ) + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.padding(bottom = 2.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                            if (lastCigaretteTime == 0L) {
                                Text(
                                    text = stringResource(R.string.stats_recovery_no_data),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            } else {
                                whoMilestones.forEach { milestone ->
                                    HealthProgressBar(
                                        label = stringResource(milestone.labelResId),
                                        timeLabel = stringResource(milestone.timeResId),
                                        progress = (timeElapsedMinutes / milestone.targetMinutes).coerceIn(0f, 1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (resistedCount > 0) {
            item {
                ResistedCravingsCard(
                    resistedCount = resistedCount,
                    packPrice = packPrice,
                    packSize = packSize,
                    currencySymbol = currencySymbol,
                    context = context
                )
            }
        }

        if (dailyLimit > 0 && packPrice > 0f) {
            item {
                Text(
                    text = stringResource(R.string.stats_savings_current_streak),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 8.dp)
                )
            }
            item {
                StatCard(
                    title = stringResource(R.string.stats_current_streak_days),
                    value = pluralStringResource(R.plurals.stats_days_plural, currentStreakDays, currentStreakDays),
                    icon = Icons.Filled.EmojiEvents,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = stringResource(R.string.stats_money_saved),
                        value = "${String.format(Locale.getDefault(), "%.2f", streakMoneySaved)} $currencySymbol",
                        icon = Icons.Filled.AttachMoney,
                        color = MaterialTheme.colorScheme.secondary,
                        isCompact = true
                    )
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = stringResource(R.string.stats_life_saved),
                        value = formatMinutes(streakLifeMinutesSaved, context),
                        icon = Icons.Filled.Favorite,
                        color = MaterialTheme.colorScheme.tertiary,
                        isCompact = true
                    )
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = containerBorder(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
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
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    title = stringResource(R.string.stats_total_count),
                    value = stats.totalCount.toString(),
                    icon = Icons.Filled.BarChart,
                    color = MaterialTheme.colorScheme.primary,
                    isCompact = true
                )
                StatCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    title = stringResource(R.string.stats_avg_per_day),
                    value = formatAvgPerDay(stats.avgPerDay),
                    icon = Icons.Filled.BarChart,
                    color = MaterialTheme.colorScheme.tertiary,
                    isCompact = true
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    title = stringResource(R.string.stats_max_per_day),
                    value = stats.maxPerDay.toString(),
                    icon = Icons.Filled.Warning,
                    color = MaterialTheme.colorScheme.error,
                    isCompact = true
                )
                StatCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    title = stringResource(R.string.stats_min_per_day),
                    value = stats.minPerDay.toString(),
                    icon = Icons.Filled.BarChart,
                    color = MaterialTheme.colorScheme.secondary,
                    isCompact = true
                )
            }
        }
        item {
            StatCard(
                title = stringResource(R.string.stats_max_smoke_free_streak),
                value = pluralStringResource(R.plurals.stats_days_plural, stats.longestStreakDays, stats.longestStreakDays),
                icon = Icons.Filled.EmojiEvents,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            StatCard(
                title = stringResource(R.string.stats_tracking_since),
                value = trackingSinceStr,
                icon = Icons.Filled.Info,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    isCompact: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = containerBorder()
    ) {
        if (isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ContainerIcon(
                    icon = icon,
                    tint = color,
                    backdropColor = color.copy(alpha = 0.2f),
                    size = 36.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(containerPadding(16.dp, 16.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContainerIcon(
                    icon = icon,
                    tint = color,
                    backdropColor = color.copy(alpha = 0.2f),
                    size = 40.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ResistedCravingsCard(
    resistedCount: Int,
    packPrice: Float,
    packSize: Int,
    currencySymbol: String,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    val resistedMoneySaved = if (packSize > 0 && packPrice > 0f) {
        resistedCount * (packPrice / packSize.toFloat())
    } else 0f
    val resistedLifeMinutes = resistedCount * 11

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = containerBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(containerPadding(16.dp, 16.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContainerIcon(
                    icon = Icons.Default.Shield,
                    tint = MaterialTheme.colorScheme.primary,
                    backdropColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    size = 40.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.stats_resisted_cravings),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(100),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.stats_resisted_badge, resistedCount),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.stats_resisted_avoided_cigs),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$resistedCount",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        if (packPrice > 0f && packSize > 0) {
                            Text(
                                text = stringResource(R.string.stats_money_saved),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", resistedMoneySaved)} $currencySymbol",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.stats_life_saved),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatMinutes(resistedLifeMinutes, context),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (resistedLifeMinutes > 0) {
                            stringResource(R.string.stats_resisted_life_saved_note, formatMinutes(resistedLifeMinutes, context))
                        } else {
                            stringResource(R.string.stats_resisted_motivational_note)
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HealthProgressBar(label: String, timeLabel: String, progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedTriggerProgressBar(
                targetProgress = progress,
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp),
                color = if (progress >= 1f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                color = if (progress >= 1f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun formatMinutes(totalMinutes: Int, context: android.content.Context): String {
    if (totalMinutes == 0) return context.getString(R.string.minutes_format_zero)
    val days = totalMinutes / (24 * 60)
    val hours = (totalMinutes % (24 * 60)) / 60
    val mins = totalMinutes % 60
    return buildString {
        if (days > 0) {
            append(context.getString(R.string.minutes_format_day, days).trim())
            if (hours > 0) append(" ").append(context.getString(R.string.minutes_format_hour, hours).trim())
        } else if (hours > 0) {
            append(context.getString(R.string.minutes_format_hour, hours).trim())
            if (mins > 0) append(" ").append(context.getString(R.string.minutes_format_min, mins).trim())
        } else {
            append(context.getString(R.string.minutes_format_min, mins).trim())
        }
    }.trim()
}

fun formatAvgPerDay(avg: Float): String {
    val rounded = Math.round(avg * 10f) / 10f
    return if (rounded % 1.0f == 0f) {
        rounded.toInt().toString()
    } else {
        String.format(java.util.Locale.getDefault(), "%.1f", rounded)
    }
}
