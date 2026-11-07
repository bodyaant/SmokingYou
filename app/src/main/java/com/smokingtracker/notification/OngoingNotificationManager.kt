package com.smokingtracker.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smokingtracker.MainActivity
import com.smokingtracker.R
import com.smokingtracker.SmokingTrackerApp
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.NotificationPreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingRepository
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
import org.koin.core.context.GlobalContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicBoolean

class OngoingNotificationManager(
    context: Context,
    private val notificationPreferences: NotificationPreferences,
    private val userPreferences: UserPreferences,
    private val repository: SmokingRepository,
    private val applicationScope: CoroutineScope,
    private val appMetaPreferences: AppMetaPreferences? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val appContext: Context = context.applicationContext ?: context
    private val mutex = Mutex()
    private val hasPendingUpdate = AtomicBoolean(false)

    fun update(context: Context = appContext) {
        val targetContext = context.applicationContext ?: context
        hasPendingUpdate.set(true)
        applicationScope.launch(ioDispatcher) {
            drainPendingUpdates(targetContext)
        }
    }

    suspend fun updateSuspend(context: Context = appContext): Unit = withContext(ioDispatcher) {
        val targetContext = context.applicationContext ?: context
        hasPendingUpdate.set(true)
        drainPendingUpdates(targetContext)
    }

    private suspend fun drainPendingUpdates(targetContext: Context) {
        try {
            mutex.withLock {
                while (hasPendingUpdate.compareAndSet(true, false)) {
                    updateInternal(targetContext)
                }
            }
        } catch (e: CancellationException) {
            hasPendingUpdate.set(true)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update ongoing notification", e)
        }
    }

    private suspend fun updateInternal(context: Context) {
        val settings = notificationPreferences.getSnapshot()
        val notificationManager = NotificationManagerCompat.from(context)

        if (!settings.enabled) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        SmokingTrackerApp.ensureNotificationChannels(context)

        val dailyLimit = userPreferences.dailyLimit.first()

        val channelId = if (settings.lowPriority) {
            SmokingTrackerApp.CHANNEL_ONGOING_LOW
        } else {
            SmokingTrackerApp.CHANNEL_ONGOING_DEFAULT
        }

        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val todayStart = startOfDayMillis(today, zoneId)
        val todayEnd = (startOfDayMillis(today.plusDays(1), zoneId) - 1L).coerceAtLeast(todayStart)

        val todaySmokedCount = repository.getEntryCountInRange(todayStart, todayEnd, isResisted = false)
        val todayResistedCount = repository.getEntryCountInRange(todayStart, todayEnd, isResisted = true)
        val lastSmokedEntity = repository.getLastSmokedEntry()

        val now = System.currentTimeMillis()
        val timerReferenceTimestamp = if (settings.showTimer) {
            lastSmokedEntity?.timestamp?.takeIf { it > 0L }
                ?: repository.getLastEntry()?.timestamp?.takeIf { it in 1..now }
                ?: appMetaPreferences?.appLaunchDates?.first()?.minOrNull()?.takeIf { it in 1..now }
                ?: userPreferences.historicalStartDate.first().takeIf { it in 1..now }
        } else {
            null
        }

        val launchIntent = (context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, MainActivity::class.java)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_cigarettebase)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(if (settings.lowPriority) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_DEFAULT)

        if (settings.showTimer && timerReferenceTimestamp != null) {
            builder.setUsesChronometer(true)
            builder.setWhen(timerReferenceTimestamp.coerceAtMost(now))
            builder.setShowWhen(true)
            builder.setContentTitle(context.getString(R.string.notification_title_app_name))
        } else {
            builder.setUsesChronometer(false)
            builder.setShowWhen(false)
            builder.setContentTitle(context.getString(R.string.notification_title_app_name))
        }

        val contentText = when {
            settings.showProgress && dailyLimit > 0 -> {
                val percent = (todaySmokedCount.toFloat() / dailyLimit.toFloat() * 100).toInt()
                context.getString(R.string.notification_content_with_limit, todaySmokedCount, dailyLimit, percent)
            }
            else -> {
                context.getString(R.string.notification_content_no_limit, todaySmokedCount)
            }
        }

        val fullContentText = if (todayResistedCount > 0) {
            contentText + context.getString(R.string.notification_content_resisted_count, todayResistedCount)
        } else {
            contentText
        }

        builder.setContentText(fullContentText)

        if (settings.showProgress && dailyLimit > 0) {
            builder.setProgress(dailyLimit, todaySmokedCount.coerceAtMost(dailyLimit), false)
        }

        if (settings.showAddButton) {
            val addIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_ADD
                setPackage(context.packageName)
            }
            val addPendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                R.drawable.ic_cigarettebase,
                context.getString(R.string.notification_action_add),
                addPendingIntent
            )
        }

        if (settings.showResistButton) {
            val resistIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_RESIST
                setPackage(context.packageName)
            }
            val resistPendingIntent = PendingIntent.getBroadcast(
                context,
                102,
                resistIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                R.drawable.ic_crosscigarette,
                context.getString(R.string.notification_action_resist),
                resistPendingIntent
            )
        }

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
            BootReceiver.scheduleMidnightRollover(context)
        } catch (e: SecurityException) {
            Log.w(TAG, "Missing permission to post ongoing notification", e)
        }
    }

    companion object {
        private const val TAG = "OngoingNotificationMgr"
        const val NOTIFICATION_ID = 1001

        fun update(context: Context) {
            GlobalContext.getOrNull()?.get<OngoingNotificationManager>()?.update(context)
        }

        suspend fun updateSuspend(context: Context) {
            GlobalContext.getOrNull()?.get<OngoingNotificationManager>()?.updateSuspend(context)
        }
    }
}
