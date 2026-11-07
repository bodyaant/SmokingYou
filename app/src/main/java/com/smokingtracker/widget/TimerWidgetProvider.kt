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
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.smokingtracker.AchievementsCoordinator
import com.smokingtracker.MainActivity
import com.smokingtracker.R
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.di.ApplicationScopeQualifier
import com.smokingtracker.di.MainDispatcherQualifier
import com.smokingtracker.notification.BootReceiver
import com.smokingtracker.startOfDayMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicLong

@Keep
class TimerWidgetProvider : AppWidgetProvider(), KoinComponent {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (appWidgetIds.isEmpty()) return
        val pendingResult = goAsync()
        val appContext = context.applicationContext ?: context
        get<CoroutineScope>(ApplicationScopeQualifier).launch {
            try {
                withTimeoutOrNull(BROADCAST_TIMEOUT_MS) {
                    updateAppWidgets(appContext, appWidgetManager, appWidgetIds)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update timer widgets in onUpdate", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val pendingResult = goAsync()
        val appContext = context.applicationContext ?: context
        get<CoroutineScope>(ApplicationScopeQualifier).launch {
            try {
                withTimeoutOrNull(BROADCAST_TIMEOUT_MS) {
                    updateAppWidgets(appContext, appWidgetManager, intArrayOf(appWidgetId))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update timer widget on options changed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action != ACTION_QUICK_ADD_TIMER && action != ACTION_RESIST_TIMER) return

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
                        val mainDispatcher: CoroutineDispatcher =
                            runCatching { get<CoroutineDispatcher>(MainDispatcherQualifier) }
                                .getOrDefault(Dispatchers.Main)

                        when (action) {
                            ACTION_QUICK_ADD_TIMER -> {
                                val insertedId = repository.addEntry(System.currentTimeMillis(), trigger = null)
                                if (insertedId <= 0L) return@withLock

                                try {
                                    get<AchievementsCoordinator>().checkAndUpdateSuspend()
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    Log.e(TAG, "Achievement check failed after timer widget quick add", e)
                                }

                                get<WidgetUpdateManager>().updateAll(appContext)

                                withContext(mainDispatcher) {
                                    Toast.makeText(
                                        appContext,
                                        appContext.getString(R.string.widget_logged_toast),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            ACTION_RESIST_TIMER -> {
                                val insertedId = repository.addResistedEntry(System.currentTimeMillis(), trigger = null)
                                if (insertedId <= 0L) return@withLock

                                try {
                                    get<AchievementsCoordinator>().checkAndUpdateSuspend()
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    Log.e(TAG, "Achievement check failed after timer widget resist", e)
                                }

                                get<WidgetUpdateManager>().updateAll(appContext)

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
                Log.e(TAG, "Failed to handle widget action: $action", e)
            } finally {
                pendingResult.finish()
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

    private data class WidgetLayoutSizeClass(
        val isCompactWidth: Boolean,
        val isCompactHeight: Boolean
    )

    companion object : KoinComponent {
        private const val TAG = "TimerWidgetProvider"
        private const val BROADCAST_TIMEOUT_MS = 9_000L
        private const val DEBOUNCE_WINDOW_MS = 750L
        private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
        private const val COMPACT_WIDTH_THRESHOLD_DP = 165
        private const val COMPACT_HEIGHT_THRESHOLD_DP = 52

        private val actionMutex = Mutex()
        private val lastActionElapsedRealtime = AtomicLong(0L)

        const val ACTION_QUICK_ADD_TIMER = "com.smokingtracker.ACTION_QUICK_ADD_TIMER"
        const val ACTION_RESIST_TIMER = "com.smokingtracker.ACTION_RESIST_TIMER"
        private const val EXTRA_AUTH_TOKEN = "com.smokingtracker.EXTRA_WIDGET_AUTH_TOKEN"

        suspend fun updateAppWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            if (appWidgetIds.isEmpty()) return
            try {
                val appContext = context.applicationContext ?: context
                val repository: SmokingRepository = get()
                val userPreferences: UserPreferences = get()
                val appMetaPreferences: AppMetaPreferences? = runCatching { get<AppMetaPreferences>() }.getOrNull()

                val zoneId = ZoneId.systemDefault()
                val today = LocalDate.now(zoneId)
                val todayStart = startOfDayMillis(today, zoneId)
                val todayEnd = (startOfDayMillis(today.plusDays(1), zoneId) - 1L).coerceAtLeast(todayStart)

                val todayCount = repository.getEntryCountInRange(todayStart, todayEnd, isResisted = false)
                val todayResistedCount = repository.getEntryCountInRange(todayStart, todayEnd, isResisted = true)
                val lastSmokedTimestamp = repository.getLastSmokedEntry()?.timestamp?.takeIf { it > 0L }

                val dailyLimit = userPreferences.dailyLimit.first()
                val showResistButtonPref = userPreferences.widgetShowResistButton.first()

                val now = System.currentTimeMillis()
                val fallbackStartTimestamp = if (lastSmokedTimestamp == null) {
                    repository.getLastEntry()?.timestamp?.takeIf { it in 1..now }
                        ?: appMetaPreferences?.appLaunchDates?.first()?.minOrNull()?.takeIf { it in 1..now }
                        ?: userPreferences.historicalStartDate.first().takeIf { it in 1..now }
                } else {
                    null
                }
                val referenceTimestamp = lastSmokedTimestamp ?: fallbackStartTimestamp

                val labelText = if (lastSmokedTimestamp == null && referenceTimestamp != null) {
                    appContext.getString(R.string.ach_streak_smokefree)
                } else {
                    appContext.getString(R.string.widget_last_smoke)
                }

                val baseCountText = if (dailyLimit > 0) {
                    appContext.getString(R.string.widget_today_limit_format, todayCount, dailyLimit)
                } else {
                    appContext.getString(R.string.widget_today_count, todayCount)
                }
                val countText = if (todayResistedCount > 0) {
                    baseCountText + appContext.getString(R.string.notification_content_resisted_count, todayResistedCount)
                } else {
                    baseCountText
                }

                val isLimitExceeded = dailyLimit > 0 && todayCount > dailyLimit
                val countColor = ContextCompat.getColor(
                    appContext,
                    if (isLimitExceeded) R.color.widget_limit_exceeded_color else R.color.widget_accent_color
                )

                val authToken = PendingIntent.getBroadcast(
                    appContext,
                    0,
                    Intent().setPackage(appContext.packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )

                val appIntent = (appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
                    ?: Intent(appContext, MainActivity::class.java)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val appPendingIntent = PendingIntent.getActivity(
                    appContext,
                    0,
                    appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val resistIntent = Intent(appContext, TimerWidgetProvider::class.java).apply {
                    action = ACTION_RESIST_TIMER
                    setPackage(appContext.packageName)
                    putExtra(EXTRA_AUTH_TOKEN, authToken)
                }
                val resistPendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    2,
                    resistIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val addIntent = Intent(appContext, TimerWidgetProvider::class.java).apply {
                    action = ACTION_QUICK_ADD_TIMER
                    setPackage(appContext.packageName)
                    putExtra(EXTRA_AUTH_TOKEN, authToken)
                }
                val addPendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    1,
                    addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val groups = appWidgetIds.groupBy { widgetId ->
                    val options = runCatching { appWidgetManager.getAppWidgetOptions(widgetId) }.getOrNull()
                    val minWidthDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180) ?: 180
                    val minHeightDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 60) ?: 60
                    WidgetLayoutSizeClass(
                        isCompactWidth = minWidthDp in 1 until COMPACT_WIDTH_THRESHOLD_DP,
                        isCompactHeight = minHeightDp in 1 until COMPACT_HEIGHT_THRESHOLD_DP
                    )
                }

                val nowElapsedRealtime = SystemClock.elapsedRealtime()
                val unitD = appContext.getString(R.string.widget_unit_d)

                for ((sizeClass, groupIds) in groups) {
                    val views = RemoteViews(appContext.packageName, R.layout.widget_timer)
                    CookieShapeDrawable.applyToRemoteViews(views, R.id.img_cookie_bg)
                    CookieShapeDrawable.applyToRemoteViews(views, R.id.img_cookie_bg_resist)

                    val showResist = showResistButtonPref && !sizeClass.isCompactWidth
                    views.setViewVisibility(
                        R.id.btn_widget_resist,
                        if (showResist) View.VISIBLE else View.GONE
                    )
                    views.setViewVisibility(
                        R.id.tv_widget_label,
                        if (sizeClass.isCompactHeight) View.GONE else View.VISIBLE
                    )
                    views.setTextViewText(R.id.tv_widget_label, labelText)

                    if (referenceTimestamp != null) {
                        val elapsedMs = (now - referenceTimestamp).coerceAtLeast(0L)
                        val days = elapsedMs / DAY_MILLIS
                        if (days > 0L) {
                            val remainderMs = elapsedMs % DAY_MILLIS
                            views.setChronometer(
                                R.id.tv_widget_timer,
                                nowElapsedRealtime - remainderMs,
                                "${days}${unitD} %s",
                                true
                            )
                        } else {
                            views.setChronometer(
                                R.id.tv_widget_timer,
                                nowElapsedRealtime - elapsedMs,
                                null,
                                true
                            )
                        }
                    } else {
                        views.setChronometer(R.id.tv_widget_timer, nowElapsedRealtime, null, false)
                        views.setTextViewText(R.id.tv_widget_timer, appContext.getString(R.string.widget_no_entries))
                    }

                    views.setTextViewText(R.id.tv_widget_today_count, countText)
                    views.setTextColor(R.id.tv_widget_today_count, countColor)

                    views.setOnClickPendingIntent(R.id.widget_timer_main_container, appPendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_timer_left_section, appPendingIntent)
                    views.setOnClickPendingIntent(R.id.btn_widget_resist, resistPendingIntent)
                    views.setOnClickPendingIntent(R.id.btn_widget_quick_add, addPendingIntent)

                    appWidgetManager.updateAppWidget(groupIds.toIntArray(), views)
                }

                BootReceiver.scheduleMidnightRollover(appContext)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update 3x1 Timer widgets", e)
            }
        }
    }
}
