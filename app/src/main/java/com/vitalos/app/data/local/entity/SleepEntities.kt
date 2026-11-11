package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SleepStageType { AWAKE, LIGHT, REM, DEEP }

@Entity(tableName = "sleep_session")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val efficiencyPercent: Double,
    val awakenings: Int,
    val sleepScore: Int,
)

@Entity(
    tableName = "sleep_stage_segment",
    foreignKeys = [
        ForeignKey(
            entity = SleepSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class SleepStageSegmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val stage: SleepStageType,
    val durationMinutes: Int,
    val orderIndex: Int,
)
