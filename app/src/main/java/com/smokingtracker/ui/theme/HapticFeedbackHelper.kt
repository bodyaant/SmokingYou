package com.smokingtracker.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticFeedbackHelper {

    var isVibrationEnabled: Boolean = false

    fun performClick(haptic: HapticFeedback?, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            context?.let { ctx ->
                vibrateOneShot(ctx, 30L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun performSuccess(haptic: HapticFeedback?, context: Context? = null) {
        if (!isVibrationEnabled) return
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            context?.let { ctx ->
                vibratePattern(ctx, longArrayOf(0, 40, 60, 40))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun performAchievementUnlock(context: Context) {
        if (!isVibrationEnabled) return
        try {
            vibratePattern(context, longArrayOf(0, 80, 50, 100, 50, 120))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibratePattern(context: Context, timings: LongArray) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(timings, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(timings, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrateOneShot(context: Context, milliseconds: Long) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(milliseconds)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
