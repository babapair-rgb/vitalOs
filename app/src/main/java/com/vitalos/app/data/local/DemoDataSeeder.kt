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
import androidx.room.withTransaction
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlin.random.Random

class DemoDataSeeder(private val database: VitalOsDatabase) {

    suspend fun seedIfEmpty() {
        if (database.profileDao().count() > 0) return

        database.withTransaction {
            seedHistoricalData()
        }
    }

    private suspend fun seedHistoricalData() {
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

    /**
     * Keeps "today" populated on every app launch, not just the first one. [seedIfEmpty] only
     * ever runs once (guarded by the profile count), and all of its rows are anchored to the
     * install-day "today" - so the day after install, every screen querying "today's" data finds
     * nothing and renders zeros/empty states. This is idempotent and cheap, so it's safe to call
     * unconditionally on every launch.
     */
    suspend fun ensureTodayIsPopulated() {
        val today = LocalDate.now().toEpochDay()

        if (database.dailyMetricsDao().observeByDate(today).first() == null) {
            database.withTransaction {
                // Re-check inside the transaction in case another launch raced us to it.
                if (database.dailyMetricsDao().observeByDate(today).first() != null) return@withTransaction

                val random = Random(seed = today) // deterministic per-day, not reusing the historical seed's state

                val mostRecent = database.dailyMetricsDao().observeRange(today - 13, today).first()
                    .maxByOrNull { it.dateEpochDay }
                val template = mostRecent ?: DailyMetricsSnapshotEntity(
                    dateEpochDay = today, readinessPercent = 80, healthScore = 80, strainTarget = 14.0,
                    steps = 7000, distanceMeters = 5000.0, activeCalories = 350, hydrationMl = 1500,
                    proteinGrams = 120, carbsGrams = 150, fatGrams = 50, calorieTarget = 2400, caloriesConsumed = 1800,
                )
                database.dailyMetricsDao().upsert(
                    // Reset the carried-over intake fields - a copied template otherwise makes
                    // today look like water/food has already been logged before the user does
                    // anything.
                    template.copy(dateEpochDay = today, hydrationMl = 0, caloriesConsumed = 0),
                )

                val hourlyActivity = (6..21).map { hour ->
                    ActivitySampleEntity(dateEpochDay = today, hourOfDay = hour, steps = random.nextInt(0, 1200), intensity = random.nextInt(0, 4))
                }
                database.activityDao().upsertAll(hourlyActivity)

                // Anchor to a fixed recent-past window rather than launch time, so an afternoon
                // launch doesn't produce a sleep session that ends in the future.
                val now = System.currentTimeMillis()
                val sleepEnd = now - 60 * 60_000L
                val sleepStart = sleepEnd - 490 * 60_000L
                val sessionId = database.sleepDao().insertSession(
                    SleepSessionEntity(
                        dateEpochDay = today,
                        startEpochMillis = sleepStart,
                        endEpochMillis = sleepEnd,
                        efficiencyPercent = 96.0,
                        awakenings = 0,
                        sleepScore = 88,
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
            }
        }

        // The daily snapshot can already exist (e.g. a water-intake tap created a zero-filled
        // row for today) while heart-rate samples are still stale from a previous launch, so this
        // is checked independently rather than being gated on the "today" snapshot check above.
        ensureRecentHeartRateIsPopulated()
    }

    private suspend fun ensureRecentHeartRateIsPopulated() {
        val recentWindowStart = System.currentTimeMillis() - 30 * 60_000L
        if (database.heartRateDao().observeSince(recentWindowStart).first().isNotEmpty()) return

        database.withTransaction {
            // Re-check inside the transaction in case another launch raced us to it.
            if (database.heartRateDao().observeSince(System.currentTimeMillis() - 30 * 60_000L).first().isNotEmpty()) {
                return@withTransaction
            }

            val now = System.currentTimeMillis()
            val random = Random(seed = now)
            val heartRateSamples = (0 until 60).map { minutesAgo ->
                HeartRateSampleEntity(
                    timestampEpochMillis = now - (59 - minutesAgo) * 60_000L,
                    bpm = 58 + random.nextInt(0, 60),
                    isResting = minutesAgo % 5 == 0,
                )
            }
            database.heartRateDao().insertAll(heartRateSamples)
        }
    }
}
