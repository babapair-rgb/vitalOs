package com.vitalos.app.feature.device

import app.cash.turbine.test
import com.vitalos.app.data.ble.BleConnectionPhase
import com.vitalos.app.data.ble.BleConnectionState
import com.vitalos.app.data.ble.BleDeviceInfo
import com.vitalos.app.testing.FakeBleRepository
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceManagementViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var bleRepo: FakeBleRepository
    private lateinit var healthRepo: FakeHealthDataRepository
    private lateinit var viewModel: DeviceManagementViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        bleRepo = FakeBleRepository()
        healthRepo = FakeHealthDataRepository()
        viewModel = DeviceManagementViewModel(bleRepo, healthRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_reflectsConnectionStateAndDiscoveredDevices() = runTest {
        bleRepo.connectionStateFlow.value = BleConnectionState(phase = BleConnectionPhase.CONNECTED, connectedDevice = BleDeviceInfo("TAGG-1234", "TAGG Verve · 1234", -40), batteryPercent = 82, lastSyncEpochMillis = 100)
        bleRepo.discoveredDevicesFlow.value = listOf(BleDeviceInfo("TAGG-1234", "TAGG Verve · 1234", -40))

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(BleConnectionPhase.CONNECTED, state.phase)
            assertEquals("TAGG Verve · 1234", state.connectedDeviceName)
            assertEquals(82, state.batteryPercent)
        }
    }

    @Test
    fun scan_delegatesToRepository() = runTest {
        viewModel.scan()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(bleRepo.scanCalled)
    }

    @Test
    fun connect_delegatesToRepositoryWithDeviceId() = runTest {
        viewModel.connect("TAGG-1234")
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("TAGG-1234", bleRepo.connectCalledWith)
    }

    @Test
    fun disconnect_delegatesToRepository() = runTest {
        viewModel.disconnect()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(bleRepo.disconnectCalled)
    }
}
