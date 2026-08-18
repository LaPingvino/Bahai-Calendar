package com.example.badi

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MonthInfo(
    val number: Int,
    val transliteration: String,
    val arabic: String,
    val translation: String,
    val meaning: String
)

data class DayInfo(
    val number: Int,
    val transliteration: String,
    val arabic: String,
    val translation: String
)

data class WeekdayInfo(
    val badiNumber: Int,
    val transliteration: String,
    val arabic: String,
    val translation: String,
    val gregorianDayName: String
)

data class VahidYearInfo(
    val number: Int,
    val transliteration: String,
    val arabic: String,
    val translation: String
)

/**
 * Represents a specific date in the Badí' (Bahá'í) calendar.
 *
 * @property year Badí' Era year (e.g. 183 B.E.)
 * @property month Month index 1..19, or 0 for Ayyám-i-Há
 * @property day Day within the month (1..19, or 1..5 for Ayyám-i-Há)
 * @property gregorianDate The corresponding Gregorian calendar date
 * @property sunsetTime Calculated sunset time for this date and location
 * @property isAfterSunset Whether the current moment is after sunset (thus in the Bahá'í evening of this date)
 */
data class BadiDate(
    val year: Int,
    val month: Int, // 1..19, 0 = Ayyám-i-Há
    val day: Int,   // 1..19, 1..4 or 5 for Ayyám-i-Há
    val gregorianDate: LocalDate,
    val primaryBadiGregorianDate: LocalDate = gregorianDate,
    val sunsetTime: LocalTime? = null,
    val isAfterSunset: Boolean = false,
    val holyDay: BadiHolyDay? = null
) {
    val monthInfo: MonthInfo = when (month) {
        0 -> MonthInfo(
            number = 0,
            transliteration = "Ayyám-i-Há",
            arabic = "أيام الهاء",
            translation = "Intercalary Days",
            meaning = "Days of charity, hospitality, and gift-giving"
        )
        in 1..19 -> MONTHS[month - 1]
        else -> MONTHS[0]
    }

    val dayInfo: DayInfo = when {
        month == 0 -> DayInfo(
            number = day,
            transliteration = "Ayyám-i-Há $day",
            arabic = "يوم $day",
            translation = "Intercalary Day $day"
        )
        day in 1..19 -> DAYS[day - 1]
        else -> DAYS[0]
    }

    val badiWeekdayInfo: WeekdayInfo = getWeekdayInfo(primaryBadiGregorianDate.dayOfWeek.value)
    val civilWeekdayInfo: WeekdayInfo = getWeekdayInfo(gregorianDate.dayOfWeek.value)
    val weekdayInfo: WeekdayInfo = badiWeekdayInfo

    // Váḥid and Kull-i-Shay' calculations
    val kullIShay: Int = ((year - 1) / 361) + 1
    val yearInKullIShay: Int = ((year - 1) % 361) + 1
    val vahid: Int = ((yearInKullIShay - 1) / 19) + 1
    val vahidYearNumber: Int = ((year - 1) % 19) + 1
    val vahidYearInfo: VahidYearInfo = VAHID_YEARS[vahidYearNumber - 1]

    val isFeastDay: Boolean = (day == 1 && month in 1..19)
    val isFastPeriod: Boolean = (month == 19)
    val isAyyamIHa: Boolean = (month == 0)

    val formattedBadiDate: String
        get() = "$day ${monthInfo.transliteration} $year B.E."

    val shortYearString: String
        get() = "$year B.E."

    val longYearString: String
        get() = "Yr $vahidYearNumber (${vahidYearInfo.transliteration}) • V.$vahid • K.$kullIShay"

    val fullLongYearString: String
        get() = "Year $vahidYearNumber (${vahidYearInfo.transliteration}) • Váḥid $vahid • Kull-i-Shay' $kullIShay"

    val formattedBadiDateLong: String
        get() = "$day ${monthInfo.transliteration} • Yr $vahidYearNumber (${vahidYearInfo.transliteration}), V.$vahid, K.$kullIShay"

    val formattedGregorianDate: String
        get() = gregorianDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault()))

    companion object {
        val MONTHS = listOf(
            MonthInfo(1, "Bahá", "بهاء", "Splendour", "First month of the Badí' Year, beginning on Naw-Rúz"),
            MonthInfo(2, "Jalál", "جلال", "Glory", "Second month of the Badí' Year"),
            MonthInfo(3, "Jamál", "جمال", "Beauty", "Third month of the Badí' Year"),
            MonthInfo(4, "'Aẓamat", "عظمة", "Grandeur", "Fourth month, includes Declaration of the Báb and Ascension of Bahá'u'lláh"),
            MonthInfo(5, "Núr", "نور", "Light", "Fifth month of the Badí' Year"),
            MonthInfo(6, "Raḥmat", "رحمة", "Mercy", "Sixth month, includes the Martyrdom of the Báb"),
            MonthInfo(7, "Kalimát", "كلمات", "Words", "Seventh month of the Badí' Year"),
            MonthInfo(8, "Kamál", "كمال", "Perfection", "Eighth month of the Badí' Year"),
            MonthInfo(9, "Asmá'", "أسماء", "Names", "Ninth month of the Badí' Year"),
            MonthInfo(10, "'Izzat", "عزة", "Might", "Tenth month of the Badí' Year"),
            MonthInfo(11, "Mashíyyat", "مشية", "Will", "Eleventh month of the Badí' Year"),
            MonthInfo(12, "'Ilm", "علم", "Knowledge", "Twelfth month of the Badí' Year"),
            MonthInfo(13, "Qudrat", "قدرة", "Power", "Thirteenth month of the Badí' Year"),
            MonthInfo(14, "Qawl", "قول", "Speech", "Fourteenth month, includes Day of the Covenant and Ascension of 'Abdu'l-Bahá"),
            MonthInfo(15, "Masá'il", "مسائل", "Questions", "Fifteenth month of the Badí' Year"),
            MonthInfo(16, "Sharaf", "شرف", "Honour", "Sixteenth month of the Badí' Year"),
            MonthInfo(17, "Sulṭán", "سلطان", "Sovereignty", "Seventeenth month of the Badí' Year"),
            MonthInfo(18, "Mulk", "ملك", "Dominion", "Eighteenth month, followed by Ayyám-i-Há"),
            MonthInfo(19, "'Alá'", "علاء", "Loftiness", "Nineteenth month, the 19 days of Fasting from sunrise to sunset")
        )

        val DAYS = listOf(
            DayInfo(1, "Bahá", "بهاء", "Splendour"),
            DayInfo(2, "Jalál", "جلال", "Glory"),
            DayInfo(3, "Jamál", "جمال", "Beauty"),
            DayInfo(4, "'Aẓamat", "عظمة", "Grandeur"),
            DayInfo(5, "Núr", "نور", "Light"),
            DayInfo(6, "Raḥmat", "رحمة", "Mercy"),
            DayInfo(7, "Kalimát", "كلمات", "Words"),
            DayInfo(8, "Kamál", "كمال", "Perfection"),
            DayInfo(9, "Asmá'", "أسماء", "Names"),
            DayInfo(10, "'Izzat", "عزة", "Might"),
            DayInfo(11, "Mashíyyat", "مشية", "Will"),
            DayInfo(12, "'Ilm", "علم", "Knowledge"),
            DayInfo(13, "Qudrat", "قدرة", "Power"),
            DayInfo(14, "Qawl", "قول", "Speech"),
            DayInfo(15, "Masá'il", "مسائل", "Questions"),
            DayInfo(16, "Sharaf", "شرف", "Honour"),
            DayInfo(17, "Sulṭán", "سلطان", "Sovereignty"),
            DayInfo(18, "Mulk", "ملك", "Dominion"),
            DayInfo(19, "'Alá'", "علاء", "Loftiness")
        )

        val WEEKDAYS = listOf(
            WeekdayInfo(1, "Jalál", "جلال", "Glory", "Saturday"),
            WeekdayInfo(2, "Jamál", "جمال", "Beauty", "Sunday"),
            WeekdayInfo(3, "Kamál", "كمال", "Perfection", "Monday"),
            WeekdayInfo(4, "Fiḍál", "فضال", "Grace", "Tuesday"),
            WeekdayInfo(5, "'Idál", "عدال", "Justice", "Wednesday"),
            WeekdayInfo(6, "Istijbáb", "استجاب", "Prayer / Efficacy", "Thursday"),
            WeekdayInfo(7, "Istiqlál", "استقلال", "Independence", "Friday")
        )

        val VAHID_YEARS = listOf(
            VahidYearInfo(1, "Alif", "ألف", "A"),
            VahidYearInfo(2, "Bá'", "باء", "B"),
            VahidYearInfo(3, "Ab", "أب", "Father"),
            VahidYearInfo(4, "Dál", "دال", "D"),
            VahidYearInfo(5, "Báb", "باب", "Gate"),
            VahidYearInfo(6, "Váv", "واو", "V"),
            VahidYearInfo(7, "Abad", "أبد", "Eternity"),
            VahidYearInfo(8, "Jád", "جاد", "Generosity"),
            VahidYearInfo(9, "Bahá", "بهاء", "Splendour"),
            VahidYearInfo(10, "Ḥubb", "حب", "Love"),
            VahidYearInfo(11, "Bahháj", "بهاج", "Delightful"),
            VahidYearInfo(12, "Javáb", "جواب", "Answer"),
            VahidYearInfo(13, "Aḥad", "أحد", "Single"),
            VahidYearInfo(14, "Vahháb", "وهاب", "Bountiful"),
            VahidYearInfo(15, "Vidád", "وداد", "Affection"),
            VahidYearInfo(16, "Badí'", "بديع", "Unique / New"),
            VahidYearInfo(17, "Bahí", "بهي", "Luminous"),
            VahidYearInfo(18, "Abhá", "أبهى", "Most Luminous"),
            VahidYearInfo(19, "Váḥid", "واحد", "Unity")
        )

        fun getWeekdayInfo(javaDayOfWeek: Int): WeekdayInfo {
            // javaDayOfWeek: 1 = Monday, ..., 6 = Saturday, 7 = Sunday
            return when (javaDayOfWeek) {
                6 -> WEEKDAYS[0] // Saturday -> Jalál
                7 -> WEEKDAYS[1] // Sunday -> Jamál
                1 -> WEEKDAYS[2] // Monday -> Kamál
                2 -> WEEKDAYS[3] // Tuesday -> Fiḍál
                3 -> WEEKDAYS[4] // Wednesday -> 'Idál
                4 -> WEEKDAYS[5] // Thursday -> Istijbáb
                5 -> WEEKDAYS[6] // Friday -> Istiqlál
                else -> WEEKDAYS[0]
            }
        }
    }
}
