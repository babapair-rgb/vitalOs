package com.vitalos.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VitalOsDatabaseTest {

    private lateinit var db: VitalOsDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VitalOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun profileDao_upsertThenObserve_emitsProfile() = runTest {
        db.profileDao().upsert(ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400))
        db.profileDao().observe().test {
            assertEquals("Alex", awaitItem()?.name)
        }
    }

    @Test
    fun dailyMetricsDao_upsertThenObserveByDate_emitsSnapshot() = runTest {
        val snapshot = DailyMetricsSnapshotEntity(
            dateEpochDay = 19000, readinessPercent = 94, healthScore = 89, strainTarget = 14.5,
            steps = 8420, distanceMeters = 6200.0, activeCalories = 540, hydrationMl = 2100,
            proteinGrams = 142, carbsGrams = 180, fatGrams = 55, calorieTarget = 2400, caloriesConsumed = 1780,
        )
        db.dailyMetricsDao().upsert(snapshot)
        db.dailyMetricsDao().observeByDate(19000).test {
            assertEquals(94, awaitItem()?.readinessPercent)
        }
    }

    @Test
    fun heartRateDao_insertThenObserveLatest_emitsMostRecentSample() = runTest {
        db.heartRateDao().insertAll(
            listOf(
                HeartRateSampleEntity(timestampEpochMillis = 1000, bpm = 60, isResting = true),
                HeartRateSampleEntity(timestampEpochMillis = 2000, bpm = 65, isResting = false),
            ),
        )
        db.heartRateDao().observeLatest().test {
            assertEquals(65, awaitItem()?.bpm)
        }
    }

    @Test
    fun sleepDao_insertSessionAndSegments_observesBoth() = runTest {
        val sessionId = db.sleepDao().insertSession(
            SleepSessionEntity(dateEpochDay = 19000, startEpochMillis = 0, endEpochMillis = 100, efficiencyPercent = 96.4, awakenings = 0, sleepScore = 91),
        )
        db.sleepDao().insertSegments(
            listOf(SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.DEEP, durationMinutes = 102, orderIndex = 0)),
        )
        db.sleepDao().observeSessionForDate(19000).test {
            assertEquals(91, awaitItem()?.sleepScore)
        }
        db.sleepDao().observeSegments(sessionId).test {
            assertEquals(SleepStageType.DEEP, awaitItem().first().stage)
        }
    }

    @Test
    fun activityDao_upsertAllThenObserveForDate_emitsHourlySamples() = runTest {
        db.activityDao().upsertAll(
            listOf(ActivitySampleEntity(dateEpochDay = 19000, hourOfDay = 8, steps = 1200, intensity = 2)),
        )
        db.activityDao().observeForDate(19000).test {
            assertEquals(1200, awaitItem().first().steps)
        }
    }

    @Test
    fun workoutDao_insertThenUpdate_persistsStatusChange() = runTest {
        val id = db.workoutDao().insert(
            WorkoutSessionEntity(type = WorkoutType.YOGA, startEpochMillis = 0, endEpochMillis = null, durationSeconds = 0, estimatedCalories = 0, avgHeartRate = null, maxHeartRate = null, status = WorkoutStatus.IN_PROGRESS),
        )
        val inProgress = db.workoutDao().observeById(id)
        inProgress.test { assertEquals(WorkoutStatus.IN_PROGRESS, awaitItem()?.status) }
        db.workoutDao().update(
            WorkoutSessionEntity(id = id, type = WorkoutType.YOGA, startEpochMillis = 0, endEpochMillis = 1800000, durationSeconds = 1800, estimatedCalories = 180, avgHeartRate = 98, maxHeartRate = 120, status = WorkoutStatus.COMPLETED),
        )
        db.workoutDao().observeById(id).test {
            assertEquals(WorkoutStatus.COMPLETED, awaitItem()?.status)
        }
    }

    @Test
    fun deviceSyncLogDao_insertThenObserveRecent_emitsLog() = runTest {
        db.deviceSyncLogDao().insert(DeviceSyncLogEntity(timestampEpochMillis = 5000, recordsMerged = 42, success = true, errorMessage = null))
        db.deviceSyncLogDao().observeRecent(10).test {
            assertEquals(42, awaitItem().first().recordsMerged)
        }
    }
}
