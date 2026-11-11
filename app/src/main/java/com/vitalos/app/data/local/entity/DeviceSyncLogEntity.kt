package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_sync_log")
data class DeviceSyncLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMillis: Long,
    val recordsMerged: Int,
    val success: Boolean,
    val errorMessage: String?,
)
