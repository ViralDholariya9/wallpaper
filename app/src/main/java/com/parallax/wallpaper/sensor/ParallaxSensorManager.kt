package com.parallax.wallpaper.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.parallax.wallpaper.utils.LiveWallpaperManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class ParallaxOffset(
    val x: Float = 0f, // -1f to +1f
    val y: Float = 0f  // -1f to +1f
)

/**
 * Studio-Grade 3D Sensor Engine with Harmonic Oscillator (Spring-Damper Physics)
 * and Stillness Motion Detection for Zero-Battery optimization.
 */
class ParallaxSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _parallaxOffset = MutableStateFlow(ParallaxOffset())
    val parallaxOffset: StateFlow<ParallaxOffset> = _parallaxOffset.asStateFlow()

    // Motion Stillness Detection (Zero-Battery Optimizer)
    private val _isMoving = MutableStateFlow(true)
    val isMoving: StateFlow<Boolean> = _isMoving.asStateFlow()

    private var previousTargetX = 0f
    private var previousTargetY = 0f
    private var stillnessDurationMs = 0L

    // Harmonic Oscillator Spring Physics
    val springDamper = SpringDamper(omega = 15.0f, zeta = 0.72f)
    var isSpringPhysicsEnabled: Boolean = true

    private var smoothX = 0f
    private var smoothY = 0f
    private val alpha = 0.18f
    private var lastTimestampNs: Long = 0L

    var sensitivity: Float = try {
        LiveWallpaperManager.getSensitivity(context)
    } catch (e: Exception) {
        1.3f
    }

    private var isListening = false

    fun startListening() {
        if (isListening) return
        val sensor = rotationVectorSensor ?: accelerometerSensor
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            isListening = true
            _isMoving.value = true
            stillnessDurationMs = 0L
        }
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager.unregisterListener(this)
        isListening = false
        lastTimestampNs = 0L
        _isMoving.value = false
    }

    fun applyImpulse(impulseX: Float, impulseY: Float) {
        springDamper.applyImpulse(impulseX, impulseY)
        _isMoving.value = true
        stillnessDurationMs = 0L
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var targetX = 0f
        var targetY = 0f

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                val pitch = orientation[1]
                val roll = orientation[2]

                targetX = (roll * 2.8f / Math.PI.toFloat()) * sensitivity
                targetY = ((pitch - 0.70f) * 2.8f / Math.PI.toFloat()) * sensitivity
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]

                targetX = (-x / 7.0f) * sensitivity
                targetY = ((y - 6.0f) / 7.0f) * sensitivity
            }
        }

        targetX = max(-1.0f, min(1.0f, targetX))
        targetY = max(-1.0f, min(1.0f, targetY))

        val nowNs = event.timestamp
        val dtSec = if (lastTimestampNs > 0L) {
            ((nowNs - lastTimestampNs) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
        } else {
            0.016f
        }
        lastTimestampNs = nowNs

        // Motion Stillness Detection: if delta motion is minimal for > 3.0 seconds, enter eco still state
        val deltaMotion = abs(targetX - previousTargetX) + abs(targetY - previousTargetY)
        previousTargetX = targetX
        previousTargetY = targetY

        val dtMs = (dtSec * 1000f).toLong()
        if (deltaMotion < 0.0020f) {
            stillnessDurationMs += dtMs
            if (stillnessDurationMs > 3000L && _isMoving.value) {
                _isMoving.value = false // Entered motionless desk state
            }
        } else {
            stillnessDurationMs = 0L
            if (!_isMoving.value) {
                _isMoving.value = true // Woke up instantly upon physical movement
            }
        }

        if (isSpringPhysicsEnabled) {
            springDamper.update(targetX, targetY, dtSec)
            _parallaxOffset.value = ParallaxOffset(
                x = springDamper.posX.coerceIn(-1.5f, 1.5f),
                y = springDamper.posY.coerceIn(-1.5f, 1.5f)
            )
        } else {
            smoothX += alpha * (targetX - smoothX)
            smoothY += alpha * (targetY - smoothY)
            _parallaxOffset.value = ParallaxOffset(x = smoothX, y = smoothY)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
