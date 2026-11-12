package com.vitalos.app.feature.workout

import com.vitalos.app.data.local.entity.WorkoutType

data class WorkoutSummaryUiState(
    val isLoading: Boolean = true,
    val type: WorkoutType? = null,
    val durationSeconds: Int = 0,
    val estimatedCalories: Int = 0,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
)
