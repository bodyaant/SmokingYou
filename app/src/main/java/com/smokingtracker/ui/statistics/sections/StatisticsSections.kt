package com.smokingtracker.ui.statistics.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokingtracker.R
import com.smokingtracker.StatisticsData
import com.smokingtracker.StatisticsManager
import com.smokingtracker.ui.components.statistics.WhoRecoveryMilestone
import com.smokingtracker.ui.components.statistics.cards.AverageIntervalBentoCard
import com.smokingtracker.ui.components.statistics.cards.LifeReclaimedBentoCard
import com.smokingtracker.ui.components.statistics.cards.MoneySavedBentoCard
import com.smokingtracker.ui.components.statistics.cards.ResistedBentoCard
import com.smokingtracker.ui.components.statistics.cards.StreakBentoCard
import com.smokingtracker.ui.components.statistics.cards.WhoRecoveryBentoCard
import com.smokingtracker.ui.components.statistics.sheets.AverageIntervalBottomSheet
import com.smokingtracker.ui.components.statistics.sheets.ConsumptionDetailBottomSheet
import com.smokingtracker.ui.components.statistics.sheets.LifeReclaimedBottomSheet
import com.smokingtracker.ui.components.statistics.sheets.MoneySavedBottomSheet
import com.smokingtracker.ui.components.statistics.sheets.PackYearsBottomSheet
import com.smokingtracker.ui.components.statistics.sheets.ResistedCravingsBottomSheet
import com.smokingtracker.ui.components.statistics.sheets.StreakDetailBottomSheet
import com.smokingtracker.ui.components.statistics.sheets.WhoRecoveryBottomSheet
import com.smokingtracker.ui.statistics.dialogs.SavingsGoalDialog
import com.smokingtracker.ui.theme.ContainerIcon
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.rememberBouncyPress
import com.smokingtracker.ui.theme.subContainer

@Composable
fun BaselineStatisticsCard(
    baselineAnalytics: StatisticsManager.SmokingBaselineAnalytics,
    currencySymbol: String,
    onOpenBaselineSheet: () -> Unit,
    onOpenPackYearsSheet: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = containerShape(RoundedCornerShape(24.dp)),
        modifier = modifier.fillMaxWidth()
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
                            val locale = LocalLocale.current.platformLocale
                            Text(
                                text = stringResource(
                                    R.string.baseline_stats_avoided_desc,
                                    baselineAnalytics.totalAvoidedCigarettes,
                                    String.format(locale, "%,.0f %s", baselineAnalytics.totalAvoidedMoney, currencySymbol)
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
            val context = LocalContext.current
            val haptic = LocalHapticFeedback.current

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.subContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyPress(packYearsBouncy)
                    .clickable(interactionSource = packYearsInteractionSource, indication = null) {
                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        onOpenPackYearsSheet()
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
                            val locale = LocalLocale.current.platformLocale
                            Column {
                                Text(
                                    text = stringResource(R.string.baseline_pack_years_title),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format(locale, "%.1f", baselineAnalytics.packYears)} • $riskText",
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

                    val locale = LocalLocale.current.platformLocale
                    Text(
                        text = stringResource(
                            R.string.baseline_stats_past_summary,
                            stringResource(R.string.baseline_years_format, baselineAnalytics.totalYearsInt),
                            String.format(locale, "%,d", baselineAnalytics.pastCigarettes),
                            String.format(locale, "%,.0f %s", baselineAnalytics.pastMoneySpent, currencySymbol)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BaselineBannerCard(
    onOpenBaselineSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = containerShape(RoundedCornerShape(24.dp)),
        modifier = modifier.fillMaxWidth()
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SavingsSetupWarningCard(
    onNavigateToSettings: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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

@Composable
fun HealthAndStreakSection(
    currentStreakDays: Int,
    longestStreakDays: Int,
    completedMilestonesCount: Int,
    totalMilestonesCount: Int,
    nextMilestoneTimeStr: String?,
    onStreakClick: () -> Unit,
    onWhoClick: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StreakBentoCard(
            currentStreakDays = currentStreakDays,
            longestStreakDays = longestStreakDays,
            onClick = onStreakClick,
            vibrationEnabled = vibrationEnabled,
            modifier = Modifier.weight(1f)
        )
        WhoRecoveryBentoCard(
            completedCount = completedMilestonesCount,
            totalCount = totalMilestonesCount,
            nextMilestoneTimeStr = nextMilestoneTimeStr,
            onClick = onWhoClick,
            vibrationEnabled = vibrationEnabled,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SavingsAndLifeSection(
    totalMoneySaved: Float,
    currencySymbol: String,
    packPrice: Float,
    savingsGoalTitle: String,
    savingsGoalAmount: Float,
    totalLifeMinutes: Int,
    onMoneyClick: () -> Unit,
    onLifeClick: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier,
    onGoalClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MoneySavedBentoCard(
            moneySaved = totalMoneySaved,
            currencySymbol = currencySymbol,
            packPrice = packPrice,
            savingsGoalTitle = savingsGoalTitle,
            savingsGoalAmount = savingsGoalAmount,
            onClick = onMoneyClick,
            vibrationEnabled = vibrationEnabled,
            modifier = Modifier.weight(1f),
            onGoalClick = onGoalClick
        )
        LifeReclaimedBentoCard(
            lifeMinutes = totalLifeMinutes,
            onClick = onLifeClick,
            vibrationEnabled = vibrationEnabled,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun IntervalsAndResistedSection(
    intervalData: StatisticsManager.SmokingIntervalData,
    resistedCount: Int,
    onIntervalClick: () -> Unit,
    onResistedClick: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AverageIntervalBentoCard(
            intervalData = intervalData,
            onClick = onIntervalClick,
            vibrationEnabled = vibrationEnabled,
            modifier = Modifier.weight(1f)
        )
        ResistedBentoCard(
            resistedCount = resistedCount,
            onClick = onResistedClick,
            vibrationEnabled = vibrationEnabled,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TrackingSinceFooter(
    trackingSinceStr: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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

val DEFAULT_WHO_MILESTONES = listOf(
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

fun getCurrencySymbol(currency: String): String = when (currency) {
    "RUB" -> "₽"
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    "TRY" -> "₺"
    "KZT" -> "₸"
    "UAH" -> "₴"
    else -> currency
}

class StatisticsSheetState {
    var showLifeSheet by mutableStateOf(false)
    var showMoneySheet by mutableStateOf(false)
    var showStreakSheet by mutableStateOf(false)
    var showWhoSheet by mutableStateOf(false)
    var showConsumptionSheet by mutableStateOf(false)
    var showResistedSheet by mutableStateOf(false)
    var showIntervalSheet by mutableStateOf(false)
    var showPackYearsSheet by mutableStateOf(false)
    var showSavingsGoalDialog by mutableStateOf(false)
}

@Composable
fun rememberStatisticsSheetState() = remember { StatisticsSheetState() }

@Composable
fun StatisticsDetailSheets(
    sheetState: StatisticsSheetState,
    savingsGoalTitle: String,
    savingsGoalAmount: Float,
    currencySymbol: String,
    computedSavings: StatisticsManager.SavingsData,
    onSaveSavingsGoal: (String, Float) -> Unit,
    stats: StatisticsData,
    dailyLimit: Int,
    packPrice: Float,
    packSize: Int,
    baselineAnalytics: StatisticsManager.SmokingBaselineAnalytics?,
    whoMilestones: List<WhoRecoveryMilestone>,
    timeElapsedMinutes: Float,
    resistedCount: Int,
    computedIntervals: StatisticsManager.SmokingIntervalData
) {
    if (sheetState.showSavingsGoalDialog) {
        SavingsGoalDialog(
            savingsGoalTitle = savingsGoalTitle,
            savingsGoalAmount = savingsGoalAmount,
            currencySymbol = currencySymbol,
            currentSaved = computedSavings.totalMoneySaved,
            onSaveGoal = onSaveSavingsGoal,
            onDismissRequest = { sheetState.showSavingsGoalDialog = false }
        )
    }

    if (sheetState.showLifeSheet) {
        LifeReclaimedBottomSheet(
            lifeMinutes = computedSavings.totalLifeMinutes,
            avoidedCigarettes = computedSavings.totalAvoidedCigarettes,
            onDismissRequest = { sheetState.showLifeSheet = false }
        )
    }

    if (sheetState.showMoneySheet) {
        MoneySavedBottomSheet(
            moneySaved = computedSavings.totalMoneySaved,
            currencySymbol = currencySymbol,
            packPrice = packPrice,
            packSize = packSize,
            dailyAvg = stats.avgPerDay,
            dailyLimit = dailyLimit,
            baselineDailyAvg = baselineAnalytics?.baselineDailyAvg ?: 0,
            trackingDays = stats.totalTrackingDays,
            savingsGoalTitle = savingsGoalTitle,
            savingsGoalAmount = savingsGoalAmount,
            onSaveSavingsGoal = onSaveSavingsGoal,
            onDismissRequest = { sheetState.showMoneySheet = false }
        )
    }

    if (sheetState.showStreakSheet) {
        StreakDetailBottomSheet(
            currentStreakDays = computedSavings.currentStreakDays,
            longestStreakDays = stats.longestStreakDays,
            onDismissRequest = { sheetState.showStreakSheet = false }
        )
    }

    if (sheetState.showWhoSheet) {
        WhoRecoveryBottomSheet(
            milestones = whoMilestones,
            timeElapsedMinutes = timeElapsedMinutes,
            onDismissRequest = { sheetState.showWhoSheet = false }
        )
    }

    if (sheetState.showConsumptionSheet) {
        ConsumptionDetailBottomSheet(
            avgPerDay = stats.avgPerDay,
            totalCount = stats.totalCount,
            minPerDay = stats.minPerDay,
            maxPerDay = stats.maxPerDay,
            totalTrackingDays = stats.totalTrackingDays,
            onDismissRequest = { sheetState.showConsumptionSheet = false }
        )
    }

    if (sheetState.showResistedSheet) {
        ResistedCravingsBottomSheet(
            resistedCount = resistedCount,
            avoidedMoney = computedSavings.resistedMoneySaved,
            currencySymbol = currencySymbol,
            onDismissRequest = { sheetState.showResistedSheet = false }
        )
    }

    if (sheetState.showIntervalSheet) {
        AverageIntervalBottomSheet(
            intervalData = computedIntervals,
            onDismissRequest = { sheetState.showIntervalSheet = false }
        )
    }

    if (sheetState.showPackYearsSheet && baselineAnalytics != null) {
        PackYearsBottomSheet(
            packYears = baselineAnalytics.packYears,
            riskLevel = baselineAnalytics.riskLevel,
            onDismissRequest = { sheetState.showPackYearsSheet = false }
        )
    }
}


