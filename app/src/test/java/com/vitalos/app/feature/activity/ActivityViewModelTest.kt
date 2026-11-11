package com.vitalos.app.feature.activity

import app.cash.turbine.test
import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
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
class ActivityViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeHealthDataRepository
    private lateinit var viewModel: ActivityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeHealthDataRepository()
        viewModel = ActivityViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun snapshot(day: Long, steps: Int) = DailyMetricsSnapshotEntity(
        dateEpochDay = day, readinessPercent = 0, healthScore = 0, strainTarget = 0.0,
        steps = steps, distanceMeters = steps * 0.8, activeCalories = steps / 20,
        hydrationMl = 0, proteinGrams = 0, carbsGrams = 0, fatGrams = 0, calorieTarget = 0, caloriesConsumed = 0,
    )

    @Test
    fun uiState_aggregatesSnapshotsAndActivityForSelectedRange() = runTest {
        repo.snapshotRangeFlow.value = listOf(snapshot(1, 7000), snapshot(2, 9000), snapshot(3, 10500))
        repo.activityRangeFlow.value = listOf(
            ActivitySampleEntity(dateEpochDay = 3, hourOfDay = 8, steps = 500, intensity = 3),
            ActivitySampleEntity(dateEpochDay = 3, hourOfDay = 9, steps = 0, intensity = 0),
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(26500, state.totalSteps)
            assertEquals(2, state.streakDays)
            assertEquals(60, state.activeMinutes)
            assertEquals(60, state.sedentaryMinutes)
        }
    }

    @Test
    fun selectRange_updatesSelectedRangeInState() = runTest {
        viewModel.selectRange(ActivityTimeRange.MONTHLY)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedRange != ActivityTimeRange.MONTHLY) state = awaitItem()
            assertEquals(ActivityTimeRange.MONTHLY, state.selectedRange)
        }
    }
}
