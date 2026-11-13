package com.vitalos.app.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vitalos.app.data.ble.BleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BleSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val bleRepository: BleRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        bleRepository.sync()
        return Result.success()
    }
}
