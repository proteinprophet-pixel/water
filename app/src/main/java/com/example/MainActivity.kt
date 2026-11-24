package com.example

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.DailyHealthRepository
import com.example.data.SystemDateProvider
import com.example.notification.MedicationReminderHelper
import com.example.ui.HealthCheckScreen
import com.example.ui.HealthCheckViewModel
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.SeniorHealthTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val ivoryBarColor = IvoryBackground.toArgb()
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(ivoryBarColor, ivoryBarColor),
      navigationBarStyle = SystemBarStyle.light(ivoryBarColor, ivoryBarColor),
    )

    MedicationReminderHelper.createNotificationChannel(applicationContext)
    MedicationReminderHelper.scheduleDaily8PmReminder(applicationContext)

    val database = AppDatabase.getInstance(applicationContext)
    val repository = DailyHealthRepository(database.dailyHealthDao())
    val dateProvider = SystemDateProvider()

    setContent {
      SeniorHealthTheme {
        val viewModel: HealthCheckViewModel =
          viewModel(factory = HealthCheckViewModel.Factory(repository, dateProvider))
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current

        var hasNotificationPermission by remember {
          mutableStateOf(MedicationReminderHelper.hasNotificationPermission(context))
        }

        val notificationPermissionLauncher =
          rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
          ) { granted ->
            hasNotificationPermission = granted
            if (granted) {
              MedicationReminderHelper.scheduleDaily8PmReminder(context)
            }
          }

        LaunchedEffect(uiState.allMedicationsTaken) {
          if (uiState.allMedicationsTaken) {
            MedicationReminderHelper.cancelWarningNotification(context)
          }
        }

        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
          viewModel.refreshDateAndTimeIfNeeded()
          hasNotificationPermission = MedicationReminderHelper.hasNotificationPermission(context)
        }

        DisposableEffect(context, viewModel) {
          val receiver =
            object : BroadcastReceiver() {
              override fun onReceive(ctx: Context?, intent: Intent?) {
                viewModel.refreshDateAndTimeIfNeeded()
                MedicationReminderHelper.scheduleDaily8PmReminder(context)
              }
            }
          val filter =
            IntentFilter().apply {
              addAction(Intent.ACTION_DATE_CHANGED)
              addAction(Intent.ACTION_TIME_CHANGED)
              addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
          ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
          )
          onDispose { context.unregisterReceiver(receiver) }
        }

        val showPermissionPrompt =
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission

        HealthCheckScreen(
          uiState = uiState,
          onToggleMedication = viewModel::onToggleMedication,
          onAddWaterCup = viewModel::onAddWaterCup,
          onRemoveWaterCup = viewModel::onRemoveWaterCup,
          showNotificationPermissionPrompt = showPermissionPrompt,
          onRequestNotificationPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
          },
        )
      }
    }
  }
}
