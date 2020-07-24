package com.interpark.smframework.base.shape

import android.opengl.GLES20
import android.opengl.Matrix
import com.interpark.smframework.IDirector
import com.interpark.smframework.base.DrawNode
import com.interpark.smframework.base.types.Vec2
import com.interpark.smframework.shader.ProgPrimitive
import com.interpark.smframework.shader.ShaderManager
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PrimitiveLine(director:IDirector) : DrawNode(director) {
    init {
        setProgramType(ShaderManager.ProgramType.Primitive)
        _drawMode = GLES20.GL_LINES
        _numVertices = 2
        _v = ByteBuffer.allocateDirect(4*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _v!!.position(0)
    }

    companion object {
        private val _vertice:FloatArray = FloatArray(2*2)
    }


    override fun _draw(modelMatrix: FloatArray) {
        val program = useProgram()
        if (program!=null) {
            if ((program as ProgPrimitive).setDrawParam(_matrix, _v!!)) {
                GLES20.glDrawArrays(_drawMode, 0, _numVertices)
            }
        }
    }

    fun drawLine(from:Vec2, to:Vec2) {
        drawLine(from.x, from.y, to.x, to.y)
    }

    fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) {
        _vertice[0] = 0f
        _vertice[1] = 1f
        _vertice[2] = (x2-x1)
        _vertice[3] = (y2-y1)
        _v!!.put(_vertice)
        _v!!.position(0)

        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x1, y1, 0f)
        _draw(_matrix)
    }
}