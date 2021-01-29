package com.brokenpc.smframework.base.shape

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.shader.ProgPrimitiveSolidRect
import com.brokenpc.smframework.shader.ProgSprite
import com.brokenpc.smframework.shader.ShaderManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.max

class PrimitiveSolidRect(director:IDirector) : DrawNode(director) {
    protected val _uv:FloatBuffer
    private var _round:Float = 0f
    private var _aaWidth:Float = 0f
    private var  _width:Float = 0f
    private var _height:Float = 0f

    init {
        _drawMode = GLES20.GL_TRIANGLE_STRIP
        _numVertices = 4

        val v:FloatArray = floatArrayOf(
            -1f, 1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
        )

        _v = ByteBuffer.allocateDirect(v.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _v!!.put(v)
        _v!!.position(0)

        _uv = ByteBuffer.allocateDirect(v.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _uv.put(_texCoordConst[Quadrant_ALL])
        _uv.position(0)

        setProgramType(ShaderManager.ProgramType.PrimitiveSolidRect)
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program = useProgram()
        if (program!=null) {
            if (program.getType()==ShaderManager.ProgramType.PrimitiveSolidRect) {
                if ((program as ProgPrimitiveSolidRect).setDrawParam(_matrix, _v!!, _uv, _width, _height, _round, _aaWidth)) {
                    GLES20.glDrawArrays(_drawMode, 0, _numVertices)
                }
            }
        }
    }

    fun draawRect(x:Float, y:Float, width:Float, height:Float, round:Float, aaWidth:Float) {
        setProgramType(ShaderManager.ProgramType.PrimitiveSolidRect)
        _width = width
        _height = height
        _round = round
        _aaWidth = aaWidth
//        drawScale(x, y, max(width, height))
        drawScaleXY(x, y, width, height)
    }
}