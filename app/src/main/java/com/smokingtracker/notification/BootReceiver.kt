package com.smokingtracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.annotation.Keep
import androidx.core.content.IntentCompat
import com.smokingtracker.di.ApplicationScopeQualifier
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalDate
import java.time.ZoneId

@Keep
class BootReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val appContext = context.applicationContext ?: context

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (sentFromUid != -1 && sentFromUid != Process.SYSTEM_UID && sentFromUid != Process.myUid()) {
                return
            }
        }

        if (action == ACTION_MIDNIGHT_ROLLOVER && !isAuthorizedMidnightRollover(appContext, intent)) {
            return
        }

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ACTION_MIDNIGHT_ROLLOVER -> {
                val pendingResult = goAsync()
                val forceStatic = action != ACTION_MIDNIGHT_ROLLOVER
                get<CoroutineScope>(ApplicationScopeQualifier).launch {
                    try {
                        withTimeoutOrNull(BROADCAST_TIMEOUT_MS) {
                            get<WidgetUpdateManager>().updateAll(
                                context = appContext,
                                includeOngoingNotification = true,
                                forceStaticWidgets = forceStatic
                            )
                            scheduleMidnightRollover(appContext)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to handle broadcast action: $action", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun isAuthorizedMidnightRollover(context: Context, intent: Intent): Boolean {
        val token = IntentCompat.getParcelableExtra(intent, EXTRA_AUTH_TOKEN, PendingIntent::class.java)
            ?: return false
        return token.creatorUid == Process.myUid() && token.creatorPackage == context.packageName
    }

    companion object {
        private const val TAG = "BootReceiver"
        private const val BROADCAST_TIMEOUT_MS = 9_000L
        private const val REQUEST_CODE_MIDNIGHT = 2001
        private const val EXTRA_AUTH_TOKEN = "com.smokingtracker.EXTRA_MIDNIGHT_AUTH_TOKEN"
        const val ACTION_MIDNIGHT_ROLLOVER = "com.smokingtracker.ACTION_MIDNIGHT_ROLLOVER"

        fun scheduleMidnightRollover(context: Context) {
            try {
                val appContext = context.applicationContext ?: context
                val alarmManager = appContext.getSystemService(AlarmManager::class.java) ?: return
                val zoneId = ZoneId.systemDefault()
                val now = System.currentTimeMillis()
                val nextMidnightMillis = LocalDate.now(zoneId)
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli()
                    .coerceAtLeast(now + 60_000L)

                val authToken = PendingIntent.getBroadcast(
                    appContext,
                    0,
                    Intent().setPackage(appContext.packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
                val rolloverIntent = Intent(appContext, BootReceiver::class.java).apply {
                    action = ACTION_MIDNIGHT_ROLLOVER
                    setPackage(appContext.packageName)
                    putExtra(EXTRA_AUTH_TOKEN, authToken)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    REQUEST_CODE_MIDNIGHT,
                    rolloverIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, nextMidnightMillis, pendingIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule midnight rollover alarm", e)
            }
        }
    }
}
