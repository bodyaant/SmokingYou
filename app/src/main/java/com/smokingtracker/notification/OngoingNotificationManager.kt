package com.smokingtracker.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smokingtracker.MainActivity
import com.smokingtracker.R
import com.smokingtracker.SmokingTrackerApp
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.repository.SmokingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.Calendar

object OngoingNotificationManager : KoinComponent {

    const val NOTIFICATION_ID = 1001

    fun update(context: Context) {
        val coroutineScope: CoroutineScope = get()
        coroutineScope.launch {
            try {
                updateInternal(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun updateInternal(context: Context) {
        val dataStoreManager: DataStoreManager = get()
        val repository: SmokingRepository = get()

        val isEnabled = dataStoreManager.ongoingNotificationEnabled.first()
        val notificationManager = NotificationManagerCompat.from(context)

        if (!isEnabled) {
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

        val isLowPriority = dataStoreManager.notificationLowPriority.first()
        val showTimer = dataStoreManager.notificationShowTimer.first()
        val showProgress = dataStoreManager.notificationShowProgress.first()
        val showAddButton = dataStoreManager.notificationShowAddButton.first()
        val showResistButton = dataStoreManager.notificationShowResistButton.first()
        val dailyLimit = dataStoreManager.dailyLimit.first()

        val channelId = if (isLowPriority) {
            SmokingTrackerApp.CHANNEL_ONGOING_LOW
        } else {
            SmokingTrackerApp.CHANNEL_ONGOING_DEFAULT
        }

        val allEntities = repository.getAllEntries()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val todaySmokedEntities = allEntities.filter { !it.isResisted && it.timestamp in todayStart..todayEnd }
        val todayResistedEntities = allEntities.filter { it.isResisted && it.timestamp in todayStart..todayEnd }
        val todaySmokedCount = todaySmokedEntities.size
        val todayResistedCount = todayResistedEntities.size

        val lastSmokedEntity = allEntities.filter { !it.isResisted }.maxByOrNull { it.timestamp }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_cigarettebase)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(if (isLowPriority) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_DEFAULT)

        // Title and Chronometer
        if (showTimer && lastSmokedEntity != null) {
            builder.setUsesChronometer(true)
            builder.setWhen(lastSmokedEntity.timestamp)
            builder.setShowWhen(true)
            builder.setContentTitle(context.getString(R.string.notification_title_app_name))
        } else {
            builder.setShowWhen(false)
            builder.setContentTitle(context.getString(R.string.notification_title_app_name))
        }

        // Content / Progress summary
        val contentText = when {
            showProgress && dailyLimit > 0 -> {
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

        if (showProgress && dailyLimit > 0) {
            builder.setProgress(dailyLimit, todaySmokedCount.coerceAtMost(dailyLimit), false)
        }

        // Action 1: Add Cigarette
        if (showAddButton) {
            val addIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_ADD
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

        // Action 2: Resisted
        if (showResistButton) {
            val resistIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_RESIST
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
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
