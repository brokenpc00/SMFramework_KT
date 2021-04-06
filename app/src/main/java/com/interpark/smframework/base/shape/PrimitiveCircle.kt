package com.brokenpc.smframework.base.shape

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.shader.ProgPrimitiveCircle
import com.brokenpc.smframework.shader.ProgPrimitiveRing
import com.brokenpc.smframework.shader.ShaderManager
import com.brokenpc.smframework.shader.ShaderManager.ProgramType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class PrimitiveCircle(director:IDirector) : DrawNode(director) {
    protected val _uv:FloatBuffer
    private var _radius:Float = 0f
    private var _thickness:Float = 0f
    private var _aaWidth:Float = 0f
    private var _anchor:Vec2 = Vec2(Vec2.MIDDLE)

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
        if (program != null) {
            when (program.getType()) {
                ProgramType.PrimitiveCircle -> if ((program as ProgPrimitiveCircle).setDrawParam(_matrix, _v!!, _uv, _radius, _aaWidth, _anchor)) {
                    GLES20.glDrawArrays(_drawMode, 0, _numVertices)
                }
                ProgramType.PrimitiveRing -> if ((program as ProgPrimitiveRing).setDrawParam(_matrix, _v!!, _uv, _radius, _thickness, _aaWidth, _anchor)) {
                    GLES20.glDrawArrays(_drawMode, 0, _numVertices)
                }
                else -> if ((program as ProgPrimitiveCircle).setDrawParam(_matrix, _v!!, _uv, _radius, _aaWidth, _anchor)) {
                    GLES20.glDrawArrays(_drawMode, 0, _numVertices)
                }
            }
        }
    }

    fun drawRing(x: Float, y: Float, radius: Float, thickness: Float) {
        drawRing(x, y, radius, thickness, 1.5f)
    }

    fun drawRing(x: Float, y: Float, radius: Float, thickness: Float, aaWidth: Float, anchor: Vec2) {
        _anchor.set(anchor)
        drawRing(x, y, radius, thickness, aaWidth)
    }

    fun drawRing(x: Float, y: Float, radius: Float, thickness: Float, aaWidth: Float) {
        setProgramType(ProgramType.PrimitiveRing)
        _radius = radius
        _thickness = thickness
        _aaWidth = aaWidth
        drawScale(x, y, radius)
    }

    fun drawCircle(x: Float, y: Float, radius: Float) {
        drawCircle(x, y, radius, 1.5f)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, aaWidth: Float, anchor: Vec2) {
        _anchor.set(anchor)
        drawCircle(x, y, radius, aaWidth)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, aaWidth: Float) {
        setProgramType(ProgramType.PrimitiveCircle)
        _radius = radius
        _aaWidth = aaWidth
        drawScale(x, y, radius)
    }

    fun drawRingRotateY(x: Float, y: Float, radius: Float, thickness: Float, aaWidth: Float, rotateY: Float) {
        setProgramType(ProgramType.PrimitiveRing)
        _radius = radius
        _thickness = thickness
        _aaWidth = aaWidth
        drawScaleXYRotateY(x, y, -radius, radius, rotateY)
    }

    fun drawCircleRotateY(x: Float, y: Float, radius: Float, aaWidth: Float, rotateY: Float) {
        setProgramType(ProgramType.PrimitiveCircle)
        _radius = radius
        _aaWidth = aaWidth
        drawScaleXYRotateY(x, y, -radius, radius, rotateY)
    }
}