package com.example.wellnessapp.util

/**
 * Utility class for emoji operations and mood scoring.
 */
object EmojiUtils {
    
    /**
     * List of allowed mood emojis
     */
    val ALLOWED_EMOJIS = listOf(
        "😀", "😃", "😄", "😁", "😊", "😍", "🥰", "😘", "😗", "😙", "😚", "🙂", "🤗", "🤩", "🤔", "🤨", "😐", "😑", "😶", "🙄", "😏", "😣", "😥", "😮", "🤐", "😯", "😪", "😫", "😴", "😌", "😛", "😜", "😝", "🤤", "😒", "😓", "😔", "😕", "🙃", "🤑", "😲", "☹️", "🙁", "😖", "😞", "😟", "😤", "😢", "😭", "😦", "😧", "😨", "😩", "🤯", "😬", "😰", "😱", "🥵", "🥶", "😳", "🤪", "😵", "😡", "😠", "🤬", "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "😇", "🤠", "🤡", "🥳", "🥴", "🥺", "🤥", "🤫", "🤭", "🧐", "🤓", "😎", "🤩", "🥸"
    )
    
    /**
     * Map of emojis to their numeric mood scores (1-5 scale)
     */
    val EMOJI_TO_SCORE = mapOf(
        // Very happy (5)
        "😀" to 5, "😃" to 5, "😄" to 5, "😁" to 5, "😊" to 5, "😍" to 5, "🥰" to 5, "😘" to 5, "😗" to 5, "😙" to 5, "😚" to 5, "🤗" to 5, "🤩" to 5, "🥳" to 5,
        
        // Happy (4)
        "🙂" to 4, "😌" to 4, "😇" to 4, "🤠" to 4, "😎" to 4, "🥸" to 4,
        
        // Neutral (3)
        "🤔" to 3, "🤨" to 3, "😐" to 3, "😑" to 3, "😶" to 3, "🙄" to 3, "😏" to 3, "😮" to 3, "🤐" to 3, "😯" to 3, "😛" to 3, "😜" to 3, "😝" to 3, "🤤" to 3, "🙃" to 3, "🤑" to 3, "😲" to 3, "🤯" to 3, "😬" to 3, "🤪" to 3, "😵" to 3, "🤠" to 3, "🤡" to 3, "🥴" to 3, "🤥" to 3, "🤫" to 3, "🤭" to 3, "🧐" to 3, "🤓" to 3,
        
        // Sad (2)
        "😣" to 2, "😥" to 2, "😪" to 2, "😫" to 2, "😴" to 2, "😒" to 2, "😓" to 2, "😔" to 2, "😕" to 2, "☹️" to 2, "🙁" to 2, "😖" to 2, "😞" to 2, "😟" to 2, "😤" to 2, "😦" to 2, "😧" to 2, "😨" to 2, "😩" to 2, "😰" to 2, "😱" to 2, "🥵" to 2, "🥶" to 2, "😳" to 2, "😷" to 2, "🤒" to 2, "🤕" to 2, "🤢" to 2, "🤮" to 2, "🤧" to 2, "🥺" to 2,
        
        // Very sad (1)
        "😢" to 1, "😭" to 1, "😡" to 1, "😠" to 1, "🤬" to 1
    )
    
    /**
     * Default emoji for new mood entries
     */
    const val DEFAULT_EMOJI = "😐"
    
    /**
     * Returns the mood score (1-5) for a given emoji
     */
    fun getMoodScore(emoji: String): Int {
        return EMOJI_TO_SCORE[emoji] ?: 3 // Default to neutral if not found
    }
    
    /**
     * Returns true if the emoji is in the allowed list
     */
    fun isValidEmoji(emoji: String): Boolean {
        return ALLOWED_EMOJIS.contains(emoji)
    }
    
    /**
     * Returns a list of emojis grouped by mood level for easy selection
     */
    fun getEmojisByMoodLevel(): Map<Int, List<String>> {
        return EMOJI_TO_SCORE.entries.groupBy({ it.value }, { it.key })
    }
    
    /**
     * Returns the most common emoji for a given mood score
     */
    fun getRepresentativeEmoji(score: Int): String {
        return when (score) {
            5 -> "😀"
            4 -> "🙂"
            3 -> "😐"
            2 -> "😔"
            1 -> "😢"
            else -> DEFAULT_EMOJI
        }
    }
    
    /**
     * Calculates the average mood score for a list of emojis
     */
    fun calculateAverageMood(emojis: List<String>): Double {
        if (emojis.isEmpty()) return 0.0
        val totalScore = emojis.sumOf { getMoodScore(it) }
        return totalScore.toDouble() / emojis.size
    }
    
    /**
     * Returns a mood description based on the score
     */
    fun getMoodDescription(score: Int): String {
        return when (score) {
            5 -> "Excellent"
            4 -> "Good"
            3 -> "Neutral"
            2 -> "Poor"
            1 -> "Very Poor"
            else -> "Unknown"
        }
    }
}
