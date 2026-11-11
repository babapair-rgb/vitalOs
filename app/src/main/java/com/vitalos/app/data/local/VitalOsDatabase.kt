package com.vitalos.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vitalos.app.data.local.dao.ActivityDao
import com.vitalos.app.data.local.dao.DailyMetricsDao
import com.vitalos.app.data.local.dao.DeviceSyncLogDao
import com.vitalos.app.data.local.dao.HeartRateDao
import com.vitalos.app.data.local.dao.ProfileDao
import com.vitalos.app.data.local.dao.SleepDao
import com.vitalos.app.data.local.dao.WorkoutDao
import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        ProfileEntity::class,
        DailyMetricsSnapshotEntity::class,
        HeartRateSampleEntity::class,
        SleepSessionEntity::class,
        SleepStageSegmentEntity::class,
        ActivitySampleEntity::class,
        WorkoutSessionEntity::class,
        DeviceSyncLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class VitalOsDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun dailyMetricsDao(): DailyMetricsDao
    abstract fun heartRateDao(): HeartRateDao
    abstract fun sleepDao(): SleepDao
    abstract fun activityDao(): ActivityDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun deviceSyncLogDao(): DeviceSyncLogDao

    companion object {
        const val DATABASE_NAME = "vitalos.db"
    }
}
