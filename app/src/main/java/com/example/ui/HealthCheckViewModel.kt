package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyHealthRepository
import com.example.data.DateProvider
import com.example.data.MedicationSlot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HealthCheckUiState(
  val dateKey: String = "",
  val displayDate: String = "",
  val dayOfWeek: String = "",
  val morningTaken: Boolean = false,
  val lunchTaken: Boolean = false,
  val dinnerTaken: Boolean = false,
  val waterCups: Int = 0,
  val waterGoalCups: Int = 8,
  val isPast8Pm: Boolean = false,
) {
  val allMedicationsTaken: Boolean
    get() = morningTaken && lunchTaken && dinnerTaken

  val missedMedications: List<MedicationSlot>
    get() = buildList {
      if (!morningTaken) add(MedicationSlot.MORNING)
      if (!lunchTaken) add(MedicationSlot.LUNCH)
      if (!dinnerTaken) add(MedicationSlot.DINNER)
    }

  val isEveningWarningActive: Boolean
    get() = isPast8Pm && !allMedicationsTaken
}

@OptIn(ExperimentalCoroutinesApi::class)
class HealthCheckViewModel(
  private val repository: DailyHealthRepository,
  private val dateProvider: DateProvider,
  private val enableBackgroundSchedulers: Boolean = true,
) : ViewModel() {

  private val activeDateKey = MutableStateFlow(dateProvider.todayKey())
  private val isPast8PmFlow = MutableStateFlow(dateProvider.isAtOrAfter8Pm())

  val uiState: StateFlow<HealthCheckUiState> =
    combine(
        activeDateKey.flatMapLatest { dateKey -> repository.observeDay(dateKey) },
        isPast8PmFlow,
      ) { record, isPast8Pm ->
        HealthCheckUiState(
          dateKey = record.dateKey,
          displayDate = dateProvider.todayDisplayDate(),
          dayOfWeek = dateProvider.todayDayOfWeek(),
          morningTaken = record.morningTaken,
          lunchTaken = record.lunchTaken,
          dinnerTaken = record.dinnerTaken,
          waterCups = record.waterCups,
          isPast8Pm = isPast8Pm,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue =
          HealthCheckUiState(
            dateKey = dateProvider.todayKey(),
            displayDate = dateProvider.todayDisplayDate(),
            dayOfWeek = dateProvider.todayDayOfWeek(),
            isPast8Pm = dateProvider.isAtOrAfter8Pm(),
          ),
      )

  init {
    viewModelScope.launch { repository.clearExpiredDays(activeDateKey.value) }
    if (enableBackgroundSchedulers) {
      startMidnightWatcher()
      start8PmWatcher()
    }
  }

  private fun startMidnightWatcher() {
    viewModelScope.launch {
      while (isActive) {
        val waitMillis = dateProvider.millisUntilNextMidnight()
        delay(waitMillis)
        refreshDateAndTimeIfNeeded()
      }
    }
  }

  private fun start8PmWatcher() {
    viewModelScope.launch {
      while (isActive) {
        val wait8Pm = dateProvider.millisUntilNext8Pm()
        if (wait8Pm != null) {
          delay(wait8Pm)
          refreshDateAndTimeIfNeeded()
        } else {
          // Already past 8 PM; wait until midnight when the day resets
          delay(dateProvider.millisUntilNextMidnight())
          refreshDateAndTimeIfNeeded()
        }
      }
    }
  }

  fun refreshDateAndTimeIfNeeded() {
    val latestKey = dateProvider.todayKey()
    if (activeDateKey.value != latestKey) {
      activeDateKey.value = latestKey
      viewModelScope.launch { repository.clearExpiredDays(latestKey) }
    }
    val past8Pm = dateProvider.isAtOrAfter8Pm()
    if (isPast8PmFlow.value != past8Pm) {
      isPast8PmFlow.value = past8Pm
    }
  }

  fun onToggleMedication(slot: MedicationSlot) {
    refreshDateAndTimeIfNeeded()
    val key = activeDateKey.value
    viewModelScope.launch { repository.toggleMedication(key, slot) }
  }

  fun onAddWaterCup() {
    refreshDateAndTimeIfNeeded()
    val key = activeDateKey.value
    viewModelScope.launch { repository.incrementWater(key) }
  }

  fun onRemoveWaterCup() {
    refreshDateAndTimeIfNeeded()
    val key = activeDateKey.value
    viewModelScope.launch { repository.decrementWater(key) }
  }

  class Factory(
    private val repository: DailyHealthRepository,
    private val dateProvider: DateProvider,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(HealthCheckViewModel::class.java)) {
        return HealthCheckViewModel(repository, dateProvider) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
