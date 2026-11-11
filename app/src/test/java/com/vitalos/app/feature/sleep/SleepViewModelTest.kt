package com.vitalos.app.feature.sleep

import app.cash.turbine.test
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.data.repository.SleepSessionWithSegments
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeHealthDataRepository
    private lateinit var viewModel: SleepViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeHealthDataRepository()
        viewModel = SleepViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_computesDurationEfficiencyAndSleepDebt() = runTest {
        val session = SleepSessionEntity(id = 1, dateEpochDay = 1, startEpochMillis = 0, endEpochMillis = 100, efficiencyPercent = 96.4, awakenings = 0, sleepScore = 91)
        val segments = listOf(
            SleepStageSegmentEntity(sessionId = 1, stage = SleepStageType.DEEP, durationMinutes = 100, orderIndex = 0),
            SleepStageSegmentEntity(sessionId = 1, stage = SleepStageType.LIGHT, durationMinutes = 236, orderIndex = 1),
        )
        repo.todaySleepSessionFlow.value = SleepSessionWithSegments(session, segments)
        repo.recentSleepSessionsFlow.value = listOf(session)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(336, state.lastNightDurationMinutes)
            assertEquals(91, state.sleepScore)
            assertEquals(144, state.sleepDebtMinutes)
            assertEquals(listOf(91), state.weeklyScores)
        }
    }
}
