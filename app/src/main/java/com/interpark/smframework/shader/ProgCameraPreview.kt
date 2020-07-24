package com.interpark.smframework.shader

import android.opengl.GLES20
import com.interpark.smframework.IDirector
import java.nio.FloatBuffer

class ProgCameraPreview : ShaderProgram() {
    private var _uniformTexture = 0
    private var _uniformTexture2 = 0
    private var _uniformModelMatrix = 0
    private var _uniformProjectionMatrix = 0
    private var _attrPosition = 0
    private var _attrTextureCoordinate = 0

    companion object {
        const val NAME_MODEL = "model"
        const val NAME_PROJECTION = "projection"
        const val NAME_POSITION = "position"
        const val NAME_TEXTURE = "inputImageTexture"
        const val NAME_TEXTURE2 = "inputImageTexture2"
        const val NAME_TEXTURECOORD = "inputTextureCoordinate"
    }

    override fun complete() {
        _uniformTexture = GLES20.glGetUniformLocation(_programId, NAME_TEXTURE)
        _uniformTexture2 = GLES20.glGetUniformLocation(_programId, NAME_TEXTURE2)
        _uniformModelMatrix = GLES20.glGetUniformLocation(_programId, NAME_MODEL)
        _uniformProjectionMatrix = GLES20.glGetUniformLocation(_programId, NAME_PROJECTION)
        _attrPosition = GLES20.glGetAttribLocation(_programId, NAME_POSITION)
        _attrTextureCoordinate = GLES20.glGetAttribLocation(_programId, NAME_TEXTURECOORD)
    }

    override fun bind() {
        GLES20.glUseProgram(_programId)
        GLES20.glUniformMatrix4fv(_uniformProjectionMatrix, 1, false, _director!!.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW).m, 0)
        GLES20.glUniform1i(_uniformTexture, 0)
        GLES20.glUniform1i(_uniformTexture2, 1)
        GLES20.glEnableVertexAttribArray(_attrPosition)
        GLES20.glEnableVertexAttribArray(_attrTextureCoordinate)
    }

    override fun unbind() {
        GLES20.glDisableVertexAttribArray(_attrPosition)
        GLES20.glDisableVertexAttribArray(_attrTextureCoordinate)
    }

    override fun setMatrix(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(_uniformProjectionMatrix, 1, false, matrix, 0)
    }

    fun setDrawParam(modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer): Boolean {
        GLES20.glVertexAttribPointer(_attrPosition, 2, GLES20.GL_FLOAT, false, 0, v)
        GLES20.glVertexAttribPointer(_attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv)
        GLES20.glUniformMatrix4fv(_uniformModelMatrix, 1, false, modelMatrix, 0)
        GLES20.glUniform1i(_uniformTexture, 0)
        GLES20.glUniform1i(_uniformTexture2, 1)
        return true
    }
}