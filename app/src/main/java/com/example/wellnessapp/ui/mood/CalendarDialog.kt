package com.example.wellnessapp.ui.mood

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.wellnessapp.util.DateUtils
import java.util.*

/**
 * Dialog for selecting a date to filter mood entries.
 */
class CalendarDialog : DialogFragment() {
    
    private var onDateSelectedListener: ((String?) -> Unit)? = null
    
    companion object {
        fun newInstance(): CalendarDialog {
            return CalendarDialog()
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        return DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                val dateString = DateUtils.formatDate(selectedCalendar.timeInMillis)
                onDateSelectedListener?.invoke(dateString)
            },
            year,
            month,
            day
        )
    }
    
    fun setOnDateSelectedListener(listener: (String?) -> Unit) {
        onDateSelectedListener = listener
    }
}
