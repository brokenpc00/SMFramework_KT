package com.brokenpc.smframework.base.sprite

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.shader.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import kotlin.math.*

class GridSprite(director:IDirector, texture: Texture, cx: Float, cy: Float, gridSize: Int) : Sprite(director) {
    private lateinit var _indices:ShortBuffer

    private var _bufferSize:Int = 0
    private var _numFace:Int = 0
    private var _genieMinimize:Float = 0f
    private var _genieBend = 0f
    private var _genieSide = 0f

    private var _genieAnchor = Vec2()
    private var _genieProgress = 0f


    protected var _gridSize = 0
    protected var _numCol:Int = 0
    protected var _numRow:Int = 0
    protected lateinit var _vertices:FloatArray


    companion object {
        const val DEFAULT_GRID_SIZE = 10

        @JvmStatic
        fun create(director: IDirector, sprite: Sprite): GridSprite? {
            return if (sprite is GridSprite) sprite else {
                val texture = sprite.getTexture()
                if (texture!=null) GridSprite(director, texture, 0f, 0f, DEFAULT_GRID_SIZE) else null
            }
        }

        @JvmStatic
        fun QuadToTrianglesWindCCWSet(vertex:ShortBuffer, pos:Int, ul:Short, ur:Short, ll:Short, lr:Short) {
            vertex.position(pos)
            vertex.put(lr)
            vertex.put(ul)
            vertex.put(ll)
            vertex.put(lr)
            vertex.put(ur)
            vertex.put(ul)
        }
    }

    init {
        val tw = texture.getWidth().toFloat()
        val th = texture.getHeight().toFloat()

        initRect(director, tw, th, cx, cy)
        setProgramType(ShaderManager.ProgramType.Sprite)

        _tx = 0f
        _ty = 0f
        _tw = tw
        _th = th
        _texture = texture
        _gridSize = max(10, gridSize)

        initTextureCoordQuard()

        texture.incRefCount()
    }

    override fun initTextureCoordQuard() {

        _drawMode = GLES20.GL_TRIANGLES

        _numCol = ceil(_contentSize.width/_gridSize.toFloat()).toInt()
        _numRow = ceil(_contentSize.height/_gridSize.toFloat()).toInt()
        _numVertices = (_numCol+1) * (_numRow+1)
        _bufferSize = _numVertices * 2

        _vertices = FloatArray(_bufferSize)
        val texCoord = FloatArray(_bufferSize)

        var idx = 0
        var xx = 0f
        var yy = 0f
        var uu = 0f
        var vv = 0f
        for (y in 0 .. _numRow) {
            if (y==_numRow) {
                yy = _contentSize.height
                vv = _contentSize.height/_th
            } else {
                yy = _gridSize * y.toFloat()
                vv = y.toFloat() * _gridSize / _contentSize.height
            }

            for (x in 0 .. _numCol) {
                if (x==_numCol) {
                    xx = _contentSize.width
                    uu = _contentSize.width/_tw
                } else {
                    xx = _gridSize * x.toFloat()
                    uu = x.toFloat() * _gridSize / _contentSize.width
                }

                _vertices[idx] = xx-_cx   // x coord
                _vertices[idx+1] = yy-_cy   // y coord
                texCoord[idx] = uu
                texCoord[idx+1] = vv

                idx += 2
            }
        }

        val FloatSize:Int = 32
        val ByteSize:Int = 8
        val ShortSize:Int = 16
        var size = _bufferSize * FloatSize / ByteSize
        _v = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _v?.put(_vertices)
        _v?.position(0)

        _uv = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _uv?.put(texCoord)
        _uv?.position(0)

        // create index buffer
        val numQuads = _numCol * _numRow
        _numFace = numQuads * 2

        size = _numFace * 3 * 2 * ShortSize / ByteSize
        _indices = ByteBuffer.allocateDirect(10).order(ByteOrder.nativeOrder()).asShortBuffer()

        var vi = 0  // vertex index
        var ll:Short
        var lr:Short
        var ul:Short
        var ur:Short

        for (index in 0 until numQuads) {
            val rowNum = index / _numCol
            val colNum = index % _numCol
            ll = (rowNum * (_numCol+1) + colNum).toShort()
            lr = (ll+1).toShort()
            ul = ((rowNum+1)*(_numCol+1)+colNum).toShort()
            ur = (ul+1).toShort()
            QuadToTrianglesWindCCWSet(_indices, vi, ul, ur, ll, lr)
            vi += 6
        }
        _indices.position(0)
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program:ShaderProgram? = useProgram()
        if (program!=null && _texture!=null) {
            when (program.getType()) {
                ShaderManager.ProgramType.GeineEffect -> {
                    if ((program as ProgGeineEffect).setDrawParam(_texture!!, _matrix, _v!!, _uv!!)) {
                        if (_setColor) {
                            getDirector().setColor(_color.r, _color.g, _color.b, _color.a)
                        }
                        (program as ProgGeineEffect).setGeineValue(_genieMinimize, _genieBend, _genieSide)
                        GLES20.glDrawElements(GLES20.GL_TRIANGLES, _numFace*3, GLES20.GL_UNSIGNED_SHORT, _indices)
                    }
                }
                ShaderManager.ProgramType.GeineEffect2 -> {
                    if ((program as ProgGeineEffect2).setDrawParam(_texture!!, _matrix, _v!!, _uv!!)) {
                        if (_setColor) {
                            getDirector().setColor(_color.r, _color.g, _color.b, _color.a)
                        }
                        (program as ProgGeineEffect2).setGeineValue(_genieAnchor, _genieProgress)
                        GLES20.glDrawElements(GLES20.GL_TRIANGLES, _numFace*3, GLES20.GL_UNSIGNED_SHORT, _indices)
                    }
                }
                else -> {
                    if ((program as ProgSprite).setDrawParam(_texture!!, _matrix, _v!!, _uv!!)) {
                        if (_setColor) {
                            getDirector().setColor(_color.r, _color.g, _color.b, _color.a)
                        }
                        GLES20.glDrawElements(GLES20.GL_TRIANGLES, _numFace*3, GLES20.GL_UNSIGNED_SHORT, _indices)
                    }
                }
            }
        }
    }

    fun getNumCols():Int {return _numCol}
    fun getNumRows():Int {return _numRow}
    fun getVertexBuffer():FloatArray {return _vertices}
    fun getNumFaces():Int {return _numFace}
    fun getIndices():ShortBuffer {return _indices}
    fun setGenieAnchor(v:Vec2) {_genieAnchor.set(v)}
    fun setGenieProgress(f:Float) {_genieProgress=f}
    fun setGeineValue(minimize:Float, bend:Float, side:Float) {
        _genieMinimize = minimize
        _genieBend = bend
        _genieSide = side
    }

    fun grow(px:Float, py:Float, value:Float, step:Float, radius:Float) {
        var sx:Float
        var sy:Float
        var dx:Float
        var dy:Float

        var r:Float
        var idx:Int
        val grouStep = abs(value*step)

        idx = 0
        for (y in 0 .. _numRow) {
            sy = if (y==_numRow) {
                _contentSize.height - _cy
            } else {
                _gridSize * y - _cy
            }
            for (x in 0 .. _numCol) {
                sx = if (x==_numCol) {
                    _contentSize.width - _cx
                } else {
                    _gridSize * x - _cx
                }

                dx = sx - px
                dy = sy - py
                r = sqrt(dx*dx+dy*dy) / radius
                r = r.pow(grouStep)

                if (value>0 && r>0.001f) {
                    _vertices[idx] = px + dx/r
                    _vertices[idx+1] = py + dy/r
                } else if (value<0 && r<0.001f) {
                    _vertices[idx] = px + dx*(r)
                    _vertices[idx+1] = py + py*(r)
                } else {
                    _vertices[idx] = sx
                    _vertices[idx+1] = sy
                }
                idx += 2
            }
        }
        _v!!.put(_vertices)
        _v!!.position(0)
    }
}