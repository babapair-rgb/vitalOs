package com.vitalos.app.feature.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SleepViewModel @Inject constructor(
    healthDataRepository: HealthDataRepository,
) : ViewModel() {

    val uiState: StateFlow<SleepUiState> = combine(
        healthDataRepository.observeTodaySleepSession(),
        healthDataRepository.observeRecentSleepSessions(limit = 7),
    ) { today, recent ->
        val durationMinutes = today?.segments?.sumOf { it.durationMinutes } ?: 0
        SleepUiState(
            isLoading = false,
            lastNightDurationMinutes = durationMinutes,
            sleepScore = today?.session?.sleepScore ?: 0,
            efficiencyPercent = today?.session?.efficiencyPercent ?: 0.0,
            awakenings = today?.session?.awakenings ?: 0,
            segments = today?.segments ?: emptyList(),
            weeklyScores = recent.map { it.sleepScore },
            sleepDebtMinutes = (SLEEP_GOAL_MINUTES - durationMinutes).coerceAtLeast(0),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SleepUiState())
}
