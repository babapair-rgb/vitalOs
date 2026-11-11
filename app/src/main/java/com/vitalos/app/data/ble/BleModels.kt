package com.vitalos.app.data.ble

enum class BleConnectionPhase { DISCONNECTED, SCANNING, CONNECTING, CONNECTED, SYNCING }

data class BleDeviceInfo(
    val id: String,
    val name: String,
    val rssi: Int,
)

data class BleConnectionState(
    val phase: BleConnectionPhase = BleConnectionPhase.DISCONNECTED,
    val connectedDevice: BleDeviceInfo? = null,
    val batteryPercent: Int? = null,
    val lastSyncEpochMillis: Long? = null,
)

data class SyncResult(
    val recordsMerged: Int,
    val success: Boolean,
    val errorMessage: String? = null,
)
