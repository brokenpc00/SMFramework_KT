package com.brokenpc.smframework.base.shape

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.shader.ProgPrimitiveRing
import com.brokenpc.smframework.shader.ProgSprite
import com.brokenpc.smframework.shader.ShaderManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

open class PrimitiveRing(director:IDirector) : DrawNode(director) {
    protected var _uv:FloatBuffer
    private var _radius:Float = 0f
    private var _thickness:Float = 0f
    private val _anchor:Vec2 = Vec2(Vec2.ZERO)

    init {
        _drawMode = GLES20.GL_TRIANGLE_STRIP
        _numVertices = 4

        val v:FloatArray = floatArrayOf(
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
        )

        _v = ByteBuffer.allocateDirect(v.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _v!!.put(v)
        _v!!.position(0)

        _uv = ByteBuffer.allocateDirect(v.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _uv.put(_texCoordConst[Quadrant_ALL])
        _uv.position(0)

        setProgramType(ShaderManager.ProgramType.PrimitiveRing)
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program = useProgram()
        if (program!=null) {
            if ((program as ProgPrimitiveRing).setDrawParam(_matrix, _v!!, _uv, _radius, _thickness, 1.5f, _anchor)) {
                GLES20.glDrawArrays(_drawMode, 0, _numVertices)
            }
        }
    }

    fun drawRing(x: Float, y: Float, radius: Float, thickness: Float) {
        _radius = radius
        _thickness = thickness
        drawScale(x, y, radius)
    }
}