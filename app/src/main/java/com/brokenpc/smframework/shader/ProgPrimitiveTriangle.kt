package com.brokenpc.smframework.shader

import android.opengl.GLES20
import com.brokenpc.smframework.base.types.Vec2
import java.nio.FloatBuffer

class ProgPrimitiveTriangle : ProgPrimitive() {
    private var _attrTextureCoordinate = 0
    private var _uniformP0 = 0
    private var _uniformP1 = 0
    private var _uniformP2 = 0
    private var _uniformAAWidth = 0

    companion object {
        const val NAME_TEXTURECOORD = "inputTextureCoordinate"
        const val NAME_P0 = "u_p0"
        const val NAME_P1 = "u_p1"
        const val NAME_P2 = "u_p2"
        const val NAME_AAWIDTH = "u_aaWidth"
    }

    override fun complete() {
        super.complete()
        _attrTextureCoordinate = GLES20.glGetAttribLocation(_programId, NAME_TEXTURECOORD)
        _uniformP0 = GLES20.glGetUniformLocation(_programId, NAME_P0)
        _uniformP1 = GLES20.glGetUniformLocation(_programId, NAME_P1)
        _uniformP2 = GLES20.glGetUniformLocation(_programId, NAME_P2)
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

    fun setDrawParam(modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer, p0: Vec2, p1: Vec2, p2: Vec2, aaWidth: Float): Boolean {
        if (super.setDrawParam(modelMatrix, v)) {
            GLES20.glVertexAttribPointer(_attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv) //00 10, 01 11
            GLES20.glUniform2f(_uniformP0, p0.x, p0.y)
            GLES20.glUniform2f(_uniformP1, p1.x, p1.y)
            GLES20.glUniform2f(_uniformP2, p2.x, p2.y)
            GLES20.glUniform1f(_uniformAAWidth, aaWidth)
            return true
        }
        return false
    }
}