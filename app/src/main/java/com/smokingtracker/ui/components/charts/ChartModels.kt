package com.smokingtracker.ui.components.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.luminance
import com.smokingtracker.ui.theme.subContainer

internal val LimitDashPathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
internal val SelectionDashPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

enum class ChartViewStyle {
    BARS,
    LINE,
    CALENDAR
}

data class ChartColorPalette(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val cleanDay: Color,
    val cleanDayContainer: Color,
    val onCleanDayContainer: Color,
    val outlineVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceContainerHighest: Color,
    val tertiary: Color = cleanDay,
    val subContainer: Color = surfaceContainerHighest
)

@Composable
fun rememberChartColorPalette(): ChartColorPalette {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val error = MaterialTheme.colorScheme.error
    val errorContainer = MaterialTheme.colorScheme.errorContainer
    val onErrorContainer = MaterialTheme.colorScheme.onErrorContainer
    val tertiary = MaterialTheme.colorScheme.tertiary
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    val onTertiaryContainer = MaterialTheme.colorScheme.onTertiaryContainer
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val subContainer = MaterialTheme.colorScheme.subContainer
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    return remember(primary, error, tertiary, isDark) {
        val hsvPrimary = FloatArray(3)
        val hsvError = FloatArray(3)
        android.graphics.Color.RGBToHSV(
            (primary.red * 255).toInt(),
            (primary.green * 255).toInt(),
            (primary.blue * 255).toInt(),
            hsvPrimary
        )
        android.graphics.Color.RGBToHSV(
            (error.red * 255).toInt(),
            (error.green * 255).toInt(),
            (error.blue * 255).toInt(),
            hsvError
        )

        val rawDiff = kotlin.math.abs(hsvPrimary[0] - hsvError[0])
        val hueDistance = if (rawDiff > 180f) 360f - rawDiff else rawDiff
        val isRedConflict = hueDistance < 42f

        val effectiveError = if (isRedConflict) {
            if (isDark) Color(0xFFFF9E40) else Color(0xFFE65100)
        } else error

        val effectiveErrorContainer = if (isRedConflict) {
            if (isDark) Color(0xFF5D2800) else Color(0xFFFFCC80)
        } else errorContainer

        val effectiveOnErrorContainer = if (isRedConflict) {
            if (isDark) Color(0xFFFFDCC1) else Color(0xFF431B00)
        } else onErrorContainer

        ChartColorPalette(
            primary = primary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            error = effectiveError,
            errorContainer = effectiveErrorContainer,
            onErrorContainer = effectiveOnErrorContainer,
            cleanDay = tertiary,
            cleanDayContainer = tertiaryContainer,
            onCleanDayContainer = onTertiaryContainer,
            outlineVariant = outlineVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerHighest = surfaceContainerHighest,
            tertiary = tertiary,
            subContainer = subContainer
        )
    }
}
