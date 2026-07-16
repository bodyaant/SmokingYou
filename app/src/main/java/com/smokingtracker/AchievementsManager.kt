package com.smokingtracker

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class AchievementCategory(val titleResId: Int) {
    LOGIN(R.string.ach_category_login),
    NO_SMOKE(R.string.ach_category_nosmoke)
}

data class Achievement(
    val id: String,
    val titleResId: Int,
    val descResId: Int,
    val category: AchievementCategory,
    val condition: (Long, List<Long>, List<Long>) -> Boolean 
)

object AchievementsManager {
    
    val achievementsList = listOf(
        Achievement("login_1", R.string.ach_curiosity_title, R.string.ach_curiosity_desc, AchievementCategory.LOGIN) { _, _, launches -> 
            launches.isNotEmpty() 
        },
        Achievement("login_3", R.string.ach_interest_title, R.string.ach_interest_desc, AchievementCategory.LOGIN) { _, _, launches -> 
            hasConsecutiveDays(launches, 3)
        },
        Achievement("login_7", R.string.ach_exploration_title, R.string.ach_exploration_desc, AchievementCategory.LOGIN) { _, _, launches -> 
            hasConsecutiveDays(launches, 7)
        },
        Achievement("login_30", R.string.ach_discipline_title, R.string.ach_discipline_desc, AchievementCategory.LOGIN) { _, _, launches -> 
            hasConsecutiveDays(launches, 30)
        },
        Achievement("login_365", R.string.ach_statistics_title, R.string.ach_statistics_desc, AchievementCategory.LOGIN) { _, _, launches -> 
            hasConsecutiveDays(launches, 365)
        },

        Achievement("nosmoke_1d", R.string.ach_nosmoke_1d_title, R.string.ach_nosmoke_1d_desc, AchievementCategory.NO_SMOKE) { timeWithoutSmoking, _, _ -> 
            timeWithoutSmoking >= TimeUnit.DAYS.toMillis(1)
        },
        Achievement("nosmoke_3d", R.string.ach_nosmoke_3d_title, R.string.ach_nosmoke_3d_desc, AchievementCategory.NO_SMOKE) { timeWithoutSmoking, _, _ -> 
            timeWithoutSmoking >= TimeUnit.DAYS.toMillis(3)
        },
        Achievement("nosmoke_1w", R.string.ach_nosmoke_1w_title, R.string.ach_nosmoke_1w_desc, AchievementCategory.NO_SMOKE) { timeWithoutSmoking, _, _ -> 
            timeWithoutSmoking >= TimeUnit.DAYS.toMillis(7)
        },
        Achievement("nosmoke_1m", R.string.ach_nosmoke_1m_title, R.string.ach_nosmoke_1m_desc, AchievementCategory.NO_SMOKE) { timeWithoutSmoking, _, _ -> 
            timeWithoutSmoking >= TimeUnit.DAYS.toMillis(30)
        },
        Achievement("nosmoke_1y", R.string.ach_nosmoke_1y_title, R.string.ach_nosmoke_1y_desc, AchievementCategory.NO_SMOKE) { timeWithoutSmoking, _, _ -> 
            timeWithoutSmoking >= TimeUnit.DAYS.toMillis(365)
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
            val diff = sortedDays[i] - sortedDays[i - 1]
            val daysDiff = TimeUnit.MILLISECONDS.toDays(diff)
            if (daysDiff == 1L) {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
            } else if (daysDiff > 1L) {
                currentStreak = 1
            }
        }

        return maxStreak >= requiredConsecutiveDays
    }

    fun calculateUnlockedAchievements(entries: List<Long>, launches: List<Long>): Set<String> {
        val lastEntry = entries.maxOrNull()
        val now = System.currentTimeMillis()
        val timeWithoutSmoking = if (lastEntry != null) {
            now - lastEntry
        } else {
            0L
        }

        val unlocked = mutableSetOf<String>()
        achievementsList.forEach { achievement ->
            if (achievement.condition(timeWithoutSmoking, entries, launches)) {
                unlocked.add(achievement.id)
            }
        }
        return unlocked
    }

    fun progressFraction(achievementId: String, entries: List<Long>, launches: List<Long>): Float {
        val now = System.currentTimeMillis()
        val timeWithoutSmoking = (entries.maxOrNull()?.let { now - it } ?: 0L).coerceAtLeast(0L)
        return when (achievementId) {
            "login_1"   -> if (launches.isNotEmpty()) 1f else 0f
            "login_3"   -> consecutiveDaysFraction(launches, 3)
            "login_7"   -> consecutiveDaysFraction(launches, 7)
            "login_30"  -> consecutiveDaysFraction(launches, 30)
            "login_365" -> consecutiveDaysFraction(launches, 365)
            "nosmoke_1d" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(1)).coerceIn(0f, 1f)
            "nosmoke_3d" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(3)).coerceIn(0f, 1f)
            "nosmoke_1w" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(7)).coerceIn(0f, 1f)
            "nosmoke_1m" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(30)).coerceIn(0f, 1f)
            "nosmoke_1y" -> (timeWithoutSmoking.toFloat() / TimeUnit.DAYS.toMillis(365)).coerceIn(0f, 1f)
            else -> 0f
        }
    }

    private fun consecutiveDaysFraction(dates: List<Long>, target: Int): Float {
        if (dates.isEmpty()) return 0f
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
            val daysDiff = TimeUnit.MILLISECONDS.toDays(sortedDays[i] - sortedDays[i - 1])
            if (daysDiff == 1L) { currentStreak++; if (currentStreak > maxStreak) maxStreak = currentStreak }
            else if (daysDiff > 1L) currentStreak = 1
        }
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
}
