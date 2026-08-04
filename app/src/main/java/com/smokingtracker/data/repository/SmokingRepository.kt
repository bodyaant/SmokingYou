package com.smokingtracker.data.repository

import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.local.SmokingDao
import com.smokingtracker.data.local.SmokingEntryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmokingRepository(
    private val smokingDao: SmokingDao,
    private val dataStoreManager: DataStoreManager,
    private val applicationScope: CoroutineScope
) {
    val smokingEntries: Flow<List<SmokingEntryEntity>> = smokingDao.getAllEntriesFlow()

    init {
        applicationScope.launch {
            if (dataStoreManager.hasOldData.first()) {
                val (oldEntries, oldTriggers) = dataStoreManager.getOldEntriesAndClear()
                if (oldEntries.isNotEmpty()) {
                    val entities = oldEntries.map { ts ->
                        SmokingEntryEntity(timestamp = ts, trigger = oldTriggers[ts])
                    }
                    smokingDao.insertEntries(entities)
                }
            }
        }
    }

    suspend fun getAllEntries(): List<SmokingEntryEntity> {
        return smokingDao.getAllEntriesList()
    }

    suspend fun addEntry(timestamp: Long, trigger: String?) {
        smokingDao.insertEntry(SmokingEntryEntity(timestamp = timestamp, trigger = trigger, isResisted = false))
    }

    suspend fun addResistedEntry(timestamp: Long, trigger: String?) {
        smokingDao.insertEntry(SmokingEntryEntity(timestamp = timestamp, trigger = trigger, isResisted = true))
    }

    suspend fun removeEntry(timestamp: Long) {
        smokingDao.deleteEntryByTimestamp(timestamp)
    }

    suspend fun removeEntryById(id: Long) {
        smokingDao.deleteEntryById(id)
    }

    suspend fun editEntry(oldTimestamp: Long, newTimestamp: Long) {
        smokingDao.updateEntryTimestamp(oldTimestamp, newTimestamp)
    }

    suspend fun updateEntryTrigger(timestamp: Long, trigger: String?) {
        smokingDao.updateEntryTrigger(timestamp, trigger)
    }

    suspend fun updateEntryTriggerById(id: Long, trigger: String?) {
        smokingDao.updateEntryTriggerById(id, trigger)
    }

    suspend fun clearAndInsertEntries(entities: List<SmokingEntryEntity>) {
        smokingDao.clearAllEntries()
        smokingDao.insertEntries(entities)
    }
}
