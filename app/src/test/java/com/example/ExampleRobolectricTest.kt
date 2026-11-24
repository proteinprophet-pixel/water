package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.DailyHealthRepository
import com.example.data.DateProvider
import com.example.data.MedicationSlot
import com.example.ui.HealthCheckScreen
import com.example.ui.HealthCheckUiState
import com.example.ui.HealthCheckViewModel
import com.example.ui.theme.SeniorHealthTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var database: AppDatabase
  private lateinit var repository: DailyHealthRepository
  private val testDispatcher = UnconfinedTestDispatcher()

  private var currentKey = "2026-11-17"
  private var currentDisplayDate = "2026년 11월 17일"
  private var currentDayOfWeek = "화요일"
  private var past8Pm = false

  private val fakeDateProvider =
    object : DateProvider {
      override fun todayKey(): String = currentKey

      override fun todayDisplayDate(): String = currentDisplayDate

      override fun todayDayOfWeek(): String = currentDayOfWeek

      override fun millisUntilNextMidnight(): Long = 60_000L

      override fun isAtOrAfter8Pm(): Boolean = past8Pm

      override fun millisUntilNext8Pm(): Long? = if (past8Pm) null else 60_000L
    }

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    currentKey = "2026-11-17"
    currentDisplayDate = "2026년 11월 17일"
    currentDayOfWeek = "화요일"
    past8Pm = false

    val context = ApplicationProvider.getApplicationContext<Context>()
    database =
      Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    repository = DailyHealthRepository(database.dailyHealthDao())
  }

  @After
  fun tearDown() {
    database.close()
    Dispatchers.resetMain()
  }

  @Test
  fun `app name matches Korean senior health check`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("건강 체크", appName)
  }

  @Test
  fun `medication and water states persist for today and auto reset after midnight`() = runTest {
    val viewModel =
      HealthCheckViewModel(
        repository = repository,
        dateProvider = fakeDateProvider,
        enableBackgroundSchedulers = false,
      )

    viewModel.onToggleMedication(MedicationSlot.MORNING)
    viewModel.onToggleMedication(MedicationSlot.LUNCH)
    viewModel.onToggleMedication(MedicationSlot.DINNER)
    viewModel.onAddWaterCup()
    viewModel.onAddWaterCup()

    val savedToday =
      repository.observeDay("2026-11-17").first {
        it.morningTaken && it.lunchTaken && it.dinnerTaken && it.waterCups == 2
      }
    assertTrue(savedToday.morningTaken)
    assertTrue(savedToday.lunchTaken)
    assertTrue(savedToday.dinnerTaken)
    assertEquals(2, savedToday.waterCups)

    // Simulate midnight passing to next day
    currentKey = "2026-11-18"
    currentDisplayDate = "2026년 11월 18일"
    currentDayOfWeek = "수요일"
    viewModel.refreshDateAndTimeIfNeeded()

    val nextDayState = viewModel.uiState.first { it.dateKey == "2026-11-18" }
    assertFalse(nextDayState.morningTaken)
    assertFalse(nextDayState.lunchTaken)
    assertFalse(nextDayState.dinnerTaken)
    assertFalse(nextDayState.allMedicationsTaken)
    assertEquals(0, nextDayState.waterCups)
    val clearedYesterday =
      repository.observeDay("2026-11-17").first { !it.morningTaken && it.waterCups == 0 }
    assertFalse(clearedYesterday.morningTaken)
    assertNull(database.dailyHealthDao().getDayOnce("2026-11-17"))
  }

  @Test
  fun `evening warning shows after 8 PM when meds missed and clears when all taken`() {
    composeTestRule.setContent {
      val stateHolder = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(
          HealthCheckUiState(
            dateKey = "2026-11-17",
            displayDate = "2026년 11월 17일",
            dayOfWeek = "화요일",
            isPast8Pm = true,
          )
        )
      }
      SeniorHealthTheme {
        HealthCheckScreen(
          uiState = stateHolder.value,
          onToggleMedication = { slot ->
            stateHolder.value =
              when (slot) {
                MedicationSlot.MORNING ->
                  stateHolder.value.copy(morningTaken = !stateHolder.value.morningTaken)
                MedicationSlot.LUNCH ->
                  stateHolder.value.copy(lunchTaken = !stateHolder.value.lunchTaken)
                MedicationSlot.DINNER ->
                  stateHolder.value.copy(dinnerTaken = !stateHolder.value.dinnerTaken)
              }
          },
          onAddWaterCup = {},
          onRemoveWaterCup = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("medication_evening_warning_banner").assertIsDisplayed()
    composeTestRule.onNodeWithText("저녁 8시 약 복용 경고!").assertIsDisplayed()

    composeTestRule.onNodeWithTag("med_morning_button").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("med_lunch_button").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("med_dinner_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("저녁 8시 약 복용 경고!").assertDoesNotExist()
    composeTestRule
      .onNodeWithTag("medication_celebration_text")
      .performScrollTo()
      .assertIsDisplayed()
      .assertTextContains("오늘도 건강 챙기기 성공!")
  }

  @Test
  fun `health check screen shows 8 cups and celebration message when all meds taken`() {
    composeTestRule.setContent {
      val stateHolder = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(
          HealthCheckUiState(
            dateKey = "2026-11-17",
            displayDate = "2026년 11월 17일",
            dayOfWeek = "화요일",
          )
        )
      }
      SeniorHealthTheme {
        HealthCheckScreen(
          uiState = stateHolder.value,
          onToggleMedication = { slot ->
            stateHolder.value =
              when (slot) {
                MedicationSlot.MORNING ->
                  stateHolder.value.copy(morningTaken = !stateHolder.value.morningTaken)
                MedicationSlot.LUNCH ->
                  stateHolder.value.copy(lunchTaken = !stateHolder.value.lunchTaken)
                MedicationSlot.DINNER ->
                  stateHolder.value.copy(dinnerTaken = !stateHolder.value.dinnerTaken)
              }
          },
          onAddWaterCup = {
            stateHolder.value =
              stateHolder.value.copy(waterCups = stateHolder.value.waterCups + 1)
          },
          onRemoveWaterCup = {
            stateHolder.value =
              stateHolder.value.copy(
                waterCups = (stateHolder.value.waterCups - 1).coerceAtLeast(0)
              )
          },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("today_date_text").assertTextContains("2026년 11월 17일")

    composeTestRule.onNodeWithText("오늘도 건강 챙기기 성공!").assertDoesNotExist()
    composeTestRule.onNodeWithText("저녁 8시 약 복용 경고!").assertDoesNotExist()

    composeTestRule.onNodeWithTag("med_morning_button").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("med_lunch_button").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("med_dinner_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule
      .onNodeWithTag("medication_celebration_text")
      .performScrollTo()
      .assertIsDisplayed()
      .assertTextContains("오늘도 건강 챙기기 성공!")

    for (cupNumber in 1..8) {
      composeTestRule.onNodeWithTag("water_cup_icon_$cupNumber").performScrollTo().assertIsDisplayed()
    }

    composeTestRule.onNodeWithTag("water_add_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("water_count_text").assertTextContains("1 잔")
  }
}
