package com.parallax.wallpaper.sensor

import kotlin.math.sqrt

/**
 * High-Performance 2D Damped Harmonic Oscillator (Spring-Damper Physics).
 * Simulates physical mass, stiffness, and viscosity damping for satisfying,
 * fluid, and organic motion with an elastic micro-rebound.
 */
class SpringDamper(
    // Natural frequency (angular speed) - higher = stiffer/snappier
    var omega: Float = 14.0f,
    // Damping ratio - 1.0 = critically damped, <1.0 = underdamped (subtle bounce), >1.0 = overdamped
    var zeta: Float = 0.74f
) {
    var posX: Float = 0f
        private set
    var posY: Float = 0f
        private set

    private var velX: Float = 0f
    private var velY: Float = 0f

    fun reset(x: Float = 0f, y: Float = 0f) {
        posX = x
        posY = y
        velX = 0f
        velY = 0f
    }

    /**
     * Updates the spring simulation towards target (x, y) over delta time.
     */
    fun update(targetX: Float, targetY: Float, dtSec: Float) {
        val dt = dtSec.coerceIn(0.001f, 0.05f) // clamp to prevent numerical explosion

        // Hooke's Law with Viscous Damping:
        // a = -omega^2 * (pos - target) - 2 * zeta * omega * vel
        val omegaSq = omega * omega
        val twoZetaOmega = 2.0f * zeta * omega

        val accelX = -omegaSq * (posX - targetX) - twoZetaOmega * velX
        val accelY = -omegaSq * (posY - targetY) - twoZetaOmega * velY

        velX += accelX * dt
        velY += accelY * dt

        posX += velX * dt
        posY += velY * dt
    }

    fun applyImpulse(impulseX: Float, impulseY: Float) {
        velX += impulseX
        velY += impulseY
    }
}
