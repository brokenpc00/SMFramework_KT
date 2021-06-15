package com.brokenpc.smframework.shader

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.texture.Texture
import java.nio.FloatBuffer

class ProgStencil : ProgPrimitive() {
    private var _uniformTexture:Int = -1
    private var _uniformModelMatrix = -1
    private var _uniformProjectionMatrix = -1
    private var _uniformAlphaTest = -1

    private var _attrPosition = -1
    private var _attrTextureCoordinate:Int = -1
    private var _attrColor = -1


    companion object {
        private const val NAME_PROJECTION = "projection"
        private const val NAME_MODEL = "model"
        private const val NAME_TEXTURE:String = "inputImageTexture"
        private const val NAME_ALPHA_VALUE = "alpha_value"

        private const val NAME_POSITION = "a_position"
        private const val NAME_TEXTURECOORD = "a_texCoord"
        private const val NAME_COLOR = "a_color"
    }

    override fun complete() {
        // uniform binding
        _uniformTexture = GLES20.glGetUniformLocation(_programId, NAME_TEXTURE)
        _uniformModelMatrix = GLES20.glGetUniformLocation(_programId, NAME_MODEL)
        _uniformProjectionMatrix = GLES20.glGetUniformLocation(_programId, NAME_PROJECTION)
        _uniformAlphaTest = GLES20.glGetUniformLocation(_programId, NAME_ALPHA_VALUE)

        // attribute
        _attrPosition = GLES20.glGetAttribLocation(_programId, NAME_POSITION)
        _attrTextureCoordinate = GLES20.glGetAttribLocation(_programId, NAME_TEXTURECOORD)
        _attrColor = GLES20.glGetAttribLocation(_programId, NAME_COLOR)
    }

    override fun bind() {
        GLES20.glUseProgram(_programId)
        GLES20.glUniformMatrix4fv(_uniformProjectionMatrix, 1, false, _director!!.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW).m, 0)
        GLES20.glUniform1i(_uniformTexture, 0)

        GLES20.glEnableVertexAttribArray(_attrPosition)
        GLES20.glEnableVertexAttribArray(_attrTextureCoordinate)
        GLES20.glEnableVertexAttribArray(_attrColor)
    }

    override fun unbind() {
        GLES20.glDisableVertexAttribArray(_attrPosition)
        GLES20.glDisableVertexAttribArray(_attrColor)
        GLES20.glDisableVertexAttribArray(_attrTextureCoordinate)
    }

    override fun setMatrix(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(_uniformProjectionMatrix, 1, false, _director!!.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW).m, 0)
    }

    open fun setDrawParam(texture: Texture, modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer, c: FloatBuffer): Boolean {
        if (_director!!.bindTexture(texture)) {

            GLES20.glVertexAttribPointer(_attrPosition, 3, GLES20.GL_FLOAT, false, 0, v)
            GLES20.glVertexAttribPointer(_attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv)
            GLES20.glVertexAttribPointer(_attrColor, 4, GLES20.GL_FLOAT, false, 0, c)
            GLES20.glUniformMatrix4fv(_uniformModelMatrix, 1, false, modelMatrix, 0)
            GLES20.glUniform1f(_uniformAlphaTest, 1f)

            return true
        }

        return false
    }
}