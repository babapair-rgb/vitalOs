package com.vitalos.app.feature.workout

import com.vitalos.app.data.local.entity.WorkoutSessionEntity

data class WorkoutHubUiState(
    val isLoading: Boolean = true,
    val recentWorkouts: List<WorkoutSessionEntity> = emptyList(),
    val weeklyYogaCount: Int = 0,
)
