package com.example.labs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreRecordDao {
    @Query("SELECT * FROM score_records ORDER BY points DESC, dateTimestamp DESC")
    fun getAllRecords(): Flow<List<ScoreRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ScoreRecordEntity)

    @Query("DELETE FROM score_records")
    suspend fun clearAllRecords()
}
