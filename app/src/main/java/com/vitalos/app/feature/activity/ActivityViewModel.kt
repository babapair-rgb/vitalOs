package com.vitalos.app.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val healthDataRepository: HealthDataRepository,
) : ViewModel() {

    private val selectedRange = MutableStateFlow(ActivityTimeRange.WEEKLY)

    val uiState: StateFlow<ActivityUiState> = selectedRange.flatMapLatest { range ->
        combine(
            healthDataRepository.observeSnapshotRange(range.days),
            healthDataRepository.observeActivityRange(range.days),
        ) { snapshots, hourly ->
            ActivityUiState(
                isLoading = false,
                selectedRange = range,
                totalSteps = snapshots.sumOf { it.steps },
                totalDistanceMeters = snapshots.sumOf { it.distanceMeters },
                totalActiveCalories = snapshots.sumOf { it.activeCalories },
                activeMinutes = hourly.count { it.intensity >= 2 } * 60,
                sedentaryMinutes = hourly.count { it.intensity == 0 } * 60,
                streakDays = snapshots.count { it.steps >= 8000 },
                stepsTrend = snapshots.map { it.steps },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActivityUiState())

    fun selectRange(range: ActivityTimeRange) {
        selectedRange.value = range
    }
}
