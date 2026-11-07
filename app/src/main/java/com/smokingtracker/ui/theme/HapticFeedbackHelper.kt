package com.smokingtracker.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

val LocalVibrationEnabled = compositionLocalOf { true }

object HapticFeedbackHelper {

    @Volatile
    private var cachedVibrator: Vibrator? = null

    private fun getVibrator(context: Context): Vibrator? {
        cachedVibrator?.let { return it }
        return try {
            val appContext = context.applicationContext ?: context
            val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (resolved != null) {
                cachedVibrator = resolved
            }
            resolved
        } catch (e: Exception) {
            null
        }
    }

    private fun isPredefinedEffectSupported(vibrator: Vibrator, effectId: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                vibrator.areAllEffectsSupported(effectId) != Vibrator.VIBRATION_EFFECT_SUPPORT_NO
            } catch (e: Exception) {
                true
            }
        } else {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }
    }

    fun performClick(isVibrationEnabled: Boolean, haptic: HapticFeedback? = null, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = context?.let { getVibrator(it) }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    isPredefinedEffectSupported(vibrator, VibrationEffect.EFFECT_CLICK)
                ) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    vibrator.vibrate(VibrationEffect.createOneShot(12L, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (e: Exception) {
            haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun performTick(isVibrationEnabled: Boolean, haptic: HapticFeedback? = null, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = context?.let { getVibrator(it) }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    isPredefinedEffectSupported(vibrator, VibrationEffect.EFFECT_TICK)
                ) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else {
                    val amplitude = if (vibrator.hasAmplitudeControl()) 100 else VibrationEffect.DEFAULT_AMPLITUDE
                    vibrator.vibrate(VibrationEffect.createOneShot(8L, amplitude))
                }
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (e: Exception) {
            haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun performHeavyThreshold(isVibrationEnabled: Boolean, haptic: HapticFeedback? = null, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = context?.let { getVibrator(it) }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    isPredefinedEffectSupported(vibrator, VibrationEffect.EFFECT_HEAVY_CLICK)
                ) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else {
                    vibrator.vibrate(VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (e: Exception) {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun performSuccess(isVibrationEnabled: Boolean, haptic: HapticFeedback? = null, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = context?.let { getVibrator(it) }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
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
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    isPredefinedEffectSupported(vibrator, VibrationEffect.EFFECT_DOUBLE_CLICK)
                ) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                } else {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 20), -1))
                }
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (e: Exception) {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun performError(isVibrationEnabled: Boolean, haptic: HapticFeedback? = null, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = context?.let { getVibrator(it) }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    isPredefinedEffectSupported(vibrator, VibrationEffect.EFFECT_DOUBLE_CLICK)
                ) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                } else {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), -1))
                }
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (e: Exception) {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun performAchievementUnlock(isVibrationEnabled: Boolean, context: Context) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = getVibrator(context)
            if (vibrator?.hasVibrator() == true) {
                val timings = longArrayOf(0, 50, 40, 70, 40, 100)
                if (vibrator.hasAmplitudeControl()) {
                    val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                }
            }
        } catch (e: Exception) {
            // Ignore vibration hardware errors
        }
    }
}
