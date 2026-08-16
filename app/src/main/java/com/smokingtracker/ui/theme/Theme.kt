package com.smokingtracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.tooling.preview.Preview
import com.smokingtracker.data.ColorPreset
import com.smokingtracker.data.ContainerStyle
import com.smokingtracker.data.FontPreset


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
    surfaceDim = md_theme_light_surfaceDim,
    surfaceBright = md_theme_light_surfaceBright,
    surfaceContainerLowest = md_theme_light_surfaceContainerLowest,
    surfaceContainerLow = md_theme_light_surfaceContainerLow,
    surfaceContainer = md_theme_light_surfaceContainer,
    surfaceContainerHigh = md_theme_light_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_light_surfaceContainerHighest,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
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
    surfaceDim = md_theme_dark_surfaceDim,
    surfaceBright = md_theme_dark_surfaceBright,
    surfaceContainerLowest = md_theme_dark_surfaceContainerLowest,
    surfaceContainerLow = md_theme_dark_surfaceContainerLow,
    surfaceContainer = md_theme_dark_surfaceContainer,
    surfaceContainerHigh = md_theme_dark_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_dark_surfaceContainerHighest,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
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
    background = Color(0xFFF0F4EC),
    onBackground = Color(0xFF1A1C16),
    surface = Color(0xFFF0F4EC),
    onSurface = Color(0xFF1A1C16),
    surfaceDim = Color(0xFFD9DBD0),
    surfaceBright = Color(0xFFF9FAEF),
    surfaceContainerLowest = Color(0xFFF0F4EC),
    surfaceContainerLow = Color(0xFFF0F4EC),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFE1E8DC),
    surfaceVariant = Color(0xFFE1E4D5),
    onSurfaceVariant = Color(0xFF44483D),
    outline = Color(0xFF75796C),
    outlineVariant = Color(0xFFC5C8BA)
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
    surfaceDim = Color(0xFF11140B),
    surfaceBright = Color(0xFF373A30),
    surfaceContainerLowest = Color(0xFF0C0F06),
    surfaceContainerLow = Color(0xFF1A1C14),
    surfaceContainer = Color(0xFF1E2117),
    surfaceContainerHigh = Color(0xFF282B21),
    surfaceContainerHighest = Color(0xFF33362B),
    surfaceVariant = Color(0xFF44483D),
    onSurfaceVariant = Color(0xFFCAC8B9),
    outline = Color(0xFF8F9285),
    outlineVariant = Color(0xFF44483D)
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
    background = Color(0xFFF9EFF0),
    onBackground = Color(0xFF231917),
    surface = Color(0xFFF9EFF0),
    onSurface = Color(0xFF231917),
    surfaceDim = Color(0xFFE8D6D2),
    surfaceBright = Color(0xFFFFF8F6),
    surfaceContainerLowest = Color(0xFFF9EFF0),
    surfaceContainerLow = Color(0xFFF9EFF0),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFF2DEDF),
    surfaceVariant = Color(0xFFF5DED8),
    onSurfaceVariant = Color(0xFF53433F),
    outline = Color(0xFF85736E),
    outlineVariant = Color(0xFFD8C2BC)
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
    background = Color(0xFF1A110F),
    onBackground = Color(0xFFF1DFDA),
    surface = Color(0xFF1A110F),
    onSurface = Color(0xFFF1DFDA),
    surfaceDim = Color(0xFF1A110F),
    surfaceBright = Color(0xFF423735),
    surfaceContainerLowest = Color(0xFF140C0A),
    surfaceContainerLow = Color(0xFF231917),
    surfaceContainer = Color(0xFF271D1B),
    surfaceContainerHigh = Color(0xFF322825),
    surfaceContainerHighest = Color(0xFF3D3230),
    surfaceVariant = Color(0xFF53433F),
    onSurfaceVariant = Color(0xFFD8C2BC),
    outline = Color(0xFFA08C87),
    outlineVariant = Color(0xFF53433F)
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
    background = Color(0xFFEFF4F9),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFEFF4F9),
    onSurface = Color(0xFF191C1E),
    surfaceDim = Color(0xFFD8DADC),
    surfaceBright = Color(0xFFF8F9FA),
    surfaceContainerLowest = Color(0xFFEFF4F9),
    surfaceContainerLow = Color(0xFFEFF4F9),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFDCE5EF),
    surfaceVariant = Color(0xFFDCE3E9),
    onSurfaceVariant = Color(0xFF40484C),
    outline = Color(0xFF70787D),
    outlineVariant = Color(0xFFC0C8CD)
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
    background = Color(0xFF0F1417),
    onBackground = Color(0xFFDEE3E6),
    surface = Color(0xFF0F1417),
    onSurface = Color(0xFFDEE3E6),
    surfaceDim = Color(0xFF0F1417),
    surfaceBright = Color(0xFF353A3D),
    surfaceContainerLowest = Color(0xFF0A0F12),
    surfaceContainerLow = Color(0xFF171D20),
    surfaceContainer = Color(0xFF1B2124),
    surfaceContainerHigh = Color(0xFF262B2E),
    surfaceContainerHighest = Color(0xFF303639),
    surfaceVariant = Color(0xFF40484C),
    onSurfaceVariant = Color(0xFFC0C7CD),
    outline = Color(0xFF8A9297),
    outlineVariant = Color(0xFF40484C)
)

private val PurpleLightColors = lightColorScheme(
    primary = Color(0xFF6B4EA2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEBDCFF),
    onPrimaryContainer = Color(0xFF250059),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7E5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E3),
    onTertiaryContainer = Color(0xFF31101D),
    background = Color(0xFFF4F0F9),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFF4F0F9),
    onSurface = Color(0xFF1D1B20),
    surfaceDim = Color(0xFFDED8E2),
    surfaceBright = Color(0xFFFAF7FF),
    surfaceContainerLowest = Color(0xFFF4F0F9),
    surfaceContainerLow = Color(0xFFF4F0F9),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFE5DEEF),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

private val PurpleDarkColors = darkColorScheme(
    primary = Color(0xFFD4BBFF),
    onPrimary = Color(0xFF3B1D71),
    primaryContainer = Color(0xFF533588),
    onPrimaryContainer = Color(0xFFEBDCFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF4A2532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD9E3),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E0E9),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E0E9),
    surfaceDim = Color(0xFF141218),
    surfaceBright = Color(0xFF3B383E),
    surfaceContainerLowest = Color(0xFF0F0D13),
    surfaceContainerLow = Color(0xFF1D1B20),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2C2930),
    surfaceContainerHighest = Color(0xFF36343B),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

private val AmberLightColors = lightColorScheme(
    primary = Color(0xFF825500),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDDB3),
    onPrimaryContainer = Color(0xFF291800),
    secondary = Color(0xFF705B40),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFCE0BE),
    onSecondaryContainer = Color(0xFF281805),
    tertiary = Color(0xFF53643E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6E9B9),
    onTertiaryContainer = Color(0xFF121F03),
    background = Color(0xFFF7F3EC),
    onBackground = Color(0xFF201B12),
    surface = Color(0xFFF7F3EC),
    onSurface = Color(0xFF201B12),
    surfaceDim = Color(0xFFE3D9CD),
    surfaceBright = Color(0xFFFFF8F3),
    surfaceContainerLowest = Color(0xFFF7F3EC),
    surfaceContainerLow = Color(0xFFF7F3EC),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFEFE4D6),
    surfaceVariant = Color(0xFFF0E0CF),
    onSurfaceVariant = Color(0xFF4F4539),
    outline = Color(0xFF817567),
    outlineVariant = Color(0xFFD3C4B4)
)

private val AmberDarkColors = darkColorScheme(
    primary = Color(0xFFFFB95B),
    onPrimary = Color(0xFF452B00),
    primaryContainer = Color(0xFF633F00),
    onPrimaryContainer = Color(0xFFFFDDB3),
    secondary = Color(0xFFDFC4A4),
    onSecondary = Color(0xFF3E2D16),
    secondaryContainer = Color(0xFF57432B),
    onSecondaryContainer = Color(0xFFFCE0BE),
    tertiary = Color(0xFFBACD9F),
    onTertiary = Color(0xFF263514),
    tertiaryContainer = Color(0xFF3C4C28),
    onTertiaryContainer = Color(0xFFD6E9B9),
    background = Color(0xFF17130B),
    onBackground = Color(0xFFECE1D4),
    surface = Color(0xFF17130B),
    onSurface = Color(0xFFECE1D4),
    surfaceDim = Color(0xFF17130B),
    surfaceBright = Color(0xFF3E382E),
    surfaceContainerLowest = Color(0xFF120E07),
    surfaceContainerLow = Color(0xFF201B12),
    surfaceContainer = Color(0xFF241F16),
    surfaceContainerHigh = Color(0xFF2F2920),
    surfaceContainerHighest = Color(0xFF3A342A),
    surfaceVariant = Color(0xFF4F4539),
    onSurfaceVariant = Color(0xFFD3C4B4),
    outline = Color(0xFF9C8E80),
    outlineVariant = Color(0xFF4F4539)
)

private val CrimsonLightColors = lightColorScheme(
    primary = Color(0xFF980038),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9DD),
    onPrimaryContainer = Color(0xFF3A0010),
    secondary = Color(0xFF775659),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9DD),
    onSecondaryContainer = Color(0xFF2C1518),
    tertiary = Color(0xFF7A5930),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC1),
    onTertiaryContainer = Color(0xFF2C1700),
    background = Color(0xFFF9EFF1),
    onBackground = Color(0xFF22191A),
    surface = Color(0xFFF9EFF1),
    onSurface = Color(0xFF22191A),
    surfaceDim = Color(0xFFE5D6D7),
    surfaceBright = Color(0xFFFFF8F7),
    surfaceContainerLowest = Color(0xFFF9EFF1),
    surfaceContainerLow = Color(0xFFF9EFF1),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFF4DEE2),
    surfaceVariant = Color(0xFFF4DDDE),
    onSurfaceVariant = Color(0xFF524344),
    outline = Color(0xFF857374),
    outlineVariant = Color(0xFFD7C1C2)
)

private val CrimsonDarkColors = darkColorScheme(
    primary = Color(0xFFFFB2BE),
    onPrimary = Color(0xFF5F0021),
    primaryContainer = Color(0xFF7C002C),
    onPrimaryContainer = Color(0xFFFFD9DD),
    secondary = Color(0xFFE6BDC1),
    onSecondary = Color(0xFF44292C),
    secondaryContainer = Color(0xFF5D3F42),
    onSecondaryContainer = Color(0xFFFFD9DD),
    tertiary = Color(0xFFECBF8F),
    onTertiary = Color(0xFF462B07),
    tertiaryContainer = Color(0xFF60411B),
    onTertiaryContainer = Color(0xFFFFDCC1),
    background = Color(0xFF1F1012),
    onBackground = Color(0xFFF0DFE0),
    surface = Color(0xFF1F1012),
    onSurface = Color(0xFFF0DFE0),
    surfaceDim = Color(0xFF1F1012),
    surfaceBright = Color(0xFF473738),
    surfaceContainerLowest = Color(0xFF190B0D),
    surfaceContainerLow = Color(0xFF27181A),
    surfaceContainer = Color(0xFF2C1C1E),
    surfaceContainerHigh = Color(0xFF372628),
    surfaceContainerHighest = Color(0xFF433133),
    surfaceVariant = Color(0xFF524344),
    onSurfaceVariant = Color(0xFFD7C1C2),
    outline = Color(0xFF9E8C8D),
    outlineVariant = Color(0xFF524344)
)

private val SlateLightColors = lightColorScheme(
    primary = Color(0xFF474747),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE2E2E2),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF5E5E5E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2E2E2),
    onSecondaryContainer = Color(0xFF1B1B1B),
    tertiary = Color(0xFF616161),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE5E5E5),
    onTertiaryContainer = Color(0xFF1D1D1D),
    background = Color(0xFFF2F2F2),
    onBackground = Color(0xFF1C1C1C),
    surface = Color(0xFFF2F2F2),
    onSurface = Color(0xFF1C1C1C),
    surfaceDim = Color(0xFFDADADA),
    surfaceBright = Color(0xFFF9F9F9),
    surfaceContainerLowest = Color(0xFFF2F2F2),
    surfaceContainerLow = Color(0xFFF2F2F2),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFE2E2E2),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF777777),
    outlineVariant = Color(0xFFC6C6C6)
)

private val SlateDarkColors = darkColorScheme(
    primary = Color(0xFFC6C6C6),
    onPrimary = Color(0xFF303030),
    primaryContainer = Color(0xFF474747),
    onPrimaryContainer = Color(0xFFE2E2E2),
    secondary = Color(0xFFC6C6C6),
    onSecondary = Color(0xFF303030),
    secondaryContainer = Color(0xFF474747),
    onSecondaryContainer = Color(0xFFE2E2E2),
    tertiary = Color(0xFFC9C9C9),
    onTertiary = Color(0xFF323232),
    tertiaryContainer = Color(0xFF4A4A4A),
    onTertiaryContainer = Color(0xFFE5E5E5),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE2E2E2),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE2E2E2),
    surfaceDim = Color(0xFF121212),
    surfaceBright = Color(0xFF393939),
    surfaceContainerLowest = Color(0xFF0D0D0D),
    surfaceContainerLow = Color(0xFF1A1A1A),
    surfaceContainer = Color(0xFF1E1E1E),
    surfaceContainerHigh = Color(0xFF282828),
    surfaceContainerHighest = Color(0xFF333333),
    surfaceVariant = Color(0xFF444444),
    onSurfaceVariant = Color(0xFFC6C6C6),
    outline = Color(0xFF8E8E8E),
    outlineVariant = Color(0xFF444444)
)

val LocalContainerBorderEnabled = staticCompositionLocalOf { true }
val LocalContainerStyle = staticCompositionLocalOf { ContainerStyle.EXPRESSIVE }

@Composable
fun containerBorder(
    strokeWidth: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
): BorderStroke? {
    return if (LocalContainerBorderEnabled.current) BorderStroke(strokeWidth, color) else null
}

enum class ContainerGroupPosition { SINGLE, FIRST, MIDDLE, LAST }

@Composable
fun containerShape(expressive: Shape): Shape {
    return if (LocalContainerStyle.current == ContainerStyle.STANDARD) RoundedCornerShape(16.dp) else expressive
}

@Composable
fun containerShape(expressive: Shape, groupPosition: ContainerGroupPosition): Shape {
    if (LocalContainerStyle.current != ContainerStyle.STANDARD) return expressive
    val outer = 16.dp
    val inner = 4.dp
    return when (groupPosition) {
        ContainerGroupPosition.SINGLE -> RoundedCornerShape(outer)
        ContainerGroupPosition.FIRST -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomStart = inner, bottomEnd = inner)
        ContainerGroupPosition.MIDDLE -> RoundedCornerShape(inner)
        ContainerGroupPosition.LAST -> RoundedCornerShape(topStart = inner, topEnd = inner, bottomStart = outer, bottomEnd = outer)
    }
}

@Composable
fun containerGroupGap(): Dp {
    return if (LocalContainerStyle.current == ContainerStyle.STANDARD) 3.dp else 4.dp
}

@Composable
fun containerPadding(
    expressiveHorizontal: Dp,
    expressiveVertical: Dp,
    standardHorizontal: Dp = 16.dp,
    standardVertical: Dp = 12.dp
): PaddingValues {
    return if (LocalContainerStyle.current == ContainerStyle.STANDARD) {
        PaddingValues(horizontal = standardHorizontal, vertical = standardVertical)
    } else {
        PaddingValues(horizontal = expressiveHorizontal, vertical = expressiveVertical)
    }
}

@Composable
fun ContainerIcon(
    icon: ImageVector,
    tint: Color,
    backdropColor: Color,
    size: Dp = 40.dp
) {
    if (LocalContainerStyle.current == ContainerStyle.STANDARD) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.5f)
        )
    } else {
        Surface(
            shape = CircleShape,
            color = backdropColor,
            modifier = Modifier.size(size)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
    }
}

@Preview
@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    fontPreset: FontPreset = FontPreset.WIDE,
    amoledThemeEnabled: Boolean = false,
    colorPreset: ColorPreset = ColorPreset.SYSTEM,
    containerBorderEnabled: Boolean = true,
    containerStyle: ContainerStyle = ContainerStyle.EXPRESSIVE,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseColors = when (colorPreset) {
        ColorPreset.FOREST_SAGE -> if (useDarkTheme) SageDarkColors else SageLightColors
        ColorPreset.SUNSET_ROSE -> if (useDarkTheme) RoseDarkColors else RoseLightColors
        ColorPreset.OCEAN_DEEP -> if (useDarkTheme) OceanDarkColors else OceanLightColors
        ColorPreset.PURPLE_NEBULA -> if (useDarkTheme) PurpleDarkColors else PurpleLightColors
        ColorPreset.AMBER_GOLD -> if (useDarkTheme) AmberDarkColors else AmberLightColors
        ColorPreset.CRIMSON_BERRY -> if (useDarkTheme) CrimsonDarkColors else CrimsonLightColors
        ColorPreset.SLATE_MONO -> if (useDarkTheme) SlateDarkColors else SlateLightColors
        ColorPreset.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (useDarkTheme) DarkColors else LightColors
            }
        }
    }

    val finalColors = if (useDarkTheme) {
        if (amoledThemeEnabled) {
            baseColors.copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceDim = Color.Black,
                surfaceContainerLowest = Color.Black,
                surfaceContainerLow = Color(0xFF121212),
                surfaceContainer = Color(0xFF181818),
                surfaceContainerHigh = Color(0xFF222222),
                surfaceContainerHighest = Color(0xFF2C2C2C),
                surfaceVariant = Color(0xFF282828),
                outlineVariant = Color(0xFF383838)
            )
        } else {
            baseColors
        }
    } else {
        baseColors.copy(
            background = baseColors.surfaceContainerLow,
            surface = baseColors.surfaceContainerLow,
            surfaceContainerLowest = baseColors.surfaceContainerLow,
            surfaceContainer = Color.White,
            surfaceContainerLow = Color.White,
            surfaceContainerHigh = Color.White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    val typography = when (fontPreset) {
        FontPreset.OUTFIT -> AppTypography
        FontPreset.SYSTEM -> androidx.compose.material3.Typography()
        FontPreset.WIDE, FontPreset.AIRY -> VariableFontFactory.createTypography(fontPreset.name)
    }

    CompositionLocalProvider(
        LocalContainerBorderEnabled provides containerBorderEnabled,
        LocalContainerStyle provides containerStyle
    ) {
        MaterialTheme(
            colorScheme = finalColors,
            typography = typography,
            content = content
        )
    }
}
