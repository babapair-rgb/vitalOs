package com.vitalos.app.data.ble

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.vitalos.app.data.local.VitalOsDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MockTaggVerveBleRepositoryTest {

    private lateinit var db: VitalOsDatabase
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

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

    private fun newRepository() = MockTaggVerveBleRepository(
        heartRateDao = db.heartRateDao(),
        deviceSyncLogDao = db.deviceSyncLogDao(),
        // The mock's live-heart-rate loop runs forever until disconnect() cancels it, so it must
        // be launched on backgroundScope (auto-cancelled when the test ends) rather than on the
        // TestScope itself, which requires every coroutine it launches to complete normally.
        externalScope = scope.backgroundScope,
        dispatcher = dispatcher,
    )

    @Test
    fun startScan_thenConnect_transitionsThroughExpectedPhases() = scope.runTest {
        val repo = newRepository()
        repo.connectionState.test {
            assertEquals(BleConnectionPhase.DISCONNECTED, awaitItem().phase)
            repo.startScan()
            advanceTimeBy(1_600)
            assertEquals(BleConnectionPhase.SCANNING, awaitItem().phase)
            val device = repo.discoveredDevices.value.first()
            repo.connect(device.id)
            assertEquals(BleConnectionPhase.CONNECTING, awaitItem().phase)
            advanceTimeBy(900)
            val connected = awaitItem()
            assertEquals(BleConnectionPhase.CONNECTED, connected.phase)
            assertNotNull(connected.batteryPercent)
        }
    }

    @Test
    fun sync_whenConnected_writesHeartRateAndSyncLogRows() = scope.runTest {
        val repo = newRepository()
        repo.startScan()
        advanceTimeBy(1_600)
        val device = repo.discoveredDevices.value.first()
        repo.connect(device.id)
        advanceTimeBy(900)

        val result = repo.sync()

        assertTrue(result.success)
        assertTrue(result.recordsMerged > 0)
        assertTrue(db.heartRateDao().count() > 0)
        val logs = db.deviceSyncLogDao().observeRecent(10).first()
        assertEquals(1, logs.size)
    }

    @Test
    fun liveHeartRate_whileConnected_emitsVaryingValuesAroundBaseline() = scope.runTest {
        val repo = newRepository()
        repo.startScan()
        advanceTimeBy(1_600)
        repo.connect(repo.discoveredDevices.value.first().id)
        advanceTimeBy(900)

        assertEquals(64, repo.liveHeartRate.value)
        advanceTimeBy(3_600)
        val bpm = repo.liveHeartRate.value
        assertNotNull(bpm)
        assertTrue(bpm!! in 60..68)
    }
}
