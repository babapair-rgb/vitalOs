package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMetricsDao {
    @Query("SELECT * FROM daily_metrics_snapshot WHERE dateEpochDay = :dateEpochDay")
    fun observeByDate(dateEpochDay: Long): Flow<DailyMetricsSnapshotEntity?>

    @Query("SELECT * FROM daily_metrics_snapshot WHERE dateEpochDay BETWEEN :fromEpochDay AND :toEpochDay ORDER BY dateEpochDay ASC")
    fun observeRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<DailyMetricsSnapshotEntity>>

    @Upsert
    suspend fun upsert(snapshot: DailyMetricsSnapshotEntity)

    @Upsert
    suspend fun upsertAll(snapshots: List<DailyMetricsSnapshotEntity>)

    @Query("SELECT COUNT(*) FROM daily_metrics_snapshot")
    suspend fun count(): Int
}
