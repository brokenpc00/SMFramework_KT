package com.brokenpc.smframework.base.shape

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.shape.ShapeConstant.LineType
import com.brokenpc.smframework.shader.ProgSprite
import com.brokenpc.smframework.shader.ShaderManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

open class PrimitiveRoundRectLine(director:IDirector, texture: Texture?, thickness: Float, lineType: LineType) : DrawNode(director) {
    protected var _uv:FloatBuffer
    private val _vertices:FloatArray
    private val _texcoord:FloatArray
    private var _lineType:LineType
    private var _thickness:Float

    companion object {
        const val CORNER_SEGMENT:Int = 10
        const val NUM_VERTICES:Int = (CORNER_SEGMENT+1)*2*4+2
    }

    init {
        _texture = texture

        setProgramType(ShaderManager.ProgramType.Sprite)

        _numVertices = NUM_VERTICES
        _drawMode = GLES20.GL_TRIANGLE_STRIP
        _lineType = lineType
        _thickness = thickness

        _vertices = FloatArray(NUM_VERTICES*2)
        _texcoord = FloatArray(NUM_VERTICES*2)

        _v = ByteBuffer.allocateDirect(_vertices.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _v!!.position(0)

        _uv = ByteBuffer.allocateDirect(_texcoord.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _uv.position(0)

        for (i in 0 until NUM_VERTICES*2 step 4) {
            _texcoord[i] = 0.5f
            _texcoord[i+1] = 0f
            _texcoord[i+2] = 0.5f
            _texcoord[i+3] = 1f
        }

        _uv.put(_texcoord)
        _uv.position(0)
    }

    fun setSize(width:Float, height:Float, cornerRadius:Float) {
        val inR = cornerRadius - _thickness/2f
        val outR = cornerRadius + _thickness/2f
        val w = width/2f-cornerRadius
        val h = height/2f-cornerRadius
        val textureRoundLength = (0.25f*2f*cornerRadius*PI).toFloat()/_thickness
        val textureWidthLength = ((width-2f*cornerRadius)/_thickness)
        val textureHeightLength = ((height-2f*cornerRadius)/_thickness)
        val stepRoundLength = textureRoundLength / CORNER_SEGMENT

        var index = 0
        var tu = 0f
        for (i in 0 .. CORNER_SEGMENT) {
            val rad = (i* PI*0.5f/ CORNER_SEGMENT).toFloat()
            val ca:Float = cos(rad)
            val sa:Float = sin(rad)

            val inA = inR*ca
            val inB = inR*sa
            val outA = outR*ca
            val outB = outR*sa

            // left top
            index = i*4
            _vertices[index  ] = -w-inA
            _vertices[index+1] = -h-inB
            _vertices[index+2] = -w-outA
            _vertices[index+3] = -h-outB
            if (_lineType==LineType.DASH) {
                tu = i*stepRoundLength
                _texcoord[index] = tu
                _texcoord[index+2] = tu
            }

            // right top
            index += (CORNER_SEGMENT+1)*4
            _vertices[index  ] = +w+inB
            _vertices[index+1] = -h-inA
            _vertices[index+2] = +w+outB
            _vertices[index+3] = -h-outA
            if (_lineType==LineType.DASH) {
                tu += textureWidthLength+textureRoundLength
                _texcoord[index] = tu
                _texcoord[index+2] = tu
            }

            // right bottom
            index += (CORNER_SEGMENT+1)*4
            _vertices[index  ] = +w+inA
            _vertices[index+1] = +h+inB
            _vertices[index+2] = +w+outA
            _vertices[index+3] = +h+outB
            if (_lineType==LineType.DASH) {
                tu += textureHeightLength+textureRoundLength
                _texcoord[index] = tu
                _texcoord[index+2] = tu
            }

            // left bottom
            index += (CORNER_SEGMENT+1)*4
            _vertices[index  ] = -w-inB
            _vertices[index+1] = +h+inA
            _vertices[index+2] = -w-outB
            _vertices[index+3] = +h+outA
            if (_lineType==LineType.DASH) {
                tu += textureWidthLength+textureRoundLength
                _texcoord[index] = tu
                _texcoord[index+2] = tu
            }
        }
        index += 4
        _vertices[index  ] = _vertices[0]
        _vertices[index+1] = _vertices[1]
        _vertices[index+2] = _vertices[2]
        _vertices[index+3] = _vertices[3]
        if (_lineType==LineType.DASH) {
            tu += textureHeightLength
            _texcoord[index] = tu
            _texcoord[index+2] = tu
        }

        _v!!.put(_vertices)
        _v!!.position(0)
        if (_lineType==LineType.DASH) {
            _uv.put(_texcoord)
            _uv.position(0)
        }
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program = useProgram()
        if (program!=null) {
            if ((program as ProgSprite).setDrawParam(_texture!!, _matrix, _v!!, _uv)) {
                GLES20.glDrawArrays(_drawMode, 0, _numVertices)
            }
        }
    }
}