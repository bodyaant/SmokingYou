package com.smokingtracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.tooling.preview.Preview


private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
)

private val SageLightColors = lightColorScheme(
    primary = Color(0xFF4C662B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCDEDA3),
    onPrimaryContainer = Color(0xFF111F00),
    secondary = Color(0xFF57624A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDBE7C8),
    onSecondaryContainer = Color(0xFF151E0C),
    tertiary = Color(0xFF386666),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCEBEB),
    onTertiaryContainer = Color(0xFF002020),
    background = Color(0xFFF9FAEF),
    onBackground = Color(0xFF1A1C16),
    surface = Color(0xFFF9FAEF),
    onSurface = Color(0xFF1A1C16),
    surfaceVariant = Color(0xFFE1E4D5),
    onSurfaceVariant = Color(0xFF44483D),
    outline = Color(0xFF75796C)
)

private val SageDarkColors = darkColorScheme(
    primary = Color(0xFFB1D18A),
    onPrimary = Color(0xFF1F3700),
    primaryContainer = Color(0xFF354E16),
    onPrimaryContainer = Color(0xFFCDEDA3),
    secondary = Color(0xFFBFCAB0),
    onSecondary = Color(0xFF2A331E),
    secondaryContainer = Color(0xFF404A33),
    onSecondaryContainer = Color(0xFFDBE7C8),
    tertiary = Color(0xFFA2CECD),
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF1E4E4E),
    onTertiaryContainer = Color(0xFFBCEBEB),
    background = Color(0xFF11140B),
    onBackground = Color(0xFFE2E3D8),
    surface = Color(0xFF11140B),
    onSurface = Color(0xFFE2E3D8),
    surfaceVariant = Color(0xFF44483D),
    onSurfaceVariant = Color(0xFFCAC8B9),
    outline = Color(0xFF8F9285)
)

private val RoseLightColors = lightColorScheme(
    primary = Color(0xFF8F4C38),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBD1),
    onPrimaryContainer = Color(0xFF3A0B01),
    secondary = Color(0xFF77574E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBD1),
    onSecondaryContainer = Color(0xFF2C150F),
    tertiary = Color(0xFF6C5D2F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF6E1A8),
    onTertiaryContainer = Color(0xFF221A00),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF201A18),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF201A18),
    surfaceVariant = Color(0xFFF5DED8),
    onSurfaceVariant = Color(0xFF53433F),
    outline = Color(0xFF85736E)
)

private val RoseDarkColors = darkColorScheme(
    primary = Color(0xFFF5B5A1),
    onPrimary = Color(0xFF561F0F),
    primaryContainer = Color(0xFF723523),
    onPrimaryContainer = Color(0xFFFFDBD1),
    secondary = Color(0xFFE7BDB2),
    onSecondary = Color(0xFF442A22),
    secondaryContainer = Color(0xFF5D4037),
    onSecondaryContainer = Color(0xFFFFDBD1),
    tertiary = Color(0xFFD9C58D),
    onTertiary = Color(0xFF3B2F05),
    tertiaryContainer = Color(0xFF53461A),
    onTertiaryContainer = Color(0xFFF6E1A8),
    background = Color(0xFF201A18),
    onBackground = Color(0xFFEDE0DC),
    surface = Color(0xFF201A18),
    onSurface = Color(0xFFEDE0DC),
    surfaceVariant = Color(0xFF53433F),
    onSurfaceVariant = Color(0xFFD8C2BC),
    outline = Color(0xFFA08C87)
)

private val OceanLightColors = lightColorScheme(
    primary = Color(0xFF006689),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC0E8FF),
    onPrimaryContainer = Color(0xFF001E2C),
    secondary = Color(0xFF4C616D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD0E5F3),
    onSecondaryContainer = Color(0xFF081E27),
    tertiary = Color(0xFF5D5B7D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE3DFFF),
    onTertiaryContainer = Color(0xFF191836),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFF8F9FA),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDCE3E9),
    onSurfaceVariant = Color(0xFF40484C),
    outline = Color(0xFF70787D)
)

private val OceanDarkColors = darkColorScheme(
    primary = Color(0xFF76D1FF),
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF004C69),
    onPrimaryContainer = Color(0xFFC0E8FF),
    secondary = Color(0xFFB3CAD8),
    onSecondary = Color(0xFF1E333E),
    secondaryContainer = Color(0xFF354955),
    onSecondaryContainer = Color(0xFFD0E5F3),
    tertiary = Color(0xFFC6C2EC),
    onTertiary = Color(0xFF2F2D4D),
    tertiaryContainer = Color(0xFF454364),
    onTertiaryContainer = Color(0xFFE3DFFF),
    background = Color(0xFF0F1113),
    onBackground = Color(0xFFE1E2E5),
    surface = Color(0xFF0F1113),
    onSurface = Color(0xFFE1E2E5),
    surfaceVariant = Color(0xFF40484C),
    onSurfaceVariant = Color(0xFFC0C7CD),
    outline = Color(0xFF8A9297)
)

@Preview
@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    fontPreset: String = "WIDE",
    amoledThemeEnabled: Boolean = false,
    colorPreset: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseColors = when (colorPreset) {
        "FOREST_SAGE" -> if (useDarkTheme) SageDarkColors else SageLightColors
        "SUNSET_ROSE" -> if (useDarkTheme) RoseDarkColors else RoseLightColors
        "OCEAN_DEEP" -> if (useDarkTheme) OceanDarkColors else OceanLightColors
        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (useDarkTheme) DarkColors else LightColors
            }
        }
    }

    val colors = if (useDarkTheme) {
        if (amoledThemeEnabled) {
            baseColors.copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceVariant = Color(0xFF121212),
                surfaceContainer = Color(0xFF1C1C1C),
                surfaceContainerLow = Color(0xFF0C0C0C),
                surfaceContainerHigh = Color(0xFF2C2C2C),
                surfaceContainerHighest = Color(0xFF3C3C3C),
                surfaceContainerLowest = Color.Black
            )
        } else {
            baseColors.copy(
                background = baseColors.surfaceContainerLow,
                surface = baseColors.surfaceContainerLow,
                surfaceVariant = baseColors.surfaceContainer,
                surfaceContainer = baseColors.surfaceContainerHigh,
                surfaceContainerLow = baseColors.surfaceContainerHigh,
                surfaceContainerHigh = baseColors.surfaceContainerHigh,
                surfaceContainerHighest = baseColors.surfaceContainerHigh,
                surfaceContainerLowest = baseColors.surfaceContainerHigh
            )
        }
    } else {
        baseColors.copy(
            background = baseColors.surfaceContainerLow,
            surface = baseColors.surfaceContainerLow,
            surfaceVariant = baseColors.surfaceContainer,
            surfaceContainer = Color.White,
            surfaceContainerLow = Color.White,
            surfaceContainerHigh = Color.White,
            surfaceContainerHighest = Color.White,
            surfaceContainerLowest = Color.White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    val typography = when (fontPreset) {
        "OUTFIT" -> AppTypography
        "SYSTEM" -> androidx.compose.material3.Typography()
        else -> VariableFontFactory.createTypography(fontPreset)
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}
