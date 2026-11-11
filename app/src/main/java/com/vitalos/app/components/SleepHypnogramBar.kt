package com.vitalos.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.designsystem.VitalOsTheme

private fun colorFor(stage: SleepStageType) = when (stage) {
    SleepStageType.AWAKE -> VitalOsTheme.colors.criticalRed
    SleepStageType.LIGHT -> VitalOsTheme.colors.recoveryMint.copy(alpha = 0.6f)
    SleepStageType.REM -> VitalOsTheme.colors.recoveryMint
    SleepStageType.DEEP -> VitalOsTheme.colors.diagnosticBlue
}

@Composable
fun SleepHypnogramBar(
    segments: List<SleepStageSegmentEntity>,
    modifier: Modifier = Modifier,
) {
    val totalMinutes = segments.sumOf { it.durationMinutes }.coerceAtLeast(1)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(VitalOsTheme.shapes.pill)
            .background(VitalOsTheme.colors.surfaceContainerHighest),
    ) {
        segments.forEach { segment ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .weight(segment.durationMinutes.toFloat() / totalMinutes)
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(colorFor(segment.stage)),
            )
        }
    }
}
