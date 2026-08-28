package com.smokingtracker

import android.app.Application
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.repository.SmokingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AchievementsCoordinator(
    private val repository: SmokingRepository,
    private val dataStoreManager: DataStoreManager,
    private val achievementsManager: AchievementsManager,
    private val application: Application,
    private val applicationScope: CoroutineScope
) {
    private val _newAchievements = MutableSharedFlow<Achievement>(extraBufferCapacity = 10)

    val newAchievements: SharedFlow<Achievement> = _newAchievements.asSharedFlow()

    fun checkAndUpdate(updatedEntries: List<Long>? = null, wasEntryRemoved: Boolean = false) {
        applicationScope.launch(Dispatchers.Default) {
            val isReg = dataStoreManager.isRegistered.first()
            if (!isReg) return@launch

            val entries = updatedEntries
                ?: repository.smokingEntries.first().filter { !it.isResisted }.map { it.timestamp }
            val launches = dataStoreManager.appLaunchDates.first()
            val dailyLimit = dataStoreManager.dailyLimit.first()
            val hasBackup = dataStoreManager.hasMadeBackup.first()
            val hasPriceChanged = dataStoreManager.hasChangedPackPrice.first()
            val hasCancelled10s = dataStoreManager.hasCancelledWithin10s.first()
            val themeLangCount = dataStoreManager.themeLangChangeCount.first()
            val analyticsCount = dataStoreManager.analyticsVisitCount.first()

            val lastEntry = entries.maxOrNull()
            val now = System.currentTimeMillis()
            val timeWithoutSmoking = if (lastEntry != null) (now - lastEntry).coerceAtLeast(0L) else 0L

            val ctx = AchievementContext(
                timeWithoutSmoking = timeWithoutSmoking,
                entries = entries,
                launches = launches,
                dailyLimit = dailyLimit,
                hasMadeBackup = hasBackup,
                hasChangedPackPrice = hasPriceChanged,
                hasCancelledWithin10s = hasCancelled10s,
                themeLangChangesToday = themeLangCount,
                analyticsVisitsToday = analyticsCount
            )

            val previouslyUnlocked = dataStoreManager.unlockedAchievements.first()
            val newUnlockedSet = achievementsManager.calculateUnlockedAchievements(ctx)

            val noSmokeIds = achievementsManager.achievementsList
                .filter { it.category == AchievementCategory.NO_SMOKE }
                .map { it.id }.toSet()

            val preservedNonNoSmoke = previouslyUnlocked - noSmokeIds

            val effectiveUnlockedSet = if (wasEntryRemoved) {
                val preservedNoSmoke = previouslyUnlocked.intersect(noSmokeIds)
                preservedNonNoSmoke + newUnlockedSet + preservedNoSmoke
            } else {
                preservedNonNoSmoke + newUnlockedSet
            }

            val newlyUnlocked = effectiveUnlockedSet - previouslyUnlocked
            newlyUnlocked.forEach { achievementId ->
                achievementsManager.sendNotificationForAchievement(application, achievementId)
                achievementsManager.getAchievementById(achievementId)?.let { ach ->
                    _newAchievements.emit(ach)
                }
            }

            dataStoreManager.setUnlockedAchievements(effectiveUnlockedSet)
        }
    }
}
