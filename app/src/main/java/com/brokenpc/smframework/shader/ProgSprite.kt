package com.brokenpc.smframework.shader

import android.opengl.GLES20
import com.brokenpc.smframework.base.texture.Texture
import java.nio.FloatBuffer

open class ProgSprite : ProgPrimitive() {

    private var _uniformTexture:Int = -1
    private var _attrTextureCoordinate:Int = -1

    companion object {
        private const val NAME_TEXTURE:String = "inputImageTexture"
        private const val NAME_TEXTURECOORD:String = "inputTextureCoordinate"
    }

    override fun complete() {
        super.complete()
        // uniform binding
        _uniformTexture = GLES20.glGetUniformLocation(_programId, NAME_TEXTURE)

        // attribute
        _attrTextureCoordinate = GLES20.glGetAttribLocation(_programId, NAME_TEXTURECOORD)
    }

    override fun bind() {
        super.bind()
        GLES20.glUniform1i(_uniformTexture, 0)
        GLES20.glEnableVertexAttribArray(_attrTextureCoordinate)
    }

    override fun unbind() {
        super.unbind()
        GLES20.glDisableVertexAttribArray(_attrTextureCoordinate)
    }

    open fun setDrawParam(texture: Texture, modelMatrix: FloatArray, v:FloatBuffer, uv:FloatBuffer): Boolean {
        if (_director!!.bindTexture(texture)) {
            super.setDrawParam(modelMatrix, v)
            GLES20.glVertexAttribPointer(_attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv)
            return true
        }

        return false
    }
}