package com.vitalos.app.feature.heart

data class HeartHealthUiState(
    val isLoading: Boolean = true,
    val currentBpm: Int = 0,
    val isLiveMeasured: Boolean = false,
    val restingBpm: Int = 0,
    val hrTrend: List<Int> = emptyList(),
    val zoneMinutes: Map<String, Int> = emptyMap(),
)

internal fun classifyZone(bpm: Int): String = when {
    bpm < 100 -> "Zone 1"
    bpm < 120 -> "Zone 2"
    bpm < 140 -> "Zone 3"
    bpm < 160 -> "Zone 4"
    else -> "Zone 5"
}
