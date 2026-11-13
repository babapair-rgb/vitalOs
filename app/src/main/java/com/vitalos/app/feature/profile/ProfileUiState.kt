package com.vitalos.app.feature.profile

data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val tierBadge: String = "",
    val dailyStepGoal: String = "",
    val dailyHydrationGoalMl: String = "",
    val dailyCalorieGoal: String = "",
    val isSaving: Boolean = false,
    val saveSuccessEvent: Int = 0,
)
