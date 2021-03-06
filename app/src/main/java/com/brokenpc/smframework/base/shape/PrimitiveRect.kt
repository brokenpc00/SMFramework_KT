package com.brokenpc.smframework.base.shape

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.shader.ProgPrimitive
import com.brokenpc.smframework.shader.ShaderManager
import com.brokenpc.smframework.shader.ShaderProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class PrimitiveRect(director:IDirector, w:Float, h:Float, cx:Float, cy:Float, fill: Boolean=true) : DrawNode(director) {

    init {
        setProgramType(ShaderManager.ProgramType.Primitive)
        if (fill) {
            initRect(director, w, h, cx, cy)
        } else {
            initRectHollow(director, w, h, cx, cy)
        }
    }

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
        val program = useProgram()
        if (program!=null) {
            if ((program as ProgPrimitive).setDrawParam(_matrix, _v!!)) {
                GLES20.glDrawArrays(_drawMode, 0, _numVertices)
            }
        }
    }

}