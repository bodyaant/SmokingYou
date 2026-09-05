package com.smokingtracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.Keep
import com.smokingtracker.AchievementsCoordinator
import com.smokingtracker.R
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Keep
class NotificationActionReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        get<CoroutineScope>().launch {
            try {
                val repository: SmokingRepository = get()
                val achievementsCoordinator: AchievementsCoordinator = get()

                when (action) {
                    ACTION_ADD -> {
                        repository.addEntry(System.currentTimeMillis(), trigger = null)
                        achievementsCoordinator.checkAndUpdate()
                        WidgetUpdateManager.updateAll(context)

                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context.applicationContext,
                                context.getString(R.string.notification_toast_added),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    ACTION_RESIST -> {
                        repository.addResistedEntry(System.currentTimeMillis(), trigger = null)
                        achievementsCoordinator.checkAndUpdate()
                        WidgetUpdateManager.updateAll(context)

                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context.applicationContext,
                                context.getString(R.string.notification_toast_resisted),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_ADD = "com.smokingtracker.ACTION_NOTIFICATION_ADD"
        const val ACTION_RESIST = "com.smokingtracker.ACTION_NOTIFICATION_RESIST"
    }
}
