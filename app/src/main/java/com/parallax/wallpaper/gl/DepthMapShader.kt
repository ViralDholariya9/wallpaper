package com.parallax.wallpaper.gl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * AAA Studio-Grade OpenGL ES 2.0 4D Holographic Shader.
 * Features:
 * 1. 3D Perspective Trapezoidal Rotation in Vertex Shader.
 * 2. 8-Layer Parallax Occlusion Mapping (POM) with Depth-Disocclusion Anti-Stretching.
 * 3. Anamorphic Horizontal Lens Flare streak glinting across highlights.
 * 4. Surface Normal Estimation, Specular Glass Sheen & Holographic Rainbow Sheen.
 * 5. Depth-dependent Chromatic Aberration & Ambient Occlusion.
 */
class DepthMapShader {

    private val vertexShaderCode = """
        attribute vec4 a_Position;
        attribute vec2 a_TexCoordinate;
        
        uniform vec2 u_Offset; // Gyro tilt offset for 3D perspective rotation
        
        varying vec2 v_TexCoordinate;
        varying vec2 v_TiltOffset;

        void main() {
            // 3D Perspective Foreshortening & Trapezoidal Tilt
            float zDepth = - (a_Position.x * u_Offset.x * 0.18 + a_Position.y * u_Offset.y * 0.18);
            float perspective = 1.0 / (1.0 - zDepth);

            // 1.18x Zoom prevents edge clipping during maximum perspective tilt
            vec2 tiltedPos = a_Position.xy * 1.18 * perspective;
            gl_Position = vec4(tiltedPos, zDepth, 1.0);

            v_TexCoordinate = a_TexCoordinate;
            v_TiltOffset = u_Offset;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;

        uniform sampler2D u_BaseTexture;
        uniform sampler2D u_DepthMap;
        uniform vec2 u_Offset;          // Gyroscope tilt offset (-1.0 to 1.0)
        uniform float u_DepthFactor;    // Parallax depth intensity (0.05 - 0.20)
        uniform vec2 u_TouchPos;        // Interactive touch point (0.0 to 1.0)
        uniform float u_TouchTime;      // Elapsed time since touch in seconds
        uniform float u_Time;           // Running animation time in seconds
        uniform float u_FlareIntensity; // Anamorphic lens flare intensity (0.0 to 1.0)
        uniform float u_HoloIntensity;  // Holographic rainbow shimmer intensity (0.0 to 1.0)

        varying vec2 v_TexCoordinate;
        varying vec2 v_TiltOffset;

        void main() {
            // 1. Interactive Touch Ripple Physics
            vec2 rippleShift = vec2(0.0);
            if (u_TouchTime >= 0.0 && u_TouchTime < 3.0) {
                float dist = distance(v_TexCoordinate, u_TouchPos);
                float wave = sin(dist * 45.0 - u_TouchTime * 16.0) * exp(-dist * 4.5) * exp(-u_TouchTime * 1.8);
                vec2 dir = v_TexCoordinate - u_TouchPos;
                if (length(dir) > 0.0001) {
                    rippleShift = normalize(dir) * wave * 0.045;
                }
            }

            vec2 baseUV = clamp(v_TexCoordinate + rippleShift, 0.0, 1.0);

            // 2. Parallax Occlusion Mapping (POM) - 8 Ray Marching Iterations
            const int NUM_STEPS = 8;
            float stepSize = 1.0 / float(NUM_STEPS);
            vec2 maxDelta = u_Offset * (u_DepthFactor * 1.35);
            vec2 stepDelta = maxDelta * stepSize;

            vec2 curUV = baseUV - maxDelta * 0.5;
            float curLayerDepth = 0.0;
            float depthVal = texture2D(u_DepthMap, clamp(curUV, 0.0, 1.0)).r;

            for (int i = 0; i < NUM_STEPS; i++) {
                if (curLayerDepth >= (1.0 - depthVal)) break;
                curUV += stepDelta;
                depthVal = texture2D(u_DepthMap, clamp(curUV, 0.0, 1.0)).r;
                curLayerDepth += stepSize;
            }

            // Linear boundary interpolation
            vec2 prevUV = curUV - stepDelta;
            float afterDepth = curLayerDepth - (1.0 - depthVal);
            float beforeDepth = (curLayerDepth - stepSize) - (1.0 - texture2D(u_DepthMap, clamp(prevUV, 0.0, 1.0)).r);
            float weight = clamp(afterDepth / (afterDepth - beforeDepth + 0.00001), 0.0, 1.0);
            vec2 pomUV = mix(curUV, prevUV, weight);

            // 3. Edge Anti-Stretching (Soft Silhouette Feathering on steep depth cliffs)
            float depthCliff = abs(afterDepth - beforeDepth);
            float edgeSoftness = smoothstep(0.65, 0.15, depthCliff);
            vec2 finalUV = clamp(mix(baseUV, pomUV, edgeSoftness), 0.001, 0.999);

            // 4. Dynamic Surface Normal Estimation from Depth Gradient
            vec2 texel = vec2(1.0 / 256.0, 1.0 / 512.0);
            float dRight = texture2D(u_DepthMap, clamp(finalUV + vec2(texel.x * 2.0, 0.0), 0.0, 1.0)).r;
            float dLeft  = texture2D(u_DepthMap, clamp(finalUV - vec2(texel.x * 2.0, 0.0), 0.0, 1.0)).r;
            float dUp    = texture2D(u_DepthMap, clamp(finalUV + vec2(0.0, texel.y * 2.0), 0.0, 1.0)).r;
            float dDown  = texture2D(u_DepthMap, clamp(finalUV - vec2(0.0, texel.y * 2.0), 0.0, 1.0)).r;
            vec3 normal = normalize(vec3((dLeft - dRight) * 2.8, (dDown - dUp) * 2.8, 0.38));

            // 5. Dynamic 3D Light & Specular Sheen (Reacts to tilting)
            vec3 lightDir = normalize(vec3(u_Offset.x * 2.0, u_Offset.y * 2.0, 0.9));
            float diffuse = max(dot(normal, lightDir), 0.0) * 0.22;

            vec3 viewDir = vec3(0.0, 0.0, 1.0);
            vec3 reflectDir = reflect(-lightDir, normal);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16.0) * 0.40;

            // 6. Holographic Rainbow Iridescent Sheen
            float glareAxis = dot(v_TexCoordinate - vec2(0.5), vec2(0.707, 0.707));
            float glarePos = (u_Offset.x * 0.75 + u_Offset.y * 0.45);
            float glareDist = abs(glareAxis - glarePos);
            float holoGlare = smoothstep(0.18, 0.0, glareDist) * 0.22 * u_HoloIntensity;
            vec3 holoColor = vec3(
                sin(holoGlare * 6.28 + 0.0) * 0.5 + 0.5,
                sin(holoGlare * 6.28 + 2.09) * 0.5 + 0.5,
                sin(holoGlare * 6.28 + 4.18) * 0.5 + 0.5
            ) * holoGlare;

            // 7. Anamorphic Streak Lens Flare (Horizontal cinematic glint)
            float flareCenterY = 0.5 - u_Offset.y * 0.35;
            float flareDistY = abs(v_TexCoordinate.y - flareCenterY);
            float flareStreak = smoothstep(0.04, 0.0, flareDistY) * pow(max(dot(normal, lightDir), 0.0), 10.0) * u_FlareIntensity;
            vec3 flareColor = vec3(0.15, 0.75, 1.0) * (flareStreak * 0.55);

            // 8. Chromatic Dispersion (RGB edge split based on depth and tilt)
            vec2 chromaOffset = u_Offset * (0.022 * depthVal);
            float r = texture2D(u_BaseTexture, clamp(finalUV + chromaOffset, 0.0, 1.0)).r;
            float g = texture2D(u_BaseTexture, finalUV).g;
            float b = texture2D(u_BaseTexture, clamp(finalUV - chromaOffset, 0.0, 1.0)).b;
            vec3 baseColor = vec3(r, g, b);

            // 9. Depth Ambient Occlusion & Soft Cinematic Vignette
            float ao = 0.88 + 0.12 * depthVal;
            vec2 vignetteCoord = v_TexCoordinate - vec2(0.5);
            float vignette = 1.0 - dot(vignetteCoord, vignetteCoord) * 0.45;

            // Final Composite
            vec3 finalColor = (baseColor * (ao + diffuse) + vec3(spec) + holoColor + flareColor) * vignette;
            gl_FragColor = vec4(finalColor, 1.0);
        }
    """.trimIndent()

    private var programHandle = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var baseTextureHandle = 0
    private var depthMapHandle = 0
    private var offsetHandle = 0
    private var depthFactorHandle = 0
    private var touchPosHandle = 0
    private var touchTimeHandle = 0
    private var timeHandle = 0
    private var flareIntensityHandle = 0
    private var holoIntensityHandle = 0

    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer

    init {
        val quadVertices = floatArrayOf(
            -1.0f,  1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
             1.0f,  1.0f, 0.0f,
             1.0f, -1.0f, 0.0f
        )

        val texCoordinates = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
        )

        vertexBuffer = ByteBuffer.allocateDirect(quadVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(quadVertices)
        vertexBuffer.position(0)

        texCoordBuffer = ByteBuffer.allocateDirect(texCoordinates.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(texCoordinates)
        texCoordBuffer.position(0)
    }

    fun initGL() {
        programHandle = GLTextureHelper.createProgram(vertexShaderCode, fragmentShaderCode)
        if (programHandle != 0) {
            positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
            texCoordHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate")
            baseTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_BaseTexture")
            depthMapHandle = GLES20.glGetUniformLocation(programHandle, "u_DepthMap")
            offsetHandle = GLES20.glGetUniformLocation(programHandle, "u_Offset")
            depthFactorHandle = GLES20.glGetUniformLocation(programHandle, "u_DepthFactor")
            touchPosHandle = GLES20.glGetUniformLocation(programHandle, "u_TouchPos")
            touchTimeHandle = GLES20.glGetUniformLocation(programHandle, "u_TouchTime")
            timeHandle = GLES20.glGetUniformLocation(programHandle, "u_Time")
            flareIntensityHandle = GLES20.glGetUniformLocation(programHandle, "u_FlareIntensity")
            holoIntensityHandle = GLES20.glGetUniformLocation(programHandle, "u_HoloIntensity")
        }
    }

    fun draw(
        baseTextureId: Int,
        depthMapId: Int,
        offsetX: Float,
        offsetY: Float,
        depthFactor: Float = 0.09f,
        touchX: Float = 0.5f,
        touchY: Float = 0.5f,
        touchTime: Float = -1.0f,
        elapsedTimeSec: Float = 0.0f,
        flareIntensity: Float = 1.0f,
        holoIntensity: Float = 1.0f
    ) {
        if (programHandle == 0 || baseTextureId == 0 || depthMapId == 0) return

        GLES20.glUseProgram(programHandle)

        // Vertex positions
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        // Texture coordinates
        texCoordBuffer.position(0)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        // Bind Base Texture to Texture Unit 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, baseTextureId)
        GLES20.glUniform1i(baseTextureHandle, 0)

        // Bind Depth Map to Texture Unit 1
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthMapId)
        GLES20.glUniform1i(depthMapHandle, 1)

        // Uniforms
        GLES20.glUniform2f(offsetHandle, offsetX, offsetY)
        GLES20.glUniform1f(depthFactorHandle, depthFactor)
        GLES20.glUniform2f(touchPosHandle, touchX, touchY)
        GLES20.glUniform1f(touchTimeHandle, touchTime)
        if (timeHandle != -1) GLES20.glUniform1f(timeHandle, elapsedTimeSec)
        if (flareIntensityHandle != -1) GLES20.glUniform1f(flareIntensityHandle, flareIntensity)
        if (holoIntensityHandle != -1) GLES20.glUniform1f(holoIntensityHandle, holoIntensity)

        // Draw quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }
}
