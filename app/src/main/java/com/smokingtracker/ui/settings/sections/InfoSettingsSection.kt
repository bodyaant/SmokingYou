package com.smokingtracker.ui.settings.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.settings.components.SettingItem
import com.smokingtracker.ui.settings.components.SettingItemWithSwitch
import com.smokingtracker.ui.theme.ContainerGroupPosition

fun LazyListScope.infoSettingsSection(
    checkUpdatesOnStart: Boolean,
    onCheckUpdatesOnStartChange: (Boolean) -> Unit,
    onAboutClick: () -> Unit,
    onCheckForUpdates: () -> Unit
) {
    item(key = "section_info") {
        Text(
            text = stringResource(R.string.settings_info),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
        )
    }
    item(key = "about") {
        SettingItem(
            icon = Icons.Filled.Info,
            title = stringResource(R.string.about_app),
            subtitle = stringResource(R.string.about_app_desc),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
            groupPosition = ContainerGroupPosition.FIRST,
            onClick = onAboutClick
        )
    }
    item(key = "check_updates") {
        SettingItem(
            icon = Icons.Filled.SystemUpdate,
            title = stringResource(R.string.settings_check_updates),
            subtitle = stringResource(R.string.settings_check_updates_desc),
            shape = RoundedCornerShape(8.dp),
            groupPosition = ContainerGroupPosition.MIDDLE,
            onClick = onCheckForUpdates
        )
    }
    item(key = "check_updates_on_start") {
        SettingItemWithSwitch(
            icon = Icons.Filled.SystemUpdate,
            title = stringResource(R.string.settings_check_updates_on_start),
            subtitle = stringResource(R.string.settings_check_updates_on_start_desc),
            checked = checkUpdatesOnStart,
            onCheckedChange = onCheckUpdatesOnStartChange,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            groupPosition = ContainerGroupPosition.LAST
        )
    }
}
