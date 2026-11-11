package com.vitalos.app.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.vitalos.app.data.ble.SyncResult
import com.vitalos.app.testing.FakeBleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BleSyncWorkerTest {

    @Test
    fun doWork_whenSyncSucceeds_returnsSuccess() = runTest {
        val fakeBle = FakeBleRepository().apply {
            syncResult = SyncResult(recordsMerged = 5, success = true)
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<BleSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: androidx.work.WorkerParameters) =
                    BleSyncWorker(appContext, workerParameters, fakeBle)
            })
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assert(fakeBle.syncCalled)
    }

    @Test
    fun doWork_whenSyncFails_returnsRetry() = runTest {
        val fakeBle = FakeBleRepository().apply {
            syncResult = SyncResult(recordsMerged = 0, success = false, errorMessage = "No connected device")
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<BleSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: androidx.work.WorkerParameters) =
                    BleSyncWorker(appContext, workerParameters, fakeBle)
            })
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
