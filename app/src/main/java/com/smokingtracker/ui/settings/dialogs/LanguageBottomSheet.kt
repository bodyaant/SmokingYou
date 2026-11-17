package com.smokingtracker.ui.settings.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.ContainerGroupPosition
import com.smokingtracker.ui.theme.containerGroupGap
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.util.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    onDismissRequest: () -> Unit,
    onRecordLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val currentPlatformLocale = LocalLocale.current.platformLocale
    val currentLocale = currentPlatformLocale.language

    val languages = listOf(
        "en" to "English",
        "ru" to "Русский",
        "es" to "Español",
        "de" to "Deutsch",
        "fr" to "Français",
        "tr" to "Türkçe",
        "it" to "Italiano",
        "pt" to "Português",
        "uk" to "Українська",
        "zh" to "中文"
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.language_dialog_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            languages.forEachIndexed { index, (langCode, langName) ->
                val groupPosition = when (index) {
                    0 -> ContainerGroupPosition.FIRST
                    languages.lastIndex -> ContainerGroupPosition.LAST
                    else -> ContainerGroupPosition.MIDDLE
                }
                val shape = containerShape(
                    when (index) {
                        0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                        languages.lastIndex -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                        else -> RoundedCornerShape(8.dp)
                    },
                    groupPosition
                )
                val isSelected = currentLocale == langCode

                Surface(
                    onClick = {
                        LocaleHelper.changeLanguage(context, langCode)
                        onRecordLanguageChange(langCode)
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = langName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (index < languages.lastIndex) {
                    Spacer(modifier = Modifier.height(containerGroupGap()))
                }
            }
        }
    }
}
