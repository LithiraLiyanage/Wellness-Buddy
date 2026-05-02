package com.example.wellnessapp.ui.habits

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.wellnessapp.R
import com.example.wellnessapp.databinding.DialogEditHabitBinding
import com.example.wellnessapp.model.Habit
import com.example.wellnessapp.model.HabitCategory
import com.example.wellnessapp.util.GradientUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Enhanced dialog for adding or editing habits with category selection.
 * Supports both boolean and countable habits with visual category preview.
 */
class EditHabitDialog : DialogFragment() {
    
    private var _binding: DialogEditHabitBinding? = null
    private val binding get() = _binding!!
    
    private var habit: Habit? = null
    private var onSaveListener: ((String, Int?, HabitCategory) -> Unit)? = null
    private var selectedCategory: HabitCategory = HabitCategory.OTHER
    
    companion object {
        private const val ARG_HABIT_TITLE = "habit_title"
        private const val ARG_HABIT_TARGET = "habit_target"
        private const val ARG_HABIT_CATEGORY = "habit_category"
        
        fun newInstance(habit: Habit? = null): EditHabitDialog {
            val dialog = EditHabitDialog()
            if (habit != null) {
                val args = Bundle()
                args.putString(ARG_HABIT_TITLE, habit.title)
                args.putInt(ARG_HABIT_TARGET, habit.targetPerDay ?: -1)
                args.putString(ARG_HABIT_CATEGORY, habit.category.name)
                dialog.arguments = args
            }
            return dialog
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            val title = args.getString(ARG_HABIT_TITLE)
            val target = args.getInt(ARG_HABIT_TARGET, -1)
            val categoryName = args.getString(ARG_HABIT_CATEGORY)
            
            if (title != null) {
                val category = try {
                    HabitCategory.valueOf(categoryName ?: HabitCategory.OTHER.name)
                } catch (e: IllegalArgumentException) {
                    HabitCategory.OTHER
                }
                
                habit = Habit(
                    id = "",
                    title = title,
                    targetPerDay = if (target == -1) null else target,
                    sortOrder = 0,
                    category = category
                )
                selectedCategory = category
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditHabitBinding.inflate(layoutInflater)
        
        setupUI()
        populateFields()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }
    
    private fun setupUI() {
        // Set up habit type selection
        val habitTypes = listOf("Simple (Yes/No)", "Countable (with target)")
        val habitTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, habitTypes)
        habitTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.habitTypeSpinner.setAdapter(habitTypeAdapter)
        
        // Set up category selection
        val categories = HabitCategory.getAllCategories().map { it.displayName }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.setAdapter(categoryAdapter)
        
        // Show/hide target input based on habit type selection
        binding.habitTypeSpinner.setOnItemClickListener { _, _, position, _ ->
            val isCountable = position == 1
            binding.targetInputLayout.visibility = if (isCountable) View.VISIBLE else View.GONE
        }
        
        // Update category preview when category is selected
        binding.categorySpinner.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = HabitCategory.getAllCategories()[position]
            updateCategoryPreview()
        }
        
        // Set up button click listeners
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        
        binding.saveButton.setOnClickListener {
            saveHabit()
        }
    }
    
    private fun populateFields() {
        habit?.let { existingHabit ->
            binding.habitTitleInput.setText(existingHabit.title)
            
            if (existingHabit.isCountableHabit()) {
                binding.habitTypeSpinner.setText("Countable (with target)", false)
                binding.targetInput.setText(existingHabit.targetPerDay?.toString() ?: "1")
                binding.targetInputLayout.visibility = View.VISIBLE
            } else {
                binding.habitTypeSpinner.setText("Simple (Yes/No)", false)
            }
            
            // Set category
            val categoryIndex = HabitCategory.getAllCategories().indexOf(existingHabit.category)
            if (categoryIndex >= 0) {
                binding.categorySpinner.setText(existingHabit.category.displayName, false)
                selectedCategory = existingHabit.category
                updateCategoryPreview()
            }
        }
    }
    
    private fun updateCategoryPreview() {
        binding.apply {
            // Update category icon
            val iconDrawable = GradientUtils.getIconDrawable(selectedCategory)
            categoryPreviewIcon.setImageResource(iconDrawable)
            
            // Update category text
            categoryPreviewText.text = selectedCategory.displayName
            
            // Show preview
            categoryPreview.visibility = View.VISIBLE
        }
    }
    
    private fun saveHabit() {
        val title = binding.habitTitleInput.text.toString().trim()
        
        if (TextUtils.isEmpty(title)) {
            binding.habitTitleInputLayout.error = "Habit title is required"
            return
        }
        
        // Auto-detect category if not manually selected
        val finalCategory = if (selectedCategory == HabitCategory.OTHER) {
            HabitCategory.detectCategory(title)
        } else {
            selectedCategory
        }
        
        val isCountable = binding.habitTypeSpinner.text.toString().contains("Countable")
        val targetPerDay = if (isCountable) {
            val targetText = binding.targetInput.text.toString().trim()
            if (TextUtils.isEmpty(targetText)) {
                binding.targetInputLayout.error = "Target is required for countable habits"
                return
            }
            try {
                val target = targetText.toInt()
                if (target <= 0) {
                    binding.targetInputLayout.error = "Target must be greater than 0"
                    return
                }
                target
            } catch (e: NumberFormatException) {
                binding.targetInputLayout.error = "Please enter a valid number"
                return
            }
        } else {
            null
        }
        
        onSaveListener?.invoke(title, targetPerDay, finalCategory)
        dismiss()
    }
    
    fun setOnSaveListener(listener: (String, Int?, HabitCategory) -> Unit) {
        onSaveListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}