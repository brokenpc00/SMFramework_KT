package com.brokenpc.smframework.shader

import android.opengl.GLES20
import com.brokenpc.smframework.base.texture.Texture
import java.nio.FloatBuffer

class ProgEdgeGlow : ProgSprite() {
    private var _uniformTextureWidth = 0
    private var _uniformTextureHeight = 0
    private var _uniformScanPosition = 0

    companion object {
        const val NAME_TEXTURE_WIDTH = "inputTextureWidth"
        const val NAME_TEXTURE_HEIGHT = "inputTextureHeight"
        const val NAME_SCAN_POSITION = "scanPosition"
    }


    override fun complete() {
        super.complete()
        _uniformTextureWidth = GLES20.glGetUniformLocation(_programId, NAME_TEXTURE_WIDTH)
        _uniformTextureHeight = GLES20.glGetUniformLocation(_programId, NAME_TEXTURE_HEIGHT)
        _uniformScanPosition = GLES20.glGetUniformLocation(_programId, NAME_SCAN_POSITION)
    }

    fun setDrawParam(texture: Texture, modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer, scanPosition: Float): Boolean {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            GLES20.glUniform1f(_uniformTextureWidth, texture.getWidth().toFloat())
            GLES20.glUniform1f(_uniformTextureHeight, texture.getHeight().toFloat())
            GLES20.glUniform1f(_uniformScanPosition, scanPosition)
            return true
        }
        return false
    }
}