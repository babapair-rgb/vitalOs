package com.vitalos.app.data.ble

import com.vitalos.app.data.local.dao.DeviceSyncLogDao
import com.vitalos.app.data.local.dao.HeartRateDao
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.di.ApplicationScope
import com.vitalos.app.di.DefaultDispatcher
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
    @ApplicationScope private val externalScope: CoroutineScope,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
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
        // Only settle back to idle if nothing else (e.g. a connect() race) already moved the
        // phase on - mirrors the same "don't stomp on a concurrent transition" guard used in sync().
        if (_connectionState.value.phase == BleConnectionPhase.SCANNING) {
            _connectionState.value = _connectionState.value.copy(phase = BleConnectionPhase.DISCONNECTED)
        }
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
            // Not a genuine sync attempt - there's nothing to do, so don't flood the sync-history
            // log with a "failure" row every time the periodic worker fires while unpaired.
            return SyncResult(recordsMerged = 0, success = false, errorMessage = "No connected device")
        }
        _connectionState.value = current.copy(phase = BleConnectionPhase.SYNCING)
        delay(400)

        // The user may have disconnected while this delay was in flight - don't resurrect a
        // CONNECTED state over a disconnect that happened meanwhile, and don't write any new
        // heart-rate rows for a sync that no longer has a device to have synced from.
        val afterDelay = _connectionState.value
        if (afterDelay.phase == BleConnectionPhase.DISCONNECTED) {
            return logAndReturnFailure(recordsMerged = 0, errorMessage = "Disconnected during sync")
        }

        var succeeded = false
        try {
            // Simulate an occasional transient failure (per the original plan's intent to exercise
            // the error state) - only while actually connected, never on scan/connect.
            if (random.nextInt(0, 8) == 0) {
                return logAndReturnFailure(recordsMerged = 0, errorMessage = "Connection interrupted during sync")
            }

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
            succeeded = true
            return SyncResult(recordsMerged = newSamples.size, success = true)
        } finally {
            // Whether we succeeded, hit the simulated failure, or a DAO call threw, make sure the
            // connection phase never gets stuck at SYNCING - unless a concurrent disconnect()
            // already moved it on, in which case leave that alone.
            val latest = _connectionState.value
            if (latest.phase == BleConnectionPhase.SYNCING) {
                _connectionState.value = if (succeeded) {
                    latest.copy(phase = BleConnectionPhase.CONNECTED, lastSyncEpochMillis = System.currentTimeMillis())
                } else {
                    latest.copy(phase = BleConnectionPhase.CONNECTED)
                }
            }
        }
    }

    private suspend fun logAndReturnFailure(recordsMerged: Int, errorMessage: String): SyncResult {
        deviceSyncLogDao.insert(
            DeviceSyncLogEntity(timestampEpochMillis = System.currentTimeMillis(), recordsMerged = recordsMerged, success = false, errorMessage = errorMessage),
        )
        return SyncResult(recordsMerged = recordsMerged, success = false, errorMessage = errorMessage)
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
