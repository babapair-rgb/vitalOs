package com.vitalos.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.ble.BleConnectionPhase
import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.repository.HealthDataRepository
import com.vitalos.app.data.repository.SleepSessionWithSegments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val healthDataRepository: HealthDataRepository,
    private val bleRepository: BleRepository,
) : ViewModel() {

    private data class BaseData(
        val profile: ProfileEntity?,
        val snapshot: DailyMetricsSnapshotEntity?,
        val sleep: SleepSessionWithSegments?,
        val heartRateSamples: List<HeartRateSampleEntity>,
    )

    private val baseData = combine(
        healthDataRepository.observeProfile(),
        healthDataRepository.observeTodaySnapshot(),
        healthDataRepository.observeTodaySleepSession(),
        healthDataRepository.observeRecentHeartRate(60),
    ) { profile, snapshot, sleep, hr -> BaseData(profile, snapshot, sleep, hr) }

    val uiState: StateFlow<HomeUiState> = combine(
        baseData,
        bleRepository.connectionState,
        bleRepository.liveHeartRate,
    ) { base, bleState, liveHr ->
        HomeUiState(
            isLoading = false,
            greetingName = base.profile?.name.orEmpty(),
            tierBadge = base.profile?.tierBadge.orEmpty(),
            readinessPercent = base.snapshot?.readinessPercent ?: 0,
            healthScore = base.snapshot?.healthScore ?: 0,
            strainTarget = base.snapshot?.strainTarget ?: 0.0,
            restingHr = base.heartRateSamples.lastOrNull { it.isResting }?.bpm
                ?: base.heartRateSamples.lastOrNull()?.bpm ?: 0,
            liveHr = liveHr,
            hrSparkline = base.heartRateSamples.map { it.bpm },
            steps = base.snapshot?.steps ?: 0,
            stepGoal = base.profile?.dailyStepGoal ?: 10000,
            distanceMeters = base.snapshot?.distanceMeters ?: 0.0,
            activeCalories = base.snapshot?.activeCalories ?: 0,
            sleepDurationMinutes = base.sleep?.segments?.sumOf { it.durationMinutes } ?: 0,
            sleepScore = base.sleep?.session?.sleepScore ?: 0,
            sleepSegments = base.sleep?.segments ?: emptyList(),
            hydrationMl = base.snapshot?.hydrationMl ?: 0,
            hydrationGoalMl = base.profile?.dailyHydrationGoalMl ?: 3000,
            proteinGrams = base.snapshot?.proteinGrams ?: 0,
            carbsGrams = base.snapshot?.carbsGrams ?: 0,
            fatGrams = base.snapshot?.fatGrams ?: 0,
            calorieTarget = base.snapshot?.calorieTarget ?: 0,
            caloriesConsumed = base.snapshot?.caloriesConsumed ?: 0,
            bleConnected = bleState.phase == BleConnectionPhase.CONNECTED,
            bleStatusLabel = bleState.phase.name,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun quickAddWater() {
        viewModelScope.launch { healthDataRepository.addWaterIntake(250) }
    }

    fun manualSync() {
        viewModelScope.launch { bleRepository.sync() }
    }
}
