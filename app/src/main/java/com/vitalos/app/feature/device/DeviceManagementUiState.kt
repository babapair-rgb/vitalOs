package com.vitalos.app.feature.device

import com.vitalos.app.data.ble.BleConnectionPhase
import com.vitalos.app.data.ble.BleDeviceInfo
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity

data class DeviceManagementUiState(
    val isLoading: Boolean = true,
    val phase: BleConnectionPhase = BleConnectionPhase.DISCONNECTED,
    val connectedDeviceName: String? = null,
    val batteryPercent: Int? = null,
    val lastSyncEpochMillis: Long? = null,
    val discoveredDevices: List<BleDeviceInfo> = emptyList(),
    val recentSyncLogs: List<DeviceSyncLogEntity> = emptyList(),
)
