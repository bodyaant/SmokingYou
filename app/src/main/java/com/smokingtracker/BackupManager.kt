package com.smokingtracker

import android.app.Application
import android.net.Uri
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.smokingtracker.data.AppIconPreset
import com.smokingtracker.data.ContainerStyle
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.local.SmokingEntryEntity
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.flow.first
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupManager(
    private val application: Application,
    private val dataStoreManager: DataStoreManager,
    private val repository: SmokingRepository,
    private val achievementsCoordinator: AchievementsCoordinator,
    private val appIconManager: AppIconManager
) {
    private val gson = Gson()

    suspend fun backup(uri: Uri) {
        val currentEntries = repository.smokingEntries.first()
        val data = BackupData(
            isRegistered = dataStoreManager.isRegistered.first(),
            entries = currentEntries.map {
                BackupEntry(timestamp = it.timestamp, trigger = it.trigger, isResisted = it.isResisted)
            },
            appTheme = dataStoreManager.appTheme.first().name,
            unlockedAchievements = dataStoreManager.unlockedAchievements.first(),
            dailyLimit = dataStoreManager.dailyLimit.first(),
            packPrice = dataStoreManager.packPrice.first(),
            packSize = dataStoreManager.packSize.first(),
            currency = dataStoreManager.currency.first(),
            colorPreset = dataStoreManager.colorPreset.first().name,
            fontPreset = dataStoreManager.fontPreset.first().name,
            amoledTheme = dataStoreManager.amoledTheme.first(),
            vibrationEnabled = dataStoreManager.vibrationEnabled.first(),
            hasMadeBackup = dataStoreManager.hasMadeBackup.first(),
            hasChangedPackPrice = dataStoreManager.hasChangedPackPrice.first(),
            hasCancelledWithin10s = dataStoreManager.hasCancelledWithin10s.first(),
            appLaunchDates = dataStoreManager.appLaunchDates.first(),
            containerBorderEnabled = dataStoreManager.containerBorderEnabled.first(),
            containerStyle = dataStoreManager.containerStyle.first().name,
            useCustomVariableFont = dataStoreManager.useCustomVariableFont.first(),
            customFontWeight = dataStoreManager.customFontWeight.first(),
            customFontWidth = dataStoreManager.customFontWidth.first(),
            customFontRoundness = dataStoreManager.customFontRoundness.first(),
            taperingPlanEnabled = dataStoreManager.taperingPlanEnabled.first(),
            taperingIntervalDays = dataStoreManager.taperingIntervalDays.first(),
            lastTaperingCheckinDate = dataStoreManager.lastTaperingCheckinDate.first(),
            hasHistoricalBaseline = dataStoreManager.hasHistoricalBaseline.first(),
            historicalStartDate = dataStoreManager.historicalStartDate.first(),
            historicalDailyAvg = dataStoreManager.historicalDailyAvg.first(),
            historicalPackPrice = dataStoreManager.historicalPackPrice.first(),
            historicalPackSize = dataStoreManager.historicalPackSize.first(),
            historicalTriggerPriorities = dataStoreManager.historicalTriggerPriorities.first(),
            appIcon = dataStoreManager.appIcon.first().name,
            checkUpdatesOnStart = dataStoreManager.checkUpdatesOnStart.first()
        )

        application.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                gson.toJson(data, writer)
            }
        } ?: throw IllegalStateException("Failed to open OutputStream for URI: $uri")

        dataStoreManager.setHasMadeBackup(true)
        achievementsCoordinator.checkAndUpdate()
    }

    suspend fun restore(uri: Uri) {
        application.contentResolver.openInputStream(uri)?.use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                val data = gson.fromJson(reader, BackupData::class.java)
                    ?: throw IllegalStateException("Backup data is null or corrupted")

                val colorPresetVal = data.colorPreset ?: "SYSTEM"
                val fontPresetVal = data.fontPreset ?: "WIDE"
                val containerStyleVal = data.containerStyle ?: ContainerStyle.EXPRESSIVE.name
                val appIconVal = data.appIcon ?: AppIconPreset.DEFAULT.name

                dataStoreManager.restoreFromBackup(
                    isReg = data.isRegistered,
                    theme = data.appTheme,
                    achievements = data.unlockedAchievements,
                    limit = data.dailyLimit ?: 0,
                    price = data.packPrice ?: 0.0f,
                    size = data.packSize ?: 20,
                    curr = data.currency ?: "USD",
                    colorPresetVal = colorPresetVal,
                    fontPresetVal = fontPresetVal,
                    amoledThemeVal = data.amoledTheme ?: false,
                    vibrationEnabledVal = data.vibrationEnabled ?: false,
                    hasBackupVal = data.hasMadeBackup ?: false,
                    hasPriceChangedVal = data.hasChangedPackPrice ?: false,
                    hasCancelled10sVal = data.hasCancelledWithin10s ?: false,
                    launchesVal = data.appLaunchDates ?: emptyList(),
                    containerBorderEnabledVal = data.containerBorderEnabled ?: true,
                    containerStyleVal = containerStyleVal,
                    useCustomVariableFontVal = data.useCustomVariableFont ?: false,
                    customFontWeightVal = data.customFontWeight ?: 500,
                    customFontWidthVal = data.customFontWidth ?: 100f,
                    customFontRoundnessVal = data.customFontRoundness ?: 0f,
                    taperingPlanEnabledVal = data.taperingPlanEnabled ?: false,
                    taperingIntervalDaysVal = data.taperingIntervalDays ?: 7,
                    lastTaperingCheckinDateVal = data.lastTaperingCheckinDate ?: 0L,
                    hasHistoricalBaselineVal = data.hasHistoricalBaseline ?: false,
                    historicalStartDateVal = data.historicalStartDate ?: 0L,
                    historicalDailyAvgVal = data.historicalDailyAvg ?: 0,
                    historicalPackPriceVal = data.historicalPackPrice ?: 0f,
                    historicalPackSizeVal = data.historicalPackSize ?: 20,
                    historicalTriggerPrioritiesVal = data.historicalTriggerPriorities ?: emptyList(),
                    appIconVal = appIconVal,
                    checkUpdatesOnStartVal = data.checkUpdatesOnStart ?: false
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
        @SerializedName("dailyLimit") val dailyLimit: Int? = 0,
        @SerializedName("packPrice") val packPrice: Float? = 0.0f,
        @SerializedName("packSize") val packSize: Int? = 20,
        @SerializedName("currency") val currency: String? = "USD",
        @SerializedName("colorPreset") val colorPreset: String? = "SYSTEM",
        @SerializedName("entryTriggers") val entryTriggers: Map<Long, String>? = emptyMap(),
        @SerializedName("fontPreset") val fontPreset: String? = "WIDE",
        @SerializedName("amoledTheme") val amoledTheme: Boolean? = false,
        @SerializedName("vibrationEnabled") val vibrationEnabled: Boolean? = false,
        @SerializedName("hasMadeBackup") val hasMadeBackup: Boolean? = false,
        @SerializedName("hasChangedPackPrice") val hasChangedPackPrice: Boolean? = false,
        @SerializedName("hasCancelledWithin10s") val hasCancelledWithin10s: Boolean? = false,
        @SerializedName("appLaunchDates") val appLaunchDates: List<Long>? = emptyList(),
        @SerializedName("containerBorderEnabled") val containerBorderEnabled: Boolean? = true,
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
        @SerializedName("checkUpdatesOnStart") val checkUpdatesOnStart: Boolean? = false
    )
}
