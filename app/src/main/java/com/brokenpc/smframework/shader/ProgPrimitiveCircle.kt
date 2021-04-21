package com.brokenpc.smframework.shader

import android.opengl.GLES20
import com.brokenpc.smframework.base.types.Vec2
import java.nio.FloatBuffer

open class ProgPrimitiveCircle : ProgPrimitive() {

    private var _attrTextureCoordinate = 0
    private var _uniformRadius = 0
    private var _uniformAAWidth = 0
    private var _uniformAnchor = 0

    companion object {
        const val NAME_TEXTURECOORD = "inputTextureCoordinate"
        const val NAME_RADIUS = "radius"
        const val NAME_AAWIDTH = "aaWidth"
        const val NAME_ANCHOR = "anchor"
    }

    override fun complete() {
        super.complete()
        _attrTextureCoordinate = GLES20.glGetAttribLocation(_programId, NAME_TEXTURECOORD)
        _uniformRadius = GLES20.glGetUniformLocation(_programId, NAME_RADIUS)
        _uniformAAWidth = GLES20.glGetUniformLocation(_programId, NAME_AAWIDTH)
        _uniformAnchor = GLES20.glGetUniformLocation(_programId, NAME_ANCHOR)
    }

    override fun bind() {
        super.bind()
        GLES20.glEnableVertexAttribArray(_attrTextureCoordinate)
    }

    override fun unbind() {
        super.unbind()
        GLES20.glDisableVertexAttribArray(_attrTextureCoordinate)
    }

    fun setDrawParam(modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer, radius: Float, aaWidth: Float, anchor: Vec2): Boolean {
        if (super.setDrawParam(modelMatrix, v)) {
            GLES20.glVertexAttribPointer(_attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv)
            GLES20.glUniform1f(_uniformRadius, radius)
            GLES20.glUniform1f(_uniformAAWidth, aaWidth)
            GLES20.glUniform2f(_uniformAnchor, anchor.x, anchor.y)
            return true
        }
        return false
    }
}