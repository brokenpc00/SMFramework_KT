package com.brokenpc.smframework.shader

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.texture.Texture
import java.nio.FloatBuffer

// raw... base... primary... 3D Sprite

class ProgSprite3D : ShaderProgram() {
    private var _uniformTexture = 0
    private var _uniformModelMatrix = 0
    private var _uniformProjectionMatrix = 0
    private var _attrPosition = 0
    private var _attrTextureCoordinate = 0
    private var _attrVertexColor = 0
    private var _uniformColor = 0

    companion object {
        const val NAME_MODEL = "model"
        const  val NAME_PROJECTION = "projection"
        const  val NAME_POSITION = "position"
        const  val NAME_VERTEXCOLOR = "vertexColor"
        const  val NAME_COLOR = "inputColor"
        const  val NAME_TEXTURE = "inputImageTexture"
        const  val NAME_TEXTURECOORD = "inputTextureCoordinate"
    }

    override fun complete() {
        _uniformTexture = GLES20.glGetUniformLocation(_programId, NAME_TEXTURE)
        _uniformModelMatrix = GLES20.glGetUniformLocation(_programId, NAME_MODEL)
        _uniformProjectionMatrix = GLES20.glGetUniformLocation(_programId, NAME_PROJECTION)
        _uniformColor = GLES20.glGetUniformLocation(_programId, NAME_COLOR)
        _attrVertexColor = GLES20.glGetAttribLocation(_programId, NAME_VERTEXCOLOR)
        _attrPosition = GLES20.glGetAttribLocation(_programId, NAME_POSITION)
        _attrTextureCoordinate = GLES20.glGetAttribLocation(_programId, NAME_TEXTURECOORD)
    }

    override fun bind() {
        GLES20.glUseProgram(_programId)
        GLES20.glUniformMatrix4fv(_uniformProjectionMatrix, 1, false, _director!!.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW).m, 0)
        GLES20.glUniform1i(_uniformTexture, 0)
        GLES20.glEnableVertexAttribArray(_attrPosition)
        GLES20.glEnableVertexAttribArray(_attrTextureCoordinate)
        GLES20.glEnableVertexAttribArray(_attrVertexColor)
    }

    override fun unbind() {
        GLES20.glDisableVertexAttribArray(_attrPosition)
        GLES20.glDisableVertexAttribArray(_attrVertexColor)
        GLES20.glDisableVertexAttribArray(_attrTextureCoordinate)
    }

    override fun setMatrix(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(_uniformProjectionMatrix, 1, false, _director!!.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW).m, 0)
    }

    fun setDrawParam(texture: Texture, modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer, c: FloatBuffer): Boolean {
        if (_director!!.bindTexture(texture)) {
            GLES20.glVertexAttribPointer(_attrPosition, 3, GLES20.GL_FLOAT, false, 0, v)
            GLES20.glVertexAttribPointer(_attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv)
            GLES20.glVertexAttribPointer(_attrVertexColor, 4, GLES20.GL_FLOAT, false, 0, c)
            GLES20.glUniformMatrix4fv(_uniformModelMatrix, 1, false, modelMatrix, 0)
            GLES20.glUniform4fv(_uniformColor, 1, _director!!.getColor(), 0)
            return true
        }
        return false
    }
}