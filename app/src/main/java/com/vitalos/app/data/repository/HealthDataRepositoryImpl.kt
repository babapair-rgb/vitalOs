package com.vitalos.app.data.repository

import com.vitalos.app.data.local.dao.ActivityDao
import com.vitalos.app.data.local.dao.DailyMetricsDao
import com.vitalos.app.data.local.dao.DeviceSyncLogDao
import com.vitalos.app.data.local.dao.HeartRateDao
import com.vitalos.app.data.local.dao.ProfileDao
import com.vitalos.app.data.local.dao.SleepDao
import com.vitalos.app.data.local.dao.WorkoutDao
import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthDataRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val dailyMetricsDao: DailyMetricsDao,
    private val heartRateDao: HeartRateDao,
    private val sleepDao: SleepDao,
    private val activityDao: ActivityDao,
    private val workoutDao: WorkoutDao,
    private val deviceSyncLogDao: DeviceSyncLogDao,
) : HealthDataRepository {

    private fun todayEpochDay() = LocalDate.now().toEpochDay()

    override fun observeProfile(): Flow<ProfileEntity?> = profileDao.observe()

    override fun observeTodaySnapshot(): Flow<DailyMetricsSnapshotEntity?> =
        dailyMetricsDao.observeByDate(todayEpochDay())

    override fun observeSnapshotRange(days: Int): Flow<List<DailyMetricsSnapshotEntity>> {
        val today = todayEpochDay()
        return dailyMetricsDao.observeRange(today - (days - 1), today)
    }

    override fun observeRecentHeartRate(windowMinutes: Int): Flow<List<HeartRateSampleEntity>> =
        heartRateDao.observeSince(System.currentTimeMillis() - windowMinutes * 60_000L)

    override fun observeLatestHeartRate(): Flow<HeartRateSampleEntity?> = heartRateDao.observeLatest()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTodaySleepSession(): Flow<SleepSessionWithSegments?> =
        sleepDao.observeSessionForDate(todayEpochDay()).flatMapLatest { session ->
            if (session == null) {
                flowOf(null)
            } else {
                sleepDao.observeSegments(session.id).map { segments -> SleepSessionWithSegments(session, segments) }
            }
        }

    override fun observeRecentSleepSessions(limit: Int): Flow<List<SleepSessionEntity>> =
        sleepDao.observeRecentSessions(limit)

    override fun observeTodayActivity(): Flow<List<ActivitySampleEntity>> =
        activityDao.observeForDate(todayEpochDay())

    override fun observeActivityRange(days: Int): Flow<List<ActivitySampleEntity>> {
        val today = todayEpochDay()
        return activityDao.observeRange(today - (days - 1), today)
    }

    override fun observeRecentWorkouts(limit: Int): Flow<List<WorkoutSessionEntity>> =
        workoutDao.observeRecent(limit)

    override fun observeWorkout(id: Long): Flow<WorkoutSessionEntity?> = workoutDao.observeById(id)

    override fun observeRecentSyncLogs(limit: Int): Flow<List<DeviceSyncLogEntity>> =
        deviceSyncLogDao.observeRecent(limit)

    override suspend fun startWorkout(type: WorkoutType): Long = workoutDao.insert(
        WorkoutSessionEntity(
            type = type,
            startEpochMillis = System.currentTimeMillis(),
            endEpochMillis = null,
            durationSeconds = 0,
            estimatedCalories = 0,
            avgHeartRate = null,
            maxHeartRate = null,
            status = WorkoutStatus.IN_PROGRESS,
        ),
    )

    override suspend fun finishWorkout(
        id: Long,
        durationSeconds: Int,
        estimatedCalories: Int,
        avgHeartRate: Int?,
        maxHeartRate: Int?,
        status: WorkoutStatus,
    ) {
        val current = workoutDao.observeById(id).first() ?: return
        workoutDao.update(
            current.copy(
                endEpochMillis = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                estimatedCalories = estimatedCalories,
                avgHeartRate = avgHeartRate,
                maxHeartRate = maxHeartRate,
                status = status,
            ),
        )
    }

    override suspend fun addWaterIntake(ml: Int) {
        val today = todayEpochDay()
        val current = dailyMetricsDao.observeByDate(today).first()
        val updated = (current ?: DailyMetricsSnapshotEntity(
            dateEpochDay = today, readinessPercent = 0, healthScore = 0, strainTarget = 0.0,
            steps = 0, distanceMeters = 0.0, activeCalories = 0, hydrationMl = 0,
            proteinGrams = 0, carbsGrams = 0, fatGrams = 0, calorieTarget = 2400, caloriesConsumed = 0,
        )).copy(hydrationMl = (current?.hydrationMl ?: 0) + ml)
        dailyMetricsDao.upsert(updated)
    }

    override suspend fun updateProfile(name: String, dailyStepGoal: Int, dailyHydrationGoalMl: Int, dailyCalorieGoal: Int) {
        val current = profileDao.observe().first()
        val updated = (current ?: ProfileEntity(
            name = name, tierBadge = "PRO LAB", dailyStepGoal = dailyStepGoal,
            dailyHydrationGoalMl = dailyHydrationGoalMl, dailyCalorieGoal = dailyCalorieGoal,
        )).copy(
            name = name,
            dailyStepGoal = dailyStepGoal,
            dailyHydrationGoalMl = dailyHydrationGoalMl,
            dailyCalorieGoal = dailyCalorieGoal,
        )
        profileDao.upsert(updated)
    }
}
