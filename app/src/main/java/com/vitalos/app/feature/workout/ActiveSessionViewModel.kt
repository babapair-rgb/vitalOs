package com.vitalos.app.feature.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val healthDataRepository: HealthDataRepository,
    private val bleRepository: BleRepository,
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])
    private val elapsedSeconds = MutableStateFlow(0)
    private val isRunning = MutableStateFlow(true)

    val uiState: StateFlow<ActiveSessionUiState> = combine(
        elapsedSeconds,
        isRunning,
        bleRepository.liveHeartRate,
    ) { elapsed, running, liveHr ->
        ActiveSessionUiState(isLoading = false, elapsedSeconds = elapsed, liveHr = liveHr, isRunning = running)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveSessionUiState())

    init {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                if (isRunning.value) {
                    elapsedSeconds.value += 1
                }
            }
        }
    }

    fun togglePause() {
        isRunning.value = !isRunning.value
    }

    fun endSession(onEnded: (Long) -> Unit) {
        viewModelScope.launch {
            val hr = bleRepository.liveHeartRate.value
            healthDataRepository.finishWorkout(
                id = workoutId,
                durationSeconds = elapsedSeconds.value,
                estimatedCalories = (elapsedSeconds.value / 60) * 6,
                avgHeartRate = hr,
                maxHeartRate = hr,
                status = WorkoutStatus.COMPLETED,
            )
            onEnded(workoutId)
        }
    }
}
