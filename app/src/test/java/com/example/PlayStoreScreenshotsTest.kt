package com.example

import android.Manifest
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ApplicationProvider
import com.example.devotional.DevotionalRepository
import com.example.ui.BadiCalendarScreen
import com.example.ui.BadiViewModel
import com.example.ui.CalendarTab
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class PlayStoreScreenshotsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        Shadows.shadowOf(application).grantPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )
    }

    private fun captureScreen(
        fileName: String,
        theme: AppThemeMode,
        tab: CalendarTab,
        badiMonth: Int? = null
    ) {
        val viewModel = BadiViewModel(application)
        viewModel.setLocation(DevotionalRepository.PRESET_LOCATIONS[0]) // Haifa / Acre (Bahjí)
        viewModel.setThemeMode(theme)
        viewModel.selectTab(tab)
        if (badiMonth != null) {
            viewModel.setMonth(badiMonth)
        }
        viewModel.dismissSnackbar()

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(themeMode = uiState.currentTheme) {
                BadiCalendarScreen(viewModel = viewModel)
            }
        }
        composeTestRule.mainClock.advanceTimeBy(300L)

        composeTestRule.onRoot().captureRoboImage(
            filePath = "build/outputs/play-store-screenshots/$fileName"
        )
    }

    @Test
    fun screenshot_01_calendar_dark() {
        captureScreen(
            fileName = "01-calendar-elegant-dark.png",
            theme = AppThemeMode.ELEGANT_DARK,
            tab = CalendarTab.MONTH_VIEW
        )
    }

    @Test
    fun screenshot_02_calendar_lapis_gold() {
        captureScreen(
            fileName = "02-calendar-lapis-gold.png",
            theme = AppThemeMode.LAPIS_GOLD,
            tab = CalendarTab.MONTH_VIEW
        )
    }

    @Test
    fun screenshot_03_holy_days() {
        captureScreen(
            fileName = "03-holy-days.png",
            theme = AppThemeMode.ELEGANT_DARK,
            tab = CalendarTab.HOLY_DAYS
        )
    }

    @Test
    fun screenshot_04_devotionals() {
        captureScreen(
            fileName = "04-devotionals.png",
            theme = AppThemeMode.LAPIS_GOLD,
            tab = CalendarTab.DEVOTIONALS
        )
    }

    @Test
    fun screenshot_05_calendar_ivory_light() {
        captureScreen(
            fileName = "05-calendar-ivory-gold.png",
            theme = AppThemeMode.PURE_IVORY_GOLD,
            tab = CalendarTab.MONTH_VIEW
        )
    }
}
