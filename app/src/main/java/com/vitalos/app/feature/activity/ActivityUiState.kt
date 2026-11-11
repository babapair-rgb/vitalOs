package com.vitalos.app.feature.activity

enum class ActivityTimeRange(val days: Int, val label: String) {
    DAILY(1, "Day"),
    WEEKLY(7, "Week"),
    MONTHLY(30, "Month"),
}

data class ActivityUiState(
    val isLoading: Boolean = true,
    val selectedRange: ActivityTimeRange = ActivityTimeRange.WEEKLY,
    val totalSteps: Int = 0,
    val totalDistanceMeters: Double = 0.0,
    val totalActiveCalories: Int = 0,
    val activeMinutes: Int = 0,
    val sedentaryMinutes: Int = 0,
    val streakDays: Int = 0,
    val stepsTrend: List<Int> = emptyList(),
)
