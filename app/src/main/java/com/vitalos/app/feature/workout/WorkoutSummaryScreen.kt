package com.vitalos.app.feature.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun WorkoutSummaryScreen(
    onDone: () -> Unit,
    viewModel: WorkoutSummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(VitalOsTheme.spacing.margin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("SESSION COMPLETE", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
        Text("${state.type?.name.orEmpty()}", style = VitalOsTheme.typography.headlineLg, color = VitalOsTheme.colors.readinessLime)

        VitalCard(modifier = Modifier.padding(top = VitalOsTheme.spacing.md)) {
            Text("Duration: ${state.durationSeconds / 60} min", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
            Text("Calories: ${state.estimatedCalories} kcal", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
            Text("Avg HR: ${state.avgHeartRate ?: "--"} bpm · Max HR: ${state.maxHeartRate ?: "--"} bpm", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
        }

        Button(onClick = onDone, modifier = Modifier.padding(top = VitalOsTheme.spacing.lg)) {
            Text("Done")
        }
    }
}
