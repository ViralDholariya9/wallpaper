package com.parallax.wallpaper.visualizer

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class MusicVisualizerManager(context: Context) {

    private val prefs = context.getSharedPreferences("rewall_visualizer_prefs", Context.MODE_PRIVATE)

    private val _isActive = MutableStateFlow(prefs.getBoolean("visualizer_active", false))
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    // 16 Audio spectrum frequency bands (normalized 0.0 to 1.0)
    private val _frequencyBands = MutableStateFlow(FloatArray(16) { 0.2f })
    val frequencyBands: StateFlow<FloatArray> = _frequencyBands.asStateFlow()

    // Bass beat impulse (0.0 to 1.0) for live wallpaper pulse
    private val _bassPulse = MutableStateFlow(0.0f)
    val bassPulse: StateFlow<Float> = _bassPulse.asStateFlow()

    private var animationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        if (_isActive.value) {
            startVisualization()
        }
    }

    fun setActive(active: Boolean) {
        _isActive.value = active
        prefs.edit().putBoolean("visualizer_active", active).apply()
        if (active) {
            startVisualization()
        } else {
            stopVisualization()
        }
    }

    private fun startVisualization() {
        if (animationJob != null) return
        animationJob = scope.launch {
            var tick = 0f
            while (true) {
                tick += 0.15f
                val bands = FloatArray(16) { i ->
                    val wave = (sin((tick + i * 0.45f).toDouble()).toFloat() + 1f) * 0.45f
                    val noise = Random.nextFloat() * 0.2f
                    (wave + noise).coerceIn(0.1f, 1.0f)
                }

                val bass = ((sin(tick * 1.8).toFloat() + 1f) * 0.5f).coerceIn(0f, 1f)

                _frequencyBands.value = bands
                _bassPulse.value = bass
                delay(33) // ~30 FPS visualization update
            }
        }
    }

    private fun stopVisualization() {
        animationJob?.cancel()
        animationJob = null
        _frequencyBands.value = FloatArray(16) { 0f }
        _bassPulse.value = 0f
    }
}
