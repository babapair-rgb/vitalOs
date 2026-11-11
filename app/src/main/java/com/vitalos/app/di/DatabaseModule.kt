package com.vitalos.app.di

import android.content.Context
import androidx.room.Room
import com.vitalos.app.data.local.VitalOsDatabase
import com.vitalos.app.data.local.dao.ActivityDao
import com.vitalos.app.data.local.dao.DailyMetricsDao
import com.vitalos.app.data.local.dao.DeviceSyncLogDao
import com.vitalos.app.data.local.dao.HeartRateDao
import com.vitalos.app.data.local.dao.ProfileDao
import com.vitalos.app.data.local.dao.SleepDao
import com.vitalos.app.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VitalOsDatabase =
        Room.databaseBuilder(context, VitalOsDatabase::class.java, VitalOsDatabase.DATABASE_NAME).build()

    @Provides
    fun provideProfileDao(db: VitalOsDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideDailyMetricsDao(db: VitalOsDatabase): DailyMetricsDao = db.dailyMetricsDao()

    @Provides
    fun provideHeartRateDao(db: VitalOsDatabase): HeartRateDao = db.heartRateDao()

    @Provides
    fun provideSleepDao(db: VitalOsDatabase): SleepDao = db.sleepDao()

    @Provides
    fun provideActivityDao(db: VitalOsDatabase): ActivityDao = db.activityDao()

    @Provides
    fun provideWorkoutDao(db: VitalOsDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideDeviceSyncLogDao(db: VitalOsDatabase): DeviceSyncLogDao = db.deviceSyncLogDao()
}
