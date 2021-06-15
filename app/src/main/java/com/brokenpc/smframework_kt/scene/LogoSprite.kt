package com.brokenpc.smframework_kt.scene

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.sprite.Sprite3D
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.shader.ProgSprite3D
import com.brokenpc.smframework.shader.ShaderManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.*

class LogoSprite : Sprite {

    private var _inputMesh: FloatArray? = null
    private var _outputMesh: FloatArray? = null
    private var _colorUtil: FloatArray? = null
    private var _colorUtil2: FloatArray? = null

    private var _colors: FloatBuffer? = null
    private var _indice: ShortBuffer? = null
    private var _numFace: Int = 0

    private var _v2: FloatBuffer? = null
    private var _texture2: Texture? = null
    private var _colors2: FloatBuffer? = null

    private var _drawFront = false

    companion object {
        const val NUM_COL = 40
        const val NUM_ROW = 20
        const val BEND_SHADE = 0.3f // 구부러진 곳 음영
        const val BEND_RADIUS = 25f


        @JvmStatic
        fun QuadToTrianglesWindCCWSet(vertex: ShortBuffer, pos: Int, ul: Short, ur: Short, ll: Short, lr: Short) {
            vertex.position(pos)
            vertex.put(lr)
            vertex.put(ul)
            vertex.put(ll)
            vertex.put(lr)
            vertex.put(ur)
            vertex.put(ul)
        }

    }

    constructor(director: IDirector, texture: Texture, texture2: Texture?) : super(director) {
        this._texture = texture
        this._texture2 = texture2
        this._tx = 0f
        this._ty = 0f
        this._tw = 1f
        this._th = 1f
        this._contentSize.set(texture.getWidth(), texture.getHeight())
        this._cx = 0f
        this._cy = 0f

    }


    init {
        _numFace = NUM_COL * NUM_ROW * 2
        _numVertices = (NUM_COL+1) * (NUM_ROW+1)

        _inputMesh = FloatArray(_numVertices*3)
        _outputMesh = FloatArray(_numVertices*3)
        _colorUtil = FloatArray(_numVertices*4)
        _colorUtil2 = FloatArray(_numVertices*4)

        _v = ByteBuffer.allocateDirect(_numVertices*3*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _uv = ByteBuffer.allocateDirect(_numVertices*2*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _colors = ByteBuffer.allocateDirect(_numVertices*4*4).order(ByteOrder.nativeOrder()).asFloatBuffer()

        _v2 = ByteBuffer.allocateDirect(_numVertices*3*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _colors2 = ByteBuffer.allocateDirect(_numVertices*4*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        val numQuads = NUM_COL * NUM_ROW
        _numFace = numQuads * 2
        _indice = ByteBuffer.allocateDirect(_numFace*3*4).order(ByteOrder.nativeOrder()).asShortBuffer()


        var vi = 0
        for (yy in 0..NUM_ROW) {
            for (xx in 0..NUM_COL) {
                val px = (NUM_COL-xx) * _contentSize.width / NUM_COL
                val py = (NUM_ROW-yy) * _contentSize.height / NUM_ROW

                _inputMesh!![vi++] = px
                _inputMesh!![vi++] = py
                _inputMesh!![vi++] = 0f

                _uv?.put(_tx + _tw * (1-xx/NUM_COL))
                _uv?.put(_ty + _th * (1-yy/NUM_ROW))
            }
        }
        _v2!!.put(_inputMesh)
        _v2!!.position(0)
        _uv?.position(0)

        var ll: Short = 0
        var lr: Short = 0
        var ul: Short = 0
        var ur: Short = 0

        vi = 0

        for (index in 0 until numQuads) {
            val rowNum = (index/ NUM_COL)
            val colNum = index % NUM_COL

            ll = (rowNum * (NUM_COL+1) + colNum).toShort()
            lr = (ll + 1).toShort()
            ul = ((rowNum+1) * (NUM_COL+1) + colNum).toShort()
            ur = (ur+1).toShort()

            QuadToTrianglesWindCCWSet(_indice!!, vi, ul, ur, ll, lr)
            vi += 6
        }
        _indice!!.position(0)

        setProgramType(ShaderManager.ProgramType.Sprite3D)
    }

    fun transform(t: Float, angle: Float) {
        var x: Double
        var y: Double
        var z: Double

        var vi = 0
        val theta = PI/2 - angle*PI/4
        val sa = sin(PI/2+theta)
        val ca = cos(PI/2+theta)
        val tt = tan(theta)

        for (i in 0 until _numVertices) {
            val ix = _inputMesh!![vi++].toDouble()
            val iy = _inputMesh!![vi++].toDouble()
            vi++
            val cx = ((1-t)*_contentSize.width).toDouble()

            // 직선과 vertex 사이의 거리
            val d1 = ((tt*ix - iy - tt*cx) / sqrt(tt*tt+1)).toFloat()

            if (d1<0 || t<=0) {
                x = ix
                y = iy
                z = 0.0

                _colorUtil!![4 * i + 0] = 1f
                _colorUtil!![4 * i + 1] = 1f
                _colorUtil!![4 * i + 2] = 1f
                _colorUtil!![4 * i + 3] = 1f

                _colorUtil2!![4 * i + 0] = 1f
                _colorUtil2!![4 * i + 1] = 1f
                _colorUtil2!![4 * i + 2] = 1f
                _colorUtil2!![4 * i + 3] = 1f
            } else {
                val f = d1 / PI* BEND_RADIUS
                val beta = PI * f
                val d2 = d1 - BEND_RADIUS * sin(beta)

                x = ix + d2*ca
                y = iy + d2*sa
                z = (BEND_RADIUS + BEND_RADIUS * sin(beta-PI/2))

                var cl = (1- BEND_SHADE* sin(beta)).toFloat()

                _colorUtil!![4 * i + 0] = cl
                _colorUtil!![4 * i + 1] = cl
                _colorUtil!![4 * i + 2] = cl
                _colorUtil!![4 * i + 3] = 1f

                cl = (f*2).coerceAtMost(1.0).toFloat()
                _colorUtil2!![4 * i + 0] = cl
                _colorUtil2!![4 * i + 1] = cl
                _colorUtil2!![4 * i + 2] = cl
                _colorUtil2!![4 * i + 3] = cl
            }

            _outputMesh!![3 * i + 0] = x.toFloat()
            _outputMesh!![3 * i + 1] = y.toFloat()
            _outputMesh!![3 * i + 2] = z.toFloat()
        }

        _v?.put(_outputMesh)
        _v?.position(0)

        _colors!!.put(_colorUtil)
        _colors!!.position(0)
        _colors2!!.put(_colorUtil2)
        _colors2!!.position(0)
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program = useProgram()
        if (program!=null) {
            if (_drawFront && _texture!=null) {
                if ((program as ProgSprite3D).setDrawParam(_texture!!, _matrix, _v!!, _uv!!, _colors!!)) {
                    GLES20.glDrawElements(GLES20.GL_TRIANGLES, _numFace*3, GLES20.GL_UNSIGNED_SHORT, _indice)
                }
            } else if (!_drawFront && _texture2!=null) {
                if ((program as ProgSprite3D).setDrawParam(_texture2!!, _matrix, _v2!!, _uv!!, _colors2!!)) {
                    GLES20.glDrawElements(GLES20.GL_TRIANGLES, _numFace*3, GLES20.GL_UNSIGNED_SHORT, _indice)
                }
            }
        }
    }

    fun setDrawFront() {_drawFront = true}
    fun setDrawBack() {_drawFront = false}

    override fun removeTexture() {
        if (_texture!=null) {
            if (getDirector().getTextureManager().removeTexture(_texture!!)) {
                _texture = null
            }
        }

        if (_texture2!=null) {
            if (getDirector().getTextureManager().removeTexture(_texture2)) {
                _texture2 = null
            }
        }

        _inputMesh = null
        _outputMesh = null
        _colorUtil = null
        _colorUtil2 = null
        _colors = null
        _colors2 = null
        _v = null
        _uv = null
        _v2 = null
    }
}