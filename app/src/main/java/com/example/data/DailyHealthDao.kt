package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHealthDao {
  @Query("SELECT * FROM daily_health WHERE dateKey = :dateKey LIMIT 1")
  fun observeDay(dateKey: String): Flow<DailyHealthEntity?>

  @Query("SELECT * FROM daily_health WHERE dateKey = :dateKey LIMIT 1")
  suspend fun getDayOnce(dateKey: String): DailyHealthEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertDay(entity: DailyHealthEntity)

  @Query("DELETE FROM daily_health WHERE dateKey != :todayKey")
  suspend fun deleteOtherDays(todayKey: String)
}
