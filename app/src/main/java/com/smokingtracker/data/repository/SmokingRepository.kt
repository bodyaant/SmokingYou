package com.smokingtracker.data.repository

import com.smokingtracker.data.local.SmokingDao
import com.smokingtracker.data.local.SmokingEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SmokingRepository(
    private val smokingDao: SmokingDao
) {
    val smokingEntries: Flow<List<SmokingEntryEntity>> = smokingDao.getAllEntriesFlow()

    val nonResistedEntries: Flow<List<SmokingEntryEntity>> = smokingEntries
        .map { entities -> entities.filter { !it.isResisted } }

    val nonResistedTimestamps: Flow<List<Long>> = nonResistedEntries
        .map { entities -> entities.map { it.timestamp } }

    val resistedEntries: Flow<List<SmokingEntryEntity>> = smokingEntries
        .map { entities -> entities.filter { it.isResisted } }

    suspend fun getAllEntries(): List<SmokingEntryEntity> {
        return smokingDao.getAllEntriesList()
    }

    suspend fun addEntry(timestamp: Long, trigger: String?) {
        smokingDao.insertEntry(SmokingEntryEntity(timestamp = timestamp, trigger = trigger, isResisted = false))
    }

    suspend fun addResistedEntry(timestamp: Long, trigger: String?) {
        smokingDao.insertEntry(SmokingEntryEntity(timestamp = timestamp, trigger = trigger, isResisted = true))
    }

    suspend fun removeEntryById(id: Long) {
        smokingDao.deleteEntryById(id)
    }

    suspend fun updateEntryTimestampById(id: Long, newTimestamp: Long) {
        smokingDao.updateEntryTimestampById(id, newTimestamp)
    }

    suspend fun updateEntryTriggerById(id: Long, trigger: String?) {
        smokingDao.updateEntryTriggerById(id, trigger)
    }

    suspend fun clearAndInsertEntries(entities: List<SmokingEntryEntity>) {
        smokingDao.clearAllEntries()
        smokingDao.insertEntries(entities)
    }

    suspend fun clearAllEntries() {
        smokingDao.clearAllEntries()
    }
}
