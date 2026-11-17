package com.smokingtracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokingtracker.R
import com.smokingtracker.domain.statistics.TaperingPlan
import com.smokingtracker.ui.theme.containerShape
import com.smokingtracker.ui.theme.subContainer

@Composable
fun TaperingChartPreview(
    plan: TaperingPlan,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceContainerColor = MaterialTheme.colorScheme.subContainer

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = containerShape(RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = surfaceContainerColor)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.tapering_plan_forecast_title),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(
                            R.string.tapering_plan_forecast_desc,
                            plan.totalDaysToZero,
                            plan.formattedTargetDate
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = primaryColor
                    )
                }
            }
        }

        if (plan.points.isNotEmpty() && plan.initialLimit > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = containerShape(RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    val points = plan.points
                    val maxLimit = plan.initialLimit.toFloat().coerceAtLeast(1f)
                    val totalDays = plan.totalDaysToZero.toFloat().coerceAtLeast(1f)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val verticalPadding = 6.dp.toPx()
                        val chartHeight = (height - verticalPadding * 2).coerceAtLeast(1f)

                        val stepPath = Path()
                        val fillPath = Path()

                        val startX = 0f
                        val startY = verticalPadding + (1f - (points.first().limit.toFloat() / maxLimit)) * chartHeight
                        stepPath.moveTo(startX, startY)
                        fillPath.moveTo(startX, height)
                        fillPath.lineTo(startX, startY)

                        var prevX = startX
                        var prevY = startY

                        for (i in 1 until points.size) {
                            val pt = points[i]
                            val currX = (pt.dayOffset.toFloat() / totalDays).coerceIn(0f, 1f) * width
                            val currY = verticalPadding + (1f - (pt.limit.toFloat() / maxLimit)).coerceIn(0f, 1f) * chartHeight

                            stepPath.lineTo(currX, prevY)
                            fillPath.lineTo(currX, prevY)

                            stepPath.lineTo(currX, currY)
                            fillPath.lineTo(currX, currY)

                            prevX = currX
                            prevY = currY
                        }

                        fillPath.lineTo(prevX, height)
                        fillPath.close()

                        val gradientBrush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.28f),
                                primaryColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = height
                        )

                        drawPath(path = fillPath, brush = gradientBrush)
                        drawPath(
                            path = stepPath,
                            color = primaryColor,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = Offset(startX, startY)
                        )

                        drawCircle(
                            color = primaryColor,
                            radius = 5.dp.toPx(),
                            center = Offset(prevX, prevY)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${plan.initialLimit} ${stringResource(R.string.daily_limit_cigs_per_day)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "0 • ${plan.formattedTargetDate}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryColor
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.tapering_plan_step_info, plan.intervalDays),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
