package com.smokingtracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SmokingDao {
    @Query("SELECT * FROM smoking_entries ORDER BY timestamp ASC")
    fun getAllEntriesFlow(): Flow<List<SmokingEntryEntity>>

    @Query("SELECT * FROM smoking_entries ORDER BY timestamp ASC")
    suspend fun getAllEntriesList(): List<SmokingEntryEntity>

    @Query("SELECT * FROM smoking_entries WHERE isResisted = :isResisted ORDER BY timestamp ASC")
    fun getEntriesByResistedFlow(isResisted: Boolean): Flow<List<SmokingEntryEntity>>

    @Query("SELECT * FROM smoking_entries WHERE isResisted = :isResisted ORDER BY timestamp ASC")
    suspend fun getEntriesByResistedList(isResisted: Boolean): List<SmokingEntryEntity>

    @Query("SELECT timestamp FROM smoking_entries WHERE isResisted = 0 ORDER BY timestamp ASC")
    fun getNonResistedTimestampsFlow(): Flow<List<Long>>

    @Query("SELECT * FROM smoking_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEntry(): SmokingEntryEntity?

    @Query("SELECT * FROM smoking_entries ORDER BY timestamp DESC LIMIT 1")
    fun getLastEntryFlow(): Flow<SmokingEntryEntity?>

    @Query("SELECT * FROM smoking_entries WHERE isResisted = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSmokedEntry(): SmokingEntryEntity?

    @Query("SELECT * FROM smoking_entries WHERE isResisted = 0 ORDER BY timestamp DESC LIMIT 1")
    fun getLastSmokedEntryFlow(): Flow<SmokingEntryEntity?>

    @Query("SELECT * FROM smoking_entries WHERE timestamp BETWEEN :startMillis AND :endMillis ORDER BY timestamp ASC")
    fun getEntriesInRangeFlow(startMillis: Long, endMillis: Long): Flow<List<SmokingEntryEntity>>

    @Query("SELECT * FROM smoking_entries WHERE timestamp BETWEEN :startMillis AND :endMillis ORDER BY timestamp ASC")
    suspend fun getEntriesInRangeList(startMillis: Long, endMillis: Long): List<SmokingEntryEntity>

    @Query("SELECT COUNT(*) FROM smoking_entries WHERE isResisted = :isResisted AND timestamp BETWEEN :startMillis AND :endMillis")
    suspend fun getEntryCountInRange(startMillis: Long, endMillis: Long, isResisted: Boolean = false): Int

    @Query("SELECT * FROM smoking_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): SmokingEntryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntry(entry: SmokingEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntries(entries: List<SmokingEntryEntity>): List<Long>

    @Update
    suspend fun updateEntry(entry: SmokingEntryEntity): Int

    @Query("DELETE FROM smoking_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long): Int

    @Query("UPDATE smoking_entries SET timestamp = :newTimestamp WHERE id = :id")
    suspend fun updateEntryTimestampById(id: Long, newTimestamp: Long): Int

    @Query("UPDATE smoking_entries SET trigger_name = :trigger WHERE id = :id")
    suspend fun updateEntryTriggerById(id: Long, trigger: String?): Int

    @Query("UPDATE smoking_entries SET isResisted = :isResisted WHERE id = :id")
    suspend fun updateEntryResistedById(id: Long, isResisted: Boolean): Int

    @Query("DELETE FROM smoking_entries")
    suspend fun clearAllEntries()

    @Transaction
    suspend fun clearAndInsertEntries(entries: List<SmokingEntryEntity>) {
        clearAllEntries()
        if (entries.isNotEmpty()) {
            insertEntries(entries)
        }
    }
}
