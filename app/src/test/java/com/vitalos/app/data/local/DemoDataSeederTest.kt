package com.vitalos.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class DemoDataSeederTest {

    private lateinit var db: VitalOsDatabase
    private lateinit var seeder: DemoDataSeeder

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VitalOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seeder = DemoDataSeeder(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun seedIfEmpty_populatesAllTables() = runTest {
        seeder.seedIfEmpty()

        assertEquals(1, db.profileDao().count())
        assertEquals(14, db.dailyMetricsDao().count())
        assertTrue(db.activityDao().count() > 0)
        assertTrue(db.heartRateDao().count() > 0)
        assertEquals(1, db.sleepDao().countSessions())
        assertEquals(1, db.workoutDao().count())
    }

    @Test
    fun seedIfEmpty_calledTwice_doesNotDuplicate() = runTest {
        seeder.seedIfEmpty()
        seeder.seedIfEmpty()

        assertEquals(1, db.profileDao().count())
        assertEquals(14, db.dailyMetricsDao().count())
    }

    @Test
    fun ensureTodayIsPopulated_whenTodayAlreadyHasData_isANoOp() = runTest {
        seeder.seedIfEmpty()
        val today = LocalDate.now().toEpochDay()
        // seedIfEmpty() already populated "today" (offset 0 of the historical seed), so this must
        // not add another daily snapshot, activity rows, or sleep session for today.
        val dailyCountBefore = db.dailyMetricsDao().count()
        val activityCountBefore = db.activityDao().count()
        val sleepCountBefore = db.sleepDao().countSessions()

        seeder.ensureTodayIsPopulated()

        assertEquals(dailyCountBefore, db.dailyMetricsDao().count())
        assertEquals(activityCountBefore, db.activityDao().count())
        assertEquals(sleepCountBefore, db.sleepDao().countSessions())
        assertNotNull(db.dailyMetricsDao().observeByDate(today).first())
    }

    @Test
    fun ensureTodayIsPopulated_whenTodayIsMissing_populatesASnapshotActivityAndSleepForToday() = runTest {
        val today = LocalDate.now().toEpochDay()
        // Simulate a day after install: only historical data exists, dated strictly before today.
        db.profileDao().upsert(com.vitalos.app.data.local.entity.ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400))
        db.dailyMetricsDao().upsert(
            DailyMetricsSnapshotEntity(
                dateEpochDay = today - 1, readinessPercent = 75, healthScore = 75, strainTarget = 12.0,
                steps = 6000, distanceMeters = 4000.0, activeCalories = 320, hydrationMl = 1800,
                proteinGrams = 110, carbsGrams = 140, fatGrams = 45, calorieTarget = 2400, caloriesConsumed = 1700,
            ),
        )
        assertEquals(1, db.dailyMetricsDao().count())
        assertEquals(0, db.sleepDao().countSessions())

        seeder.ensureTodayIsPopulated()

        assertEquals(2, db.dailyMetricsDao().count())
        assertNotNull(db.dailyMetricsDao().observeByDate(today).first())
        assertTrue(db.activityDao().observeForDate(today).first().isNotEmpty())
        assertEquals(1, db.sleepDao().countSessions())
    }

    @Test
    fun ensureTodayIsPopulated_whenHeartRateIsStale_insertsFreshSamples() = runTest {
        val today = LocalDate.now().toEpochDay()
        // Today's snapshot already exists (e.g. from a previous launch, or a water-intake tap
        // that created a zero-filled row), but heart-rate samples are stale - this must be
        // checked independently of the "today" snapshot check, not gated on it.
        db.profileDao().upsert(com.vitalos.app.data.local.entity.ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400))
        db.dailyMetricsDao().upsert(
            DailyMetricsSnapshotEntity(
                dateEpochDay = today, readinessPercent = 75, healthScore = 75, strainTarget = 12.0,
                steps = 6000, distanceMeters = 4000.0, activeCalories = 320, hydrationMl = 1800,
                proteinGrams = 110, carbsGrams = 140, fatGrams = 45, calorieTarget = 2400, caloriesConsumed = 1700,
            ),
        )
        val staleCutoff = System.currentTimeMillis() - 60 * 60_000L
        db.heartRateDao().insertAll(
            listOf(HeartRateSampleEntity(timestampEpochMillis = staleCutoff, bpm = 70, isResting = true)),
        )
        val countBefore = db.heartRateDao().count()
        assertEquals(0, db.heartRateDao().observeSince(System.currentTimeMillis() - 30 * 60_000L).first().size)

        seeder.ensureTodayIsPopulated()

        assertTrue(db.heartRateDao().count() > countBefore)
        assertTrue(db.heartRateDao().observeSince(System.currentTimeMillis() - 30 * 60_000L).first().isNotEmpty())
        // The "today" snapshot check was a no-op (it already existed) - only heart rate changed.
        assertEquals(1, db.dailyMetricsDao().count())
    }

    @Test
    fun ensureTodayIsPopulated_whenHeartRateIsRecent_isANoOpForHeartRate() = runTest {
        seeder.seedIfEmpty()
        // seedIfEmpty() already seeds heart-rate samples up through "now", so they're fresh.
        val countBefore = db.heartRateDao().count()

        seeder.ensureTodayIsPopulated()

        assertEquals(countBefore, db.heartRateDao().count())
    }
}
