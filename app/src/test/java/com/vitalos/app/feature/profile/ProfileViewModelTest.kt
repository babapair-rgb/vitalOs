package com.vitalos.app.feature.profile

import app.cash.turbine.test
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

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
    fun uiState_onLoad_populatesFieldsFromExistingProfile() = runTest {
        repo.profileFlow.value = ProfileEntity(
            name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000,
            dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400,
        )
        val viewModel = ProfileViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Alex", state.name)
        assertEquals("PRO LAB", state.tierBadge)
        assertEquals("10000", state.dailyStepGoal)
        assertEquals("3000", state.dailyHydrationGoalMl)
        assertEquals("2400", state.dailyCalorieGoal)
    }

    @Test
    fun onFieldChanged_updatesDraftStateWithoutSaving() = runTest {
        repo.profileFlow.value = ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400)
        val viewModel = ProfileViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onNameChanged("Jordan")
        viewModel.onStepGoalChanged("8000")

        assertEquals("Jordan", viewModel.uiState.value.name)
        assertEquals("8000", viewModel.uiState.value.dailyStepGoal)
        assertNotEquals("Jordan", repo.profileFlow.value?.name)
    }

    @Test
    fun save_withValidInput_callsUpdateProfileAndBumpsSuccessEvent() = runTest {
        repo.profileFlow.value = ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400)
        val viewModel = ProfileViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onNameChanged("Jordan")
        viewModel.onStepGoalChanged("8000")
        viewModel.onHydrationGoalChanged("2500")
        viewModel.onCalorieGoalChanged("2200")
        viewModel.save()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Jordan", repo.profileFlow.value?.name)
        assertEquals(8000, repo.profileFlow.value?.dailyStepGoal)
        assertEquals(2500, repo.profileFlow.value?.dailyHydrationGoalMl)
        assertEquals(2200, repo.profileFlow.value?.dailyCalorieGoal)
        assertEquals(1, viewModel.uiState.value.saveSuccessEvent)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun save_withBlankName_doesNotCallUpdateProfile() = runTest {
        repo.profileFlow.value = ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400)
        val viewModel = ProfileViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onNameChanged("   ")
        viewModel.save()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Alex", repo.profileFlow.value?.name)
        assertEquals(0, viewModel.uiState.value.saveSuccessEvent)
    }

    @Test
    fun save_withNonNumericGoal_doesNotCallUpdateProfile() = runTest {
        repo.profileFlow.value = ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400)
        val viewModel = ProfileViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onStepGoalChanged("not-a-number")
        viewModel.save()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(10000, repo.profileFlow.value?.dailyStepGoal)
        assertEquals(0, viewModel.uiState.value.saveSuccessEvent)
    }
}
