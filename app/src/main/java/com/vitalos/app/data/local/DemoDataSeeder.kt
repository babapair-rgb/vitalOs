package com.vitalos.app.data.local

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
import java.time.LocalDate
import kotlin.random.Random

class DemoDataSeeder(private val database: VitalOsDatabase) {

    suspend fun seedIfEmpty() {
        if (database.profileDao().count() > 0) return

        val today = LocalDate.now().toEpochDay()
        val now = System.currentTimeMillis()
        val random = Random(seed = 42)

        database.profileDao().upsert(
            ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400),
        )

        val snapshots = (0..13).map { offset ->
            val day = today - offset
            DailyMetricsSnapshotEntity(
                dateEpochDay = day,
                readinessPercent = 70 + random.nextInt(0, 25),
                healthScore = 70 + random.nextInt(0, 20),
                strainTarget = 10.0 + random.nextInt(0, 8),
                steps = 5000 + random.nextInt(0, 6000),
                distanceMeters = 3000.0 + random.nextInt(0, 5000),
                activeCalories = 300 + random.nextInt(0, 400),
                hydrationMl = 1200 + random.nextInt(0, 1800),
                proteinGrams = 100 + random.nextInt(0, 80),
                carbsGrams = 120 + random.nextInt(0, 100),
                fatGrams = 40 + random.nextInt(0, 30),
                calorieTarget = 2400,
                caloriesConsumed = 1500 + random.nextInt(0, 900),
            )
        }
        database.dailyMetricsDao().upsertAll(snapshots)

        val hourlyActivity = (6..21).map { hour ->
            ActivitySampleEntity(
                dateEpochDay = today,
                hourOfDay = hour,
                steps = random.nextInt(0, 1200),
                intensity = random.nextInt(0, 4),
            )
        }
        database.activityDao().upsertAll(hourlyActivity)

        val heartRateSamples = (0 until 60).map { minutesAgo ->
            HeartRateSampleEntity(
                timestampEpochMillis = now - (59 - minutesAgo) * 60_000L,
                bpm = 58 + random.nextInt(0, 60),
                isResting = minutesAgo % 5 == 0,
            )
        }
        database.heartRateDao().insertAll(heartRateSamples)

        val sessionId = database.sleepDao().insertSession(
            SleepSessionEntity(
                dateEpochDay = today,
                startEpochMillis = now - 8 * 60 * 60_000L,
                endEpochMillis = (now - 8 * 60 * 60_000L) + 490 * 60_000L,
                efficiencyPercent = 96.4,
                awakenings = 0,
                sleepScore = 91,
            ),
        )
        database.sleepDao().insertSegments(
            listOf(
                SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.AWAKE, durationMinutes = 22, orderIndex = 0),
                SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.LIGHT, durationMinutes = 236, orderIndex = 1),
                SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.REM, durationMinutes = 130, orderIndex = 2),
                SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.DEEP, durationMinutes = 102, orderIndex = 3),
            ),
        )

        database.workoutDao().insert(
            WorkoutSessionEntity(
                type = WorkoutType.YOGA,
                startEpochMillis = now - 26 * 60 * 60_000L,
                endEpochMillis = now - 25 * 60 * 60_000L - 25 * 60_000L,
                durationSeconds = 35 * 60,
                estimatedCalories = 180,
                avgHeartRate = 98,
                maxHeartRate = 124,
                status = WorkoutStatus.COMPLETED,
            ),
        )

        database.deviceSyncLogDao().insert(
            DeviceSyncLogEntity(timestampEpochMillis = now - 4 * 60_000L, recordsMerged = 42, success = true, errorMessage = null),
        )
    }
}
