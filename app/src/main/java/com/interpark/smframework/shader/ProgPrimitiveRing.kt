package com.interpark.smframework.shader

import android.opengl.GLES20
import com.interpark.smframework.base.types.Vec2
import java.nio.FloatBuffer

class ProgPrimitiveRing : ProgPrimitiveCircle() {
    companion object {
        const val NAME_THICKNESS = "thickness"
    }

    private var _uniformThickness = 0

    override fun complete() {
        super.complete()
        _uniformThickness = GLES20.glGetUniformLocation(_programId, NAME_THICKNESS)
    }

    fun setDrawParam(modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer, radius: Float, thickness: Float, aaWidth: Float, anchor: Vec2): Boolean {
        if (super.setDrawParam(modelMatrix, v, uv, radius, aaWidth, anchor)) {
            GLES20.glUniform1f(_uniformThickness, thickness)
            return true
        }
        return false
    }
}