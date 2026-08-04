package com.smokingtracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.Keep
import com.smokingtracker.R
import com.smokingtracker.data.repository.SmokingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Keep
class QuickAddWidgetProvider : AppWidgetProvider(), KoinComponent {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAppWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_QUICK_ADD) {
            val pendingResult = goAsync()
            get<CoroutineScope>().launch {
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
        const val ACTION_QUICK_ADD = "com.smokingtracker.ACTION_QUICK_ADD_1X1"

        fun updateAppWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_quick_add)

                val pillColor = androidx.core.content.ContextCompat.getColor(context, R.color.widget_pill_bg_color)
                val cookieBitmap = CookieShapeDrawable.createCookieBitmap(context, 50, pillColor, petals = 12)
                views.setImageViewBitmap(R.id.img_cookie_bg, cookieBitmap)

                val intent = Intent(context, QuickAddWidgetProvider::class.java).apply {
                    action = ACTION_QUICK_ADD
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_quick_add_container, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
