package com.smokingtracker

import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.math.abs

enum class AchievementCategory(val titleResId: Int) {
    LOGIN(R.string.ach_category_login),
    NO_SMOKE(R.string.ach_category_nosmoke),
    SECRET(R.string.ach_category_secret)
}

data class AchievementContext(
    val timeWithoutSmoking: Long,
    val entries: List<Long>,
    val launches: List<Long>,
    val dailyLimit: Int = 0,
    val hasMadeBackup: Boolean = false,
    val hasChangedPackPrice: Boolean = false,
    val hasCancelledWithin10s: Boolean = false,
    val themeLangChangesToday: Int = 0,
    val analyticsVisitsToday: Int = 0,
    val nowMillis: Long = System.currentTimeMillis(),
    val zoneId: ZoneId = ZoneId.systemDefault()
) {
    internal val sortedEntries: List<Long> by lazy(LazyThreadSafetyMode.NONE) {
        if (entries.isAscending()) entries else entries.sorted()
    }

    internal val sortedLaunches: List<Long> by lazy(LazyThreadSafetyMode.NONE) {
        if (launches.isAscending()) launches else launches.sorted()
    }

    internal val maxConsecutiveLaunchDays: Int by lazy(LazyThreadSafetyMode.NONE) {
        AchievementsManager.calculateMaxConsecutiveDays(sortedLaunches, zoneId)
    }

    internal val maxSmokeFreeDurationMs: Long by lazy(LazyThreadSafetyMode.NONE) {
        maxOf(
            timeWithoutSmoking.coerceAtLeast(0L),
            AchievementsManager.calculateMaxSmokeFreeDuration(sortedEntries, sortedLaunches, nowMillis)
        )
    }

    internal val entriesCountByDay: Map<LocalDate, Int> by lazy(LazyThreadSafetyMode.NONE) {
        groupCountByDay(sortedEntries, zoneId)
    }
}

data class AchievementData(
    val id: String,
    val titleResId: Int,
    val descResId: Int,
    val category: AchievementCategory,
    val isSecret: Boolean = false
)

internal fun List<Long>.isAscending(): Boolean {
    for (i in 1 until size) {
        if (this[i] < this[i - 1]) return false
    }
    return true
}

class AchievementsManager {

    companion object {
        private const val NIGHT_OWL_START_MINUTE = 3 * 60 // 03:00
        private const val NIGHT_OWL_END_MINUTE = 5 * 60   // 05:00
        private const val MORNING_RITUAL_START_MINUTE = 4 * 60 // 04:00
        private const val MORNING_RITUAL_END_MINUTE = 7 * 60   // 07:00 (exclusive)
        private const val DOUBLE_DAMAGE_WINDOW_MS = 10 * 60 * 1000L
        private const val MIN_PUNCTUAL_INTERVAL_MS = 60 * 1000L
        private val MAX_PUNCTUAL_INTERVAL_MS = TimeUnit.DAYS.toMillis(1)
        private const val PUNCTUAL_TOLERANCE_MS = 60 * 1000L

        internal fun calculateMaxConsecutiveDays(
            dates: List<Long>,
            zoneId: ZoneId = ZoneId.systemDefault()
        ): Int {
            if (dates.isEmpty()) return 0
            val sorted = if (dates.isAscending()) dates else dates.sorted()
            var prevDay = toLocalDate(sorted[0], zoneId)
            var currentStreak = 1
            var maxStreak = 1

            for (i in 1 until sorted.size) {
                val day = toLocalDate(sorted[i], zoneId)
                if (day == prevDay) continue
                val daysDiff = daysBetween(prevDay, day)
                if (daysDiff == 1L) {
                    currentStreak++
                    if (currentStreak > maxStreak) maxStreak = currentStreak
                } else if (daysDiff > 1L) {
                    currentStreak = 1
                }
                prevDay = day
            }
            return maxStreak
        }

        internal fun calculateMaxSmokeFreeDuration(
            sortedEntries: List<Long>,
            sortedLaunches: List<Long>,
            nowMillis: Long
        ): Long {
            val entries = if (sortedEntries.isAscending()) sortedEntries else sortedEntries.sorted()
            val launches = if (sortedLaunches.isAscending()) sortedLaunches else sortedLaunches.sorted()
            val firstLaunch = launches.firstOrNull()

            if (entries.isEmpty()) {
                return if (firstLaunch != null) (nowMillis - firstLaunch).coerceAtLeast(0L) else 0L
            }

            var maxDuration = (nowMillis - entries.last()).coerceAtLeast(0L)
            if (firstLaunch != null) {
                val initialGap = (entries.first() - firstLaunch).coerceAtLeast(0L)
                if (initialGap > maxDuration) maxDuration = initialGap
            }
            for (i in 0 until entries.size - 1) {
                val gap = (entries[i + 1] - entries[i]).coerceAtLeast(0L)
                if (gap > maxDuration) maxDuration = gap
            }
            return maxDuration
        }
    }

    val achievementsList = listOf(
        AchievementData("login_1", R.string.ach_curiosity_title, R.string.ach_curiosity_desc, AchievementCategory.LOGIN),
        AchievementData("login_3", R.string.ach_interest_title, R.string.ach_interest_desc, AchievementCategory.LOGIN),
        AchievementData("login_7", R.string.ach_exploration_title, R.string.ach_exploration_desc, AchievementCategory.LOGIN),
        AchievementData("login_30", R.string.ach_discipline_title, R.string.ach_discipline_desc, AchievementCategory.LOGIN),
        AchievementData("login_90", R.string.ach_habit_title, R.string.ach_habit_desc, AchievementCategory.LOGIN),
        AchievementData("login_180", R.string.ach_dedication_title, R.string.ach_dedication_desc, AchievementCategory.LOGIN),
        AchievementData("login_365", R.string.ach_statistics_title, R.string.ach_statistics_desc, AchievementCategory.LOGIN),
        AchievementData("nosmoke_1d", R.string.ach_nosmoke_1d_title, R.string.ach_nosmoke_1d_desc, AchievementCategory.NO_SMOKE),
        AchievementData("nosmoke_3d", R.string.ach_nosmoke_3d_title, R.string.ach_nosmoke_3d_desc, AchievementCategory.NO_SMOKE),
        AchievementData("nosmoke_1w", R.string.ach_nosmoke_1w_title, R.string.ach_nosmoke_1w_desc, AchievementCategory.NO_SMOKE),
        AchievementData("nosmoke_1m", R.string.ach_nosmoke_1m_title, R.string.ach_nosmoke_1m_desc, AchievementCategory.NO_SMOKE),
        AchievementData("nosmoke_3m", R.string.ach_nosmoke_3m_title, R.string.ach_nosmoke_3m_desc, AchievementCategory.NO_SMOKE),
        AchievementData("nosmoke_6m", R.string.ach_nosmoke_6m_title, R.string.ach_nosmoke_6m_desc, AchievementCategory.NO_SMOKE),
        AchievementData("nosmoke_1y", R.string.ach_nosmoke_1y_title, R.string.ach_nosmoke_1y_desc, AchievementCategory.NO_SMOKE),
        AchievementData("secret_night_owl", R.string.ach_night_owl_title, R.string.ach_night_owl_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_morning_ritual", R.string.ach_morning_ritual_title, R.string.ach_morning_ritual_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_synchronization", R.string.ach_synchronization_title, R.string.ach_synchronization_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_punctuality", R.string.ach_punctuality_title, R.string.ach_punctuality_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_hesitant", R.string.ach_hesitant_title, R.string.ach_hesitant_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_explorer", R.string.ach_explorer_title, R.string.ach_explorer_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_archivist", R.string.ach_archivist_title, R.string.ach_archivist_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_analytics_collector", R.string.ach_analytics_collector_title, R.string.ach_analytics_collector_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_double_damage", R.string.ach_double_damage_title, R.string.ach_double_damage_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_inflation", R.string.ach_inflation_title, R.string.ach_inflation_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_crisis", R.string.ach_crisis_title, R.string.ach_crisis_desc, AchievementCategory.SECRET, isSecret = true),
        AchievementData("secret_blind_eye", R.string.ach_blind_eye_title, R.string.ach_blind_eye_desc, AchievementCategory.SECRET, isSecret = true)
    )

    private val achievementsById: Map<String, AchievementData> = achievementsList.associateBy { it.id }

    fun getAchievementById(id: String): AchievementData? = achievementsById[id]

    private val conditions: Map<String, (AchievementContext) -> Boolean> = mapOf(
        "login_1" to { ctx -> ctx.sortedLaunches.isNotEmpty() },
        "login_3" to { ctx -> ctx.maxConsecutiveLaunchDays >= 3 },
        "login_7" to { ctx -> ctx.maxConsecutiveLaunchDays >= 7 },
        "login_30" to { ctx -> ctx.maxConsecutiveLaunchDays >= 30 },
        "login_90" to { ctx -> ctx.maxConsecutiveLaunchDays >= 90 },
        "login_180" to { ctx -> ctx.maxConsecutiveLaunchDays >= 180 },
        "login_365" to { ctx -> ctx.maxConsecutiveLaunchDays >= 365 },
        "nosmoke_1d" to { ctx -> hasSmokeFreeDuration(ctx, TimeUnit.DAYS.toMillis(1)) },
        "nosmoke_3d" to { ctx -> hasSmokeFreeDuration(ctx, TimeUnit.DAYS.toMillis(3)) },
        "nosmoke_1w" to { ctx -> hasSmokeFreeDuration(ctx, TimeUnit.DAYS.toMillis(7)) },
        "nosmoke_1m" to { ctx -> hasSmokeFreeDuration(ctx, TimeUnit.DAYS.toMillis(30)) },
        "nosmoke_3m" to { ctx -> hasSmokeFreeDuration(ctx, TimeUnit.DAYS.toMillis(90)) },
        "nosmoke_6m" to { ctx -> hasSmokeFreeDuration(ctx, TimeUnit.DAYS.toMillis(180)) },
        "nosmoke_1y" to { ctx -> hasSmokeFreeDuration(ctx, TimeUnit.DAYS.toMillis(365)) },
        "secret_night_owl" to { ctx ->
            ctx.sortedEntries.any { ts -> isNightOwlTime(ts, ctx.zoneId) }
        },
        "secret_morning_ritual" to { ctx ->
            hasMorningRitualStreak(ctx.sortedEntries, 5, ctx.zoneId)
        },
        "secret_synchronization" to { ctx ->
            ctx.sortedEntries.any { ts -> isSynchronizationTime(ts, ctx.zoneId) }
        },
        "secret_punctuality" to { ctx -> hasPunctualIntervals(ctx.sortedEntries) },
        "secret_hesitant" to { ctx -> ctx.hasCancelledWithin10s },
        "secret_explorer" to { ctx -> ctx.themeLangChangesToday > 3 },
        "secret_archivist" to { ctx -> ctx.hasMadeBackup },
        "secret_analytics_collector" to { ctx -> ctx.analyticsVisitsToday > 10 },
        "secret_double_damage" to { ctx ->
            findDoubleDamageDate(ctx.sortedEntries) != null
        },
        "secret_inflation" to { ctx -> ctx.hasChangedPackPrice },
        "secret_crisis" to { ctx ->
            if (ctx.dailyLimit <= 0) {
                false
            } else {
                val today = toLocalDate(ctx.nowMillis, ctx.zoneId)
                ctx.entriesCountByDay.any { (day, count) ->
                    day.isBefore(today) && count == ctx.dailyLimit
                }
            }
        },
        "secret_blind_eye" to { ctx ->
            if (ctx.dailyLimit <= 0) {
                false
            } else {
                ctx.entriesCountByDay.values.any { it >= ctx.dailyLimit + 5 }
            }
        }
    )

    private fun isNightOwlTime(timestamp: Long, zoneId: ZoneId): Boolean {
        return minuteOfDay(timestamp, zoneId) in NIGHT_OWL_START_MINUTE..NIGHT_OWL_END_MINUTE
    }

    private fun isSynchronizationTime(timestamp: Long, zoneId: ZoneId): Boolean {
        val minOfDay = minuteOfDay(timestamp, zoneId)
        return minOfDay == 0 || minOfDay == 12 * 60
    }

    private fun hasConsecutiveDays(
        dates: List<Long>,
        requiredConsecutiveDays: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        return calculateMaxConsecutiveDays(dates, zoneId) >= requiredConsecutiveDays
    }

    private fun hasMorningRitualStreak(
        entries: List<Long>,
        targetDays: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        return findMorningRitualDate(entries, targetDays, zoneId) != null
    }

    private fun hasPunctualIntervals(entries: List<Long>): Boolean {
        return findPunctualDate(entries) != null
    }

    private fun hasSmokeFreeDuration(ctx: AchievementContext, duration: Long): Boolean {
        return ctx.maxSmokeFreeDurationMs >= duration
    }

    fun calculateUnlockedAchievements(ctx: AchievementContext): Set<String> {
        val unlocked = mutableSetOf<String>()
        for (achievement in achievementsList) {
            val condition = conditions[achievement.id]
            if (condition?.invoke(ctx) == true) {
                unlocked.add(achievement.id)
            }
        }
        return unlocked
    }

    fun calculateUnlockedAchievements(
        entries: List<Long>,
        launches: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): Set<String> {
        val sortedEntries = if (entries.isAscending()) entries else entries.sorted()
        val sortedLaunches = if (launches.isAscending()) launches else launches.sorted()
        val lastEntry = sortedEntries.lastOrNull()
        val timeWithoutSmoking = if (lastEntry != null) {
            (nowMillis - lastEntry).coerceAtLeast(0L)
        } else {
            val firstLaunch = sortedLaunches.firstOrNull()
            if (firstLaunch != null) (nowMillis - firstLaunch).coerceAtLeast(0L) else 0L
        }
        val ctx = AchievementContext(
            timeWithoutSmoking = timeWithoutSmoking,
            entries = sortedEntries,
            launches = sortedLaunches,
            nowMillis = nowMillis,
            zoneId = zoneId
        )
        return calculateUnlockedAchievements(ctx)
    }

    data class AchievementProgress(
        val fraction: Float,
        val currentDisplay: String,
        val targetDisplay: String,
        val remainingDisplay: String? = null
    )

    fun getAchievementProgress(
        achievementId: String,
        entries: List<Long>,
        launches: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): AchievementProgress {
        return when (achievementId) {
            "login_1" -> {
                val curr = if (launches.isNotEmpty()) 1 else 0
                AchievementProgress(curr.toFloat(), "$curr", "1")
            }
            "login_3" -> calcLoginProgress(launches, 3, zoneId)
            "login_7" -> calcLoginProgress(launches, 7, zoneId)
            "login_30" -> calcLoginProgress(launches, 30, zoneId)
            "login_90" -> calcLoginProgress(launches, 90, zoneId)
            "login_180" -> calcLoginProgress(launches, 180, zoneId)
            "login_365" -> calcLoginProgress(launches, 365, zoneId)
            "nosmoke_1d", "nosmoke_3d", "nosmoke_1w",
            "nosmoke_1m", "nosmoke_3m", "nosmoke_6m", "nosmoke_1y" -> {
                val bestSmokeFreeMs = calculateMaxSmokeFreeDuration(entries, launches, nowMillis)
                val smokeFreeDays = (bestSmokeFreeMs / TimeUnit.DAYS.toMillis(1)).toInt()
                val smokeFreeHours = (bestSmokeFreeMs / TimeUnit.HOURS.toMillis(1)).toInt()
                when (achievementId) {
                    "nosmoke_1d" -> {
                        val fraction = (bestSmokeFreeMs.toFloat() / TimeUnit.DAYS.toMillis(1)).coerceIn(0f, 1f)
                        val cappedHours = smokeFreeHours.coerceIn(0, 24)
                        val remainingHours = (24 - cappedHours).coerceAtLeast(0)
                        AchievementProgress(
                            fraction = fraction,
                            currentDisplay = "${cappedHours}h",
                            targetDisplay = "24h",
                            remainingDisplay = if (remainingHours > 0) "${remainingHours}h" else null
                        )
                    }
                    "nosmoke_3d" -> calcSmokeFreeDaysProgress(bestSmokeFreeMs, smokeFreeDays, 3)
                    "nosmoke_1w" -> calcSmokeFreeDaysProgress(bestSmokeFreeMs, smokeFreeDays, 7)
                    "nosmoke_1m" -> calcSmokeFreeDaysProgress(bestSmokeFreeMs, smokeFreeDays, 30)
                    "nosmoke_3m" -> calcSmokeFreeDaysProgress(bestSmokeFreeMs, smokeFreeDays, 90)
                    "nosmoke_6m" -> calcSmokeFreeDaysProgress(bestSmokeFreeMs, smokeFreeDays, 180)
                    "nosmoke_1y" -> calcSmokeFreeDaysProgress(bestSmokeFreeMs, smokeFreeDays, 365)
                    else -> AchievementProgress(0f, "0", "1")
                }
            }
            else -> AchievementProgress(0f, "0", "1")
        }
    }

    private fun calcLoginProgress(
        dates: List<Long>,
        target: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): AchievementProgress {
        val maxStreak = calculateMaxConsecutiveDays(dates, zoneId).coerceAtMost(target)
        val fraction = (maxStreak.toFloat() / target).coerceIn(0f, 1f)
        val rem = (target - maxStreak).coerceAtLeast(0)
        return AchievementProgress(
            fraction = fraction,
            currentDisplay = "$maxStreak",
            targetDisplay = "$target",
            remainingDisplay = if (rem > 0) "$rem" else null
        )
    }

    private fun calcSmokeFreeDaysProgress(
        bestSmokeFreeMs: Long,
        currentDays: Int,
        targetDays: Int
    ): AchievementProgress {
        val targetMillis = TimeUnit.DAYS.toMillis(targetDays.toLong())
        val fraction = (bestSmokeFreeMs.toFloat() / targetMillis).coerceIn(0f, 1f)
        val cappedDays = currentDays.coerceIn(0, targetDays)
        val rem = (targetDays - cappedDays).coerceAtLeast(0)
        return AchievementProgress(
            fraction = fraction,
            currentDisplay = "$cappedDays",
            targetDisplay = "$targetDays",
            remainingDisplay = if (rem > 0) "$rem" else null
        )
    }

    fun progressFraction(
        achievementId: String,
        entries: List<Long>,
        launches: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): Float {
        return getAchievementProgress(achievementId, entries, launches, zoneId, nowMillis).fraction
    }

    fun estimateHistoricalUnlockTimestamp(
        achievementId: String,
        entries: List<Long>,
        launches: List<Long>,
        dailyLimit: Int = 0,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        val sortedEntries = if (entries.isAscending()) entries else entries.sorted()
        val sortedLaunches = if (launches.isAscending()) launches else launches.sorted()

        return when (achievementId) {
            "login_1" -> sortedLaunches.firstOrNull() ?: sortedEntries.firstOrNull()
            "login_3" -> findStreakDate(sortedLaunches, 3, zoneId)
            "login_7" -> findStreakDate(sortedLaunches, 7, zoneId)
            "login_30" -> findStreakDate(sortedLaunches, 30, zoneId)
            "login_90" -> findStreakDate(sortedLaunches, 90, zoneId)
            "login_180" -> findStreakDate(sortedLaunches, 180, zoneId)
            "login_365" -> findStreakDate(sortedLaunches, 365, zoneId)

            "nosmoke_1d" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(1), nowMillis)
            "nosmoke_3d" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(3), nowMillis)
            "nosmoke_1w" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(7), nowMillis)
            "nosmoke_1m" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(30), nowMillis)
            "nosmoke_3m" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(90), nowMillis)
            "nosmoke_6m" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(180), nowMillis)
            "nosmoke_1y" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(365), nowMillis)

            "secret_night_owl" -> {
                sortedEntries.firstOrNull { ts -> isNightOwlTime(ts, zoneId) }
            }
            "secret_morning_ritual" -> {
                findMorningRitualDate(sortedEntries, 5, zoneId)
            }
            "secret_synchronization" -> {
                sortedEntries.firstOrNull { ts -> isSynchronizationTime(ts, zoneId) }
            }
            "secret_punctuality" -> {
                findPunctualDate(sortedEntries)
            }
            "secret_double_damage" -> {
                findDoubleDamageDate(sortedEntries)
            }
            "secret_crisis" -> {
                if (dailyLimit > 0) {
                    findDailyLimitDate(sortedEntries, dailyLimit, isAtLeast = false, zoneId = zoneId, nowMillis = nowMillis)
                } else {
                    null
                }
            }
            "secret_blind_eye" -> {
                if (dailyLimit > 0) {
                    findDailyLimitDate(sortedEntries, dailyLimit + 5, isAtLeast = true, zoneId = zoneId, nowMillis = nowMillis)
                } else {
                    null
                }
            }
            else -> null
        }
    }

    internal fun findStreakDate(
        dates: List<Long>,
        targetDays: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        if (targetDays <= 0 || dates.isEmpty()) return null
        val sorted = if (dates.isAscending()) dates else dates.sorted()
        if (targetDays == 1) return sorted.first()

        var prevDay = toLocalDate(sorted[0], zoneId)
        var currentStreak = 1

        for (i in 1 until sorted.size) {
            val ts = sorted[i]
            val day = toLocalDate(ts, zoneId)
            if (day == prevDay) continue
            val daysDiff = daysBetween(prevDay, day)
            if (daysDiff == 1L) {
                currentStreak++
                if (currentStreak >= targetDays) {
                    return ts
                }
            } else if (daysDiff > 1L) {
                currentStreak = 1
            }
            prevDay = day
        }
        return null
    }

    private fun findSmokeFreeCrossingDate(
        sortedEntries: List<Long>,
        sortedLaunches: List<Long>,
        duration: Long,
        nowMillis: Long
    ): Long? {
        val firstLaunch = sortedLaunches.firstOrNull()
        if (sortedEntries.isEmpty()) {
            if (firstLaunch == null) return null
            return if (nowMillis - firstLaunch >= duration) {
                (firstLaunch + duration).coerceAtMost(nowMillis)
            } else {
                null
            }
        }

        if (firstLaunch != null && sortedEntries.first() - firstLaunch >= duration) {
            return firstLaunch + duration
        }
        for (i in 0 until sortedEntries.size - 1) {
            val gap = sortedEntries[i + 1] - sortedEntries[i]
            if (gap >= duration) {
                return sortedEntries[i] + duration
            }
        }
        val lastEntry = sortedEntries.last()
        if (nowMillis - lastEntry >= duration) {
            return (lastEntry + duration).coerceAtMost(nowMillis)
        }
        return null
    }

    private fun findMorningRitualDate(
        entries: List<Long>,
        targetDays: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        if (targetDays <= 0 || entries.isEmpty()) return null
        val sorted = if (entries.isAscending()) entries else entries.sorted()

        // Find the earliest cigarette of each day and keep only days where it falls in [04:00, 07:00)
        val validDayTimestamps = ArrayList<Pair<LocalDate, Long>>()
        var currentDay: LocalDate? = null

        for (ts in sorted) {
            val day = toLocalDate(ts, zoneId)
            if (day != currentDay) {
                currentDay = day
                val minOfDay = minuteOfDay(ts, zoneId)
                if (minOfDay in MORNING_RITUAL_START_MINUTE until MORNING_RITUAL_END_MINUTE) {
                    validDayTimestamps.add(day to ts)
                }
            }
        }

        if (validDayTimestamps.isEmpty()) return null
        if (targetDays == 1) return validDayTimestamps.first().second

        var currentStreak = 1
        for (i in 1 until validDayTimestamps.size) {
            val diff = daysBetween(validDayTimestamps[i - 1].first, validDayTimestamps[i].first)
            if (diff == 1L) {
                currentStreak++
                if (currentStreak >= targetDays) {
                    return validDayTimestamps[i].second
                }
            } else if (diff > 1L) {
                currentStreak = 1
            }
        }
        return null
    }

    internal fun findPunctualDate(entries: List<Long>): Long? {
        if (entries.size < 3) return null
        val sorted = if (entries.isAscending()) entries else entries.sorted()
        for (i in 0 until sorted.size - 2) {
            val diff1 = sorted[i + 1] - sorted[i]
            val diff2 = sorted[i + 2] - sorted[i + 1]
            if (diff1 in MIN_PUNCTUAL_INTERVAL_MS..MAX_PUNCTUAL_INTERVAL_MS &&
                diff2 in MIN_PUNCTUAL_INTERVAL_MS..MAX_PUNCTUAL_INTERVAL_MS &&
                (diff1 / MIN_PUNCTUAL_INTERVAL_MS) == (diff2 / MIN_PUNCTUAL_INTERVAL_MS) &&
                abs(diff1 - diff2) < PUNCTUAL_TOLERANCE_MS
            ) {
                return sorted[i + 2]
            }
        }
        return null
    }

    private fun findDoubleDamageDate(entries: List<Long>): Long? {
        if (entries.size < 2) return null
        val sorted = if (entries.isAscending()) entries else entries.sorted()
        for (i in 0 until sorted.size - 1) {
            val diff = sorted[i + 1] - sorted[i]
            if (diff in 0L until DOUBLE_DAMAGE_WINDOW_MS) {
                return sorted[i + 1]
            }
        }
        return null
    }

    private fun findDailyLimitDate(
        entries: List<Long>,
        targetCount: Int,
        isAtLeast: Boolean,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        if (targetCount <= 0 || entries.isEmpty()) return null
        val sorted = if (entries.isAscending()) entries else entries.sorted()
        val today = toLocalDate(nowMillis, zoneId)

        val byDay = LinkedHashMap<LocalDate, MutableList<Long>>()
        for (ts in sorted) {
            val day = toLocalDate(ts, zoneId)
            byDay.getOrPut(day) { ArrayList() }.add(ts)
        }

        for ((day, dayEntries) in byDay) {
            if (isAtLeast) {
                if (dayEntries.size >= targetCount) {
                    return dayEntries[targetCount - 1]
                }
            } else {
                // For exact daily limit match (secret_crisis), only evaluate completed days
                if (day.isBefore(today) && dayEntries.size == targetCount) {
                    return dayEntries.last()
                }
            }
        }
        return null
    }
}
