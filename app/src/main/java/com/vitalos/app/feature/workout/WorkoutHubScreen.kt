package com.vitalos.app.feature.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun WorkoutHubScreen(
    onStartYoga: (Long) -> Unit,
    viewModel: WorkoutHubViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            VitalCard(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.startYoga(onStartYoga) },
            ) {
                Text("Start Yoga Session", style = VitalOsTheme.typography.headlineSm, color = VitalOsTheme.colors.readinessLime)
                Text("Begin a guided, manually-tracked session", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.textSecondary)
            }
        }

        item {
            Text("WEEKLY CONSISTENCY", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
            Text("${state.weeklyYogaCount} yoga sessions", style = VitalOsTheme.typography.headlineMd, color = VitalOsTheme.colors.onSurface)
        }

        items(state.recentWorkouts) { workout ->
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(workout.type.name, style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
                    Text("${workout.durationSeconds / 60} min · ${workout.estimatedCalories} kcal", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.textSecondary)
                }
            }
        }

        item {
            Text(
                "Demonstration data",
                style = VitalOsTheme.typography.dataTabularSm,
                color = VitalOsTheme.colors.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(top = VitalOsTheme.spacing.sm),
            )
        }
    }
}
