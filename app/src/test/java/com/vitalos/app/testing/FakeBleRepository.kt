package com.vitalos.app.testing

import com.vitalos.app.data.ble.BleConnectionState
import com.vitalos.app.data.ble.BleDeviceInfo
import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.ble.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeBleRepository : BleRepository {
    val connectionStateFlow = MutableStateFlow(BleConnectionState())
    val discoveredDevicesFlow = MutableStateFlow<List<BleDeviceInfo>>(emptyList())
    val liveHeartRateFlow = MutableStateFlow<Int?>(null)

    var syncResult: SyncResult = SyncResult(recordsMerged = 0, success = true)
    var scanCalled = false
    var connectCalledWith: String? = null
    var disconnectCalled = false
    var syncCalled = false

    override val connectionState: StateFlow<BleConnectionState> = connectionStateFlow
    override val discoveredDevices: StateFlow<List<BleDeviceInfo>> = discoveredDevicesFlow
    override val liveHeartRate: StateFlow<Int?> = liveHeartRateFlow

    override suspend fun startScan() {
        scanCalled = true
    }

    override suspend fun stopScan() {}

    override suspend fun connect(deviceId: String) {
        connectCalledWith = deviceId
    }

    override suspend fun disconnect() {
        disconnectCalled = true
    }

    override suspend fun sync(): SyncResult {
        syncCalled = true
        return syncResult
    }
}
