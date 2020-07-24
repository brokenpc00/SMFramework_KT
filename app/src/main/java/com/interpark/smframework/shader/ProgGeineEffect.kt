package com.interpark.smframework.shader

import android.opengl.GLES20
import com.interpark.smframework.base.texture.Texture
import java.nio.FloatBuffer

class ProgGeineEffect : ProgSprite() {
    private var _uniformWidth = 0
    private var _uniformHeight = 0
    private var _uniformMinimize = 0
    private var _uniformBend = 0
    private var _uniformSide = 0

    companion object {
        const val NAME_WIDTH = "width"
        const val NAME_HEIGHT = "height"
        const val NAME_MINIMIZE = "minimize"
        const val NAME_BEND = "bend"
        const val NAME_SIDE = "side"
    }

    override fun complete() {
        super.complete()
        _uniformWidth = GLES20.glGetUniformLocation(_programId, NAME_WIDTH)
        _uniformHeight = GLES20.glGetUniformLocation(_programId, NAME_HEIGHT)
        _uniformMinimize = GLES20.glGetUniformLocation(_programId, NAME_MINIMIZE)
        _uniformBend = GLES20.glGetUniformLocation(_programId, NAME_BEND)
        _uniformSide = GLES20.glGetUniformLocation(_programId, NAME_SIDE)
    }

    fun setDrawParam(texture: Texture, modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer): Boolean {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            GLES20.glUniform1f(_uniformWidth, texture.getWidth().toFloat())
            GLES20.glUniform1f(_uniformHeight, texture.getHeight().toFloat())
            return true
        }
        return false
    }

    fun setGeineValue(minimize: Float, bend: Float, side: Float) {
        GLES20.glUniform1f(_uniformMinimize, minimize)
        GLES20.glUniform1f(_uniformBend, bend)
        GLES20.glUniform1f(_uniformSide, side)
    }
}