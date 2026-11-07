package com.smokingtracker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.TriggerItem
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingEntry
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.repository.TriggerRepository
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

sealed interface HomeFabUiAction {
    data object OpenSmokedDialog : HomeFabUiAction
    data object OpenMindfulPauseDialog : HomeFabUiAction
    data class ResistedCraving(val alreadyRecorded: Boolean = true) : HomeFabUiAction
}

data class HomeUiState(
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val isSelectedDateToday: Boolean = true,
    val selectedDateSmokedTimestamps: List<Long> = emptyList(),
    val selectedDateEntries: List<SmokingEntry> = emptyList(),
    val todaySmokedCount: Int = 0,
    val todayResistedCount: Int = 0,
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0,
    val dailyLimit: Int = 0,
    val isLimitReached: Boolean = false,
    val isLimitExceeded: Boolean = false,
    val lastSmokedTimestamp: Long? = null,
    val unlockedAchievements: Set<String> = emptySet(),
    val unlockedAchievementsCount: Int = 0,
    val totalAchievementsCount: Int = 0,
    val activeTriggers: List<TriggerItem> = emptyList(),
    val customTriggers: List<String> = emptyList(),
    val disabledDefaultTriggers: Set<String> = emptySet(),
    val showTaperingCheckIn: Boolean = false,
    val taperingIntervalDays: Int = 7
)

class HomeViewModel(
    private val repository: SmokingRepository,
    private val triggerRepository: TriggerRepository,
    private val userPreferences: UserPreferences,
    private val appMetaPreferences: AppMetaPreferences,
    private val achievementsCoordinator: AchievementsCoordinator,
    private val widgetUpdateManager: WidgetUpdateManager,
    val statisticsManager: StatisticsManager = StatisticsManager(),
    val achievementsManager: AchievementsManager = AchievementsManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val entryMutationMutex = Mutex()
    private val taperingMutex = Mutex()
    private val lastEntryActionTimestampNs = AtomicLong(0L)
    private val lastFabActionTimestampNs = AtomicLong(0L)
    internal val recentSmokedEntryCreationTimesMs = ConcurrentHashMap<Long, Long>()

    private val _fabActionEvents = Channel<HomeFabUiAction>(capacity = Channel.BUFFERED)
    val fabActionEvents: Flow<HomeFabUiAction> = _fabActionEvents.receiveAsFlow()

    private val _customTriggerAddedEvents = Channel<String?>(capacity = Channel.BUFFERED)
    val customTriggerAddedEvents: Flow<String?> = _customTriggerAddedEvents.receiveAsFlow()

    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    fun setSelectedDate(timestampMillis: Long) {
        if (timestampMillis <= 0L) return
        val maxAllowed = System.currentTimeMillis()
        _selectedDateMillis.value = timestampMillis.coerceAtMost(maxAllowed)
    }

    fun triggerFabSmoked() {
        if (!allowFabAction()) return
        _fabActionEvents.trySend(HomeFabUiAction.OpenSmokedDialog)
    }

    fun triggerFabMindfulPause() {
        if (!allowFabAction()) return
        _fabActionEvents.trySend(HomeFabUiAction.OpenMindfulPauseDialog)
    }

    fun triggerFabResisted() {
        if (!allowFabAction()) return
        val targetTimestamp = resolveTimestampForSelectedDate()
        safeLaunch(ioDispatcher) {
            val inserted = addResistedEntryInternal(trigger = null, timestamp = targetTimestamp)
            if (inserted) {
                _fabActionEvents.send(HomeFabUiAction.ResistedCraving(alreadyRecorded = true))
            }
        }
    }

    val smokingEntries: StateFlow<List<Long>> = repository.nonResistedTimestamps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSmokingEntities: StateFlow<List<SmokingEntryEntity>> = repository.smokingEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resistedEntries: StateFlow<List<SmokingEntryEntity>> = repository.resistedEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val domainEntries: StateFlow<List<SmokingEntry>> = repository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smokedDomainEntries: StateFlow<List<SmokingEntry>> = repository.smokedEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resistedDomainEntries: StateFlow<List<SmokingEntry>> = repository.resistedDomainEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyLimit: StateFlow<Int> = userPreferences.dailyLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unlockedAchievements: StateFlow<Set<String>> = appMetaPreferences.unlockedAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val customTriggers: StateFlow<List<String>> = triggerRepository.customTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val disabledDefaultTriggers: StateFlow<Set<String>> = triggerRepository.disabledDefaultTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val activeTriggers: StateFlow<List<TriggerItem>> = triggerRepository.activeTriggers
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            TriggerType.allEntries().map { TriggerItem.fromBuiltIn(it) }
        )

    private val _showTaperingCheckIn = MutableStateFlow(false)
    val showTaperingCheckIn: StateFlow<Boolean> = _showTaperingCheckIn.asStateFlow()

    val taperingIntervalDays: StateFlow<Int> = userPreferences.taperingIntervalDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            repository.entries,
            repository.nonResistedTimestamps,
            _selectedDateMillis,
            userPreferences.dailyLimit
        ) { allEntries, smokedTimestamps, selectedMillis, limit ->
            val zoneId = ZoneId.systemDefault()
            val today = LocalDate.now(zoneId)
            val selectedDay = toLocalDate(selectedMillis, zoneId).let { if (it.isAfter(today)) today else it }
            val dayStart = startOfDayMillis(selectedDay, zoneId)
            val dayEnd = startOfDayMillis(selectedDay.plusDays(1), zoneId)

            val selectedEntries = allEntries
                .filter { it.timestamp in dayStart until dayEnd }
                .sortedByDescending { it.timestamp }
            val selectedSmokedTimestamps = selectedEntries
                .asSequence()
                .filter { !it.isResisted }
                .map { it.timestamp }
                .toList()
            val selectedResistedCount = selectedEntries.count { it.isResisted }

            val weekly = statisticsManager.getWeeklyCount(smokedTimestamps, selectedDay, zoneId)
            val monthly = statisticsManager.getMonthlyCount(smokedTimestamps, selectedDay, zoneId)
            val lastSmoked = smokedTimestamps.maxOrNull()

            HomeDataSnapshot(
                selectedDateMillis = selectedMillis,
                isSelectedDateToday = selectedDay == today,
                selectedDateSmokedTimestamps = selectedSmokedTimestamps,
                selectedDateEntries = selectedEntries,
                todaySmokedCount = selectedSmokedTimestamps.size,
                todayResistedCount = selectedResistedCount,
                weeklyCount = weekly,
                monthlyCount = monthly,
                dailyLimit = limit,
                lastSmokedTimestamp = lastSmoked
            )
        },
        combine(
            appMetaPreferences.unlockedAchievements,
            triggerRepository.activeTriggers,
            triggerRepository.customTriggers,
            triggerRepository.disabledDefaultTriggers
        ) { unlocked, activeTrig, customTrig, disabledTrig ->
            HomeMetaTriggerSnapshot(
                unlockedAchievements = unlocked,
                activeTriggers = activeTrig,
                customTriggers = customTrig,
                disabledDefaultTriggers = disabledTrig
            )
        },
        _showTaperingCheckIn,
        userPreferences.taperingIntervalDays
    ) { dataSnap, metaSnap, showTapering, taperingDays ->
        val allAchIds = achievementsManager.achievementsList.map { it.id }.toSet()
        val validUnlockedCount = metaSnap.unlockedAchievements.count { it in allAchIds }
        HomeUiState(
            selectedDateMillis = dataSnap.selectedDateMillis,
            isSelectedDateToday = dataSnap.isSelectedDateToday,
            selectedDateSmokedTimestamps = dataSnap.selectedDateSmokedTimestamps,
            selectedDateEntries = dataSnap.selectedDateEntries,
            todaySmokedCount = dataSnap.todaySmokedCount,
            todayResistedCount = dataSnap.todayResistedCount,
            weeklyCount = dataSnap.weeklyCount,
            monthlyCount = dataSnap.monthlyCount,
            dailyLimit = dataSnap.dailyLimit,
            isLimitReached = dataSnap.dailyLimit > 0 && dataSnap.todaySmokedCount >= dataSnap.dailyLimit,
            isLimitExceeded = dataSnap.dailyLimit > 0 && dataSnap.todaySmokedCount > dataSnap.dailyLimit,
            lastSmokedTimestamp = dataSnap.lastSmokedTimestamp,
            unlockedAchievements = metaSnap.unlockedAchievements,
            unlockedAchievementsCount = validUnlockedCount,
            totalAchievementsCount = achievementsManager.achievementsList.size,
            activeTriggers = metaSnap.activeTriggers,
            customTriggers = metaSnap.customTriggers,
            disabledDefaultTriggers = metaSnap.disabledDefaultTriggers,
            showTaperingCheckIn = showTapering,
            taperingIntervalDays = taperingDays
        )
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(
                totalAchievementsCount = achievementsManager.achievementsList.size,
                activeTriggers = TriggerType.allEntries().map { TriggerItem.fromBuiltIn(it) }
            )
        )

    suspend fun addCustomTriggerSuspend(name: String): String? {
        val added = triggerRepository.addCustomTrigger(name)
        _customTriggerAddedEvents.trySend(added)
        return added
    }

    fun addCustomTrigger(name: String, onResult: ((String?) -> Unit)? = null) {
        safeLaunch(ioDispatcher) {
            val added = addCustomTriggerSuspend(name)
            if (onResult != null) {
                withContext(mainDispatcher) {
                    onResult(added)
                }
            }
        }
    }

    fun removeCustomTrigger(name: String) {
        safeLaunch(ioDispatcher) {
            triggerRepository.removeCustomTrigger(name)
        }
    }

    fun toggleDefaultTrigger(key: String, isEnabled: Boolean) {
        safeLaunch(ioDispatcher) {
            triggerRepository.toggleDefaultTrigger(key, isEnabled)
        }
    }

    init {
        checkTaperingPlanEligibility()
    }

    fun addSmokingEntry(timestamp: Long = System.currentTimeMillis()) {
        addSmokingEntryWithTrigger(timestamp, null)
    }

    fun addSmokingEntryWithTrigger(timestamp: Long = System.currentTimeMillis(), trigger: String?) {
        if (!allowEntryAddAction()) return
        val validTimestamp = sanitizeTimestamp(timestamp) ?: return
        val cleanTrigger = sanitizeTrigger(trigger)

        safeLaunch(ioDispatcher) {
            entryMutationMutex.withLock {
                val insertedId = repository.addEntry(validTimestamp, cleanTrigger)
                if (insertedId <= 0L) return@withLock

                recordSmokedEntryCreationTime(insertedId)
                achievementsCoordinator.checkAndUpdateSuspend()
                widgetUpdateManager.updateAllAsync()
            }
        }
    }

    fun addResistedEntry(trigger: String?, timestamp: Long = System.currentTimeMillis()) {
        if (!allowEntryAddAction()) return
        safeLaunch(ioDispatcher) {
            addResistedEntryInternal(trigger = trigger, timestamp = timestamp)
        }
    }

    private suspend fun addResistedEntryInternal(trigger: String?, timestamp: Long): Boolean {
        val validTimestamp = sanitizeTimestamp(timestamp) ?: return false
        val cleanTrigger = sanitizeTrigger(trigger)

        return entryMutationMutex.withLock {
            val insertedId = repository.addResistedEntry(validTimestamp, cleanTrigger)
            if (insertedId <= 0L) return@withLock false

            achievementsCoordinator.checkAndUpdateSuspend()
            widgetUpdateManager.updateAllAsync()
            true
        }
    }

    fun removeSmokingEntry(id: Long, timestamp: Long, isResisted: Boolean = false) {
        if (id <= 0L) return
        safeLaunch(ioDispatcher) {
            entryMutationMutex.withLock {
                val now = System.currentTimeMillis()
                val cancelledWithin10s = determineHesitantCancellation(
                    id = id,
                    timestamp = timestamp,
                    isResisted = isResisted,
                    recentSmokedEntryCreationTimesMs = recentSmokedEntryCreationTimesMs,
                    now = now
                )

                val removed = repository.removeEntryById(id)
                if (!removed) return@withLock

                if (cancelledWithin10s) {
                    appMetaPreferences.setHasCancelledWithin10s(true)
                }
                achievementsCoordinator.checkAndUpdateSuspend()
                widgetUpdateManager.updateAllAsync()
            }
        }
    }

    fun editSmokingEntry(id: Long, oldTimestamp: Long, newTimestamp: Long) {
        if (id <= 0L) return
        val validNewTimestamp = sanitizeTimestamp(newTimestamp) ?: return
        if (validNewTimestamp == oldTimestamp) return

        safeLaunch(ioDispatcher) {
            entryMutationMutex.withLock {
                val updated = repository.updateEntryTimestampById(id, validNewTimestamp)
                if (!updated) return@withLock

                achievementsCoordinator.checkAndUpdateSuspend()
                widgetUpdateManager.updateAllAsync()
            }
        }
    }

    fun updateSmokingEntryTrigger(id: Long, trigger: String?) {
        if (id <= 0L) return
        val cleanTrigger = sanitizeTrigger(trigger)

        safeLaunch(ioDispatcher) {
            entryMutationMutex.withLock {
                val updated = repository.updateEntryTriggerById(id, cleanTrigger)
                if (!updated) return@withLock

                achievementsCoordinator.checkAndUpdateSuspend()
                widgetUpdateManager.updateAllAsync()
            }
        }
    }

    fun refreshState() {
        safeLaunch(ioDispatcher) {
            achievementsCoordinator.checkAndUpdateSuspend()
            widgetUpdateManager.updateAllAsync()
        }
    }

    fun checkTaperingPlanEligibility() {
        safeLaunch(ioDispatcher) {
            taperingMutex.withLock {
                val enabled = userPreferences.taperingPlanEnabled.first()
                if (!enabled) {
                    _showTaperingCheckIn.value = false
                    return@withLock
                }

                val limit = userPreferences.dailyLimit.first()
                if (limit <= MIN_TAPERING_DAILY_LIMIT) {
                    _showTaperingCheckIn.value = false
                    return@withLock
                }

                val intervalDays = userPreferences.taperingIntervalDays.first().coerceAtLeast(1)
                val lastCheckin = userPreferences.lastTaperingCheckinDate.first()
                val now = System.currentTimeMillis()

                if (lastCheckin <= 0L || lastCheckin > now) {
                    userPreferences.updateLastTaperingCheckinDate(now)
                    _showTaperingCheckIn.value = false
                    return@withLock
                }

                val daysPassed = daysBetween(toLocalDate(lastCheckin), toLocalDate(now))
                if (daysPassed >= intervalDays.toLong()) {
                    _showTaperingCheckIn.value = true
                }
            }
        }
    }

    fun dismissTaperingCheckIn() {
        _showTaperingCheckIn.value = false
    }

    fun acceptTaperingReduction() {
        safeLaunch(ioDispatcher) {
            taperingMutex.withLock {
                val currentLimit = userPreferences.dailyLimit.first()
                if (currentLimit > MIN_TAPERING_DAILY_LIMIT) {
                    val newLimit = (currentLimit - 1).coerceAtLeast(MIN_TAPERING_DAILY_LIMIT)
                    userPreferences.setDailyLimit(newLimit)
                }
                userPreferences.updateLastTaperingCheckinDate(System.currentTimeMillis())
                _showTaperingCheckIn.value = false
                achievementsCoordinator.checkAndUpdateSuspend()
                widgetUpdateManager.updateAllAsync()
            }
        }
    }

    fun keepTaperingLimit() {
        safeLaunch(ioDispatcher) {
            taperingMutex.withLock {
                userPreferences.updateLastTaperingCheckinDate(System.currentTimeMillis())
                _showTaperingCheckIn.value = false
            }
        }
    }

    fun snoozeTaperingCheckIn() {
        safeLaunch(ioDispatcher) {
            taperingMutex.withLock {
                val now = System.currentTimeMillis()
                val safeIntervalDays = userPreferences.taperingIntervalDays.first().coerceAtLeast(1)
                val intervalMs = TimeUnit.DAYS.toMillis(safeIntervalDays.toLong())
                val snoozeMs = TimeUnit.DAYS.toMillis(SNOOZE_DAYS)
                val effectiveLastCheckin = (now - (intervalMs - snoozeMs)).coerceIn(1L, now)
                userPreferences.updateLastTaperingCheckinDate(effectiveLastCheckin)
                _showTaperingCheckIn.value = false
            }
        }
    }

    fun resolveTimestampForSelectedDate(nowMillis: Long = System.currentTimeMillis()): Long {
        val zoneId = ZoneId.systemDefault()
        val selectedDay = toLocalDate(_selectedDateMillis.value, zoneId)
        val today = toLocalDate(nowMillis, zoneId)
        if (!selectedDay.isBefore(today)) {
            return nowMillis
        }
        val currentLocalTime = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalTime()
        val candidateMillis = selectedDay.atTime(currentLocalTime).atZone(zoneId).toInstant().toEpochMilli()
        return candidateMillis.coerceIn(1L, nowMillis)
    }

    private fun sanitizeTimestamp(timestamp: Long, nowMillis: Long = System.currentTimeMillis()): Long? {
        if (timestamp <= 0L) return null
        if (timestamp > nowMillis + FUTURE_CLOCK_SKEW_TOLERANCE_MS) return null
        return timestamp.coerceAtMost(nowMillis)
    }

    private fun sanitizeTrigger(trigger: String?): String? {
        val normalized = trigger?.let(TriggerItem::normalizeCustomName)?.takeIf { it.isNotEmpty() }
            ?: return null
        return TriggerType.fromKey(normalized)?.key ?: normalized
    }

    private fun recordSmokedEntryCreationTime(entryId: Long) {
        val now = System.currentTimeMillis()
        recentSmokedEntryCreationTimesMs[entryId] = now
        if (recentSmokedEntryCreationTimesMs.size > 64) {
            val cutoff = now - CREATION_TRACKING_TTL_MS
            val iterator = recentSmokedEntryCreationTimesMs.entries.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().value < cutoff) {
                    iterator.remove()
                }
            }
        }
    }

    private fun allowEntryAddAction(): Boolean {
        val nowNs = System.nanoTime()
        val prevNs = lastEntryActionTimestampNs.get()
        if (prevNs != 0L && (nowNs - prevNs) in 0L until ENTRY_ACTION_DEBOUNCE_NS) {
            return false
        }
        return lastEntryActionTimestampNs.compareAndSet(prevNs, nowNs)
    }

    private fun allowFabAction(): Boolean {
        val nowNs = System.nanoTime()
        val prevNs = lastFabActionTimestampNs.get()
        if (prevNs != 0L && (nowNs - prevNs) in 0L until FAB_ACTION_DEBOUNCE_NS) {
            return false
        }
        return lastFabActionTimestampNs.compareAndSet(prevNs, nowNs)
    }

    private fun safeLaunch(
        dispatcher: CoroutineDispatcher = ioDispatcher,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error in HomeViewModel coroutine", e)
            }
        }
    }

    private data class HomeDataSnapshot(
        val selectedDateMillis: Long,
        val isSelectedDateToday: Boolean,
        val selectedDateSmokedTimestamps: List<Long>,
        val selectedDateEntries: List<SmokingEntry>,
        val todaySmokedCount: Int,
        val todayResistedCount: Int,
        val weeklyCount: Int,
        val monthlyCount: Int,
        val dailyLimit: Int,
        val lastSmokedTimestamp: Long?
    )

    private data class HomeMetaTriggerSnapshot(
        val unlockedAchievements: Set<String>,
        val activeTriggers: List<TriggerItem>,
        val customTriggers: List<String>,
        val disabledDefaultTriggers: Set<String>
    )

    companion object {
        private const val TAG = "HomeViewModel"
        private const val ENTRY_ACTION_DEBOUNCE_NS = 350_000_000L // 350ms
        private const val FAB_ACTION_DEBOUNCE_NS = 300_000_000L // 300ms
        const val CANCEL_WINDOW_MS = 10_000L
        private const val CREATION_TRACKING_TTL_MS = 60_000L
        private const val FUTURE_CLOCK_SKEW_TOLERANCE_MS = 60_000L
        private const val MIN_TAPERING_DAILY_LIMIT = 1
        private const val SNOOZE_DAYS = 3L

        internal fun determineHesitantCancellation(
            id: Long,
            timestamp: Long,
            isResisted: Boolean,
            recentSmokedEntryCreationTimesMs: MutableMap<Long, Long>,
            now: Long = System.currentTimeMillis()
        ): Boolean {
            if (isResisted) {
                recentSmokedEntryCreationTimesMs.remove(id)
                return false
            }
            val createdAt = recentSmokedEntryCreationTimesMs.remove(id)
            return if (createdAt != null) {
                (now - createdAt) in 0L..CANCEL_WINDOW_MS
            } else {
                timestamp > 0L && (now - timestamp) in 0L..CANCEL_WINDOW_MS
            }
        }
    }
}
