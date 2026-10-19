package com.example.labs.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score_records")
data class ScoreRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0,
    val userName: String,
    val points: Int,
    val difficulty: Int,
    val speed: Int,
    val roundDuration: Int,
    val dateTimestamp: Long = System.currentTimeMillis()
)
