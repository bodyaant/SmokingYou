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

    @Query("SELECT * FROM smoking_entries ORDER BY timestamp ASC")
    suspend fun getAllEntriesList(): List<SmokingEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: SmokingEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<SmokingEntryEntity>)

    @Query("DELETE FROM smoking_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("UPDATE smoking_entries SET timestamp = :newTimestamp WHERE id = :id")
    suspend fun updateEntryTimestampById(id: Long, newTimestamp: Long)

    @Query("UPDATE smoking_entries SET `trigger` = :trigger WHERE id = :id")
    suspend fun updateEntryTriggerById(id: Long, trigger: String?)

    @Query("DELETE FROM smoking_entries")
    suspend fun clearAllEntries()
}
