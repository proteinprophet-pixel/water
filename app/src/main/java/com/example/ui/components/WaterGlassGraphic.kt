package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.WaterFillBottom
import com.example.ui.theme.WaterFillTop
import com.example.ui.theme.WaterGlassInterior
import com.example.ui.theme.WaterGlassOutline

@Composable
fun WaterGlassGraphic(
  cups: Int,
  goalCups: Int,
  modifier: Modifier = Modifier,
) {
  val targetRatio =
    when {
      cups <= 0 -> 0f
      else -> (cups.toFloat() / goalCups.toFloat()).coerceIn(0.14f, 1f)
    }

  val animatedFill by
    animateFloatAsState(
      targetValue = targetRatio,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessLow,
        ),
      label = "water_fill_level",
    )

  val cdWaterCup = stringResource(R.string.cd_water_cup)

  Canvas(
    modifier =
      modifier
        .size(width = 104.dp, height = 124.dp)
        .testTag("water_glass_graphic")
        .semantics { contentDescription = "$cdWaterCup: ${cups}잔" }
  ) {
    drawCupShape(
      fillFraction = animatedFill,
      strokeWidthPx = 4.dp.toPx(),
      showMeasurementTicks = true,
    )
  }
}

@Composable
fun WaterCupEightGrid(
  cups: Int,
  goalCups: Int = 8,
  modifier: Modifier = Modifier,
) {
  val gridDescription = stringResource(R.string.cd_water_cups_grid, cups)

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .testTag("water_eight_cups_grid")
        .semantics { contentDescription = gridDescription },
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    val rows = (0 until goalCups).chunked(4)
    rows.forEach { rowIndices ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        rowIndices.forEach { index ->
          MiniWaterCupIcon(
            isFilled = index < cups,
            cupNumber = index + 1,
          )
        }
      }
    }
  }
}

@Composable
private fun MiniWaterCupIcon(
  isFilled: Boolean,
  cupNumber: Int,
) {
  val animatedFill by
    animateFloatAsState(
      targetValue = if (isFilled) 0.84f else 0f,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMediumLow,
        ),
      label = "mini_cup_fill_$cupNumber",
    )

  Canvas(
    modifier =
      Modifier
        .size(width = 48.dp, height = 56.dp)
        .testTag("water_cup_icon_$cupNumber")
  ) {
    drawCupShape(
      fillFraction = animatedFill,
      strokeWidthPx = 3.dp.toPx(),
      showMeasurementTicks = false,
    )
  }
}

private fun DrawScope.drawCupShape(
  fillFraction: Float,
  strokeWidthPx: Float,
  showMeasurementTicks: Boolean,
) {
  val w = size.width
  val h = size.height

  val topLeftX = w * 0.14f
  val topRightX = w * 0.86f
  val bottomLeftX = w * 0.24f
  val bottomRightX = w * 0.76f
  val topY = h * 0.08f
  val bottomY = h * 0.92f
  val cornerCurve = h * 0.04f

  val glassPath =
    Path().apply {
      moveTo(topLeftX, topY)
      lineTo(topRightX, topY)
      lineTo(bottomRightX, bottomY - cornerCurve)
      quadraticTo(bottomRightX, bottomY, bottomRightX - cornerCurve, bottomY)
      lineTo(bottomLeftX + cornerCurve, bottomY)
      quadraticTo(bottomLeftX, bottomY, bottomLeftX, bottomY - cornerCurve)
      close()
    }

  drawPath(path = glassPath, color = WaterGlassInterior)

  if (fillFraction > 0.005f) {
    clipPath(glassPath) {
      val usableHeight = bottomY - topY - (h * 0.06f)
      val waterTopY = bottomY - (usableHeight * fillFraction)

      val waterPath =
        Path().apply {
          moveTo(0f, waterTopY)
          quadraticTo(w * 0.28f, waterTopY - (h * 0.03f), w * 0.52f, waterTopY)
          quadraticTo(w * 0.76f, waterTopY + (h * 0.03f), w, waterTopY)
          lineTo(w, h)
          lineTo(0f, h)
          close()
        }

      drawPath(
        path = waterPath,
        brush =
          Brush.verticalGradient(
            colors = listOf(WaterFillTop, WaterFillBottom),
            startY = waterTopY,
            endY = bottomY,
          ),
      )

      drawLine(
        color = WaterGlassInterior.copy(alpha = 0.55f),
        start = Offset(w * 0.22f, waterTopY + 2.dp.toPx()),
        end = Offset(w * 0.76f, waterTopY + 2.dp.toPx()),
        strokeWidth = (strokeWidthPx * 0.6f),
        cap = StrokeCap.Round,
      )
    }
  }

  if (showMeasurementTicks) {
    val tickStroke = 2.dp.toPx()
    listOf(0.32f, 0.52f, 0.72f).forEach { fraction ->
      val tickY = topY + (bottomY - topY) * fraction
      val tickStartX = topLeftX + (bottomLeftX - topLeftX) * fraction + 4.dp.toPx()
      drawLine(
        color = WaterGlassOutline.copy(alpha = 0.35f),
        start = Offset(tickStartX, tickY),
        end = Offset(tickStartX + 10.dp.toPx(), tickY),
        strokeWidth = tickStroke,
        cap = StrokeCap.Round,
      )
    }
  }

  drawPath(
    path = glassPath,
    color = WaterGlassOutline,
    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round),
  )
}
