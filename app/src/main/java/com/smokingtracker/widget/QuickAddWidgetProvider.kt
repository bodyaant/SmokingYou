package com.smokingtracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.content.IntentCompat
import com.smokingtracker.AchievementsCoordinator
import com.smokingtracker.R
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.di.ApplicationScopeQualifier
import com.smokingtracker.di.MainDispatcherQualifier
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
class QuickAddWidgetProvider : AppWidgetProvider(), KoinComponent {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val appContext = context.applicationContext ?: context
        updateAppWidgets(appContext, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_QUICK_ADD) {
            val appContext = context.applicationContext ?: context
            if (!isAuthorizedWidgetAction(appContext, intent)) return

            val nowElapsed = SystemClock.elapsedRealtime()
            val prevElapsed = lastActionElapsedRealtime.get()
            if (nowElapsed - prevElapsed in 0 until DEBOUNCE_WINDOW_MS) {
                return
            }
            if (!lastActionElapsedRealtime.compareAndSet(prevElapsed, nowElapsed)) {
                return
            }

            val pendingResult = goAsync()
            get<CoroutineScope>(ApplicationScopeQualifier).launch {
                try {
                    withTimeoutOrNull(BROADCAST_TIMEOUT_MS) {
                        actionMutex.withLock {
                            val repository: SmokingRepository = get()
                            val insertedId = repository.addEntry(System.currentTimeMillis(), trigger = null)
                            if (insertedId <= 0L) return@withLock

                            try {
                                get<AchievementsCoordinator>().checkAndUpdateSuspend()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                Log.e(TAG, "Achievement check failed after 1x1 widget quick add", e)
                            }

                            get<WidgetUpdateManager>().updateAll(appContext)

                            val mainDispatcher: CoroutineDispatcher =
                                runCatching { get<CoroutineDispatcher>(MainDispatcherQualifier) }
                                    .getOrDefault(Dispatchers.Main)
                            withContext(mainDispatcher) {
                                Toast.makeText(
                                    appContext,
                                    appContext.getString(R.string.widget_logged_toast),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to handle ACTION_QUICK_ADD", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun isAuthorizedWidgetAction(context: Context, intent: Intent): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (sentFromUid != -1 && sentFromUid != Process.myUid()) return false
        }
        val token = IntentCompat.getParcelableExtra(intent, EXTRA_AUTH_TOKEN, PendingIntent::class.java)
            ?: return false
        return token.creatorUid == Process.myUid() && token.creatorPackage == context.packageName
    }

    companion object {
        private const val TAG = "QuickAddWidgetProvider"
        private const val BROADCAST_TIMEOUT_MS = 9_000L
        private const val DEBOUNCE_WINDOW_MS = 750L
        private val actionMutex = Mutex()
        private val lastActionElapsedRealtime = AtomicLong(0L)

        const val ACTION_QUICK_ADD = "com.smokingtracker.ACTION_QUICK_ADD_1X1"
        private const val EXTRA_AUTH_TOKEN = "com.smokingtracker.EXTRA_WIDGET_AUTH_TOKEN"

        fun updateAppWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            if (appWidgetIds.isEmpty()) return
            try {
                val appContext = context.applicationContext ?: context
                val views = RemoteViews(appContext.packageName, R.layout.widget_quick_add)
                CookieShapeDrawable.applyToRemoteViews(views, R.id.img_cookie_bg)

                val authToken = PendingIntent.getBroadcast(
                    appContext,
                    0,
                    Intent().setPackage(appContext.packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
                val intent = Intent(appContext, QuickAddWidgetProvider::class.java).apply {
                    action = ACTION_QUICK_ADD
                    setPackage(appContext.packageName)
                    putExtra(EXTRA_AUTH_TOKEN, authToken)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_quick_add_container, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetIds, views)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update 1x1 QuickAdd widgets", e)
            }
        }
    }
}
