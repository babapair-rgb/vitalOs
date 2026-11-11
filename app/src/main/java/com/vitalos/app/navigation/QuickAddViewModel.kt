package com.vitalos.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.local.entity.WorkoutType
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val healthDataRepository: HealthDataRepository,
) : ViewModel() {

    fun logWater(onDone: () -> Unit) {
        viewModelScope.launch {
            healthDataRepository.addWaterIntake(250)
            onDone()
        }
    }

    fun startYogaSession(onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val id = healthDataRepository.startWorkout(WorkoutType.YOGA)
            onStarted(id)
        }
    }
}
