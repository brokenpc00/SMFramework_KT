package com.interpark.smframework.base

import android.opengl.GLES20
import android.opengl.Matrix
import com.interpark.smframework.IDirector
import com.interpark.smframework.base.texture.Texture
import com.interpark.smframework.base.types.Color4F
import com.interpark.smframework.base.types.Ref
import com.interpark.smframework.base.types.Size
import com.interpark.smframework.shader.ShaderManager.ProgramType
import com.interpark.smframework.shader.ShaderProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

open abstract class DrawNode(director: IDirector) : Ref(director) {

    companion object {
        val _matrix: FloatArray = FloatArray(16)

        val _texCoordConst: ArrayList<FloatArray> = arrayListOf(
            floatArrayOf(  0f,   0f,   1f,   0f,   0f,   1f,   1f,   1f), // ALL
            floatArrayOf(  0f,   0f, 0.5f,   0f,   0f,   1f, 0.5f,   1f), // LEFT_HALF
            floatArrayOf(0.5f,   0f,   1f,   0f, 0.5f,   1f,   1f,   1f), // RIGHT_HALF
            floatArrayOf(  0f, 0.5f,   1f, 0.5f,   0f,   1f,   1f,   1f), // TOP_HALF
            floatArrayOf(  0f,   0f,   1f,   0f,   0f, 0.5f,   1f, 0.5f), // BOTTOM_HALF
            floatArrayOf(  0f, 0.5f, 0.5f, 0.5f,   0f,   1f, 0.5f,   1f), // LEFT_TOP
            floatArrayOf(  0f,   0f, 0.5f,   0f,   0f, 0.5f, 0.5f, 0.5f), // LEFT_BOTTOM
            floatArrayOf(0.5f, 0.5f,   1f, 0.5f, 0.5f,   1f,   1f,   1f), // RIGHT_TOP
            floatArrayOf(0.5f,   0f,   1f,   0f, 0.5f, 0.5f,   1f, 0.5f) // RIGHT_BOTTOM
        )

        const val Quadrant_ALL:Int = 0
        const val Quadrant_LEFT_HALF:Int = 1
        const val Quadrant_RIGHT_HALF:Int = 2
        const val Quadrant_TOP_HALF:Int = 3
        const val Quadrant_BOTTOM_HALF:Int = 4
        const val Quadrant_LEFT_TOP:Int = 5
        const val Quadrant_LEFT_BOTTOM:Int = 6
        const val Quadrant_RIGHT_TOP:Int = 7
        const val Quadrant_RIGHT_BOTTOM:Int = 8

    }

    fun getContentSize():Size {return _contentSize}
    fun getWidth():Float {return _contentSize.width}
    fun getHeight():Float {return _contentSize.height}
    fun getNumVertices():Int {return _numVertices}
    fun getTexture():Texture? {return _texture}
    fun setTexture(texture: Texture) {_texture = texture}
    fun setProgramType(programType: ProgramType) {_programType = programType}
    protected fun useProgram():ShaderProgram? {return _director!!.useProgram(_programType!!)}

    fun initRect(director: IDirector, w:Float, h:Float, cx:Float, cy:Float) {
        this._director = director
        _contentSize = Size(w, h)
        _cx = cx
        _cy = cy

        initVertexQuad()
    }

    protected fun initVertexQuad() {
        val v:FloatArray = floatArrayOf(
            -_cx, -_cy,
            -_cx+_contentSize.width, -_cy,
            -_cx, -_cy+_contentSize.height,
            -_cx+_contentSize.width, -_cy+_contentSize.height
        )

        _drawMode = GLES20.GL_TRIANGLE_STRIP
        _numVertices = 4

        this._v = ByteBuffer.allocateDirect(v.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        this._v?.put(v)
        this._v?.position(0)
    }

    protected abstract fun _draw(modelMatrix:FloatArray)

    fun draw(x:Float, y:Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)

        _draw(_matrix)
    }

    fun drawScale(x: Float, y: Float, scale: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.scaleM(_matrix, 0, scale, scale, 1f)

        _draw(_matrix)
    }

    fun drawScale(x: Float, y: Float, z: Float, scale: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, z)
        Matrix.scaleM(_matrix, 0, scale, scale, 1f)

        _draw(_matrix)
    }

    fun drawScaleXY(x: Float, y: Float, scaleX: Float, scaleY: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.scaleM(_matrix, 0, scaleX, scaleY, 1f)

        _draw(_matrix)
    }

    fun drawScaleXY(x: Float, y: Float, z: Float, scaleX: Float, scaleY: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, z)
        Matrix.scaleM(_matrix, 0, scaleX, scaleY, 1f)

        _draw(_matrix)
    }

    fun drawRotate(x: Float,y: Float, angle: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.rotateM(_matrix, 0, angle, 0f, 0f, 1f)

        _draw(_matrix)
    }

    fun drawRotateX(x: Float, y: Float, angle: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.rotateM(_matrix, 0, angle, 1f, 0f, 1f)

        _draw(_matrix)
    }

    fun drawScaleRotate(x: Float, y: Float, scale: Float, angle: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.scaleM(_matrix, 0, scale, scale, 1f)
        Matrix.rotateM(_matrix, 0, angle, 0f, 0f, 1f)

        _draw(_matrix)
    }

    fun drawScaleRotateX(x: Float, y: Float, scale: Float, angle: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.scaleM(_matrix, 0, scale, scale, 1f)
        Matrix.rotateM(_matrix, 0, angle, 1f, 0f, 0f)

        _draw(_matrix)
    }

    fun drawScaleXYRotate(x: Float, y: Float, scaleX: Float, scaleY: Float, angle: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.rotateM(_matrix, 0, angle, 0f, 0f, 1f)
        Matrix.scaleM(_matrix, 0, scaleX, scaleY, 1f)

        _draw(_matrix)
    }

    fun drawScaleXYRotateY(x: Float, y: Float, scaleX: Float, scaleY: Float, angle: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.rotateM(_matrix, 0, angle, 0f, 1f, 0f)
        Matrix.scaleM(_matrix, 0, scaleX, scaleY, 1f)

        _draw(_matrix)
    }

    fun drawRotateXYZ(x: Float, y: Float, z: Float, angle: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, z)
        Matrix.rotateM(_matrix, 0, angle, 1f, 1f, 1f)

        _draw(_matrix)
    }

    fun drawRotateY(x: Float, y: Float, angle: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.rotateM(_matrix, 0, angle, 0f, 1f, 0f)

        _draw(_matrix)
    }

    fun drawRotateZ(x: Float, y: Float, angle: Float) {
        Matrix.setIdentityM(_matrix, 0)
        Matrix.translateM(_matrix, 0, x, y, 0f)
        Matrix.rotateM(_matrix, 0, angle, 0f, 0f, 1f)

        _draw(_matrix)
    }

    fun drawMatrix(matrix:FloatArray) {_draw(matrix)}

    fun releaseResources() {
        if (_texture!=null) {
            _director?.getTextureManager()!!.removeTexture(_texture)
            _texture = null
        }
    }

    fun setOpacity(opacity:Int) {
        var alpha:Int = opacity
        if (alpha>0xff) alpha = 0xff
        if (alpha<0) alpha = 0

        _color.a = alpha.toFloat()/255.0f
    }

    fun setAlpha(alpha:Float) {_color.a = alpha}

    fun setColor(color: Color4F) {
        _setColor = true
        _color.set(color)
    }

    protected var _texture:Texture? = null
    protected var _contentSize:Size = Size(Size.ZERO)
    protected var _cx:Float = 0f
    protected var _cy:Float = 0f

    protected var _v:FloatBuffer? = null
    protected var _numVertices:Int = 0
    protected var _drawMode:Int = GLES20.GL_TRIANGLE_STRIP

    protected var _programType:ProgramType? = null
    protected var _setColor:Boolean = false
    protected var _color:Color4F = Color4F(1f, 1f, 1f, 0f)
}