package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateDao {
    @Query("SELECT * FROM heart_rate_sample WHERE timestampEpochMillis >= :sinceEpochMillis ORDER BY timestampEpochMillis ASC")
    fun observeSince(sinceEpochMillis: Long): Flow<List<HeartRateSampleEntity>>

    @Query("SELECT * FROM heart_rate_sample ORDER BY timestampEpochMillis DESC LIMIT 1")
    fun observeLatest(): Flow<HeartRateSampleEntity?>

    @Insert
    suspend fun insert(sample: HeartRateSampleEntity): Long

    @Insert
    suspend fun insertAll(samples: List<HeartRateSampleEntity>)

    @Query("SELECT COUNT(*) FROM heart_rate_sample")
    suspend fun count(): Int
}
