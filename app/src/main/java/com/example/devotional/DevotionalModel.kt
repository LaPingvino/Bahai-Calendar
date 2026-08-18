package com.example.devotional

enum class WritingCategory(val displayName: String, val iconEmoji: String) {
    OBLIGATORY("Obligatory Prayers", "🛐"),
    FASTING("The Fast & Dawn", "🌅"),
    HIDDEN_WORDS("The Hidden Words", "✨"),
    MORNING_EVENING("Morning & Evening", "☀️"),
    HEALING("Healing & Solace", "🌿"),
    ASSISTANCE("Assistance & Tests", "🛡️"),
    UNITY_PEACE("Unity & Peace", "🕊️"),
    HOLY_DAYS("Holy Days & Tablets", "📜"),
    DEVOTIONAL_PROGRAMS("Devotional Gatherings", "👥")
}

data class HolyWriting(
    val id: String,
    val title: String,
    val author: String,
    val category: WritingCategory,
    val textEnglish: String,
    val textArabicPersian: String? = null,
    val sourceReference: String,
    val instructions: String? = null,
    val isFavorite: Boolean = false
)

data class DevotionalProgram(
    val id: String,
    val title: String,
    val description: String,
    val themeEmoji: String,
    val writingIds: List<String>
)

data class CityLocation(
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)
