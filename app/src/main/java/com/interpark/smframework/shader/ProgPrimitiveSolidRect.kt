package com.interpark.smframework.shader

import android.opengl.GLES20
import java.nio.FloatBuffer

class ProgPrimitiveSolidRect : ProgPrimitive() {
    private var _attrTextureCoordinate = 0
    private var _uniformDimen = 0
    private var _uniformRound = 0
    private var _uniformAAWidth = 0

    companion object {
        const val NAME_TEXTURECOORD = "inputTextureCoordinate"
        const val NAME_DIMEN = "dimension"
        const val NAME_ROUND = "round"
        const val NAME_AAWIDTH = "aaWidth"
    }

    override fun complete() {
        super.complete()
        _attrTextureCoordinate = GLES20.glGetAttribLocation(_programId, NAME_TEXTURECOORD)
        _uniformDimen = GLES20.glGetUniformLocation(_programId, NAME_DIMEN)
        _uniformRound = GLES20.glGetUniformLocation(_programId, NAME_ROUND)
        _uniformAAWidth = GLES20.glGetUniformLocation(_programId, NAME_AAWIDTH)
    }

    override fun bind() {
        super.bind()
        GLES20.glEnableVertexAttribArray(_attrTextureCoordinate)
    }

    override fun unbind() {
        super.unbind()
        GLES20.glDisableVertexAttribArray(_attrTextureCoordinate)
    }

    fun setDrawParam(modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer, width: Float, height: Float, roundPixels: Float, aaWidthPixels: Float): Boolean {
        if (width > 0 && height > 0 && super.setDrawParam(modelMatrix, v)) {
            GLES20.glVertexAttribPointer(_attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv)
            val r: Float
            val b: Float
            if (width > height) {
                GLES20.glUniform2f(_uniformDimen, .5f, .5f * height / width)
                r = roundPixels / width
                b = aaWidthPixels / width
            } else {
                GLES20.glUniform2f(_uniformDimen, .5f * width / height, .5f)
                r = roundPixels / height
                b = aaWidthPixels / height
            }
            GLES20.glUniform1f(_uniformRound, r)
            GLES20.glUniform1f(_uniformAAWidth, b)
            return true
        }
        return false
    }
}