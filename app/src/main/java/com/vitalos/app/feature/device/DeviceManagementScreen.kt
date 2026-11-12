package com.vitalos.app.feature.device

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.StatusPill
import com.vitalos.app.components.VitalCard
import com.vitalos.app.data.ble.BleConnectionPhase
import com.vitalos.app.designsystem.VitalOsTheme
import com.vitalos.app.permissions.rememberBlePermissionState

@Composable
fun DeviceManagementScreen(viewModel: DeviceManagementViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val permissionState = rememberBlePermissionState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(state.connectedDeviceName ?: "No device connected", style = VitalOsTheme.typography.headlineSm, color = VitalOsTheme.colors.onSurface)
                    StatusPill(
                        text = state.phase.name,
                        dotColor = if (state.phase == BleConnectionPhase.CONNECTED) VitalOsTheme.colors.readinessLime else VitalOsTheme.colors.textDisabled,
                        pulsing = state.phase == BleConnectionPhase.CONNECTED || state.phase == BleConnectionPhase.SYNCING,
                    )
                }
                state.batteryPercent?.let {
                    Text("Battery: $it%", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.textSecondary)
                }
                state.lastSyncEpochMillis?.let {
                    Text("Last sync: ${it}ms epoch", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.textSecondary)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.sm)) {
                if (!permissionState.allGranted) {
                    Button(onClick = { permissionState.request() }) { Text("Grant Bluetooth permission") }
                } else {
                    Button(onClick = { viewModel.scan() }) { Text("Scan") }
                    if (state.phase == BleConnectionPhase.CONNECTED) {
                        OutlinedButton(onClick = { viewModel.disconnect() }) { Text("Disconnect") }
                        OutlinedButton(onClick = { viewModel.manualSync() }) { Text("Sync now") }
                    }
                }
            }
        }

        items(state.discoveredDevices) { device ->
            VitalCard(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.connect(device.id) },
            ) {
                Text(device.name, style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
                Text("RSSI ${device.rssi} dBm", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.textSecondary)
            }
        }

        item {
            Text("SYNC HISTORY", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
        }

        items(state.recentSyncLogs) { log ->
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (log.success) "${log.recordsMerged} records merged" else "Sync failed", style = VitalOsTheme.typography.bodyMd, color = if (log.success) VitalOsTheme.colors.onSurface else VitalOsTheme.colors.criticalRed)
                    Text("${log.timestampEpochMillis}", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.textSecondary)
                }
                log.errorMessage?.let {
                    Text(it, style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.criticalRed)
                }
            }
        }
    }
}
