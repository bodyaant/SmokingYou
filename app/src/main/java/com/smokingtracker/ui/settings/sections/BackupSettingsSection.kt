package com.smokingtracker.ui.settings.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.settings.components.SettingItem
import com.smokingtracker.ui.theme.ContainerGroupPosition

fun LazyListScope.backupSettingsSection(
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onResetDataClick: () -> Unit
) {
    item(key = "section_data") {
        Text(
            text = stringResource(R.string.settings_data),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
        )
    }
    item(key = "backup") {
        SettingItem(
            icon = Icons.Filled.Backup,
            title = stringResource(R.string.settings_backup),
            subtitle = stringResource(R.string.backup_desc),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
            groupPosition = ContainerGroupPosition.FIRST,
            onClick = onBackupClick
        )
    }
    item(key = "restore") {
        SettingItem(
            icon = Icons.Filled.Restore,
            title = stringResource(R.string.settings_restore),
            subtitle = stringResource(R.string.restore_desc),
            shape = RoundedCornerShape(8.dp),
            groupPosition = ContainerGroupPosition.MIDDLE,
            onClick = onRestoreClick
        )
    }
    item(key = "reset_data") {
        SettingItem(
            icon = Icons.Filled.RestartAlt,
            title = stringResource(R.string.settings_reset_data),
            subtitle = stringResource(R.string.settings_reset_data_desc),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            groupPosition = ContainerGroupPosition.LAST,
            onClick = onResetDataClick
        )
    }
}
