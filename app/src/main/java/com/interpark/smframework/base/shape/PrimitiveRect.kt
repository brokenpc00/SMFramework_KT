package com.interpark.smframework.base.shape

import android.opengl.GLES20
import com.interpark.smframework.IDirector
import com.interpark.smframework.base.DrawNode
import com.interpark.smframework.shader.ProgPrimitive
import com.interpark.smframework.shader.ShaderManager
import com.interpark.smframework.shader.ShaderProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class PrimitiveRect(director:IDirector, w:Float, h:Float, cx:Float, cy:Float, fill: Boolean=true) : DrawNode(director) {

    protected fun initRectHollow(director: IDirector, w: Float, h: Float, cx: Float, cy: Float) {
        _director = director
        _contentSize.set(w, h)
        _cx = cx
        _cy = cy
        initVertexHollowQuad()
    }

    protected fun initVertexHollowQuad() {
        val v:FloatArray = floatArrayOf(-_cx, -_cy,
                                        -_cx+_contentSize.width, -_cy,
                                        -_cx+_contentSize.width, -_cy+_contentSize.height,
                                        -_cx, -_cy+_contentSize.height
                                        -_cx, -_cy)

        _drawMode = GLES20.GL_LINE_STRIP
        _numVertices = 5

        _v = ByteBuffer.allocateDirect(v.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _v!!.put(v)
        _v!!.position(0)
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program:ShaderProgram = useProgram()
        if ((program as ProgPrimitive).setDrawParam(_matrix, _v!!)) {
            GLES20.glDrawArrays(_drawMode, 0, _numVertices)
        }
    }

    init {
        setProgramType(ShaderManager.ProgramType.Primitive)
        if (fill) {
            initRect(director, w, h, cx, cy)
        } else {
            initRectHollow(director, w, h, cx, cy)
        }
    }
}