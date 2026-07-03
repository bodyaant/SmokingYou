package com.smokingtracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.smokingtracker.R

@OptIn(ExperimentalTextApi::class)
data class FontAxes(
    val weight: Float,
    val width: Float,
    val opsz: Float,
    val grade: Float,
    val slant: Float,
    val roundness: Float
) {
    fun toVariationSettings() = FontVariation.Settings(
        FontVariation.weight(weight.toInt().coerceIn(1, 1000)),
        FontVariation.width(width.coerceIn(25f, 150f)),
        FontVariation.Setting("opsz", opsz.coerceIn(6f, 72f)),
        FontVariation.grade(grade.toInt().coerceIn(-200, 200)),
        FontVariation.slant(slant.coerceIn(-10f, 0f)),
        FontVariation.Setting("ROND", roundness.coerceIn(0f, 100f))
    )
}

enum class GSFlexPreset {
    WIDE, NEO, COMPACT, AIRY
}

object VariableFontFactory {
    
    fun createTypography(presetName: String): Typography {
        val preset = try {
            GSFlexPreset.valueOf(presetName)
        } catch (e: Exception) {
            GSFlexPreset.WIDE
        }
        val p = getPresetAxes(preset)
        return createExpressiveTypography(p.first, p.second, p.third)
    }

    fun getPresetFontAxes(preset: GSFlexPreset): Triple<FontAxes, FontAxes, FontAxes> {
        return when (preset) {
            GSFlexPreset.WIDE -> Triple(
                FontAxes(950f, 85f, 30f, 0f, 0f, 100f),
                FontAxes(700f, 115f, 32f, 0f, 0f, 60f),
                FontAxes(450f, 100f, 16f, 20f, 0f, 0f)
            )
            GSFlexPreset.NEO -> Triple(
                FontAxes(800f, 125f, 72f, 0f, 0f, 0f),
                FontAxes(600f, 100f, 32f, 0f, 0f, 0f),
                FontAxes(400f, 95f, 16f, 10f, 0f, 0f)
            )
            GSFlexPreset.COMPACT -> Triple(
                FontAxes(900f, 75f, 30f, 0f, 0f, 30f),
                FontAxes(800f, 85f, 32f, 50f, 0f, 20f),
                FontAxes(500f, 90f, 16f, 30f, 0f, 10f)
            )
            GSFlexPreset.AIRY -> Triple(
                FontAxes(300f, 130f, 72f, 0f, 0f, 100f),
                FontAxes(500f, 120f, 32f, 0f, 0f, 100f),
                FontAxes(400f, 110f, 16f, 0f, 0f, 50f)
            )
        }
    }

    @OptIn(ExperimentalTextApi::class)
    private fun getPresetAxes(preset: GSFlexPreset): Triple<FontVariation.Settings, FontVariation.Settings, FontVariation.Settings> {
        val axes = getPresetFontAxes(preset)
        return Triple(
            axes.first.toVariationSettings(),
            axes.second.toVariationSettings(),
            axes.third.toVariationSettings()
        )
    }

    @OptIn(ExperimentalTextApi::class)
    private fun createExpressiveTypography(
        displaySettings: FontVariation.Settings,
        headlineSettings: FontVariation.Settings,
        bodySettings: FontVariation.Settings
    ): Typography {
        val displayFont = FontFamily(Font(resId = R.font.google_sans_flex_variable, variationSettings = displaySettings))
        val headlineFont = FontFamily(Font(resId = R.font.google_sans_flex_variable, variationSettings = headlineSettings))
        val bodyFont = FontFamily(Font(resId = R.font.google_sans_flex_variable, variationSettings = bodySettings))

        return Typography(
            displayLarge = TextStyle(fontFamily = displayFont, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
            displayMedium = TextStyle(fontFamily = displayFont, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp),
            displaySmall = TextStyle(fontFamily = displayFont, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp),
            headlineLarge = TextStyle(fontFamily = headlineFont, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp),
            headlineMedium = TextStyle(fontFamily = headlineFont, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
            headlineSmall = TextStyle(fontFamily = headlineFont, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
            titleLarge = TextStyle(fontFamily = headlineFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
            titleMedium = TextStyle(fontFamily = headlineFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
            titleSmall = TextStyle(fontFamily = headlineFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
            bodyLarge = TextStyle(fontFamily = bodyFont, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
            bodyMedium = TextStyle(fontFamily = bodyFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
            bodySmall = TextStyle(fontFamily = bodyFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
            labelLarge = TextStyle(fontFamily = bodyFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
            labelMedium = TextStyle(fontFamily = bodyFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
            labelSmall = TextStyle(fontFamily = bodyFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
        )
    }
}
