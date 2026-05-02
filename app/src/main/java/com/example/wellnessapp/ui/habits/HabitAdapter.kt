package com.example.wellnessapp.ui.habits

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.databinding.ItemHabitBinding
import com.example.wellnessapp.model.Habit
import com.example.wellnessapp.util.GradientUtils

/**
 * Enhanced adapter for displaying habits in a RecyclerView with modern visual enhancements.
 * Features gradient cards, category icons, progress rings, and smooth animations.
 */
class HabitAdapter(
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onToggleCompletion: (Habit) -> Unit,
    private val onIncrementCompletion: (Habit) -> Unit,
    private val onDecrementCompletion: (Habit) -> Unit,
    private val getCompletionCount: (String) -> Int,
    private val isCompleted: (String) -> Boolean
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
        
        // Add staggered animation for habit cards
        holder.itemView.alpha = 0f
        holder.itemView.translationY = 30f
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(position * 100L)
            .setInterpolator(android.view.animation.OvershootInterpolator(1.1f))
            .start()
    }
    
    inner class HabitViewHolder(
        private val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(habit: Habit) {
            binding.apply {
                // Set habit title and description
                habitTitle.text = habit.title
                
                // Apply category-based theming
                applyCategoryTheming(habit)
                
                // Configure UI based on habit type
                if (habit.isCountableHabit()) {
                    setupCountableHabit(habit)
                } else {
                    setupBooleanHabit(habit)
                }
                
                // Set up click listeners with animations
                setupClickListeners(habit)
                
                // Add card lift animation on touch
                setupCardAnimations()
                
                // Update completion state
                val completionCount = getCompletionCount(habit.id)
                val isCompletedToday = isCompleted(habit.id)
                updateCompletionState(habit, completionCount, isCompletedToday)
            }
        }
        
        /**
         * Applies category-based theming including gradient background and icon
         */
        private fun applyCategoryTheming(habit: Habit) {
            val context = binding.root.context
            
            // Set gradient background
            val gradientDrawable = GradientUtils.getGradientDrawable(habit.category)
            binding.habitCard.setBackgroundResource(gradientDrawable)
            
            // Set category icon
            val iconDrawable = GradientUtils.getIconDrawable(habit.category)
            binding.categoryIcon.setImageResource(iconDrawable)
            
            // Set habit description with target
            if (habit.isCountableHabit()) {
                val target = habit.targetPerDay ?: 1
                binding.habitDescription.text = "Target: $target per day"
            } else {
                binding.habitDescription.text = "Complete this habit daily"
            }
        }
        
        /**
         * Sets up UI for countable habits with progress indicators
         */
        private fun setupCountableHabit(habit: Habit) {
            binding.apply {
                // Show circular progress and hide checkbox
                circularProgress.visibility = View.VISIBLE
                completionCheckbox.visibility = View.GONE
                
                // Show linear progress bar
                linearProgress.visibility = View.VISIBLE
                
                // Show completion count controls
                completionCountLayout.visibility = View.VISIBLE
                
                // Show action buttons for countable habits too
                actionButtons.visibility = View.VISIBLE
            }
        }
        
        /**
         * Sets up UI for boolean habits with checkbox
         */
        private fun setupBooleanHabit(habit: Habit) {
            binding.apply {
                // Show checkbox and hide progress indicators
                completionCheckbox.visibility = View.VISIBLE
                circularProgress.visibility = View.GONE
                linearProgress.visibility = View.GONE
                completionCountLayout.visibility = View.GONE
                progressText.visibility = View.GONE
                
                // Show action buttons
                actionButtons.visibility = View.VISIBLE
            }
        }
        
        /**
         * Sets up click listeners with smooth animations
         */
        private fun setupClickListeners(habit: Habit) {
            binding.apply {
                editButton.setOnClickListener { 
                    animateButtonClick(editButton) {
                        onEditClick(habit)
                    }
                }
                
                deleteButton.setOnClickListener { 
                    animateButtonClick(deleteButton) {
                        onDeleteClick(habit)
                    }
                }
                
                completionCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        animateCompletionCelebration()
                    }
                    onToggleCompletion(habit)
                }
                
                incrementButton.setOnClickListener { 
                    animateButtonClick(incrementButton) {
                        onIncrementCompletion(habit)
                    }
                }
                
                decrementButton.setOnClickListener { 
                    animateButtonClick(decrementButton) {
                        onDecrementCompletion(habit)
                    }
                }
            }
        }
        
        /**
         * Sets up card lift animations on touch
         */
        private fun setupCardAnimations() {
            binding.habitCard.setOnTouchListener { _, _ ->
                // Card lift animation will be handled by the card's stateListAnimator
                false
            }
        }
        
        /**
         * Animates button clicks with a subtle scale effect
         */
        private fun animateButtonClick(button: View, action: () -> Unit) {
            val scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f)
            val scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f)
            val scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f)
            val scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f)
            
            scaleDown.duration = 100
            scaleUp.duration = 100
            scaleDownY.duration = 100
            scaleUpY.duration = 100
            
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleDown, scaleDownY)
            animatorSet.play(scaleUp).after(scaleDown)
            animatorSet.play(scaleUpY).after(scaleDownY)
            
            animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    action()
                }
            })
            
            animatorSet.start()
        }
        
        /**
         * Animates completion celebration with overshoot effect
         */
        private fun animateCompletionCelebration() {
            val scaleX = ObjectAnimator.ofFloat(binding.habitCard, "scaleX", 1f, 1.1f, 1f)
            val scaleY = ObjectAnimator.ofFloat(binding.habitCard, "scaleY", 1f, 1.1f, 1f)
            val alpha = ObjectAnimator.ofFloat(binding.habitCard, "alpha", 1f, 0.8f, 1f)
            
            scaleX.duration = 300
            scaleY.duration = 300
            alpha.duration = 300
            
            scaleX.interpolator = OvershootInterpolator(1.5f)
            scaleY.interpolator = OvershootInterpolator(1.5f)
            alpha.interpolator = AccelerateDecelerateInterpolator()
            
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleX, scaleY, alpha)
            animatorSet.start()
        }
        
        /**
         * Updates the completion state for a habit with smooth progress animation
         */
        fun updateCompletionState(habit: Habit, completionCount: Int, isCompleted: Boolean) {
            binding.apply {
                if (habit.isCountableHabit()) {
                    // Update completion count
                    binding.completionCount.text = completionCount.toString()
                    
                    // Calculate progress
                    val target = habit.targetPerDay ?: 1
                    val progress = ((completionCount.toFloat() / target) * 100).toInt()
                    val isFullyCompleted = completionCount >= target
                    
                    // Update progress text
                    progressText.text = "$completionCount/$target"
                    
                    if (isFullyCompleted) {
                        // Hide progress indicators when completed
                        circularProgress.visibility = View.GONE
                        linearProgress.visibility = View.GONE
                        progressText.visibility = View.GONE
                        
                        // Update title to show completion
                        habitTitle.text = "✅ ${habit.title}"
                    } else {
                        // Show progress indicators when not completed
                        circularProgress.visibility = View.VISIBLE
                        linearProgress.visibility = View.VISIBLE
                        progressText.visibility = View.VISIBLE
                        
                        // Reset title if it was marked as completed
                        habitTitle.text = habit.title
                        
                        // Animate progress
                        animateProgress(circularProgress, progress)
                        animateProgress(linearProgress, progress)
                    }
                    
                } else {
                    // Update checkbox state
                    completionCheckbox.isChecked = isCompleted
                    
                    if (isCompleted) {
                        // Update title to show completion
                        habitTitle.text = "✅ ${habit.title}"
                    } else {
                        habitTitle.text = habit.title
                    }
                }
            }
        }
        
        /**
         * Animates progress bar changes smoothly
         */
        private fun animateProgress(progressBar: android.widget.ProgressBar, targetProgress: Int) {
            val currentProgress = progressBar.progress
            val animator = ObjectAnimator.ofInt(progressBar, "progress", currentProgress, targetProgress)
            animator.duration = 500
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.start()
        }
    }
    
    /**
     * Updates the completion state for a specific habit
     */
    fun updateHabitCompletion(habitId: String, completionCount: Int, isCompleted: Boolean) {
        val position = currentList.indexOfFirst { it.id == habitId }
        if (position != -1) {
            val habit = currentList[position]
            notifyItemChanged(position)
        }
    }
    
    /**
     * Attaches swipe functionality to the RecyclerView
     */
    fun attachSwipeToRecyclerView(recyclerView: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val habit = currentList[position]
                    when (direction) {
                        ItemTouchHelper.LEFT -> onEditClick(habit)
                        ItemTouchHelper.RIGHT -> onDeleteClick(habit)
                    }
                    // Notify that the item was swiped (this will restore the view)
                    notifyItemChanged(position)
                }
            }
            
            override fun onChildDraw(c: android.graphics.Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.height
                val itemWidth = itemView.width
                
                // Draw background based on swipe direction
                val background = android.graphics.Paint()
                val icon = android.graphics.Paint()
                icon.color = android.graphics.Color.WHITE
                icon.textSize = 48f
                icon.textAlign = android.graphics.Paint.Align.CENTER
                
                if (dX > 0) {
                    // Swiping right - show delete action
                    background.color = android.graphics.Color.parseColor("#FF5722")
                    c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat(), background)
                    
                    // Draw delete icon
                    val iconX = itemView.left + (dX / 2)
                    val iconY = itemView.top + (itemHeight / 2) + 16f
                    c.drawText("🗑", iconX, iconY, icon)
                } else if (dX < 0) {
                    // Swiping left - show edit action
                    background.color = android.graphics.Color.parseColor("#2196F3")
                    c.drawRect(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), background)
                    
                    // Draw edit icon
                    val iconX = itemView.right + (dX / 2)
                    val iconY = itemView.top + (itemHeight / 2) + 16f
                    c.drawText("✏️", iconX, iconY, icon)
                }
                
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    
    private class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}