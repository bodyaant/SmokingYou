package com.smokingtracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.Keep
import com.smokingtracker.AchievementsCoordinator
import com.smokingtracker.R
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.di.ApplicationScopeQualifier
import com.smokingtracker.di.MainDispatcherQualifier
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.concurrent.atomic.AtomicLong

@Keep
class NotificationActionReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != ACTION_ADD && action != ACTION_RESIST) return

        val nowElapsed = SystemClock.elapsedRealtime()
        val prevElapsed = lastActionElapsedRealtime.get()
        if (nowElapsed - prevElapsed in 0 until DEBOUNCE_WINDOW_MS) {
            return
        }
        if (!lastActionElapsedRealtime.compareAndSet(prevElapsed, nowElapsed)) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext ?: context

        get<CoroutineScope>(ApplicationScopeQualifier).launch {
            try {
                withTimeoutOrNull(BROADCAST_TIMEOUT_MS) {
                    actionMutex.withLock {
                        val repository: SmokingRepository = get()
                        val achievementsCoordinator: AchievementsCoordinator = get()
                        val widgetUpdateManager: WidgetUpdateManager = get()
                        val mainDispatcher: CoroutineDispatcher =
                            runCatching { get<CoroutineDispatcher>(MainDispatcherQualifier) }
                                .getOrDefault(Dispatchers.Main)

                        when (action) {
                            ACTION_ADD -> {
                                val insertedId = repository.addEntry(System.currentTimeMillis(), trigger = null)
                                if (insertedId <= 0L) return@withLock

                                try {
                                    achievementsCoordinator.checkAndUpdateSuspend()
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    Log.e(TAG, "Achievement check failed after notification add", e)
                                }

                                widgetUpdateManager.updateAll(appContext)

                                withContext(mainDispatcher) {
                                    Toast.makeText(
                                        appContext,
                                        appContext.getString(R.string.notification_toast_added),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            ACTION_RESIST -> {
                                val insertedId = repository.addResistedEntry(System.currentTimeMillis(), trigger = null)
                                if (insertedId <= 0L) return@withLock

                                try {
                                    achievementsCoordinator.checkAndUpdateSuspend()
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    Log.e(TAG, "Achievement check failed after notification resist", e)
                                }

                                widgetUpdateManager.updateAll(appContext)

                                withContext(mainDispatcher) {
                                    Toast.makeText(
                                        appContext,
                                        appContext.getString(R.string.notification_toast_resisted),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle notification action: $action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "NotifActionReceiver"
        private const val BROADCAST_TIMEOUT_MS = 9_000L
        private const val DEBOUNCE_WINDOW_MS = 750L
        private val actionMutex = Mutex()
        private val lastActionElapsedRealtime = AtomicLong(0L)

        const val ACTION_ADD = "com.smokingtracker.ACTION_NOTIFICATION_ADD"
        const val ACTION_RESIST = "com.smokingtracker.ACTION_NOTIFICATION_RESIST"
    }
}
