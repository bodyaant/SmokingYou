package com.smokingtracker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: MainViewModel, onBack: () -> Unit, onNavigateToSettings: (() -> Unit)? = null) {
    val entries by viewModel.smokingEntries.collectAsState()
    val dailyLimit by viewModel.dailyLimit.collectAsState()
    val packPrice by viewModel.packPrice.collectAsState()
    val packSize by viewModel.packSize.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val stats = remember(entries) { StatisticsManager.calculateStats(entries) }

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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        }
    ) { paddingValues ->
        if (entries.isEmpty()) {
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
                dailyLimit = dailyLimit,
                packPrice = packPrice,
                packSize = packSize,
                currency = currency,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

@Composable
fun StatisticsList(
    modifier: Modifier = Modifier,
    stats: StatisticsData,
    entries: List<Long>,
    dailyLimit: Int,
    packPrice: Float,
    packSize: Int,
    currency: String,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault())
    val trackingSinceStr = stats.trackingSince?.let { dateFormat.format(Date(it)) }
        ?: stringResource(R.string.stats_no_data)
    val context = LocalContext.current

    val lastCigaretteTime = entries.maxOrNull() ?: 0L
    val timeElapsedMs = if (lastCigaretteTime > 0L) System.currentTimeMillis() - lastCigaretteTime else 0L
    val timeElapsedMinutes = (timeElapsedMs / (1000 * 60)).toFloat()

    val currentStreakDays = remember(entries) { StatisticsManager.currentSmokeFreeStreakDays(entries) }
    val streakCigarettesSaved = (currentStreakDays * dailyLimit).coerceAtLeast(0)
    val streakMoneySaved = if (packSize > 0) streakCigarettesSaved.toFloat() * (packPrice / packSize.toFloat()) else 0f
    val streakLifeMinutesSaved = streakCigarettesSaved * 11

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.stats_money_saved),
                        value = "${String.format(Locale.getDefault(), "%.2f", streakMoneySaved)} $currency",
                        icon = Icons.Filled.AttachMoney,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.stats_life_saved),
                        value = formatMinutes(streakLifeMinutesSaved, context),
                        icon = Icons.Filled.Favorite,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.stats_savings_setup_warning),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (onNavigateToSettings != null) {
                            Button(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
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
            StatCard(
                title = stringResource(R.string.stats_total_count),
                value = stats.totalCount.toString(),
                icon = Icons.Filled.BarChart,
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.stats_max_per_day),
                    value = stats.maxPerDay.toString(),
                    icon = Icons.Filled.Warning,
                    color = MaterialTheme.colorScheme.errorContainer
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.stats_min_per_day),
                    value = stats.minPerDay.toString(),
                    icon = Icons.Filled.BarChart,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
        item {
            StatCard(
                title = stringResource(R.string.stats_avg_per_day),
                value = stats.avgPerDay.toString(),
                icon = Icons.Filled.BarChart,
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
        item {
            StatCard(
                title = stringResource(R.string.stats_max_smoke_free_streak),
                value = pluralStringResource(R.plurals.stats_days_plural, stats.longestStreakDays, stats.longestStreakDays),
                icon = Icons.Filled.EmojiEvents,
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
        item {
            StatCard(
                title = stringResource(R.string.stats_tracking_since),
                value = trackingSinceStr,
                icon = Icons.Filled.Info,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        item {
            Text(
                text = stringResource(R.string.stats_body_recovery_who),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (lastCigaretteTime == 0L) {
                        Text(stringResource(R.string.stats_recovery_no_data), style = MaterialTheme.typography.bodyMedium)
                    } else {
                        HealthProgressBar(
                            label = stringResource(R.string.who_bp_desc),
                            timeLabel = stringResource(R.string.who_bp_time),
                            progress = (timeElapsedMinutes / 20f).coerceIn(0f, 1f)
                        )
                        HealthProgressBar(
                            label = stringResource(R.string.who_oxygen_desc),
                            timeLabel = stringResource(R.string.who_oxygen_time),
                            progress = (timeElapsedMinutes / 480f).coerceIn(0f, 1f)
                        )
                        HealthProgressBar(
                            label = stringResource(R.string.who_co_desc),
                            timeLabel = stringResource(R.string.who_co_time),
                            progress = (timeElapsedMinutes / 720f).coerceIn(0f, 1f)
                        )
                        HealthProgressBar(
                            label = stringResource(R.string.who_lung_desc),
                            timeLabel = stringResource(R.string.who_lung_time),
                            progress = (timeElapsedMinutes / 20160f).coerceIn(0f, 1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                contentColor = color,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
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
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
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
        if (days > 0) append(context.getString(R.string.minutes_format_day, days))
        if (hours > 0) append(context.getString(R.string.minutes_format_hour, hours))
        if (mins > 0 || isEmpty()) append(context.getString(R.string.minutes_format_min, mins))
    }.trim()
}
