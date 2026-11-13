package com.vitalos.app.testing

import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import com.vitalos.app.data.repository.HealthDataRepository
import com.vitalos.app.data.repository.SleepSessionWithSegments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeHealthDataRepository : HealthDataRepository {
    val profileFlow = MutableStateFlow<ProfileEntity?>(null)
    val todaySnapshotFlow = MutableStateFlow<DailyMetricsSnapshotEntity?>(null)
    val snapshotRangeFlow = MutableStateFlow<List<DailyMetricsSnapshotEntity>>(emptyList())
    val recentHeartRateFlow = MutableStateFlow<List<HeartRateSampleEntity>>(emptyList())
    val latestHeartRateFlow = MutableStateFlow<HeartRateSampleEntity?>(null)
    val todaySleepSessionFlow = MutableStateFlow<SleepSessionWithSegments?>(null)
    val recentSleepSessionsFlow = MutableStateFlow<List<SleepSessionEntity>>(emptyList())
    val todayActivityFlow = MutableStateFlow<List<ActivitySampleEntity>>(emptyList())
    val activityRangeFlow = MutableStateFlow<List<ActivitySampleEntity>>(emptyList())
    val recentWorkoutsFlow = MutableStateFlow<List<WorkoutSessionEntity>>(emptyList())
    val workoutFlows = mutableMapOf<Long, MutableStateFlow<WorkoutSessionEntity?>>()
    val recentSyncLogsFlow = MutableStateFlow<List<DeviceSyncLogEntity>>(emptyList())

    var nextWorkoutId = 1L
    var waterAddedMl = 0

    override fun observeProfile(): StateFlow<ProfileEntity?> = profileFlow
    override fun observeTodaySnapshot(): StateFlow<DailyMetricsSnapshotEntity?> = todaySnapshotFlow
    override fun observeSnapshotRange(days: Int): StateFlow<List<DailyMetricsSnapshotEntity>> = snapshotRangeFlow
    override fun observeRecentHeartRate(windowMinutes: Int): StateFlow<List<HeartRateSampleEntity>> = recentHeartRateFlow
    override fun observeLatestHeartRate(): StateFlow<HeartRateSampleEntity?> = latestHeartRateFlow
    override fun observeTodaySleepSession(): StateFlow<SleepSessionWithSegments?> = todaySleepSessionFlow
    override fun observeRecentSleepSessions(limit: Int): StateFlow<List<SleepSessionEntity>> = recentSleepSessionsFlow
    override fun observeTodayActivity(): StateFlow<List<ActivitySampleEntity>> = todayActivityFlow
    override fun observeActivityRange(days: Int): StateFlow<List<ActivitySampleEntity>> = activityRangeFlow
    override fun observeRecentWorkouts(limit: Int): StateFlow<List<WorkoutSessionEntity>> = recentWorkoutsFlow
    override fun observeWorkout(id: Long): StateFlow<WorkoutSessionEntity?> =
        workoutFlows.getOrPut(id) { MutableStateFlow(null) }
    override fun observeRecentSyncLogs(limit: Int): StateFlow<List<DeviceSyncLogEntity>> = recentSyncLogsFlow

    override suspend fun startWorkout(type: WorkoutType): Long {
        val id = nextWorkoutId++
        workoutFlows[id] = MutableStateFlow(
            WorkoutSessionEntity(id = id, type = type, startEpochMillis = 0, endEpochMillis = null, durationSeconds = 0, estimatedCalories = 0, avgHeartRate = null, maxHeartRate = null, status = WorkoutStatus.IN_PROGRESS),
        )
        return id
    }

    override suspend fun finishWorkout(id: Long, durationSeconds: Int, estimatedCalories: Int, avgHeartRate: Int?, maxHeartRate: Int?, status: WorkoutStatus) {
        val flow = workoutFlows.getOrPut(id) { MutableStateFlow(null) }
        val current = flow.value ?: return
        flow.value = current.copy(durationSeconds = durationSeconds, estimatedCalories = estimatedCalories, avgHeartRate = avgHeartRate, maxHeartRate = maxHeartRate, status = status)
    }

    override suspend fun addWaterIntake(ml: Int) {
        waterAddedMl += ml
    }

    override suspend fun updateProfile(name: String, dailyStepGoal: Int, dailyHydrationGoalMl: Int, dailyCalorieGoal: Int) {
        val current = profileFlow.value
        profileFlow.value = (current ?: ProfileEntity(
            name = name, tierBadge = "PRO LAB", dailyStepGoal = dailyStepGoal,
            dailyHydrationGoalMl = dailyHydrationGoalMl, dailyCalorieGoal = dailyCalorieGoal,
        )).copy(
            name = name,
            dailyStepGoal = dailyStepGoal,
            dailyHydrationGoalMl = dailyHydrationGoalMl,
            dailyCalorieGoal = dailyCalorieGoal,
        )
    }
}
