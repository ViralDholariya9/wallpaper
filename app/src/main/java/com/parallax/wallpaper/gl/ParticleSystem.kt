package com.parallax.wallpaper.gl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.random.Random

enum class ParticleTheme(val title: String, val emoji: String) {
    STARDUST("Celestial Stardust", "✨"),
    RAIN_DROPS("Cyber Rain", "🌧️"),
    FIRE_EMBERS("Neon Embers", "🔥"),
    CYBER_MATRIX("Digital Sparks", "⚡"),
    NONE("Off", "🚫")
}

/**
 * Multi-Theme 3D Particle FX Engine.
 * Supports dynamic runtime switching between Stardust, Rain, Fire Embers, and Cyber Matrix.
 */
class ParticleSystem(private val particleCount: Int = 120) {

    var currentTheme: ParticleTheme = ParticleTheme.STARDUST

    private val vertexShaderCode = """
        attribute vec3 a_Position;      // Initial position (x, y, z)
        attribute float a_Depth;        // 3D Depth tier (0.15 to 1.0)
        attribute float a_PointSize;    // Base particle size
        attribute float a_Speed;        // Speed multiplier
        attribute float a_Phase;        // Organic swaying phase
        attribute float a_BaseAlpha;    // Base transparency

        uniform vec2 u_Offset;          // Sensor tilt offset
        uniform float u_Time;           // Running animation time
        uniform vec2 u_TouchPos;        // Interactive touch position
        uniform float u_TouchTime;      // Elapsed time since touch
        uniform int u_ThemeType;        // 0=Stardust, 1=Rain, 2=Embers, 3=Cyber

        varying float v_Alpha;
        varying float v_Depth;
        varying float v_ThemeType;

        void main() {
            vec2 pos = a_Position.xy;

            if (u_ThemeType == 1) {
                // Cyber Rain: Falls downwards rapidly with vertical parallax streak
                pos.y -= u_Time * (a_Speed * 0.95);
                pos.x += sin(u_Time * 0.5 + a_Phase) * 0.01;
            } else if (u_ThemeType == 2) {
                // Neon Embers: Floats upwards with turbulent swaying heat drift
                pos.y += u_Time * (a_Speed * 0.18);
                pos.x += sin(u_Time * 2.2 + a_Phase) * (0.07 * a_Depth);
            } else {
                // Stardust / Cyber Matrix: Gentle floating drift
                pos.y += u_Time * (a_Speed * 0.12);
                pos.x += sin(u_Time * 1.3 + a_Phase) * (0.04 * a_Depth);
            }

            // Screen wrap (-1.0 to 1.0)
            pos.x = mod(pos.x + 1.0, 2.0) - 1.0;
            pos.y = mod(pos.y + 1.0, 2.0) - 1.0;

            // 3D Parallax displacement (close particles have dramatic shift)
            vec2 parallaxShift = u_Offset * (a_Depth * 0.24);
            pos += parallaxShift;

            // Touch Repulsion Blast
            if (u_TouchTime >= 0.0 && u_TouchTime < 2.0) {
                vec2 normPos = (pos * 0.5) + vec2(0.5);
                normPos.y = 1.0 - normPos.y;
                float dist = distance(normPos, u_TouchPos);
                if (dist < 0.35) {
                    float blastPower = (1.0 - (dist / 0.35)) * exp(-u_TouchTime * 2.5) * 0.18;
                    vec2 blastDir = normalize(normPos - u_TouchPos);
                    pos += blastDir * blastPower;
                }
            }

            pos.x = mod(pos.x + 1.0, 2.0) - 1.0;
            pos.y = mod(pos.y + 1.0, 2.0) - 1.0;

            gl_Position = vec4(pos, a_Position.z, 1.0);

            // Size scaling
            if (u_ThemeType == 1) {
                gl_PointSize = a_PointSize * 0.65; // Sleek thin rain streaks
            } else {
                gl_PointSize = a_PointSize * (0.7 + a_Depth * 0.65);
            }

            // Twinkle / shimmer
            float twinkle = sin(u_Time * 3.0 + a_Phase) * 0.35 + 0.65;
            v_Alpha = a_BaseAlpha * twinkle;
            v_Depth = a_Depth;
            v_ThemeType = float(u_ThemeType);
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;

        uniform vec3 u_Color;
        varying float v_Alpha;
        varying float v_Depth;
        varying float v_ThemeType;

        void main() {
            vec2 coord = gl_PointCoord - vec2(0.5);

            if (v_ThemeType > 0.5 && v_ThemeType < 1.5) {
                // Rain Drop Streak (elongated vertical drop)
                if (abs(coord.x) > 0.25 || abs(coord.y) > 0.5) discard;
                float streakAlpha = (1.0 - abs(coord.y) * 2.0) * (1.0 - abs(coord.x) * 4.0) * v_Alpha;
                gl_FragColor = vec4(vec3(0.5, 0.85, 1.0) * streakAlpha, streakAlpha);
                return;
            }

            float dist = length(coord);
            if (dist > 0.5) discard;

            if (v_ThemeType > 2.5) {
                // Cyber Matrix: Sharp geometric square sparkle
                if (abs(coord.x) > 0.38 || abs(coord.y) > 0.38) discard;
                float boxAlpha = (1.0 - max(abs(coord.x), abs(coord.y)) * 2.0) * v_Alpha;
                gl_FragColor = vec4(u_Color * boxAlpha, boxAlpha);
                return;
            }

            // Soft glowing neon circular core
            float innerCore = smoothstep(0.12, 0.0, dist) * 0.85;
            float outerHalo = exp(-dist * 5.0) * 0.85;
            float intensity = (innerCore + outerHalo) * v_Alpha;

            vec3 particleColor = u_Color;
            if (v_ThemeType > 1.5 && v_ThemeType < 2.5) {
                // Fire Embers: Warm orange-to-gold gradient
                particleColor = mix(vec3(1.0, 0.35, 0.05), vec3(1.0, 0.85, 0.2), v_Depth);
            }

            vec3 finalColor = mix(particleColor, vec3(1.0, 1.0, 1.0), innerCore * 0.7);
            gl_FragColor = vec4(finalColor * intensity, intensity);
        }
    """.trimIndent()

    private var programHandle = 0
    private var positionHandle = 0
    private var depthHandle = 0
    private var pointSizeHandle = 0
    private var speedHandle = 0
    private var phaseHandle = 0
    private var baseAlphaHandle = 0

    private var offsetHandle = 0
    private var timeHandle = 0
    private var touchPosHandle = 0
    private var touchTimeHandle = 0
    private var colorHandle = 0
    private var themeTypeHandle = 0

    private val vertexData = FloatArray(particleCount * 8)
    private val floatBuffer: FloatBuffer

    init {
        for (i in 0 until particleCount) {
            val idx = i * 8
            vertexData[idx + 0] = Random.nextFloat() * 2f - 1f
            vertexData[idx + 1] = Random.nextFloat() * 2f - 1f
            vertexData[idx + 2] = 0f
            vertexData[idx + 3] = Random.nextFloat() * 0.85f + 0.15f
            vertexData[idx + 4] = Random.nextFloat() * 16f + 7f
            vertexData[idx + 5] = Random.nextFloat() * 0.8f + 0.3f
            vertexData[idx + 6] = Random.nextFloat() * 6.283f
            vertexData[idx + 7] = Random.nextFloat() * 0.55f + 0.35f
        }

        floatBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        floatBuffer.position(0)
    }

    fun initGL() {
        programHandle = GLTextureHelper.createProgram(vertexShaderCode, fragmentShaderCode)
        if (programHandle != 0) {
            positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
            depthHandle = GLES20.glGetAttribLocation(programHandle, "a_Depth")
            pointSizeHandle = GLES20.glGetAttribLocation(programHandle, "a_PointSize")
            speedHandle = GLES20.glGetAttribLocation(programHandle, "a_Speed")
            phaseHandle = GLES20.glGetAttribLocation(programHandle, "a_Phase")
            baseAlphaHandle = GLES20.glGetAttribLocation(programHandle, "a_BaseAlpha")

            offsetHandle = GLES20.glGetUniformLocation(programHandle, "u_Offset")
            timeHandle = GLES20.glGetUniformLocation(programHandle, "u_Time")
            touchPosHandle = GLES20.glGetUniformLocation(programHandle, "u_TouchPos")
            touchTimeHandle = GLES20.glGetUniformLocation(programHandle, "u_TouchTime")
            colorHandle = GLES20.glGetUniformLocation(programHandle, "u_Color")
            themeTypeHandle = GLES20.glGetUniformLocation(programHandle, "u_ThemeType")
        }
    }

    fun draw(
        offsetX: Float,
        offsetY: Float,
        elapsedTimeSec: Float = 0.0f,
        touchX: Float = 0.5f,
        touchY: Float = 0.5f,
        touchTime: Float = -1.0f,
        colorR: Float = 0.0f,
        colorG: Float = 0.9f,
        colorB: Float = 1.0f
    ) {
        if (programHandle == 0 || currentTheme == ParticleTheme.NONE) return

        GLES20.glUseProgram(programHandle)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE)

        val stride = 8 * 4

        floatBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, stride, floatBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        floatBuffer.position(3)
        GLES20.glVertexAttribPointer(depthHandle, 1, GLES20.GL_FLOAT, false, stride, floatBuffer)
        GLES20.glEnableVertexAttribArray(depthHandle)

        floatBuffer.position(4)
        GLES20.glVertexAttribPointer(pointSizeHandle, 1, GLES20.GL_FLOAT, false, stride, floatBuffer)
        GLES20.glEnableVertexAttribArray(pointSizeHandle)

        floatBuffer.position(5)
        GLES20.glVertexAttribPointer(speedHandle, 1, GLES20.GL_FLOAT, false, stride, floatBuffer)
        GLES20.glEnableVertexAttribArray(speedHandle)

        floatBuffer.position(6)
        GLES20.glVertexAttribPointer(phaseHandle, 1, GLES20.GL_FLOAT, false, stride, floatBuffer)
        GLES20.glEnableVertexAttribArray(phaseHandle)

        floatBuffer.position(7)
        GLES20.glVertexAttribPointer(baseAlphaHandle, 1, GLES20.GL_FLOAT, false, stride, floatBuffer)
        GLES20.glEnableVertexAttribArray(baseAlphaHandle)

        val themeId = when (currentTheme) {
            ParticleTheme.STARDUST -> 0
            ParticleTheme.RAIN_DROPS -> 1
            ParticleTheme.FIRE_EMBERS -> 2
            ParticleTheme.CYBER_MATRIX -> 3
            ParticleTheme.NONE -> 0
        }

        GLES20.glUniform2f(offsetHandle, offsetX, offsetY)
        if (timeHandle != -1) GLES20.glUniform1f(timeHandle, elapsedTimeSec)
        if (touchPosHandle != -1) GLES20.glUniform2f(touchPosHandle, touchX, touchY)
        if (touchTimeHandle != -1) GLES20.glUniform1f(touchTimeHandle, touchTime)
        if (colorHandle != -1) GLES20.glUniform3f(colorHandle, colorR, colorG, colorB)
        if (themeTypeHandle != -1) GLES20.glUniform1i(themeTypeHandle, themeId)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, particleCount)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(depthHandle)
        GLES20.glDisableVertexAttribArray(pointSizeHandle)
        GLES20.glDisableVertexAttribArray(speedHandle)
        GLES20.glDisableVertexAttribArray(phaseHandle)
        GLES20.glDisableVertexAttribArray(baseAlphaHandle)

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }
}
