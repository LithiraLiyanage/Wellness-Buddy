package com.example.wellnessapp.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.wellnessapp.R
import com.example.wellnessapp.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment for app settings and configuration.
 * Manages notifications, themes, privacy, backup, and data management.
 */
class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        observeViewModel()
        setupIntervalDropdown()
        setupAppVersion()
        setupStorageInfo()
    }
    
    private fun setupClickListeners() {
        // Notification Settings
        binding.enableHydrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            val interval = viewModel.hydrationInterval.value ?: 90
            viewModel.updateHydrationSettings(isChecked, interval)
        }
        
        binding.enableHabitRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateHabitReminders(isChecked)
        }
        
        binding.enableMoodRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateMoodReminders(isChecked)
        }
        
        // Theme Settings
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateDarkMode(isChecked)
        }
        
        binding.accentColorButton.setOnClickListener {
            showAccentColorDialog()
        }
        
        binding.fontSizeButton.setOnClickListener {
            showFontSizeDialog()
        }
        
        // Privacy Settings
        binding.analyticsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAnalytics(isChecked)
        }
        
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateBiometricLock(isChecked)
        }
        
        binding.privacyPolicyButton.setOnClickListener {
            openPrivacyPolicy()
        }
        
        // Backup Settings
        binding.autoBackupSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAutoBackup(isChecked)
        }
        
        binding.backupNowButton.setOnClickListener {
            performBackup()
        }
        
        binding.restoreButton.setOnClickListener {
            showRestoreDialog()
        }
        
        // Data Management
        binding.clearCacheButton.setOnClickListener {
            clearCache()
        }
        
        binding.resetDataButton.setOnClickListener {
            showResetConfirmation()
        }
        
        // About & Support
        binding.helpButton.setOnClickListener {
            openHelp()
        }
        
        binding.contactSupportButton.setOnClickListener {
            contactSupport()
        }
        
        binding.rateAppButton.setOnClickListener {
            rateApp()
        }
        
        // Sensor Features
        binding.enableSensorFeaturesSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSensorSettings(isChecked)
        }
        
        binding.openSensorSettingsButton.setOnClickListener {
            openSensorSettings()
        }
    }
    
    private fun setupIntervalDropdown() {
        val intervals = viewModel.getIntervalOptions()
        val intervalStrings = intervals.map { it.second }
        val intervalValues = intervals.map { it.first }
        
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervalStrings)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.intervalDropdown.setAdapter(adapter)
        
        // Set current selection
        viewModel.hydrationInterval.observe(viewLifecycleOwner, Observer { currentInterval ->
            val index = intervalValues.indexOf(currentInterval)
            if (index != -1) {
                binding.intervalDropdown.setText(intervalStrings[index], false)
            }
        })
        
        binding.intervalDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedInterval = intervalValues[position]
            val enabled = viewModel.hydrationEnabled.value ?: false
            viewModel.updateHydrationSettings(enabled, selectedInterval)
        }
    }
    
    private fun setupAppVersion() {
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            binding.appVersionText.text = packageInfo.versionName
        } catch (e: Exception) {
            binding.appVersionText.text = "1.0.0"
        }
    }
    
    private fun setupStorageInfo() {
        // Calculate app storage usage
        val storageUsed = viewModel.getStorageUsed()
        binding.storageUsedText.text = storageUsed
    }
    
    private fun showAccentColorDialog() {
        val colors = listOf(
            "Purple" to R.color.primary,
            "Blue" to R.color.hydration_primary,
            "Green" to R.color.health_primary,
            "Orange" to R.color.fitness_primary,
            "Pink" to R.color.social_primary,
            "Teal" to R.color.mindfulness_primary
        )
        
        val colorNames = colors.map { it.first }.toTypedArray()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose Accent Color")
            .setItems(colorNames) { _, which ->
                val selectedColor = colors[which]
                viewModel.updateAccentColor(selectedColor.second)
                binding.accentColorButton.text = selectedColor.first
                Snackbar.make(binding.root, "Accent color updated to ${selectedColor.first}", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }
    
    private fun showFontSizeDialog() {
        val fontSizes = arrayOf("Small", "Medium", "Large", "Extra Large")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose Font Size")
            .setItems(fontSizes) { _, which ->
                val selectedSize = fontSizes[which]
                viewModel.updateFontSize(selectedSize)
                binding.fontSizeButton.text = selectedSize
                Snackbar.make(binding.root, "Font size updated to $selectedSize", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }
    
    private fun openPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy-policy"))
        startActivity(intent)
    }
    
    private fun performBackup() {
        viewModel.performBackup()
        Snackbar.make(binding.root, "Backup started...", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showRestoreDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Restore from Backup")
            .setMessage("This will replace all current data with the backup. Are you sure?")
            .setPositiveButton("Restore") { _, _ ->
                viewModel.restoreFromBackup()
                Snackbar.make(binding.root, "Restore started...", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun clearCache() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear Cache")
            .setMessage("This will clear temporary files and free up storage space.")
            .setPositiveButton("Clear") { _, _ ->
                viewModel.clearCache()
                Snackbar.make(binding.root, "Cache cleared successfully", Snackbar.LENGTH_SHORT).show()
                setupStorageInfo() // Refresh storage info
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showResetConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset All Data")
            .setMessage("Are you sure you want to reset all data? This will permanently delete all your habits, mood entries, and settings. This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                viewModel.resetAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun openHelp() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/help"))
        startActivity(intent)
    }
    
    private fun contactSupport() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@example.com")
            putExtra(Intent.EXTRA_SUBJECT, "Wellness App Support")
        }
        startActivity(intent)
    }
    
    private fun rateApp() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().packageName}"))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}"))
            startActivity(webIntent)
        }
    }
    
    private fun observeViewModel() {
        // Hydration Settings
        viewModel.hydrationEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.enableHydrationSwitch.isChecked = enabled
            binding.intervalLayout.visibility = if (enabled) View.VISIBLE else View.GONE
        })
        
        // Habit Reminders
        viewModel.habitRemindersEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.enableHabitRemindersSwitch.isChecked = enabled
        })
        
        // Mood Reminders
        viewModel.moodRemindersEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.enableMoodRemindersSwitch.isChecked = enabled
        })
        
        // Theme Settings
        viewModel.darkModeEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.darkModeSwitch.isChecked = enabled
        })
        
        // Privacy Settings
        viewModel.analyticsEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.analyticsSwitch.isChecked = enabled
        })
        
        viewModel.biometricEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.biometricSwitch.isChecked = enabled
        })
        
        // Backup Settings
        viewModel.autoBackupEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.autoBackupSwitch.isChecked = enabled
        })
        
        // Sensor Settings
        viewModel.sensorEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.enableSensorFeaturesSwitch.isChecked = enabled
        })
        
        // Loading States
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.resetDataButton.isEnabled = !isLoading
            binding.backupNowButton.isEnabled = !isLoading
            binding.restoreButton.isEnabled = !isLoading
        })
        
        // Messages
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })
        
        viewModel.successMessage.observe(viewLifecycleOwner, Observer { successMessage ->
            successMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        })
    }
    
    private fun openSensorSettings() {
        try {
            // For now, show a dialog with sensor settings
            // In a full implementation, you would navigate to a dedicated sensor settings fragment
            showSensorSettingsDialog()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Unable to open sensor settings", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun showSensorSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sensor Settings")
            .setMessage("Sensor features are now available! You can:\n\n" +
                    "• Track your daily steps automatically\n" +
                    "• Set step goals (5K to 20K steps)\n" +
                    "• Use shake gestures for quick mood entry\n" +
                    "• View step progress and statistics\n\n" +
                    "Enable sensor features using the toggle above to get started!")
            .setPositiveButton("Got it!") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}