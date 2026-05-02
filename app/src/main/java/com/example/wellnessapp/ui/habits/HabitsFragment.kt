package com.example.wellnessapp.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.databinding.FragmentHabitsBinding
import com.example.wellnessapp.model.Habit
import com.example.wellnessapp.model.HabitCategory
import com.example.wellnessapp.data.SensorRepository
import com.example.wellnessapp.data.PrefsManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlinx.coroutines.*

enum class HabitView {
    DAILY, WEEKLY, MONTHLY
}

/**
 * Fragment for displaying and managing daily habits.
 * Shows completion percentage and allows CRUD operations on habits.
 */
class HabitsFragment : Fragment(), HabitDetailDialog.HabitDetailListener {
    
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    
    val viewModel: HabitsViewModel by activityViewModels()
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var calendarAdapter: CalendarAdapter
    private var hasShownCelebrationToday = false
    private var currentView = HabitView.DAILY
    
    // Step counter components
    private lateinit var sensorRepository: SensorRepository
    private lateinit var prefsManager: PrefsManager
    private var stepCounterJob: Job? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        
        // Wrap the root view in SwipeRefreshLayout
        val swipeRefreshLayout = SwipeRefreshLayout(requireContext())
        swipeRefreshLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        swipeRefreshLayout.addView(binding.root)
        
        return swipeRefreshLayout
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize step counter components
        prefsManager = PrefsManager(requireContext())
        sensorRepository = SensorRepository(prefsManager)
        
        setupCalendar()
        setupViewSelector()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        setupStepCounter()
        animateViews()
    }
    
    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter { selectedDate ->
            // Handle date selection
            // TODO: Update habits for selected date
        }
        
        // Find the calendar container in the included layout
        val calendarContainer = binding.root.findViewById<ViewGroup>(R.id.calendar_container)
        if (calendarContainer != null) {
            // Add calendar items programmatically
            val inflater = LayoutInflater.from(requireContext())
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -7) // Start from 7 days ago
            
            for (i in 0..14) { // Show 15 days total
                val dayView = inflater.inflate(R.layout.calendar_day_item, calendarContainer, false)
                val dayOfWeek = dayView.findViewById<TextView>(R.id.day_of_week)
                val dayNumber = dayView.findViewById<TextView>(R.id.day_number)
                val completionIndicator = dayView.findViewById<View>(R.id.completion_indicator)
                
                val dateFormat = java.text.SimpleDateFormat("EEE", Locale.getDefault())
                val dayFormat = java.text.SimpleDateFormat("d", Locale.getDefault())
                
                dayOfWeek.text = dateFormat.format(calendar.time)
                dayNumber.text = dayFormat.format(calendar.time)
                
                // Check if this is today
                val today = Calendar.getInstance()
                val isToday = calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                             calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                
                dayView.isSelected = isToday
                
                // Show completion indicator for today
                completionIndicator.visibility = if (isToday) View.VISIBLE else View.GONE
                
                dayView.setOnClickListener {
                    // Handle date selection
                    // TODO: Update habits for selected date
                }
                
                calendarContainer.addView(dayView)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }
    
    private fun animateViews() {
        // Animate calendar header (included layout)
        val calendarHeader = binding.root.findViewById<View>(R.id.calendar_header)
        calendarHeader?.let {
            it.alpha = 0f
            it.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(100)
                .start()
        }
        
        // Animate progress card
        binding.progressCard.alpha = 0f
        binding.progressCard.translationY = 50f
        binding.progressCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(200)
            .setInterpolator(android.view.animation.OvershootInterpolator(1.2f))
            .start()
        
        // Animate habits list
        binding.habitsRecyclerView.alpha = 0f
        binding.habitsRecyclerView.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(300)
            .start()
        
        // Animate FAB
        binding.addHabitFab.alpha = 0f
        binding.addHabitFab.scaleX = 0f
        binding.addHabitFab.scaleY = 0f
        binding.addHabitFab.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setStartDelay(400)
            .setInterpolator(android.view.animation.OvershootInterpolator(1.5f))
            .start()
    }
    
    private fun setupViewSelector() {
        val dailyBtn = binding.root.findViewById<TextView>(R.id.daily_view_btn)
        val weeklyBtn = binding.root.findViewById<TextView>(R.id.weekly_view_btn)
        val monthlyBtn = binding.root.findViewById<TextView>(R.id.monthly_view_btn)
        
        dailyBtn.setOnClickListener { switchView(HabitView.DAILY) }
        weeklyBtn.setOnClickListener { switchView(HabitView.WEEKLY) }
        monthlyBtn.setOnClickListener { switchView(HabitView.MONTHLY) }
        
        // Set initial selection
        updateViewSelector(HabitView.DAILY)
    }
    
    private fun switchView(view: HabitView) {
        if (currentView == view) return
        
        currentView = view
        updateViewSelector(view)
        updateViewContent(view)
        
        // Force refresh the habits list to ensure it's visible
        viewModel.loadHabits()
    }
    
    private fun updateViewSelector(view: HabitView) {
        val dailyBtn = binding.root.findViewById<TextView>(R.id.daily_view_btn)
        val weeklyBtn = binding.root.findViewById<TextView>(R.id.weekly_view_btn)
        val monthlyBtn = binding.root.findViewById<TextView>(R.id.monthly_view_btn)
        
        // Reset all buttons
        dailyBtn.isSelected = false
        weeklyBtn.isSelected = false
        monthlyBtn.isSelected = false
        
        // Update text colors
        dailyBtn.setTextColor(requireContext().getColor(if (view == HabitView.DAILY) R.color.on_primary else R.color.on_surface))
        weeklyBtn.setTextColor(requireContext().getColor(if (view == HabitView.WEEKLY) R.color.on_primary else R.color.on_surface))
        monthlyBtn.setTextColor(requireContext().getColor(if (view == HabitView.MONTHLY) R.color.on_primary else R.color.on_surface))
        
        // Select current button
        when (view) {
            HabitView.DAILY -> dailyBtn.isSelected = true
            HabitView.WEEKLY -> weeklyBtn.isSelected = true
            HabitView.MONTHLY -> monthlyBtn.isSelected = true
        }
    }
    
    private fun updateViewContent(view: HabitView) {
        val dailyView = binding.root.findViewById<View>(R.id.daily_view)
        val weeklyView = binding.root.findViewById<View>(R.id.weekly_view)
        val monthlyView = binding.root.findViewById<View>(R.id.monthly_view)
        val habitsRecyclerView = binding.habitsRecyclerView
        
        // Hide all view headers
        dailyView.visibility = View.GONE
        weeklyView.visibility = View.GONE
        monthlyView.visibility = View.GONE
        
        // Always show habits list
        habitsRecyclerView.visibility = View.VISIBLE
        
        // Show selected view header and update constraints
        when (view) {
            HabitView.DAILY -> {
                dailyView.visibility = View.VISIBLE
                // Update RecyclerView constraints to be below daily view
                val layoutParams = habitsRecyclerView.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToBottom = R.id.daily_view
                habitsRecyclerView.layoutParams = layoutParams
            }
            HabitView.WEEKLY -> {
                weeklyView.visibility = View.VISIBLE
                // Update RecyclerView constraints to be below weekly view
                val layoutParams = habitsRecyclerView.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToBottom = R.id.weekly_view
                habitsRecyclerView.layoutParams = layoutParams
            }
            HabitView.MONTHLY -> {
                monthlyView.visibility = View.VISIBLE
                // Update RecyclerView constraints to be below monthly view
                val layoutParams = habitsRecyclerView.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToBottom = R.id.monthly_view
                habitsRecyclerView.layoutParams = layoutParams
            }
        }
        
        // Force layout update to apply constraint changes
        binding.root.requestLayout()
    }
    
    
    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onEditClick = { habit -> showEditHabitDialog(habit) },
            onDeleteClick = { habit -> showDeleteConfirmation(habit) },
            onToggleCompletion = { habit -> viewModel.toggleHabitCompletion(habit.id) },
            onIncrementCompletion = { habit -> viewModel.incrementHabitCompletion(habit.id) },
            onDecrementCompletion = { habit -> viewModel.decrementHabitCompletion(habit.id) },
            getCompletionCount = { habitId -> viewModel.getTodayCompletionCount(habitId) },
            isCompleted = { habitId -> viewModel.isHabitCompletedToday(habitId) }
        )
        
        binding.habitsRecyclerView.apply {
            adapter = habitAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        
        // Attach swipe functionality
        habitAdapter.attachSwipeToRecyclerView(binding.habitsRecyclerView)
    }
    
    private fun setupClickListeners() {
        binding.addHabitFab.setOnClickListener {
            showEditHabitDialog()
        }
        
        // Setup pull-to-refresh (get the SwipeRefreshLayout from the root view)
        val swipeRefreshLayout = view?.parent as? SwipeRefreshLayout
        swipeRefreshLayout?.setOnRefreshListener {
            refreshData()
        }
        
        // Customize refresh colors
        swipeRefreshLayout?.setColorSchemeResources(
            R.color.primary,
            R.color.primary_variant,
            R.color.secondary
        )
        
    }
    
    private fun setupStepCounter() {
        // Always show step counter card for now
        binding.root.findViewById<View>(R.id.step_counter_card)?.visibility = View.VISIBLE
        
        // Start periodic step counter updates
        startStepCounterUpdates()
        
        // Setup step counter click listener
        binding.root.findViewById<View>(R.id.step_counter_card)?.setOnClickListener {
            showStepCounterDetails()
        }
        
        // Add long click to simulate step increment (for testing)
        binding.root.findViewById<View>(R.id.step_counter_card)?.setOnLongClickListener {
            simulateStepIncrement()
            true
        }
        
        // Initial update
        updateStepCounterDisplay()
    }
    
    private fun startStepCounterUpdates() {
        stepCounterJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                updateStepCounterDisplay()
                delay(5000) // Update every 5 seconds
            }
        }
    }
    
    private fun updateStepCounterDisplay() {
        try {
            val todayData = sensorRepository.getTodaySensorData()
            val stepGoal = sensorRepository.getStepGoal()
            
            // For testing, let's add some sample data if no real data exists
            val currentSteps = if (todayData.dailySteps > 0) todayData.dailySteps else 7543 // Sample data
            val currentGoal = if (stepGoal > 0) stepGoal else 10000 // Default goal
            
            // Update step count
            val stepCountText = binding.root.findViewById<TextView>(R.id.step_count_text)
            stepCountText?.text = String.format("%,d", currentSteps)
            
            // Update step goal
            val stepGoalText = binding.root.findViewById<TextView>(R.id.step_goal_text)
            stepGoalText?.text = "Goal: ${String.format("%,d", currentGoal)}"
            
            // Calculate progress
            val progress = if (currentGoal > 0) {
                (currentSteps.toFloat() / currentGoal.toFloat()).coerceAtMost(1.0f)
            } else 0f
            
            val progressPercentage = (progress * 100).toInt()
            
            // Update progress percentage
            val progressPercentageText = binding.root.findViewById<TextView>(R.id.step_progress_percentage)
            progressPercentageText?.text = "$progressPercentage%"
            
            // Update progress bar
            val progressBar = binding.root.findViewById<ProgressBar>(R.id.step_progress_bar)
            progressBar?.progress = progressPercentage
            
            // Update motivational text
            val motivationalText = binding.root.findViewById<TextView>(R.id.step_motivational_text)
            motivationalText?.text = when {
                progressPercentage >= 100 -> "🎉 Goal achieved! Amazing work!"
                progressPercentage >= 75 -> "Almost there! Keep going! 💪"
                progressPercentage >= 50 -> "Great progress! You're halfway there!"
                progressPercentage >= 25 -> "Good start! Keep moving! 🚶‍♂️"
                else -> "Start walking to track your steps!"
            }
            
        } catch (e: Exception) {
            // Handle any errors and show default values
            val stepCountText = binding.root.findViewById<TextView>(R.id.step_count_text)
            stepCountText?.text = "0"
            
            val stepGoalText = binding.root.findViewById<TextView>(R.id.step_goal_text)
            stepGoalText?.text = "Goal: 10,000"
            
            val progressPercentageText = binding.root.findViewById<TextView>(R.id.step_progress_percentage)
            progressPercentageText?.text = "0%"
            
            val progressBar = binding.root.findViewById<ProgressBar>(R.id.step_progress_bar)
            progressBar?.progress = 0
            
            val motivationalText = binding.root.findViewById<TextView>(R.id.step_motivational_text)
            motivationalText?.text = "Start walking to track your steps!"
        }
    }
    
    private fun showStepCounterDetails() {
        val todayData = sensorRepository.getTodaySensorData()
        val stepGoal = sensorRepository.getStepGoal()
        
        // Use sample data if no real data exists
        val currentSteps = if (todayData.dailySteps > 0) todayData.dailySteps else 7543
        val currentGoal = if (stepGoal > 0) stepGoal else 10000
        
        val progress = if (currentGoal > 0) {
            (currentSteps.toFloat() / currentGoal.toFloat()).coerceAtMost(1.0f)
        } else 0f
        val progressPercentage = (progress * 100).toInt()
        
        val message = buildString {
            appendLine("📊 Today's Step Summary")
            appendLine()
            appendLine("Steps taken: ${String.format("%,d", currentSteps)}")
            appendLine("Daily goal: ${String.format("%,d", currentGoal)}")
            appendLine("Progress: $progressPercentage%")
            appendLine()
            if (progressPercentage >= 100) {
                appendLine("🎉 Congratulations! You've reached your daily goal!")
            } else {
                val remaining = currentGoal - currentSteps
                appendLine("Steps remaining: ${String.format("%,d", remaining)}")
            }
            appendLine()
            appendLine("💡 Tip: Enable sensor features in Settings to track steps automatically!")
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Step Counter Details")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Settings") { dialog, _ ->
                // Navigate to settings (this would need proper navigation implementation)
                dialog.dismiss()
            }
            .show()
    }
    
    private fun simulateStepIncrement() {
        try {
            // Get current step count and add 100 steps
            val todayData = sensorRepository.getTodaySensorData()
            val currentSteps = if (todayData.dailySteps > 0) todayData.dailySteps else 7543
            val newSteps = currentSteps + 100
            
            // Update the step count in repository
            sensorRepository.updateDailySteps(newSteps)
            
            // Show a quick feedback
            Snackbar.make(binding.root, "+100 steps added! 🚶‍♂️", Snackbar.LENGTH_SHORT).show()
            
            // Update display immediately
            updateStepCounterDisplay()
            
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Error updating steps", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun refreshData() {
        // Refresh all data
        viewModel.loadHabits()
        
        // Simulate refresh delay
        val swipeRefreshLayout = view?.parent as? SwipeRefreshLayout
        swipeRefreshLayout?.postDelayed({
            swipeRefreshLayout.isRefreshing = false
        }, 1000)
    }
    
    private fun observeViewModel() {
        viewModel.habits.observe(viewLifecycleOwner, Observer { habits ->
            println("DEBUG: Habits updated, count: ${habits.size}")
            habitAdapter.submitList(habits)
            updateEmptyState(habits.isEmpty())
            updateViewStats(habits)
        })
        
        viewModel.completionPercentage.observe(viewLifecycleOwner, Observer { percentage ->
            updateProgressDisplay(percentage)
        })
        
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // TODO: Show/hide loading indicator
        })
        
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })
    }
    
    private fun updateViewStats(habits: List<Habit>) {
        // Update daily view stats
        val completedCount = habits.count { viewModel.isHabitCompletedToday(it.id) }
        val totalCount = habits.size
        
        val completedCountView = binding.root.findViewById<TextView>(R.id.completed_count)
        val totalCountView = binding.root.findViewById<TextView>(R.id.total_count)
        
        completedCountView?.text = completedCount.toString()
        totalCountView?.text = totalCount.toString()
        
        // Add click listener to total count card
        val totalCountCard = binding.root.findViewById<View>(R.id.total_count_card)
        totalCountCard?.setOnClickListener {
            showHabitDetailDialog()
        }
        
        // Update weekly view stats
        val weeklyCompletionRate = if (totalCount > 0) (completedCount * 100 / totalCount) else 0
        val weeklyCompletionRateView = binding.root.findViewById<TextView>(R.id.weekly_completion_rate)
        weeklyCompletionRateView?.text = "$weeklyCompletionRate%"
        
        // Update monthly view stats
        val monthlyCompletion = if (totalCount > 0) (completedCount * 100 / totalCount) else 0
        val monthlyCompletionView = binding.root.findViewById<TextView>(R.id.monthly_completion)
        monthlyCompletionView?.text = "$monthlyCompletion%"
    }
    
    private fun updateProgressDisplay(percentage: Double) {
        val percentageInt = percentage.toInt()
        binding.completionPercentage.text = "$percentageInt%"
        
        // Update progress card background based on percentage
        updateProgressCardBackground(percentageInt)
        
        // Animate progress bar
        animateProgressBar(percentageInt)
        
        // Update motivational text based on progress
        updateMotivationalText(percentageInt)
        
        // Show celebration message when 100% completed (only once per day)
        if (percentageInt >= 100 && !hasShownCelebrationToday) {
            showCelebrationMessage()
            animateProgressCardCelebration()
            hasShownCelebrationToday = true
        } else if (percentageInt < 100) {
            // Reset celebration flag when not at 100%
            hasShownCelebrationToday = false
        }
    }
    
    private fun updateProgressCardBackground(percentage: Int) {
        val progressCard = binding.progressCard
        val backgroundRes = when {
            percentage == 0 -> R.drawable.progress_gradient_0
            percentage <= 25 -> R.drawable.progress_gradient_25
            percentage <= 50 -> R.drawable.progress_gradient_50
            percentage <= 75 -> R.drawable.progress_gradient_75
            else -> R.drawable.progress_gradient_100
        }
        
        // Animate background change
        progressCard.animate()
            .scaleX(0.98f)
            .scaleY(0.98f)
            .setDuration(150)
            .withEndAction {
                progressCard.setBackgroundResource(backgroundRes)
                progressCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
    
    private fun animateProgressBar(targetProgress: Int) {
        val progressBar = binding.progressBar
        val animator = android.animation.ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, targetProgress)
        animator.duration = 800
        animator.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        animator.start()
    }
    
    private fun updateMotivationalText(percentage: Int) {
        // Motivational text is now hidden by default
        // Only show for 100% completion
        if (percentage == 100) {
            binding.motivationalText.visibility = View.VISIBLE
            binding.motivationalText.text = "Perfect day! 🌟"
        } else {
            binding.motivationalText.visibility = View.GONE
        }
    }
    
    private fun animateProgressCardCelebration() {
        val progressCard = binding.progressCard
        val scaleX = android.animation.ObjectAnimator.ofFloat(progressCard, "scaleX", 1f, 1.1f, 1f)
        val scaleY = android.animation.ObjectAnimator.ofFloat(progressCard, "scaleY", 1f, 1.1f, 1f)
        val alpha = android.animation.ObjectAnimator.ofFloat(progressCard, "alpha", 1f, 0.8f, 1f)
        val rotation = android.animation.ObjectAnimator.ofFloat(progressCard, "rotation", 0f, 3f, -3f, 0f)
        
        scaleX.duration = 300
        scaleY.duration = 300
        alpha.duration = 300
        rotation.duration = 600
        
        scaleX.interpolator = android.view.animation.OvershootInterpolator(2f)
        scaleY.interpolator = android.view.animation.OvershootInterpolator(2f)
        alpha.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        rotation.interpolator = android.view.animation.OvershootInterpolator()
        
        val animatorSet = android.animation.AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, alpha, rotation)
        animatorSet.start()
        
        // Add confetti effect
        showConfettiEffect()
    }
    
    private fun showConfettiEffect() {
        // Simple confetti effect using multiple animated views
        // Use the activity's root view to avoid ScrollView issues
        val activity = requireActivity()
        val parent = activity.findViewById<ViewGroup>(android.R.id.content)
        val confettiColors = listOf(
            requireContext().getColor(R.color.primary),
            requireContext().getColor(R.color.secondary),
            requireContext().getColor(R.color.tertiary)
        )
        
        repeat(8) { i ->
            val confetti = View(requireContext()).apply {
                setBackgroundColor(confettiColors[i % confettiColors.size])
                layoutParams = ViewGroup.LayoutParams(8, 8)
            }
            
            parent.addView(confetti)
            
            // Animate confetti falling
            confetti.animate()
                .translationY(800f)
                .translationX((Math.random() * 400 - 200).toFloat())
                .rotation(360f)
                .alpha(0f)
                .setDuration(2000)
                .withEndAction {
                    parent.removeView(confetti)
                }
                .setStartDelay(i * 100L)
                .start()
        }
    }
    
    private fun showCelebrationMessage() {
        val celebrationMessages = listOf(
            "🎉 Amazing! You've completed all your habits today!",
            "🌟 Fantastic! Perfect day achieved!",
            "🏆 Outstanding! All habits completed!",
            "✨ Incredible! You're on fire today!",
            "🎯 Perfect! Mission accomplished!"
        )
        
        val randomMessage = celebrationMessages.random()
        val snackbar = Snackbar.make(binding.root, randomMessage, Snackbar.LENGTH_LONG)
        
        // Customize the snackbar appearance
        snackbar.setBackgroundTint(requireContext().getColor(R.color.primary))
        snackbar.setTextColor(requireContext().getColor(R.color.on_primary))
        
        // Auto-dismiss after 3 seconds
        snackbar.duration = 3000
        snackbar.show()
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        // Don't hide habits list here since it's now always visible
        // The empty state will show over the habits list when needed
    }
    
    fun showEditHabitDialog(habit: Habit? = null) {
        println("DEBUG: Opening habit dialog")
        val dialog = EditHabitDialog.newInstance(habit)
        dialog.setOnSaveListener { title, targetPerDay, category ->
            println("DEBUG: Saving habit: $title")
            if (habit == null) {
                viewModel.addHabit(title, targetPerDay, category)
            } else {
                val updatedHabit = habit.copy(title = title, targetPerDay = targetPerDay, category = category)
                viewModel.updateHabit(updatedHabit)
            }
        }
        dialog.show(parentFragmentManager, "EditHabitDialog")
    }
    
    fun showDeleteConfirmation(habit: Habit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"${habit.title}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteHabit(habit.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showHabitDetailDialog() {
        val dialog = HabitDetailDialog.newInstance()
        dialog.setListener(this)
        dialog.show(parentFragmentManager, "HabitDetailDialog")
    }
    
    override fun onEditHabit(habit: Habit) {
        showEditHabitDialog(habit)
    }
    
    override fun onDeleteHabit(habit: Habit) {
        showDeleteConfirmation(habit)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // Cancel step counter job
        stepCounterJob?.cancel()
        
        _binding = null
    }
}
