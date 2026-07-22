package com.smokingtracker.di

import androidx.room.Room
import com.smokingtracker.MainViewModel
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.local.SmokingDatabase
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.manager.GitHubUpdateManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

import com.smokingtracker.data.local.MIGRATION_1_2

val appModule = module {
    single { DataStoreManager(androidContext()) }
    single { GitHubUpdateManager(androidContext()) }
    single {
        Room.databaseBuilder(
            androidContext(),
            SmokingDatabase::class.java,
            "smoking_tracker.db"
        ).addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration().build()
    }
    single { get<SmokingDatabase>().smokingDao() }
    single { SmokingRepository(get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), androidContext()) }
}
