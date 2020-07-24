package com.interpark.smframework.shader

import android.opengl.GLES20
import com.interpark.smframework.IDirector
import java.nio.FloatBuffer

open class ProgPrimitive : ShaderProgram() {

    private var _uniformModelMatrix:Int = 0
    private var _uniformProjectionMatrix:Int = 0
    private var _uniformColor:Int = 0
    private var _attrPosition:Int = 0

    companion object {
        private const val NAME_MODEL:String = "model"
        private const val NAME_PROJECTION:String = "projection"
        private const val NAME_POSITION:String = "position"
        private const val NAME_COLOR:String = "inputColor"
    }

    override fun complete() {
        // uniform binding
        _uniformModelMatrix = GLES20.glGetUniformLocation(_programId, NAME_MODEL)
        _uniformProjectionMatrix = GLES20.glGetUniformLocation(_programId, NAME_PROJECTION)
        _uniformColor = GLES20.glGetUniformLocation(_programId, NAME_COLOR)

        // attribute
        _attrPosition = GLES20.glGetAttribLocation(_programId, NAME_POSITION)
    }

    override fun bind() {
        GLES20.glUseProgram(_programId)
        GLES20.glUniformMatrix4fv(_uniformProjectionMatrix, 1, false, _director!!.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW).m, 0)
        GLES20.glEnableVertexAttribArray(_attrPosition)
    }

    override fun unbind() {
        GLES20.glDisableVertexAttribArray(_attrPosition)
    }

    override fun setMatrix(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(_uniformProjectionMatrix, 1, false, matrix, 0)
    }

    open fun setDrawParam(modelMatrix: FloatArray, v: FloatBuffer): Boolean {
        GLES20.glVertexAttribPointer(_attrPosition, 2, GLES20.GL_FLOAT, false, 0, v)
        GLES20.glUniformMatrix4fv(_uniformModelMatrix, 1, false, modelMatrix, 0)
        GLES20.glUniform4fv(_uniformColor, 1, _director!!.getColor(), 0)
        return true
    }

}