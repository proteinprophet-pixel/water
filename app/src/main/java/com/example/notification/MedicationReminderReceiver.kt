package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.R
import com.example.data.AppDatabase
import com.example.data.SystemDateProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MedicationReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val pendingResult = goAsync()
    val appContext = context.applicationContext

    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
      try {
        val dateProvider = SystemDateProvider()
        val todayKey = dateProvider.todayKey()
        val dao = AppDatabase.getInstance(appContext).dailyHealthDao()
        val todayRecord = dao.getDayOnce(todayKey)

        val morningTaken = todayRecord?.morningTaken == true
        val lunchTaken = todayRecord?.lunchTaken == true
        val dinnerTaken = todayRecord?.dinnerTaken == true

        val missedNames = buildList {
          if (!morningTaken) add(appContext.getString(R.string.med_morning_title))
          if (!lunchTaken) add(appContext.getString(R.string.med_lunch_title))
          if (!dinnerTaken) add(appContext.getString(R.string.med_dinner_title))
        }

        if (missedNames.isNotEmpty()) {
          MedicationReminderHelper.showEveningRedWarningNotification(
            appContext,
            missedNames.joinToString(", "),
          )
        }

        MedicationReminderHelper.scheduleDaily8PmReminder(appContext)
      } finally {
        pendingResult.finish()
      }
    }
  }
}
