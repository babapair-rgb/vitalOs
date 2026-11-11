package com.vitalos.app.data.ble

import kotlinx.coroutines.flow.StateFlow

interface BleRepository {
    val connectionState: StateFlow<BleConnectionState>
    val discoveredDevices: StateFlow<List<BleDeviceInfo>>
    val liveHeartRate: StateFlow<Int?>

    suspend fun startScan()
    suspend fun stopScan()
    suspend fun connect(deviceId: String)
    suspend fun disconnect()
    suspend fun sync(): SyncResult
}
