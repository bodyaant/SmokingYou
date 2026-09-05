package com.smokingtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.smokingtracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SmokingTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SmokingTrackerApp)
            modules(appModule)
        }
        createNotificationChannel()
        com.smokingtracker.notification.OngoingNotificationManager.update(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val achievementChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }

            val ongoingLowChannel = NotificationChannel(
                CHANNEL_ONGOING_LOW,
                getString(R.string.notification_channel_ongoing_low_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_ongoing_desc)
                setShowBadge(false)
            }

            val ongoingDefaultChannel = NotificationChannel(
                CHANNEL_ONGOING_DEFAULT,
                getString(R.string.notification_channel_ongoing_default_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_ongoing_desc)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(listOf(achievementChannel, ongoingLowChannel, ongoingDefaultChannel))
        }
    }

    companion object {
        const val CHANNEL_ID = "achievements_channel"
        const val CHANNEL_ONGOING_LOW = "ongoing_status_channel_low"
        const val CHANNEL_ONGOING_DEFAULT = "ongoing_status_channel_default"
    }
}
