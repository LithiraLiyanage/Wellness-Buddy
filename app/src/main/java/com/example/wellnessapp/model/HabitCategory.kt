package com.example.wellnessapp.model

import androidx.annotation.DrawableRes
import androidx.annotation.ColorRes
import com.example.wellnessapp.R

/**
 * Represents different categories of habits with associated icons, colors, and gradients.
 */
enum class HabitCategory(
    val displayName: String,
    val emoji: String,
    @DrawableRes val iconRes: Int,
    @ColorRes val primaryColor: Int,
    @ColorRes val secondaryColor: Int,
    @ColorRes val gradientStart: Int,
    @ColorRes val gradientEnd: Int
) {
    HEALTH(
        displayName = "Health",
        emoji = "💊",
        iconRes = R.drawable.ic_health,
        primaryColor = R.color.health_primary,
        secondaryColor = R.color.health_secondary,
        gradientStart = R.color.health_gradient_start,
        gradientEnd = R.color.health_gradient_end
    ),
    
    FITNESS(
        displayName = "Fitness",
        emoji = "🏃",
        iconRes = R.drawable.ic_fitness,
        primaryColor = R.color.fitness_primary,
        secondaryColor = R.color.fitness_secondary,
        gradientStart = R.color.fitness_gradient_start,
        gradientEnd = R.color.fitness_gradient_end
    ),
    
    HYDRATION(
        displayName = "Hydration",
        emoji = "💧",
        iconRes = R.drawable.ic_hydration,
        primaryColor = R.color.hydration_primary,
        secondaryColor = R.color.hydration_secondary,
        gradientStart = R.color.hydration_gradient_start,
        gradientEnd = R.color.hydration_gradient_end
    ),
    
    LEARNING(
        displayName = "Learning",
        emoji = "📚",
        iconRes = R.drawable.ic_learning,
        primaryColor = R.color.learning_primary,
        secondaryColor = R.color.learning_secondary,
        gradientStart = R.color.learning_gradient_start,
        gradientEnd = R.color.learning_gradient_end
    ),
    
    MINDFULNESS(
        displayName = "Mindfulness",
        emoji = "🧘",
        iconRes = R.drawable.ic_mindfulness,
        primaryColor = R.color.mindfulness_primary,
        secondaryColor = R.color.mindfulness_secondary,
        gradientStart = R.color.mindfulness_gradient_start,
        gradientEnd = R.color.mindfulness_gradient_end
    ),
    
    PRODUCTIVITY(
        displayName = "Productivity",
        emoji = "⚡",
        iconRes = R.drawable.ic_productivity,
        primaryColor = R.color.productivity_primary,
        secondaryColor = R.color.productivity_secondary,
        gradientStart = R.color.productivity_gradient_start,
        gradientEnd = R.color.productivity_gradient_end
    ),
    
    SOCIAL(
        displayName = "Social",
        emoji = "👥",
        iconRes = R.drawable.ic_social,
        primaryColor = R.color.social_primary,
        secondaryColor = R.color.social_secondary,
        gradientStart = R.color.social_gradient_start,
        gradientEnd = R.color.social_gradient_end
    ),
    
    CREATIVITY(
        displayName = "Creativity",
        emoji = "🎨",
        iconRes = R.drawable.ic_creativity,
        primaryColor = R.color.creativity_primary,
        secondaryColor = R.color.creativity_secondary,
        gradientStart = R.color.creativity_gradient_start,
        gradientEnd = R.color.creativity_gradient_end
    ),
    
    SLEEP(
        displayName = "Sleep",
        emoji = "😴",
        iconRes = R.drawable.ic_sleep,
        primaryColor = R.color.sleep_primary,
        secondaryColor = R.color.sleep_secondary,
        gradientStart = R.color.sleep_gradient_start,
        gradientEnd = R.color.sleep_gradient_end
    ),
    
    NUTRITION(
        displayName = "Nutrition",
        emoji = "🥗",
        iconRes = R.drawable.ic_nutrition,
        primaryColor = R.color.nutrition_primary,
        secondaryColor = R.color.nutrition_secondary,
        gradientStart = R.color.nutrition_gradient_start,
        gradientEnd = R.color.nutrition_gradient_end
    ),
    
    OTHER(
        displayName = "Other",
        emoji = "⭐",
        iconRes = R.drawable.ic_other,
        primaryColor = R.color.other_primary,
        secondaryColor = R.color.other_secondary,
        gradientStart = R.color.other_gradient_start,
        gradientEnd = R.color.other_gradient_end
    );
    
    companion object {
        /**
         * Detects the most likely category for a habit based on its title
         */
        fun detectCategory(habitTitle: String): HabitCategory {
            val title = habitTitle.lowercase()
            
            return when {
                title.contains("water") || title.contains("drink") || title.contains("hydrat") -> HYDRATION
                title.contains("exercise") || title.contains("workout") || title.contains("run") || 
                title.contains("walk") || title.contains("gym") || title.contains("fitness") -> FITNESS
                title.contains("read") || title.contains("book") || title.contains("study") || 
                title.contains("learn") || title.contains("course") -> LEARNING
                title.contains("meditat") || title.contains("mindful") || title.contains("yoga") || 
                title.contains("breath") -> MINDFULNESS
                title.contains("work") || title.contains("task") || title.contains("productiv") || 
                title.contains("focus") -> PRODUCTIVITY
                title.contains("social") || title.contains("friend") || title.contains("call") || 
                title.contains("meet") -> SOCIAL
                title.contains("creat") || title.contains("art") || title.contains("music") || 
                title.contains("write") -> CREATIVITY
                title.contains("sleep") || title.contains("bed") || title.contains("rest") -> SLEEP
                title.contains("eat") || title.contains("food") || title.contains("meal") || 
                title.contains("nutrition") || title.contains("diet") -> NUTRITION
                title.contains("health") || title.contains("vitamin") || title.contains("medicine") -> HEALTH
                else -> OTHER
            }
        }
        
        /**
         * Gets all categories as a list for selection
         */
        fun getAllCategories(): List<HabitCategory> = values().toList()
    }
}
