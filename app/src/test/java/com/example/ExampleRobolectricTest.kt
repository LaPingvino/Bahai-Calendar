package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.badi.BadiCalendarEngine
import com.example.badi.BadiDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bahá'í Calendar", appName)
    }

    @Test
    fun `verify badi calendar conversion`() {
        // Test Naw-Ruz 183 B.E. (March 21, 2026)
        val nawRuz = LocalDate.of(2026, 3, 21)
        val badiDate = BadiCalendarEngine.gregorianToBadi(nawRuz, LocalTime.of(12, 0))

        assertEquals(183, badiDate.year)
        assertEquals(1, badiDate.month) // Baha
        assertEquals(1, badiDate.day)
        assertEquals("Bahá", badiDate.monthInfo.transliteration)
        assertNotNull(badiDate.holyDay)
        assertEquals("Naw-Rúz (Bahá'í New Year)", badiDate.holyDay?.name)
    }

    @Test
    fun `verify ayyam-i-ha intercalary days calculation`() {
        val len183 = BadiCalendarEngine.getAyyamIHaLength(183)
        assertTrue(len183 == 4 || len183 == 5)
    }

    @Test
    fun `verify civil gregorian date does not advance at sunset while badi day advances`() {
        val testDate = LocalDate.of(2026, 8, 17)
        // Before sunset (12:00 PM)
        val beforeSunset = BadiCalendarEngine.gregorianToBadi(testDate, LocalTime.of(12, 0))
        assertEquals(testDate, beforeSunset.gregorianDate)
        assertEquals(false, beforeSunset.isAfterSunset)

        // After sunset (9:00 PM)
        val afterSunset = BadiCalendarEngine.gregorianToBadi(testDate, LocalTime.of(21, 0))
        // The civil Gregorian date MUST remain Aug 17 (changes only at midnight!)
        assertEquals(testDate, afterSunset.gregorianDate)
        assertEquals(true, afterSunset.isAfterSunset)
        // Badí' day has advanced to next day
        assertEquals(beforeSunset.day + 1, afterSunset.day)
    }

    @Test
    fun `verify post-sunset appointment calculation advances badi date and marks eve`() {
        val date = LocalDate.of(2026, 8, 17)
        val sunset = BadiCalendarEngine.calculateSunset(date)
        val postSunsetTime = sunset.plusMinutes(20)

        val appointmentBadiDate = BadiCalendarEngine.gregorianToBadi(date, postSunsetTime)
        assertTrue(appointmentBadiDate.isAfterSunset)
        val dayTimeBadiDate = BadiCalendarEngine.gregorianToBadi(date, LocalTime.of(10, 0))

        assertEquals(dayTimeBadiDate.day + 1, appointmentBadiDate.day)
        assertEquals(date, appointmentBadiDate.gregorianDate)
    }

    @Test
    fun `verify 19 months definitions`() {
        assertEquals(19, BadiDate.MONTHS.size)
        assertEquals("Bahá", BadiDate.MONTHS[0].transliteration)
        assertEquals("'Alá'", BadiDate.MONTHS[18].transliteration)
    }
}
