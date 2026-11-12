package com.vitalos.app.feature.workout

data class ActiveSessionUiState(
    val isLoading: Boolean = true,
    val elapsedSeconds: Int = 0,
    val liveHr: Int? = null,
    val isRunning: Boolean = true,
)
