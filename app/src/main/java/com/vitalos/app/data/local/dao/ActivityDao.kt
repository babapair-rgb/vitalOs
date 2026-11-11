package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vitalos.app.data.local.entity.ActivitySampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_sample WHERE dateEpochDay = :dateEpochDay ORDER BY hourOfDay ASC")
    fun observeForDate(dateEpochDay: Long): Flow<List<ActivitySampleEntity>>

    @Query("SELECT * FROM activity_sample WHERE dateEpochDay BETWEEN :fromEpochDay AND :toEpochDay ORDER BY dateEpochDay ASC, hourOfDay ASC")
    fun observeRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<ActivitySampleEntity>>

    @Upsert
    suspend fun upsertAll(samples: List<ActivitySampleEntity>)

    @Query("SELECT COUNT(*) FROM activity_sample")
    suspend fun count(): Int
}
