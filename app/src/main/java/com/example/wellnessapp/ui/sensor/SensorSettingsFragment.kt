package com.example.wellnessapp.ui.sensor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.wellnessapp.databinding.FragmentSensorSettingsBinding
import com.example.wellnessapp.service.SensorService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment for managing sensor settings and viewing sensor data.
 */
class SensorSettingsFragment : Fragment() {
    
    private var _binding: FragmentSensorSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SensorSettingsViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        observeViewModel()
        viewModel.loadSensorData()
    }
    
    private fun setupClickListeners() {
        // Note: Sensor features toggle will be handled in the main settings
        // This fragment focuses on sensor-specific settings
        
        // Shake to mood toggle
        binding.enableShakeToMoodSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateShakeToMood(isChecked)
        }
        
        // Step goal button
        binding.stepGoalButton.setOnClickListener {
            showStepGoalDialog()
        }
        
        // Reset steps button
        binding.resetStepsButton.setOnClickListener {
            showResetStepsDialog()
        }
        
        // Test shake button
        binding.testShakeButton.setOnClickListener {
            viewModel.testShakeDetection()
        }
        
        // View sensor info button
        binding.viewSensorInfoButton.setOnClickListener {
            showSensorInfoDialog()
        }
    }
    
    private fun observeViewModel() {
        // Note: Sensor features enabled is handled in main settings
        
        // Shake to mood enabled
        viewModel.shakeToMoodEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.enableShakeToMoodSwitch.isChecked = enabled
        })
        
        // Today's step count
        viewModel.todayStepCount.observe(viewLifecycleOwner, Observer { steps ->
            binding.todayStepCountText.text = steps.toString()
            updateStepProgress(steps)
        })
        
        // Step goal
        viewModel.stepGoal.observe(viewLifecycleOwner, Observer { goal ->
            binding.stepGoalButton.text = "$goal steps"
            updateStepProgress(viewModel.todayStepCount.value ?: 0)
        })
        
        // Sensor info
        viewModel.sensorInfo.observe(viewLifecycleOwner, Observer { info ->
            // Update sensor info display
        })
        
        // Loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
    
    // Note: Sensor service management is handled in main settings
    
    private fun updateStepProgress(steps: Int) {
        val goal = viewModel.stepGoal.value ?: 10000
        val progress = if (goal > 0) (steps.toFloat() / goal.toFloat()).coerceAtMost(1.0f) else 0f
        
        binding.stepProgressBar.progress = (progress * 100).toInt()
        binding.stepProgressText.text = "${(progress * 100).toInt()}%"
        
        // Update progress color based on completion
        val color = when {
            progress >= 1.0f -> requireContext().getColor(com.example.wellnessapp.R.color.health_primary)
            progress >= 0.5f -> requireContext().getColor(com.example.wellnessapp.R.color.fitness_primary)
            else -> requireContext().getColor(com.example.wellnessapp.R.color.other_primary)
        }
        binding.stepProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color))
    }
    
    private fun showStepGoalDialog() {
        val currentGoal = viewModel.stepGoal.value ?: 10000
        val goals = arrayOf("5,000", "7,500", "10,000", "12,500", "15,000", "20,000")
        val goalValues = intArrayOf(5000, 7500, 10000, 12500, 15000, 20000)
        
        val currentIndex = goalValues.indexOf(currentGoal).takeIf { it >= 0 } ?: 2
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Step Goal")
            .setSingleChoiceItems(goals, currentIndex) { dialog, which ->
                val newGoal = goalValues[which]
                viewModel.setStepGoal(newGoal)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showResetStepsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset Step Count")
            .setMessage("Are you sure you want to reset today's step count? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                viewModel.resetTodaySteps()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showSensorInfoDialog() {
        val sensorInfo = viewModel.sensorInfo.value ?: "No sensor information available"
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sensor Information")
            .setMessage(sensorInfo)
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
