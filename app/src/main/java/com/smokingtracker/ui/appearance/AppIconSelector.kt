package com.smokingtracker.ui.appearance

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.data.AppIconPreset

data class AppIconOption(
    val preset: AppIconPreset,
    val nameRes: Int,
    val drawableRes: Int
)

val DEFAULT_APP_ICON_OPTIONS: List<AppIconOption> = listOf(
    AppIconOption(AppIconPreset.DEFAULT, R.string.app_icon_classic, R.drawable.ic_launcher_classic),
    AppIconOption(AppIconPreset.DARK, R.string.app_icon_dark, R.drawable.ic_launcher_dark),
    AppIconOption(AppIconPreset.SUNSET, R.string.app_icon_sunset, R.drawable.ic_launcher_sunset),
    AppIconOption(AppIconPreset.CREAM, R.string.app_icon_cream, R.drawable.ic_launcher_cream),
    AppIconOption(AppIconPreset.NEON, R.string.app_icon_neon, R.drawable.ic_launcher_neon),
    AppIconOption(AppIconPreset.GREEN, R.string.app_icon_organic, R.drawable.ic_launcher_green),
    AppIconOption(AppIconPreset.NIGHT, R.string.app_icon_midnight, R.drawable.ic_launcher_night),
    AppIconOption(AppIconPreset.MONOCHROME, R.string.app_icon_monochrome, R.drawable.ic_launcher_monochrome_variant)
)

@Composable
fun AppIconSelector(
    currentIcon: AppIconPreset,
    onIconChange: (AppIconPreset) -> Unit,
    modifier: Modifier = Modifier,
    options: List<AppIconOption> = DEFAULT_APP_ICON_OPTIONS
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val chunkedOptions = options.chunked(4)
        chunkedOptions.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowItems.forEach { option ->
                    val isSelected = currentIcon == option.preset

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                        label = "icon_scale_${option.preset.name}"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .scale(scale)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onIconChange(option.preset) }
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .border(
                                    border = BorderStroke(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    ),
                                    shape = CircleShape
                                )
                                .padding(if (isSelected) 4.dp else 0.dp)
                        ) {
                            Image(
                                painter = painterResource(id = option.drawableRes),
                                contentDescription = stringResource(id = option.nameRes),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(id = option.nameRes),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
                if (rowItems.size < 4) {
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
