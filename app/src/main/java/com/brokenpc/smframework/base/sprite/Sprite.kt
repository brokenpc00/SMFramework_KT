package com.brokenpc.smframework.base.sprite

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.shader.ProgSprite
import com.brokenpc.smframework.shader.ProgSpriteCircle
import com.brokenpc.smframework.shader.ShaderManager
import com.brokenpc.smframework.shader.ShaderManager.ProgramType
import com.brokenpc.smframework.shader.ShaderProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

open class Sprite : DrawNode {

    protected var _tx:Float = 0f
    protected var _ty:Float = 0f
    protected var _tw:Float = 0f
    protected var _th:Float = 0f
    protected var _uv:FloatBuffer? = null

    protected var _extra_divX1:Float = 0f
    protected var _extra_divX2:Float = 0f
    protected var _extra_v:FloatBuffer? = null
    protected var _extra_uv:FloatBuffer? = null
    protected var _extra_numVertices:Int = 0
    protected var _extra_drawMode:Int = GLES20.GL_TRIANGLE_STRIP

    constructor(director: IDirector) : super(director)

    constructor(director: IDirector, w:Float, h:Float, cx:Float, cy:Float, tx:Int, ty:Int, texture: Texture) : super(director) {
        initRect(director, w, h, cx, cy)
        setProgramType(ShaderManager.ProgramType.Sprite)

        _tx = tx.toFloat()
        _ty = ty.toFloat()
        _tw = texture.getWidth().toFloat()
        _th = texture.getHeight().toFloat()
        _texture = texture

        initTextureCoordQuard()
        texture.incRefCount()
    }

    constructor(director: IDirector, texture: Texture, cx: Float, cy: Float) : super(director) {
        val tw:Float = (texture.getWidth()/2).toFloat()
        val th:Float = (texture.getHeight()/2).toFloat()

        initRect(director, tw, th, cx, cy)
        setProgramType(ShaderManager.ProgramType.Sprite)

        _tx = 0f
        _ty = 0f
        _tw = tw
        _th = th
        _texture = texture

        initTextureCoordQuard()
        texture.incRefCount()
    }

    protected open fun initTextureCoordQuard() {
        val uv:FloatArray = floatArrayOf(
            _tx/_tw, _ty/_th,
            (_tx+_contentSize.width)/_tw, _ty/_th,
            _tx/_tw, (_ty+_contentSize.height)/_th,
            (_tx+_contentSize.width)/_tw, (_ty+_contentSize.height)/_th
        )

        _uv = ByteBuffer.allocateDirect(uv.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _uv!!.put(uv)
        _uv!!.position(0)
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program:ShaderProgram? = useProgram()
        if (program!=null && _texture!=null) {
            when (program.getType()) {
                ProgramType.SpriteCircle -> {
                    val cx:Float = (_tx + 0.5f * _contentSize.width) / _tw
                    val cy:Float = (_ty + 0.5f * _contentSize.height) / _th
                    val radius:Float = 0.5f * Math.min(_contentSize.width / _tw, _contentSize.height / _th)
                    val aaWidth:Float = 2.0f / _tw
                    if ((program as ProgSpriteCircle).setDrawParam(_texture!!, _matrix, _v!!, _uv!!, cx, cy, radius, aaWidth)) {
                        if (_setColor) {
                            getDirector().setColor(_color.r, _color.g, _color.b, _color.a)
                        }
                    }
                    GLES20.glDrawArrays(_drawMode, 0, _numVertices)
                }
                // 나중에 구현...
//                ProgramType.Bilateral ->
//                ProgramType.GeineEffect ->
//                ProgramType.GaussianBlur ->
                else -> {
                    if ((program as ProgSprite).setDrawParam(_texture!!, _matrix, _v!!, _uv!!)) {
                        if (_setColor) {
                            getDirector().setColor(_color.r, _color.g, _color.b, _color.a)
                        }
                        GLES20.glDrawArrays(_drawMode, 0, _numVertices)
                    }
                }
            }
        }
    }

    open fun removeTexture() {
        if (_texture!=null) {
            if (_director!!.getTextureManager().removeTexture(_texture)) {
                _texture = null
            }
        }
    }

    fun convertHotizontalResizable(div1:Float, div2:Float) {
        val v:FloatArray = floatArrayOf(
            -_cx, -_cy,
            -_cx, -_cy+_contentSize.height,
            -_cx+_contentSize.width*div1, -_cy,
            -_cx+_contentSize.width*div1, -_cy+_contentSize.height,
            -_cx+_contentSize.width*div1, -_cy,
            -_cx+_contentSize.width*div1, -_cy+_contentSize.height,
            -_cx+_contentSize.width*div2, -_cy,
            -_cx+_contentSize.width*div2, -_cy+_contentSize.height,
            -_cx+_contentSize.width*div2, -_cy,
            -_cx+_contentSize.width*div2, -_cy+_contentSize.height,
            -_cx+_contentSize.width,      -_cy,
            -_cx+_contentSize.width,      -_cy+_contentSize.height
        )

        val uv:FloatArray = floatArrayOf(
            (_tx        )/_tw, (_ty   )/_th,
            (_tx        )/_tw, (_ty+_contentSize.height)/_th,
            (_tx+_contentSize.width*div1)/_tw, (_ty   )/_th,
            (_tx+_contentSize.width*div1)/_tw, (_ty+_contentSize.height)/_th,
            (_tx+_contentSize.width*div1)/_tw, (_ty   )/_th,
            (_tx+_contentSize.width*div1)/_tw, (_ty+_contentSize.height)/_th,
            (_tx+_contentSize.width*div2)/_tw, (_ty   )/_th,
            (_tx+_contentSize.width*div2)/_tw, (_ty+_contentSize.height)/_th,
            (_tx+_contentSize.width*div2)/_tw, (_ty   )/_th,
            (_tx+_contentSize.width*div2)/_tw, (_ty+_contentSize.height)/_th,
            (_tx+_contentSize.width     )/_tw, (_ty   )/_th,
            (_tx+_contentSize.width     )/_tw, (_ty+_contentSize.height)/_th
        )

        _extra_numVertices = 12;
        _extra_divX1 = div1;
        _extra_divX2 = div2;

        var byteBuf:ByteBuffer = ByteBuffer.allocateDirect(v.size*4)
        byteBuf.order(ByteOrder.nativeOrder())
        _extra_v = byteBuf.asFloatBuffer()
        _extra_v!!.put(v)
        _extra_v!!.position(0)

        byteBuf = ByteBuffer.allocateDirect(uv.size*4)
        byteBuf.order(ByteOrder.nativeOrder())
        _extra_uv =  byteBuf.asFloatBuffer()
        _extra_uv!!.put(uv)
        _extra_uv!!.position(0)
    }

    protected fun _extra_draw(modelMatrix: FloatArray) {
        val program:ShaderProgram? = useProgram()
        if (program!= null && _texture!=null) {
            if (program.getType() == ShaderManager.ProgramType.Sprite) {
                if ((program as ProgSprite).setDrawParam(_texture!!, _matrix!!, _extra_v!!, _extra_uv!!)) {
                    if (_setColor) getDirector().setColor(_color.r, _color.g, _color.b, _color.a)
                    GLES20.glDrawArrays(_extra_drawMode, 0, _extra_numVertices)
                }
            }
        }
    }


}