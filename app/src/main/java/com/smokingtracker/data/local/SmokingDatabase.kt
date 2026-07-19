package com.smokingtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SmokingEntryEntity::class], version = 1, exportSchema = false)
abstract class SmokingDatabase : RoomDatabase() {
    abstract fun smokingDao(): SmokingDao
}
