package com.smokingtracker

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

fun interface AchievementNotifier {
    fun notifyUnlocked(achievementIds: List<String>)
}

class AndroidAchievementNotifier(
    context: Context,
    private val achievementsManager: AchievementsManager
) : AchievementNotifier {

    private val appContext: Context = context.applicationContext ?: context

    override fun notifyUnlocked(achievementIds: List<String>) {
        if (achievementIds.isEmpty()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                appContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationManager = NotificationManagerCompat.from(appContext)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        SmokingTrackerApp.ensureNotificationChannels(appContext)

        val contentPendingIntent = createContentPendingIntent()
        val achievements = achievementIds.mapNotNull { achievementsManager.getAchievementById(it) }
        if (achievements.isEmpty()) return

        val isBatch = achievements.size > 1

        try {
            for ((index, achievement) in achievements.withIndex()) {
                val title = appContext.getString(achievement.titleResId)
                val desc = appContext.getString(achievement.descResId)
                val icon = if (achievement.category == AchievementCategory.NO_SMOKE) {
                    R.drawable.ic_crosscigarette
                } else {
                    R.drawable.ic_cigarettebase
                }

                val builder = NotificationCompat.Builder(appContext, SmokingTrackerApp.CHANNEL_ID)
                    .setSmallIcon(icon)
                    .setContentTitle(appContext.getString(R.string.notification_title, title))
                    .setContentText(desc)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(true)
                    .setGroup(GROUP_KEY_ACHIEVEMENTS)

                if (isBatch) {
                    builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                    if (index > 0) {
                        builder.setSilent(true)
                    }
                }

                notificationManager.notify(achievement.id.hashCode(), builder.build())
            }

            if (isBatch) {
                val firstTitle = appContext.getString(achievements.first().titleResId)
                val summaryBuilder = NotificationCompat.Builder(appContext, SmokingTrackerApp.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_cigarettebase)
                    .setContentTitle(appContext.getString(R.string.achievement_unlocked_dialog_title))
                    .setContentText(appContext.getString(R.string.notification_title, firstTitle))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(true)
                    .setGroup(GROUP_KEY_ACHIEVEMENTS)
                    .setGroupSummary(true)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)

                notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryBuilder.build())
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException while posting achievement notification", e)
        }
    }

    private fun createContentPendingIntent(): PendingIntent {
        val launchIntent = (appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
            ?: Intent(appContext, MainActivity::class.java)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_ACHIEVEMENTS, true)
        }
        return PendingIntent.getActivity(
            appContext,
            REQUEST_CODE_OPEN_ACHIEVEMENTS,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "AchievementNotifier"
        private const val GROUP_KEY_ACHIEVEMENTS = "com.smokingtracker.ACHIEVEMENTS_GROUP"
        private const val SUMMARY_NOTIFICATION_ID = 0x4143485F // "ACH_"
        private const val REQUEST_CODE_OPEN_ACHIEVEMENTS = 2001
        const val EXTRA_OPEN_ACHIEVEMENTS = "com.smokingtracker.EXTRA_OPEN_ACHIEVEMENTS"
    }
}

fun AchievementsManager.sendNotificationForAchievement(context: Context, achievementId: String) {
    AndroidAchievementNotifier(context, this).notifyUnlocked(listOf(achievementId))
}

interface AchievementsCoordinator {
    val newAchievements: Flow<AchievementData>
    fun checkAndUpdate()
    suspend fun checkAndUpdateSuspend()
}

fun AchievementsCoordinator(
    repository: SmokingRepository,
    appMetaPreferences: AppMetaPreferences,
    userPreferences: UserPreferences,
    achievementsManager: AchievementsManager,
    achievementNotifier: AchievementNotifier,
    applicationScope: CoroutineScope,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
): AchievementsCoordinator = DefaultAchievementsCoordinator(
    repository = repository,
    appMetaPreferences = appMetaPreferences,
    userPreferences = userPreferences,
    achievementsManager = achievementsManager,
    achievementNotifier = achievementNotifier,
    applicationScope = applicationScope,
    defaultDispatcher = defaultDispatcher
)

fun AchievementsCoordinator(
    repository: SmokingRepository,
    appMetaPreferences: AppMetaPreferences,
    userPreferences: UserPreferences,
    achievementsManager: AchievementsManager,
    application: Application,
    applicationScope: CoroutineScope,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
): AchievementsCoordinator = DefaultAchievementsCoordinator(
    repository = repository,
    appMetaPreferences = appMetaPreferences,
    userPreferences = userPreferences,
    achievementsManager = achievementsManager,
    achievementNotifier = AndroidAchievementNotifier(application, achievementsManager),
    applicationScope = applicationScope,
    defaultDispatcher = defaultDispatcher
)

class DefaultAchievementsCoordinator(
    private val repository: SmokingRepository,
    private val appMetaPreferences: AppMetaPreferences,
    private val userPreferences: UserPreferences,
    private val achievementsManager: AchievementsManager,
    private val achievementNotifier: AchievementNotifier,
    private val applicationScope: CoroutineScope,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : AchievementsCoordinator {

    private val _newAchievements = Channel<AchievementData>(capacity = Channel.BUFFERED)
    private val mutex = Mutex()
    private val hasPendingCheck = AtomicBoolean(false)

    override val newAchievements: Flow<AchievementData> = _newAchievements.receiveAsFlow()

    override fun checkAndUpdate() {
        hasPendingCheck.set(true)
        applicationScope.launch(defaultDispatcher) {
            drainPendingChecks()
        }
    }

    override suspend fun checkAndUpdateSuspend() {
        hasPendingCheck.set(true)
        withContext(defaultDispatcher) {
            drainPendingChecks()
        }
    }

    private suspend fun drainPendingChecks() {
        try {
            mutex.withLock {
                while (hasPendingCheck.compareAndSet(true, false)) {
                    performCheckAndUpdate()
                }
            }
        } catch (e: CancellationException) {
            hasPendingCheck.set(true)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check and update achievements", e)
        }
    }

    private suspend fun performCheckAndUpdate() {
        val snapshot = appMetaPreferences.getAchievementMetaSnapshot()
        if (!snapshot.isRegistered) return

        val entries = repository.nonResistedTimestamps.first()
        val dailyLimit = userPreferences.dailyLimit.first()

        val sortedEntries = if (entries.isAscending()) entries else entries.sorted()
        val sortedLaunches = if (snapshot.appLaunchDates.isAscending()) {
            snapshot.appLaunchDates
        } else {
            snapshot.appLaunchDates.sorted()
        }

        val now = System.currentTimeMillis()
        val lastEntry = sortedEntries.lastOrNull()
        val timeWithoutSmoking = if (lastEntry != null) {
            (now - lastEntry).coerceAtLeast(0L)
        } else {
            val firstLaunch = sortedLaunches.firstOrNull()
            if (firstLaunch != null) (now - firstLaunch).coerceAtLeast(0L) else 0L
        }

        val ctx = AchievementContext(
            timeWithoutSmoking = timeWithoutSmoking,
            entries = sortedEntries,
            launches = sortedLaunches,
            dailyLimit = dailyLimit,
            hasMadeBackup = snapshot.hasMadeBackup,
            hasChangedPackPrice = snapshot.hasChangedPackPrice,
            hasCancelledWithin10s = snapshot.hasCancelledWithin10s,
            themeLangChangesToday = snapshot.themeLangChangeCount,
            analyticsVisitsToday = snapshot.analyticsVisitCount,
            nowMillis = now
        )

        val previouslyUnlocked = snapshot.unlockedAchievements
        val existingDates = snapshot.achievementUnlockDates
        val calculatedUnlocked = achievementsManager.calculateUnlockedAchievements(ctx)
        val effectiveUnlockedSet = previouslyUnlocked + calculatedUnlocked

        val updatedDates = existingDates.toMutableMap()
        val newlyUnlockedIds = ArrayList<String>()

        for (achievementId in effectiveUnlockedSet) {
            val isNewlyUnlocked = achievementId !in previouslyUnlocked
            val hasExistingDate = updatedDates.containsKey(achievementId)
            if (!hasExistingDate) {
                val estimated = achievementsManager.estimateHistoricalUnlockTimestamp(
                    achievementId = achievementId,
                    entries = sortedEntries,
                    launches = sortedLaunches,
                    dailyLimit = dailyLimit,
                    nowMillis = now
                ) ?: now
                updatedDates[achievementId] = estimated
            }
            if (isNewlyUnlocked && !hasExistingDate) {
                newlyUnlockedIds.add(achievementId)
            }
        }

        if (effectiveUnlockedSet != previouslyUnlocked || updatedDates != existingDates || newlyUnlockedIds.isNotEmpty()) {
            appMetaPreferences.saveAchievementsState(
                achievements = effectiveUnlockedSet,
                dates = updatedDates,
                newlyUnlockedIds = newlyUnlockedIds
            )
        }

        if (newlyUnlockedIds.isNotEmpty()) {
            achievementNotifier.notifyUnlocked(newlyUnlockedIds)
            for (achievementId in newlyUnlockedIds) {
                achievementsManager.getAchievementById(achievementId)?.let { ach ->
                    _newAchievements.send(ach)
                }
            }
        }
    }

    companion object {
        private const val TAG = "AchievementsCoordinator"
    }
}
