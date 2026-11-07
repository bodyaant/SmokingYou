package com.smokingtracker.di

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.google.gson.Gson
import com.smokingtracker.AchievementNotifier
import com.smokingtracker.AchievementsCoordinator
import com.smokingtracker.AchievementsManager
import com.smokingtracker.AchievementsViewModel
import com.smokingtracker.AndroidAchievementNotifier
import com.smokingtracker.AppIconManager
import com.smokingtracker.AppearanceViewModel
import com.smokingtracker.BackupManager
import com.smokingtracker.HomeViewModel
import com.smokingtracker.MainViewModel
import com.smokingtracker.SettingsViewModel
import com.smokingtracker.StatisticsManager
import com.smokingtracker.StatisticsViewModel
import com.smokingtracker.data.local.MIGRATION_1_2
import com.smokingtracker.data.local.MIGRATION_2_3
import com.smokingtracker.data.local.SmokingDatabase
import com.smokingtracker.data.manager.GitHubUpdateManager
import com.smokingtracker.data.preferences.AppMetaPreferences
import com.smokingtracker.data.preferences.NotificationPreferences
import com.smokingtracker.data.preferences.ThemePreferences
import com.smokingtracker.data.preferences.UserPreferences
import com.smokingtracker.data.preferences.dataStore
import com.smokingtracker.data.repository.DataStoreTriggerRepository
import com.smokingtracker.data.repository.RoomSmokingRepository
import com.smokingtracker.data.repository.SmokingRepository
import com.smokingtracker.data.repository.TriggerRepository
import com.smokingtracker.notification.OngoingNotificationManager
import com.smokingtracker.widget.WidgetUpdateManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val APPLICATION_SCOPE = "ApplicationScope"
const val IO_DISPATCHER = "IoDispatcher"
const val DEFAULT_DISPATCHER = "DefaultDispatcher"
const val MAIN_DISPATCHER = "MainDispatcher"

val ApplicationScopeQualifier = named(APPLICATION_SCOPE)
val IoDispatcherQualifier = named(IO_DISPATCHER)
val DefaultDispatcherQualifier = named(DEFAULT_DISPATCHER)
val MainDispatcherQualifier = named(MAIN_DISPATCHER)

val appModule = module {
    single { Gson() }

    single<CoroutineDispatcher>(IoDispatcherQualifier) { Dispatchers.IO }
    single<CoroutineDispatcher>(DefaultDispatcherQualifier) { Dispatchers.Default }
    single<CoroutineDispatcher>(MainDispatcherQualifier) { Dispatchers.Main }

    single<CoroutineScope>(ApplicationScopeQualifier) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(APPLICATION_SCOPE, "Uncaught exception in applicationScope", throwable)
        }
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(IoDispatcherQualifier) + exceptionHandler)
    }

    single<DataStore<Preferences>> { androidContext().dataStore }

    singleOf(::AchievementsManager)
    single { StatisticsManager(defaultDispatcher = get(DefaultDispatcherQualifier)) }

    singleOf(::ThemePreferences)
    singleOf(::UserPreferences)
    singleOf(::NotificationPreferences)
    singleOf(::AppMetaPreferences)
    single {
        GitHubUpdateManager(
            gson = get(),
            ioDispatcher = get(IoDispatcherQualifier)
        )
    }
    single {
        AppIconManager(
            application = androidApplication(),
            ioDispatcher = get(IoDispatcherQualifier)
        )
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            SmokingDatabase::class.java,
            "smoking_tracker.db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }
    single { get<SmokingDatabase>().smokingDao() }
    single<SmokingRepository> {
        RoomSmokingRepository(
            smokingDao = get(),
            ioDispatcher = get(IoDispatcherQualifier),
            defaultDispatcher = get(DefaultDispatcherQualifier)
        )
    }
    single<TriggerRepository> {
        DataStoreTriggerRepository(
            dataStore = get(),
            gson = get(),
            ioDispatcher = get(IoDispatcherQualifier),
            defaultDispatcher = get(DefaultDispatcherQualifier)
        )
    }

    single {
        OngoingNotificationManager(
            context = androidContext(),
            notificationPreferences = get(),
            userPreferences = get(),
            repository = get(),
            applicationScope = get(ApplicationScopeQualifier),
            appMetaPreferences = get(),
            ioDispatcher = get(IoDispatcherQualifier)
        )
    }
    single {
        WidgetUpdateManager(
            context = androidContext(),
            ongoingNotificationManager = get(),
            applicationScope = get(ApplicationScopeQualifier),
            ioDispatcher = get(IoDispatcherQualifier)
        )
    }
    single<AchievementNotifier> {
        AndroidAchievementNotifier(
            context = androidContext(),
            achievementsManager = get()
        )
    }
    single<AchievementsCoordinator> {
        AchievementsCoordinator(
            repository = get(),
            appMetaPreferences = get(),
            userPreferences = get(),
            achievementsManager = get(),
            achievementNotifier = get(),
            applicationScope = get(ApplicationScopeQualifier),
            defaultDispatcher = get(DefaultDispatcherQualifier)
        )
    }
    single {
        BackupManager(
            application = androidApplication(),
            themePreferences = get(),
            userPreferences = get(),
            notificationPreferences = get(),
            appMetaPreferences = get(),
            triggerRepository = get(),
            repository = get(),
            achievementsCoordinator = get(),
            appIconManager = get(),
            widgetUpdateManager = get(),
            gson = get(),
            ongoingNotificationManager = get(),
            ioDispatcher = get(IoDispatcherQualifier)
        )
    }

    viewModel {
        MainViewModel(
            themePreferences = get(),
            appMetaPreferences = get(),
            updateManager = get(),
            achievementsCoordinator = get(),
            ioDispatcher = get(IoDispatcherQualifier),
            defaultDispatcher = get(DefaultDispatcherQualifier)
        )
    }
    viewModel {
        HomeViewModel(
            repository = get(),
            triggerRepository = get(),
            userPreferences = get(),
            appMetaPreferences = get(),
            achievementsCoordinator = get(),
            widgetUpdateManager = get(),
            statisticsManager = get(),
            achievementsManager = get(),
            ioDispatcher = get(IoDispatcherQualifier),
            defaultDispatcher = get(DefaultDispatcherQualifier),
            mainDispatcher = get(MainDispatcherQualifier)
        )
    }
    viewModel {
        AppearanceViewModel(
            themePreferences = get(),
            appMetaPreferences = get(),
            achievementsCoordinator = get(),
            appIconManager = get(),
            ioDispatcher = get(IoDispatcherQualifier),
            defaultDispatcher = get(DefaultDispatcherQualifier)
        )
    }
    viewModel {
        StatisticsViewModel(
            repository = get(),
            triggerRepository = get(),
            userPreferences = get(),
            themePreferences = get(),
            notificationPreferences = get(),
            appMetaPreferences = get(),
            achievementsCoordinator = get(),
            statisticsManager = get(),
            ioDispatcher = get(IoDispatcherQualifier),
            defaultDispatcher = get(DefaultDispatcherQualifier),
            mainDispatcher = get(MainDispatcherQualifier)
        )
    }
    viewModel {
        AchievementsViewModel(
            repository = get(),
            appMetaPreferences = get(),
            achievementsCoordinator = get(),
            achievementsManager = get(),
            statisticsManager = get(),
            ioDispatcher = get(IoDispatcherQualifier),
            defaultDispatcher = get(DefaultDispatcherQualifier)
        )
    }
    viewModel {
        SettingsViewModel(
            repository = get(),
            triggerRepository = get(),
            themePreferences = get(),
            userPreferences = get(),
            notificationPreferences = get(),
            appMetaPreferences = get(),
            updateManager = get(),
            achievementsCoordinator = get(),
            backupManager = get(),
            ongoingNotificationManager = get(),
            widgetUpdateManager = get(),
            ioDispatcher = get(IoDispatcherQualifier),
            defaultDispatcher = get(DefaultDispatcherQualifier),
            mainDispatcher = get(MainDispatcherQualifier)
        )
    }
}
