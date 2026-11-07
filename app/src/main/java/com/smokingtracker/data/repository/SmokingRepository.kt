package com.smokingtracker.data.repository

import android.database.SQLException
import android.util.Log
import com.smokingtracker.data.TriggerItem
import com.smokingtracker.data.TriggerType
import com.smokingtracker.data.local.SmokingDao
import com.smokingtracker.data.local.SmokingEntryEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

data class SmokingEntry(
    val id: Long = 0L,
    val timestamp: Long,
    val trigger: String? = null,
    val isResisted: Boolean = false
)

fun SmokingEntryEntity.toDomain(): SmokingEntry = SmokingEntry(
    id = id,
    timestamp = timestamp,
    trigger = trigger,
    isResisted = isResisted
)

fun SmokingEntryEntity.toSmokingEntry(): SmokingEntry = toDomain()

fun SmokingEntry.toEntity(): SmokingEntryEntity = SmokingEntryEntity(
    id = id,
    timestamp = timestamp,
    trigger = trigger,
    isResisted = isResisted
)

fun SmokingEntry.toSmokingEntryEntity(): SmokingEntryEntity = toEntity()

fun List<SmokingEntryEntity>.toDomainList(): List<SmokingEntry> = map(SmokingEntryEntity::toDomain)

fun List<SmokingEntry>.toEntityList(): List<SmokingEntryEntity> = map(SmokingEntry::toEntity)

interface SmokingRepository {
    val smokingEntries: Flow<List<SmokingEntryEntity>>
    val nonResistedEntries: Flow<List<SmokingEntryEntity>>
    val nonResistedTimestamps: Flow<List<Long>>
    val resistedEntries: Flow<List<SmokingEntryEntity>>

    val entries: Flow<List<SmokingEntry>>
    val smokedEntries: Flow<List<SmokingEntry>>
    val resistedDomainEntries: Flow<List<SmokingEntry>>

    suspend fun getAllEntries(): List<SmokingEntryEntity>
    suspend fun getAllDomainEntries(): List<SmokingEntry>

    suspend fun getLastEntry(): SmokingEntryEntity?
    suspend fun getLastDomainEntry(): SmokingEntry?
    fun getLastEntryFlow(): Flow<SmokingEntryEntity?>
    fun getLastDomainEntryFlow(): Flow<SmokingEntry?>

    suspend fun getLastSmokedEntry(): SmokingEntryEntity?
    suspend fun getLastSmokedDomainEntry(): SmokingEntry?
    fun getLastSmokedEntryFlow(): Flow<SmokingEntryEntity?>
    fun getLastSmokedDomainEntryFlow(): Flow<SmokingEntry?>

    fun getEntriesInRangeFlow(startMillis: Long, endMillis: Long): Flow<List<SmokingEntryEntity>>
    fun getDomainEntriesInRangeFlow(startMillis: Long, endMillis: Long): Flow<List<SmokingEntry>>
    suspend fun getEntriesInRange(startMillis: Long, endMillis: Long): List<SmokingEntryEntity>
    suspend fun getDomainEntriesInRange(startMillis: Long, endMillis: Long): List<SmokingEntry>
    suspend fun getEntryCountInRange(startMillis: Long, endMillis: Long, isResisted: Boolean = false): Int

    suspend fun addEntry(timestamp: Long, trigger: String?): Long
    suspend fun addResistedEntry(timestamp: Long, trigger: String?): Long
    suspend fun updateEntry(entry: SmokingEntryEntity): Int
    suspend fun updateDomainEntry(entry: SmokingEntry): Int
    suspend fun removeEntryById(id: Long): Boolean
    suspend fun updateEntryTimestampById(id: Long, newTimestamp: Long): Boolean
    suspend fun updateEntryTriggerById(id: Long, trigger: String?): Boolean
    suspend fun updateEntryResistedById(id: Long, isResisted: Boolean): Boolean
    suspend fun clearAndInsertEntries(entities: List<SmokingEntryEntity>): Boolean
    suspend fun replaceAllDomainEntries(entries: List<SmokingEntry>): Boolean
    suspend fun clearAllEntries(): Boolean

    companion object {
        operator fun invoke(
            smokingDao: SmokingDao,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
            defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
        ): SmokingRepository = RoomSmokingRepository(
            smokingDao = smokingDao,
            ioDispatcher = ioDispatcher,
            defaultDispatcher = defaultDispatcher
        )
    }
}

class RoomSmokingRepository(
    private val smokingDao: SmokingDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : SmokingRepository {

    override val smokingEntries: Flow<List<SmokingEntryEntity>> = smokingDao.getAllEntriesFlow()
        .catchDatabaseError(emptyList())
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override val nonResistedEntries: Flow<List<SmokingEntryEntity>> = smokingDao.getEntriesByResistedFlow(isResisted = false)
        .catchDatabaseError(emptyList())
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override val nonResistedTimestamps: Flow<List<Long>> = smokingDao.getNonResistedTimestampsFlow()
        .catchDatabaseError(emptyList())
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override val resistedEntries: Flow<List<SmokingEntryEntity>> = smokingDao.getEntriesByResistedFlow(isResisted = true)
        .catchDatabaseError(emptyList())
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override val entries: Flow<List<SmokingEntry>> = smokingEntries
        .map { it.toDomainList() }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override val smokedEntries: Flow<List<SmokingEntry>> = nonResistedEntries
        .map { it.toDomainList() }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override val resistedDomainEntries: Flow<List<SmokingEntry>> = resistedEntries
        .map { it.toDomainList() }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override suspend fun getAllEntries(): List<SmokingEntryEntity> = safeDbCall(emptyList()) {
        smokingDao.getAllEntriesList()
    }

    override suspend fun getAllDomainEntries(): List<SmokingEntry> = safeDbCall(emptyList()) {
        smokingDao.getAllEntriesList().toDomainList()
    }

    override suspend fun getLastEntry(): SmokingEntryEntity? = safeDbCall(null) {
        smokingDao.getLastEntry()
    }

    override suspend fun getLastDomainEntry(): SmokingEntry? = safeDbCall(null) {
        smokingDao.getLastEntry()?.toDomain()
    }

    override fun getLastEntryFlow(): Flow<SmokingEntryEntity?> = smokingDao.getLastEntryFlow()
        .catchDatabaseError(null)
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override fun getLastDomainEntryFlow(): Flow<SmokingEntry?> = getLastEntryFlow()
        .map { it?.toDomain() }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override suspend fun getLastSmokedEntry(): SmokingEntryEntity? = safeDbCall(null) {
        smokingDao.getLastSmokedEntry()
    }

    override suspend fun getLastSmokedDomainEntry(): SmokingEntry? = safeDbCall(null) {
        smokingDao.getLastSmokedEntry()?.toDomain()
    }

    override fun getLastSmokedEntryFlow(): Flow<SmokingEntryEntity?> = smokingDao.getLastSmokedEntryFlow()
        .catchDatabaseError(null)
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override fun getLastSmokedDomainEntryFlow(): Flow<SmokingEntry?> = getLastSmokedEntryFlow()
        .map { it?.toDomain() }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    override fun getEntriesInRangeFlow(startMillis: Long, endMillis: Long): Flow<List<SmokingEntryEntity>> {
        val start = minOf(startMillis, endMillis)
        val end = maxOf(startMillis, endMillis)
        return smokingDao.getEntriesInRangeFlow(start, end)
            .catchDatabaseError(emptyList())
            .distinctUntilChanged()
            .flowOn(defaultDispatcher)
    }

    override fun getDomainEntriesInRangeFlow(startMillis: Long, endMillis: Long): Flow<List<SmokingEntry>> {
        return getEntriesInRangeFlow(startMillis, endMillis)
            .map { it.toDomainList() }
            .distinctUntilChanged()
            .flowOn(defaultDispatcher)
    }

    override suspend fun getEntriesInRange(startMillis: Long, endMillis: Long): List<SmokingEntryEntity> =
        safeDbCall(emptyList()) {
            val start = minOf(startMillis, endMillis)
            val end = maxOf(startMillis, endMillis)
            smokingDao.getEntriesInRangeList(start, end)
        }

    override suspend fun getDomainEntriesInRange(startMillis: Long, endMillis: Long): List<SmokingEntry> =
        safeDbCall(emptyList()) {
            val start = minOf(startMillis, endMillis)
            val end = maxOf(startMillis, endMillis)
            smokingDao.getEntriesInRangeList(start, end).toDomainList()
        }

    override suspend fun getEntryCountInRange(
        startMillis: Long,
        endMillis: Long,
        isResisted: Boolean
    ): Int = safeDbCall(0) {
        val start = minOf(startMillis, endMillis)
        val end = maxOf(startMillis, endMillis)
        smokingDao.getEntryCountInRange(start, end, isResisted)
    }

    override suspend fun addEntry(timestamp: Long, trigger: String?): Long {
        if (timestamp <= 0L) return -1L
        return safeDbCall(-1L) {
            smokingDao.insertEntry(
                SmokingEntryEntity(
                    timestamp = timestamp,
                    trigger = sanitizeTrigger(trigger),
                    isResisted = false
                )
            )
        }
    }

    override suspend fun addResistedEntry(timestamp: Long, trigger: String?): Long {
        if (timestamp <= 0L) return -1L
        return safeDbCall(-1L) {
            smokingDao.insertEntry(
                SmokingEntryEntity(
                    timestamp = timestamp,
                    trigger = sanitizeTrigger(trigger),
                    isResisted = true
                )
            )
        }
    }

    override suspend fun updateEntry(entry: SmokingEntryEntity): Int {
        if (entry.id <= 0L || entry.timestamp <= 0L) return 0
        return safeDbCall(0) {
            smokingDao.updateEntry(entry.copy(trigger = sanitizeTrigger(entry.trigger)))
        }
    }

    override suspend fun updateDomainEntry(entry: SmokingEntry): Int {
        return updateEntry(entry.toEntity())
    }

    override suspend fun removeEntryById(id: Long): Boolean {
        if (id <= 0L) return false
        return safeDbCall(false) {
            smokingDao.deleteEntryById(id) > 0
        }
    }

    override suspend fun updateEntryTimestampById(id: Long, newTimestamp: Long): Boolean {
        if (id <= 0L || newTimestamp <= 0L) return false
        return safeDbCall(false) {
            smokingDao.updateEntryTimestampById(id, newTimestamp) > 0
        }
    }

    override suspend fun updateEntryTriggerById(id: Long, trigger: String?): Boolean {
        if (id <= 0L) return false
        return safeDbCall(false) {
            smokingDao.updateEntryTriggerById(id, sanitizeTrigger(trigger)) > 0
        }
    }

    override suspend fun updateEntryResistedById(id: Long, isResisted: Boolean): Boolean {
        if (id <= 0L) return false
        return safeDbCall(false) {
            smokingDao.updateEntryResistedById(id, isResisted) > 0
        }
    }

    override suspend fun clearAndInsertEntries(entities: List<SmokingEntryEntity>): Boolean =
        safeDbCall(false) {
            val sanitized = entities
                .filter { it.timestamp > 0L }
                .map {
                    it.copy(
                        id = 0L,
                        trigger = sanitizeTrigger(it.trigger)
                    )
                }
            smokingDao.clearAndInsertEntries(sanitized)
            true
        }

    override suspend fun replaceAllDomainEntries(entries: List<SmokingEntry>): Boolean {
        return clearAndInsertEntries(entries.toEntityList())
    }

    override suspend fun clearAllEntries(): Boolean = safeDbCall(false) {
        smokingDao.clearAllEntries()
        true
    }

    private fun sanitizeTrigger(trigger: String?): String? {
        val normalized = trigger?.let(TriggerItem::normalizeCustomName)?.takeIf { it.isNotEmpty() }
            ?: return null
        return TriggerType.fromKey(normalized)?.key ?: normalized
    }

    private fun <T> Flow<T>.catchDatabaseError(fallback: T): Flow<T> = catch { exception ->
        if (exception is CancellationException) throw exception
        if (exception is SQLException || exception is IOException || exception is IllegalStateException) {
            Log.e(TAG, "Database flow error", exception)
            emit(fallback)
        } else {
            throw exception
        }
    }

    private suspend inline fun <T> safeDbCall(
        fallback: T,
        crossinline block: suspend () -> T
    ): T = withContext(ioDispatcher) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: SQLException) {
            Log.e(TAG, "SQLite operation failed", e)
            fallback
        } catch (e: IOException) {
            Log.e(TAG, "Database I/O operation failed", e)
            fallback
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Database state error", e)
            fallback
        }
    }

    companion object {
        private const val TAG = "SmokingRepository"
    }
}
