package com.brokenpc.smframework.base.shape

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.shader.ProgPrimitiveTriangle
import com.brokenpc.smframework.shader.ShaderManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class PrimitiveTriangle(director:IDirector, width: Float, height: Float) : DrawNode(director) {
    protected val _uv:FloatBuffer
    private val _p0:Vec2 = Vec2(Vec2.ZERO)
    private val _p1:Vec2 = Vec2(Vec2.ZERO)
    private val _p2:Vec2 = Vec2(Vec2.ZERO)
    private var _aaWidth:Float = 0.015f

    init {
        _contentSize = Size(width, height)


        val v:FloatArray = floatArrayOf(
            0f, 0f,
            _contentSize.width, 0f,
            0f, _contentSize.height,
            _contentSize.width, _contentSize.height
        )

        _drawMode = GLES20.GL_TRIANGLE_STRIP
        _numVertices = 4

        _v = ByteBuffer.allocateDirect(v.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _v!!.put(v)
        _v!!.position(0)

        _uv = ByteBuffer.allocateDirect(v.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _uv.put(_texCoordConst[Quadrant_ALL])
        _uv.position(0)

        setProgramType(ShaderManager.ProgramType.PrimitiveTriangle)
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program = useProgram()
        if (program!=null && program.getType()==ShaderManager.ProgramType.PrimitiveTriangle) {
            if ((program as ProgPrimitiveTriangle).setDrawParam(_matrix, _v!!, _uv, _p0, _p1, _p2, _aaWidth)) {
                GLES20.glDrawArrays(_drawMode, 0, _numVertices)
            }
        }
    }

    fun drawTriangle(p0:Vec2, p1:Vec2, p2:Vec2, aaWidth:Float) {
        setProgramType(ShaderManager.ProgramType.PrimitiveTriangle)

        _p0.set(p0.x/_contentSize.width, p0.y/_contentSize.height)
        _p1.set(p1.x/_contentSize.width, p1.y/_contentSize.height)
        _p2.set(p2.x/_contentSize.width, p2.y/_contentSize.height)
        _aaWidth = aaWidth

        draw(0f, 0f)

    }
}