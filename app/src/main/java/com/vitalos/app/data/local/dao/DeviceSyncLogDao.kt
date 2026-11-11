package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceSyncLogDao {
    @Query("SELECT * FROM device_sync_log ORDER BY timestampEpochMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<DeviceSyncLogEntity>>

    @Insert
    suspend fun insert(log: DeviceSyncLogEntity): Long

    @Query("SELECT COUNT(*) FROM device_sync_log")
    suspend fun count(): Int
}
