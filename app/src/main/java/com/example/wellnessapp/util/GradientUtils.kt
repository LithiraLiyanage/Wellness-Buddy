package com.example.wellnessapp.util

import android.content.Context
import com.example.wellnessapp.R
import com.example.wellnessapp.model.HabitCategory

/**
 * Utility class for managing gradient backgrounds and visual theming
 */
object GradientUtils {
    
    /**
     * Gets the gradient drawable resource ID for a given habit category
     */
    fun getGradientDrawable(category: HabitCategory): Int {
        return when (category) {
            HabitCategory.HEALTH -> R.drawable.gradient_health
            HabitCategory.FITNESS -> R.drawable.gradient_fitness
            HabitCategory.HYDRATION -> R.drawable.gradient_hydration
            HabitCategory.LEARNING -> R.drawable.gradient_learning
            HabitCategory.MINDFULNESS -> R.drawable.gradient_mindfulness
            HabitCategory.PRODUCTIVITY -> R.drawable.gradient_productivity
            HabitCategory.SOCIAL -> R.drawable.gradient_social
            HabitCategory.CREATIVITY -> R.drawable.gradient_creativity
            HabitCategory.SLEEP -> R.drawable.gradient_sleep
            HabitCategory.NUTRITION -> R.drawable.gradient_nutrition
            HabitCategory.OTHER -> R.drawable.gradient_other
        }
    }
    
    /**
     * Gets the primary color resource ID for a given habit category
     */
    fun getPrimaryColor(category: HabitCategory): Int {
        return when (category) {
            HabitCategory.HEALTH -> R.color.health_primary
            HabitCategory.FITNESS -> R.color.fitness_primary
            HabitCategory.HYDRATION -> R.color.hydration_primary
            HabitCategory.LEARNING -> R.color.learning_primary
            HabitCategory.MINDFULNESS -> R.color.mindfulness_primary
            HabitCategory.PRODUCTIVITY -> R.color.productivity_primary
            HabitCategory.SOCIAL -> R.color.social_primary
            HabitCategory.CREATIVITY -> R.color.creativity_primary
            HabitCategory.SLEEP -> R.color.sleep_primary
            HabitCategory.NUTRITION -> R.color.nutrition_primary
            HabitCategory.OTHER -> R.color.other_primary
        }
    }
    
    /**
     * Gets the secondary color resource ID for a given habit category
     */
    fun getSecondaryColor(category: HabitCategory): Int {
        return when (category) {
            HabitCategory.HEALTH -> R.color.health_secondary
            HabitCategory.FITNESS -> R.color.fitness_secondary
            HabitCategory.HYDRATION -> R.color.hydration_secondary
            HabitCategory.LEARNING -> R.color.learning_secondary
            HabitCategory.MINDFULNESS -> R.color.mindfulness_secondary
            HabitCategory.PRODUCTIVITY -> R.color.productivity_secondary
            HabitCategory.SOCIAL -> R.color.social_secondary
            HabitCategory.CREATIVITY -> R.color.creativity_secondary
            HabitCategory.SLEEP -> R.color.sleep_secondary
            HabitCategory.NUTRITION -> R.color.nutrition_secondary
            HabitCategory.OTHER -> R.color.other_secondary
        }
    }
    
    /**
     * Gets the icon resource ID for a given habit category
     */
    fun getIconDrawable(category: HabitCategory): Int {
        return when (category) {
            HabitCategory.HEALTH -> R.drawable.ic_health
            HabitCategory.FITNESS -> R.drawable.ic_fitness
            HabitCategory.HYDRATION -> R.drawable.ic_hydration
            HabitCategory.LEARNING -> R.drawable.ic_learning
            HabitCategory.MINDFULNESS -> R.drawable.ic_mindfulness
            HabitCategory.PRODUCTIVITY -> R.drawable.ic_productivity
            HabitCategory.SOCIAL -> R.drawable.ic_social
            HabitCategory.CREATIVITY -> R.drawable.ic_creativity
            HabitCategory.SLEEP -> R.drawable.ic_sleep
            HabitCategory.NUTRITION -> R.drawable.ic_nutrition
            HabitCategory.OTHER -> R.drawable.ic_other
        }
    }
}
