package com.example

import com.example.badi.BadiCalendarEngine
import com.example.location.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class BadiCalendarEngineTest {

    @Test
    fun fiftyYearDailyContinuityAndRoundTrip() {
        // Every single day from 172 B.E. (2015) through 221 B.E. (2065) must have valid day bounds,
        // exact 1-to-1 round-trip conversion, and never produce day 20 of 'Alá' or skip day 19.
        val startNawRuz = BadiCalendarEngine.getNawRuzDate(172)
        val endNawRuz = BadiCalendarEngine.getNawRuzDate(222)
        var current = startNawRuz
        while (current.isBefore(endNawRuz)) {
            val badi = BadiCalendarEngine.gregorianToBadi(current, time = null)
            val maxDay = if (badi.month == 0) {
                BadiCalendarEngine.getAyyamIHaLength(badi.year)
            } else {
                19
            }
            assertTrue(
                "Invalid day ${badi.day} for ${badi.year}/${badi.month} on $current",
                badi.day in 1..maxDay
            )
            assertTrue(
                "Invalid month ${badi.month} on $current",
                badi.month in 0..19
            )

            val roundTrip = BadiCalendarEngine.badiToGregorian(badi.year, badi.month, badi.day)
            assertEquals("Round-trip mismatch for $current -> $badi", current, roundTrip)
            current = current.plusDays(1)
        }
    }

    @Test
    fun ayyamIHaLengthMatchesExactYearLength() {
        for (badiYear in 172..221) {
            val yearLength = ChronoUnit.DAYS.between(
                BadiCalendarEngine.getNawRuzDate(badiYear),
                BadiCalendarEngine.getNawRuzDate(badiYear + 1)
            ).toInt()
            val ayyamLength = BadiCalendarEngine.getAyyamIHaLength(badiYear)
            assertEquals(
                "Ayyám-i-Há length mismatch for year $badiYear B.E.",
                yearLength - 361,
                ayyamLength
            )
            assertTrue("Ayyám-i-Há must be 4 or 5 days for $badiYear", ayyamLength in 4..5)

            val alaDays = BadiCalendarEngine.getDaysInMonth(badiYear, 19)
            assertEquals(19, alaDays.size)
            assertEquals(
                BadiCalendarEngine.getNawRuzDate(badiYear + 1).minusDays(1),
                alaDays.last().gregorianDate
            )
        }
    }

    @Test
    fun daytimeNeverPrematurelyFlipsAcrossTimeZones() {
        // Regression test for the month-start bug:
        // On Aug 19, 2026 (19 Kamál 183 B.E.), midday in any timezone must stay on 19 Kamál (month 8, day 19)
        // and only advance to 1 Asmá' (month 9, day 1) at/after local sunset.
        val testDate = LocalDate.of(2026, 8, 19)
        val timeZones = listOf(
            "UTC",
            "Etc/UTC",
            "Europe/Amsterdam",
            "Europe/London",
            "America/Los_Angeles",
            "America/New_York",
            "America/Denver",
            "America/Sao_Paulo",
            "Asia/Tokyo",
            "Asia/Jerusalem",
            "Australia/Sydney",
            "Pacific/Auckland",
            "America/Halifax" // Unmapped zone using offset-derived longitude fallback
        )

        for (tzId in timeZones) {
            val loc = LocationHelper.getDefaultLocationForTimeZone(tzId)
            val zoneId = ZoneId.of(tzId)
            val sunset = BadiCalendarEngine.calculateSunset(testDate, loc.latitude, loc.longitude, zoneId)
            val sunrise = BadiCalendarEngine.calculateSunrise(testDate, loc.latitude, loc.longitude, zoneId)

            assertTrue("Sunrise ($sunrise) should be before 10:00 in $tzId", sunrise.isBefore(LocalTime.of(10, 0)))
            assertTrue("Sunset ($sunset) should be after 15:00 in $tzId", sunset.isAfter(LocalTime.of(15, 0)))

            // Midday should still be 19 Kamál (not yet 1 Asmá')
            val middayBadi = BadiCalendarEngine.gregorianToBadi(
                date = testDate,
                time = LocalTime.of(12, 0),
                latitude = loc.latitude,
                longitude = loc.longitude,
                zoneId = zoneId
            )
            assertEquals("Midday month wrong in $tzId (loc=${loc.cityName})", 8, middayBadi.month)
            assertEquals("Midday day wrong in $tzId (loc=${loc.cityName})", 19, middayBadi.day)
            assertFalse("Midday should not be after sunset in $tzId", middayBadi.isAfterSunset)

            // 1 minute before sunset is still 19 Kamál
            val beforeSunsetBadi = BadiCalendarEngine.gregorianToBadi(
                date = testDate,
                time = sunset.minusMinutes(1),
                latitude = loc.latitude,
                longitude = loc.longitude,
                zoneId = zoneId
            )
            assertEquals("1m before sunset month wrong in $tzId", 8, beforeSunsetBadi.month)
            assertEquals("1m before sunset day wrong in $tzId", 19, beforeSunsetBadi.day)
            assertFalse("1m before sunset should not be after sunset in $tzId", beforeSunsetBadi.isAfterSunset)

            // Exact sunset transitions to 1 Asmá'
            val atSunsetBadi = BadiCalendarEngine.gregorianToBadi(
                date = testDate,
                time = sunset,
                latitude = loc.latitude,
                longitude = loc.longitude,
                zoneId = zoneId
            )
            assertEquals("At sunset month wrong in $tzId", 9, atSunsetBadi.month)
            assertEquals("At sunset day wrong in $tzId", 1, atSunsetBadi.day)
            assertTrue("At sunset should be after sunset in $tzId", atSunsetBadi.isAfterSunset)
            assertEquals(testDate.plusDays(1), atSunsetBadi.primaryBadiGregorianDate)
            assertEquals(testDate, atSunsetBadi.gregorianDate)
        }
    }

    @Test
    fun polarLatitudesUseFallbackWithoutCrashing() {
        val zone = ZoneId.of("Europe/Oslo")
        // Midnight sun in June at 75°N
        val summerSunrise = BadiCalendarEngine.calculateSunrise(LocalDate.of(2026, 6, 21), 75.0, 18.95, zone)
        val summerSunset = BadiCalendarEngine.calculateSunset(LocalDate.of(2026, 6, 21), 75.0, 18.95, zone)
        assertEquals(LocalTime.of(6, 0), summerSunrise)
        assertEquals(LocalTime.of(18, 0), summerSunset)

        // Polar night in December at 75°N
        val winterSunrise = BadiCalendarEngine.calculateSunrise(LocalDate.of(2026, 12, 21), 75.0, 18.95, zone)
        val winterSunset = BadiCalendarEngine.calculateSunset(LocalDate.of(2026, 12, 21), 75.0, 18.95, zone)
        assertEquals(LocalTime.of(6, 0), winterSunrise)
        assertEquals(LocalTime.of(18, 0), winterSunset)
    }

    @Test
    fun defaultZoneAlignedCoordinatesMatchBetweenEngineMethods() {
        val date = LocalDate.of(2026, 8, 19)
        val zone = ZoneId.of("America/Los_Angeles")
        val sunsetDefault = BadiCalendarEngine.calculateSunset(date, zoneId = zone)
        val middayBadi = BadiCalendarEngine.gregorianToBadi(date, LocalTime.of(14, 0), zoneId = zone)
        assertEquals(sunsetDefault, middayBadi.sunsetTime)
        assertFalse(middayBadi.isAfterSunset)
        assertEquals(8, middayBadi.month)
        assertEquals(19, middayBadi.day)
    }
}
