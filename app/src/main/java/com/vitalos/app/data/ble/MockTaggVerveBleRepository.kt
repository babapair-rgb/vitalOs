package com.vitalos.app.data.ble

import com.vitalos.app.data.local.dao.DeviceSyncLogDao
import com.vitalos.app.data.local.dao.HeartRateDao
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockTaggVerveBleRepository @Inject constructor(
    private val heartRateDao: HeartRateDao,
    private val deviceSyncLogDao: DeviceSyncLogDao,
    private val externalScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
) : BleRepository {

    private val _connectionState = MutableStateFlow(BleConnectionState())
    override val connectionState: StateFlow<BleConnectionState> = _connectionState

    private val _discoveredDevices = MutableStateFlow<List<BleDeviceInfo>>(emptyList())
    override val discoveredDevices: StateFlow<List<BleDeviceInfo>> = _discoveredDevices

    private val _liveHeartRate = MutableStateFlow<Int?>(null)
    override val liveHeartRate: StateFlow<Int?> = _liveHeartRate

    private var liveHrJob: Job? = null
    private val random = Random(seed = 7)

    override suspend fun startScan() {
        _connectionState.value = _connectionState.value.copy(phase = BleConnectionPhase.SCANNING)
        delay(1_500)
        val suffix = (1000..9999).random(random)
        _discoveredDevices.value = listOf(BleDeviceInfo(id = "TAGG-$suffix", name = "TAGG Verve · $suffix", rssi = -45))
    }

    override suspend fun stopScan() {
        if (_connectionState.value.phase == BleConnectionPhase.SCANNING) {
            _connectionState.value = _connectionState.value.copy(phase = BleConnectionPhase.DISCONNECTED)
        }
    }

    override suspend fun connect(deviceId: String) {
        val device = _discoveredDevices.value.firstOrNull { it.id == deviceId } ?: return
        _connectionState.value = _connectionState.value.copy(phase = BleConnectionPhase.CONNECTING)
        delay(800)
        _connectionState.value = BleConnectionState(
            phase = BleConnectionPhase.CONNECTED,
            connectedDevice = device,
            batteryPercent = 60 + random.nextInt(0, 35),
            lastSyncEpochMillis = _connectionState.value.lastSyncEpochMillis,
        )
        startLiveHeartRate()
    }

    override suspend fun disconnect() {
        liveHrJob?.cancel()
        liveHrJob = null
        _liveHeartRate.value = null
        _connectionState.value = BleConnectionState(phase = BleConnectionPhase.DISCONNECTED)
    }

    override suspend fun sync(): SyncResult {
        val current = _connectionState.value
        if (current.phase != BleConnectionPhase.CONNECTED) {
            return SyncResult(recordsMerged = 0, success = false, errorMessage = "No connected device")
        }
        _connectionState.value = current.copy(phase = BleConnectionPhase.SYNCING)
        delay(400)

        val now = System.currentTimeMillis()
        val newSamples = (0 until 5).map { i ->
            HeartRateSampleEntity(
                timestampEpochMillis = now - (4 - i) * 60_000L,
                bpm = 58 + random.nextInt(0, 60),
                isResting = i == 0,
            )
        }
        heartRateDao.insertAll(newSamples)
        deviceSyncLogDao.insert(
            DeviceSyncLogEntity(timestampEpochMillis = now, recordsMerged = newSamples.size, success = true, errorMessage = null),
        )

        _connectionState.value = current.copy(phase = BleConnectionPhase.CONNECTED, lastSyncEpochMillis = now)
        return SyncResult(recordsMerged = newSamples.size, success = true)
    }

    private fun startLiveHeartRate() {
        liveHrJob?.cancel()
        liveHrJob = externalScope.launch(dispatcher) {
            _liveHeartRate.value = 64
            while (true) {
                delay(3_500)
                val delta = random.nextInt(-1, 2)
                _liveHeartRate.value = 64 + delta
            }
        }
    }
}
