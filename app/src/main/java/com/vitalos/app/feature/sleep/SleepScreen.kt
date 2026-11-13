package com.vitalos.app.feature.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.SleepHypnogramBar
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun SleepScreen(viewModel: SleepViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${state.lastNightDurationMinutes / 60}h ${state.lastNightDurationMinutes % 60}m",
                        style = VitalOsTheme.typography.headlineLg,
                        color = VitalOsTheme.colors.recoveryMint,
                    )
                    Text("${state.sleepScore} score", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.onSurface)
                }
                SleepHypnogramBar(segments = state.segments)
                Text(
                    "${state.efficiencyPercent}% efficiency · ${state.awakenings} awakenings",
                    style = VitalOsTheme.typography.bodySm,
                    color = VitalOsTheme.colors.textSecondary,
                )
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("SLEEP DEBT", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                Text("${state.sleepDebtMinutes} min behind goal", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.warningAmber)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("WEEKLY SCORES", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                Text(state.weeklyScores.joinToString(" · "), style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.onSurface)
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
