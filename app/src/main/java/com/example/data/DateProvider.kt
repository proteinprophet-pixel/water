package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

interface DateProvider {
  fun todayKey(): String

  fun todayDisplayDate(): String

  fun todayDayOfWeek(): String

  fun millisUntilNextMidnight(): Long

  fun isAtOrAfter8Pm(): Boolean

  fun millisUntilNext8Pm(): Long?
}

class SystemDateProvider : DateProvider {
  override fun todayKey(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(Date())
  }

  override fun todayDisplayDate(): String {
    val formatter = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN)
    return formatter.format(Date())
  }

  override fun todayDayOfWeek(): String {
    val formatter = SimpleDateFormat("EEEE", Locale.KOREAN)
    return formatter.format(Date())
  }

  override fun millisUntilNextMidnight(): Long {
    val now = Calendar.getInstance()
    val nextMidnight =
      (now.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 250)
      }
    return (nextMidnight.timeInMillis - now.timeInMillis).coerceAtLeast(1_000L)
  }

  override fun isAtOrAfter8Pm(): Boolean {
    return Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 20
  }

  override fun millisUntilNext8Pm(): Long? {
    val now = Calendar.getInstance()
    if (now.get(Calendar.HOUR_OF_DAY) >= 20) return null
    val today8Pm =
      (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 20)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 250)
      }
    return (today8Pm.timeInMillis - now.timeInMillis).coerceAtLeast(1_000L)
  }
}
