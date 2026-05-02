package com.example.wellnessapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wellnessapp.R
import com.example.wellnessapp.databinding.FragmentOnboardingPreferencesBinding
import com.google.android.material.chip.Chip

/**
 * Second onboarding screen - User preferences and interests selection.
 */
class PreferencesFragment : Fragment() {
    
    private var _binding: FragmentOnboardingPreferencesBinding? = null
    private val binding get() = _binding!!
    
    private val selectedInterests = mutableSetOf<String>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupInterestChips()
        
        // Add entrance animation
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(800)
            .start()
    }
    
    private fun setupInterestChips() {
        val interests = listOf(
            "Fitness & Exercise" to "fitness",
            "Mental Health" to "mental_health",
            "Nutrition" to "nutrition",
            "Sleep & Recovery" to "sleep",
            "Mindfulness" to "mindfulness",
            "Social Wellness" to "social",
            "Learning & Growth" to "learning",
            "Creativity" to "creativity"
        )
        
        interests.forEach { (displayName, key) ->
            val chip = Chip(requireContext()).apply {
                text = displayName
                isCheckable = true
                isChecked = false
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedInterests.add(key)
                    } else {
                        selectedInterests.remove(key)
                    }
                }
            }
            binding.interestsChipGroup.addView(chip)
        }
    }
    
    fun getSelectedInterests(): Set<String> = selectedInterests.toSet()
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
