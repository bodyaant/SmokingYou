package com.smokingtracker.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.smokingtracker.R
import java.util.concurrent.atomic.AtomicLong

object WidgetPinningHelper {
    private const val TAG = "WidgetPinningHelper"
    private const val DEFAULT_DEBOUNCE_MS = 1000L
    private val lastPinRequestMs = AtomicLong(0L)

    fun isRequestPinSupported(context: Context): Boolean {
        return try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.isRequestPinAppWidgetSupported
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check pin support", e)
            false
        }
    }

    fun requestPin(
        context: Context,
        providerClass: Class<*>,
        debounceMs: Long = DEFAULT_DEBOUNCE_MS,
        onUnsupported: (() -> Unit)? = null
    ): Boolean {
        val now = SystemClock.elapsedRealtime()
        val last = lastPinRequestMs.get()
        if (now - last < debounceMs) {
            return false
        }
        if (!lastPinRequestMs.compareAndSet(last, now)) {
            return false
        }

        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val myProvider = ComponentName(context, providerClass)
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
            } else {
                if (onUnsupported != null) {
                    onUnsupported()
                } else {
                    Toast.makeText(
                        context,
                        R.string.widget_pin_unsupported_toast,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request pin widget", e)
        }
        return true
    }

    fun requestPinTimerWidget(
        context: Context,
        debounceMs: Long = DEFAULT_DEBOUNCE_MS,
        onUnsupported: (() -> Unit)? = null
    ): Boolean {
        return requestPin(context, TimerWidgetProvider::class.java, debounceMs, onUnsupported)
    }

    fun requestPinQuickAddWidget(
        context: Context,
        debounceMs: Long = DEFAULT_DEBOUNCE_MS,
        onUnsupported: (() -> Unit)? = null
    ): Boolean {
        return requestPin(context, QuickAddWidgetProvider::class.java, debounceMs, onUnsupported)
    }
}
