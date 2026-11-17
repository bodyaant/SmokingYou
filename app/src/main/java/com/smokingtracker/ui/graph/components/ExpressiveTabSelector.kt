package com.smokingtracker.ui.graph.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ExpressiveTabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<String>,
    modifier: Modifier = Modifier
) {
    val animatedSelectedTab by animateFloatAsState(
        targetValue = selectedTab.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 400f
        ),
        label = "tabIndicatorOffset"
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    val indicatorBrush = remember(primaryColor) {
        Brush.horizontalGradient(
            colors = listOf(
                primaryColor,
                primaryColor.copy(alpha = 0.85f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(4.dp)
    ) {
        if (tabs.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f / tabs.size)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX = size.width * animatedSelectedTab
                    }
                    .clip(CircleShape)
                    .background(
                        brush = indicatorBrush,
                        shape = CircleShape
                    )
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTab
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "tabTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
