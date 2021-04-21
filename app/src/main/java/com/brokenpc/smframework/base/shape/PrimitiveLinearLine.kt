package com.brokenpc.smframework.base.shape

import android.opengl.GLES20
import android.opengl.Matrix
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.shape.ShapeConstant.LineType
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.shader.ProgSprite
import com.brokenpc.smframework.shader.ShaderManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.atan2
import kotlin.math.sqrt

class PrimitiveLinearLine(director:IDirector, texture:Texture, thickness:Float, type:LineType) : DrawNode(director) {
    private var _type:LineType
    protected var _uv:FloatBuffer
    private var _thickness:Float = 1f

    companion object {
        const val DEFAULT_THICKNESS = 20f

        val _vertices = FloatArray(4*2)
        val _texCoord = FloatArray(4*2)
    }

    init {
        _texture = texture
        _type = type

        setProgramType(ShaderManager.ProgramType.Sprite)

        _numVertices = 4
        _drawMode = GLES20.GL_TRIANGLE_STRIP

        _thickness = if (thickness<=0) DEFAULT_THICKNESS else thickness

        _v = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _uv = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer()

        _vertices[0] = 0f
        _vertices[1] = -thickness/2
        _vertices[2] = thickness
        _vertices[3] = -thickness/2
        _vertices[4] = 0f
        _vertices[5] = thickness/2
        _vertices[6] = thickness
        _vertices[7] = thickness/2
        _v!!.put(_vertices).position(0)

        _texCoord[0] = 0f
        _texCoord[1] = 0f
        _texCoord[2] = 0.5f
        _texCoord[3] = 0f
        _texCoord[4] = 0f
        _texCoord[5] = 1f
        _texCoord[6] = 0.5f
        _texCoord[7] = 1f
        _uv.put(_texCoord).position(0)
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program = useProgram()
        if (program!=null) {
            if ((program as ProgSprite).setDrawParam(_texture!!, _matrix, _v!!, _uv)) {
                GLES20.glDrawArrays(_drawMode, 0, _numVertices)
            }
        }
    }

    fun drawLine(from:Vec2, to:Vec2) {
        drawLine(from.x, from.y, to.x, to.y)
    }

    fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) {
        val dx = x2-x1
        val dy = y2-y1
        val width:Float = sqrt(dx*dx+dy*dy)
        val angle:Float = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()

        _vertices[2] = width
        _vertices[6] = angle
        _v!!.put(_vertices).position(0)

        if (_type==LineType.DASH) {
            _texCoord[2] = width/_thickness
            _texCoord[6] = width/_thickness
            _uv.put(_texCoord).position(0)
        }

        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x1, y1, 0f)
        Matrix.rotateM(_matrix, 0, angle, 0f, 0f, 1f)

        _draw(_matrix)
    }

}