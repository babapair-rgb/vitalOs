package com.vitalos.app.data.repository

import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import kotlinx.coroutines.flow.Flow

data class SleepSessionWithSegments(
    val session: SleepSessionEntity,
    val segments: List<SleepStageSegmentEntity>,
)

interface HealthDataRepository {
    fun observeProfile(): Flow<ProfileEntity?>
    fun observeTodaySnapshot(): Flow<DailyMetricsSnapshotEntity?>
    fun observeSnapshotRange(days: Int): Flow<List<DailyMetricsSnapshotEntity>>
    fun observeRecentHeartRate(windowMinutes: Int): Flow<List<HeartRateSampleEntity>>
    fun observeLatestHeartRate(): Flow<HeartRateSampleEntity?>
    fun observeTodaySleepSession(): Flow<SleepSessionWithSegments?>
    fun observeRecentSleepSessions(limit: Int): Flow<List<SleepSessionEntity>>
    fun observeTodayActivity(): Flow<List<ActivitySampleEntity>>
    fun observeActivityRange(days: Int): Flow<List<ActivitySampleEntity>>
    fun observeRecentWorkouts(limit: Int): Flow<List<WorkoutSessionEntity>>
    fun observeWorkout(id: Long): Flow<WorkoutSessionEntity?>
    fun observeRecentSyncLogs(limit: Int): Flow<List<DeviceSyncLogEntity>>
    suspend fun startWorkout(type: WorkoutType): Long
    suspend fun finishWorkout(
        id: Long,
        durationSeconds: Int,
        estimatedCalories: Int,
        avgHeartRate: Int?,
        maxHeartRate: Int?,
        status: WorkoutStatus,
    )
    suspend fun addWaterIntake(ml: Int)
}
