package com.example.wellnessapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.wellnessapp.MainActivity
import com.example.wellnessapp.R
import com.example.wellnessapp.data.PrefsManager
import com.example.wellnessapp.databinding.ActivityOnboardingBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Onboarding activity that introduces users to the app with 3 interactive screens.
 * Helps users set up their wellness preferences and goals.
 */
class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var prefsManager: PrefsManager
    private lateinit var onboardingAdapter: OnboardingAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefsManager = PrefsManager(this)
        
        setupViewPager()
        setupClickListeners()
    }
    
    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(this)
        binding.viewPager.adapter = onboardingAdapter
        
        // Disable swipe to prevent accidental navigation
        binding.viewPager.isUserInputEnabled = false
        
        // Setup page change listener
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateNavigationButtons(position)
            }
        })
        
        // Setup tab indicator
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ ->
            // Tab configuration is handled by the tab layout itself
        }.attach()
    }
    
    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingAdapter.itemCount - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                completeOnboarding()
            }
        }
        
        binding.btnSkip.setOnClickListener {
            completeOnboarding()
        }
        
        binding.btnGetStarted.setOnClickListener {
            completeOnboarding()
        }
    }
    
    private fun updateNavigationButtons(position: Int) {
        when (position) {
            0 -> {
                binding.btnSkip.visibility = MaterialButton.VISIBLE
                binding.btnNext.visibility = MaterialButton.VISIBLE
                binding.btnGetStarted.visibility = MaterialButton.GONE
                binding.btnNext.text = "Next"
            }
            1 -> {
                binding.btnSkip.visibility = MaterialButton.VISIBLE
                binding.btnNext.visibility = MaterialButton.VISIBLE
                binding.btnGetStarted.visibility = MaterialButton.GONE
                binding.btnNext.text = "Next"
            }
            2 -> {
                binding.btnSkip.visibility = MaterialButton.GONE
                binding.btnNext.visibility = MaterialButton.GONE
                binding.btnGetStarted.visibility = MaterialButton.VISIBLE
            }
        }
    }
    
    private fun completeOnboarding() {
        // Save user preferences
        saveUserPreferences()
        
        // Mark onboarding as completed
        prefsManager.setBoolean("onboarding_completed", true)
        
        // Start main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun saveUserPreferences() {
        try {
            // Get preferences from fragments
            val preferencesFragment = onboardingAdapter.createFragment(1) as? PreferencesFragment
            val goalsFragment = onboardingAdapter.createFragment(2) as? GoalsFragment
            
            // Save selected interests
            preferencesFragment?.getSelectedInterests()?.let { interests ->
                prefsManager.setString("user_interests", interests.joinToString(","))
            }
            
            // Save wellness level (default to beginner for now)
            prefsManager.setString("wellness_level", "beginner")
            
            // Save goals
            goalsFragment?.getSelectedGoals()?.let { goals ->
                goals["daily_habits"]?.let { prefsManager.setInt("daily_habits_goal", it) }
                goals["daily_steps"]?.let { prefsManager.setInt("daily_steps_goal", it) }
                goals["mood_checkins"]?.let { prefsManager.setInt("daily_mood_goal", it) }
            }
            
        } catch (e: Exception) {
            // Handle any errors silently
        }
    }
    
    private inner class OnboardingAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 3
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> WelcomeFragment()
                1 -> PreferencesFragment()
                2 -> GoalsFragment()
                else -> WelcomeFragment()
            }
        }
    }
}
