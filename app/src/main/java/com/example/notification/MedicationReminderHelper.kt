package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object MedicationReminderHelper {
  const val CHANNEL_ID = "med_evening_warning_channel"
  const val WARNING_NOTIFICATION_ID = 2008
  private const val ALARM_REQUEST_CODE = 8000
  private const val WARNING_RED_COLOR = 0xFFC62828.toInt()

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = context.getString(R.string.notif_channel_name)
      val descriptionText = context.getString(R.string.notif_channel_desc)
      val channel =
        NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH).apply {
          description = descriptionText
          enableLights(true)
          lightColor = WARNING_RED_COLOR
          enableVibration(true)
        }
      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun scheduleDaily8PmReminder(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, MedicationReminderReceiver::class.java)
    val pendingIntent =
      PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val now = Calendar.getInstance()
    val next8Pm =
      (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 20)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (!after(now)) {
          add(Calendar.DAY_OF_YEAR, 1)
        }
      }

    alarmManager.setAndAllowWhileIdle(
      AlarmManager.RTC_WAKEUP,
      next8Pm.timeInMillis,
      pendingIntent,
    )
  }

  fun showEveningRedWarningNotification(context: Context, missedMedsText: String) {
    if (!hasNotificationPermission(context)) return
    createNotificationChannel(context)

    val launchIntent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
    val contentPendingIntent =
      PendingIntent.getActivity(
        context,
        0,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val title = context.getString(R.string.notif_warning_title)
    val body = context.getString(R.string.notif_warning_body, missedMedsText)

    val notification =
      NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setColor(WARNING_RED_COLOR)
        .setColorized(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setAutoCancel(true)
        .setContentIntent(contentPendingIntent)
        .build()

    try {
      NotificationManagerCompat.from(context).notify(WARNING_NOTIFICATION_ID, notification)
    } catch (_: SecurityException) {
      // Permission revoked between check and notify
    }
  }

  fun cancelWarningNotification(context: Context) {
    NotificationManagerCompat.from(context).cancel(WARNING_NOTIFICATION_ID)
  }
}
