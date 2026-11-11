package com.vitalos.app.feature.sleep

import com.vitalos.app.data.local.entity.SleepStageSegmentEntity

data class SleepUiState(
    val isLoading: Boolean = true,
    val lastNightDurationMinutes: Int = 0,
    val sleepScore: Int = 0,
    val efficiencyPercent: Double = 0.0,
    val awakenings: Int = 0,
    val segments: List<SleepStageSegmentEntity> = emptyList(),
    val weeklyScores: List<Int> = emptyList(),
    val sleepDebtMinutes: Int = 0,
)

internal const val SLEEP_GOAL_MINUTES = 480
