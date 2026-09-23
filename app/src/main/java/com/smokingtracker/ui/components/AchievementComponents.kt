package com.smokingtracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smokingtracker.AchievementCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun badgeShape(id: String): Shape = when (abs(id.hashCode()) % 6) {
    0 -> MaterialShapes.Cookie9Sided.toShape()
    1 -> MaterialShapes.Clover8Leaf.toShape()
    2 -> MaterialShapes.Sunny.toShape()
    3 -> MaterialShapes.Cookie12Sided.toShape()
    4 -> MaterialShapes.Burst.toShape()
    else -> MaterialShapes.Flower.toShape()
}

fun badgeIcon(id: String, isSecret: Boolean, isUnlocked: Boolean): ImageVector {
    if (isSecret && !isUnlocked) {
        return Icons.AutoMirrored.Rounded.HelpOutline
    }
    return when (id) {
        "login_1" -> Icons.Rounded.Star
        "login_3" -> Icons.Rounded.WbSunny
        "login_7" -> Icons.Rounded.CalendarMonth
        "login_30" -> Icons.Rounded.LocalFireDepartment
        "login_90" -> Icons.Rounded.Whatshot
        "login_180" -> Icons.Rounded.MilitaryTech
        "login_365" -> Icons.Rounded.Diamond

        "nosmoke_1d" -> Icons.AutoMirrored.Rounded.DirectionsWalk
        "nosmoke_3d" -> Icons.Rounded.FitnessCenter
        "nosmoke_1w" -> Icons.Rounded.SelfImprovement
        "nosmoke_1m" -> Icons.Rounded.Spa
        "nosmoke_3m" -> Icons.Rounded.Shield
        "nosmoke_6m" -> Icons.Rounded.WorkspacePremium
        "nosmoke_1y" -> Icons.Rounded.EmojiEvents

        "secret_night_owl" -> Icons.Rounded.Bedtime
        "secret_morning_ritual" -> Icons.Rounded.Alarm
        "secret_synchronization" -> Icons.Rounded.Schedule
        "secret_punctuality" -> Icons.Rounded.Timer
        "secret_hesitant" -> Icons.AutoMirrored.Rounded.Undo
        "secret_explorer" -> Icons.Rounded.Palette
        "secret_archivist" -> Icons.Rounded.Archive
        "secret_analytics_collector" -> Icons.Rounded.Insights
        "secret_double_damage" -> Icons.Rounded.Bolt
        "secret_inflation" -> Icons.AutoMirrored.Rounded.TrendingUp
        "secret_crisis" -> Icons.Rounded.Psychology
        "secret_blind_eye" -> Icons.Rounded.VisibilityOff

        else -> Icons.Rounded.EmojiEvents
    }
}

@Composable
fun badgeGradient(category: AchievementCategory, isUnlocked: Boolean): Brush {
    val darkBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val lightBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    return remember(category, isUnlocked, darkBg, lightBg, primary, tertiary) {
        if (!isUnlocked) {
            Brush.linearGradient(listOf(darkBg, lightBg))
        } else {
            when (category) {
                AchievementCategory.LOGIN -> Brush.linearGradient(
                    listOf(
                        Color(0xFFFF8F00),
                        Color(0xFFFFB300),
                        primary
                    )
                )
                AchievementCategory.NO_SMOKE -> Brush.linearGradient(
                    listOf(
                        Color(0xFF00897B),
                        Color(0xFF26A69A),
                        primary
                    )
                )
                AchievementCategory.SECRET -> Brush.linearGradient(
                    listOf(
                        Color(0xFF7B1FA2),
                        Color(0xFF512DA8),
                        tertiary
                    )
                )
            }
        }
    }
}

@Composable
fun BadgeMedallion(
    id: String,
    category: AchievementCategory,
    isUnlocked: Boolean,
    isSecret: Boolean = false,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val shape = badgeShape(id)
    val icon = remember(id, isSecret, isUnlocked) { badgeIcon(id, isSecret, isUnlocked) }
    val bgBrush = badgeGradient(category, isUnlocked)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bgBrush)
            .then(
                if (!isUnlocked) {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        shape
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isUnlocked) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            },
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

@Composable
fun Modifier.entrance(
    index: Int = 0,
    alreadyAppeared: Boolean = false,
    delayMs: Long? = null,
    onAppeared: () -> Unit = {}
): Modifier {
    if (alreadyAppeared) return this

    val alphaAnim = remember { Animatable(0f) }
    val offsetAnim = remember { Animatable(32f) }
    val scaleAnim = remember { Animatable(0.95f) }

    val actualDelay = delayMs ?: (index * 36L).coerceAtMost(360L)

    DisposableEffect(Unit) {
        onDispose {
            onAppeared()
        }
    }

    LaunchedEffect(Unit) {
        if (actualDelay > 0L) {
            delay(actualDelay)
        }
        launch { alphaAnim.animateTo(1f, tween(220, easing = LinearOutSlowInEasing)) }
        launch {
            offsetAnim.animateTo(
                0f,
                spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMedium)
            )
        }
        launch {
            scaleAnim.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
            onAppeared()
        }
    }

    return graphicsLayer {
        alpha = alphaAnim.value
        translationY = offsetAnim.value.dp.toPx()
        scaleX = scaleAnim.value
        scaleY = scaleAnim.value
    }
}

@Composable
fun Modifier.pulse(from: Float = 0.96f, to: Float = 1.05f, durationMs: Int = 1400): Modifier {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = from,
        targetValue = to,
        animationSpec = infiniteRepeatable(
            tween(durationMs, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val spin: Float,
    val isCircle: Boolean,
    val originX: Float,
    val drift: Float
)

@Composable
fun ConfettiBurst(
    burstKey: Int,
    colors: List<Color> = listOf(
        Color(0xFFFFC107), Color(0xFFFF5722), Color(0xFF4CAF50),
        Color(0xFF00BCD4), Color(0xFFE91E63), Color(0xFF9C27B0)
    ),
    modifier: Modifier = Modifier
) {
    if (burstKey <= 0) return
    val progress = remember(burstKey) { Animatable(0f) }
    var particles by remember(burstKey) { mutableStateOf<List<ConfettiParticle>>(emptyList()) }

    LaunchedEffect(burstKey) {
        particles = List(120) {
            ConfettiParticle(
                angle = (-95f + Random.nextFloat() * 100f - 50f),
                speed = 0.55f + Random.nextFloat() * 1.15f,
                size = 8f + Random.nextFloat() * 14f,
                color = colors[Random.nextInt(colors.size)],
                spin = Random.nextFloat() * 720f - 360f,
                isCircle = Random.nextBoolean(),
                originX = 0.15f + Random.nextFloat() * 0.7f,
                drift = Random.nextFloat() * 0.3f - 0.15f
            )
        }
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 2200, easing = LinearEasing))
    }

    val t = progress.value
    if (t >= 1f) return

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            val rad = Math.toRadians(p.angle.toDouble())
            val dist = p.speed * h * 0.55f * t
            val gravity = 1200f * t * t
            val x = p.originX * w + (cos(rad) * dist).toFloat() + p.drift * w * t
            val y = h * 0.38f + (sin(rad) * dist).toFloat() + gravity * 0.35f
            val alpha = (1f - t).coerceIn(0f, 1f)
            rotate(degrees = p.spin * t, pivot = Offset(x, y)) {
                if (p.isCircle) {
                    drawCircle(p.color.copy(alpha = alpha), radius = p.size / 2, center = Offset(x, y))
                } else {
                    drawRect(
                        p.color.copy(alpha = alpha),
                        topLeft = Offset(x - p.size / 2, y - p.size / 4),
                        size = Size(p.size, p.size / 2)
                    )
                }
            }
        }
    }
}
