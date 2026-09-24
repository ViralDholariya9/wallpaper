package com.parallax.wallpaper.gl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log

object GLTextureHelper {

    private const val TAG = "GLTextureHelper"

    /**
     * Compiles a GLSL shader (vertex or fragment)
     */
    fun compileShader(shaderType: Int, shaderSource: String): Int {
        val shaderHandle = GLES20.glCreateShader(shaderType)
        if (shaderHandle != 0) {
            GLES20.glShaderSource(shaderHandle, shaderSource)
            GLES20.glCompileShader(shaderHandle)

            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            if (compileStatus[0] == 0) {
                val log = GLES20.glGetShaderInfoLog(shaderHandle)
                Log.e(TAG, "Error compiling shader: $log")
                GLES20.glDeleteShader(shaderHandle)
                return 0
            }
        }
        return shaderHandle
    }

    /**
     * Creates and links an OpenGL program from vertex and fragment shaders
     */
    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)

        if (vertexShader == 0 || fragmentShader == 0) return 0

        val programHandle = GLES20.glCreateProgram()
        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vertexShader)
            GLES20.glAttachShader(programHandle, fragmentShader)
            GLES20.glLinkProgram(programHandle)

            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

            if (linkStatus[0] == 0) {
                val log = GLES20.glGetProgramInfoLog(programHandle)
                Log.e(TAG, "Error linking program: $log")
                GLES20.glDeleteProgram(programHandle)
                return 0
            }
        }
        return programHandle
    }

    /**
     * Loads an Android Bitmap into an OpenGL 2D texture
     */
    fun loadTexture(bitmap: Bitmap): Int {
        val textureObjectIds = IntArray(1)
        GLES20.glGenTextures(1, textureObjectIds, 0)

        if (textureObjectIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL texture object.")
            return 0
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureObjectIds[0]
    }

    /**
     * Synthesizes an intelligent Multi-Cue 3D Depth Map from any source photo:
     * Combines:
     * 1. Perspective horizon gradient (sky recedes deep, foreground comes forward)
     * 2. Central focal saliency bubble (main subject/character pops out)
     * 3. Hardware-accelerated luminance extraction (lights, neon, faces, and highlights stand out in relief)
     */
    fun generateDepthMap(baseBitmap: Bitmap): Bitmap {
        val width = 256
        val height = 512
        val depthBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(depthBitmap)

        // 1. Perspective Base Gradient (distant sky = 20, horizon = 60, close ground = 190)
        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    Color.rgb(25, 25, 25),
                    Color.rgb(75, 75, 75),
                    Color.rgb(140, 140, 140),
                    Color.rgb(210, 210, 210)
                ),
                floatArrayOf(0f, 0.35f, 0.70f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), basePaint)

        // 2. Central Saliency Focal Bubble (prominence for main subject / character / vehicle)
        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.5f, height * 0.44f,
                width * 0.65f,
                intArrayOf(Color.argb(210, 255, 255, 255), Color.argb(100, 130, 130, 130), Color.TRANSPARENT),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SCREEN)
        }
        canvas.drawCircle(width * 0.5f, height * 0.44f, width * 0.65f, centerPaint)

        // 3. High-Contrast Luminance Detail Extraction from source photo
        try {
            val lumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                val cm = android.graphics.ColorMatrix().apply {
                    setSaturation(0f) // Grayscale
                    // Boost contrast and brighten midtones
                    val scale = 1.35f
                    val translate = -35f
                    val matrix = floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 0.40f, 0f // 40% blend into depth
                    )
                    postConcat(android.graphics.ColorMatrix(matrix))
                }
                colorFilter = android.graphics.ColorMatrixColorFilter(cm)
                xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SCREEN)
            }
            val srcRect = android.graphics.Rect(0, 0, baseBitmap.width, baseBitmap.height)
            val dstRect = android.graphics.Rect(0, 0, width, height)
            canvas.drawBitmap(baseBitmap, srcRect, dstRect, lumPaint)
        } catch (e: Exception) {
            Log.w(TAG, "Luminance depth extraction fallback", e)
        }

        return depthBitmap
    }
}
