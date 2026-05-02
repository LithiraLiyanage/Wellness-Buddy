package com.example.wellnessapp.ui.mood

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wellnessapp.R
import com.example.wellnessapp.charts.ChartUtils
import com.example.wellnessapp.databinding.FragmentMoodBinding
import com.example.wellnessapp.model.MoodEntry
import com.example.wellnessapp.model.MoodCategory
import com.example.wellnessapp.util.DateUtils
import com.example.wellnessapp.util.EmojiUtils
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment for mood tracking and visualization.
 * Displays mood entries, trend chart, and allows adding new entries.
 */
class MoodFragment : Fragment() {
    
    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MoodViewModel by viewModels()
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var notesAdapter: NotesAdapter
    private var selectedEmoji: String = EmojiUtils.DEFAULT_EMOJI
    private var moodIntensity: Float = 5f
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupEmojiSelection()
        setupMoodIntensitySlider()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(
            onDeleteClick = { moodEntry -> showDeleteConfirmation(moodEntry) },
            onEditClick = { moodEntry -> showEditDialog(moodEntry) }
        )
        
        notesAdapter = NotesAdapter()
        
        // Notes RecyclerView (this one can stay as RecyclerView since it's small)
        binding.notesRecyclerView.apply {
            adapter = notesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupEmojiSelection() {
        val emojisContainer = binding.moodEmojisContainer
        emojisContainer.removeAllViews()
        
        // All emojis in one horizontal row
        val allEmojis = listOf(
            "😊", "😄", "😃", "😍", "🤩", "😎", "😜", "🤪", // Happy & Excited
            "😌", "😴", "🤤", "😪", "🧘", "😌", "😌", "😌", // Calm
            "🙂", "😐", "😶", "😑", "😏", "🙃", "😐", "😐", // Neutral
            "😢", "😭", "😔", "😞", "😟", "😕", "😢", "😢", // Sad
            "😠", "😡", "🤬", "😤", "😾", "😠", "😠", "😠"  // Angry
        )
        
        allEmojis.forEach { emoji ->
            val button = MaterialButton(requireContext()).apply {
                text = emoji
                textSize = 24f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 8, 0)
                }
                background = resources.getDrawable(R.drawable.emoji_button_background, null)
                setOnClickListener { 
                    selectedEmoji = emoji
                    updateEmojiSelection()
                }
            }
            emojisContainer.addView(button)
        }
        
        updateEmojiSelection()
    }
    
    private fun setupMoodIntensitySlider() {
        binding.moodIntensitySlider.addOnChangeListener { _, value, _ ->
            moodIntensity = value
        }
    }
    
    private fun updateEmojiSelection() {
        // Update visual selection state for all emoji buttons
        for (i in 0 until binding.moodEmojisContainer.childCount) {
            val button = binding.moodEmojisContainer.getChildAt(i) as MaterialButton
            button.isSelected = button.text.toString() == selectedEmoji
        }
    }
    
    private fun setupClickListeners() {
        binding.addMoodButton.setOnClickListener {
            addMoodEntry()
        }
        
        binding.addNoteButton.setOnClickListener {
            showAddNoteDialog()
        }
        
        binding.analyticsButton.setOnClickListener {
            navigateToAnalytics()
        }
        
        binding.filterDateButton.setOnClickListener {
            showDatePicker()
        }
        
        binding.shareWeeklySummaryButton.setOnClickListener {
            shareWeeklySummary()
        }
    }
    
    private fun observeViewModel() {
        viewModel.moodEntries.observe(viewLifecycleOwner, Observer { entries ->
            updateMoodEntriesList(entries)
            updateEmptyState(entries.isEmpty())
            
            // Filter notes (entries with 📝 emoji or entries with notes)
            val notes = entries.filter { it.emoji == "📝" || !it.note.isNullOrBlank() }
                .sortedByDescending { it.timestamp }
                .take(5) // Show only last 5 notes
            notesAdapter.submitList(notes)
            
            // Show/hide no notes text
            binding.noNotesText.visibility = if (notes.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        })
        
        viewModel.dailyAverages.observe(viewLifecycleOwner, Observer { averages ->
            ChartUtils.setupMoodTrendChart(binding.moodChart, averages, requireContext())
        })
        
        viewModel.selectedDate.observe(viewLifecycleOwner, Observer { date ->
            updateFilterButton(date)
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
    
    private fun updateMoodEntriesList(entries: List<MoodEntry>) {
        binding.moodEntriesContainer.removeAllViews()
        
        entries.forEach { entry ->
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_mood_entry, binding.moodEntriesContainer, false)
            
            val moodEmoji = view.findViewById<TextView>(R.id.mood_emoji)
            val moodDateTime = view.findViewById<TextView>(R.id.mood_date_time)
            val moodNote = view.findViewById<TextView>(R.id.mood_note)
            val deleteButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.delete_button)
            val editButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.edit_button)
            
            moodEmoji.text = entry.emoji
            val dateTime = "${DateUtils.formatDisplayDate(entry.timestamp)} at ${DateUtils.formatDisplayTime(entry.timestamp)}"
            moodDateTime.text = dateTime
            
            if (entry.note.isNullOrBlank()) {
                moodNote.visibility = android.view.View.GONE
            } else {
                moodNote.text = entry.note
                moodNote.visibility = android.view.View.VISIBLE
            }
            
            deleteButton.setOnClickListener { showDeleteConfirmation(entry) }
            editButton.setOnClickListener { showEditDialog(entry) }
            
            binding.moodEntriesContainer.addView(view)
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.moodEntriesContainer.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun updateFilterButton(selectedDate: String?) {
        if (selectedDate != null) {
            val displayDate = DateUtils.parseDate(selectedDate)?.let { 
                DateUtils.formatDisplayDate(it.time) 
            } ?: selectedDate
            binding.filterDateButton.text = "Filter: $displayDate"
        } else {
            binding.filterDateButton.text = getString(R.string.filter_by_date)
        }
    }
    
    private fun addMoodEntry() {
        if (selectedEmoji.isNotEmpty()) {
            viewModel.addMoodEntry(selectedEmoji, null, moodIntensity)
            
            // Reset form
            selectedEmoji = ""
            binding.moodIntensitySlider.value = 5f
            moodIntensity = 5f
            updateEmojiSelection()
        } else {
            // Show error message
            Toast.makeText(requireContext(), "Please select a mood", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_note, null)
        
        val noteInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.note_input)
        
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val note = noteInput.text.toString().trim()
                if (note.isNotEmpty()) {
                    viewModel.addNote(note)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDatePicker() {
        val dialog = CalendarDialog.newInstance()
        dialog.setOnDateSelectedListener { dateString ->
            if (dateString == DateUtils.getTodayString()) {
                viewModel.clearDateFilter()
            } else {
                viewModel.filterByDate(dateString)
            }
        }
        dialog.show(parentFragmentManager, "CalendarDialog")
    }
    
    private fun navigateToAnalytics() {
        findNavController().navigate(R.id.action_moodFragment_to_moodAnalyticsFragment)
    }
    
    private fun shareWeeklySummary() {
        val summary = viewModel.generateWeeklySummary()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
            putExtra(Intent.EXTRA_SUBJECT, "My Weekly Mood Summary")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Weekly Summary"))
    }
    
    private fun showEditDialog(moodEntry: MoodEntry) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_mood, null)
        
        val editNoteInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_note_input)
        editNoteInput.setText(moodEntry.note ?: "")
        
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newNote = editNoteInput.text.toString().trim()
                viewModel.updateMoodEntry(moodEntry.id, newNote)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteConfirmation(moodEntry: MoodEntry) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMoodEntry(moodEntry.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
