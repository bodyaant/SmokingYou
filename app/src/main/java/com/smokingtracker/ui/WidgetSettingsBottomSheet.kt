package com.smokingtracker.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.widget.CookieShapeDrawable
import com.smokingtracker.widget.QuickAddWidgetProvider
import com.smokingtracker.widget.TimerWidgetProvider

private enum class WidgetOption {
    TIMER_3X1,
    QUICK_ADD_1X1
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSettingsBottomSheet(
    widgetShowResistButton: Boolean = true,
    onWidgetShowResistButtonChange: (Boolean) -> Unit = {},
    dailyLimit: Int = 0,
    vibrationEnabled: Boolean = true,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedOption by remember { mutableStateOf(WidgetOption.TIMER_3X1) }

    fun requestPin(providerClass: Class<*>) {
        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            val myProvider = ComponentName(context, providerClass)
            appWidgetManager.requestPinAppWidget(myProvider, null, null)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.widget_pin_unsupported_toast),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = if (MaterialTheme.colorScheme.surfaceContainerLow == Color.White) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Widgets,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_widgets_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_widgets_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val widgetOptions = listOf(
                Triple(WidgetOption.TIMER_3X1, stringResource(R.string.widget_tab_timer), Icons.Default.Timer),
                Triple(WidgetOption.QUICK_ADD_1X1, stringResource(R.string.widget_tab_quick_add), Icons.Default.TouchApp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                widgetOptions.forEachIndexed { index, (option, title, icon) ->
                    val isSelected = selectedOption == option

                    val animatedWeight by animateFloatAsState(
                        targetValue = if (isSelected) 1.25f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "widget_tab_weight_$index"
                    )

                    val startR by animateDpAsState(
                        targetValue = if (isSelected || index == 0) 24.dp else 8.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "widget_tab_startR_$index"
                    )
                    val endR by animateDpAsState(
                        targetValue = if (isSelected || index == widgetOptions.size - 1) 24.dp else 8.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "widget_tab_endR_$index"
                    )

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        },
                        animationSpec = tween(220),
                        label = "widget_tab_container_$index"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        animationSpec = tween(220),
                        label = "widget_tab_content_$index"
                    )

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "widget_tab_icon_scale_$index"
                    )

                    Surface(
                        onClick = {
                            if (!isSelected) {
                                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                selectedOption = option
                            }
                        },
                        modifier = Modifier
                            .weight(animatedWeight)
                            .height(48.dp),
                        shape = RoundedCornerShape(
                            topStart = startR,
                            bottomStart = startR,
                            topEnd = endR,
                            bottomEnd = endR
                        ),
                        color = containerColor,
                        contentColor = contentColor
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .scale(iconScale)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
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
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1F2633),
                                Color(0xFF141923),
                                Color(0xFF0F121A)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                            )
                            Text(
                                text = stringResource(R.string.widget_preview_screen_title),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            contentColor = Color.White.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = if (selectedOption == WidgetOption.TIMER_3X1) "3 × 1" else "1 × 1",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = selectedOption,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.95f, animationSpec = tween(220)) togetherWith
                                    fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.95f, animationSpec = tween(180))
                        },
                        label = "widget_preview_content"
                    ) { option ->
                        when (option) {
                            WidgetOption.TIMER_3X1 -> {
                                TimerWidgetPreview(
                                    widgetShowResistButton = widgetShowResistButton,
                                    dailyLimit = dailyLimit
                                )
                            }
                            WidgetOption.QUICK_ADD_1X1 -> {
                                QuickAddWidgetPreview()
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.padding(top = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            val active = (selectedOption == WidgetOption.TIMER_3X1 && index == 0) ||
                                    (selectedOption == WidgetOption.QUICK_ADD_1X1 && index == 1)
                            Box(
                                modifier = Modifier
                                    .height(4.dp)
                                    .width(if (active) 14.dp else 4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.25f))
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedOption == WidgetOption.TIMER_3X1) {
                    FeatureBadge(text = stringResource(R.string.widget_feature_bg_update), modifier = Modifier.weight(1f))
                    FeatureBadge(text = stringResource(R.string.action_resisted_craving), modifier = Modifier.weight(1f))
                    FeatureBadge(text = stringResource(R.string.widget_feature_one_tap), modifier = Modifier.weight(1f))
                } else {
                    FeatureBadge(text = stringResource(R.string.widget_feature_one_tap), modifier = Modifier.weight(1f))
                    FeatureBadge(text = stringResource(R.string.widget_feature_instant), modifier = Modifier.weight(1.3f))
                    FeatureBadge(text = "1×1 Launcher", modifier = Modifier.weight(1f))
                }
            }

            if (selectedOption == WidgetOption.TIMER_3X1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                onWidgetShowResistButtonChange(!widgetShowResistButton)
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (widgetShowResistButton) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (widgetShowResistButton) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Security,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.widget_show_resist_button_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.widget_show_resist_button_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Switch(
                            checked = widgetShowResistButton,
                            onCheckedChange = {
                                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                onWidgetShowResistButtonChange(it)
                            }
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = containerShape(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.widget_quick_add_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.widget_quick_add_info_text),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    when (selectedOption) {
                        WidgetOption.TIMER_3X1 -> requestPin(TimerWidgetProvider::class.java)
                        WidgetOption.QUICK_ADD_1X1 -> requestPin(QuickAddWidgetProvider::class.java)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.widget_add_to_home_screen),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = stringResource(R.string.widget_pin_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun FeatureBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun TimerWidgetPreview(
    widgetShowResistButton: Boolean,
    dailyLimit: Int
) {
    val context = LocalContext.current
    val pillBgColorInt = "#E1E3EC".toColorInt()
    val cookieBitmap = remember {
        CookieShapeDrawable.createCookieBitmap(context, 48, pillBgColorInt, petals = 12).asImageBitmap()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF2F3F8))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.widget_last_smoke),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF616474),
                    fontSize = 11.sp
                )
                Text(
                    text = "1ч 25м",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF191A23),
                    fontSize = 19.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
                val countText = if (dailyLimit > 0) {
                    stringResource(R.string.widget_today_limit_format, 4, dailyLimit)
                } else {
                    stringResource(R.string.widget_today_count, 4)
                }
                Text(
                    text = countText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE65100),
                    fontSize = 11.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnimatedVisibility(
                    visible = widgetShowResistButton,
                    enter = fadeIn(tween(180)) + expandHorizontally(
                        animationSpec = spring(stiffness = 500f),
                        expandFrom = Alignment.End
                    ),
                    exit = fadeOut(tween(140)) + shrinkHorizontally(
                        animationSpec = spring(stiffness = 500f),
                        shrinkTowards = Alignment.End
                    )
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = cookieBitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shield),
                            contentDescription = stringResource(R.string.action_resisted_craving),
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = cookieBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cigarettebase),
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "+1",
                            color = Color(0xFFE65100),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAddWidgetPreview() {
    val context = LocalContext.current
    val pillBgColorInt = "#E1E3EC".toColorInt()
    val cookieBitmap = remember {
        CookieShapeDrawable.createCookieBitmap(context, 50, pillBgColorInt, petals = 12).asImageBitmap()
    }

    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MockLauncherIcon(iconTint = Color(0xFF5C6BC0), label = "Phone")

        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(12.dp, shape = RoundedCornerShape(22.dp), clip = false)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFF2F3F8))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = cookieBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cigarettebase),
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "+1",
                    color = Color(0xFFE65100),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }

        MockLauncherIcon(iconTint = Color(0xFF26A69A), label = "Chat")
    }
}

@Composable
private fun MockLauncherIcon(
    iconTint: Color,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.size(72.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = iconTint.copy(alpha = 0.25f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.7f))
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}
