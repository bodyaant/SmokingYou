package com.smokingtracker.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.smokingtracker.notification.OngoingNotificationManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import java.util.concurrent.atomic.AtomicBoolean

class WidgetUpdateManager(
    context: Context,
    private val ongoingNotificationManager: OngoingNotificationManager,
    private val applicationScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val appContext: Context = context.applicationContext ?: context
    private val mutex = Mutex()
    private val hasPendingUpdate = AtomicBoolean(false)
    private val staticWidgetsInitialized = AtomicBoolean(false)

    suspend fun updateAll(
        context: Context = appContext,
        includeOngoingNotification: Boolean = true,
        forceStaticWidgets: Boolean = false
    ): Unit = withContext(ioDispatcher) {
        val targetContext = context.applicationContext ?: context
        if (forceStaticWidgets) {
            staticWidgetsInitialized.set(false)
        }
        hasPendingUpdate.set(true)
        drainPendingUpdates(targetContext, includeOngoingNotification)
    }

    fun updateAllAsync(
        context: Context = appContext,
        includeOngoingNotification: Boolean = true,
        forceStaticWidgets: Boolean = false
    ) {
        val targetContext = context.applicationContext ?: context
        if (forceStaticWidgets) {
            staticWidgetsInitialized.set(false)
        }
        hasPendingUpdate.set(true)
        applicationScope.launch(ioDispatcher) {
            drainPendingUpdates(targetContext, includeOngoingNotification)
        }
    }

    private suspend fun drainPendingUpdates(
        targetContext: Context,
        includeOngoingNotification: Boolean
    ) {
        try {
            mutex.withLock {
                while (hasPendingUpdate.compareAndSet(true, false)) {
                    performUpdate(targetContext, includeOngoingNotification)
                }
            }
        } catch (e: CancellationException) {
            hasPendingUpdate.set(true)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widgets", e)
        }
    }

    private suspend fun performUpdate(
        targetContext: Context,
        includeOngoingNotification: Boolean
    ) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(targetContext)

            if (staticWidgetsInitialized.compareAndSet(false, true)) {
                val quickAddComponent = ComponentName(targetContext, QuickAddWidgetProvider::class.java)
                val quickAddIds = appWidgetManager.getAppWidgetIds(quickAddComponent)
                if (quickAddIds.isNotEmpty()) {
                    QuickAddWidgetProvider.updateAppWidgets(targetContext, appWidgetManager, quickAddIds)
                }
            }

            val timerComponent = ComponentName(targetContext, TimerWidgetProvider::class.java)
            val timerIds = appWidgetManager.getAppWidgetIds(timerComponent)
            if (timerIds.isNotEmpty()) {
                TimerWidgetProvider.updateAppWidgets(targetContext, appWidgetManager, timerIds)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error updating home screen widgets", e)
        }

        if (includeOngoingNotification) {
            try {
                ongoingNotificationManager.updateSuspend(targetContext)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error updating ongoing notification", e)
            }
        }
    }

    companion object {
        private const val TAG = "WidgetUpdateManager"

        suspend fun updateAll(context: Context) {
            GlobalContext.getOrNull()?.get<WidgetUpdateManager>()?.updateAll(context)
        }

        fun updateAllAsync(context: Context) {
            GlobalContext.getOrNull()?.get<WidgetUpdateManager>()?.updateAllAsync(context)
        }
    }
}
