package com.brokenpc.smframework.base.shape

import android.opengl.GLES20
import android.opengl.Matrix
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.base.shape.ShapeConstant.LineType
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.shader.ProgSprite
import com.brokenpc.smframework.shader.ShaderManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class PrimitiveCircleLine(director:IDirector, texture: Texture, radius:Float, thickness:Float, type:LineType) : DrawNode(director) {
    protected var _uv:FloatBuffer
    protected var _thickness:Float

    companion object {
        const val NUM_SEGMENT = 100
        const val DEFAULT_THICKNESS = 20f
    }

    init {
        _texture = texture

        setProgramType(ShaderManager.ProgramType.Sprite)
        _drawMode = GLES20.GL_TRIANGLE_STRIP

        _thickness = if (thickness<0) DEFAULT_THICKNESS else thickness

        val vertices:FloatArray = FloatArray(_numVertices*2)
        val texCoord:FloatArray = FloatArray(_numVertices*2)
        var idx:Int = 0

        val inRadius:Float = radius - _thickness/2f
        val outRadius:Float = radius + _thickness/2f

        var uu:Float = 0f
        if (type == LineType.SOLID) {
            // solid line
            uu = 0.5f
            for (i in 0 until NUM_SEGMENT) {
                val r:Float = (i*2* PI/ NUM_SEGMENT).toFloat()
                val ca:Float = cos(r)
                val sa:Float = sin(r)

                vertices[idx+0] = inRadius * ca
                vertices[idx+1] = inRadius * sa
                vertices[idx+2] = outRadius * ca
                vertices[idx+3] = outRadius + sa
                texCoord[idx+0] = uu
                texCoord[idx+1] = 0f
                texCoord[idx+1] = uu
                texCoord[idx+1] = 1f
                idx += 4
            }
        } else {
            // dashed line
            uu = 0f
            val ud:Float = (((2f*radius*PI)/ NUM_SEGMENT)/_thickness).toFloat()
            for (i in 0 until NUM_SEGMENT) {
                val r:Float = (i*2*PI/ NUM_SEGMENT).toFloat()
                val ca:Float = cos(r)
                val sa:Float = sin(r)

                vertices[idx+0] = inRadius * ca
                vertices[idx+1] = inRadius * sa
                vertices[idx+2] = outRadius * ca
                vertices[idx+3] = outRadius + sa
                texCoord[idx+0] = uu
                texCoord[idx+1] = 0f
                texCoord[idx+1] = uu
                texCoord[idx+1] = 1f
                idx += 4
                uu += ud*1.5f
            }
        }

        vertices[idx + 0] = vertices[0]
        vertices[idx + 1] = vertices[1]
        vertices[idx + 2] = vertices[2]
        vertices[idx + 3] = vertices[3]
        texCoord[idx + 0] = uu
        texCoord[idx + 1] = 0f
        texCoord[idx + 2] = uu
        texCoord[idx + 3] = 1f

        _v = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _v!!.put(vertices)
        _v!!.position(0)

        _uv = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _uv.put(texCoord)
        _uv.position(0)
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program = useProgram()
        if (program!=null) {
            if ((program as ProgSprite).setDrawParam(_texture!!, _matrix, _v!!, _uv)) {
                GLES20.glDrawArrays(_drawMode, 0, _numVertices)
            }
        }
    }

    fun _drawPercent(modelMatrix: FloatArray, start:Int, end:Int) {
        val program = useProgram()
        if (program!=null) {
            if ((program as ProgSprite).setDrawParam(_texture!!, _matrix, _v!!, _uv)) {
                GLES20.glDrawArrays(_drawMode, start*2, (end-start)*2)
            }
        }
    }

    fun drawPie(x:Float, y:Float, _start: Int, _end: Int) {
        var start:Int = _start*_numVertices/2
        var end:Int = _end*_numVertices*2

        if (start>end) {
            val t = start
            start = end
            end = t
        }

        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        _drawPercent(_matrix, start, end)
    }

    fun drawPieScaleXY(x:Float, y:Float, _start: Int, _end: Int, scaleX:Float, scaleY:Float) {
        var start = (2 * (_start*(_numVertices/2))) % _numVertices
        var end = (2 * (_end*(_numVertices/2))) % _numVertices
        if (start==end) return
        if (end>start) {
            val t = start
            start = end
            end = t
        }

        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.scaleM(_matrix, 0, scaleX, scaleY, 1f)
        _drawPercent(_matrix, _start, _end)
    }
}