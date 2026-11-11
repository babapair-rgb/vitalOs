package com.vitalos.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.ConcentricRingGauge
import com.vitalos.app.components.HrSparkline
import com.vitalos.app.components.RingSpec
import com.vitalos.app.components.SegmentedProgressBar
import com.vitalos.app.components.SleepHypnogramBar
import com.vitalos.app.components.StatusPill
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Good morning, ${state.greetingName}", style = VitalOsTheme.typography.headlineLgMobile, color = VitalOsTheme.colors.readinessLime)
                    Text(state.tierBadge, style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.recoveryMint)
                }
                StatusPill(
                    text = if (state.bleConnected) "TAGG Verve · Connected" else "TAGG Verve · ${state.bleStatusLabel}",
                    dotColor = if (state.bleConnected) VitalOsTheme.colors.readinessLime else VitalOsTheme.colors.textDisabled,
                    pulsing = state.bleConnected,
                )
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("COMPOSITE STATE", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                ConcentricRingGauge(
                    modifier = Modifier.padding(vertical = VitalOsTheme.spacing.md),
                    rings = listOf(
                        RingSpec(progress = state.readinessPercent / 100f, color = VitalOsTheme.colors.readinessLime, strokeWidth = 8.dp),
                        RingSpec(progress = state.healthScore / 100f, color = VitalOsTheme.colors.recoveryMint, strokeWidth = 8.dp),
                        RingSpec(progress = (state.strainTarget / 20.0).toFloat(), color = VitalOsTheme.colors.diagnosticBlue, strokeWidth = 6.dp),
                    ),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.readinessPercent}%", style = VitalOsTheme.typography.dataTabularLg, color = VitalOsTheme.colors.readinessLime)
                        Text("READINESS", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                    }
                }
                Text(
                    "Resting HR ${state.restingHr} bpm  ·  Health ${state.healthScore}  ·  Strain ${state.strainTarget}",
                    style = VitalOsTheme.typography.dataTabularSm,
                    color = VitalOsTheme.colors.onSurface,
                )
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("DAILY ACTIVITY", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.steps} / ${state.stepGoal} steps", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.readinessLime)
                }
                SegmentedProgressBar(
                    progress = state.steps.toFloat() / state.stepGoal.coerceAtLeast(1),
                    modifier = Modifier.padding(top = VitalOsTheme.spacing.sm),
                )
                Text(
                    "${"%.1f".format(state.distanceMeters / 1000.0)} km · ${state.activeCalories} kcal",
                    style = VitalOsTheme.typography.bodySm,
                    color = VitalOsTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = VitalOsTheme.spacing.xs),
                )
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("REST & RECOVERY", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.sleepDurationMinutes / 60}h ${state.sleepDurationMinutes % 60}m · ${state.sleepScore}", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.recoveryMint)
                }
                SleepHypnogramBar(segments = state.sleepSegments, modifier = Modifier.padding(top = VitalOsTheme.spacing.sm))
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CONTINUOUS HEART RATE", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.liveHr ?: state.restingHr} BPM", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.criticalRed)
                }
                HrSparkline(samples = state.hrSparkline, modifier = Modifier.padding(top = VitalOsTheme.spacing.sm))
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("INTAKE & FUEL", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.hydrationMl} / ${state.hydrationGoalMl} mL", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.diagnosticBlue)
                }
                SegmentedProgressBar(
                    progress = state.hydrationMl.toFloat() / state.hydrationGoalMl.coerceAtLeast(1),
                    fillColor = VitalOsTheme.colors.diagnosticBlue,
                    modifier = Modifier.padding(top = VitalOsTheme.spacing.sm),
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = VitalOsTheme.spacing.sm), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text("${state.proteinGrams}g protein", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.carbsGrams}g carbs", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.fatGrams}g fat", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.textSecondary)
                }
            }
        }

        item {
            Text(
                "Demonstration Biometric Stream",
                style = VitalOsTheme.typography.dataTabularSm,
                color = VitalOsTheme.colors.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(top = VitalOsTheme.spacing.sm),
            )
        }
    }
}
