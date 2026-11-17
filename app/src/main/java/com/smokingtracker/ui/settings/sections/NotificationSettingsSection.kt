package com.smokingtracker.ui.settings.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.settings.components.SettingItem
import com.smokingtracker.ui.theme.ContainerGroupPosition

fun LazyListScope.notificationSettingsSection(
    ongoingNotificationEnabled: Boolean,
    notificationLowPriority: Boolean,
    onNotificationClick: () -> Unit
) {
    item(key = "section_notifications") {
        Text(
            text = stringResource(R.string.settings_section_notifications),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
        )
    }
    item(key = "ongoing_notification") {
        val notifStatus = if (ongoingNotificationEnabled) {
            stringResource(
                R.string.notification_status_enabled_format,
                stringResource(if (notificationLowPriority) R.string.notification_priority_low else R.string.notification_priority_default)
            )
        } else {
            stringResource(R.string.notification_status_disabled)
        }
        SettingItem(
            icon = Icons.Filled.Notifications,
            title = stringResource(R.string.settings_ongoing_notification_title),
            subtitle = notifStatus,
            shape = RoundedCornerShape(24.dp),
            groupPosition = ContainerGroupPosition.SINGLE,
            onClick = onNotificationClick
        )
    }
}
