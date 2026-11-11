package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WorkoutType { YOGA, RUNNING, WALKING, STRENGTH, OTHER }
enum class WorkoutStatus { IN_PROGRESS, COMPLETED, DISCARDED }

@Entity(tableName = "workout_session")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: WorkoutType,
    val startEpochMillis: Long,
    val endEpochMillis: Long?,
    val durationSeconds: Int,
    val estimatedCalories: Int,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val status: WorkoutStatus,
)
