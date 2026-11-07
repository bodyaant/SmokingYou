package com.smokingtracker.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "smoking_entries",
    indices = [Index(value = ["timestamp"])]
)
data class SmokingEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    @ColumnInfo(name = "trigger_name") val trigger: String? = null,
    @ColumnInfo(defaultValue = "0") val isResisted: Boolean = false
)
