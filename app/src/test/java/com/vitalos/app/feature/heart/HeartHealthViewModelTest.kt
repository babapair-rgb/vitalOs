package com.vitalos.app.feature.heart

import app.cash.turbine.test
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
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
class HeartHealthViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var healthRepo: FakeHealthDataRepository
    private lateinit var bleRepo: FakeBleRepository
    private lateinit var viewModel: HeartHealthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        healthRepo = FakeHealthDataRepository()
        bleRepo = FakeBleRepository()
        viewModel = HeartHealthViewModel(healthRepo, bleRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_prefersLiveHeartRateOverLatestSample() = runTest {
        healthRepo.latestHeartRateFlow.value = HeartRateSampleEntity(timestampEpochMillis = 1, bpm = 70, isResting = false)
        healthRepo.recentHeartRateFlow.value = listOf(
            HeartRateSampleEntity(timestampEpochMillis = 1, bpm = 58, isResting = true),
            HeartRateSampleEntity(timestampEpochMillis = 2, bpm = 130, isResting = false),
        )
        bleRepo.liveHeartRateFlow.value = 72

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(72, state.currentBpm)
            assertTrue(state.isLiveMeasured)
            assertEquals(58, state.restingBpm)
            assertEquals(1, state.zoneMinutes["Zone 1"])
            assertEquals(1, state.zoneMinutes["Zone 3"])
        }
    }
}
