package com.smokingtracker

import android.app.Application
import android.net.Uri
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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
    private val achievementsCoordinator: AchievementsCoordinator
) {
    private val gson = Gson()

    suspend fun backup(uri: Uri) {
        val currentEntries = repository.smokingEntries.first()
        val data = BackupData(
            isRegistered = dataStoreManager.isRegistered.first() ?: false,
            smokingEntries = currentEntries.map { it.timestamp },
            appTheme = dataStoreManager.appTheme.first().name,
            unlockedAchievements = dataStoreManager.unlockedAchievements.first(),
            dailyLimit = dataStoreManager.dailyLimit.first(),
            packPrice = dataStoreManager.packPrice.first(),
            packSize = dataStoreManager.packSize.first(),
            currency = dataStoreManager.currency.first(),
            colorPreset = dataStoreManager.colorPreset.first().name,
            entryTriggers = currentEntries.filter { it.trigger != null }
                .associate { it.timestamp to it.trigger!! },
            fontPreset = dataStoreManager.fontPreset.first().name,
            amoledTheme = dataStoreManager.amoledTheme.first(),
            vibrationEnabled = dataStoreManager.vibrationEnabled.first(),
            hasMadeBackup = dataStoreManager.hasMadeBackup.first(),
            hasChangedPackPrice = dataStoreManager.hasChangedPackPrice.first(),
            hasCancelledWithin10s = dataStoreManager.hasCancelledWithin10s.first(),
            appLaunchDates = dataStoreManager.appLaunchDates.first()
        )

        application.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                gson.toJson(data, writer)
            }
        } ?: throw IllegalStateException("Не удалось открыть OutputStream для URI: $uri")

        dataStoreManager.setHasMadeBackup(true)
        achievementsCoordinator.checkAndUpdate()
    }

    suspend fun restore(uri: Uri) {
        application.contentResolver.openInputStream(uri)?.use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                val data = gson.fromJson(reader, BackupData::class.java)
                    ?: throw IllegalStateException("Данные бэкапа пусты или повреждены")

                dataStoreManager.restoreFromBackup(
                    isReg = data.isRegistered,
                    theme = data.appTheme,
                    achievements = data.unlockedAchievements,
                    limit = data.dailyLimit ?: 0,
                    price = data.packPrice ?: 0.0f,
                    size = data.packSize ?: 20,
                    curr = data.currency ?: "USD",
                    colorPresetVal = data.colorPreset ?: "SYSTEM",
                    fontPresetVal = data.fontPreset ?: "WIDE",
                    amoledThemeVal = data.amoledTheme ?: false,
                    vibrationEnabledVal = data.vibrationEnabled ?: false,
                    hasBackupVal = data.hasMadeBackup ?: false,
                    hasPriceChangedVal = data.hasChangedPackPrice ?: false,
                    hasCancelled10sVal = data.hasCancelledWithin10s ?: false,
                    launchesVal = data.appLaunchDates ?: emptyList()
                )

                val newEntities = data.smokingEntries.map { ts ->
                    SmokingEntryEntity(
                        timestamp = ts,
                        trigger = data.entryTriggers?.get(ts)
                    )
                }
                repository.clearAndInsertEntries(newEntities)
                WidgetUpdateManager.updateAllAsync(application)
            }
        } ?: throw IllegalStateException("Не удалось открыть InputStream для URI: $uri")
    }

    @Keep
    data class BackupData(
        @SerializedName("version") val version: Int = 2,
        @SerializedName("isRegistered") val isRegistered: Boolean,
        @SerializedName("smokingEntries") val smokingEntries: List<Long>,
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
        @SerializedName("appLaunchDates") val appLaunchDates: List<Long>? = emptyList()
    )
}
