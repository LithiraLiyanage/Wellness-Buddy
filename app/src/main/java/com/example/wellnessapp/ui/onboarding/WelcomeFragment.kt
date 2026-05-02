package com.example.wellnessapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wellnessapp.R
import com.example.wellnessapp.databinding.FragmentOnboardingWelcomeBinding

/**
 * First onboarding screen - Welcome and introduction to the app.
 */
class WelcomeFragment : Fragment() {
    
    private var _binding: FragmentOnboardingWelcomeBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Add entrance animation
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(800)
            .start()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
