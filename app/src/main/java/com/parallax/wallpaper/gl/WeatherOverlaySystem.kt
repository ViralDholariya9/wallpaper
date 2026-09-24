package com.parallax.wallpaper.gl

import android.opengl.GLES20
import com.parallax.wallpaper.weather.WeatherCondition
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-Performance Dynamic Weather Overlay & Atmospheric FX Engine.
 * Renders realistic Live Rain (with tilt slant), Winter Snow (with wind drift),
 * Volumetric Parallax Clouds/Fog, and Thunderstorm Atmospheric Lightning Flashes.
 */
class WeatherOverlaySystem(private val particleCount: Int = 180) {

    var currentCondition: WeatherCondition = WeatherCondition.RAINY
    var isEnabled: Boolean = true

    // --- 1. Weather Particles (Rain / Snow) Shader ---
    private val particleVertexShader = """
        attribute vec3 a_Position;
        attribute float a_Depth;
        attribute float a_Size;
        attribute float a_Speed;
        attribute float a_Phase;
        attribute float a_Slant;

        uniform vec2 u_Offset;          // Gyro tilt offset
        uniform float u_Time;           // Animation timer
        uniform int u_Condition;        // 0=Sunny, 1=Rain, 2=Snow, 3=Clouds, 4=Thunder

        varying float v_Alpha;
        varying float v_Depth;
        varying float v_Condition;

        void main() {
            vec2 pos = a_Position.xy;

            if (u_Condition == 1 || u_Condition == 4) {
                // RAIN / THUNDERSTORM: Fast vertical fall slanted by phone tilt
                float fallSpeed = (u_Condition == 4) ? (a_Speed * 1.6) : (a_Speed * 1.15);
                pos.y -= u_Time * fallSpeed;
                pos.x += (u_Offset.x * 0.45 + a_Slant * 0.1) * a_Depth;
            } else if (u_Condition == 2) {
                // SNOW: Gentle tumbling drift with organic wind sway
                pos.y -= u_Time * (a_Speed * 0.18);
                pos.x += sin(u_Time * 1.6 + a_Phase) * (0.06 * a_Depth) + (u_Offset.x * 0.2 * a_Depth);
            } else {
                // SUNNY / CLOUDS: Floating light dust & atmospheric mist
                pos.y += sin(u_Time * 0.8 + a_Phase) * 0.05;
                pos.x += cos(u_Time * 0.6 + a_Phase) * 0.05;
            }

            // Screen wrap (-1.0 to 1.0)
            pos.x = mod(pos.x + 1.0, 2.0) - 1.0;
            pos.y = mod(pos.y + 1.0, 2.0) - 1.0;

            // 3D Parallax displacement
            vec2 parallaxShift = u_Offset * (a_Depth * 0.25);
            pos += parallaxShift;

            pos.x = mod(pos.x + 1.0, 2.0) - 1.0;
            pos.y = mod(pos.y + 1.0, 2.0) - 1.0;

            gl_Position = vec4(pos, a_Position.z, 1.0);

            if (u_Condition == 1 || u_Condition == 4) {
                // Elongated rain streaks
                gl_PointSize = a_Size * (0.8 + a_Depth * 0.9);
            } else if (u_Condition == 2) {
                // Soft fluffy snowflakes
                gl_PointSize = a_Size * (1.1 + a_Depth * 1.2);
            } else {
                gl_PointSize = a_Size * 0.6;
            }

            v_Alpha = 0.4 + 0.6 * a_Depth;
            v_Depth = a_Depth;
            v_Condition = float(u_Condition);
        }
    """.trimIndent()

    private val particleFragmentShader = """
        precision mediump float;

        varying float v_Alpha;
        varying float v_Depth;
        varying float v_Condition;

        void main() {
            vec2 coord = gl_PointCoord - vec2(0.5);

            if (v_Condition > 0.5 && v_Condition < 1.5 || v_Condition > 3.5) {
                // RAIN STREAK: Sleek vertical water streak
                if (abs(coord.x) > 0.18 || abs(coord.y) > 0.5) discard;
                float alpha = (1.0 - abs(coord.y) * 2.0) * (1.0 - abs(coord.x) * 5.0) * v_Alpha;
                vec3 rainColor = vec3(0.68, 0.88, 1.0);
                gl_FragColor = vec4(rainColor * alpha, alpha * 0.85);
                return;
            }

            float dist = length(coord);
            if (dist > 0.5) discard;

            if (v_Condition > 1.5 && v_Condition < 2.5) {
                // SNOW FLAKE: Soft glowing hexagonal/circular crystalline flake
                float core = smoothstep(0.2, 0.0, dist);
                float softEdge = 1.0 - smoothstep(0.2, 0.5, dist);
                float flakeAlpha = (core * 0.7 + softEdge * 0.3) * v_Alpha;
                vec3 snowColor = mix(vec3(0.92, 0.96, 1.0), vec3(1.0, 1.0, 1.0), core);
                gl_FragColor = vec4(snowColor * flakeAlpha, flakeAlpha * 0.9);
                return;
            }

            // SUNNY / CLOUDS: Soft golden solar shimmer
            float shimmer = smoothstep(0.5, 0.0, dist) * v_Alpha;
            vec3 sunDust = vec3(1.0, 0.92, 0.65);
            gl_FragColor = vec4(sunDust * shimmer, shimmer * 0.5);
        }
    """.trimIndent()

    // --- 2. Atmospheric Clouds & Lightning Full-Screen Quad Shader ---
    private val atmosphereVertexShader = """
        attribute vec2 a_QuadPos;
        varying vec2 v_TexCoord;

        void main() {
            v_TexCoord = a_QuadPos * 0.5 + 0.5;
            gl_Position = vec4(a_QuadPos, 0.0, 1.0);
        }
    """.trimIndent()

    private val atmosphereFragmentShader = """
        precision mediump float;

        varying vec2 v_TexCoord;
        uniform vec2 u_Offset;
        uniform float u_Time;
        uniform int u_Condition;
        uniform float u_Lightning;

        // Procedural smooth sinusoidal cloud noise
        float cloudNoise(vec2 uv) {
            float n = sin(uv.x * 2.5 + u_Time * 0.08) * cos(uv.y * 2.2 + u_Time * 0.06);
            n += sin(uv.x * 4.8 - u_Time * 0.12) * cos(uv.y * 4.5 + u_Time * 0.09) * 0.5;
            n += sin(uv.x * 9.2 + uv.y * 8.5) * 0.25;
            return n * 0.5 + 0.5;
        }

        void main() {
            vec2 uv = v_TexCoord + u_Offset * 0.08;

            if (u_Condition == 3 || u_Condition == 1 || u_Condition == 4) {
                // CLOUDS & OVERCAST MIST
                float clouds = cloudNoise(uv);
                float density = smoothstep(0.35, 0.85, clouds);
                float edgeFade = smoothstep(0.0, 0.3, v_TexCoord.y) * (1.0 - smoothstep(0.85, 1.0, v_TexCoord.y));
                
                vec3 cloudColor = (u_Condition == 4) ? vec3(0.12, 0.14, 0.22) : vec3(0.45, 0.52, 0.65);
                float cloudAlpha = density * edgeFade * 0.38;

                // Atmospheric Lightning Flash illumination (Thunderstorm)
                if (u_Condition == 4 && u_Lightning > 0.01) {
                    vec3 lightningTint = vec3(0.85, 0.92, 1.0) * u_Lightning * 0.65;
                    gl_FragColor = vec4(lightningTint + cloudColor * cloudAlpha, cloudAlpha + u_Lightning * 0.6);
                    return;
                }

                gl_FragColor = vec4(cloudColor * cloudAlpha, cloudAlpha);
                return;
            }

            if (u_Condition == 0) {
                // SUNNY: Warm radiant solar gradient at top right
                vec2 sunPos = vec2(0.85, 0.15) + u_Offset * 0.05;
                float sunDist = length(v_TexCoord - sunPos);
                float sunGlow = exp(-sunDist * 3.5) * 0.32;
                vec3 sunTint = vec3(1.0, 0.82, 0.55) * sunGlow;
                gl_FragColor = vec4(sunTint, sunGlow * 0.8);
                return;
            }

            gl_FragColor = vec4(0.0);
        }
    """.trimIndent()

    // Particle GL handles
    private var particleProgram = 0
    private var pPosHandle = 0
    private var pDepthHandle = 0
    private var pSizeHandle = 0
    private var pSpeedHandle = 0
    private var pPhaseHandle = 0
    private var pSlantHandle = 0
    private var pOffsetHandle = 0
    private var pTimeHandle = 0
    private var pConditionHandle = 0

    // Atmosphere GL handles
    private var atmosProgram = 0
    private var aQuadPosHandle = 0
    private var aOffsetHandle = 0
    private var aTimeHandle = 0
    private var aConditionHandle = 0
    private var aLightningHandle = 0

    private val particleBuffer: FloatBuffer
    private val quadBuffer: FloatBuffer

    // Lightning Flash state
    private var lightningTimer = 0f
    private var nextLightningInterval = 4.5f
    private var currentLightningBrightness = 0f

    init {
        // Vertex data: x, y, z, depth, size, speed, phase, slant (8 floats per particle)
        val pData = FloatArray(particleCount * 8)
        for (i in 0 until particleCount) {
            val idx = i * 8
            pData[idx + 0] = Random.nextFloat() * 2f - 1f
            pData[idx + 1] = Random.nextFloat() * 2f - 1f
            pData[idx + 2] = 0f
            pData[idx + 3] = Random.nextFloat() * 0.85f + 0.15f // Depth
            pData[idx + 4] = Random.nextFloat() * 18f + 8f      // Size
            pData[idx + 5] = Random.nextFloat() * 0.9f + 0.4f   // Speed
            pData[idx + 6] = Random.nextFloat() * 6.283f        // Phase
            pData[idx + 7] = Random.nextFloat() * 0.6f - 0.3f   // Slant
        }
        particleBuffer = ByteBuffer.allocateDirect(pData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(pData)
        particleBuffer.position(0)

        // Full-screen quad
        val quadVertices = floatArrayOf(
            -1f, -1f,
             1f, -1f,
            -1f,  1f,
             1f,  1f
        )
        quadBuffer = ByteBuffer.allocateDirect(quadVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(quadVertices)
        quadBuffer.position(0)
    }

    fun initGL() {
        // 1. Compile Particles Program
        particleProgram = GLTextureHelper.createProgram(particleVertexShader, particleFragmentShader)
        if (particleProgram != 0) {
            pPosHandle = GLES20.glGetAttribLocation(particleProgram, "a_Position")
            pDepthHandle = GLES20.glGetAttribLocation(particleProgram, "a_Depth")
            pSizeHandle = GLES20.glGetAttribLocation(particleProgram, "a_Size")
            pSpeedHandle = GLES20.glGetAttribLocation(particleProgram, "a_Speed")
            pPhaseHandle = GLES20.glGetAttribLocation(particleProgram, "a_Phase")
            pSlantHandle = GLES20.glGetAttribLocation(particleProgram, "a_Slant")

            pOffsetHandle = GLES20.glGetUniformLocation(particleProgram, "u_Offset")
            pTimeHandle = GLES20.glGetUniformLocation(particleProgram, "u_Time")
            pConditionHandle = GLES20.glGetUniformLocation(particleProgram, "u_Condition")
        }

        // 2. Compile Atmosphere Program
        atmosProgram = GLTextureHelper.createProgram(atmosphereVertexShader, atmosphereFragmentShader)
        if (atmosProgram != 0) {
            aQuadPosHandle = GLES20.glGetAttribLocation(atmosProgram, "a_QuadPos")
            aOffsetHandle = GLES20.glGetUniformLocation(atmosProgram, "u_Offset")
            aTimeHandle = GLES20.glGetUniformLocation(atmosProgram, "u_Time")
            aConditionHandle = GLES20.glGetUniformLocation(atmosProgram, "u_Condition")
            aLightningHandle = GLES20.glGetUniformLocation(atmosProgram, "u_Lightning")
        }
    }

    fun draw(offsetX: Float, offsetY: Float, elapsedTimeSec: Float, deltaSec: Float) {
        if (!isEnabled) return

        val conditionCode = when (currentCondition) {
            WeatherCondition.SUNNY -> 0
            WeatherCondition.RAINY -> 1
            WeatherCondition.SNOW -> 2
            WeatherCondition.CLOUDY -> 3
            WeatherCondition.THUNDERSTORM -> 4
        }

        // Update lightning simulation for thunderstorm
        if (currentCondition == WeatherCondition.THUNDERSTORM) {
            lightningTimer += deltaSec
            if (lightningTimer >= nextLightningInterval) {
                // Flash triggered!
                currentLightningBrightness = 1.0f
                lightningTimer = 0f
                nextLightningInterval = Random.nextFloat() * 4.0f + 2.5f // Next flash in 2.5 - 6.5s
            } else if (currentLightningBrightness > 0.01f) {
                currentLightningBrightness *= 0.82f // Realistic rapid exponential decay
            } else {
                currentLightningBrightness = 0f
            }
        } else {
            currentLightningBrightness = 0f
        }

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // PASS 1: Volumetric Clouds / Fog & Lightning Atmospheric Quad
        if (atmosProgram != 0) {
            GLES20.glUseProgram(atmosProgram)
            GLES20.glUniform2f(aOffsetHandle, offsetX, offsetY)
            GLES20.glUniform1f(aTimeHandle, elapsedTimeSec)
            GLES20.glUniform1i(aConditionHandle, conditionCode)
            GLES20.glUniform1f(aLightningHandle, currentLightningBrightness)

            quadBuffer.position(0)
            GLES20.glEnableVertexAttribArray(aQuadPosHandle)
            GLES20.glVertexAttribPointer(aQuadPosHandle, 2, GLES20.GL_FLOAT, false, 0, quadBuffer)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            GLES20.glDisableVertexAttribArray(aQuadPosHandle)
        }

        // PASS 2: Dynamic Weather Particles (Rain / Snow / Solar Dust)
        if (particleProgram != 0) {
            GLES20.glUseProgram(particleProgram)
            GLES20.glUniform2f(pOffsetHandle, offsetX, offsetY)
            GLES20.glUniform1f(pTimeHandle, elapsedTimeSec)
            GLES20.glUniform1i(pConditionHandle, conditionCode)

            val stride = 8 * 4 // 8 floats = 32 bytes

            particleBuffer.position(0)
            GLES20.glEnableVertexAttribArray(pPosHandle)
            GLES20.glVertexAttribPointer(pPosHandle, 3, GLES20.GL_FLOAT, false, stride, particleBuffer)

            particleBuffer.position(3)
            GLES20.glEnableVertexAttribArray(pDepthHandle)
            GLES20.glVertexAttribPointer(pDepthHandle, 1, GLES20.GL_FLOAT, false, stride, particleBuffer)

            particleBuffer.position(4)
            GLES20.glEnableVertexAttribArray(pSizeHandle)
            GLES20.glVertexAttribPointer(pSizeHandle, 1, GLES20.GL_FLOAT, false, stride, particleBuffer)

            particleBuffer.position(5)
            GLES20.glEnableVertexAttribArray(pSpeedHandle)
            GLES20.glVertexAttribPointer(pSpeedHandle, 1, GLES20.GL_FLOAT, false, stride, particleBuffer)

            particleBuffer.position(6)
            GLES20.glEnableVertexAttribArray(pPhaseHandle)
            GLES20.glVertexAttribPointer(pPhaseHandle, 1, GLES20.GL_FLOAT, false, stride, particleBuffer)

            particleBuffer.position(7)
            GLES20.glEnableVertexAttribArray(pSlantHandle)
            GLES20.glVertexAttribPointer(pSlantHandle, 1, GLES20.GL_FLOAT, false, stride, particleBuffer)

            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, particleCount)

            GLES20.glDisableVertexAttribArray(pPosHandle)
            GLES20.glDisableVertexAttribArray(pDepthHandle)
            GLES20.glDisableVertexAttribArray(pSizeHandle)
            GLES20.glDisableVertexAttribArray(pSpeedHandle)
            GLES20.glDisableVertexAttribArray(pPhaseHandle)
            GLES20.glDisableVertexAttribArray(pSlantHandle)
        }

        GLES20.glDisable(GLES20.GL_BLEND)
    }
}
