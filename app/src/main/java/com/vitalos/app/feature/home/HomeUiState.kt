package com.vitalos.app.feature.home

import com.vitalos.app.data.local.entity.SleepStageSegmentEntity

data class HomeUiState(
    val isLoading: Boolean = true,
    val greetingName: String = "",
    val tierBadge: String = "",
    val readinessPercent: Int = 0,
    val healthScore: Int = 0,
    val strainTarget: Double = 0.0,
    val restingHr: Int = 0,
    val liveHr: Int? = null,
    val hrSparkline: List<Int> = emptyList(),
    val steps: Int = 0,
    val stepGoal: Int = 10000,
    val distanceMeters: Double = 0.0,
    val activeCalories: Int = 0,
    val sleepDurationMinutes: Int = 0,
    val sleepScore: Int = 0,
    val sleepSegments: List<SleepStageSegmentEntity> = emptyList(),
    val hydrationMl: Int = 0,
    val hydrationGoalMl: Int = 3000,
    val proteinGrams: Int = 0,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0,
    val calorieTarget: Int = 0,
    val caloriesConsumed: Int = 0,
    val bleConnected: Boolean = false,
    val bleStatusLabel: String = "DISCONNECTED",
)
