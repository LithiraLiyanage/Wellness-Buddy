package com.example.wellnessapp.ui.habits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for horizontal scrollable calendar in habits fragment
 */
class CalendarAdapter(
    private val onDateSelected: (Date) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val dates = mutableListOf<Date>()
    private var selectedPosition = -1
    private val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("d", Locale.getDefault())

    init {
        generateDates()
    }

    private fun generateDates() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -7) // Start from 7 days ago
        
        for (i in 0..14) { // Show 15 days total
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Set today as selected by default
        val today = Calendar.getInstance()
        selectedPosition = dates.indexOfFirst { 
            val cal = Calendar.getInstance()
            cal.time = it
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_day_item, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(dates[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = dates.size

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayOfWeek: TextView = itemView.findViewById(R.id.day_of_week)
        private val dayNumber: TextView = itemView.findViewById(R.id.day_number)
        private val completionIndicator: View = itemView.findViewById(R.id.completion_indicator)

        fun bind(date: Date, isSelected: Boolean) {
            dayOfWeek.text = dateFormat.format(date)
            dayNumber.text = dayFormat.format(date)

            // Check if this is today
            val today = Calendar.getInstance()
            val cal = Calendar.getInstance()
            cal.time = date
            val isToday = cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                         cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            
            // Update background based on state
            updateBackground(isSelected, isToday)

            // Show completion indicator (placeholder for now)
            completionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = adapterPosition
                
                if (previousSelected != -1) {
                    notifyItemChanged(previousSelected)
                }
                notifyItemChanged(selectedPosition)
                
                onDateSelected(date)
            }
        }
        
        private fun updateBackground(isSelected: Boolean, isToday: Boolean) {
            val context = itemView.context
            val background = when {
                isSelected -> context.getDrawable(R.drawable.calendar_day_selected)
                isToday -> context.getDrawable(R.drawable.calendar_day_today)
                else -> context.getDrawable(R.drawable.calendar_day_default)
            }
            itemView.background = background
        }
    }
}
