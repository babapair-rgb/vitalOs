package com.vitalos.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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
}
