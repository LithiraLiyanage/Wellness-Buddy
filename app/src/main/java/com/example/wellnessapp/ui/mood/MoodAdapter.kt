package com.example.wellnessapp.ui.mood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.databinding.ItemMoodEntryBinding
import com.example.wellnessapp.model.MoodEntry
import com.example.wellnessapp.util.DateUtils

/**
 * Adapter for displaying mood entries in a RecyclerView.
 */
class MoodAdapter(
    private val onDeleteClick: (MoodEntry) -> Unit,
    private val onEditClick: (MoodEntry) -> Unit
) : ListAdapter<MoodEntry, MoodAdapter.MoodViewHolder>(MoodDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class MoodViewHolder(
        private val binding: ItemMoodEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(moodEntry: MoodEntry) {
            binding.apply {
                moodEmoji.text = moodEntry.emoji
                
                val dateTime = "${DateUtils.formatDisplayDate(moodEntry.timestamp)} at ${DateUtils.formatDisplayTime(moodEntry.timestamp)}"
                moodDateTime.text = dateTime
                
                if (moodEntry.note.isNullOrBlank()) {
                    moodNote.visibility = android.view.View.GONE
                } else {
                    moodNote.text = moodEntry.note
                    moodNote.visibility = android.view.View.VISIBLE
                }
                
                deleteButton.setOnClickListener { onDeleteClick(moodEntry) }
                editButton.setOnClickListener { onEditClick(moodEntry) }
            }
        }
    }
    
    private class MoodDiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
        override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem == newItem
        }
    }
}
