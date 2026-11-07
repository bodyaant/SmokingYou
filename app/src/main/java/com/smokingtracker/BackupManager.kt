package com.smokingtracker

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.smokingtracker.data.AppIconPreset
import com.smokingtracker.data.ColorPreset
import com.smokingtracker.data.FontPreset
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.data.TriggerItem
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.NotificationPreferences
import com.smokingtracker.data.preferences.ThemePreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.repository.TriggerRepository
import com.smokingtracker.notification.OngoingNotificationManager
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class BackupManager(
    private val application: Application,
    private val themePreferences: ThemePreferences,
    private val userPreferences: UserPreferences,
    private val notificationPreferences: NotificationPreferences,
    private val appMetaPreferences: AppMetaPreferences,
    private val triggerRepository: TriggerRepository,
    private val repository: SmokingRepository,
    private val achievementsCoordinator: AchievementsCoordinator,
    private val appIconManager: AppIconManager,
    private val widgetUpdateManager: WidgetUpdateManager,
    private val gson: Gson = Gson(),
    private val ongoingNotificationManager: OngoingNotificationManager? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun backup(uri: Uri): Unit = withContext(ioDispatcher) {
        val currentEntries = repository.getAllEntries()
        val snapshot = captureLocalSnapshot(currentEntries)
        val data = snapshot.toBackupData()

        val outputStream = try {
            application.contentResolver.openOutputStream(uri, "wt")
        } catch (_: Exception) {
            null
        } ?: application.contentResolver.openOutputStream(uri)
        ?: throw IOException("Failed to open OutputStream for URI: $uri")

        outputStream.use { stream ->
            BufferedWriter(OutputStreamWriter(stream, StandardCharsets.UTF_8)).use { writer ->
                gson.toJson(data, writer)
                writer.flush()
            }
        }

        appMetaPreferences.setHasMadeBackup(true)
        achievementsCoordinator.checkAndUpdateSuspend()
    }

    suspend fun restore(uri: Uri): Unit = withContext(ioDispatcher) {
        val rawJson = application.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                reader.readText()
            }
        } ?: throw IOException("Failed to open InputStream for URI: $uri")

        val data = parseAndValidateBackupJson(rawJson)
        val now = System.currentTimeMillis()
        val newEntities = sanitizeBackupEntries(data, now)

        val previousSnapshot = captureLocalSnapshot(repository.getAllEntries())

        try {
            val entriesReplaced = repository.clearAndInsertEntries(newEntities)
            if (!entriesReplaced) {
                throw IOException("Failed to write backup entries to database")
            }

            val triggersRestored = triggerRepository.restoreTriggers(
                customTriggersVal = (data.customTriggers ?: emptyList()).filterNotNull(),
                disabledDefaultTriggersVal = (data.disabledDefaultTriggers ?: emptySet()).filterNotNull().toSet()
            )
            if (!triggersRestored) {
                throw IOException("Failed to restore triggers")
            }

            val colorPresetVal = ColorPreset.fromName(data.colorPreset).name
            val fontPresetVal = FontPreset.fromName(data.fontPreset).name
            val appIconPreset = AppIconPreset.fromName(data.appIcon)
            val appIconVal = appIconPreset.name
            val themeVal = ThemePreference.fromName(data.appTheme).name

            themePreferences.restoreThemePreferences(
                theme = themeVal,
                colorPresetVal = colorPresetVal,
                fontPresetVal = fontPresetVal,
                amoledThemeVal = data.amoledTheme ?: false,
                vibrationEnabledVal = data.vibrationEnabled ?: true,
                useCustomVariableFontVal = data.useCustomVariableFont ?: false,
                customFontWeightVal = (data.customFontWeight ?: 500).coerceIn(100, 1000),
                customFontWidthVal = sanitizeFloat(data.customFontWidth, 100f).coerceIn(50f, 150f),
                customFontRoundnessVal = sanitizeFloat(data.customFontRoundness, 0f).coerceIn(0f, 100f),
                appIconVal = appIconVal,
                appLanguageTagVal = data.appLanguageTag?.trim()?.takeIf { it.isNotEmpty() }
            )

            userPreferences.restoreUserPreferences(
                limit = (data.dailyLimit ?: 0).coerceIn(0, MAX_DAILY_LIMIT),
                price = sanitizeNonNegativeFloat(data.packPrice, 0.0f),
                size = (data.packSize ?: UserPreferences.DEFAULT_PACK_SIZE)
                    .takeIf { it in 1..MAX_PACK_SIZE } ?: UserPreferences.DEFAULT_PACK_SIZE,
                curr = data.currency?.trim()?.takeIf { it.isNotEmpty() } ?: UserPreferences.DEFAULT_CURRENCY,
                taperingPlanEnabledVal = data.taperingPlanEnabled ?: false,
                taperingIntervalDaysVal = (data.taperingIntervalDays ?: UserPreferences.DEFAULT_TAPERING_INTERVAL_DAYS)
                    .takeIf { it in 1..MAX_TAPERING_DAYS } ?: UserPreferences.DEFAULT_TAPERING_INTERVAL_DAYS,
                lastTaperingCheckinDateVal = sanitizeTimestampOrZero(data.lastTaperingCheckinDate, now),
                hasHistoricalBaselineVal = data.hasHistoricalBaseline ?: false,
                historicalStartDateVal = sanitizeTimestampOrZero(data.historicalStartDate, now),
                historicalDailyAvgVal = (data.historicalDailyAvg ?: 0).coerceIn(0, MAX_DAILY_LIMIT),
                historicalPackPriceVal = sanitizeNonNegativeFloat(data.historicalPackPrice, 0f),
                historicalPackSizeVal = (data.historicalPackSize ?: UserPreferences.DEFAULT_PACK_SIZE)
                    .takeIf { it in 1..MAX_PACK_SIZE } ?: UserPreferences.DEFAULT_PACK_SIZE,
                historicalTriggerPrioritiesVal = (data.historicalTriggerPriorities ?: emptyList())
                    .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) },
                savingsGoalTitleVal = data.savingsGoalTitle?.trim().orEmpty(),
                savingsGoalAmountVal = sanitizeNonNegativeFloat(data.savingsGoalAmount, 0f),
                widgetShowResistButtonVal = data.widgetShowResistButton ?: true,
                showLimitOnGraphVal = data.showLimitOnGraph ?: true
            )

            notificationPreferences.restoreNotificationPreferences(
                notificationEnabledVal = data.notificationEnabled ?: false,
                notificationLowPriorityVal = data.notificationLowPriority ?: true,
                notificationShowTimerVal = data.notificationShowTimer ?: true,
                notificationShowProgressVal = data.notificationShowProgress ?: true,
                notificationShowAddButtonVal = data.notificationShowAddButton ?: true,
                notificationShowResistButtonVal = data.notificationShowResistButton ?: false,
                widgetShowResistButtonVal = data.widgetShowResistButton ?: true,
                showLimitOnGraphVal = data.showLimitOnGraph ?: true
            )

            val sanitizedAchievements = (data.unlockedAchievements ?: emptySet())
                .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
                .toSet()

            val sanitizedUnlockDates = (data.achievementUnlockDates ?: emptyMap())
                .entries
                .mapNotNull { (k, v) ->
                    val key = k?.trim()
                    if (!key.isNullOrEmpty() && v != null && v in 1L..now + MAX_FUTURE_DRIFT_MS) {
                        key to v
                    } else {
                        null
                    }
                }
                .toMap()

            val sanitizedLaunches = (data.appLaunchDates ?: emptyList())
                .mapNotNull { ts -> ts?.takeIf { it in 1L..now + MAX_FUTURE_DRIFT_MS } }

            appMetaPreferences.restoreAppMetaPreferences(
                isReg = data.isRegistered ?: true,
                achievements = sanitizedAchievements,
                achievementUnlockDatesVal = sanitizedUnlockDates,
                hasBackupVal = data.hasMadeBackup ?: false,
                hasPriceChangedVal = data.hasChangedPackPrice ?: false,
                hasCancelled10sVal = data.hasCancelledWithin10s ?: false,
                launchesVal = sanitizedLaunches,
                checkUpdatesOnStartVal = data.checkUpdatesOnStart ?: false,
                themeLangChangeCountVal = (data.themeLangChangeCount ?: 0).coerceAtLeast(0),
                themeLangChangeDateVal = sanitizeTimestampOrZero(data.themeLangChangeDate, now),
                analyticsVisitCountVal = (data.analyticsVisitCount ?: 0).coerceAtLeast(0),
                analyticsVisitDateVal = sanitizeTimestampOrZero(data.analyticsVisitDate, now)
            )

            appIconManager.applyIcon(appIconPreset)
            achievementsCoordinator.checkAndUpdateSuspend()
            if (ongoingNotificationManager != null) {
                ongoingNotificationManager.updateSuspend(application)
            } else {
                OngoingNotificationManager.updateSuspend(application)
            }
            widgetUpdateManager.updateAll(application)
        } catch (e: CancellationException) {
            runCatching { rollbackToSnapshot(previousSnapshot) }
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed, rolling back to previous snapshot", e)
            runCatching { rollbackToSnapshot(previousSnapshot) }
            throw e
        }
    }

    internal fun parseAndValidateBackupJson(rawJson: String): BackupData {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("Backup file is empty")
        }

        val jsonElement = try {
            JsonParser.parseString(trimmed)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid JSON format in backup file", e)
        }

        if (!jsonElement.isJsonObject) {
            throw IllegalArgumentException("Backup root must be a JSON object")
        }

        val jsonObj: JsonObject = jsonElement.asJsonObject
        val hasEntriesField = jsonObj.has("entries") && !jsonObj.get("entries").isJsonNull
        val hasLegacyEntriesField = jsonObj.has("smokingEntries") && !jsonObj.get("smokingEntries").isJsonNull
        if (!hasEntriesField && !hasLegacyEntriesField) {
            throw IllegalArgumentException("Backup JSON is missing required 'entries' or 'smokingEntries' field")
        }

        val hasKnownMetadata = jsonObj.has("isRegistered") ||
            jsonObj.has("appTheme") ||
            jsonObj.has("unlockedAchievements") ||
            jsonObj.has("dailyLimit") ||
            jsonObj.has("packPrice") ||
            jsonObj.has("version")
        if (!hasKnownMetadata) {
            throw IllegalArgumentException("Unrecognized backup schema: missing core application metadata")
        }

        val data = try {
            gson.fromJson(jsonObj, BackupData::class.java)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to deserialize BackupData", e)
        } ?: throw IllegalArgumentException("Backup data is null")

        val version = data.version ?: CURRENT_BACKUP_VERSION
        if (version !in 1..CURRENT_BACKUP_VERSION) {
            throw IllegalArgumentException("Unsupported backup version: $version (max supported: $CURRENT_BACKUP_VERSION)")
        }

        return data
    }

    internal fun sanitizeBackupEntries(data: BackupData, nowMillis: Long = System.currentTimeMillis()): List<SmokingEntryEntity> {
        val maxAllowedTimestamp = nowMillis + MAX_FUTURE_DRIFT_MS

        val parsedLegacyTriggers: Map<Long, String> = data.entryTriggers
            ?.entries
            ?.mapNotNull { (k, v) ->
                val ts = k?.trim()?.toLongOrNull()
                val sanitizedTrigger = sanitizeTriggerValue(v)
                if (ts != null && ts in 1L..maxAllowedTimestamp && sanitizedTrigger != null) {
                    ts to sanitizedTrigger
                } else {
                    null
                }
            }
            ?.toMap()
            ?: emptyMap()

        val rawEntries: List<BackupEntry> = when {
            data.entries != null -> data.entries.filterNotNull()
            data.smokingEntries != null -> data.smokingEntries.filterNotNull().map { ts ->
                BackupEntry(
                    timestamp = ts,
                    trigger = parsedLegacyTriggers[ts],
                    isResisted = false
                )
            }
            else -> throw IllegalArgumentException("Backup contains neither 'entries' nor 'smokingEntries'")
        }

        return rawEntries
            .asSequence()
            .mapNotNull { entry ->
                val ts = entry.timestamp ?: return@mapNotNull null
                if (ts !in 1L..maxAllowedTimestamp) return@mapNotNull null
                val cleanTrigger = sanitizeTriggerValue(entry.trigger)
                val resisted = entry.isResisted ?: false
                SmokingEntryEntity(
                    timestamp = ts,
                    trigger = cleanTrigger,
                    isResisted = resisted
                )
            }
            .distinctBy { Triple(it.timestamp, it.isResisted, it.trigger) }
            .sortedBy { it.timestamp }
            .toList()
    }

    private fun sanitizeTriggerValue(raw: String?): String? {
        val normalized = raw?.let(TriggerItem::normalizeCustomName)?.takeIf { it.isNotEmpty() }
            ?: return null
        return TriggerType.fromKey(normalized)?.key ?: normalized
    }

    private fun sanitizeFloat(value: Float?, defaultVal: Float): Float {
        return if (value != null && value.isFinite()) value else defaultVal
    }

    private fun sanitizeNonNegativeFloat(value: Float?, defaultVal: Float): Float {
        return if (value != null && value.isFinite() && value >= 0f) value else defaultVal
    }

    private fun sanitizeTimestampOrZero(value: Long?, nowMillis: Long): Long {
        if (value == null || value <= 0L) return 0L
        return if (value <= nowMillis + MAX_FUTURE_DRIFT_MS) value else nowMillis
    }

    private suspend fun captureLocalSnapshot(entries: List<SmokingEntryEntity>): LocalSnapshot {
        val meta = appMetaPreferences.getAchievementMetaSnapshot()
        return LocalSnapshot(
            entries = entries,
            isRegistered = meta.isRegistered,
            unlockedAchievements = meta.unlockedAchievements,
            achievementUnlockDates = meta.achievementUnlockDates,
            hasMadeBackup = meta.hasMadeBackup,
            hasChangedPackPrice = meta.hasChangedPackPrice,
            hasCancelledWithin10s = meta.hasCancelledWithin10s,
            appLaunchDates = meta.appLaunchDates,
            checkUpdatesOnStart = appMetaPreferences.checkUpdatesOnStart.first(),
            themeLangChangeCount = meta.themeLangChangeCount,
            themeLangChangeDate = meta.themeLangChangeDate,
            analyticsVisitCount = meta.analyticsVisitCount,
            analyticsVisitDate = meta.analyticsVisitDate,
            appTheme = themePreferences.appTheme.first().name,
            colorPreset = themePreferences.colorPreset.first().name,
            fontPreset = themePreferences.fontPreset.first().name,
            amoledTheme = themePreferences.amoledTheme.first(),
            vibrationEnabled = themePreferences.vibrationEnabled.first(),
            useCustomVariableFont = themePreferences.useCustomVariableFont.first(),
            customFontWeight = themePreferences.customFontWeight.first(),
            customFontWidth = themePreferences.customFontWidth.first(),
            customFontRoundness = themePreferences.customFontRoundness.first(),
            appIcon = themePreferences.appIcon.first().name,
            appLanguageTag = themePreferences.appLanguageTag.first(),
            dailyLimit = userPreferences.dailyLimit.first(),
            packPrice = userPreferences.packPrice.first(),
            packSize = userPreferences.packSize.first(),
            currency = userPreferences.currency.first(),
            taperingPlanEnabled = userPreferences.taperingPlanEnabled.first(),
            taperingIntervalDays = userPreferences.taperingIntervalDays.first(),
            lastTaperingCheckinDate = userPreferences.lastTaperingCheckinDate.first(),
            hasHistoricalBaseline = userPreferences.hasHistoricalBaseline.first(),
            historicalStartDate = userPreferences.historicalStartDate.first(),
            historicalDailyAvg = userPreferences.historicalDailyAvg.first(),
            historicalPackPrice = userPreferences.historicalPackPrice.first(),
            historicalPackSize = userPreferences.historicalPackSize.first(),
            historicalTriggerPriorities = userPreferences.historicalTriggerPriorities.first(),
            savingsGoalTitle = userPreferences.savingsGoalTitle.first(),
            savingsGoalAmount = userPreferences.savingsGoalAmount.first(),
            widgetShowResistButton = userPreferences.widgetShowResistButton.first(),
            showLimitOnGraph = userPreferences.showLimitOnGraph.first(),
            customTriggers = triggerRepository.customTriggers.first(),
            disabledDefaultTriggers = triggerRepository.disabledDefaultTriggers.first(),
            notificationEnabled = notificationPreferences.ongoingNotificationEnabled.first(),
            notificationLowPriority = notificationPreferences.notificationLowPriority.first(),
            notificationShowTimer = notificationPreferences.notificationShowTimer.first(),
            notificationShowProgress = notificationPreferences.notificationShowProgress.first(),
            notificationShowAddButton = notificationPreferences.notificationShowAddButton.first(),
            notificationShowResistButton = notificationPreferences.notificationShowResistButton.first()
        )
    }

    private suspend fun rollbackToSnapshot(snapshot: LocalSnapshot) {
        repository.clearAndInsertEntries(snapshot.entries)
        triggerRepository.restoreTriggers(
            customTriggersVal = snapshot.customTriggers,
            disabledDefaultTriggersVal = snapshot.disabledDefaultTriggers
        )
        themePreferences.restoreThemePreferences(
            theme = snapshot.appTheme,
            colorPresetVal = snapshot.colorPreset,
            fontPresetVal = snapshot.fontPreset,
            amoledThemeVal = snapshot.amoledTheme,
            vibrationEnabledVal = snapshot.vibrationEnabled,
            useCustomVariableFontVal = snapshot.useCustomVariableFont,
            customFontWeightVal = snapshot.customFontWeight,
            customFontWidthVal = snapshot.customFontWidth,
            customFontRoundnessVal = snapshot.customFontRoundness,
            appIconVal = snapshot.appIcon,
            appLanguageTagVal = snapshot.appLanguageTag
        )
        userPreferences.restoreUserPreferences(
            limit = snapshot.dailyLimit,
            price = snapshot.packPrice,
            size = snapshot.packSize,
            curr = snapshot.currency,
            taperingPlanEnabledVal = snapshot.taperingPlanEnabled,
            taperingIntervalDaysVal = snapshot.taperingIntervalDays,
            lastTaperingCheckinDateVal = snapshot.lastTaperingCheckinDate,
            hasHistoricalBaselineVal = snapshot.hasHistoricalBaseline,
            historicalStartDateVal = snapshot.historicalStartDate,
            historicalDailyAvgVal = snapshot.historicalDailyAvg,
            historicalPackPriceVal = snapshot.historicalPackPrice,
            historicalPackSizeVal = snapshot.historicalPackSize,
            historicalTriggerPrioritiesVal = snapshot.historicalTriggerPriorities,
            savingsGoalTitleVal = snapshot.savingsGoalTitle,
            savingsGoalAmountVal = snapshot.savingsGoalAmount,
            widgetShowResistButtonVal = snapshot.widgetShowResistButton,
            showLimitOnGraphVal = snapshot.showLimitOnGraph
        )
        notificationPreferences.restoreNotificationPreferences(
            notificationEnabledVal = snapshot.notificationEnabled,
            notificationLowPriorityVal = snapshot.notificationLowPriority,
            notificationShowTimerVal = snapshot.notificationShowTimer,
            notificationShowProgressVal = snapshot.notificationShowProgress,
            notificationShowAddButtonVal = snapshot.notificationShowAddButton,
            notificationShowResistButtonVal = snapshot.notificationShowResistButton,
            widgetShowResistButtonVal = snapshot.widgetShowResistButton,
            showLimitOnGraphVal = snapshot.showLimitOnGraph
        )
        appMetaPreferences.restoreAppMetaPreferences(
            isReg = snapshot.isRegistered,
            achievements = snapshot.unlockedAchievements,
            achievementUnlockDatesVal = snapshot.achievementUnlockDates,
            hasBackupVal = snapshot.hasMadeBackup,
            hasPriceChangedVal = snapshot.hasChangedPackPrice,
            hasCancelled10sVal = snapshot.hasCancelledWithin10s,
            launchesVal = snapshot.appLaunchDates,
            checkUpdatesOnStartVal = snapshot.checkUpdatesOnStart,
            themeLangChangeCountVal = snapshot.themeLangChangeCount,
            themeLangChangeDateVal = snapshot.themeLangChangeDate,
            analyticsVisitCountVal = snapshot.analyticsVisitCount,
            analyticsVisitDateVal = snapshot.analyticsVisitDate
        )
    }

    private data class LocalSnapshot(
        val entries: List<SmokingEntryEntity>,
        val isRegistered: Boolean,
        val unlockedAchievements: Set<String>,
        val achievementUnlockDates: Map<String, Long>,
        val hasMadeBackup: Boolean,
        val hasChangedPackPrice: Boolean,
        val hasCancelledWithin10s: Boolean,
        val appLaunchDates: List<Long>,
        val checkUpdatesOnStart: Boolean,
        val themeLangChangeCount: Int,
        val themeLangChangeDate: Long,
        val analyticsVisitCount: Int,
        val analyticsVisitDate: Long,
        val appTheme: String,
        val colorPreset: String,
        val fontPreset: String,
        val amoledTheme: Boolean,
        val vibrationEnabled: Boolean,
        val useCustomVariableFont: Boolean,
        val customFontWeight: Int,
        val customFontWidth: Float,
        val customFontRoundness: Float,
        val appIcon: String,
        val appLanguageTag: String?,
        val dailyLimit: Int,
        val packPrice: Float,
        val packSize: Int,
        val currency: String,
        val taperingPlanEnabled: Boolean,
        val taperingIntervalDays: Int,
        val lastTaperingCheckinDate: Long,
        val hasHistoricalBaseline: Boolean,
        val historicalStartDate: Long,
        val historicalDailyAvg: Int,
        val historicalPackPrice: Float,
        val historicalPackSize: Int,
        val historicalTriggerPriorities: List<String>,
        val savingsGoalTitle: String,
        val savingsGoalAmount: Float,
        val widgetShowResistButton: Boolean,
        val showLimitOnGraph: Boolean,
        val customTriggers: List<String>,
        val disabledDefaultTriggers: Set<String>,
        val notificationEnabled: Boolean,
        val notificationLowPriority: Boolean,
        val notificationShowTimer: Boolean,
        val notificationShowProgress: Boolean,
        val notificationShowAddButton: Boolean,
        val notificationShowResistButton: Boolean
    ) {
        fun toBackupData(): BackupData = BackupData(
            version = CURRENT_BACKUP_VERSION,
            isRegistered = isRegistered,
            entries = entries.map {
                BackupEntry(timestamp = it.timestamp, trigger = it.trigger, isResisted = it.isResisted)
            },
            appTheme = appTheme,
            unlockedAchievements = unlockedAchievements,
            achievementUnlockDates = achievementUnlockDates,
            dailyLimit = dailyLimit,
            packPrice = packPrice,
            packSize = packSize,
            currency = currency,
            colorPreset = colorPreset,
            fontPreset = fontPreset,
            amoledTheme = amoledTheme,
            vibrationEnabled = vibrationEnabled,
            hasMadeBackup = hasMadeBackup,
            hasChangedPackPrice = hasChangedPackPrice,
            hasCancelledWithin10s = hasCancelledWithin10s,
            appLaunchDates = appLaunchDates,
            useCustomVariableFont = useCustomVariableFont,
            customFontWeight = customFontWeight,
            customFontWidth = customFontWidth,
            customFontRoundness = customFontRoundness,
            taperingPlanEnabled = taperingPlanEnabled,
            taperingIntervalDays = taperingIntervalDays,
            lastTaperingCheckinDate = lastTaperingCheckinDate,
            hasHistoricalBaseline = hasHistoricalBaseline,
            historicalStartDate = historicalStartDate,
            historicalDailyAvg = historicalDailyAvg,
            historicalPackPrice = historicalPackPrice,
            historicalPackSize = historicalPackSize,
            historicalTriggerPriorities = historicalTriggerPriorities,
            savingsGoalTitle = savingsGoalTitle,
            savingsGoalAmount = savingsGoalAmount,
            widgetShowResistButton = widgetShowResistButton,
            showLimitOnGraph = showLimitOnGraph,
            appIcon = appIcon,
            appLanguageTag = appLanguageTag,
            checkUpdatesOnStart = checkUpdatesOnStart,
            themeLangChangeCount = themeLangChangeCount,
            themeLangChangeDate = themeLangChangeDate,
            analyticsVisitCount = analyticsVisitCount,
            analyticsVisitDate = analyticsVisitDate,
            customTriggers = customTriggers,
            disabledDefaultTriggers = disabledDefaultTriggers,
            notificationEnabled = notificationEnabled,
            notificationLowPriority = notificationLowPriority,
            notificationShowTimer = notificationShowTimer,
            notificationShowProgress = notificationShowProgress,
            notificationShowAddButton = notificationShowAddButton,
            notificationShowResistButton = notificationShowResistButton
        )
    }

    @Keep
    data class BackupEntry(
        @SerializedName("timestamp") val timestamp: Long? = null,
        @SerializedName("trigger") val trigger: String? = null,
        @SerializedName("isResisted") val isResisted: Boolean? = false
    )

    @Keep
    data class BackupData(
        @SerializedName("version") val version: Int? = CURRENT_BACKUP_VERSION,
        @SerializedName("isRegistered") val isRegistered: Boolean? = true,
        @SerializedName("entries") val entries: List<BackupEntry?>? = null,
        @SerializedName("smokingEntries") val smokingEntries: List<Long?>? = null,
        @SerializedName("appTheme") val appTheme: String? = "SYSTEM",
        @SerializedName("unlockedAchievements") val unlockedAchievements: Set<String?>? = emptySet(),
        @SerializedName("achievementUnlockDates") val achievementUnlockDates: Map<out String?, Long?>? = null,
        @SerializedName("dailyLimit") val dailyLimit: Int? = 0,
        @SerializedName("packPrice") val packPrice: Float? = 0.0f,
        @SerializedName("packSize") val packSize: Int? = UserPreferences.DEFAULT_PACK_SIZE,
        @SerializedName("currency") val currency: String? = UserPreferences.DEFAULT_CURRENCY,
        @SerializedName("colorPreset") val colorPreset: String? = "SYSTEM",
        @SerializedName("entryTriggers") val entryTriggers: Map<out String?, String?>? = emptyMap(),
        @SerializedName("fontPreset") val fontPreset: String? = "SYSTEM",
        @SerializedName("amoledTheme") val amoledTheme: Boolean? = false,
        @SerializedName("vibrationEnabled") val vibrationEnabled: Boolean? = true,
        @SerializedName("hasMadeBackup") val hasMadeBackup: Boolean? = false,
        @SerializedName("hasChangedPackPrice") val hasChangedPackPrice: Boolean? = false,
        @SerializedName("hasCancelledWithin10s") val hasCancelledWithin10s: Boolean? = false,
        @SerializedName("appLaunchDates") val appLaunchDates: List<Long?>? = emptyList(),
        @SerializedName("useCustomVariableFont") val useCustomVariableFont: Boolean? = false,
        @SerializedName("customFontWeight") val customFontWeight: Int? = 500,
        @SerializedName("customFontWidth") val customFontWidth: Float? = 100f,
        @SerializedName("customFontRoundness") val customFontRoundness: Float? = 0f,
        @SerializedName("taperingPlanEnabled") val taperingPlanEnabled: Boolean? = false,
        @SerializedName("taperingIntervalDays") val taperingIntervalDays: Int? = UserPreferences.DEFAULT_TAPERING_INTERVAL_DAYS,
        @SerializedName("lastTaperingCheckinDate") val lastTaperingCheckinDate: Long? = 0L,
        @SerializedName("hasHistoricalBaseline") val hasHistoricalBaseline: Boolean? = false,
        @SerializedName("historicalStartDate") val historicalStartDate: Long? = 0L,
        @SerializedName("historicalDailyAvg") val historicalDailyAvg: Int? = 0,
        @SerializedName("historicalPackPrice") val historicalPackPrice: Float? = 0f,
        @SerializedName("historicalPackSize") val historicalPackSize: Int? = UserPreferences.DEFAULT_PACK_SIZE,
        @SerializedName("historicalTriggerPriorities") val historicalTriggerPriorities: List<String?>? = emptyList(),
        @SerializedName("savingsGoalTitle") val savingsGoalTitle: String? = "",
        @SerializedName("savingsGoalAmount") val savingsGoalAmount: Float? = 0f,
        @SerializedName("widgetShowResistButton") val widgetShowResistButton: Boolean? = true,
        @SerializedName("showLimitOnGraph") val showLimitOnGraph: Boolean? = true,
        @SerializedName("appIcon") val appIcon: String? = "DEFAULT",
        @SerializedName("appLanguageTag") val appLanguageTag: String? = null,
        @SerializedName("checkUpdatesOnStart") val checkUpdatesOnStart: Boolean? = false,
        @SerializedName("themeLangChangeCount") val themeLangChangeCount: Int? = 0,
        @SerializedName("themeLangChangeDate") val themeLangChangeDate: Long? = 0L,
        @SerializedName("analyticsVisitCount") val analyticsVisitCount: Int? = 0,
        @SerializedName("analyticsVisitDate") val analyticsVisitDate: Long? = 0L,
        @SerializedName("customTriggers") val customTriggers: List<String?>? = emptyList(),
        @SerializedName("disabledDefaultTriggers") val disabledDefaultTriggers: Set<String?>? = emptySet(),
        @SerializedName("notificationEnabled") val notificationEnabled: Boolean? = false,
        @SerializedName("notificationLowPriority") val notificationLowPriority: Boolean? = true,
        @SerializedName("notificationShowTimer") val notificationShowTimer: Boolean? = true,
        @SerializedName("notificationShowProgress") val notificationShowProgress: Boolean? = true,
        @SerializedName("notificationShowAddButton") val notificationShowAddButton: Boolean? = true,
        @SerializedName("notificationShowResistButton") val notificationShowResistButton: Boolean? = false
    )

    companion object {
        private const val TAG = "BackupManager"
        const val CURRENT_BACKUP_VERSION = 3
        private const val MAX_FUTURE_DRIFT_MS = 24L * 60L * 60L * 1000L // Allow up to 24h timezone drift
        private const val MAX_DAILY_LIMIT = 500
        private const val MAX_PACK_SIZE = 1000
        private const val MAX_TAPERING_DAYS = 365
    }
}
