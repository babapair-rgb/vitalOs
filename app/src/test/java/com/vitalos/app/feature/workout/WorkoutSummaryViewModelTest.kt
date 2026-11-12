package com.vitalos.app.feature.workout

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
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
class WorkoutSummaryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeHealthDataRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeHealthDataRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_loadsCompletedWorkoutById() = runTest {
        val id = repo.startWorkout(WorkoutType.YOGA)
        repo.finishWorkout(id, durationSeconds = 1800, estimatedCalories = 180, avgHeartRate = 98, maxHeartRate = 120, status = WorkoutStatus.COMPLETED)
        val viewModel = WorkoutSummaryViewModel(SavedStateHandle(mapOf("workoutId" to id)), repo)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(WorkoutType.YOGA, state.type)
            assertEquals(1800, state.durationSeconds)
            assertEquals(180, state.estimatedCalories)
        }
    }
}
