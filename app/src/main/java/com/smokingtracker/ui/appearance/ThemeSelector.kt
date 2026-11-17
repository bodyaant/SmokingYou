package com.smokingtracker.ui.appearance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.data.ThemePreference

@Composable
fun ThemeSegmentedButton(
    currentTheme: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        Triple(ThemePreference.SYSTEM, stringResource(R.string.theme_system), Icons.Filled.BrightnessAuto),
        Triple(ThemePreference.LIGHT, stringResource(R.string.theme_light), Icons.Filled.LightMode),
        Triple(ThemePreference.DARK, stringResource(R.string.theme_dark), Icons.Filled.DarkMode)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, (theme, title, icon) ->
            val isSelected = currentTheme == theme

            val animatedWeight by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "theme_weight_$index"
            )

            val startR by animateDpAsState(
                targetValue = if (isSelected || index == 0) 24.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "theme_startR_$index"
            )
            val endR by animateDpAsState(
                targetValue = if (isSelected || index == options.size - 1) 24.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "theme_endR_$index"
            )

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                label = "theme_container_$index"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                label = "theme_content_$index"
            )

            Surface(
                onClick = { onThemeChange(theme) },
                modifier = Modifier
                    .weight(animatedWeight)
                    .height(48.dp),
                shape = RoundedCornerShape(topStart = startR, bottomStart = startR, topEnd = endR, bottomEnd = endR),
                color = containerColor,
                contentColor = contentColor
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
