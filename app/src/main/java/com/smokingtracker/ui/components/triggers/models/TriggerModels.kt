package com.smokingtracker.ui.components.triggers.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TriggerTrendType {
    INCREASED,
    DECREASED,
    SAME,
    NEW,
    NONE
}

data class TriggerTrendData(
    val type: TriggerTrendType,
    val delta: Int
)

data class QuadrantDominantTrigger(
    val quadrantIndex: Int,
    val triggerKey: String?,
    val count: Int,
    val totalInQuadrant: Int
)

data class TriggerQuadPart(
    val index: Int,
    val title: String,
    val hours: String,
    val count: Int,
    val icon: ImageVector,
    val color: Color
)
