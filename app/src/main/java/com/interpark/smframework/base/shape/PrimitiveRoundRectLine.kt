package com.interpark.smframework.base.shape

import android.opengl.GLES20
import com.interpark.smframework.IDirector
import com.interpark.smframework.base.DrawNode
import com.interpark.smframework.base.texture.Texture
import com.interpark.smframework.base.shape.ShapeConstant.LineType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI

class PrimitiveRoundRectLine(director:IDirector, texture: Texture, thickness: Float, lineType: LineType) : DrawNode(director) {
    protected var _uv:FloatBuffer
    private val _vertices:FloatArray
    private val _texcoord:FloatArray
    private var _lineType:LineType
    private var _thickness:Float

    companion object {
        const val CONER_SEGMENT:Int = 10
        const val NUM_VERTICES:Int = (CONER_SEGMENT+1)*2*4+2
    }

    init {
        _thickness = thickness
        _lineType = lineType
        _texture = texture
        _drawMode = GLES20.GL_TRIANGLE_STRIP
        _numVertices = NUM_VERTICES

        _vertices = FloatArray(NUM_VERTICES*2)
        _texcoord = FloatArray(NUM_VERTICES*2)

        _v = ByteBuffer.allocateDirect(_vertices.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _v!!.position(0)

        for (i in 0 until NUM_VERTICES*2 step 4) {
            _texcoord[i] = 0.5f
            _texcoord[i+1] = 0f
            _texcoord[i+2] = 0.5f
            _texcoord[i+3] = 1f
        }

        _uv = ByteBuffer.allocateDirect(_texcoord.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _uv.put(_texcoord)
        _uv.position(0)
    }

    fun setSize(width:Float, height:Float, cornerRadius:Float) {
        val inR:Float = cornerRadius - _thickness/2f
        val outR:Float = cornerRadius + _thickness/2f
        val w = width/2-cornerRadius
        val h = height/2-cornerRadius
        val textureRoundLength:Float = ((0.25f*2*cornerRadius*PI)/_thickness).toFloat()
        val textureWidthLength:Float = ((width-2*cornerRadius)/_thickness).toFloat()
        val textureHeightLength = ((height-2*cornerRadius)/_thickness).toFloat()
    }
}