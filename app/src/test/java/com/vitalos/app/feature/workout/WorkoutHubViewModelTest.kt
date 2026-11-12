package com.vitalos.app.feature.workout

import app.cash.turbine.test
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
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
class WorkoutHubViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeHealthDataRepository
    private lateinit var viewModel: WorkoutHubViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeHealthDataRepository()
        viewModel = WorkoutHubViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_countsYogaSessionsAmongRecentWorkouts() = runTest {
        repo.recentWorkoutsFlow.value = listOf(
            WorkoutSessionEntity(id = 1, type = WorkoutType.YOGA, startEpochMillis = 0, endEpochMillis = 1, durationSeconds = 1800, estimatedCalories = 180, avgHeartRate = 98, maxHeartRate = 120, status = WorkoutStatus.COMPLETED),
            WorkoutSessionEntity(id = 2, type = WorkoutType.RUNNING, startEpochMillis = 0, endEpochMillis = 1, durationSeconds = 1200, estimatedCalories = 220, avgHeartRate = 140, maxHeartRate = 160, status = WorkoutStatus.COMPLETED),
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(2, state.recentWorkouts.size)
            assertEquals(1, state.weeklyYogaCount)
        }
    }

    @Test
    fun startYoga_invokesCallbackWithNewWorkoutId() = runTest {
        var startedId: Long? = null
        viewModel.startYoga { startedId = it }
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1L, startedId)
    }
}
