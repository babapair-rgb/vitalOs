package com.vitalos.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val healthDataRepository: HealthDataRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        viewModelScope.launch {
            val profile = healthDataRepository.observeProfile().first()
            _uiState.value = if (profile != null) {
                ProfileUiState(
                    isLoading = false,
                    name = profile.name,
                    tierBadge = profile.tierBadge,
                    dailyStepGoal = profile.dailyStepGoal.toString(),
                    dailyHydrationGoalMl = profile.dailyHydrationGoalMl.toString(),
                    dailyCalorieGoal = profile.dailyCalorieGoal.toString(),
                )
            } else {
                _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun onStepGoalChanged(value: String) {
        _uiState.value = _uiState.value.copy(dailyStepGoal = value)
    }

    fun onHydrationGoalChanged(value: String) {
        _uiState.value = _uiState.value.copy(dailyHydrationGoalMl = value)
    }

    fun onCalorieGoalChanged(value: String) {
        _uiState.value = _uiState.value.copy(dailyCalorieGoal = value)
    }

    fun save() {
        val state = _uiState.value
        val stepGoal = state.dailyStepGoal.toIntOrNull()
        val hydrationGoal = state.dailyHydrationGoalMl.toIntOrNull()
        val calorieGoal = state.dailyCalorieGoal.toIntOrNull()
        if (state.name.isBlank() || stepGoal == null || hydrationGoal == null || calorieGoal == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            healthDataRepository.updateProfile(state.name.trim(), stepGoal, hydrationGoal, calorieGoal)
            _uiState.value = _uiState.value.copy(isSaving = false, saveSuccessEvent = _uiState.value.saveSuccessEvent + 1)
        }
    }
}
