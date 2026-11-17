package com.smokingtracker.ui.components.charts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.rememberBouncyPress
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.WeekFields

@Composable
fun MonthCalendarHeatmap(
    year: Int,
    month: Int,
    countsByDay: Map<Int, Int>,
    dailyLimit: Int,
    selectedDay: Int?,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
    todayDay: Int? = null,
    vibrationEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val locale = LocalLocale.current.platformLocale

    val palette = rememberChartColorPalette()
    val primaryColor = palette.primary
    val primaryContainer = palette.primaryContainer
    val tertiaryColor = palette.cleanDay
    val tertiaryContainer = palette.cleanDayContainer
    val errorColor = palette.error
    val errorContainer = palette.errorContainer
    val surfaceContainerHighest = palette.surfaceContainerHighest
    val onSurfaceVariant = palette.onSurfaceVariant

    val todayDate = remember { LocalDate.now() }
    val yearMonth = remember(year, month) {
        YearMonth.of(year, month.coerceIn(1, 12))
    }
    val isCurrentMonth = yearMonth.year == todayDate.year && yearMonth.monthValue == todayDate.monthValue
    val isFutureMonth = yearMonth.isAfter(YearMonth.from(todayDate))
    val currentDayOfMonth = todayDate.dayOfMonth

    val firstDayOfWeek = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstOfMonthDow = yearMonth.atDay(1).dayOfWeek
    val leadingBlanks = (firstOfMonthDow.value - firstDayOfWeek.value + 7) % 7

    val maxCount = remember(countsByDay, dailyLimit) {
        val observed = countsByDay.values.maxOrNull() ?: 1
        maxOf(observed, if (dailyLimit > 0) dailyLimit else 1).coerceAtLeast(1)
    }

    val dayHeaders = remember(locale, firstDayOfWeek) {
        (0..6).map { offset ->
            firstDayOfWeek.plus(offset.toLong()).getDisplayName(JavaTextStyle.NARROW, locale)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            dayHeaders.forEach { header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceVariant.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val totalSlots = leadingBlanks + daysInMonth
        val rows = (totalSlots + 6) / 7
        var currentDay = 1

        repeat(rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(7) { colIndex ->
                    val slotIndex = it * 7 + colIndex
                    if (slotIndex < leadingBlanks || currentDay > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val dayNumber = currentDay
                        val isFuture = when {
                            isFutureMonth -> true
                            isCurrentMonth -> dayNumber > currentDayOfMonth
                            todayDay != null -> dayNumber > todayDay
                            else -> false
                        }

                        val count = countsByDay[dayNumber] ?: 0
                        val hasData = countsByDay.containsKey(dayNumber)
                        val isSelected = !isFuture && selectedDay == dayNumber
                        val isToday = todayDay == dayNumber || (isCurrentMonth && dayNumber == currentDayOfMonth)
                        val isOverLimit = !isFuture && dailyLimit > 0 && count > dailyLimit
                        val isCleanDay = !isFuture && hasData && count == 0

                        val intensity = (count.toFloat() / maxCount).coerceIn(0f, 1f)

                        val cellColor = when {
                            isFuture -> surfaceContainerHighest.copy(alpha = 0.18f)
                            !hasData -> surfaceContainerHighest.copy(alpha = 0.35f)
                            isCleanDay -> tertiaryContainer.copy(alpha = 0.6f)
                            isOverLimit -> errorContainer.copy(alpha = 0.75f)
                            intensity <= 0.05f -> surfaceContainerHighest
                            else -> lerp(primaryContainer, primaryColor, intensity * 0.85f)
                        }

                        val textColor = when {
                            isFuture -> onSurfaceVariant.copy(alpha = 0.25f)
                            !hasData -> onSurfaceVariant.copy(alpha = 0.4f)
                            isCleanDay -> tertiaryColor
                            isOverLimit -> errorColor
                            intensity > 0.65f -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        CalendarDayCell(
                            dayNumber = dayNumber,
                            count = count,
                            hasData = hasData,
                            isFuture = isFuture,
                            isSelected = isSelected,
                            isToday = isToday,
                            targetCellColor = cellColor,
                            textColor = textColor,
                            primaryColor = primaryColor,
                            vibrationEnabled = vibrationEnabled,
                            onSelectDay = onSelectDay,
                            modifier = Modifier.weight(1f)
                        )
                        currentDay++
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    count: Int,
    hasData: Boolean,
    isFuture: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    targetCellColor: Color,
    textColor: Color,
    primaryColor: Color,
    vibrationEnabled: Boolean,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val bouncy = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.90f)

    val cornerPercent by animateIntAsState(
        targetValue = if (isSelected) 50 else 24,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "calendarCornerPercent"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "calendarCellScale"
    )

    val cellColor by animateColorAsState(
        targetValue = targetCellColor,
        animationSpec = tween(220),
        label = "calendarCellColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else if (isToday) 1.2.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "calendarBorderWidth"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else if (isToday) primaryColor.copy(alpha = 0.6f) else Color.Transparent,
        animationSpec = tween(200),
        label = "calendarBorderColor"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .zIndex(if (isSelected) 1f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(percent = cornerPercent))
            .background(cellColor)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, RoundedCornerShape(percent = cornerPercent))
                } else Modifier
            )
            .semantics(mergeDescendants = true) {
                contentDescription = if (hasData && !isFuture) "$dayNumber: $count" else "$dayNumber"
                selected = isSelected
            }
            .bouncyPress(bouncy)
            .clickable(
                enabled = !isFuture,
                interactionSource = interactionSource,
                indication = null
            ) {
                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                onSelectDay(dayNumber)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$dayNumber",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}
