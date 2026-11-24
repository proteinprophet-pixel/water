package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CharcoalInk
import com.example.ui.theme.MedDoneBadgeBg
import com.example.ui.theme.MedDoneBorder
import com.example.ui.theme.MedDoneContainer
import com.example.ui.theme.MedPendingAccent
import com.example.ui.theme.MedPendingBadgeBg
import com.example.ui.theme.MedPendingBorder
import com.example.ui.theme.MedPendingContainer
import com.example.ui.theme.OnMedDone
import com.example.ui.theme.WarmMutedInk

@Composable
fun MedicationButtonCard(
  title: String,
  subtitle: String,
  pendingIcon: ImageVector,
  isTaken: Boolean,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current

  val containerColor by
    animateColorAsState(
      targetValue = if (isTaken) MedDoneContainer else MedPendingContainer,
      animationSpec = tween(durationMillis = 220),
      label = "med_container_color",
    )
  val borderColor by
    animateColorAsState(
      targetValue = if (isTaken) MedDoneBorder else MedPendingBorder,
      animationSpec = tween(durationMillis = 220),
      label = "med_border_color",
    )
  val titleColor by
    animateColorAsState(
      targetValue = if (isTaken) OnMedDone else CharcoalInk,
      animationSpec = tween(durationMillis = 220),
      label = "med_title_color",
    )
  val subtitleColor by
    animateColorAsState(
      targetValue = if (isTaken) OnMedDone.copy(alpha = 0.92f) else WarmMutedInk,
      animationSpec = tween(durationMillis = 220),
      label = "med_subtitle_color",
    )
  val badgeBackground by
    animateColorAsState(
      targetValue = if (isTaken) MedDoneBadgeBg else MedPendingBadgeBg,
      animationSpec = tween(durationMillis = 220),
      label = "med_badge_bg",
    )

  val statusDoneLabel = stringResource(R.string.med_status_done)
  val statusPendingLabel = stringResource(R.string.med_status_pending)
  val cdDone = stringResource(R.string.cd_med_done)
  val cdPending = stringResource(R.string.cd_med_pending)
  val statusText = if (isTaken) statusDoneLabel else subtitle

  Surface(
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      onClick()
    },
    shape = RoundedCornerShape(24.dp),
    color = containerColor,
    border = BorderStroke(2.dp, borderColor),
    shadowElevation = if (isTaken) 4.dp else 2.dp,
    modifier =
      modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 98.dp)
        .minimumInteractiveComponentSize()
        .testTag(testTag)
        .semantics {
          role = Role.Checkbox
          stateDescription = if (isTaken) statusDoneLabel else statusPendingLabel
          contentDescription = "$title, ${if (isTaken) statusDoneLabel else statusPendingLabel}"
        },
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        Box(
          modifier =
            Modifier.size(60.dp).clip(CircleShape).background(badgeBackground),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (isTaken) Icons.Rounded.CheckCircle else pendingIcon,
            contentDescription = if (isTaken) cdDone else cdPending,
            tint = if (isTaken) OnMedDone else MedPendingAccent,
            modifier = Modifier.size(36.dp),
          )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = titleColor,
          )
          Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTaken) FontWeight.Bold else FontWeight.Medium,
            color = subtitleColor,
          )
        }
      }

      if (isTaken) {
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = OnMedDone,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Check,
              contentDescription = null,
              tint = MedDoneContainer,
              modifier = Modifier.size(26.dp),
            )
            Text(
              text = statusDoneLabel,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MedDoneContainer,
            )
          }
        }
      }
    }
  }
}
