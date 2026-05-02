package com.example.wellnessapp.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.wellnessapp.R
import com.example.wellnessapp.charts.ChartUtils
import com.example.wellnessapp.databinding.FragmentMoodAnalyticsBinding
import com.example.wellnessapp.model.MoodEntry
import com.example.wellnessapp.util.DateUtils
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Fragment for displaying advanced mood analytics and insights.
 * Shows mood distribution, trends, statistics, and AI-generated insights.
 */
class MoodAnalyticsFragment : Fragment() {
    
    private var _binding: FragmentMoodAnalyticsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MoodAnalyticsViewModel by viewModels()
    private var selectedTimePeriod = TimePeriod.WEEK
    
    enum class TimePeriod {
        WEEK, MONTH, QUARTER
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTimePeriodSelector()
        setupClickListeners()
        observeViewModel()
        loadAnalytics()
    }
    
    private fun setupTimePeriodSelector() {
        // Set initial selection
        updateTimePeriodButtons(TimePeriod.WEEK)
    }
    
    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.weekButton.setOnClickListener { 
            selectedTimePeriod = TimePeriod.WEEK
            updateTimePeriodButtons(TimePeriod.WEEK)
            loadAnalytics()
        }
        
        binding.monthButton.setOnClickListener { 
            selectedTimePeriod = TimePeriod.MONTH
            updateTimePeriodButtons(TimePeriod.MONTH)
            loadAnalytics()
        }
        
        binding.quarterButton.setOnClickListener { 
            selectedTimePeriod = TimePeriod.QUARTER
            updateTimePeriodButtons(TimePeriod.QUARTER)
            loadAnalytics()
        }
    }
    
    private fun updateTimePeriodButtons(selected: TimePeriod) {
        val buttons = listOf(
            binding.weekButton to TimePeriod.WEEK,
            binding.monthButton to TimePeriod.MONTH,
            binding.quarterButton to TimePeriod.QUARTER
        )
        
        buttons.forEach { (button, period) ->
            if (period == selected) {
                button.setBackgroundColor(resources.getColor(R.color.primary, null))
                button.setTextColor(resources.getColor(R.color.on_primary, null))
            } else {
                button.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
                button.setTextColor(resources.getColor(R.color.primary, null))
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.moodDistribution.observe(viewLifecycleOwner, Observer { distribution ->
            setupMoodDistributionChart(distribution)
        })
        
        viewModel.moodTrends.observe(viewLifecycleOwner, Observer { trends ->
            ChartUtils.setupMoodTrendsChart(binding.moodTrendsChart, trends, requireContext())
        })
        
        viewModel.moodStatistics.observe(viewLifecycleOwner, Observer { stats ->
            updateMoodStatistics(stats)
        })
        
        viewModel.moodInsights.observe(viewLifecycleOwner, Observer { insights ->
            displayMoodInsights(insights)
        })
        
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // TODO: Show/hide loading indicator
        })
    }
    
    private fun loadAnalytics() {
        viewModel.loadAnalytics(selectedTimePeriod)
    }
    
    private fun setupMoodDistributionChart(distribution: Map<String, Int>) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        
        val moodColors = mapOf(
            "Happy" to "mood_happy_primary",
            "Neutral" to "mood_neutral_primary",
            "Sad" to "mood_sad_primary",
            "Angry" to "mood_angry_primary",
            "Excited" to "mood_excited_primary",
            "Calm" to "mood_calm_primary"
        )
        
        distribution.forEach { (mood, count) ->
            if (count > 0) {
                entries.add(PieEntry(count.toFloat(), mood))
                val colorResName = moodColors[mood] ?: "primary"
                val colorResId = resources.getIdentifier(colorResName, "color", requireContext().packageName)
                val color = if (colorResId != 0) {
                    resources.getColor(colorResId, null)
                } else {
                    resources.getColor(R.color.primary, null)
                }
                colors.add(color)
            }
        }
        
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = resources.getColor(R.color.on_surface, null)
        }
        
        val data = PieData(dataSet)
        binding.moodDistributionChart.apply {
            this.data = data
            description.isEnabled = false
            legend.isEnabled = false
            setHoleColor(resources.getColor(android.R.color.transparent, null))
            setTransparentCircleColor(resources.getColor(android.R.color.transparent, null))
            setTransparentCircleAlpha(0)
            holeRadius = 40f
            transparentCircleRadius = 45f
            setDrawEntryLabels(false)
            animateY(1000)
            invalidate()
        }
        
        // Create custom legend
        createMoodLegend(distribution, moodColors)
    }
    
    private fun createMoodLegend(distribution: Map<String, Int>, moodColors: Map<String, String>) {
        val legendContainer = binding.moodLegend
        legendContainer.removeAllViews()
        
        distribution.forEach { (mood, count) ->
            if (count > 0) {
                val legendItem = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_mood_legend, legendContainer, false)
                
                val colorView = legendItem.findViewById<View>(R.id.legend_color)
                val moodText = legendItem.findViewById<TextView>(R.id.legend_mood)
                val countText = legendItem.findViewById<TextView>(R.id.legend_count)
                
                val colorResName = moodColors[mood] ?: "primary"
                val colorResId = resources.getIdentifier(colorResName, "color", requireContext().packageName)
                val color = if (colorResId != 0) {
                    resources.getColor(colorResId, null)
                } else {
                    resources.getColor(R.color.primary, null)
                }
                colorView.setBackgroundColor(color)
                moodText.text = mood
                countText.text = count.toString()
                
                legendContainer.addView(legendItem)
            }
        }
    }
    
    private fun updateMoodStatistics(stats: MoodStatistics) {
        binding.averageMoodText.text = String.format("%.1f", stats.averageMood)
        binding.totalEntriesText.text = stats.totalEntries.toString()
        binding.streakText.text = stats.currentStreak.toString()
        binding.bestMoodText.text = String.format("%.1f", stats.bestMood)
    }
    
    private fun displayMoodInsights(insights: List<String>) {
        val insightsContainer = binding.insightsContainer
        insightsContainer.removeAllViews()
        
        insights.forEach { insight ->
            val insightCard = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 12
                }
                radius = 12f
                setCardBackgroundColor(resources.getColor(R.color.primary_container, null))
                elevation = 2f
            }
            
            val insightText = TextView(requireContext()).apply {
                text = insight
                textSize = 14f
                setTextColor(resources.getColor(R.color.on_primary_container, null))
                setPadding(16, 12, 16, 12)
                setLineSpacing(0f, 1.2f)
            }
            
            insightCard.addView(insightText)
            insightsContainer.addView(insightCard)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class MoodStatistics(
    val averageMood: Float,
    val totalEntries: Int,
    val currentStreak: Int,
    val bestMood: Float
)
