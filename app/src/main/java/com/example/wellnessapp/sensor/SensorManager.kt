package com.example.wellnessapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

/**
 * Manages sensor operations including step counting and shake detection.
 * Provides real-time sensor data for wellness tracking features.
 */
class SensorManager(private val context: Context) : SensorEventListener {
    
    private val systemSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    private var accelerometer: Sensor? = null
    private var stepCounter: Sensor? = null
    private var stepDetector: Sensor? = null
    
    // Step counting variables
    private var stepCount = 0
    private var lastStepTime = 0L
    private var stepThreshold = 10.0f // Minimum acceleration for step detection
    
    // Shake detection variables
    private var lastShakeTime = 0L
    private var shakeThreshold = 15.0f // Minimum acceleration for shake detection
    private var shakeTimeout = 1000L // Minimum time between shake detections (ms)
    
    // Callbacks
    private var onStepDetected: (() -> Unit)? = null
    private var onShakeDetected: (() -> Unit)? = null
    private var onStepCountChanged: ((Int) -> Unit)? = null
    
    // Sensor data
    private var lastAccelerometerValues = floatArrayOf(0f, 0f, 0f)
    private var isListening = false
    
    init {
        initializeSensors()
    }
    
    /**
     * Initializes available sensors
     */
    private fun initializeSensors() {
        // Get accelerometer sensor
        accelerometer = systemSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        // Get step counter sensor (hardware-based, if available)
        stepCounter = systemSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        // Get step detector sensor (hardware-based, if available)
        stepDetector = systemSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        
        Log.d("SensorManager", "Accelerometer available: ${accelerometer != null}")
        Log.d("SensorManager", "Step Counter available: ${stepCounter != null}")
        Log.d("SensorManager", "Step Detector available: ${stepDetector != null}")
    }
    
    /**
     * Starts listening to sensors
     */
    fun startListening() {
        if (isListening) return
        
        try {
            // Register accelerometer listener for step counting and shake detection
            accelerometer?.let { sensor ->
                systemSensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                Log.d("SensorManager", "Accelerometer listener registered")
            }
            
            // Register step counter listener (hardware-based)
            stepCounter?.let { sensor ->
                systemSensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                Log.d("SensorManager", "Step counter listener registered")
            }
            
            // Register step detector listener (hardware-based)
            stepDetector?.let { sensor ->
                systemSensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                Log.d("SensorManager", "Step detector listener registered")
            }
            
            isListening = true
            Log.d("SensorManager", "Sensor listening started")
            
        } catch (e: Exception) {
            Log.e("SensorManager", "Error starting sensor listening", e)
        }
    }
    
    /**
     * Stops listening to sensors
     */
    fun stopListening() {
        if (!isListening) return
        
        try {
            systemSensorManager.unregisterListener(this)
            isListening = false
            Log.d("SensorManager", "Sensor listening stopped")
        } catch (e: Exception) {
            Log.e("SensorManager", "Error stopping sensor listening", e)
        }
    }
    
    /**
     * Sets callback for step detection
     */
    fun setOnStepDetected(callback: () -> Unit) {
        onStepDetected = callback
    }
    
    /**
     * Sets callback for shake detection
     */
    fun setOnShakeDetected(callback: () -> Unit) {
        onShakeDetected = callback
    }
    
    /**
     * Sets callback for step count changes
     */
    fun setOnStepCountChanged(callback: (Int) -> Unit) {
        onStepCountChanged = callback
    }
    
    /**
     * Gets current step count
     */
    fun getStepCount(): Int = stepCount
    
    /**
     * Resets step count
     */
    fun resetStepCount() {
        stepCount = 0
        onStepCountChanged?.invoke(stepCount)
    }
    
    /**
     * Sets step detection threshold
     */
    fun setStepThreshold(threshold: Float) {
        stepThreshold = threshold
    }
    
    /**
     * Sets shake detection threshold
     */
    fun setShakeThreshold(threshold: Float) {
        shakeThreshold = threshold
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    handleAccelerometerData(sensorEvent.values)
                }
                Sensor.TYPE_STEP_COUNTER -> {
                    handleStepCounterData(sensorEvent.values[0].toInt())
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    handleStepDetectorData()
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }
    
    /**
     * Handles accelerometer data for step counting and shake detection
     */
    private fun handleAccelerometerData(values: FloatArray) {
        val currentTime = System.currentTimeMillis()
        
        // Calculate acceleration magnitude
        val acceleration = sqrt(
            (values[0] - lastAccelerometerValues[0]) * (values[0] - lastAccelerometerValues[0]) +
            (values[1] - lastAccelerometerValues[1]) * (values[1] - lastAccelerometerValues[1]) +
            (values[2] - lastAccelerometerValues[2]) * (values[2] - lastAccelerometerValues[2])
        )
        
        // Check for shake detection
        if (acceleration > shakeThreshold && 
            currentTime - lastShakeTime > shakeTimeout) {
            lastShakeTime = currentTime
            onShakeDetected?.invoke()
            Log.d("SensorManager", "Shake detected! Acceleration: $acceleration")
        }
        
        // Check for step detection (if no hardware step counter available)
        if (stepCounter == null && stepDetector == null) {
            if (acceleration > stepThreshold && 
                currentTime - lastStepTime > 200) { // Minimum 200ms between steps
                lastStepTime = currentTime
                stepCount++
                onStepDetected?.invoke()
                onStepCountChanged?.invoke(stepCount)
                Log.d("SensorManager", "Step detected! Count: $stepCount")
            }
        }
        
        // Update last values
        lastAccelerometerValues = values.copyOf()
    }
    
    /**
     * Handles hardware step counter data
     */
    private fun handleStepCounterData(totalSteps: Int) {
        // Hardware step counter provides total steps since last reboot
        // We need to track the difference for daily step count
        stepCount = totalSteps
        onStepCountChanged?.invoke(stepCount)
        Log.d("SensorManager", "Hardware step counter: $totalSteps")
    }
    
    /**
     * Handles hardware step detector data
     */
    private fun handleStepDetectorData() {
        stepCount++
        onStepDetected?.invoke()
        onStepCountChanged?.invoke(stepCount)
        Log.d("SensorManager", "Hardware step detector: $stepCount")
    }
    
    /**
     * Checks if sensors are available
     */
    fun areSensorsAvailable(): Boolean {
        return accelerometer != null
    }
    
    /**
     * Checks if hardware step counter is available
     */
    fun isHardwareStepCounterAvailable(): Boolean {
        return stepCounter != null
    }
    
    /**
     * Checks if hardware step detector is available
     */
    fun isHardwareStepDetectorAvailable(): Boolean {
        return stepDetector != null
    }
    
    /**
     * Gets sensor information
     */
    fun getSensorInfo(): String {
        return buildString {
            appendLine("Accelerometer: ${if (accelerometer != null) "Available" else "Not Available"}")
            appendLine("Step Counter: ${if (stepCounter != null) "Available" else "Not Available"}")
            appendLine("Step Detector: ${if (stepDetector != null) "Available" else "Not Available"}")
            appendLine("Current Step Count: $stepCount")
            appendLine("Listening: $isListening")
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopListening()
        onStepDetected = null
        onShakeDetected = null
        onStepCountChanged = null
    }
}
