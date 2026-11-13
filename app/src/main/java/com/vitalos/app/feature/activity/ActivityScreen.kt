package com.vitalos.app.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.HrSparkline
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(viewModel: ActivityViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.sm)) {
                ActivityTimeRange.entries.forEach { range ->
                    FilterChip(
                        selected = state.selectedRange == range,
                        onClick = { viewModel.selectRange(range) },
                        label = { Text(range.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VitalOsTheme.colors.elevatedSurface,
                            selectedLabelColor = VitalOsTheme.colors.onSurface,
                        ),
                    )
                }
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("TOTAL STEPS", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                Text("${state.totalSteps}", style = VitalOsTheme.typography.headlineLg, color = VitalOsTheme.colors.readinessLime)
                HrSparkline(samples = state.stepsTrend, lineColor = VitalOsTheme.colors.readinessLime)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("DISTANCE", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                Text("${"%.1f".format(state.totalDistanceMeters / 1000.0)} km", style = VitalOsTheme.typography.headlineMd, color = VitalOsTheme.colors.onSurface)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("ACTIVE CALORIES", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                Text("${state.totalActiveCalories} kcal", style = VitalOsTheme.typography.headlineMd, color = VitalOsTheme.colors.onSurface)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Active minutes: ${state.activeMinutes}", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
                    Text("Sedentary: ${state.sedentaryMinutes}m", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.textSecondary)
                }
                Text("Streak: ${state.streakDays} days ≥ 8,000 steps", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.recoveryMint)
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
