package com.smokingtracker.ui.components.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.subContainer

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun DayDetailCard(
    dateFormatted: String,
    count: Int,
    dailyLimit: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    cost: Float = 0f,
    currency: String = "",
    avgIntervalMinutes: Int? = null,
    topTrigger: String? = null,
    title: String = stringResource(R.string.chart_selected_day)
) {
    val isCleanDay = count == 0
    val isLimitApplicable = dailyLimit > 0
    val isOverLimit = isLimitApplicable && count > dailyLimit
    val withinLimit = isLimitApplicable && count <= dailyLimit

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(22.dp)),
        color = MaterialTheme.colorScheme.subContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.btn_close),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val palette = rememberChartColorPalette()
            val (badgeBg, badgeFg, badgeText, badgeIcon) = when {
                isCleanDay -> Quadruple(
                    palette.cleanDayContainer,
                    palette.onCleanDayContainer,
                    stringResource(R.string.chart_clean_day),
                    Icons.Rounded.Star
                )
                isOverLimit -> Quadruple(
                    palette.errorContainer,
                    palette.onErrorContainer,
                    stringResource(R.string.graph_over_limit_badge, count - dailyLimit),
                    Icons.Default.LocalFireDepartment
                )
                withinLimit -> Quadruple(
                    palette.primaryContainer,
                    palette.onPrimaryContainer,
                    stringResource(R.string.graph_within_limit_badge),
                    Icons.Default.Shield
                )
                else -> Quadruple(
                    palette.surfaceContainerHighest,
                    MaterialTheme.colorScheme.onSurface,
                    stringResource(R.string.history_cigs_count_format, count.toString()),
                    Icons.Default.AccessTime
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
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
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = badgeFg
                    )
                }
            }

            val currencySymbol = remember(currency) {
                when (currency) {
                    "RUB" -> "₽"
                    "USD" -> "$"
                    "EUR" -> "€"
                    "GBP" -> "£"
                    "TRY" -> "₺"
                    "KZT" -> "₸"
                    "UAH" -> "₴"
                    "BYN" -> "Br"
                    else -> currency
                }
            }

            val locale = LocalLocale.current.platformLocale
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = stringResource(R.string.day_detail_cost),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = if (cost > 0f) String.format(locale, "%.1f %s", cost, currencySymbol).trim() else "0 $currencySymbol".trim(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(28.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.day_detail_interval),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val intervalText = if (avgIntervalMinutes != null && avgIntervalMinutes > 0) {
                        val h = avgIntervalMinutes / 60
                        val m = avgIntervalMinutes % 60
                        if (h > 0) {
                            stringResource(R.string.format_hours_minutes, h, m)
                        } else {
                            stringResource(R.string.format_minutes_only, m)
                        }
                    } else "–"
                    Text(
                        text = intervalText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(28.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = stringResource(R.string.day_detail_triggers),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = topTrigger ?: stringResource(R.string.day_detail_no_triggers),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
