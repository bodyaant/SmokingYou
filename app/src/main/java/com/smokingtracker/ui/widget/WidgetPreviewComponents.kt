package com.smokingtracker.ui.widget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.smokingtracker.R
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.widget.CookieShapeDrawable

enum class WidgetOption {
    TIMER_3X1,
    QUICK_ADD_1X1
}

@Composable
fun rememberWidgetCookieBitmap(
    sizeDp: Int,
    petals: Int = 12
): ImageBitmap {
    val context = LocalContext.current
    val pillBgColorInt = ContextCompat.getColor(context, R.color.widget_pill_bg_color)
    return remember(pillBgColorInt, sizeDp, petals) {
        CookieShapeDrawable.createCookieBitmap(context, sizeDp, pillBgColorInt, petals = petals).asImageBitmap()
    }
}

@Composable
fun WidgetPreviewCard(
    selectedOption: WidgetOption,
    widgetShowResistButton: Boolean,
    dailyLimit: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = containerShape(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
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
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    )
                    Text(
                        text = stringResource(R.string.widget_preview_screen_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                            .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
fun TimerWidgetPreview(
    widgetShowResistButton: Boolean,
    dailyLimit: Int,
    modifier: Modifier = Modifier
) {
    val widgetBgColor = colorResource(R.color.widget_bg_color)
    val widgetPrimaryTextColor = colorResource(R.color.widget_primary_text_color)
    val widgetSecondaryTextColor = colorResource(R.color.widget_secondary_text_color)
    val widgetAccentColor = colorResource(R.color.widget_accent_color)
    val widgetShieldAccentColor = colorResource(R.color.widget_shield_accent_color)
    val cookieBitmap = rememberWidgetCookieBitmap(sizeDp = 48, petals = 12)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(widgetBgColor)
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
                    color = widgetSecondaryTextColor,
                    fontSize = 11.sp
                )
                Text(
                    text = "1${stringResource(R.string.widget_unit_h)} 25${stringResource(R.string.widget_unit_m)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = widgetPrimaryTextColor,
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
                    color = widgetAccentColor,
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
                            tint = widgetShieldAccentColor,
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
                            tint = widgetAccentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "+1",
                            color = widgetAccentColor,
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
fun QuickAddWidgetPreview(
    modifier: Modifier = Modifier
) {
    val widgetBgColor = colorResource(R.color.widget_bg_color)
    val widgetAccentColor = colorResource(R.color.widget_accent_color)
    val cookieBitmap = rememberWidgetCookieBitmap(sizeDp = 50, petals = 12)

    Row(
        modifier = modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MockLauncherIcon(iconTint = Color(0xFF5C6BC0), label = "Phone")

        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(12.dp, shape = RoundedCornerShape(22.dp), clip = false)
                .clip(RoundedCornerShape(22.dp))
                .background(widgetBgColor)
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
                        tint = widgetAccentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "+1",
                    color = widgetAccentColor,
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
fun MockLauncherIcon(
    iconTint: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.size(72.dp)
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
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun WidgetFeatureBadges(
    selectedOption: WidgetOption,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
}

@Composable
fun FeatureBadge(
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
