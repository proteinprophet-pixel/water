package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SeniorIvoryColorScheme =
  lightColorScheme(
    primary = MedDoneContainer,
    onPrimary = OnMedDone,
    primaryContainer = MedDoneBadgeBg,
    onPrimaryContainer = OnMedDone,
    secondary = WaterButtonPrimary,
    onSecondary = OnWaterPrimary,
    secondaryContainer = WaterCardContainer,
    onSecondaryContainer = CharcoalInk,
    tertiary = MedPendingAccent,
    onTertiary = OnMedDone,
    background = IvoryBackground,
    onBackground = CharcoalInk,
    surface = IvorySurface,
    onSurface = CharcoalInk,
    surfaceVariant = MedPendingContainer,
    onSurfaceVariant = WarmMutedInk,
    outline = MedPendingBorder,
  )

private val SeniorShapes =
  Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
  )

@Composable
fun SeniorHealthTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = SeniorIvoryColorScheme,
    typography = SeniorTypography,
    shapes = SeniorShapes,
    content = content,
  )
}
