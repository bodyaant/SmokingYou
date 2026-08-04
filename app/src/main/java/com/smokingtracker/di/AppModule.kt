package com.smokingtracker.di

import androidx.room.Room
import com.smokingtracker.AchievementsCoordinator
import com.smokingtracker.AchievementsManager
import com.smokingtracker.AppIconManager
import com.smokingtracker.BackupManager
import com.smokingtracker.HomeViewModel
import com.smokingtracker.MainViewModel
import com.smokingtracker.StatisticsManager
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.local.SmokingDatabase
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.manager.GitHubUpdateManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

import com.smokingtracker.data.local.MIGRATION_1_2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single { AchievementsManager() }
    single { StatisticsManager() }
    single { DataStoreManager(androidContext()) }
    single { GitHubUpdateManager(androidContext()) }
    single {
        Room.databaseBuilder(
            androidContext(),
            SmokingDatabase::class.java,
            "smoking_tracker.db"
        ).addMigrations(MIGRATION_1_2).build()
    }
    single { get<SmokingDatabase>().smokingDao() }
    single { SmokingRepository(get(), get(), get()) }
    single { AchievementsCoordinator(get(), get(), get(), androidApplication(), get()) }
    single { AppIconManager(androidApplication()) }
    single { BackupManager(androidApplication(), get(), get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), androidApplication()) }
    viewModel { HomeViewModel(get(), get(), get(), androidApplication()) }
}
