package com.smokingtracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmokingDao {
    @Query("SELECT * FROM smoking_entries ORDER BY timestamp ASC")
    fun getAllEntriesFlow(): Flow<List<SmokingEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: SmokingEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<SmokingEntryEntity>)

    @Query("DELETE FROM smoking_entries WHERE timestamp = :timestamp")
    suspend fun deleteEntryByTimestamp(timestamp: Long)

    @Query("UPDATE smoking_entries SET timestamp = :newTimestamp WHERE timestamp = :oldTimestamp")
    suspend fun updateEntryTimestamp(oldTimestamp: Long, newTimestamp: Long)

    @Query("DELETE FROM smoking_entries")
    suspend fun clearAllEntries()
}
