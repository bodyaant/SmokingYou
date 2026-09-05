package com.smokingtracker.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

val LocalVibrationEnabled = staticCompositionLocalOf { true }

object HapticFeedbackHelper {

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Standard crisp click for key actions (buttons, switches, trigger selection).
     */
    fun performClick(isVibrationEnabled: Boolean, haptic: HapticFeedback? = null, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = context?.let { getVibrator(it) }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(12L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(12L)
                }
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Light subtle tick for continuous adjustments (time picker, sliders, day navigation).
     */
    fun performTick(isVibrationEnabled: Boolean, haptic: HapticFeedback? = null, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = context?.let { getVibrator(it) }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(8L, 100))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(8L)
                }
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Heavy tactile pulse for threshold triggers (swipe-to-dismiss deletion threshold, heavy snap).
     */
    fun performHeavyThreshold(isVibrationEnabled: Boolean, haptic: HapticFeedback? = null, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = context?.let { getVibrator(it) }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(25L)
                }
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Success confirmation (e.g., resisted craving in Mindful Pause, successful restore).
     * Creates a refined double-impulse (click + subtle fall).
     */
    fun performSuccess(isVibrationEnabled: Boolean, haptic: HapticFeedback? = null, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = context?.let { getVibrator(it) }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    vibrator.areAllPrimitivesSupported(
                        VibrationEffect.Composition.PRIMITIVE_CLICK,
                        VibrationEffect.Composition.PRIMITIVE_LOW_TICK
                    )
                ) {
                    val composition = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.9f)
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.7f, 40)
                        .compose()
                    vibrator.vibrate(composition)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 20), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 20, 40, 20), -1)
                }
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Error / Reject feedback (e.g., duplicate input, invalid time in future).
     */
    fun performError(isVibrationEnabled: Boolean, haptic: HapticFeedback? = null, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = context?.let { getVibrator(it) }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 30, 50, 30), -1)
                }
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Celebratory vibration pattern for unlocking an achievement.
     */
    fun performAchievementUnlock(isVibrationEnabled: Boolean, context: Context) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = getVibrator(context)
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 50, 40, 70, 40, 100)
                    val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 50, 40, 70, 40, 100), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
