package com.example.wellnessapp.ui.habits

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wellnessapp.R
import com.example.wellnessapp.databinding.HabitDetailViewBinding
import com.example.wellnessapp.model.Habit
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Dialog showing detailed view of all habits with edit/delete options
 */
class HabitDetailDialog : DialogFragment() {
    
    interface HabitDetailListener {
        fun onEditHabit(habit: Habit)
        fun onDeleteHabit(habit: Habit)
    }
    
    private var listener: HabitDetailListener? = null
    
    private var _binding: HabitDetailViewBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HabitsViewModel by activityViewModels()
    private lateinit var habitAdapter: HabitAdapter
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = HabitDetailViewBinding.inflate(layoutInflater)
        
        // Create dialog with custom view
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onEditClick = { habit -> 
                // Close this dialog and open edit dialog
                dismiss()
                listener?.onEditHabit(habit)
            },
            onDeleteClick = { habit -> 
                // Close this dialog and show delete confirmation
                dismiss()
                listener?.onDeleteHabit(habit)
            },
            onToggleCompletion = { habit -> viewModel.toggleHabitCompletion(habit.id) },
            onIncrementCompletion = { habit -> viewModel.incrementHabitCompletion(habit.id) },
            onDecrementCompletion = { habit -> viewModel.decrementHabitCompletion(habit.id) },
            getCompletionCount = { habitId -> viewModel.getTodayCompletionCount(habitId) },
            isCompleted = { habitId -> viewModel.isHabitCompletedToday(habitId) }
        )
        
        binding.detailHabitsRecyclerView.apply {
            adapter = habitAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        
        // Attach swipe functionality
        habitAdapter.attachSwipeToRecyclerView(binding.detailHabitsRecyclerView)
    }
    
    private fun setupClickListeners() {
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }
    
    private fun observeViewModel() {
        viewModel.habits.observe(viewLifecycleOwner, Observer { habits ->
            habitAdapter.submitList(habits)
            updateStats(habits)
            updateEmptyState(habits.isEmpty())
        })
    }
    
    private fun updateStats(habits: List<Habit>) {
        val completedCount = habits.count { viewModel.isHabitCompletedToday(it.id) }
        val totalCount = habits.size
        
        binding.detailTotalCount.text = totalCount.toString()
        binding.detailCompletedCount.text = completedCount.toString()
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.detailEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.detailHabitsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    fun setListener(listener: HabitDetailListener) {
        this.listener = listener
    }
    
    companion object {
        fun newInstance(): HabitDetailDialog {
            return HabitDetailDialog()
        }
    }
}
