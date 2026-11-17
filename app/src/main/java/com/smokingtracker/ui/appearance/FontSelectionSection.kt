package com.smokingtracker.ui.appearance

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.data.FontPreset
import com.smokingtracker.ui.SwitchThumb
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerShape

@Composable
fun FontSelectionSection(
    currentPreset: FontPreset,
    onPresetChange: (FontPreset) -> Unit,
    useCustomVariableFont: Boolean,
    onUseCustomVariableFontChange: (Boolean) -> Unit,
    customFontWeight: Int,
    onCustomFontWeightChange: (Int) -> Unit,
    customFontWidth: Float,
    onCustomFontWidthChange: (Float) -> Unit,
    customFontRoundness: Float,
    onCustomFontRoundnessChange: (Float) -> Unit,
    vibrationEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var lastGsFlexPreset by remember {
        mutableStateOf(
            if (currentPreset != FontPreset.SYSTEM && currentPreset != FontPreset.OUTFIT) {
                if (currentPreset == FontPreset.WIDE) FontPreset.ZENITH else currentPreset
            } else {
                FontPreset.ZENITH
            }
        )
    }

    LaunchedEffect(currentPreset) {
        if (currentPreset != FontPreset.SYSTEM && currentPreset != FontPreset.OUTFIT) {
            lastGsFlexPreset = if (currentPreset == FontPreset.WIDE) FontPreset.ZENITH else currentPreset
        }
    }

    val isGsFlexSelected = currentPreset != FontPreset.SYSTEM

    val fontTypeOptions = listOf(
        Triple(true, stringResource(R.string.font_family_gs_flex), Icons.Filled.TextFields),
        Triple(false, stringResource(R.string.font_preset_system), Icons.Filled.Smartphone)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            fontTypeOptions.forEachIndexed { index, (isGsFlex, title, icon) ->
                val isSelected = isGsFlex == isGsFlexSelected

                val animatedWeight by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "font_type_weight_$index"
                )

                val startR by animateDpAsState(
                    targetValue = if (isSelected || index == 0) 24.dp else 8.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "font_type_startR_$index"
                )
                val endR by animateDpAsState(
                    targetValue = if (isSelected || index == fontTypeOptions.size - 1) 24.dp else 8.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "font_type_endR_$index"
                )

                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    label = "font_type_container_$index"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                    label = "font_type_content_$index"
                )

                Surface(
                    onClick = {
                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        if (isGsFlex) {
                            onPresetChange(lastGsFlexPreset)
                        } else {
                            onPresetChange(FontPreset.SYSTEM)
                        }
                    },
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

        AnimatedVisibility(
            visible = isGsFlexSelected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = containerShape(RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = stringResource(R.string.settings_custom_variable_font),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.settings_custom_variable_font_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        Switch(
                            checked = useCustomVariableFont,
                            onCheckedChange = {
                                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                onUseCustomVariableFontChange(it)
                            },
                            thumbContent = { SwitchThumb(useCustomVariableFont) }
                        )
                    }
                }

                AnimatedContent(
                    targetState = useCustomVariableFont,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                    label = "customFontContent"
                ) { isCustom ->
                    if (!isCustom) {
                        val presetOptions = listOf(
                            FontPreset.ZENITH to stringResource(R.string.font_preset_zenith),
                            FontPreset.NEO to stringResource(R.string.font_preset_neo),
                            FontPreset.COMPACT to stringResource(R.string.font_preset_impact),
                            FontPreset.AIRY to stringResource(R.string.font_preset_airy)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            presetOptions.forEachIndexed { index, (preset, title) ->
                                val isSelected = isGsFlexSelected && (currentPreset == preset || (currentPreset == FontPreset.WIDE && preset == FontPreset.ZENITH))

                                val animatedWeight by animateFloatAsState(
                                    targetValue = if (isSelected) 1.25f else 1.0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                                    label = "gsflex_preset_weight_$index"
                                )

                                val startR by animateDpAsState(
                                    targetValue = if (isSelected || index == 0) 24.dp else 8.dp,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                                    label = "gsflex_preset_startR_$index"
                                )
                                val endR by animateDpAsState(
                                    targetValue = if (isSelected || index == presetOptions.size - 1) 24.dp else 8.dp,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                                    label = "gsflex_preset_endR_$index"
                                )

                                val containerColor by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    label = "gsflex_preset_container_$index"
                                )
                                val contentColor by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                    label = "gsflex_preset_content_$index"
                                )

                                Surface(
                                    onClick = {
                                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                        lastGsFlexPreset = preset
                                        onPresetChange(preset)
                                    },
                                    modifier = Modifier
                                        .weight(animatedWeight)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(topStart = startR, bottomStart = startR, topEnd = endR, bottomEnd = endR),
                                    color = containerColor,
                                    contentColor = contentColor
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Card(
                                shape = containerShape(RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.font_preview_sample),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "12:45 • 20 pcs • \$5.50 • 98%",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.font_weight_label),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$customFontWeight",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = customFontWeight.toFloat().coerceIn(100f, 1000f),
                                    onValueChange = { onCustomFontWeightChange(it.toInt()) },
                                    valueRange = 100f..1000f,
                                    steps = 17
                                )
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.font_width_label),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${customFontWidth.toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = customFontWidth.coerceIn(25f, 150f),
                                    onValueChange = { onCustomFontWidthChange(it) },
                                    valueRange = 25f..150f,
                                    steps = 24
                                )
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.font_roundness_label),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${customFontRoundness.toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = customFontRoundness.coerceIn(0f, 100f),
                                    onValueChange = { onCustomFontRoundnessChange(it) },
                                    valueRange = 0f..100f,
                                    steps = 19
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
