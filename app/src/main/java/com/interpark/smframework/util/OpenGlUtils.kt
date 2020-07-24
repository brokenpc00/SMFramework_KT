package com.interpark.smframework.util

import android.opengl.Matrix
import kotlin.math.tan

class OpenGlUtils {
    companion object {
        @JvmStatic
        fun getPerspectiveMatrix(result: FloatArray, fovy:Float, aspect:Float, zNear:Float, zFar:Float) {
            val top:Float = zNear * tan(fovy * Math.PI / 360.0).toFloat()
            val bottom:Float = -top
            val left:Float = bottom * aspect
            val right:Float = top * aspect

            Matrix.frustumM(result, 0, left, right, bottom, top, zNear, zFar)
        }

        @JvmStatic
        fun getLookAtMatrix(matrix: FloatArray, eyeX:Float, eyeY:Float, eyeZ: Float, centerX:Float, centerY:Float, centerZ:Float, upX:Float, upY:Float, upZ:Float) {
            Matrix.setLookAtM(matrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
        }

        @JvmStatic
        fun copyMatrix(dst:FloatArray, src:FloatArray, count:Int) {
            for (i in 0 until count) {
                dst[i] = src[i]
            }
        }
    }
}