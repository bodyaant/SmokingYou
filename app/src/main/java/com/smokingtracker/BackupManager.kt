package com.smokingtracker

import android.app.Application
import android.net.Uri
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.smokingtracker.data.AppIconPreset
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.NotificationPreferences
import com.smokingtracker.data.preferences.ThemePreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.repository.TriggerRepository
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupManager(
    private val application: Application,
    private val themePreferences: ThemePreferences,
    private val userPreferences: UserPreferences,
    private val notificationPreferences: NotificationPreferences,
    private val appMetaPreferences: AppMetaPreferences,
    private val triggerRepository: TriggerRepository,
    private val repository: SmokingRepository,
    private val achievementsCoordinator: AchievementsCoordinator,
    private val appIconManager: AppIconManager
) {
    private val gson = Gson()

    suspend fun backup(uri: Uri) {
        val currentEntries = repository.smokingEntries.first()

        coroutineScope {
            val isRegisteredDeferred = async { appMetaPreferences.isRegistered.first() }
            val appThemeDeferred = async { themePreferences.appTheme.first() }
            val unlockedAchievementsDeferred = async { appMetaPreferences.unlockedAchievements.first() }
            val achievementUnlockDatesDeferred = async { appMetaPreferences.achievementUnlockDates.first() }
            val dailyLimitDeferred = async { userPreferences.dailyLimit.first() }
            val packPriceDeferred = async { userPreferences.packPrice.first() }
            val packSizeDeferred = async { userPreferences.packSize.first() }
            val currencyDeferred = async { userPreferences.currency.first() }
            val colorPresetDeferred = async { themePreferences.colorPreset.first() }
            val fontPresetDeferred = async { themePreferences.fontPreset.first() }
            val amoledThemeDeferred = async { themePreferences.amoledTheme.first() }
            val vibrationEnabledDeferred = async { themePreferences.vibrationEnabled.first() }
            val hasMadeBackupDeferred = async { appMetaPreferences.hasMadeBackup.first() }
            val hasChangedPackPriceDeferred = async { appMetaPreferences.hasChangedPackPrice.first() }
            val hasCancelledWithin10sDeferred = async { appMetaPreferences.hasCancelledWithin10s.first() }
            val appLaunchDatesDeferred = async { appMetaPreferences.appLaunchDates.first() }
            val useCustomVariableFontDeferred = async { themePreferences.useCustomVariableFont.first() }
            val customFontWeightDeferred = async { themePreferences.customFontWeight.first() }
            val customFontWidthDeferred = async { themePreferences.customFontWidth.first() }
            val customFontRoundnessDeferred = async { themePreferences.customFontRoundness.first() }
            val taperingPlanEnabledDeferred = async { userPreferences.taperingPlanEnabled.first() }
            val taperingIntervalDaysDeferred = async { userPreferences.taperingIntervalDays.first() }
            val lastTaperingCheckinDateDeferred = async { userPreferences.lastTaperingCheckinDate.first() }
            val hasHistoricalBaselineDeferred = async { userPreferences.hasHistoricalBaseline.first() }
            val historicalStartDateDeferred = async { userPreferences.historicalStartDate.first() }
            val historicalDailyAvgDeferred = async { userPreferences.historicalDailyAvg.first() }
            val historicalPackPriceDeferred = async { userPreferences.historicalPackPrice.first() }
            val historicalPackSizeDeferred = async { userPreferences.historicalPackSize.first() }
            val historicalTriggerPrioritiesDeferred = async { userPreferences.historicalTriggerPriorities.first() }
            val appIconDeferred = async { themePreferences.appIcon.first() }
            val checkUpdatesOnStartDeferred = async { appMetaPreferences.checkUpdatesOnStart.first() }
            val customTriggersDeferred = async { triggerRepository.customTriggers.first() }
            val disabledDefaultTriggersDeferred = async { triggerRepository.disabledDefaultTriggers.first() }
            val notificationEnabledDeferred = async { notificationPreferences.ongoingNotificationEnabled.first() }
            val notificationLowPriorityDeferred = async { notificationPreferences.notificationLowPriority.first() }
            val notificationShowTimerDeferred = async { notificationPreferences.notificationShowTimer.first() }
            val notificationShowProgressDeferred = async { notificationPreferences.notificationShowProgress.first() }
            val notificationShowAddButtonDeferred = async { notificationPreferences.notificationShowAddButton.first() }
            val notificationShowResistButtonDeferred = async { notificationPreferences.notificationShowResistButton.first() }

            val data = BackupData(
                isRegistered = isRegisteredDeferred.await(),
                entries = currentEntries.map {
                    BackupEntry(timestamp = it.timestamp, trigger = it.trigger, isResisted = it.isResisted)
                },
                appTheme = appThemeDeferred.await().name,
                unlockedAchievements = unlockedAchievementsDeferred.await(),
                achievementUnlockDates = achievementUnlockDatesDeferred.await(),
                dailyLimit = dailyLimitDeferred.await(),
                packPrice = packPriceDeferred.await(),
                packSize = packSizeDeferred.await(),
                currency = currencyDeferred.await(),
                colorPreset = colorPresetDeferred.await().name,
                fontPreset = fontPresetDeferred.await().name,
                amoledTheme = amoledThemeDeferred.await(),
                vibrationEnabled = vibrationEnabledDeferred.await(),
                hasMadeBackup = hasMadeBackupDeferred.await(),
                hasChangedPackPrice = hasChangedPackPriceDeferred.await(),
                hasCancelledWithin10s = hasCancelledWithin10sDeferred.await(),
                appLaunchDates = appLaunchDatesDeferred.await(),
                useCustomVariableFont = useCustomVariableFontDeferred.await(),
                customFontWeight = customFontWeightDeferred.await(),
                customFontWidth = customFontWidthDeferred.await(),
                customFontRoundness = customFontRoundnessDeferred.await(),
                taperingPlanEnabled = taperingPlanEnabledDeferred.await(),
                taperingIntervalDays = taperingIntervalDaysDeferred.await(),
                lastTaperingCheckinDate = lastTaperingCheckinDateDeferred.await(),
                hasHistoricalBaseline = hasHistoricalBaselineDeferred.await(),
                historicalStartDate = historicalStartDateDeferred.await(),
                historicalDailyAvg = historicalDailyAvgDeferred.await(),
                historicalPackPrice = historicalPackPriceDeferred.await(),
                historicalPackSize = historicalPackSizeDeferred.await(),
                historicalTriggerPriorities = historicalTriggerPrioritiesDeferred.await(),
                appIcon = appIconDeferred.await().name,
                checkUpdatesOnStart = checkUpdatesOnStartDeferred.await(),
                customTriggers = customTriggersDeferred.await(),
                disabledDefaultTriggers = disabledDefaultTriggersDeferred.await(),
                notificationEnabled = notificationEnabledDeferred.await(),
                notificationLowPriority = notificationLowPriorityDeferred.await(),
                notificationShowTimer = notificationShowTimerDeferred.await(),
                notificationShowProgress = notificationShowProgressDeferred.await(),
                notificationShowAddButton = notificationShowAddButtonDeferred.await(),
                notificationShowResistButton = notificationShowResistButtonDeferred.await()
            )

            application.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    gson.toJson(data, writer)
                }
            } ?: throw IllegalStateException("Failed to open OutputStream for URI: $uri")
        }

        appMetaPreferences.setHasMadeBackup(true)
        achievementsCoordinator.checkAndUpdate()
    }

    suspend fun restore(uri: Uri) {
        application.contentResolver.openInputStream(uri)?.use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                val data = gson.fromJson(reader, BackupData::class.java)
                    ?: throw IllegalStateException("Backup data is null or corrupted")

                val colorPresetVal = data.colorPreset ?: "SYSTEM"
                val fontPresetVal = data.fontPreset ?: "SYSTEM"
                val appIconVal = data.appIcon ?: AppIconPreset.DEFAULT.name

                themePreferences.restoreThemePreferences(
                    theme = data.appTheme,
                    colorPresetVal = colorPresetVal,
                    fontPresetVal = fontPresetVal,
                    amoledThemeVal = data.amoledTheme ?: false,
                    vibrationEnabledVal = data.vibrationEnabled ?: true,
                    useCustomVariableFontVal = data.useCustomVariableFont ?: false,
                    customFontWeightVal = data.customFontWeight ?: 500,
                    customFontWidthVal = data.customFontWidth ?: 100f,
                    customFontRoundnessVal = data.customFontRoundness ?: 0f,
                    appIconVal = appIconVal
                )

                userPreferences.restoreUserPreferences(
                    limit = data.dailyLimit ?: 0,
                    price = data.packPrice ?: 0.0f,
                    size = data.packSize ?: 20,
                    curr = data.currency ?: "USD",
                    taperingPlanEnabledVal = data.taperingPlanEnabled ?: false,
                    taperingIntervalDaysVal = data.taperingIntervalDays ?: 7,
                    lastTaperingCheckinDateVal = data.lastTaperingCheckinDate ?: 0L,
                    hasHistoricalBaselineVal = data.hasHistoricalBaseline ?: false,
                    historicalStartDateVal = data.historicalStartDate ?: 0L,
                    historicalDailyAvgVal = data.historicalDailyAvg ?: 0,
                    historicalPackPriceVal = data.historicalPackPrice ?: 0f,
                    historicalPackSizeVal = data.historicalPackSize ?: 20,
                    historicalTriggerPrioritiesVal = data.historicalTriggerPriorities ?: emptyList()
                )

                notificationPreferences.restoreNotificationPreferences(
                    notificationEnabledVal = data.notificationEnabled ?: false,
                    notificationLowPriorityVal = data.notificationLowPriority ?: true,
                    notificationShowTimerVal = data.notificationShowTimer ?: true,
                    notificationShowProgressVal = data.notificationShowProgress ?: true,
                    notificationShowAddButtonVal = data.notificationShowAddButton ?: true,
                    notificationShowResistButtonVal = data.notificationShowResistButton ?: false
                )

                appMetaPreferences.restoreAppMetaPreferences(
                    isReg = data.isRegistered,
                    achievements = data.unlockedAchievements,
                    achievementUnlockDatesVal = data.achievementUnlockDates ?: emptyMap(),
                    hasBackupVal = data.hasMadeBackup ?: false,
                    hasPriceChangedVal = data.hasChangedPackPrice ?: false,
                    hasCancelled10sVal = data.hasCancelledWithin10s ?: false,
                    launchesVal = data.appLaunchDates ?: emptyList(),
                    checkUpdatesOnStartVal = data.checkUpdatesOnStart ?: false
                )

                triggerRepository.restoreTriggers(
                    customTriggersVal = data.customTriggers ?: emptyList(),
                    disabledDefaultTriggersVal = data.disabledDefaultTriggers ?: emptySet()
                )

                val backupEntries = data.entries ?: data.smokingEntries?.map { ts ->
                    BackupEntry(timestamp = ts, trigger = data.entryTriggers?.get(ts), isResisted = false)
                } ?: emptyList()

                val newEntities = backupEntries.map { entry ->
                    SmokingEntryEntity(
                        timestamp = entry.timestamp,
                        trigger = entry.trigger,
                        isResisted = entry.isResisted
                    )
                }
                repository.clearAndInsertEntries(newEntities)

                val appIconPreset = try {
                    AppIconPreset.valueOf(appIconVal)
                } catch (e: Exception) {
                    AppIconPreset.DEFAULT
                }
                appIconManager.applyIcon(appIconPreset)

                WidgetUpdateManager.updateAllAsync(application)
            }
        } ?: throw IllegalStateException("Failed to open InputStream for URI: $uri")
    }

    @Keep
    data class BackupEntry(
        @SerializedName("timestamp") val timestamp: Long,
        @SerializedName("trigger") val trigger: String? = null,
        @SerializedName("isResisted") val isResisted: Boolean = false
    )

    @Keep
    data class BackupData(
        @SerializedName("version") val version: Int = 3,
        @SerializedName("isRegistered") val isRegistered: Boolean,
        @SerializedName("entries") val entries: List<BackupEntry>? = null,
        @SerializedName("smokingEntries") val smokingEntries: List<Long>? = null,
        @SerializedName("appTheme") val appTheme: String,
        @SerializedName("unlockedAchievements") val unlockedAchievements: Set<String>,
        @SerializedName("achievementUnlockDates") val achievementUnlockDates: Map<String, Long>? = null,
        @SerializedName("dailyLimit") val dailyLimit: Int? = 0,
        @SerializedName("packPrice") val packPrice: Float? = 0.0f,
        @SerializedName("packSize") val packSize: Int? = 20,
        @SerializedName("currency") val currency: String? = "USD",
        @SerializedName("colorPreset") val colorPreset: String? = "SYSTEM",
        @SerializedName("entryTriggers") val entryTriggers: Map<Long, String>? = emptyMap(),
        @SerializedName("fontPreset") val fontPreset: String? = "SYSTEM",
        @SerializedName("amoledTheme") val amoledTheme: Boolean? = false,
        @SerializedName("vibrationEnabled") val vibrationEnabled: Boolean? = true,
        @SerializedName("hasMadeBackup") val hasMadeBackup: Boolean? = false,
        @SerializedName("hasChangedPackPrice") val hasChangedPackPrice: Boolean? = false,
        @SerializedName("hasCancelledWithin10s") val hasCancelledWithin10s: Boolean? = false,
        @SerializedName("appLaunchDates") val appLaunchDates: List<Long>? = emptyList(),
        @SerializedName("containerStyle") val containerStyle: String? = "EXPRESSIVE",
        @SerializedName("useCustomVariableFont") val useCustomVariableFont: Boolean? = false,
        @SerializedName("customFontWeight") val customFontWeight: Int? = 500,
        @SerializedName("customFontWidth") val customFontWidth: Float? = 100f,
        @SerializedName("customFontRoundness") val customFontRoundness: Float? = 0f,
        @SerializedName("taperingPlanEnabled") val taperingPlanEnabled: Boolean? = false,
        @SerializedName("taperingIntervalDays") val taperingIntervalDays: Int? = 7,
        @SerializedName("lastTaperingCheckinDate") val lastTaperingCheckinDate: Long? = 0L,
        @SerializedName("hasHistoricalBaseline") val hasHistoricalBaseline: Boolean? = false,
        @SerializedName("historicalStartDate") val historicalStartDate: Long? = 0L,
        @SerializedName("historicalDailyAvg") val historicalDailyAvg: Int? = 0,
        @SerializedName("historicalPackPrice") val historicalPackPrice: Float? = 0f,
        @SerializedName("historicalPackSize") val historicalPackSize: Int? = 20,
        @SerializedName("historicalTriggerPriorities") val historicalTriggerPriorities: List<String>? = emptyList(),
        @SerializedName("appIcon") val appIcon: String? = "DEFAULT",
        @SerializedName("checkUpdatesOnStart") val checkUpdatesOnStart: Boolean? = false,
        @SerializedName("customTriggers") val customTriggers: List<String>? = emptyList(),
        @SerializedName("disabledDefaultTriggers") val disabledDefaultTriggers: Set<String>? = emptySet(),
        @SerializedName("notificationEnabled") val notificationEnabled: Boolean? = false,
        @SerializedName("notificationLowPriority") val notificationLowPriority: Boolean? = true,
        @SerializedName("notificationShowTimer") val notificationShowTimer: Boolean? = true,
        @SerializedName("notificationShowProgress") val notificationShowProgress: Boolean? = true,
        @SerializedName("notificationShowAddButton") val notificationShowAddButton: Boolean? = true,
        @SerializedName("notificationShowResistButton") val notificationShowResistButton: Boolean? = false
    )
}
