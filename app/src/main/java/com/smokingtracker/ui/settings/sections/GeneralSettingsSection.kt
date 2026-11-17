package com.smokingtracker.ui.settings.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.data.TriggerType
import com.smokingtracker.ui.settings.components.SettingItem
import com.smokingtracker.ui.theme.ContainerGroupPosition
import java.util.concurrent.TimeUnit

fun LazyListScope.generalSettingsSection(
    dailyLimit: Int,
    taperingPlanEnabled: Boolean,
    taperingIntervalDays: Int,
    hasHistoricalBaseline: Boolean,
    historicalStartDate: Long,
    historicalDailyAvg: Int,
    customTriggers: List<String>,
    disabledDefaultTriggers: Set<String>,
    packPrice: Float,
    packSize: Int,
    currency: String,
    onLimitClick: () -> Unit,
    onBaselineClick: () -> Unit,
    onTriggersClick: () -> Unit,
    onPackParamsClick: () -> Unit
) {
    item(key = "section_goals") {
        Text(
            text = stringResource(R.string.settings_section_goals),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
        )
    }
    item(key = "daily_limit") {
        val limitSubtitle = if (dailyLimit > 0) {
            if (taperingPlanEnabled) {
                stringResource(R.string.daily_limit_subtitle_with_tapering, dailyLimit, taperingIntervalDays)
            } else {
                stringResource(R.string.daily_limit_active_subtitle, dailyLimit)
            }
        } else {
            stringResource(R.string.daily_limit_off_subtitle)
        }
        SettingItem(
            icon = Icons.Default.Speed,
            title = stringResource(R.string.settings_daily_limit),
            subtitle = limitSubtitle,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
            groupPosition = ContainerGroupPosition.FIRST,
            onClick = onLimitClick
        )
    }
    item(key = "baseline") {
        val yearsSmoked = if (historicalStartDate > 0L) {
            val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - historicalStartDate)
            (days / 365).toInt()
        } else 0
        val baselineSubtitle = if (hasHistoricalBaseline && historicalDailyAvg > 0) {
            val yearsStr = stringResource(R.string.baseline_years_format, yearsSmoked)
            stringResource(R.string.settings_baseline_subtitle_active, historicalDailyAvg, yearsStr)
        } else {
            stringResource(R.string.settings_baseline_subtitle_off)
        }
        SettingItem(
            icon = Icons.Default.History,
            title = stringResource(R.string.settings_baseline_title),
            subtitle = baselineSubtitle,
            shape = RoundedCornerShape(8.dp),
            groupPosition = ContainerGroupPosition.MIDDLE,
            onClick = onBaselineClick
        )
    }
    item(key = "triggers") {
        val activeTriggerCount = remember(customTriggers, disabledDefaultTriggers) {
            (TriggerType.allEntries().size - disabledDefaultTriggers.size) + customTriggers.size
        }
        SettingItem(
            icon = Icons.Filled.Psychology,
            title = stringResource(R.string.trigger_management_title),
            subtitle = stringResource(R.string.trigger_active_count, activeTriggerCount),
            shape = RoundedCornerShape(8.dp),
            groupPosition = ContainerGroupPosition.MIDDLE,
            onClick = onTriggersClick
        )
    }
    item(key = "pack_params") {
        val currentPlatformLocale = LocalLocale.current.platformLocale
        val packSubtitle = if (packPrice > 0f) {
            val formattedPrice = if (packPrice % 1f == 0f) packPrice.toInt().toString() else packPrice.toString()
            val pricePerCig = if (packSize > 0) packPrice / packSize else 0f
            val formattedPerCig = String.format(currentPlatformLocale, "%.2f", pricePerCig)
            val base = stringResource(R.string.settings_pack_subtitle_pattern, formattedPrice, currency, packSize)
            val rate = stringResource(R.string.pack_subtitle_piece_rate, formattedPerCig, currency)
            "$base • $rate"
        } else {
            stringResource(R.string.settings_tap_configure)
        }
        SettingItem(
            icon = Icons.Default.AccountBalanceWallet,
            title = stringResource(R.string.settings_pack_params),
            subtitle = packSubtitle,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            groupPosition = ContainerGroupPosition.LAST,
            onClick = onPackParamsClick
        )
    }
}
