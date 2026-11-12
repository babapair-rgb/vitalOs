package com.vitalos.app.feature.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceManagementViewModel @Inject constructor(
    private val bleRepository: BleRepository,
    healthDataRepository: HealthDataRepository,
) : ViewModel() {

    val uiState: StateFlow<DeviceManagementUiState> = combine(
        bleRepository.connectionState,
        bleRepository.discoveredDevices,
        healthDataRepository.observeRecentSyncLogs(limit = 10),
    ) { connection, discovered, logs ->
        DeviceManagementUiState(
            isLoading = false,
            phase = connection.phase,
            connectedDeviceName = connection.connectedDevice?.name,
            batteryPercent = connection.batteryPercent,
            lastSyncEpochMillis = connection.lastSyncEpochMillis,
            discoveredDevices = discovered,
            recentSyncLogs = logs,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DeviceManagementUiState())

    fun scan() {
        viewModelScope.launch { bleRepository.startScan() }
    }

    fun connect(deviceId: String) {
        viewModelScope.launch { bleRepository.connect(deviceId) }
    }

    fun disconnect() {
        viewModelScope.launch { bleRepository.disconnect() }
    }

    fun manualSync() {
        viewModelScope.launch { bleRepository.sync() }
    }
}
