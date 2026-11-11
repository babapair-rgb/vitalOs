package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val tierBadge: String,
    val dailyStepGoal: Int,
    val dailyHydrationGoalMl: Int,
    val dailyCalorieGoal: Int,
)
