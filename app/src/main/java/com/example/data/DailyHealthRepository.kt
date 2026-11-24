package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class MedicationSlot {
  MORNING,
  LUNCH,
  DINNER,
}

class DailyHealthRepository(private val dao: DailyHealthDao) {

  private val writeMutex = Mutex()

  fun observeDay(dateKey: String): Flow<DailyHealthEntity> {
    return dao.observeDay(dateKey).map { record -> record ?: DailyHealthEntity(dateKey = dateKey) }
  }

  suspend fun clearExpiredDays(todayKey: String) {
    writeMutex.withLock { dao.deleteOtherDays(todayKey) }
  }

  suspend fun toggleMedication(dateKey: String, slot: MedicationSlot) {
    writeMutex.withLock {
      val current = dao.getDayOnce(dateKey) ?: DailyHealthEntity(dateKey = dateKey)
      val updated =
        when (slot) {
          MedicationSlot.MORNING -> current.copy(morningTaken = !current.morningTaken)
          MedicationSlot.LUNCH -> current.copy(lunchTaken = !current.lunchTaken)
          MedicationSlot.DINNER -> current.copy(dinnerTaken = !current.dinnerTaken)
        }.copy(updatedAt = System.currentTimeMillis())
      dao.upsertDay(updated)
    }
  }

  suspend fun incrementWater(dateKey: String) {
    writeMutex.withLock {
      val current = dao.getDayOnce(dateKey) ?: DailyHealthEntity(dateKey = dateKey)
      val updated =
        current.copy(waterCups = current.waterCups + 1, updatedAt = System.currentTimeMillis())
      dao.upsertDay(updated)
    }
  }

  suspend fun decrementWater(dateKey: String) {
    writeMutex.withLock {
      val current = dao.getDayOnce(dateKey) ?: DailyHealthEntity(dateKey = dateKey)
      if (current.waterCups <= 0) return@withLock
      val updated =
        current.copy(waterCups = current.waterCups - 1, updatedAt = System.currentTimeMillis())
      dao.upsertDay(updated)
    }
  }
}
