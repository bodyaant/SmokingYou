package com.smokingtracker

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Calendar
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
    val analyticsVisitsToday: Int = 0
)

data class Achievement(
    val id: String,
    val titleResId: Int,
    val descResId: Int,
    val category: AchievementCategory,
    val isSecret: Boolean = false,
    val condition: (AchievementContext) -> Boolean
)

class AchievementsManager {

    fun getAchievementById(id: String): Achievement? = achievementsList.find { it.id == id }

    val achievementsList = listOf(
        Achievement("login_1", R.string.ach_curiosity_title, R.string.ach_curiosity_desc, AchievementCategory.LOGIN) { ctx ->
            ctx.launches.isNotEmpty()
        },
        Achievement("login_3", R.string.ach_interest_title, R.string.ach_interest_desc, AchievementCategory.LOGIN) { ctx ->
            hasConsecutiveDays(ctx.launches, 3)
        },
        Achievement("login_7", R.string.ach_exploration_title, R.string.ach_exploration_desc, AchievementCategory.LOGIN) { ctx ->
            hasConsecutiveDays(ctx.launches, 7)
        },
        Achievement("login_30", R.string.ach_discipline_title, R.string.ach_discipline_desc, AchievementCategory.LOGIN) { ctx ->
            hasConsecutiveDays(ctx.launches, 30)
        },
        Achievement("login_90", R.string.ach_habit_title, R.string.ach_habit_desc, AchievementCategory.LOGIN) { ctx ->
            hasConsecutiveDays(ctx.launches, 90)
        },
        Achievement("login_180", R.string.ach_dedication_title, R.string.ach_dedication_desc, AchievementCategory.LOGIN) { ctx ->
            hasConsecutiveDays(ctx.launches, 180)
        },
        Achievement("login_365", R.string.ach_statistics_title, R.string.ach_statistics_desc, AchievementCategory.LOGIN) { ctx ->
            hasConsecutiveDays(ctx.launches, 365)
        },

        Achievement("nosmoke_1d", R.string.ach_nosmoke_1d_title, R.string.ach_nosmoke_1d_desc, AchievementCategory.NO_SMOKE) { ctx ->
            ctx.timeWithoutSmoking >= TimeUnit.DAYS.toMillis(1)
        },
        Achievement("nosmoke_3d", R.string.ach_nosmoke_3d_title, R.string.ach_nosmoke_3d_desc, AchievementCategory.NO_SMOKE) { ctx ->
            ctx.timeWithoutSmoking >= TimeUnit.DAYS.toMillis(3)
        },
        Achievement("nosmoke_1w", R.string.ach_nosmoke_1w_title, R.string.ach_nosmoke_1w_desc, AchievementCategory.NO_SMOKE) { ctx ->
            ctx.timeWithoutSmoking >= TimeUnit.DAYS.toMillis(7)
        },
        Achievement("nosmoke_1m", R.string.ach_nosmoke_1m_title, R.string.ach_nosmoke_1m_desc, AchievementCategory.NO_SMOKE) { ctx ->
            ctx.timeWithoutSmoking >= TimeUnit.DAYS.toMillis(30)
        },
        Achievement("nosmoke_3m", R.string.ach_nosmoke_3m_title, R.string.ach_nosmoke_3m_desc, AchievementCategory.NO_SMOKE) { ctx ->
            ctx.timeWithoutSmoking >= TimeUnit.DAYS.toMillis(90)
        },
        Achievement("nosmoke_6m", R.string.ach_nosmoke_6m_title, R.string.ach_nosmoke_6m_desc, AchievementCategory.NO_SMOKE) { ctx ->
            ctx.timeWithoutSmoking >= TimeUnit.DAYS.toMillis(180)
        },
        Achievement("nosmoke_1y", R.string.ach_nosmoke_1y_title, R.string.ach_nosmoke_1y_desc, AchievementCategory.NO_SMOKE) { ctx ->
            ctx.timeWithoutSmoking >= TimeUnit.DAYS.toMillis(365)
        },

        Achievement("secret_night_owl", R.string.ach_night_owl_title, R.string.ach_night_owl_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            ctx.entries.any { timestamp ->
                val hour = Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.HOUR_OF_DAY)
                hour in 3..4
            }
        },
        Achievement("secret_morning_ritual", R.string.ach_morning_ritual_title, R.string.ach_morning_ritual_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            hasMorningRitualStreak(ctx.entries, 5)
        },
        Achievement("secret_synchronization", R.string.ach_synchronization_title, R.string.ach_synchronization_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            ctx.entries.any { timestamp ->
                val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val min = cal.get(Calendar.MINUTE)
                (hour == 0 || hour == 12) && min == 0
            }
        },
        Achievement("secret_punctuality", R.string.ach_punctuality_title, R.string.ach_punctuality_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            hasPunctualIntervals(ctx.entries)
        },
        Achievement("secret_hesitant", R.string.ach_hesitant_title, R.string.ach_hesitant_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            ctx.hasCancelledWithin10s
        },
        Achievement("secret_explorer", R.string.ach_explorer_title, R.string.ach_explorer_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            ctx.themeLangChangesToday >= 3
        },
        Achievement("secret_archivist", R.string.ach_archivist_title, R.string.ach_archivist_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            ctx.hasMadeBackup
        },
        Achievement("secret_analytics_collector", R.string.ach_analytics_collector_title, R.string.ach_analytics_collector_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            ctx.analyticsVisitsToday >= 10
        },
        Achievement("secret_double_damage", R.string.ach_double_damage_title, R.string.ach_double_damage_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            val sorted = ctx.entries.sorted()
            sorted.zipWithNext().any { (t1, t2) -> (t2 - t1) in 1 until (10 * 60 * 1000L) }
        },
        Achievement("secret_inflation", R.string.ach_inflation_title, R.string.ach_inflation_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            ctx.hasChangedPackPrice
        },
        Achievement("secret_crisis", R.string.ach_crisis_title, R.string.ach_crisis_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            if (ctx.dailyLimit <= 0) false
            else {
                val dayCounts = ctx.entries.groupBy { timestamp ->
                    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                    cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
                }.mapValues { it.value.size }
                dayCounts.values.any { it == ctx.dailyLimit }
            }
        },
        Achievement("secret_blind_eye", R.string.ach_blind_eye_title, R.string.ach_blind_eye_desc, AchievementCategory.SECRET, isSecret = true) { ctx ->
            if (ctx.dailyLimit <= 0) false
            else {
                val dayCounts = ctx.entries.groupBy { timestamp ->
                    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                    cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
                }.mapValues { it.value.size }
                dayCounts.values.any { it >= ctx.dailyLimit + 5 }
            }
        }
    )

    private fun hasConsecutiveDays(dates: List<Long>, requiredConsecutiveDays: Int): Boolean {
        if (dates.isEmpty()) return false

        val sortedDays = dates.map {
            val cal = Calendar.getInstance().apply { timeInMillis = it }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sorted()

        var currentStreak = 1
        var maxStreak = 1

        for (i in 1 until sortedDays.size) {
            val daysDiff = daysBetween(sortedDays[i - 1], sortedDays[i])
            if (daysDiff == 1L) {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
            } else if (daysDiff > 1L) {
                currentStreak = 1
            }
        }

        return maxStreak >= requiredConsecutiveDays
    }

    private fun hasMorningRitualStreak(entries: List<Long>, targetDays: Int): Boolean {
        if (entries.isEmpty()) return false
        val dayEarliestMinutes = entries.groupBy { timestamp ->
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.mapValues { (_, timestamps) ->
            timestamps.minOf { ts ->
                val cal = Calendar.getInstance().apply { timeInMillis = ts }
                cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            }
        }

        val validDays = dayEarliestMinutes.filterValues { it <= 7 * 60 }.keys.sorted()
        if (validDays.isEmpty()) return false

        var currentStreak = 1
        var maxStreak = 1

        for (i in 1 until validDays.size) {
            val daysDiff = daysBetween(validDays[i - 1], validDays[i])
            if (daysDiff == 1L) {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
            } else {
                currentStreak = 1
            }
        }

        return maxStreak >= targetDays
    }

    private fun hasPunctualIntervals(entries: List<Long>): Boolean {
        if (entries.size < 3) return false
        val sorted = entries.sorted()
        for (i in 0 until sorted.size - 2) {
            val diff1 = sorted[i + 1] - sorted[i]
            val diff2 = sorted[i + 2] - sorted[i + 1]
            if (diff1 >= 5 * 60 * 1000L && abs(diff1 - diff2) <= 2 * 60 * 1000L) {
                return true
            }
        }
        return false
    }

    fun calculateUnlockedAchievements(ctx: AchievementContext): Set<String> {
        val unlocked = mutableSetOf<String>()
        achievementsList.forEach { achievement ->
            if (achievement.condition(ctx)) {
                unlocked.add(achievement.id)
            }
        }
        return unlocked
    }

    fun calculateUnlockedAchievements(entries: List<Long>, launches: List<Long>): Set<String> {
        val lastEntry = entries.maxOrNull()
        val now = System.currentTimeMillis()
        val timeWithoutSmoking = if (lastEntry != null) (now - lastEntry).coerceAtLeast(0L) else 0L
        val ctx = AchievementContext(
            timeWithoutSmoking = timeWithoutSmoking,
            entries = entries,
            launches = launches
        )
        return calculateUnlockedAchievements(ctx)
    }

    data class AchievementProgress(
        val fraction: Float,
        val currentDisplay: String,
        val targetDisplay: String,
        val remainingDisplay: String? = null
    )

    fun getAchievementProgress(achievementId: String, entries: List<Long>, launches: List<Long>): AchievementProgress {
        val now = System.currentTimeMillis()
        val timeWithoutSmoking = (entries.maxOrNull()?.let { now - it } ?: 0L).coerceAtLeast(0L)
        val smokeFreeDays = (timeWithoutSmoking / TimeUnit.DAYS.toMillis(1)).toInt()
        val smokeFreeHours = (timeWithoutSmoking / TimeUnit.HOURS.toMillis(1)).toInt()

        return when (achievementId) {
            "login_1" -> {
                val curr = if (launches.isNotEmpty()) 1 else 0
                AchievementProgress(curr.toFloat(), "$curr", "1")
            }
            "login_3" -> calcLoginProgress(launches, 3)
            "login_7" -> calcLoginProgress(launches, 7)
            "login_30" -> calcLoginProgress(launches, 30)
            "login_90" -> calcLoginProgress(launches, 90)
            "login_180" -> calcLoginProgress(launches, 180)
            "login_365" -> calcLoginProgress(launches, 365)
            "nosmoke_1d" -> {
                val fraction = (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(1)).coerceIn(0f, 1f)
                val remainingHours = (24 - smokeFreeHours).coerceAtLeast(0)
                AchievementProgress(
                    fraction = fraction,
                    currentDisplay = "${smokeFreeHours}h",
                    targetDisplay = "24h",
                    remainingDisplay = if (remainingHours > 0) "${remainingHours}h" else null
                )
            }
            "nosmoke_3d" -> calcSmokeFreeDaysProgress(timeWithoutSmoking, smokeFreeDays, 3)
            "nosmoke_1w" -> calcSmokeFreeDaysProgress(timeWithoutSmoking, smokeFreeDays, 7)
            "nosmoke_1m" -> calcSmokeFreeDaysProgress(timeWithoutSmoking, smokeFreeDays, 30)
            "nosmoke_3m" -> calcSmokeFreeDaysProgress(timeWithoutSmoking, smokeFreeDays, 90)
            "nosmoke_6m" -> calcSmokeFreeDaysProgress(timeWithoutSmoking, smokeFreeDays, 180)
            "nosmoke_1y" -> calcSmokeFreeDaysProgress(timeWithoutSmoking, smokeFreeDays, 365)
            else -> AchievementProgress(0f, "0", "1")
        }
    }

    private fun calcLoginProgress(dates: List<Long>, target: Int): AchievementProgress {
        val maxStreak = calculateMaxConsecutiveDays(dates)
        val fraction = (maxStreak.toFloat() / target).coerceIn(0f, 1f)
        val rem = (target - maxStreak).coerceAtLeast(0)
        return AchievementProgress(
            fraction = fraction,
            currentDisplay = "$maxStreak",
            targetDisplay = "$target",
            remainingDisplay = if (rem > 0) "$rem" else null
        )
    }

    private fun calcSmokeFreeDaysProgress(timeWithoutSmoking: Long, currentDays: Int, targetDays: Int): AchievementProgress {
        val targetMillis = TimeUnit.DAYS.toMillis(targetDays.toLong())
        val fraction = (timeWithoutSmoking.toFloat() / targetMillis).coerceIn(0f, 1f)
        val rem = (targetDays - currentDays).coerceAtLeast(0)
        return AchievementProgress(
            fraction = fraction,
            currentDisplay = "$currentDays",
            targetDisplay = "$targetDays",
            remainingDisplay = if (rem > 0) "$rem" else null
        )
    }

    private fun calculateMaxConsecutiveDays(dates: List<Long>): Int {
        if (dates.isEmpty()) return 0
        val sortedDays = dates.map {
            Calendar.getInstance().apply {
                timeInMillis = it
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.distinct().sorted()
        var currentStreak = 1
        var maxStreak = 1
        for (i in 1 until sortedDays.size) {
            val daysDiff = daysBetween(sortedDays[i - 1], sortedDays[i])
            if (daysDiff == 1L) { currentStreak++; if (currentStreak > maxStreak) maxStreak = currentStreak }
            else if (daysDiff > 1L) currentStreak = 1
        }
        return maxStreak
    }

    fun progressFraction(achievementId: String, entries: List<Long>, launches: List<Long>): Float {
        val now = System.currentTimeMillis()
        val timeWithoutSmoking = (entries.maxOrNull()?.let { now - it } ?: 0L).coerceAtLeast(0L)
        return when (achievementId) {
            "login_1"   -> if (launches.isNotEmpty()) 1f else 0f
            "login_3"   -> consecutiveDaysFraction(launches, 3)
            "login_7"   -> consecutiveDaysFraction(launches, 7)
            "login_30"  -> consecutiveDaysFraction(launches, 30)
            "login_90"  -> consecutiveDaysFraction(launches, 90)
            "login_180" -> consecutiveDaysFraction(launches, 180)
            "login_365" -> consecutiveDaysFraction(launches, 365)
            "nosmoke_1d" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(1)).coerceIn(0f, 1f)
            "nosmoke_3d" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(3)).coerceIn(0f, 1f)
            "nosmoke_1w" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(7)).coerceIn(0f, 1f)
            "nosmoke_1m" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(30)).coerceIn(0f, 1f)
            "nosmoke_3m" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(90)).coerceIn(0f, 1f)
            "nosmoke_6m" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(180)).coerceIn(0f, 1f)
            "nosmoke_1y" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(365)).coerceIn(0f, 1f)
            else -> 0f
        }
    }

    private fun consecutiveDaysFraction(dates: List<Long>, target: Int): Float {
        val maxStreak = calculateMaxConsecutiveDays(dates)
        return (maxStreak.toFloat() / target).coerceIn(0f, 1f)
    }

    fun sendNotificationForAchievement(context: Context, achievementId: String) {
        val achievement = achievementsList.find { it.id == achievementId } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val title = context.getString(achievement.titleResId)
        val desc = context.getString(achievement.descResId)
        val icon = if (achievement.category == AchievementCategory.NO_SMOKE) {
            R.drawable.ic_crosscigarette
        } else {
            R.drawable.ic_cigarettebase
        }

        val builder = NotificationCompat.Builder(context, SmokingTrackerApp.CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(context.getString(R.string.notification_title, title))
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(achievement.id.hashCode(), builder.build())
            } catch (e: SecurityException) {
            }
        }
    }

    fun estimateHistoricalUnlockTimestamp(
        achievementId: String,
        entries: List<Long>,
        launches: List<Long>,
        dailyLimit: Int = 0
    ): Long? {
        val now = System.currentTimeMillis()
        val sortedEntries = entries.sorted()
        val sortedLaunches = launches.sorted()

        return when (achievementId) {
            "login_1" -> sortedLaunches.firstOrNull() ?: sortedEntries.firstOrNull() ?: now
            "login_3" -> findStreakDate(sortedLaunches, 3) ?: sortedLaunches.firstOrNull() ?: now
            "login_7" -> findStreakDate(sortedLaunches, 7) ?: sortedLaunches.firstOrNull() ?: now
            "login_30" -> findStreakDate(sortedLaunches, 30) ?: sortedLaunches.firstOrNull() ?: now
            "login_90" -> findStreakDate(sortedLaunches, 90) ?: sortedLaunches.firstOrNull() ?: now
            "login_180" -> findStreakDate(sortedLaunches, 180) ?: sortedLaunches.firstOrNull() ?: now
            "login_365" -> findStreakDate(sortedLaunches, 365) ?: sortedLaunches.firstOrNull() ?: now

            "nosmoke_1d" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(1), now)
            "nosmoke_3d" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(3), now)
            "nosmoke_1w" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(7), now)
            "nosmoke_1m" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(30), now)
            "nosmoke_3m" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(90), now)
            "nosmoke_6m" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(180), now)
            "nosmoke_1y" -> findSmokeFreeCrossingDate(sortedEntries, sortedLaunches, TimeUnit.DAYS.toMillis(365), now)

            "secret_night_owl" -> {
                sortedEntries.firstOrNull { ts ->
                    val hour = Calendar.getInstance().apply { timeInMillis = ts }.get(Calendar.HOUR_OF_DAY)
                    hour in 3..4
                } ?: sortedLaunches.firstOrNull() ?: now
            }
            "secret_morning_ritual" -> {
                findMorningRitualDate(sortedEntries, 5) ?: sortedLaunches.firstOrNull() ?: now
            }
            "secret_synchronization" -> {
                sortedEntries.firstOrNull { ts ->
                    val cal = Calendar.getInstance().apply { timeInMillis = ts }
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val min = cal.get(Calendar.MINUTE)
                    (hour == 0 || hour == 12) && min == 0
                } ?: sortedLaunches.firstOrNull() ?: now
            }
            "secret_punctuality" -> {
                findPunctualDate(sortedEntries) ?: sortedLaunches.firstOrNull() ?: now
            }
            "secret_double_damage" -> {
                for (i in 0 until sortedEntries.size - 1) {
                    val diff = sortedEntries[i + 1] - sortedEntries[i]
                    if (diff in 1 until (10 * 60 * 1000L)) {
                        return sortedEntries[i + 1]
                    }
                }
                sortedLaunches.firstOrNull() ?: now
            }
            "secret_crisis" -> {
                if (dailyLimit > 0) {
                    findDailyLimitDate(sortedEntries, dailyLimit, false) ?: sortedLaunches.firstOrNull() ?: now
                } else {
                    sortedLaunches.firstOrNull() ?: now
                }
            }
            "secret_blind_eye" -> {
                if (dailyLimit > 0) {
                    findDailyLimitDate(sortedEntries, dailyLimit + 5, true) ?: sortedLaunches.firstOrNull() ?: now
                } else {
                    sortedLaunches.firstOrNull() ?: now
                }
            }
            else -> sortedLaunches.firstOrNull() ?: sortedEntries.firstOrNull() ?: now
        }
    }

    private fun findStreakDate(dates: List<Long>, targetDays: Int): Long? {
        if (dates.isEmpty()) return null
        val daysWithTimestamp = dates.groupBy { ts ->
            Calendar.getInstance().apply {
                timeInMillis = ts
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.toSortedMap()

        val sortedDayKeys = daysWithTimestamp.keys.toList()
        var currentStreak = 1
        for (i in 1 until sortedDayKeys.size) {
            val daysDiff = daysBetween(sortedDayKeys[i - 1], sortedDayKeys[i])
            if (daysDiff == 1L) {
                currentStreak++
                if (currentStreak == targetDays) {
                    return daysWithTimestamp[sortedDayKeys[i]]?.minOrNull() ?: sortedDayKeys[i]
                }
            } else if (daysDiff > 1L) {
                currentStreak = 1
            }
        }
        return null
    }

    private fun findSmokeFreeCrossingDate(
        sortedEntries: List<Long>,
        sortedLaunches: List<Long>,
        duration: Long,
        now: Long
    ): Long {
        if (sortedEntries.isEmpty()) {
            val start = sortedLaunches.firstOrNull() ?: (now - duration)
            return (start + duration).coerceAtMost(now)
        }
        for (i in 0 until sortedEntries.size - 1) {
            val gap = sortedEntries[i + 1] - sortedEntries[i]
            if (gap >= duration) {
                return sortedEntries[i] + duration
            }
        }
        val lastEntry = sortedEntries.last()
        if (now - lastEntry >= duration) {
            return lastEntry + duration
        }
        return (lastEntry + duration).coerceAtMost(now)
    }

    private fun findMorningRitualDate(entries: List<Long>, targetDays: Int): Long? {
        val dayEarliest = entries.groupBy { ts ->
            Calendar.getInstance().apply {
                timeInMillis = ts
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.mapValues { (_, timestamps) ->
            timestamps.filter { ts ->
                val cal = Calendar.getInstance().apply { timeInMillis = ts }
                (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) <= 7 * 60
            }.minOrNull()
        }.filterValues { it != null }

        val validDays = dayEarliest.keys.sorted()
        var currentStreak = 1
        for (i in 1 until validDays.size) {
            val diff = daysBetween(validDays[i - 1], validDays[i])
            if (diff == 1L) {
                currentStreak++
                if (currentStreak == targetDays) {
                    return dayEarliest[validDays[i]]
                }
            } else {
                currentStreak = 1
            }
        }
        return null
    }

    private fun findPunctualDate(entries: List<Long>): Long? {
        if (entries.size < 3) return null
        for (i in 0 until entries.size - 2) {
            val diff1 = entries[i + 1] - entries[i]
            val diff2 = entries[i + 2] - entries[i + 1]
            if (diff1 >= 5 * 60 * 1000L && abs(diff1 - diff2) <= 2 * 60 * 1000L) {
                return entries[i + 2]
            }
        }
        return null
    }

    private fun findDailyLimitDate(entries: List<Long>, targetCount: Int, isAtLeast: Boolean): Long? {
        val days = entries.groupBy { ts ->
            Calendar.getInstance().apply {
                timeInMillis = ts
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.toSortedMap()

        for ((_, dayEntries) in days) {
            val sortedDayEntries = dayEntries.sorted()
            if (isAtLeast && sortedDayEntries.size >= targetCount) {
                return sortedDayEntries[targetCount - 1]
            } else if (!isAtLeast && sortedDayEntries.size == targetCount) {
                return sortedDayEntries.last()
            }
        }
        return null
    }
}
