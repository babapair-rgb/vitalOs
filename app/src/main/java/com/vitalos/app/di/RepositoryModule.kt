package com.vitalos.app.di

import com.vitalos.app.data.repository.HealthDataRepository
import com.vitalos.app.data.repository.HealthDataRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindHealthDataRepository(impl: HealthDataRepositoryImpl): HealthDataRepository
}
