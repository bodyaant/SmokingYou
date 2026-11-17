package com.smokingtracker.ui.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun HeroTimerSection(
    lastEntryTimestamp: Long?,
    modifier: Modifier = Modifier
) {
    val calculatingText = stringResource(R.string.time_calculating)
    val invalidEntryText = stringResource(R.string.invalid_future_entry)
    val startTrackingText = stringResource(R.string.start_tracking)
    val formatHm = stringResource(R.string.duration_hm)
    val formatHms = stringResource(R.string.duration_hms)
    val formatMs = stringResource(R.string.duration_ms)
    val locale = LocalLocale.current.platformLocale

    var timePassedText by remember { mutableStateOf(calculatingText) }

    LaunchedEffect(lastEntryTimestamp, formatHm, formatHms, formatMs, locale, calculatingText, invalidEntryText, startTrackingText) {
        while (true) {
            if (lastEntryTimestamp != null) {
                val now = System.currentTimeMillis()
                val diff = now - lastEntryTimestamp
                if (diff < 0) {
                    timePassedText = invalidEntryText
                } else {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
                    timePassedText = when {
                        hours >= 24 -> String.format(locale, formatHm, hours, minutes)
                        hours > 0 -> String.format(locale, formatHms, hours, minutes, seconds)
                        else -> String.format(locale, formatMs, minutes, seconds)
                    }
                }
            } else {
                timePassedText = startTrackingText
            }
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer { alpha = pulseAlpha }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.time_past_label),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
    val heroTextStyle = when {
        timePassedText.length > 16 -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
        timePassedText.length > 10 -> MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
        else -> MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold)
    }
    Text(
        text = timePassedText,
        style = heroTextStyle,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
