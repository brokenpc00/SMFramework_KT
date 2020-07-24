package com.interpark.smframework.shader

import android.opengl.GLES20
import com.interpark.smframework.base.texture.Texture
import java.nio.FloatBuffer

class ProgRadialAlpha : ProgSprite() {
    private var _uniformDimension = 0
    private var _uniformCenter = 0
    private var _uniformRadius = 0
    private var _uniformBorder = 0

    companion object {
        const val NAME_DIMENSION = "u_dimension"
        const val NAME_CENTER = "u_center"
        const val NAME_RADIUS = "u_radius"
        const val NAME_BORDER = "u_border"
    }

    override fun complete() {
        super.complete()
        _uniformDimension = GLES20.glGetUniformLocation(_programId, NAME_DIMENSION)
        _uniformCenter = GLES20.glGetUniformLocation(_programId, NAME_CENTER)
        _uniformRadius = GLES20.glGetUniformLocation(_programId, NAME_RADIUS)
        _uniformBorder = GLES20.glGetUniformLocation(_programId, NAME_BORDER)
    }

    fun setDrawParam(texture: Texture, modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer, cx: Float, cy: Float, radius: Float, border: Float): Boolean {
        // TODO : 갤럭시S2의 presion bug로 인해 0.1곱함
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            GLES20.glUniform2f(_uniformDimension, 0.1f * texture.getWidth(), 0.1f * texture.getHeight())
            GLES20.glUniform2f(_uniformCenter, 0.1f * cx, 0.1f * cy)
            GLES20.glUniform1f(_uniformRadius, 0.1f * radius)
            GLES20.glUniform1f(_uniformBorder, 0.1f * border)
            return true
        }
        return false
    }
}