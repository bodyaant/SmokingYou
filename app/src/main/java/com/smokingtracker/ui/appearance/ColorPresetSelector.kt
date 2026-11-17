package com.smokingtracker.ui.appearance

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.data.ColorPreset
import com.smokingtracker.ui.theme.HapticFeedbackHelper

data class ColorPresetOption(
    val preset: ColorPreset,
    val nameRes: Int,
    val containerColor: Color,
    val contentColor: Color,
    val checkedContainerColor: Color,
    val checkedContentColor: Color
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun toggleButtonColors(
    containerColor: Color,
    contentColor: Color,
    checkedContainerColor: Color,
    checkedContentColor: Color
): ToggleButtonColors = ToggleButtonColors(
    containerColor = containerColor,
    contentColor = contentColor,
    disabledContainerColor = containerColor.copy(alpha = 0.38f),
    disabledContentColor = contentColor.copy(alpha = 0.38f),
    checkedContainerColor = checkedContainerColor,
    checkedContentColor = checkedContentColor
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorPresetSelector(
    currentPreset: ColorPreset,
    useDarkTheme: Boolean,
    onPresetChange: (ColorPreset) -> Unit,
    modifier: Modifier = Modifier,
    vibrationEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val colorPresets = remember(useDarkTheme) {
        listOf(
            ColorPresetOption(
                preset = ColorPreset.FOREST_SAGE,
                nameRes = R.string.color_preset_sage,
                containerColor = if (useDarkTheme) Color(0xFF354E16) else Color(0xFFCDEDA3),
                contentColor = if (useDarkTheme) Color(0xFFCDEDA3) else Color(0xFF101F00),
                checkedContainerColor = if (useDarkTheme) Color(0xFFB1D18A) else Color(0xFF4C662B),
                checkedContentColor = if (useDarkTheme) Color(0xFF1F3701) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.SUNSET_ROSE,
                nameRes = R.string.color_preset_rose,
                containerColor = if (useDarkTheme) Color(0xFF723523) else Color(0xFFFFDBD1),
                contentColor = if (useDarkTheme) Color(0xFFFFDBD1) else Color(0xFF3A0B01),
                checkedContainerColor = if (useDarkTheme) Color(0xFFF5B5A1) else Color(0xFF8F4C38),
                checkedContentColor = if (useDarkTheme) Color(0xFF561F0F) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.OCEAN_DEEP,
                nameRes = R.string.color_preset_ocean,
                containerColor = if (useDarkTheme) Color(0xFF004C69) else Color(0xFFC0E8FF),
                contentColor = if (useDarkTheme) Color(0xFFC0E8FF) else Color(0xFF001E2C),
                checkedContainerColor = if (useDarkTheme) Color(0xFF76D1FF) else Color(0xFF006689),
                checkedContentColor = if (useDarkTheme) Color(0xFF003549) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.PURPLE_NEBULA,
                nameRes = R.string.color_preset_purple,
                containerColor = if (useDarkTheme) Color(0xFF533588) else Color(0xFFEBDCFF),
                contentColor = if (useDarkTheme) Color(0xFFEBDCFF) else Color(0xFF250059),
                checkedContainerColor = if (useDarkTheme) Color(0xFFD4BBFF) else Color(0xFF6B4EA2),
                checkedContentColor = if (useDarkTheme) Color(0xFF3B1D71) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.AMBER_GOLD,
                nameRes = R.string.color_preset_amber,
                containerColor = if (useDarkTheme) Color(0xFF633F00) else Color(0xFFFFDDB3),
                contentColor = if (useDarkTheme) Color(0xFFFFDDB3) else Color(0xFF291800),
                checkedContainerColor = if (useDarkTheme) Color(0xFFFFB95B) else Color(0xFF825500),
                checkedContentColor = if (useDarkTheme) Color(0xFF452B00) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.CRIMSON_BERRY,
                nameRes = R.string.color_preset_crimson,
                containerColor = if (useDarkTheme) Color(0xFF7C002C) else Color(0xFFFFD9DD),
                contentColor = if (useDarkTheme) Color(0xFFFFD9DD) else Color(0xFF3A0010),
                checkedContainerColor = if (useDarkTheme) Color(0xFFFFB2BE) else Color(0xFF980038),
                checkedContentColor = if (useDarkTheme) Color(0xFF5F0021) else Color(0xFFFFFFFF)
            ),
            ColorPresetOption(
                preset = ColorPreset.SLATE_MONO,
                nameRes = R.string.color_preset_slate,
                containerColor = if (useDarkTheme) Color(0xFF383838) else Color(0xFFE2E2E2),
                contentColor = if (useDarkTheme) Color(0xFFE2E2E2) else Color(0xFF1A1A1A),
                checkedContainerColor = if (useDarkTheme) Color(0xFFC6C6C6) else Color(0xFF474747),
                checkedContentColor = if (useDarkTheme) Color(0xFF202020) else Color(0xFFFFFFFF)
            )
        )
    }

    val isSystemSelected = currentPreset == ColorPreset.SYSTEM
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                val systemEndR by animateDpAsState(
                    targetValue = if (isSystemSelected) 20.dp else 6.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "color_system_endR"
                )
                val systemShape = RoundedCornerShape(
                    topStart = 20.dp,
                    bottomStart = 20.dp,
                    topEnd = systemEndR,
                    bottomEnd = systemEndR
                )

                ToggleButton(
                    checked = isSystemSelected,
                    onCheckedChange = {
                        if (!isSystemSelected) {
                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            onPresetChange(ColorPreset.SYSTEM)
                        }
                    },
                    shapes = ToggleButtonShapes(systemShape, systemShape, systemShape),
                    colors = toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        checkedContainerColor = MaterialTheme.colorScheme.primary,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 40.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedContent(
                            targetState = isSystemSelected,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(140)) +
                                 scaleIn(initialScale = 0.75f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(70)) +
                                        scaleOut(targetScale = 0.75f, animationSpec = tween(70))
                                    )
                            },
                            label = "color_system_icon"
                        ) { checked ->
                            Icon(
                                imageVector = if (checked) Icons.Filled.Check else Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.color_preset_system),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                }
            }

            itemsIndexed(colorPresets, key = { _, item -> item.preset }) { index, item ->
                val isChecked = currentPreset == item.preset
                val isLast = index == colorPresets.size - 1

                val startR by animateDpAsState(
                    targetValue = if (isChecked) 20.dp else 6.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "color_startR_$index"
                )
                val endR by animateDpAsState(
                    targetValue = if (isChecked || isLast) 20.dp else 6.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "color_endR_$index"
                )
                val itemShape = RoundedCornerShape(
                    topStart = startR,
                    bottomStart = startR,
                    topEnd = endR,
                    bottomEnd = endR
                )

                ToggleButton(
                    checked = isChecked,
                    onCheckedChange = {
                        if (!isChecked) {
                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            onPresetChange(item.preset)
                        }
                    },
                    shapes = ToggleButtonShapes(itemShape, itemShape, itemShape),
                    colors = toggleButtonColors(
                        containerColor = item.containerColor,
                        contentColor = item.contentColor,
                        checkedContainerColor = item.checkedContainerColor,
                        checkedContentColor = item.checkedContentColor
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 40.dp)
                ) {
                    AnimatedContent(
                        targetState = isChecked,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(150, delayMillis = 50)) +
                             scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(70)) +
                                    scaleOut(targetScale = 0.85f, animationSpec = tween(70))
                                )
                                .using(
                                    SizeTransform(clip = false) { _, _ ->
                                        spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    }
                                )
                        },
                        label = "color_preset_content_${item.preset.name}"
                    ) { checked ->
                        if (checked) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(item.nameRes),
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
