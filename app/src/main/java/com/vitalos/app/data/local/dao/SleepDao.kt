package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_session WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun observeSessionForDate(dateEpochDay: Long): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_session ORDER BY dateEpochDay DESC LIMIT :limit")
    fun observeRecentSessions(limit: Int): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_stage_segment WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun observeSegments(sessionId: Long): Flow<List<SleepStageSegmentEntity>>

    @Insert
    suspend fun insertSession(session: SleepSessionEntity): Long

    @Insert
    suspend fun insertSegments(segments: List<SleepStageSegmentEntity>)

    @Query("SELECT COUNT(*) FROM sleep_session")
    suspend fun countSessions(): Int
}
