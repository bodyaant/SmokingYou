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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `smoking_entries_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `trigger_name` TEXT,
                `isResisted` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `smoking_entries_new` (`id`, `timestamp`, `trigger_name`, `isResisted`)
            SELECT `id`, `timestamp`, `trigger`, `isResisted` FROM `smoking_entries`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `smoking_entries`")
        db.execSQL("ALTER TABLE `smoking_entries_new` RENAME TO `smoking_entries`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_smoking_entries_timestamp` ON `smoking_entries` (`timestamp`)")
    }
}

@Database(entities = [SmokingEntryEntity::class], version = 3, exportSchema = true)
abstract class SmokingDatabase : RoomDatabase() {
    abstract fun smokingDao(): SmokingDao
}
