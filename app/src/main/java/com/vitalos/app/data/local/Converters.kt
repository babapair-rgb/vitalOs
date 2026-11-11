package com.vitalos.app.data.local

import androidx.room.TypeConverter
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType

class Converters {
    @TypeConverter
    fun fromSleepStageType(value: SleepStageType): String = value.name

    @TypeConverter
    fun toSleepStageType(value: String): SleepStageType = SleepStageType.valueOf(value)

    @TypeConverter
    fun fromWorkoutType(value: WorkoutType): String = value.name

    @TypeConverter
    fun toWorkoutType(value: String): WorkoutType = WorkoutType.valueOf(value)

    @TypeConverter
    fun fromWorkoutStatus(value: WorkoutStatus): String = value.name

    @TypeConverter
    fun toWorkoutStatus(value: String): WorkoutStatus = WorkoutStatus.valueOf(value)
}
