package com.example.wellnessapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wellnessapp.R
import com.example.wellnessapp.databinding.FragmentOnboardingGoalsBinding
import com.google.android.material.slider.Slider

/**
 * Third onboarding screen - Goal setting and final setup.
 */
class GoalsFragment : Fragment() {
    
    private var _binding: FragmentOnboardingGoalsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupGoalSliders()
        
        // Add entrance animation
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(800)
            .start()
    }
    
    private fun setupGoalSliders() {
        // Daily habits goal slider
        binding.habitsGoalSlider.addOnChangeListener { _, value, _ ->
            binding.habitsGoalText.text = "${value.toInt()} habits per day"
        }
        
        // Step goal slider
        binding.stepsGoalSlider.addOnChangeListener { _, value, _ ->
            val steps = (value * 1000).toInt()
            binding.stepsGoalText.text = "${String.format("%,d", steps)} steps per day"
        }
        
        // Mood check-ins goal slider
        binding.moodGoalSlider.addOnChangeListener { _, value, _ ->
            binding.moodGoalText.text = "${value.toInt()} mood check-ins per day"
        }
    }
    
    fun getSelectedGoals(): Map<String, Int> {
        return mapOf(
            "daily_habits" to binding.habitsGoalSlider.value.toInt(),
            "daily_steps" to (binding.stepsGoalSlider.value * 1000).toInt(),
            "mood_checkins" to binding.moodGoalSlider.value.toInt()
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
