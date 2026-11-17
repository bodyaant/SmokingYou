package com.smokingtracker.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.ui.home.models.AppSnackbarVisuals
import com.smokingtracker.ui.theme.ContainerIcon
import com.smokingtracker.ui.theme.containerShape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .padding(bottom = 96.dp)
            .padding(horizontal = 16.dp)
    ) { snackbarData ->
        val customVisuals = snackbarData.visuals as? AppSnackbarVisuals
        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        val isDelete = customVisuals?.isError ?: (snackbarData.visuals.actionLabel != null)
        val icon = customVisuals?.icon ?: if (isDelete) Icons.Filled.Delete else Icons.Filled.Shield
        val (tint, backdropColor) = if (isDelete) {
            MaterialTheme.colorScheme.onErrorContainer to MaterialTheme.colorScheme.errorContainer
        } else {
            if (isDark) {
                Color(0xFFAFE1B3) to Color(0xFF224426)
            } else {
                Color(0xFF184D20) to Color(0xFFD6ECCF)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = containerShape(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ContainerIcon(
                        icon = icon,
                        tint = tint,
                        backdropColor = backdropColor,
                        size = 36.dp
                    )
                    Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                snackbarData.visuals.actionLabel?.let { actionLabel ->
                    TextButton(
                        onClick = { snackbarData.performAction() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    }
}
