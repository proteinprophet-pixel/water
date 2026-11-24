package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MedicationSlot
import com.example.ui.components.MedicationButtonCard
import com.example.ui.components.WaterCupEightGrid
import com.example.ui.components.WaterGlassGraphic
import com.example.ui.theme.CelebrationBg
import com.example.ui.theme.CelebrationBorder
import com.example.ui.theme.CelebrationGold
import com.example.ui.theme.CelebrationInk
import com.example.ui.theme.CharcoalInk
import com.example.ui.theme.IvoryHeaderBorder
import com.example.ui.theme.IvoryHeaderCard
import com.example.ui.theme.OnWarningRed
import com.example.ui.theme.OnWaterPrimary
import com.example.ui.theme.SeniorHealthTheme
import com.example.ui.theme.WarmMutedInk
import com.example.ui.theme.WarningRedBadge
import com.example.ui.theme.WarningRedBorder
import com.example.ui.theme.WarningRedContainer
import com.example.ui.theme.WaterButtonPrimary
import com.example.ui.theme.WaterCardBorder
import com.example.ui.theme.WaterCardContainer
import com.example.ui.theme.WaterGoalSuccessText

@Composable
fun HealthCheckScreen(
  uiState: HealthCheckUiState,
  onToggleMedication: (MedicationSlot) -> Unit,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
  showNotificationPermissionPrompt: Boolean = false,
  onRequestNotificationPermission: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .widthIn(max = 620.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
      ) {
        // 1. Today's Date Header
        TodayDateHeader(
          displayDate = uiState.displayDate,
          dayOfWeek = uiState.dayOfWeek,
        )

        // Optional notification permission prompt for 8 PM alerts (Android 13+)
        AnimatedVisibility(
          visible = showNotificationPermissionPrompt,
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically(),
        ) {
          NotificationPermissionCard(onRequestPermission = onRequestNotificationPermission)
        }

        // Red warning banner if 8:00 PM or later and any medication is not taken
        AnimatedVisibility(
          visible = uiState.isEveningWarningActive,
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically(),
        ) {
          EveningRedWarningBanner(missedMedications = uiState.missedMedications)
        }

        // 2. Medication Section (Morning, Lunch, Dinner stacked vertically with wide spacing)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(
            text = stringResource(R.string.med_section_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CharcoalInk,
            modifier = Modifier.padding(horizontal = 4.dp),
          )

          MedicationButtonCard(
            title = stringResource(R.string.med_morning_title),
            subtitle = stringResource(R.string.med_morning_subtitle),
            pendingIcon = Icons.Rounded.WbSunny,
            isTaken = uiState.morningTaken,
            onClick = { onToggleMedication(MedicationSlot.MORNING) },
            testTag = "med_morning_button",
          )

          MedicationButtonCard(
            title = stringResource(R.string.med_lunch_title),
            subtitle = stringResource(R.string.med_lunch_subtitle),
            pendingIcon = Icons.Rounded.LightMode,
            isTaken = uiState.lunchTaken,
            onClick = { onToggleMedication(MedicationSlot.LUNCH) },
            testTag = "med_lunch_button",
          )

          MedicationButtonCard(
            title = stringResource(R.string.med_dinner_title),
            subtitle = stringResource(R.string.med_dinner_subtitle),
            pendingIcon = Icons.Rounded.NightsStay,
            isTaken = uiState.dinnerTaken,
            onClick = { onToggleMedication(MedicationSlot.DINNER) },
            testTag = "med_dinner_button",
          )

          // Celebration banner when all three daily medications are completed
          AnimatedVisibility(
            visible = uiState.allMedicationsTaken,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
          ) {
            MedicationCelebrationBanner()
          }
        }

        // 3. Water Intake Tracker Section with 8 fillable cup icons
        WaterTrackerCard(
          waterCups = uiState.waterCups,
          goalCups = uiState.waterGoalCups,
          onAddWaterCup = onAddWaterCup,
          onRemoveWaterCup = onRemoveWaterCup,
        )

        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

@Composable
private fun EveningRedWarningBanner(missedMedications: List<MedicationSlot>) {
  val morningLabel = stringResource(R.string.med_morning_title)
  val lunchLabel = stringResource(R.string.med_lunch_title)
  val dinnerLabel = stringResource(R.string.med_dinner_title)

  val missedText =
    missedMedications.joinToString(", ") { slot ->
      when (slot) {
        MedicationSlot.MORNING -> morningLabel
        MedicationSlot.LUNCH -> lunchLabel
        MedicationSlot.DINNER -> dinnerLabel
      }
    }

  Surface(
    shape = RoundedCornerShape(24.dp),
    color = WarningRedContainer,
    border = BorderStroke(3.dp, WarningRedBorder),
    shadowElevation = 6.dp,
    modifier = Modifier.fillMaxWidth().testTag("medication_evening_warning_banner"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier.size(60.dp).clip(CircleShape).background(WarningRedBadge),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.WarningAmber,
          contentDescription = null,
          tint = OnWarningRed,
          modifier = Modifier.size(38.dp),
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = stringResource(R.string.med_evening_warning_title),
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.ExtraBold,
          color = OnWarningRed,
          modifier = Modifier.testTag("medication_evening_warning_text"),
        )
        Text(
          text = stringResource(R.string.med_evening_warning_detail, missedText),
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold,
          color = OnWarningRed,
        )
      }
    }
  }
}

@Composable
private fun NotificationPermissionCard(onRequestPermission: () -> Unit) {
  Surface(
    shape = RoundedCornerShape(22.dp),
    color = IvoryHeaderCard,
    border = BorderStroke(2.dp, IvoryHeaderBorder),
    modifier = Modifier.fillMaxWidth().testTag("notification_permission_card"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Text(
        text = stringResource(R.string.notif_permission_prompt),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = CharcoalInk,
      )
      Button(
        onClick = onRequestPermission,
        shape = RoundedCornerShape(18.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = WarningRedContainer,
            contentColor = OnWarningRed,
          ),
        modifier =
          Modifier.fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .minimumInteractiveComponentSize()
            .testTag("notification_permission_button"),
      ) {
        Icon(
          imageVector = Icons.Rounded.NotificationsActive,
          contentDescription = null,
          modifier = Modifier.size(26.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = stringResource(R.string.notif_permission_button),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
private fun MedicationCelebrationBanner() {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = CelebrationBg,
    border = BorderStroke(2.5.dp, CelebrationBorder),
    shadowElevation = 4.dp,
    modifier = Modifier.fillMaxWidth().testTag("medication_celebration_banner"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier.size(60.dp).clip(CircleShape).background(CelebrationGold),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.EmojiEvents,
          contentDescription = null,
          tint = OnWaterPrimary,
          modifier = Modifier.size(36.dp),
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = stringResource(R.string.med_all_done_celebration),
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.ExtraBold,
          color = CelebrationInk,
          modifier = Modifier.testTag("medication_celebration_text"),
        )
        Text(
          text = stringResource(R.string.med_all_done_sub),
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = CelebrationBorder,
        )
      }
    }
  }
}

@Composable
private fun TodayDateHeader(
  displayDate: String,
  dayOfWeek: String,
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = IvoryHeaderCard,
    border = BorderStroke(2.dp, IvoryHeaderBorder),
    modifier = Modifier.fillMaxWidth().testTag("today_date_header"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Rounded.CalendarToday,
        contentDescription = stringResource(R.string.today_label),
        tint = CharcoalInk,
        modifier = Modifier.size(34.dp),
      )

      Spacer(modifier = Modifier.width(16.dp))

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = displayDate,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.ExtraBold,
          color = CharcoalInk,
          modifier = Modifier.testTag("today_date_text"),
        )
        Text(
          text = "${stringResource(R.string.today_label)} · $dayOfWeek",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = WarmMutedInk,
        )
      }
    }
  }
}

@Composable
private fun WaterTrackerCard(
  waterCups: Int,
  goalCups: Int,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val goalReached = waterCups >= goalCups

  Surface(
    shape = RoundedCornerShape(28.dp),
    color = WaterCardContainer,
    border = BorderStroke(2.dp, WaterCardBorder),
    modifier = Modifier.fillMaxWidth().testTag("water_section_card"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      Text(
        text = stringResource(R.string.water_section_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = CharcoalInk,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(22.dp),
      ) {
        WaterGlassGraphic(cups = waterCups, goalCups = goalCups)

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text =
              buildAnnotatedString {
                withStyle(
                  SpanStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WaterButtonPrimary,
                  )
                ) {
                  append(waterCups.toString())
                }
                append(" ")
                withStyle(
                  SpanStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalInk,
                  )
                ) {
                  append(stringResource(R.string.water_cup_unit))
                }
              },
            modifier = Modifier.testTag("water_count_text"),
          )

          Text(
            text =
              if (goalReached) {
                stringResource(R.string.water_goal_done)
              } else {
                stringResource(R.string.water_goal_hint)
              },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (goalReached) WaterGoalSuccessText else WarmMutedInk,
          )
        }
      }

      // 8 individual fillable water cup icons (4 x 2 grid)
      WaterCupEightGrid(cups = waterCups, goalCups = goalCups)

      Button(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onAddWaterCup()
        },
        shape = RoundedCornerShape(22.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = WaterButtonPrimary,
            contentColor = OnWaterPrimary,
          ),
        modifier =
          Modifier.fillMaxWidth()
            .defaultMinSize(minHeight = 78.dp)
            .minimumInteractiveComponentSize()
            .testTag("water_add_button"),
      ) {
        Icon(
          imageVector = Icons.Rounded.WaterDrop,
          contentDescription = null,
          modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = stringResource(R.string.water_add_button),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.ExtraBold,
        )
      }

      AnimatedVisibility(visible = waterCups > 0) {
        OutlinedButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onRemoveWaterCup()
          },
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.5.dp, WaterCardBorder),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalInk),
          modifier =
            Modifier.fillMaxWidth()
              .defaultMinSize(minHeight = 56.dp)
              .minimumInteractiveComponentSize()
              .testTag("water_undo_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.RemoveCircleOutline,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.water_undo_button),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun HealthCheckScreenEveningWarningPreview() {
  SeniorHealthTheme {
    HealthCheckScreen(
      uiState =
        HealthCheckUiState(
          dateKey = "2026-11-17",
          displayDate = "2026년 11월 17일",
          dayOfWeek = "화요일",
          morningTaken = true,
          lunchTaken = false,
          dinnerTaken = false,
          waterCups = 5,
          isPast8Pm = true,
        ),
      onToggleMedication = {},
      onAddWaterCup = {},
      onRemoveWaterCup = {},
    )
  }
}
