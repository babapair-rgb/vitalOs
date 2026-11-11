package com.vitalos.app.permissions

import android.Manifest
import android.os.Build
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class BlePermissionsTest {

    @Test
    fun required_onApi31OrAbove_returnsScanAndConnect() {
        val result = BlePermissions.required(sdkInt = Build.VERSION_CODES.S)
        assertArrayEquals(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), result)
    }

    @Test
    fun required_belowApi31_returnsLegacyPermissions() {
        val result = BlePermissions.required(sdkInt = Build.VERSION_CODES.R)
        assertArrayEquals(
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION),
            result,
        )
    }
}
