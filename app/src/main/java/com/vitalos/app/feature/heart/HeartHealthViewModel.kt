package com.vitalos.app.feature.heart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HeartHealthViewModel @Inject constructor(
    healthDataRepository: HealthDataRepository,
    bleRepository: BleRepository,
) : ViewModel() {

    val uiState: StateFlow<HeartHealthUiState> = combine(
        healthDataRepository.observeLatestHeartRate(),
        healthDataRepository.observeRecentHeartRate(windowMinutes = 1440),
        bleRepository.liveHeartRate,
    ) { latest, recent, liveHr ->
        val restingSamples = recent.filter { it.isResting }.map { it.bpm }
        HeartHealthUiState(
            isLoading = false,
            currentBpm = liveHr ?: latest?.bpm ?: 0,
            isLiveMeasured = liveHr != null,
            restingBpm = restingSamples.minOrNull() ?: latest?.bpm ?: 0,
            hrTrend = recent.map { it.bpm },
            zoneMinutes = recent.groupingBy { classifyZone(it.bpm) }.eachCount(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HeartHealthUiState())
}
