package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_metrics_snapshot")
data class DailyMetricsSnapshotEntity(
    @PrimaryKey val dateEpochDay: Long,
    val readinessPercent: Int,
    val healthScore: Int,
    val strainTarget: Double,
    val steps: Int,
    val distanceMeters: Double,
    val activeCalories: Int,
    val hydrationMl: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val calorieTarget: Int,
    val caloriesConsumed: Int,
)
