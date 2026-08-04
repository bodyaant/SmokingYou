package com.smokingtracker

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.util.Log
import com.smokingtracker.data.AppIconPreset

class AppIconManager(private val application: Application) {

    fun applyIcon(preset: AppIconPreset) {
        val pm = application.packageManager
        val packageName = application.packageName
        val targetAlias = presetToAlias(packageName, preset)

        allAliases(packageName).forEach { alias ->
            val state = if (alias == targetAlias) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                pm.setComponentEnabledSetting(
                    ComponentName(application, alias),
                    state,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось применить alias: $alias", e)
            }
        }
    }

    private fun presetToAlias(packageName: String, preset: AppIconPreset): String = when (preset) {
        AppIconPreset.DEFAULT    -> "$packageName.MainActivityDefault"
        AppIconPreset.DARK       -> "$packageName.MainActivityDark"
        AppIconPreset.SUNSET     -> "$packageName.MainActivitySunset"
        AppIconPreset.CREAM      -> "$packageName.MainActivityCream"
        AppIconPreset.NEON       -> "$packageName.MainActivityNeon"
        AppIconPreset.GREEN      -> "$packageName.MainActivityGreen"
        AppIconPreset.NIGHT      -> "$packageName.MainActivityNight"
        AppIconPreset.MONOCHROME -> "$packageName.MainActivityMonochrome"
    }

    private fun allAliases(packageName: String): List<String> = listOf(
        "$packageName.MainActivityDefault",
        "$packageName.MainActivityDark",
        "$packageName.MainActivitySunset",
        "$packageName.MainActivityCream",
        "$packageName.MainActivityNeon",
        "$packageName.MainActivityGreen",
        "$packageName.MainActivityNight",
        "$packageName.MainActivityMonochrome"
    )

    companion object {
        private const val TAG = "AppIconManager"
    }
}
