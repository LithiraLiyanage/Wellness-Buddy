package com.example.wellnessapp.ui.mood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.databinding.ItemNoteBinding
import com.example.wellnessapp.model.MoodEntry
import com.example.wellnessapp.util.DateUtils

/**
 * Adapter for displaying notes in a RecyclerView.
 */
class NotesAdapter : ListAdapter<MoodEntry, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(moodEntry: MoodEntry) {
            binding.apply {
                noteText.text = moodEntry.note
                noteDate.text = DateUtils.formatDisplayTime(moodEntry.timestamp)
            }
        }
    }
    
    private class NoteDiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
        override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem == newItem
        }
    }
}
