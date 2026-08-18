package com.example.badi

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Core engine for Badí' (Bahá'í) calendar calculations.
 * Implements the Universal House of Justice tables (172 B.E. - 221 B.E.)
 * and astronomical formulas for date conversion, sunset calculations,
 * and Holy Day determinations.
 */
object BadiCalendarEngine {

    /**
     * Official Universal House of Justice table data for Badí' years 172 to 221 B.E.
     * Contains:
     * - nawRuzDay: March day (20 or 21)
     * - ayyamIHaLength: 4 or 5 days
     * - twinBirthdaysMonth: Gregorian month of Birth of the Báb (10 = Oct, 11 = Nov)
     * - twinBirthdaysDay: Gregorian day of Birth of the Báb (Birth of Bahá'u'lláh is the next day)
     */
    data class UhjYearData(
        val nawRuzDay: Int,
        val ayyamIHaLength: Int,
        val twinBirthdaysMonth: Int,
        val twinBirthdaysDay: Int
    )

    private val UHJ_TABLE: Map<Int, UhjYearData> = mapOf(
        172 to UhjYearData(21, 4, 10, 14), // 2015-2016
        173 to UhjYearData(20, 4, 11, 1),  // 2016-2017
        174 to UhjYearData(20, 4, 10, 21), // 2017-2018 (Bicentenary of Bahá'u'lláh)
        175 to UhjYearData(21, 4, 11, 9),  // 2018-2019
        176 to UhjYearData(21, 4, 10, 29), // 2019-2020 (Bicentenary of the Báb)
        177 to UhjYearData(20, 4, 10, 18), // 2020-2021
        178 to UhjYearData(20, 4, 11, 6),  // 2021-2022
        179 to UhjYearData(21, 4, 10, 26), // 2022-2023
        180 to UhjYearData(21, 5, 10, 16), // 2023-2024
        181 to UhjYearData(20, 4, 11, 2),  // 2024-2025
        182 to UhjYearData(20, 4, 10, 22), // 2025-2026
        183 to UhjYearData(21, 4, 11, 10), // 2026-2027
        184 to UhjYearData(21, 4, 10, 31), // 2027-2028
        185 to UhjYearData(20, 4, 10, 19), // 2028-2029
        186 to UhjYearData(20, 4, 11, 8),  // 2029-2030
        187 to UhjYearData(21, 4, 10, 28), // 2030-2031
        188 to UhjYearData(21, 5, 10, 18), // 2031-2032
        189 to UhjYearData(20, 4, 11, 5),  // 2032-2033
        190 to UhjYearData(20, 4, 10, 25), // 2033-2034
        191 to UhjYearData(21, 4, 11, 12), // 2034-2035
        192 to UhjYearData(21, 4, 11, 1),  // 2035-2036
        193 to UhjYearData(20, 4, 10, 20), // 2036-2037
        194 to UhjYearData(20, 4, 11, 9),  // 2037-2038
        195 to UhjYearData(21, 4, 10, 29), // 2038-2039
        196 to UhjYearData(21, 5, 10, 19), // 2039-2040
        197 to UhjYearData(20, 4, 11, 6),  // 2040-2041
        198 to UhjYearData(20, 4, 10, 26), // 2041-2042
        199 to UhjYearData(21, 4, 11, 15), // 2042-2043
        200 to UhjYearData(21, 4, 11, 3),  // 2043-2044
        201 to UhjYearData(20, 4, 10, 23), // 2044-2045
        202 to UhjYearData(20, 4, 11, 11), // 2045-2046
        203 to UhjYearData(21, 4, 10, 31), // 2046-2047
        204 to UhjYearData(21, 5, 10, 20), // 2047-2048
        205 to UhjYearData(20, 4, 11, 8),  // 2048-2049
        206 to UhjYearData(20, 4, 10, 28), // 2049-2050
        207 to UhjYearData(21, 4, 10, 17), // 2050-2051
        208 to UhjYearData(21, 4, 11, 5),  // 2051-2052
        209 to UhjYearData(20, 4, 10, 24), // 2052-2053
        210 to UhjYearData(20, 4, 11, 13), // 2053-2054
        211 to UhjYearData(21, 4, 11, 2),  // 2054-2055
        212 to UhjYearData(21, 5, 10, 22), // 2055-2056
        213 to UhjYearData(20, 4, 11, 10), // 2056-2057
        214 to UhjYearData(20, 4, 10, 30), // 2057-2058
        215 to UhjYearData(21, 4, 10, 19), // 2058-2059
        216 to UhjYearData(21, 4, 11, 7),  // 2059-2060
        217 to UhjYearData(20, 4, 10, 26), // 2060-2061
        218 to UhjYearData(20, 4, 11, 15), // 2061-2062
        219 to UhjYearData(21, 4, 11, 4),  // 2062-2063
        220 to UhjYearData(21, 5, 10, 24), // 2063-2064
        221 to UhjYearData(20, 4, 11, 12)  // 2064-2065
    )

    /**
     * Gets the Gregorian date of Naw-Rúz for a given Badí' year.
     */
    fun getNawRuzDate(badiYear: Int): LocalDate {
        val gregorianYear = badiYear + 1843
        val uhj = UHJ_TABLE[badiYear]
        val day = uhj?.nawRuzDay ?: if (LocalDate.of(gregorianYear, 1, 1).isLeapYear) 20 else 21
        return LocalDate.of(gregorianYear, 3, day)
    }

    /**
     * Gets the length of Ayyám-i-Há (4 or 5 days) for a given Badí' year.
     */
    fun getAyyamIHaLength(badiYear: Int): Int {
        val uhj = UHJ_TABLE[badiYear]
        if (uhj != null) return uhj.ayyamIHaLength
        // Fallback: Check if next Badí' year starts 366 days after current
        val startThis = getNawRuzDate(badiYear)
        val startNext = getNawRuzDate(badiYear + 1)
        val totalDays = ChronoUnit.DAYS.between(startThis, startNext)
        return (totalDays - (19 * 19)).toInt().coerceIn(4, 5)
    }

    /**
     * Converts a Gregorian [LocalDate] to its corresponding [BadiDate].
     */
    fun gregorianToBadi(
        date: LocalDate,
        time: LocalTime? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): BadiDate {
        // Calculate sunset for the day
        val sunset = calculateSunset(date, latitude ?: 35.6892, longitude ?: 51.3890, zoneId)
        val isAfterSunset = if (time != null) time.isAfter(sunset) else false

        // In Badí' calendar, sunset begins the NEXT Badí' day!
        val effectiveGregorianDate = if (isAfterSunset) date.plusDays(1) else date

        // Find which Badí' year this effective date falls into
        val approxBadiYear = effectiveGregorianDate.year - 1843
        val startCurrent = getNawRuzDate(approxBadiYear)

        val badiYear = if (effectiveGregorianDate.isBefore(startCurrent)) {
            approxBadiYear - 1
        } else {
            val startNext = getNawRuzDate(approxBadiYear + 1)
            if (!effectiveGregorianDate.isBefore(startNext)) approxBadiYear + 1 else approxBadiYear
        }

        val nawRuz = getNawRuzDate(badiYear)
        val daysSinceNawRuz = ChronoUnit.DAYS.between(nawRuz, effectiveGregorianDate).toInt() // 0-indexed

        val ayyamIHaLength = getAyyamIHaLength(badiYear)
        var month = 1
        var day = 1

        if (daysSinceNawRuz < 18 * 19) {
            // In months 1 to 18
            month = (daysSinceNawRuz / 19) + 1
            day = (daysSinceNawRuz % 19) + 1
        } else if (daysSinceNawRuz < (18 * 19) + ayyamIHaLength) {
            // In Ayyám-i-Há
            month = 0
            day = (daysSinceNawRuz - (18 * 19)) + 1
        } else {
            // In Month 19 ('Alá')
            month = 19
            day = (daysSinceNawRuz - ((18 * 19) + ayyamIHaLength)) + 1
        }

        val holyDay = getHolyDayForDate(badiYear, month, day, effectiveGregorianDate)

        return BadiDate(
            year = badiYear,
            month = month,
            day = day,
            gregorianDate = date,
            primaryBadiGregorianDate = effectiveGregorianDate,
            sunsetTime = sunset,
            isAfterSunset = isAfterSunset,
            holyDay = holyDay
        )
    }

    /**
     * Converts a Badí' date ([badiYear], [badiMonth], [badiDay]) to its Gregorian [LocalDate].
     */
    fun badiToGregorian(badiYear: Int, badiMonth: Int, badiDay: Int): LocalDate {
        val nawRuz = getNawRuzDate(badiYear)
        val ayyamIHaLength = getAyyamIHaLength(badiYear)

        val offsetDays: Long = when {
            badiMonth in 1..18 -> ((badiMonth - 1) * 19 + (badiDay - 1)).toLong()
            badiMonth == 0 -> ((18 * 19) + (badiDay - 1)).toLong()
            badiMonth == 19 -> ((18 * 19) + ayyamIHaLength + (badiDay - 1)).toLong()
            else -> 0L
        }

        return nawRuz.plusDays(offsetDays)
    }

    /**
     * Gets all [BadiDate]s for a specific Badí' month (1..19 or 0 for Ayyám-i-Há).
     */
    fun getDaysInMonth(badiYear: Int, badiMonth: Int): List<BadiDate> {
        val numDays = if (badiMonth == 0) getAyyamIHaLength(badiYear) else 19
        return (1..numDays).map { day ->
            val gregDate = badiToGregorian(badiYear, badiMonth, day)
            val holyDay = getHolyDayForDate(badiYear, badiMonth, day, gregDate)
            BadiDate(
                year = badiYear,
                month = badiMonth,
                day = day,
                gregorianDate = gregDate,
                holyDay = holyDay
            )
        }
    }

    /**
     * Checks if a given Badí' date corresponds to a Bahá'í Holy Day.
     */
    fun getHolyDayForDate(badiYear: Int, month: Int, day: Int, gregDate: LocalDate): BadiHolyDay? {
        // 1. Check fixed Holy Days
        for (fixed in BadiHolyDay.ALL_FIXED) {
            if (fixed.badiMonth == month && fixed.badiDay == day) {
                return fixed
            }
        }

        // 2. Check movable Twin Holy Birthdays
        val uhj = UHJ_TABLE[badiYear]
        if (uhj != null) {
            val birthOfBabDate = LocalDate.of(badiYear + 1843, uhj.twinBirthdaysMonth, uhj.twinBirthdaysDay)
            val birthOfBahaullahDate = birthOfBabDate.plusDays(1)

            if (gregDate == birthOfBabDate) return BadiHolyDay.BIRTH_OF_THE_BAB
            if (gregDate == birthOfBahaullahDate) return BadiHolyDay.BIRTH_OF_BAHAULLAH
        }

        return null
    }

    /**
     * Gets all 11 Holy Days for a given Badí' year with their Gregorian dates.
     */
    fun getAllHolyDaysForYear(badiYear: Int): List<Pair<BadiHolyDay, LocalDate>> {
        val list = mutableListOf<Pair<BadiHolyDay, LocalDate>>()

        // Fixed Holy Days
        for (fixed in BadiHolyDay.ALL_FIXED) {
            if (fixed.badiMonth != null && fixed.badiDay != null) {
                val gDate = badiToGregorian(badiYear, fixed.badiMonth, fixed.badiDay)
                list.add(fixed to gDate)
            }
        }

        // Movable Twin Holy Days
        val uhj = UHJ_TABLE[badiYear]
        if (uhj != null) {
            val birthBab = LocalDate.of(badiYear + 1843, uhj.twinBirthdaysMonth, uhj.twinBirthdaysDay)
            val birthBahaullah = birthBab.plusDays(1)
            list.add(BadiHolyDay.BIRTH_OF_THE_BAB to birthBab)
            list.add(BadiHolyDay.BIRTH_OF_BAHAULLAH to birthBahaullah)
        }

        return list.sortedBy { it.second }
    }

    /**
     * Gets upcoming 19-Day Feasts and Holy Days starting from [fromDate].
     */
    fun getUpcomingEvents(fromDate: LocalDate, count: Int = 10): List<BadiDate> {
        val currentBadi = gregorianToBadi(fromDate)
        val result = mutableListOf<BadiDate>()

        var checkDate = fromDate
        while (result.size < count && ChronoUnit.DAYS.between(fromDate, checkDate) < 400) {
            val badi = gregorianToBadi(checkDate)
            if (badi.isFeastDay || badi.holyDay != null || badi.isAyyamIHa) {
                result.add(badi)
            }
            checkDate = checkDate.plusDays(1)
        }

        return result
    }

    /**
     * Calculates astronomical sunrise time using the standard solar zenith algorithm.
     */
    fun calculateSunrise(
        date: LocalDate,
        latitude: Double = 32.8191,
        longitude: Double = 34.9983,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalTime {
        val dayOfYear = date.dayOfYear
        val zenith = 90.83333333333333 // Official sunrise zenith

        val lngHour = longitude / 15.0
        val t = dayOfYear + ((6.0 - lngHour) / 24.0)

        // Sun's mean anomaly
        val m = (0.9856 * t) - 3.289

        // Sun's true longitude
        var l = m + (1.916 * sin(Math.toRadians(m))) + (0.020 * sin(Math.toRadians(2 * m))) + 282.634
        if (l >= 360.0) l -= 360.0
        if (l < 0.0) l += 360.0

        // Right ascension
        var ra = Math.toDegrees(kotlin.math.atan(0.91764 * tan(Math.toRadians(l))))
        if (ra >= 360.0) ra -= 360.0
        if (ra < 0.0) ra += 360.0

        // Right ascension in same quadrant as L
        val lQuadrant = Math.floor(l / 90.0) * 90.0
        val raQuadrant = Math.floor(ra / 90.0) * 90.0
        ra += (lQuadrant - raQuadrant)
        ra /= 15.0

        // Sun's declination
        val sinDec = 0.39782 * sin(Math.toRadians(l))
        val cosDec = cos(asin(sinDec))

        // Sun's local hour angle for sunrise
        val cosH = (cos(Math.toRadians(zenith)) - (sinDec * sin(Math.toRadians(latitude)))) /
                (cosDec * cos(Math.toRadians(latitude)))

        if (cosH > 1.0) {
            return LocalTime.of(6, 0)
        }
        if (cosH < -1.0) {
            return LocalTime.of(7, 0)
        }

        val h = (360.0 - Math.toDegrees(acos(cosH))) / 15.0

        // Local mean time of sunrise
        val localMeanTime = h + ra - (0.06571 * t) - 6.622

        // Universal Time
        var ut = localMeanTime - lngHour
        if (ut >= 24.0) ut -= 24.0
        if (ut < 0.0) ut += 24.0

        val offsetHours = zoneId.rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0
        var localTime = ut + offsetHours
        if (localTime >= 24.0) localTime -= 24.0
        if (localTime < 0.0) localTime += 24.0

        val hour = localTime.toInt().coerceIn(0, 23)
        val minute = ((localTime - hour) * 60).toInt().coerceIn(0, 59)

        return LocalTime.of(hour, minute)
    }

    /**
     * Calculates astronomical sunset time using the standard solar zenith algorithm.
     */
    fun calculateSunset(
        date: LocalDate,
        latitude: Double = 32.8191,
        longitude: Double = 34.9983,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalTime {
        val dayOfYear = date.dayOfYear
        val zenith = 90.83333333333333 // Official sunset zenith (includes atmospheric refraction)

        val lngHour = longitude / 15.0
        val t = dayOfYear + ((18.0 - lngHour) / 24.0)

        // Sun's mean anomaly
        val m = (0.9856 * t) - 3.289

        // Sun's true longitude
        var l = m + (1.916 * sin(Math.toRadians(m))) + (0.020 * sin(Math.toRadians(2 * m))) + 282.634
        if (l >= 360.0) l -= 360.0
        if (l < 0.0) l += 360.0

        // Right ascension
        var ra = Math.toDegrees(kotlin.math.atan(0.91764 * tan(Math.toRadians(l))))
        if (ra >= 360.0) ra -= 360.0
        if (ra < 0.0) ra += 360.0

        // Right ascension in same quadrant as L
        val lQuadrant = Math.floor(l / 90.0) * 90.0
        val raQuadrant = Math.floor(ra / 90.0) * 90.0
        ra += (lQuadrant - raQuadrant)
        ra /= 15.0

        // Sun's declination
        val sinDec = 0.39782 * sin(Math.toRadians(l))
        val cosDec = cos(asin(sinDec))

        // Sun's local hour angle for sunset
        val cosH = (cos(Math.toRadians(zenith)) - (sinDec * sin(Math.toRadians(latitude)))) /
                (cosDec * cos(Math.toRadians(latitude)))

        if (cosH > 1.0) {
            // Sun never sets (polar day) - return standard default
            return LocalTime.of(19, 0)
        }
        if (cosH < -1.0) {
            // Sun never rises (polar night)
            return LocalTime.of(17, 0)
        }

        val h = Math.toDegrees(acos(cosH)) / 15.0

        // Local mean time of sunset
        val localMeanTime = h + ra - (0.06571 * t) - 6.622

        // Universal Time
        var ut = localMeanTime - lngHour
        if (ut >= 24.0) ut -= 24.0
        if (ut < 0.0) ut += 24.0

        val offsetHours = zoneId.rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0
        var localTime = ut + offsetHours
        if (localTime >= 24.0) localTime -= 24.0
        if (localTime < 0.0) localTime += 24.0

        val hour = localTime.toInt().coerceIn(0, 23)
        val minute = ((localTime - hour) * 60).toInt().coerceIn(0, 59)

        return LocalTime.of(hour, minute)
    }

    /**
     * Calculates the Great Circle bearing (azimuth in degrees, 0..360) towards the
     * Qiblih — the Shrine of Bahá'u'lláh at Bahjí (32.9433° N, 35.0922° E).
     */
    fun calculateQiblihBearing(userLatitude: Double, userLongitude: Double): Double {
        val latBahji = Math.toRadians(32.9433)
        val lngBahji = Math.toRadians(35.0922)
        val latUser = Math.toRadians(userLatitude)
        val lngUser = Math.toRadians(userLongitude)

        val dLng = lngBahji - lngUser
        val y = sin(dLng) * cos(latBahji)
        val x = cos(latUser) * sin(latBahji) - sin(latUser) * cos(latBahji) * cos(dLng)

        var bearing = Math.toDegrees(kotlin.math.atan2(y, x))
        if (bearing < 0) {
            bearing += 360.0
        }
        return (bearing % 360.0)
    }

    /**
     * Details about a specific Badí' Year according to both Short and Long systems.
     */
    data class BadiYearDetails(
        val year: Int,
        val shortString: String,
        val longString: String,
        val fullLongString: String,
        val vahidYearNumber: Int,
        val vahidYearInfo: VahidYearInfo,
        val vahidNumber: Int,
        val kullIShayNumber: Int,
        val gregorianYearSpan: String,
        val nawRuzDate: LocalDate,
        val ayyamIHaLength: Int
    )

    fun getYearDetails(badiYear: Int): BadiYearDetails {
        val safeYear = badiYear.coerceAtLeast(1)
        val kullIShay = ((safeYear - 1) / 361) + 1
        val yearInKullIShay = ((safeYear - 1) % 361) + 1
        val vahid = ((yearInKullIShay - 1) / 19) + 1
        val vahidYearNumber = ((safeYear - 1) % 19) + 1
        val vahidYearInfo = BadiDate.VAHID_YEARS[vahidYearNumber - 1]
        val gregStart = safeYear + 1843
        val gregEnd = gregStart + 1
        val nawRuz = getNawRuzDate(safeYear)
        val ayyamDays = getAyyamIHaLength(safeYear)

        return BadiYearDetails(
            year = safeYear,
            shortString = "$safeYear B.E.",
            longString = "Yr $vahidYearNumber (${vahidYearInfo.transliteration}) • V.$vahid • K.$kullIShay",
            fullLongString = "Year $vahidYearNumber (${vahidYearInfo.transliteration}) • Váḥid $vahid • Kull-i-Shay' $kullIShay",
            vahidYearNumber = vahidYearNumber,
            vahidYearInfo = vahidYearInfo,
            vahidNumber = vahid,
            kullIShayNumber = kullIShay,
            gregorianYearSpan = "$gregStart–$gregEnd A.D.",
            nawRuzDate = nawRuz,
            ayyamIHaLength = ayyamDays
        )
    }
}
