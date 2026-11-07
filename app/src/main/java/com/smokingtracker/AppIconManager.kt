package com.smokingtracker

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.util.Log
import com.smokingtracker.data.AppIconPreset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AppIconManager(
    private val application: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val mutex = Mutex()

    suspend fun applyIcon(preset: AppIconPreset): Unit = withContext(ioDispatcher) {
        mutex.withLock {
            applyIconInternal(preset)
        }
    }

    internal fun applyIconInternal(preset: AppIconPreset) {
        val pm = application.packageManager
        val aliasPrefix = MainActivity::class.java.`package`?.name ?: "com.smokingtracker"
        val targetAlias = presetToAlias(aliasPrefix, preset)
        val targetComponent = ComponentName(application, targetAlias)

        try {
            val currentTargetState = pm.getComponentEnabledSetting(targetComponent)
            if (currentTargetState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                pm.setComponentEnabledSetting(
                    targetComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable component alias: $targetAlias; aborting disable of existing aliases to prevent launcher lockout", e)
            return
        }

        val targetEnabled = try {
            val state = pm.getComponentEnabledSetting(targetComponent)
            state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                (preset == AppIconPreset.DEFAULT && state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
        } catch (e: Exception) {
            false
        }
        if (!targetEnabled) {
            Log.w(TAG, "Target alias $targetAlias did not transition to enabled state; keeping existing aliases intact")
            return
        }

        for (alias in allAliases(aliasPrefix)) {
            if (alias == targetAlias) continue
            val component = ComponentName(application, alias)
            try {
                val currentState = pm.getComponentEnabledSetting(component)
                if (currentState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    pm.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to disable component alias: $alias", e)
            }
        }
    }

    internal fun presetToAlias(packageName: String, preset: AppIconPreset): String = when (preset) {
        AppIconPreset.DEFAULT    -> "$packageName.MainActivityDefault"
        AppIconPreset.DARK       -> "$packageName.MainActivityDark"
        AppIconPreset.SUNSET     -> "$packageName.MainActivitySunset"
        AppIconPreset.CREAM      -> "$packageName.MainActivityCream"
        AppIconPreset.NEON       -> "$packageName.MainActivityNeon"
        AppIconPreset.GREEN      -> "$packageName.MainActivityGreen"
        AppIconPreset.NIGHT      -> "$packageName.MainActivityNight"
        AppIconPreset.MONOCHROME -> "$packageName.MainActivityMonochrome"
    }

    internal fun allAliases(packageName: String): List<String> =
        AppIconPreset.entries.map { preset -> presetToAlias(packageName, preset) }

    companion object {
        private const val TAG = "AppIconManager"
    }
}
