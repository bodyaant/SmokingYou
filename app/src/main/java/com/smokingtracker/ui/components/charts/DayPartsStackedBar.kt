package com.smokingtracker.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import kotlin.math.roundToInt

data class QuadrantInfo(
    val index: Int,
    val title: String,
    val emoji: String,
    val count: Int,
    val color: Color
)

@Composable
fun DayPartsStackedBar(
    nightCount: Int,
    morningCount: Int,
    afternoonCount: Int,
    eveningCount: Int,
    modifier: Modifier = Modifier,
    selectedQuadrant: Int? = null,
    onSelectQuadrant: (Int?) -> Unit = {},
    vibrationEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val total = (nightCount + morningCount + afternoonCount + eveningCount).coerceAtLeast(1)

    val quadrants = listOf(
        QuadrantInfo(0, stringResource(R.string.day_part_night), "🌙", nightCount, MaterialTheme.colorScheme.tertiary),
        QuadrantInfo(1, stringResource(R.string.day_part_morning), "🌅", morningCount, MaterialTheme.colorScheme.primaryContainer),
        QuadrantInfo(2, stringResource(R.string.day_part_afternoon), "☀️", afternoonCount, MaterialTheme.colorScheme.primary),
        QuadrantInfo(3, stringResource(R.string.day_part_evening), "🌆", eveningCount, MaterialTheme.colorScheme.secondary)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            quadrants.forEach { q ->
                if (q.count > 0) {
                    val isSelected = selectedQuadrant == q.index
                    val weight = (q.count.toFloat() / total).coerceAtLeast(0.04f)

                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(q.color.copy(alpha = if (isSelected) 1f else 0.82f))
                            .clickable {
                                HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                                onSelectQuadrant(if (selectedQuadrant == q.index) null else q.index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (weight > 0.12f) {
                            Text(
                                text = "${((q.count.toFloat() / total) * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (q.color == MaterialTheme.colorScheme.primaryContainer) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quadrants.forEach { q ->
                val isSelected = selectedQuadrant == q.index
                val percent = ((q.count.toFloat() / total) * 100).roundToInt()

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            HapticFeedbackHelper.performTick(vibrationEnabled, haptic, context)
                            onSelectQuadrant(if (selectedQuadrant == q.index) null else q.index)
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = q.emoji,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${q.count}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
