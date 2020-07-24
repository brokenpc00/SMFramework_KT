package com.interpark.smframework.shader

import android.opengl.GLES20
import com.interpark.smframework.base.texture.Texture
import java.nio.FloatBuffer

class ProgGaussianBlur : ProgSprite() {
    private var _uniformTexelWidth = 0
    private var _uniformTexelHeight = 0
    private var _programIndex = 0
    private var _texelSpacingMultiplier = DEFAULT_TEXEL_SPACING_MULTIPLIER

    companion object {
        const val DEFAULT_TEXEL_SPACING_MULTIPLIER = 1.0f
        const val NAME_TEXEL_WIDTH = "texelWidthOffset"
        const val NAME_TEXEL_HEIGHT = "texelHeightOffset"
    }

    override fun complete() {
        super.complete()
        _uniformTexelWidth = GLES20.glGetUniformLocation(_programId, NAME_TEXEL_WIDTH)
        _uniformTexelHeight = GLES20.glGetUniformLocation(_programId, NAME_TEXEL_HEIGHT)
    }

    override fun setDrawParam(texture: Texture, modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer): Boolean {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            if (_programIndex == 0) {
                GLES20.glUniform1f(_uniformTexelWidth, 0f)
                GLES20.glUniform1f(_uniformTexelHeight, _texelSpacingMultiplier / texture.getHeight())
            } else {
                GLES20.glUniform1f(_uniformTexelWidth, _texelSpacingMultiplier / texture.getWidth())
                GLES20.glUniform1f(_uniformTexelHeight, 0f)
            }
            return true
        }
        return false
    }

    fun setProgramIndex(index: Int) {
        _programIndex = index
    }

    fun setTexelSpacingMultiplier(texelSpacingMultiplier: Float) {
        _texelSpacingMultiplier = texelSpacingMultiplier
    }
}