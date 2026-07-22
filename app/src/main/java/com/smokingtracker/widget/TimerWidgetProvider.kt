package com.smokingtracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.Keep
import com.smokingtracker.MainActivity
import com.smokingtracker.R
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.repository.SmokingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.Calendar

@Keep
class TimerWidgetProvider : AppWidgetProvider(), KoinComponent {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAppWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_QUICK_ADD_TIMER) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repository: SmokingRepository = get()
                    repository.addEntry(System.currentTimeMillis(), trigger = null)

                    WidgetUpdateManager.updateAll(context)

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context.applicationContext,
                            context.getString(R.string.widget_logged_toast),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object : KoinComponent {
        const val ACTION_QUICK_ADD_TIMER = "com.smokingtracker.ACTION_QUICK_ADD_TIMER"

        fun updateAppWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repository: SmokingRepository = get()
                    val dataStoreManager: DataStoreManager = get()

                    val entries = repository.getAllEntries()
                    val dailyLimit = dataStoreManager.dailyLimit.first()

                    val nonResistedEntries = entries.filter { !it.isResisted }.map { it.timestamp }
                    val lastTimestamp = nonResistedEntries.maxOrNull()

                    val calToday = Calendar.getInstance()
                    val todayYear = calToday.get(Calendar.YEAR)
                    val todayDay = calToday.get(Calendar.DAY_OF_YEAR)

                    val todayCount = nonResistedEntries.count { ts ->
                        val c = Calendar.getInstance().apply { timeInMillis = ts }
                        c.get(Calendar.YEAR) == todayYear && c.get(Calendar.DAY_OF_YEAR) == todayDay
                    }

                    val timeElapsedText = formatTimeElapsed(context, lastTimestamp)

                    val countText = if (dailyLimit > 0) {
                        context.getString(R.string.widget_today_limit_format, todayCount, dailyLimit)
                    } else {
                        context.getString(R.string.widget_today_count, todayCount)
                    }

                    val isLimitExceeded = dailyLimit > 0 && todayCount >= dailyLimit
                    val countColor = if (isLimitExceeded) {
                        Color.parseColor("#FF5252")
                    } else {
                        androidx.core.content.ContextCompat.getColor(context, R.color.widget_accent_color)
                    }

                    for (appWidgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.widget_timer)

                        val pillColor = androidx.core.content.ContextCompat.getColor(context, R.color.widget_pill_bg_color)
                        val cookieBitmap = CookieShapeDrawable.createCookieBitmap(context, 52, pillColor, petals = 12)
                        views.setImageViewBitmap(R.id.img_cookie_bg, cookieBitmap)

                        views.setTextViewText(R.id.tv_widget_timer, timeElapsedText)
                        views.setTextViewText(R.id.tv_widget_today_count, countText)
                        views.setTextColor(R.id.tv_widget_today_count, countColor)

                        val appIntent = Intent(context, MainActivity::class.java)
                        val appPendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            appIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_timer_left_section, appPendingIntent)

                        val addIntent = Intent(context, TimerWidgetProvider::class.java).apply {
                            action = ACTION_QUICK_ADD_TIMER
                        }
                        val addPendingIntent = PendingIntent.getBroadcast(
                            context,
                            1,
                            addIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.btn_widget_quick_add, addPendingIntent)

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun formatTimeElapsed(context: Context, lastTimestamp: Long?): String {
            if (lastTimestamp == null) return context.getString(R.string.widget_no_entries)
            val diffMs = (System.currentTimeMillis() - lastTimestamp).coerceAtLeast(0L)
            val diffMins = (diffMs / (1000 * 60)).toInt()
            if (diffMins < 1) {
                return context.getString(R.string.widget_just_now)
            }

            val hours = diffMins / 60
            val mins = diffMins % 60
            val days = hours / 24
            val remainingHours = hours % 24

            val unitD = context.getString(R.string.widget_unit_d)
            val unitH = context.getString(R.string.widget_unit_h)
            val unitM = context.getString(R.string.widget_unit_m)

            return when {
                days > 0 -> "${days}${unitD} ${remainingHours}${unitH}"
                hours > 0 -> if (mins > 0) "${hours}${unitH} ${mins}${unitM}" else "${hours}${unitH}"
                else -> "${mins}${unitM}"
            }
        }
    }
}
