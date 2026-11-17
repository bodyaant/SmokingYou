package com.smokingtracker.ui.settings.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.settings.components.SettingItem
import com.smokingtracker.ui.theme.ContainerGroupPosition

fun LazyListScope.interfaceSettingsSection(
    onAppearanceClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onWidgetsClick: () -> Unit
) {
    item(key = "section_interface") {
        Text(
            text = stringResource(R.string.settings_section_interface),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, start = 8.dp)
        )
    }
    item(key = "appearance") {
        SettingItem(
            icon = Icons.Filled.Brightness4,
            title = stringResource(R.string.settings_appearance),
            subtitle = stringResource(R.string.settings_appearance_desc),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
            groupPosition = ContainerGroupPosition.FIRST,
            onClick = onAppearanceClick
        )
    }
    item(key = "language") {
        val currentPlatformLocale = LocalLocale.current.platformLocale
        val langDisplay = currentPlatformLocale.getDisplayLanguage(currentPlatformLocale).replaceFirstChar { it.uppercase() }
        SettingItem(
            icon = Icons.Filled.Language,
            title = stringResource(R.string.settings_language),
            subtitle = langDisplay,
            shape = RoundedCornerShape(8.dp),
            groupPosition = ContainerGroupPosition.MIDDLE,
            onClick = onLanguageClick
        )
    }
    item(key = "widgets") {
        SettingItem(
            icon = Icons.Filled.Widgets,
            title = stringResource(R.string.settings_widgets_title),
            subtitle = stringResource(R.string.settings_widgets_desc),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            groupPosition = ContainerGroupPosition.LAST,
            onClick = onWidgetsClick
        )
    }
}
