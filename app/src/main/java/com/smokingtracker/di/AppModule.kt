package com.smokingtracker.di

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.smokingtracker.AchievementsCoordinator
import com.smokingtracker.AchievementsManager
import com.smokingtracker.AchievementsViewModel
import com.smokingtracker.AppIconManager
import com.smokingtracker.AppearanceViewModel
import com.smokingtracker.BackupManager
import com.smokingtracker.HomeViewModel
import com.smokingtracker.MainViewModel
import com.smokingtracker.SettingsViewModel
import com.smokingtracker.StatisticsManager
import com.smokingtracker.StatisticsViewModel
import com.smokingtracker.data.local.MIGRATION_1_2
import com.smokingtracker.data.local.SmokingDatabase
import com.smokingtracker.data.manager.GitHubUpdateManager
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.NotificationPreferences
import com.smokingtracker.data.preferences.ThemePreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.repository.TriggerRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ProcessLifecycleOwner.get().lifecycleScope }
    single { AchievementsManager() }
    single { StatisticsManager() }
    single { ThemePreferences(androidContext()) }
    single { UserPreferences(androidContext()) }
    single { NotificationPreferences(androidContext()) }
    single { AppMetaPreferences(androidContext()) }
    single { GitHubUpdateManager(androidContext()) }
    single {
        Room.databaseBuilder(
            androidContext(),
            SmokingDatabase::class.java,
            "smoking_tracker.db"
        ).addMigrations(MIGRATION_1_2).build()
    }
    single { get<SmokingDatabase>().smokingDao() }
    single { SmokingRepository(get()) }
    single { TriggerRepository(androidContext()) }
    single { AchievementsCoordinator(get(), get(), get(), get(), androidApplication(), get()) }
    single { AppIconManager(androidApplication()) }
    single { BackupManager(androidApplication(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), androidApplication()) }
    viewModel { AppearanceViewModel(get(), get(), get(), get()) }
    viewModel { StatisticsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AchievementsViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), androidApplication()) }
}
