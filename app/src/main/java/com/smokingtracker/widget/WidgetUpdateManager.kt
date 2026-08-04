package com.smokingtracker.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object WidgetUpdateManager : KoinComponent {

    fun updateAll(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val quickAddComponent = ComponentName(context, QuickAddWidgetProvider::class.java)
        val quickAddIds = appWidgetManager.getAppWidgetIds(quickAddComponent)
        if (quickAddIds.isNotEmpty()) {
            QuickAddWidgetProvider.updateAppWidgets(context, appWidgetManager, quickAddIds)
        }

        val timerComponent = ComponentName(context, TimerWidgetProvider::class.java)
        val timerIds = appWidgetManager.getAppWidgetIds(timerComponent)
        if (timerIds.isNotEmpty()) {
            TimerWidgetProvider.updateAppWidgets(context, appWidgetManager, timerIds)
        }
    }

    fun updateAllAsync(context: Context) {
        get<CoroutineScope>().launch {
            updateAll(context)
        }
    }
}
