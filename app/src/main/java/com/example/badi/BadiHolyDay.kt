package com.example.badi

import java.time.LocalDate

/**
 * Holy Days in the Bahá'í Calendar.
 */
data class BadiHolyDay(
    val id: String,
    val name: String,
    val arabicName: String,
    val isWorkSuspended: Boolean,
    val commemorationTime: String,
    val description: String,
    val badiMonth: Int? = null, // null for movable holy days
    val badiDay: Int? = null,
    val isMovable: Boolean = false
) {
    companion object {
        val NAW_RUZ = BadiHolyDay(
            id = "naw_ruz",
            name = "Naw-Rúz (Bahá\'í New Year)",
            arabicName = "نوروز",
            isWorkSuspended = true,
            commemorationTime = "Sunset on eve / all day",
            description = "The first day of the Badí' calendar year, coinciding with the vernal equinox in Tehran.",
            badiMonth = 1,
            badiDay = 1
        )

        val FIRST_DAY_OF_RIDVAN = BadiHolyDay(
            id = "ridvan_1",
            name = "First Day of Riḍván",
            arabicName = "عيد الرضوان - اليوم الأول",
            isWorkSuspended = true,
            commemorationTime = "3:00 PM (Standard time of Bahá\'u\'lláh\'s declaration)",
            description = "Commemorating Bahá\'u\'lláh\'s arrival at the Najibiyyih Garden (Garden of Riḍván) in Baghdad in 1863 and the declaration of His Mission.",
            badiMonth = 2,
            badiDay = 13
        )

        val NINTH_DAY_OF_RIDVAN = BadiHolyDay(
            id = "ridvan_9",
            name = "Ninth Day of Riḍván",
            arabicName = "عيد الرضوان - اليوم التاسع",
            isWorkSuspended = true,
            commemorationTime = "All day",
            description = "Commemorating the arrival of Bahá\'u\'lláh\'s family in the Garden of Riḍván.",
            badiMonth = 3,
            badiDay = 2
        )

        val TWELFTH_DAY_OF_RIDVAN = BadiHolyDay(
            id = "ridvan_12",
            name = "Twelfth Day of Riḍván",
            arabicName = "عيد الرضوان - اليوم الثاني عشر",
            isWorkSuspended = true,
            commemorationTime = "All day",
            description = "Commemorating Bahá\'u\'lláh\'s departure from the Garden of Riḍván for Constantinople.",
            badiMonth = 3,
            badiDay = 5
        )

        val DECLARATION_OF_THE_BAB = BadiHolyDay(
            id = "declaration_of_bab",
            name = "Declaration of the Báb",
            arabicName = "بعثة الباب",
            isWorkSuspended = true,
            commemorationTime = "~10:00 PM (approx. 2 hours and 11 minutes after sunset on eve)",
            description = "Commemorating the Báb\'s declaration of His mission to Mullá Ḥusayn in Shíráz, Iran in 1844.",
            badiMonth = 4,
            badiDay = 8
        )

        val ASCENSION_OF_BAHAULLAH = BadiHolyDay(
            id = "ascension_of_bahaullah",
            name = "Ascension of Bahá\'u\'lláh",
            arabicName = "صعود بهاءالله",
            isWorkSuspended = true,
            commemorationTime = "3:00 AM",
            description = "Commemorating the passing of Bahá\'u\'lláh at the Mansion of Bahjí near Acre in 1892.",
            badiMonth = 4,
            badiDay = 13
        )

        val MARTYRDOM_OF_THE_BAB = BadiHolyDay(
            id = "martyrdom_of_bab",
            name = "Martyrdom of the Báb",
            arabicName = "استشهاد الباب",
            isWorkSuspended = true,
            commemorationTime = "Solar Noon (~12:00 PM)",
            description = "Commemorating the execution of the Báb by firing squad in Tabríz, Iran in 1850.",
            badiMonth = 6,
            badiDay = 17
        )

        val BIRTH_OF_THE_BAB = BadiHolyDay(
            id = "birth_of_bab",
            name = "Birth of the Báb",
            arabicName = "ولادة الباب",
            isWorkSuspended = true,
            commemorationTime = "All day",
            description = "First of the Twin Holy Birthdays, celebrating the birth of the Báb in Shíráz in 1819 (1st day following 8th new moon after Naw-Rúz).",
            isMovable = true
        )

        val BIRTH_OF_BAHAULLAH = BadiHolyDay(
            id = "birth_of_bahaullah",
            name = "Birth of Bahá\'u\'lláh",
            arabicName = "ولادة بهاءالله",
            isWorkSuspended = true,
            commemorationTime = "All day",
            description = "Second of the Twin Holy Birthdays, celebrating the birth of Bahá\'u\'lláh in Tehran in 1817 (2nd day following 8th new moon after Naw-Rúz).",
            isMovable = true
        )

        val DAY_OF_THE_COVENANT = BadiHolyDay(
            id = "day_of_covenant",
            name = "Day of the Covenant",
            arabicName = "يوم الميثاق",
            isWorkSuspended = false,
            commemorationTime = "All day",
            description = "Commemorating the appointment of \'Abdu\'l-Bahá as the Centre of the Covenant of Bahá\'u\'lláh.",
            badiMonth = 14,
            badiDay = 4
        )

        val ASCENSION_OF_ABDUL_BAHA = BadiHolyDay(
            id = "ascension_of_abdul_baha",
            name = "Ascension of \'Abdu\'l-Bahá",
            arabicName = "صعود عبدالبهاء",
            isWorkSuspended = false,
            commemorationTime = "1:00 AM",
            description = "Commemorating the passing of \'Abdu\'l-Bahá in Haifa in 1921.",
            badiMonth = 14,
            badiDay = 6
        )

        val ALL_FIXED = listOf(
            NAW_RUZ,
            FIRST_DAY_OF_RIDVAN,
            NINTH_DAY_OF_RIDVAN,
            TWELFTH_DAY_OF_RIDVAN,
            DECLARATION_OF_THE_BAB,
            ASCENSION_OF_BAHAULLAH,
            MARTYRDOM_OF_THE_BAB,
            DAY_OF_THE_COVENANT,
            ASCENSION_OF_ABDUL_BAHA
        )
    }
}
