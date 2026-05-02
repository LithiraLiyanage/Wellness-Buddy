package com.example.wellnessapp.charts

import android.content.Context
import android.graphics.Color
import com.example.wellnessapp.R
import com.example.wellnessapp.util.DateUtils
import com.example.wellnessapp.util.EmojiUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * Utility class for setting up and configuring MPAndroidChart components.
 */
object ChartUtils {
    
    /**
     * Sets up a mood trend line chart with the last 7 days of data
     */
    fun setupMoodTrendChart(
        chart: LineChart,
        dailyAverages: Map<String, Double>,
        context: Context
    ) {
        // Clear any existing data
        chart.clear()
        
        // Configure chart appearance
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.setTouchEnabled(true)
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.setDoubleTapToZoomEnabled(false)
        
        // Disable legend
        chart.legend.isEnabled = false
        
        // Disable description
        chart.description.isEnabled = false
        
        // Configure X-axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.labelCount = 7
        xAxis.textColor = context.getColor(R.color.on_surface)
        xAxis.textSize = 12f
        
        // Configure Y-axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawAxisLine(true)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 5f
        leftAxis.granularity = 1f
        leftAxis.textColor = context.getColor(R.color.on_surface)
        leftAxis.textSize = 12f
        
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
        
        // Prepare data
        val entries = mutableListOf<Entry>()
        val last7Days = DateUtils.getLastNDays(7)
        val labels = mutableListOf<String>()
        
        last7Days.forEachIndexed { index, dateString ->
            val average = dailyAverages[dateString] ?: 0.0
            if (average > 0) {
                entries.add(Entry(index.toFloat(), average.toFloat()))
            }
            labels.add(formatDateLabel(dateString))
        }
        
        // Set up data set
        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Mood Trend").apply {
                color = context.getColor(R.color.primary)
                setCircleColor(context.getColor(R.color.primary))
                lineWidth = 3f
                circleRadius = 6f
                setDrawCircleHole(true)
                circleHoleColor = Color.WHITE
                setDrawValues(false)
                setDrawFilled(true)
                fillColor = context.getColor(R.color.primary)
                fillAlpha = 30
                setDrawHorizontalHighlightIndicator(false)
                setDrawVerticalHighlightIndicator(true)
                highlightLineWidth = 2f
            }
            
            val lineData = LineData(dataSet)
            chart.data = lineData
            
            // Set X-axis labels
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < labels.size) {
                        labels[index]
                    } else {
                        ""
                    }
                }
            }
            
            // Set Y-axis labels
            leftAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when (value.toInt()) {
                        1 -> "😢"
                        2 -> "😔"
                        3 -> "😐"
                        4 -> "🙂"
                        5 -> "😀"
                        else -> ""
                    }
                }
            }
            
        } else {
            // Show empty state
            chart.data = null
            chart.setNoDataText("No mood data available")
            chart.setNoDataTextColor(context.getColor(R.color.on_surface_variant))
        }
        
        // Refresh chart
        chart.invalidate()
    }
    
    /**
     * Sets up a mood trends line chart for analytics
     */
    fun setupMoodTrendsChart(
        chart: LineChart,
        trends: List<Pair<String, Float>>,
        context: Context
    ) {
        // Clear any existing data
        chart.clear()
        
        // Configure chart appearance
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.setTouchEnabled(true)
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.setDoubleTapToZoomEnabled(false)
        
        // Disable legend and description
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        
        // Configure X-axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.textColor = context.getColor(R.color.on_surface)
        xAxis.textSize = 12f
        
        // Configure Y-axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawAxisLine(true)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.granularity = 2f
        leftAxis.textColor = context.getColor(R.color.on_surface)
        leftAxis.textSize = 12f
        
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
        
        // Prepare data
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        trends.forEachIndexed { index, trend ->
            entries.add(Entry(index.toFloat(), trend.second))
            labels.add(trend.first)
        }
        
        // Set up data set
        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Mood Trends").apply {
                color = context.getColor(R.color.primary)
                setCircleColor(context.getColor(R.color.primary))
                lineWidth = 3f
                circleRadius = 6f
                setDrawCircleHole(true)
                circleHoleColor = Color.WHITE
                setDrawValues(false)
                setDrawFilled(true)
                fillColor = context.getColor(R.color.primary)
                fillAlpha = 30
                setDrawHorizontalHighlightIndicator(false)
                setDrawVerticalHighlightIndicator(true)
                highlightLineWidth = 2f
            }
            
            val lineData = LineData(dataSet)
            chart.data = lineData
            
            // Set X-axis labels
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < labels.size) {
                        labels[index]
                    } else {
                        ""
                    }
                }
            }
            
            // Set Y-axis labels
            leftAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when (value.toInt()) {
                        2 -> "😢"
                        4 -> "😐"
                        6 -> "🙂"
                        8 -> "😊"
                        10 -> "🤩"
                        else -> ""
                    }
                }
            }
            
        } else {
            chart.data = null
            chart.setNoDataText("No mood trends data available")
            chart.setNoDataTextColor(context.getColor(R.color.on_surface_variant))
        }
        
        // Refresh chart
        chart.invalidate()
    }
    
    /**
     * Formats a date string for chart display (e.g., "Jan 15" from "2024-01-15")
     */
    private fun formatDateLabel(dateString: String): String {
        val date = DateUtils.parseDate(dateString)
        return if (date != null) {
            val formatter = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            formatter.format(date)
        } else {
            dateString
        }
    }
    
    /**
     * Sets up a simple completion percentage chart
     */
    fun setupCompletionChart(
        chart: LineChart,
        completionData: Map<String, Double>,
        context: Context
    ) {
        // Clear any existing data
        chart.clear()
        
        // Configure chart appearance
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.setTouchEnabled(true)
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.setDoubleTapToZoomEnabled(false)
        
        // Disable legend and description
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        
        // Configure X-axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.textColor = context.getColor(R.color.on_surface)
        xAxis.textSize = 12f
        
        // Configure Y-axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawAxisLine(true)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f
        leftAxis.granularity = 20f
        leftAxis.textColor = context.getColor(R.color.on_surface)
        leftAxis.textSize = 12f
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        }
        
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
        
        // Prepare data
        val entries = mutableListOf<Entry>()
        val last7Days = DateUtils.getLastNDays(7)
        val labels = mutableListOf<String>()
        
        last7Days.forEachIndexed { index, dateString ->
            val percentage = completionData[dateString] ?: 0.0
            entries.add(Entry(index.toFloat(), percentage.toFloat()))
            labels.add(formatDateLabel(dateString))
        }
        
        // Set up data set
        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Completion %").apply {
                color = context.getColor(R.color.secondary)
                setCircleColor(context.getColor(R.color.secondary))
                lineWidth = 3f
                circleRadius = 6f
                setDrawCircleHole(true)
                circleHoleColor = Color.WHITE
                setDrawValues(false)
                setDrawFilled(true)
                fillColor = context.getColor(R.color.secondary)
                fillAlpha = 30
                setDrawHorizontalHighlightIndicator(false)
                setDrawVerticalHighlightIndicator(true)
                highlightLineWidth = 2f
            }
            
            val lineData = LineData(dataSet)
            chart.data = lineData
            
            // Set X-axis labels
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < labels.size) {
                        labels[index]
                    } else {
                        ""
                    }
                }
            }
        } else {
            // Show empty state
            chart.data = null
            chart.setNoDataText("No completion data available")
            chart.setNoDataTextColor(context.getColor(R.color.on_surface_variant))
        }
        
        // Refresh chart
        chart.invalidate()
    }
}
