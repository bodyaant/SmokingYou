package com.smokingtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE smoking_entries ADD COLUMN isResisted INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(entities = [SmokingEntryEntity::class], version = 2, exportSchema = false)
abstract class SmokingDatabase : RoomDatabase() {
    abstract fun smokingDao(): SmokingDao
}

