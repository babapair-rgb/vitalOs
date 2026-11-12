package com.vitalos.app.feature.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WorkoutSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    healthDataRepository: HealthDataRepository,
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])

    val uiState: StateFlow<WorkoutSummaryUiState> = healthDataRepository.observeWorkout(workoutId).map { workout ->
        WorkoutSummaryUiState(
            isLoading = false,
            type = workout?.type,
            durationSeconds = workout?.durationSeconds ?: 0,
            estimatedCalories = workout?.estimatedCalories ?: 0,
            avgHeartRate = workout?.avgHeartRate,
            maxHeartRate = workout?.maxHeartRate,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutSummaryUiState())
}
