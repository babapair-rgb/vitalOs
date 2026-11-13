package com.vitalos.app.feature.heart

import androidx.compose.foundation.clickable
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
import com.vitalos.app.components.HrSparkline
import com.vitalos.app.components.StatusPill
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun HeartHealthScreen(
    viewModel: HeartHealthViewModel = hiltViewModel(),
    onOpenSleep: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${state.currentBpm} BPM", style = VitalOsTheme.typography.headlineLg, color = VitalOsTheme.colors.criticalRed)
                    StatusPill(
                        text = if (state.isLiveMeasured) "LIVE · MEASURED" else "LAST RECORDED",
                        dotColor = if (state.isLiveMeasured) VitalOsTheme.colors.recoveryMint else VitalOsTheme.colors.textDisabled,
                        pulsing = state.isLiveMeasured,
                    )
                }
                Text("Resting HR: ${state.restingBpm} bpm", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.textSecondary)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("24H TREND", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                HrSparkline(samples = state.hrTrend)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("HEART RATE ZONES", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                listOf("Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5").forEach { zone ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(zone, style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
                        Text("${state.zoneMinutes[zone] ?: 0} min", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.textSecondary)
                    }
                }
            }
        }

        item {
            VitalCard(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenSleep),
            ) {
                Text("View Sleep Intelligence →", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.recoveryMint)
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
