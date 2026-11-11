package com.vitalos.app.feature.home

import app.cash.turbine.test
import com.vitalos.app.data.ble.BleConnectionPhase
import com.vitalos.app.data.ble.BleConnectionState
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
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
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var healthRepo: FakeHealthDataRepository
    private lateinit var bleRepo: FakeBleRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        healthRepo = FakeHealthDataRepository()
        bleRepo = FakeBleRepository()
        viewModel = HomeViewModel(healthRepo, bleRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_mapsRepositoryDataIntoDashboardFields() = runTest {
        healthRepo.profileFlow.value = ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400)
        healthRepo.todaySnapshotFlow.value = DailyMetricsSnapshotEntity(
            dateEpochDay = 1, readinessPercent = 94, healthScore = 89, strainTarget = 14.5,
            steps = 8420, distanceMeters = 6200.0, activeCalories = 540, hydrationMl = 2100,
            proteinGrams = 142, carbsGrams = 180, fatGrams = 55, calorieTarget = 2400, caloriesConsumed = 1780,
        )
        healthRepo.recentHeartRateFlow.value = listOf(
            HeartRateSampleEntity(timestampEpochMillis = 1, bpm = 60, isResting = true),
            HeartRateSampleEntity(timestampEpochMillis = 2, bpm = 65, isResting = false),
        )
        bleRepo.connectionStateFlow.value = BleConnectionState(phase = BleConnectionPhase.CONNECTED)
        bleRepo.liveHeartRateFlow.value = 64

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals("Alex", state.greetingName)
            assertEquals(94, state.readinessPercent)
            assertEquals(8420, state.steps)
            assertEquals(60, state.restingHr)
            assertEquals(64, state.liveHr)
            assertTrue(state.bleConnected)
        }
    }

    @Test
    fun quickAddWater_callsRepositoryAddWaterIntake() = runTest {
        viewModel.quickAddWater()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(250, healthRepo.waterAddedMl)
    }

    @Test
    fun manualSync_callsBleRepositorySync() = runTest {
        viewModel.manualSync()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(bleRepo.syncCalled)
    }
}
