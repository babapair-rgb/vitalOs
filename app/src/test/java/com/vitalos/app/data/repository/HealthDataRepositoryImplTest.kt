package com.vitalos.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.vitalos.app.data.local.VitalOsDatabase
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class HealthDataRepositoryImplTest {

    private lateinit var db: VitalOsDatabase
    private lateinit var repository: HealthDataRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VitalOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HealthDataRepositoryImpl(
            profileDao = db.profileDao(),
            dailyMetricsDao = db.dailyMetricsDao(),
            heartRateDao = db.heartRateDao(),
            sleepDao = db.sleepDao(),
            activityDao = db.activityDao(),
            workoutDao = db.workoutDao(),
            deviceSyncLogDao = db.deviceSyncLogDao(),
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeTodaySleepSession_combinesSessionAndSegments() = runTest {
        val today = LocalDate.now().toEpochDay()
        val sessionId = db.sleepDao().insertSession(
            SleepSessionEntity(dateEpochDay = today, startEpochMillis = 0, endEpochMillis = 100, efficiencyPercent = 96.0, awakenings = 0, sleepScore = 91),
        )
        db.sleepDao().insertSegments(
            listOf(SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.DEEP, durationMinutes = 100, orderIndex = 0)),
        )

        repository.observeTodaySleepSession().test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(91, result?.session?.sleepScore)
            assertEquals(1, result?.segments?.size)
        }
    }

    @Test
    fun startWorkout_thenFinishWorkout_updatesStatusAndMetrics() = runTest {
        val id = repository.startWorkout(WorkoutType.YOGA)
        repository.finishWorkout(id, durationSeconds = 1800, estimatedCalories = 180, avgHeartRate = 98, maxHeartRate = 120, status = WorkoutStatus.COMPLETED)

        repository.observeWorkout(id).test {
            val workout = awaitItem()
            assertEquals(WorkoutStatus.COMPLETED, workout?.status)
            assertEquals(180, workout?.estimatedCalories)
        }
    }

    @Test
    fun addWaterIntake_incrementsTodaySnapshotHydration() = runTest {
        val today = LocalDate.now().toEpochDay()
        db.dailyMetricsDao().upsert(
            DailyMetricsSnapshotEntity(
                dateEpochDay = today, readinessPercent = 90, healthScore = 85, strainTarget = 12.0,
                steps = 1000, distanceMeters = 800.0, activeCalories = 100, hydrationMl = 500,
                proteinGrams = 10, carbsGrams = 10, fatGrams = 10, calorieTarget = 2400, caloriesConsumed = 300,
            ),
        )

        repository.addWaterIntake(250)

        repository.observeTodaySnapshot().test {
            assertEquals(750, awaitItem()?.hydrationMl)
        }
    }

    @Test
    fun updateProfile_whenProfileExists_updatesNameAndGoalsButKeepsTierBadge() = runTest {
        db.profileDao().upsert(
            com.vitalos.app.data.local.entity.ProfileEntity(
                name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000,
                dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400,
            ),
        )

        repository.updateProfile(name = "Jordan", dailyStepGoal = 8000, dailyHydrationGoalMl = 2500, dailyCalorieGoal = 2200)

        repository.observeProfile().test {
            val profile = awaitItem()
            assertEquals("Jordan", profile?.name)
            assertEquals(8000, profile?.dailyStepGoal)
            assertEquals(2500, profile?.dailyHydrationGoalMl)
            assertEquals(2200, profile?.dailyCalorieGoal)
            assertEquals("PRO LAB", profile?.tierBadge)
        }
    }
}
