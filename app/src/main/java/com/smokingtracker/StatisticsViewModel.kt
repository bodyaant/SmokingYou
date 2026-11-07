package com.smokingtracker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.NotificationPreferences
import com.smokingtracker.data.preferences.ThemePreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingEntry
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.repository.TriggerRepository
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicLong

data class StatisticsUiState(
    val entries: List<Long> = emptyList(),
    val nonResistedEntries: List<SmokingEntry> = emptyList(),
    val resistedEntries: List<SmokingEntry> = emptyList(),
    val resistedTimestamps: List<Long> = emptyList(),
    val resistedCount: Int = 0,
    val firstAppLaunchDate: Long? = null,
    val dailyLimit: Int = 0,
    val vibrationEnabled: Boolean = true,
    val packPrice: Float = 0.0f,
    val packSize: Int = UserPreferences.DEFAULT_PACK_SIZE,
    val currency: String = UserPreferences.DEFAULT_CURRENCY,
    val savingsGoalTitle: String = "",
    val savingsGoalAmount: Float = 0f,
    val hasHistoricalBaseline: Boolean = false,
    val historicalStartDate: Long = 0L,
    val historicalDailyAvg: Int = 0,
    val historicalPackPrice: Float = 0f,
    val historicalPackSize: Int = UserPreferences.DEFAULT_PACK_SIZE,
    val historicalTriggerPriorities: List<String> = emptyList(),
    val stats: StatisticsData = StatisticsData.EMPTY,
    val savingsData: StatisticsManager.SavingsData = StatisticsManager.SavingsData(),
    val baselineAnalytics: StatisticsManager.SmokingBaselineAnalytics? = null,
    val smokingIntervals: StatisticsManager.SmokingIntervalData = StatisticsManager.SmokingIntervalData.EMPTY,
    val hourlyDistribution: StatisticsManager.HourlyDistributionData = StatisticsManager.HourlyDistributionData.EMPTY
)

data class GraphUiState(
    val entries: List<Long> = emptyList(),
    val triggerEntries: List<SmokingEntry> = emptyList(),
    val resistedCount: Int = 0,
    val resistedTimestamps: List<Long> = emptyList(),
    val trackingStartMillis: Long? = null,
    val dailyLimit: Int = 0,
    val showLimitOnGraph: Boolean = true,
    val packPrice: Float = 0.0f,
    val packSize: Int = UserPreferences.DEFAULT_PACK_SIZE,
    val currency: String = UserPreferences.DEFAULT_CURRENCY,
    val vibrationEnabled: Boolean = true,
    val savingsGoalTitle: String = "",
    val savingsGoalAmount: Float = 0f,
    val hasHistoricalBaseline: Boolean = false,
    val historicalStartDate: Long = 0L,
    val historicalDailyAvg: Int = 0,
    val customTriggers: List<String> = emptyList(),
    val disabledDefaultTriggers: Set<String> = emptySet(),
    val dailyDateMillis: Long = System.currentTimeMillis(),
    val weeklyDateMillis: Long = System.currentTimeMillis(),
    val monthlyDateMillis: Long = System.currentTimeMillis(),
    val yearlyDateMillis: Long = System.currentTimeMillis(),
    val dailyData: List<Int> = StatisticsManager.EMPTY_DAILY_DATA,
    val weeklyData: List<Int> = StatisticsManager.EMPTY_WEEKLY_DATA,
    val monthlyData: List<Int> = StatisticsManager.EMPTY_MONTHLY_CHUNKS,
    val monthlyDailyData: List<Int> = List(30) { 0 },
    val yearlyData: List<Int> = StatisticsManager.EMPTY_YEARLY_DATA,
    val weeklyComparison: StatisticsManager.WeeklyComparisonData = StatisticsManager.WeeklyComparisonData.EMPTY,
    val hourlyDistribution: StatisticsManager.HourlyDistributionData = StatisticsManager.HourlyDistributionData.EMPTY,
    val smokingIntervals: StatisticsManager.SmokingIntervalData = StatisticsManager.SmokingIntervalData.EMPTY,
    val stats: StatisticsData = StatisticsData.EMPTY,
    val savingsData: StatisticsManager.SavingsData = StatisticsManager.SavingsData(),
    val baselineAnalytics: StatisticsManager.SmokingBaselineAnalytics? = null
)

class StatisticsViewModel(
    private val repository: SmokingRepository,
    private val triggerRepository: TriggerRepository,
    private val userPreferences: UserPreferences,
    private val themePreferences: ThemePreferences,
    private val notificationPreferences: NotificationPreferences,
    private val appMetaPreferences: AppMetaPreferences,
    private val achievementsCoordinator: AchievementsCoordinator,
    val statisticsManager: StatisticsManager = StatisticsManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val analyticsVisitMutex = Mutex()
    private val baselineMutex = Mutex()
    private val lastAnalyticsVisitTimestampNs = AtomicLong(0L)

    private val _graphScrollTarget = MutableStateFlow<String?>(null)
    val graphScrollTarget: StateFlow<String?> = _graphScrollTarget.asStateFlow()

    private val _graphScrollEvents = Channel<String>(capacity = Channel.BUFFERED)
    val graphScrollEvents: Flow<String> = _graphScrollEvents.receiveAsFlow()

    private val _customTriggerAddedEvents = Channel<String?>(capacity = Channel.BUFFERED)
    val customTriggerAddedEvents: Flow<String?> = _customTriggerAddedEvents.receiveAsFlow()

    fun setGraphScrollTarget(target: String?) {
        val clean = target?.trim()?.takeIf { it.isNotEmpty() }
        _graphScrollTarget.value = clean
        if (clean != null) {
            _graphScrollEvents.trySend(clean)
        }
    }

    fun clearGraphScrollTarget() {
        _graphScrollTarget.value = null
    }

    private val _dailyDateMillis = MutableStateFlow(System.currentTimeMillis())
    val dailyDateMillis: StateFlow<Long> = _dailyDateMillis.asStateFlow()

    private val _weeklyDateMillis = MutableStateFlow(System.currentTimeMillis())
    val weeklyDateMillis: StateFlow<Long> = _weeklyDateMillis.asStateFlow()

    private val _monthlyDateMillis = MutableStateFlow(System.currentTimeMillis())
    val monthlyDateMillis: StateFlow<Long> = _monthlyDateMillis.asStateFlow()

    private val _yearlyDateMillis = MutableStateFlow(System.currentTimeMillis())
    val yearlyDateMillis: StateFlow<Long> = _yearlyDateMillis.asStateFlow()

    fun setDailyDate(timestampMillis: Long) {
        if (timestampMillis > 0L) _dailyDateMillis.value = timestampMillis
    }

    fun setWeeklyDate(timestampMillis: Long) {
        if (timestampMillis > 0L) _weeklyDateMillis.value = timestampMillis
    }

    fun setMonthlyDate(timestampMillis: Long) {
        if (timestampMillis > 0L) _monthlyDateMillis.value = timestampMillis
    }

    fun setYearlyDate(timestampMillis: Long) {
        if (timestampMillis > 0L) _yearlyDateMillis.value = timestampMillis
    }

    val smokingEntries: StateFlow<List<Long>> = repository.nonResistedTimestamps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val nonResistedEntities: StateFlow<List<SmokingEntryEntity>> = repository.nonResistedEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val resistedEntries: StateFlow<List<SmokingEntryEntity>> = repository.resistedEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val nonResistedDomainEntries: StateFlow<List<SmokingEntry>> = repository.smokedEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val resistedDomainEntries: StateFlow<List<SmokingEntry>> = repository.resistedDomainEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val resistedTimestampsFlow: Flow<List<Long>> = repository.resistedEntries
        .map { list -> list.map { it.timestamp } }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    val resistedTimestamps: StateFlow<List<Long>> = resistedTimestampsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val firstAppLaunchDateFlow: Flow<Long?> = appMetaPreferences.appLaunchDates
        .map { it.minOrNull() }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    val firstAppLaunchDate: StateFlow<Long?> = firstAppLaunchDateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val dailyLimit: StateFlow<Int> = userPreferences.dailyLimit.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val vibrationEnabled: StateFlow<Boolean> = themePreferences.vibrationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val packPrice: StateFlow<Float> = userPreferences.packPrice.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0f
    )

    val packSize: StateFlow<Int> = userPreferences.packSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences.DEFAULT_PACK_SIZE
    )

    val currency: StateFlow<String> = userPreferences.currency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences.DEFAULT_CURRENCY
    )

    val savingsGoalTitle: StateFlow<String> = userPreferences.savingsGoalTitle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val savingsGoalAmount: StateFlow<Float> = userPreferences.savingsGoalAmount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    val hasHistoricalBaseline: StateFlow<Boolean> = userPreferences.hasHistoricalBaseline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val historicalStartDate: StateFlow<Long> = userPreferences.historicalStartDate.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val historicalDailyAvg: StateFlow<Int> = userPreferences.historicalDailyAvg.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val historicalPackPrice: StateFlow<Float> = userPreferences.historicalPackPrice.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    val historicalPackSize: StateFlow<Int> = userPreferences.historicalPackSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences.DEFAULT_PACK_SIZE
    )

    val historicalTriggerPriorities: StateFlow<List<String>> = userPreferences.historicalTriggerPriorities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customTriggers: StateFlow<List<String>> = triggerRepository.customTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val disabledDefaultTriggers: StateFlow<Set<String>> = triggerRepository.disabledDefaultTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val showLimitOnGraph: StateFlow<Boolean> = userPreferences.showLimitOnGraph.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val stats: StateFlow<StatisticsData> = combine(
        repository.nonResistedTimestamps,
        firstAppLaunchDateFlow
    ) { entries, trackingStart ->
        statisticsManager.calculateStats(
            entries = entries,
            trackingStartMillis = trackingStart
        )
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsData.EMPTY
        )

    val savingsData: StateFlow<StatisticsManager.SavingsData> = combine(
        repository.nonResistedTimestamps,
        resistedTimestampsFlow,
        userPreferences.dailyLimit,
        userPreferences.packPrice,
        combine(userPreferences.packSize, firstAppLaunchDateFlow) { size, start -> size to start }
    ) { entries, resistedTs, limit, price, (size, trackingStart) ->
        statisticsManager.calculateSavings(
            entries = entries,
            resistedCount = resistedTs.size,
            dailyLimit = limit,
            packPrice = price,
            packSize = size,
            trackingStartMillis = trackingStart
        )
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsManager.SavingsData()
        )

    val baselineAnalytics: StateFlow<StatisticsManager.SmokingBaselineAnalytics?> = combine(
        combine(
            userPreferences.hasHistoricalBaseline,
            userPreferences.historicalStartDate,
            userPreferences.historicalDailyAvg,
            userPreferences.packPrice,
            userPreferences.packSize
        ) { hasBaseline, startDate, dailyAvg, price, size ->
            BaselinePrefsPart(hasBaseline, startDate, dailyAvg, price, size)
        },
        repository.nonResistedTimestamps,
        resistedTimestampsFlow,
        firstAppLaunchDateFlow
    ) { prefs, entries, resistedTs, trackingStart ->
        if (prefs.hasBaseline) {
            statisticsManager.calculateBaselineAnalytics(
                startDate = prefs.startDate,
                dailyAvg = prefs.dailyAvg,
                packPrice = prefs.packPrice,
                packSize = prefs.packSize,
                entries = entries,
                resistedEntries = resistedTs,
                trackingStartMillis = trackingStart
            )
        } else {
            null
        }
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val smokingIntervals: StateFlow<StatisticsManager.SmokingIntervalData> = repository.nonResistedTimestamps
        .map { entries -> statisticsManager.calculateSmokingIntervals(entries) }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsManager.SmokingIntervalData.EMPTY
        )

    val hourlyDistribution: StateFlow<StatisticsManager.HourlyDistributionData> = repository.nonResistedTimestamps
        .map { entries -> statisticsManager.calculateHourlyDistribution(entries) }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsManager.HourlyDistributionData.EMPTY
        )

    val statisticsUiState: StateFlow<StatisticsUiState> = combine(
        combine(
            repository.nonResistedTimestamps,
            repository.smokedEntries,
            repository.resistedDomainEntries,
            resistedTimestampsFlow,
            firstAppLaunchDateFlow
        ) { entries, smokedDom, resistedDom, resistedTs, launchDate ->
            StatsDataPart(entries, smokedDom, resistedDom, resistedTs, launchDate)
        },
        combine(
            userPreferences.dailyLimit,
            themePreferences.vibrationEnabled,
            userPreferences.packPrice,
            userPreferences.packSize,
            userPreferences.currency
        ) { limit, vibration, price, size, curr ->
            StatsPackPrefsPart(limit, vibration, price, size, curr)
        },
        combine(
            userPreferences.savingsGoalTitle,
            userPreferences.savingsGoalAmount,
            userPreferences.hasHistoricalBaseline,
            userPreferences.historicalStartDate,
            userPreferences.historicalDailyAvg
        ) { goalTitle, goalAmt, hasBase, baseStart, baseAvg ->
            StatsGoalBaselinePart(goalTitle, goalAmt, hasBase, baseStart, baseAvg)
        },
        combine(
            userPreferences.historicalPackPrice,
            userPreferences.historicalPackSize,
            userPreferences.historicalTriggerPriorities
        ) { histPrice, histSize, histPriorities ->
            StatsHistoricalExtraPart(histPrice, histSize, histPriorities)
        },
        combine(
            stats,
            savingsData,
            baselineAnalytics,
            smokingIntervals,
            hourlyDistribution
        ) { calcStats, calcSavings, calcBaseline, calcIntervals, calcHourly ->
            StatsComputedPart(calcStats, calcSavings, calcBaseline, calcIntervals, calcHourly)
        }
    ) { dataPart, packPart, goalBasePart, histExtraPart, computedPart ->
        StatisticsUiState(
            entries = dataPart.entries,
            nonResistedEntries = dataPart.nonResistedEntries,
            resistedEntries = dataPart.resistedEntries,
            resistedTimestamps = dataPart.resistedTimestamps,
            resistedCount = dataPart.resistedEntries.size,
            firstAppLaunchDate = dataPart.firstAppLaunchDate,
            dailyLimit = packPart.dailyLimit,
            vibrationEnabled = packPart.vibrationEnabled,
            packPrice = packPart.packPrice,
            packSize = packPart.packSize,
            currency = packPart.currency,
            savingsGoalTitle = goalBasePart.savingsGoalTitle,
            savingsGoalAmount = goalBasePart.savingsGoalAmount,
            hasHistoricalBaseline = goalBasePart.hasHistoricalBaseline,
            historicalStartDate = goalBasePart.historicalStartDate,
            historicalDailyAvg = goalBasePart.historicalDailyAvg,
            historicalPackPrice = histExtraPart.historicalPackPrice,
            historicalPackSize = histExtraPart.historicalPackSize,
            historicalTriggerPriorities = histExtraPart.historicalTriggerPriorities,
            stats = computedPart.stats,
            savingsData = computedPart.savingsData,
            baselineAnalytics = computedPart.baselineAnalytics,
            smokingIntervals = computedPart.smokingIntervals,
            hourlyDistribution = computedPart.hourlyDistribution
        )
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsUiState()
        )

    val graphUiState: StateFlow<GraphUiState> = combine(
        statisticsUiState,
        combine(
            userPreferences.showLimitOnGraph,
            triggerRepository.customTriggers,
            triggerRepository.disabledDefaultTriggers
        ) { showLimit, customTrig, disabledTrig ->
            Triple(showLimit, customTrig, disabledTrig)
        },
        combine(
            _dailyDateMillis,
            _weeklyDateMillis,
            _monthlyDateMillis,
            _yearlyDateMillis
        ) { dMillis, wMillis, mMillis, yMillis ->
            GraphDatesPart(dMillis, wMillis, mMillis, yMillis)
        }
    ) { statsState, (showLimit, customTrig, disabledTrig), datesPart ->
        val zoneId = ZoneId.systemDefault()
        val dailyLocalDate = toLocalDate(datesPart.dailyDateMillis, zoneId)
        val weeklyLocalDate = toLocalDate(datesPart.weeklyDateMillis, zoneId)
        val monthlyLocalDate = toLocalDate(datesPart.monthlyDateMillis, zoneId)
        val yearlyLocalDate = toLocalDate(datesPart.yearlyDateMillis, zoneId)

        val entries = statsState.entries
        val dailyChartData = statisticsManager.generateDailyData(entries, dailyLocalDate, zoneId)
        val weeklyChartData = statisticsManager.generateWeeklyData(entries, weeklyLocalDate, zoneId)
        val monthlyChartData = statisticsManager.generateMonthlyData(entries, monthlyLocalDate, zoneId)
        val monthlyDailyChartData = statisticsManager.generateMonthlyDailyData(entries, monthlyLocalDate, zoneId)
        val yearlyChartData = statisticsManager.generateYearlyData(entries, yearlyLocalDate, zoneId)
        val weeklyComp = statisticsManager.calculateWeeklyComparison(
            entries = entries,
            referenceDate = weeklyLocalDate,
            zoneId = zoneId
        )

        GraphUiState(
            entries = entries,
            triggerEntries = statsState.nonResistedEntries,
            resistedCount = statsState.resistedCount,
            resistedTimestamps = statsState.resistedTimestamps,
            trackingStartMillis = statsState.firstAppLaunchDate,
            dailyLimit = statsState.dailyLimit,
            showLimitOnGraph = showLimit,
            packPrice = statsState.packPrice,
            packSize = statsState.packSize,
            currency = statsState.currency,
            vibrationEnabled = statsState.vibrationEnabled,
            savingsGoalTitle = statsState.savingsGoalTitle,
            savingsGoalAmount = statsState.savingsGoalAmount,
            hasHistoricalBaseline = statsState.hasHistoricalBaseline,
            historicalStartDate = statsState.historicalStartDate,
            historicalDailyAvg = statsState.historicalDailyAvg,
            customTriggers = customTrig,
            disabledDefaultTriggers = disabledTrig,
            dailyDateMillis = datesPart.dailyDateMillis,
            weeklyDateMillis = datesPart.weeklyDateMillis,
            monthlyDateMillis = datesPart.monthlyDateMillis,
            yearlyDateMillis = datesPart.yearlyDateMillis,
            dailyData = dailyChartData,
            weeklyData = weeklyChartData,
            monthlyData = monthlyChartData,
            monthlyDailyData = monthlyDailyChartData,
            yearlyData = yearlyChartData,
            weeklyComparison = weeklyComp,
            hourlyDistribution = statsState.hourlyDistribution,
            smokingIntervals = statsState.smokingIntervals,
            stats = statsState.stats,
            savingsData = statsState.savingsData,
            baselineAnalytics = statsState.baselineAnalytics
        )
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GraphUiState()
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

    fun onAnalyticsTabVisited() {
        val nowNs = System.nanoTime()
        val prevNs = lastAnalyticsVisitTimestampNs.get()
        if (prevNs != 0L && (nowNs - prevNs) in 0L until ANALYTICS_VISIT_COOLDOWN_NS) {
            return
        }
        if (!lastAnalyticsVisitTimestampNs.compareAndSet(prevNs, nowNs)) {
            return
        }

        safeLaunch(ioDispatcher) {
            analyticsVisitMutex.withLock {
                appMetaPreferences.recordAnalyticsVisit()
                achievementsCoordinator.checkAndUpdateSuspend()
            }
        }
    }

    fun refreshStatistics() {
        safeLaunch(ioDispatcher) {
            achievementsCoordinator.checkAndUpdateSuspend()
        }
    }

    fun saveBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float = this.packPrice.value,
        packSize: Int = this.packSize.value
    ) {
        val now = System.currentTimeMillis()
        if (startDate <= 0L || startDate > now || dailyAvg <= 0) return

        val safeDailyAvg = dailyAvg.coerceIn(1, MAX_DAILY_AVG)
        val safePackPrice = if (packPrice.isFinite() && packPrice >= 0f) {
            packPrice.coerceAtMost(MAX_PACK_PRICE)
        } else {
            0f
        }
        val safePackSize = packSize.takeIf { it in 1..MAX_PACK_SIZE } ?: UserPreferences.DEFAULT_PACK_SIZE

        safeLaunch(ioDispatcher) {
            baselineMutex.withLock {
                val existingPriorities = userPreferences.historicalTriggerPriorities.first()
                userPreferences.saveHistoricalBaseline(
                    startDate = startDate,
                    dailyAvg = safeDailyAvg,
                    packPrice = safePackPrice,
                    packSize = safePackSize,
                    triggerPriorities = existingPriorities
                )
            }
        }
    }

    fun saveHistoricalBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        triggerPriorities: List<String>
    ) {
        val now = System.currentTimeMillis()
        if (startDate <= 0L || startDate > now || dailyAvg <= 0) return

        val safeDailyAvg = dailyAvg.coerceIn(1, MAX_DAILY_AVG)
        val safePackPrice = if (packPrice.isFinite() && packPrice >= 0f) {
            packPrice.coerceAtMost(MAX_PACK_PRICE)
        } else {
            0f
        }
        val safePackSize = packSize.takeIf { it in 1..MAX_PACK_SIZE } ?: UserPreferences.DEFAULT_PACK_SIZE
        val sanitizedPriorities = triggerPriorities
            .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
            .distinct()

        safeLaunch(ioDispatcher) {
            baselineMutex.withLock {
                userPreferences.saveHistoricalBaseline(
                    startDate = startDate,
                    dailyAvg = safeDailyAvg,
                    packPrice = safePackPrice,
                    packSize = safePackSize,
                    triggerPriorities = sanitizedPriorities
                )
            }
        }
    }

    fun clearHistoricalBaseline() {
        safeLaunch(ioDispatcher) {
            baselineMutex.withLock {
                userPreferences.clearHistoricalBaseline()
            }
        }
    }

    fun saveSavingsGoal(title: String, amount: Float) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty() || !amount.isFinite() || amount <= 0f) return
        val safeAmount = amount.coerceAtMost(MAX_SAVINGS_GOAL_AMOUNT)

        safeLaunch(ioDispatcher) {
            userPreferences.saveSavingsGoal(cleanTitle, safeAmount)
        }
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
                Log.e(TAG, "Error in StatisticsViewModel coroutine", e)
            }
        }
    }

    private data class BaselinePrefsPart(
        val hasBaseline: Boolean,
        val startDate: Long,
        val dailyAvg: Int,
        val packPrice: Float,
        val packSize: Int
    )

    private data class StatsDataPart(
        val entries: List<Long>,
        val nonResistedEntries: List<SmokingEntry>,
        val resistedEntries: List<SmokingEntry>,
        val resistedTimestamps: List<Long>,
        val firstAppLaunchDate: Long?
    )

    private data class StatsPackPrefsPart(
        val dailyLimit: Int,
        val vibrationEnabled: Boolean,
        val packPrice: Float,
        val packSize: Int,
        val currency: String
    )

    private data class StatsGoalBaselinePart(
        val savingsGoalTitle: String,
        val savingsGoalAmount: Float,
        val hasHistoricalBaseline: Boolean,
        val historicalStartDate: Long,
        val historicalDailyAvg: Int
    )

    private data class StatsHistoricalExtraPart(
        val historicalPackPrice: Float,
        val historicalPackSize: Int,
        val historicalTriggerPriorities: List<String>
    )

    private data class StatsComputedPart(
        val stats: StatisticsData,
        val savingsData: StatisticsManager.SavingsData,
        val baselineAnalytics: StatisticsManager.SmokingBaselineAnalytics?,
        val smokingIntervals: StatisticsManager.SmokingIntervalData,
        val hourlyDistribution: StatisticsManager.HourlyDistributionData
    )

    private data class GraphDatesPart(
        val dailyDateMillis: Long,
        val weeklyDateMillis: Long,
        val monthlyDateMillis: Long,
        val yearlyDateMillis: Long
    )

    companion object {
        private const val TAG = "StatisticsViewModel"
        private const val ANALYTICS_VISIT_COOLDOWN_NS = 3_000_000_000L // 3s cooldown across rotations/recompositions
        private const val MAX_DAILY_AVG = 500
        private const val MAX_PACK_SIZE = 1000
        private const val MAX_PACK_PRICE = 1_000_000f
        private const val MAX_SAVINGS_GOAL_AMOUNT = 100_000_000f
    }
}
