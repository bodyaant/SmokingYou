package com.smokingtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.NotificationPreferences
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.di.ApplicationScopeQualifier
import com.smokingtracker.di.appModule
import com.smokingtracker.notification.OngoingNotificationManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SmokingTrackerApp : Application() {

    private val repository: SmokingRepository by inject()
    private val appMetaPreferences: AppMetaPreferences by inject()
    private val notificationPreferences: NotificationPreferences by inject()
    private val ongoingNotificationManager: OngoingNotificationManager by inject()
    private val applicationScope: CoroutineScope by inject(ApplicationScopeQualifier)

    override fun onCreate() {
        super.onCreate()
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
                androidContext(this@SmokingTrackerApp)
                modules(appModule)
            }
        }

        applicationScope.launch {
            try {
                ensureNotificationChannels(this@SmokingTrackerApp)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize notification channels", e)
            }

            try {
                if (appMetaPreferences.hasOldData.first()) {
                    val (oldEntries, oldTriggers) = appMetaPreferences.getOldEntries()
                    if (oldEntries.isNotEmpty()) {
                        val entities = oldEntries.map { ts ->
                            SmokingEntryEntity(timestamp = ts, trigger = oldTriggers[ts], isResisted = false)
                        }
                        repository.clearAndInsertEntries(entities)
                    }
                    appMetaPreferences.clearOldEntries()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to migrate legacy smoking entries from DataStore to Room", e)
            }

            try {
                if (notificationPreferences.ongoingNotificationEnabled.first()) {
                    ongoingNotificationManager.updateSuspend(this@SmokingTrackerApp)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update ongoing notification on startup", e)
            }
        }
    }

    companion object {
        private const val TAG = "SmokingTrackerApp"
        const val CHANNEL_ID = "achievements_channel"
        const val CHANNEL_ONGOING_LOW = "ongoing_status_channel_low"
        const val CHANNEL_ONGOING_DEFAULT = "ongoing_status_channel_default"

        @Volatile
        private var channelsCreated = false

        fun ensureNotificationChannels(context: Context) {
            if (channelsCreated) return
            synchronized(this) {
                if (channelsCreated) return
                val appContext = context.applicationContext ?: context
                val notificationManager = appContext.getSystemService(NotificationManager::class.java) ?: return

                val achievementChannel = NotificationChannel(
                    CHANNEL_ID,
                    appContext.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = appContext.getString(R.string.notification_channel_desc)
                }

                val ongoingLowChannel = NotificationChannel(
                    CHANNEL_ONGOING_LOW,
                    appContext.getString(R.string.notification_channel_ongoing_low_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = appContext.getString(R.string.notification_channel_ongoing_desc)
                    setShowBadge(false)
                }

                val ongoingDefaultChannel = NotificationChannel(
                    CHANNEL_ONGOING_DEFAULT,
                    appContext.getString(R.string.notification_channel_ongoing_default_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = appContext.getString(R.string.notification_channel_ongoing_desc)
                    setShowBadge(false)
                    setSound(null, null)
                    enableVibration(false)
                }

                notificationManager.createNotificationChannels(
                    listOf(achievementChannel, ongoingLowChannel, ongoingDefaultChannel)
                )
                channelsCreated = true
            }
        }
    }
}
