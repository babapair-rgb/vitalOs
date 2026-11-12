package com.vitalos.app.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.local.entity.WorkoutType
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutHubViewModel @Inject constructor(
    private val healthDataRepository: HealthDataRepository,
) : ViewModel() {

    val uiState: StateFlow<WorkoutHubUiState> = healthDataRepository.observeRecentWorkouts(limit = 10).map { workouts ->
        WorkoutHubUiState(
            isLoading = false,
            recentWorkouts = workouts,
            weeklyYogaCount = workouts.count { it.type == WorkoutType.YOGA },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutHubUiState())

    fun startYoga(onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val id = healthDataRepository.startWorkout(WorkoutType.YOGA)
            onStarted(id)
        }
    }
}
