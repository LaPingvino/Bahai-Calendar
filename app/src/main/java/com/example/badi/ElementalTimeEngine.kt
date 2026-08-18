package com.example.badi

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

/**
 * Elemental Time System (etime / hora.net.br style time)
 *
 * Divides the 24-hour UTC day into 4 universal blocks of 6 hours each,
 * universally identical everywhere in the world:
 *
 * 1. Fire (00:00 - 05:59 UTC)  • Arabic: Nár
 * 2. Air  (06:00 - 11:59 UTC)  • Arabic: Hawá'
 * 3. Water (12:00 - 17:59 UTC) • Arabic: Má'
 * 4. Earth (18:00 - 23:59 UTC) • Arabic: Turáb
 *
 * Directly corresponds to the Badí' calendar's 4 elemental season divisions!
 */
enum class ElementalTimeSeason(
    val englishName: String,
    val arabicName: String,
    val emoji: String,
    val startUtcHour: Int,
    val endUtcHour: Int,
    val accentColorHex: Long,
    val description: String,
    val badiMonths: String
) {
    FIRE(
        englishName = "Fire",
        arabicName = "Nár",
        emoji = "🔥",
        startUtcHour = 0,
        endUtcHour = 5,
        accentColorHex = 0xFFFF7043, // Vibrant Warm Fire
        description = "Block 1 (00:00 – 06:00)",
        badiMonths = "Bahá, Jalál, Jamál, 'Aẓamat"
    ),
    AIR(
        englishName = "Air",
        arabicName = "Hawá'",
        emoji = "💨",
        startUtcHour = 6,
        endUtcHour = 11,
        accentColorHex = 0xFF4FC3F7, // Sky Breeze Blue
        description = "Block 2 (06:00 – 12:00)",
        badiMonths = "Núr, Raḥmat, Kalimát, Kamál, Asmá'"
    ),
    WATER(
        englishName = "Water",
        arabicName = "Má'",
        emoji = "💧",
        startUtcHour = 12,
        endUtcHour = 17,
        accentColorHex = 0xFF26C6DA, // Deep Ocean Teal
        description = "Block 3 (12:00 – 18:00)",
        badiMonths = "'Izzat, Mas͟híyyat, 'Ilm, Qudrat, Qawl, Masá'il"
    ),
    EARTH(
        englishName = "Earth",
        arabicName = "Turáb",
        emoji = "🌱",
        startUtcHour = 18,
        endUtcHour = 23,
        accentColorHex = 0xFF81C784, // Terra & Foliage Green
        description = "Block 4 (18:00 – 24:00)",
        badiMonths = "S͟haraf, Sulṭán, Mulk, Ayyám-i-Há, 'Alá'"
    );

    val timeRangeUtc: String
        get() = String.format(Locale.US, "%02d:00 – %02d:00", startUtcHour, endUtcHour + 1)
}

enum class TimeSystemMode(
    val displayName: String,
    val shortBadge: String,
    val description: String
) {
    STANDARD_CIVIL(
        displayName = "Civil Solar Time",
        shortBadge = "Solar / Civil",
        description = "Local 12h/24h time tied to sunset and civil timezones."
    ),
    ELEMENTAL_ETIME(
        displayName = "Elemental Time (etime)",
        shortBadge = "etime",
        description = "Universal 4×6h blocks: Fire 🔥, Air 💨, Water 💧, Earth 🌱."
    ),
    DUAL_DISPLAY(
        displayName = "Dual Time (Civil + etime)",
        shortBadge = "Dual Mode",
        description = "Displays both local Civil Solar time and Universal Elemental Time side-by-side."
    )
}

data class ElementalTime(
    val season: ElementalTimeSeason,
    val blockHour: Int, // 0..5
    val minute: Int,    // 0..59
    val second: Int,    // 0..59
    val totalSecondsInBlock: Int,
    val progress: Float, // 0.0f..1.0f
    val utcInstant: Instant
) {
    val formattedShort: String
        get() = String.format(Locale.US, "%s %d:%02d", season.emoji, blockHour, minute)

    val formattedWithSeconds: String
        get() = String.format(Locale.US, "%s %d:%02d:%02d", season.emoji, blockHour, minute, second)

    val formattedFull: String
        get() = String.format(
            Locale.US,
            "%s %s %d:%02d:%02d (%s)",
            season.emoji,
            season.englishName,
            blockHour,
            minute,
            second,
            season.arabicName
        )

    val blockProgressPercent: Int
        get() = (progress * 100).toInt().coerceIn(0, 100)
}

data class ElementalSequenceBlock(
    val season: ElementalTimeSeason,
    val localStart: LocalTime,
    val localEnd: LocalTime,
    val isCurrent: Boolean,
    val stepOrder: Int, // 0 = Current, 1 = Next (+1), 2 = Following (+2), 3 = Later (+3)
    val localRangeFormatted: String,
    val stepLabel: String
)

object ElementalTimeEngine {

    val SEASONS_IN_ORDER = listOf(
        ElementalTimeSeason.FIRE,
        ElementalTimeSeason.AIR,
        ElementalTimeSeason.WATER,
        ElementalTimeSeason.EARTH
    )

    private val localTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    private val shortLocalTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("h a", Locale.ENGLISH)

    /**
     * Calculates the Elemental Time (etime) for a given instant.
     */
    fun calculate(instant: Instant = Instant.now()): ElementalTime {
        val utcZdt: ZonedDateTime = instant.atZone(ZoneOffset.UTC)
        val utcHour = utcZdt.hour
        val minute = utcZdt.minute
        val second = utcZdt.second

        val season = when (utcHour) {
            in 0..5 -> ElementalTimeSeason.FIRE
            in 6..11 -> ElementalTimeSeason.AIR
            in 12..17 -> ElementalTimeSeason.WATER
            else -> ElementalTimeSeason.EARTH
        }

        val blockHour = utcHour % 6
        val totalSecondsInBlock = (blockHour * 3600) + (minute * 60) + second
        val maxSecondsInBlock = 6 * 3600
        val progress = totalSecondsInBlock.toFloat() / maxSecondsInBlock.toFloat()

        return ElementalTime(
            season = season,
            blockHour = blockHour,
            minute = minute,
            second = second,
            totalSecondsInBlock = totalSecondsInBlock,
            progress = progress.coerceIn(0.0f, 1.0f),
            utcInstant = instant
        )
    }

    /**
     * Calculates the Elemental Time (etime) for a local time and date in a given time zone.
     */
    fun calculateForLocalTime(
        localTime: LocalTime,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ElementalTime {
        val zdt = localTime.atDate(date).atZone(zoneId)
        return calculate(zdt.toInstant())
    }

    /**
     * Converts a specific Elemental Time (season + block hour + minute) into the corresponding local LocalTime.
     */
    fun elementalTimeToLocalTime(
        season: ElementalTimeSeason,
        blockHour: Int,
        minute: Int,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalTime {
        val utcHour = season.startUtcHour + (blockHour % 6)
        val utcZdt = ZonedDateTime.of(date, LocalTime.of(utcHour.coerceIn(0, 23), minute.coerceIn(0, 59)), ZoneOffset.UTC)
        return utcZdt.withZoneSameInstant(zoneId).toLocalTime()
    }

    /**
     * Returns the formatted local time range string for a season on a given date/timeZone (e.g. "6:00 AM – 12:00 PM").
     */
    fun getLocalTimeRangeString(
        season: ElementalTimeSeason,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val start = elementalTimeToLocalTime(season, 0, 0, date, zoneId)
        val end = elementalTimeToLocalTime(season, 5, 59, date, zoneId)
        val startStr = start.format(localTimeFormatter)
        val endStr = end.plusMinutes(1).format(localTimeFormatter)
        return "$startStr – $endStr"
    }

    /**
     * Returns the 4 consecutive elemental blocks starting from the active one,
     * including the following 3 elements with their exact local time references for planning.
     */
    fun getChronologicalSequence(
        instant: Instant = Instant.now(),
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<ElementalSequenceBlock> {
        val currentEtime = calculate(instant)
        val currentSeason = currentEtime.season
        val currentIndex = SEASONS_IN_ORDER.indexOf(currentSeason)

        val labels = listOf("Now (Active)", "Next (+6h)", "Then (+12h)", "Later (+18h)")

        return (0..3).map { offset ->
            val season = SEASONS_IN_ORDER[(currentIndex + offset) % 4]
            val localStart = elementalTimeToLocalTime(season, 0, 0, date, zoneId)
            val localEnd = elementalTimeToLocalTime(season, 5, 59, date, zoneId)
            val rangeFormatted = "${localStart.format(localTimeFormatter)} – ${localEnd.plusMinutes(1).format(localTimeFormatter)}"

            ElementalSequenceBlock(
                season = season,
                localStart = localStart,
                localEnd = localEnd,
                isCurrent = offset == 0,
                stepOrder = offset,
                localRangeFormatted = rangeFormatted,
                stepLabel = labels[offset]
            )
        }
    }

    /**
     * Returns which Badí' Season corresponds to the given Badí' month.
     */
    fun getBadiSeasonForMonth(month: Int): ElementalTimeSeason {
        return when (month) {
            in 1..4 -> ElementalTimeSeason.FIRE
            in 5..9 -> ElementalTimeSeason.AIR
            in 10..15 -> ElementalTimeSeason.WATER
            else -> ElementalTimeSeason.EARTH // 16..19 and Ayyam-i-Ha (0)
        }
    }
}
