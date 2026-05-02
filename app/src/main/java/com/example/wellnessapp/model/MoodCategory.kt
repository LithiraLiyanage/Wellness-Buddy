package com.example.wellnessapp.model

/**
 * Represents a mood category with associated emojis and metadata.
 */
data class MoodCategory(
    val id: String,
    val name: String,
    val emojis: List<String>,
    val colorResName: String,
    val description: String,
    val moodValue: Float // 1-10 scale
) {
    companion object {
        fun getAllCategories(): List<MoodCategory> {
            return listOf(
                MoodCategory(
                    id = "happy",
                    name = "Happy",
                    emojis = listOf("😊", "😄", "😃", "😁", "🤗", "😀", "🙂", "😇", "🥰", "😍"),
                    colorResName = "mood_happy_primary",
                    description = "Feeling joyful and positive",
                    moodValue = 8f
                ),
                MoodCategory(
                    id = "excited",
                    name = "Excited",
                    emojis = listOf("🤩", "😎", "🤪", "😜", "😝", "🤭", "🥳", "🎉", "🚀", "💫"),
                    colorResName = "mood_excited_primary",
                    description = "Full of energy and enthusiasm",
                    moodValue = 9f
                ),
                MoodCategory(
                    id = "calm",
                    name = "Calm",
                    emojis = listOf("😌", "😴", "🤤", "😪", "🧘", "🙏", "🌊", "🌸", "🍃", "☁️"),
                    colorResName = "mood_calm_primary",
                    description = "Peaceful and relaxed",
                    moodValue = 6f
                ),
                MoodCategory(
                    id = "neutral",
                    name = "Neutral",
                    emojis = listOf("😐", "😑", "😶", "🤔", "😏", "🙃", "🤷", "😕", "🤨", "😒"),
                    colorResName = "mood_neutral_primary",
                    description = "Neither happy nor sad",
                    moodValue = 5f
                ),
                MoodCategory(
                    id = "sad",
                    name = "Sad",
                    emojis = listOf("😢", "😭", "😔", "😞", "😟", "🙁", "☹️", "😣", "😖", "💔"),
                    colorResName = "mood_sad_primary",
                    description = "Feeling down or blue",
                    moodValue = 2f
                ),
                MoodCategory(
                    id = "angry",
                    name = "Angry",
                    emojis = listOf("😠", "😡", "🤬", "😤", "😾", "👿", "💢", "🔥", "⚡", "🌪️"),
                    colorResName = "mood_angry_primary",
                    description = "Feeling frustrated or mad",
                    moodValue = 1f
                )
            )
        }
        
        fun getCategoryByEmoji(emoji: String): MoodCategory? {
            return getAllCategories().find { category ->
                category.emojis.contains(emoji)
            }
        }
        
        fun getCategoryById(id: String): MoodCategory? {
            return getAllCategories().find { it.id == id }
        }
    }
}
