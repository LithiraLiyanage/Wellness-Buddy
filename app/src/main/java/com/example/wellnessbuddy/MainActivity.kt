package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.wellnessapp.databinding.ActivityMainBinding
import com.example.wellnessapp.util.NotificationUtils
import com.example.wellnessapp.widget.WellnessWidgetProvider
import com.example.wellnessapp.data.PrefsManager
import com.example.wellnessapp.ui.onboarding.OnboardingActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if onboarding is completed
        val prefsManager = PrefsManager(this)
        val isOnboardingCompleted = prefsManager.getBoolean("onboarding_completed", false)
        
        if (!isOnboardingCompleted) {
            // Start onboarding
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        // Create notification channel
        NotificationUtils.createNotificationChannel(this)
        
        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        enableEdgeToEdge()
        
        // Set up navigation
        setupNavigation()
        
        // Handle deep link from widget
        handleDeepLink(intent)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Set up bottom navigation
        binding.bottomNavigation.setupWithNavController(navController)
    }
    
    private fun handleDeepLink(intent: Intent?) {
        val fragment = intent?.getStringExtra("fragment")
        if (fragment != null) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            
            when (fragment) {
                "habits" -> navController.navigate(R.id.habitsFragment)
                "mood" -> navController.navigate(R.id.moodFragment)
                "settings" -> navController.navigate(R.id.settingsFragment)
                "about" -> navController.navigate(R.id.aboutFragment)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Update widget when returning to app
        WellnessWidgetProvider.triggerWidgetUpdate(this)
    }
}