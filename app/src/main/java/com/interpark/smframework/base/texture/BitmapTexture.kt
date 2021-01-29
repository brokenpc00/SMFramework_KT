package com.brokenpc.smframework.base.texture

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.brokenpc.smframework.IDirector

class BitmapTexture(director: IDirector, key: String, bitmap: Bitmap) : Texture(director, key, false, null) {

    private var _tempBitmap:Bitmap? = null

    init {
        _width = bitmap.width
        _height = bitmap.height
        _originalWidth = _width
        _originalHeight = _height
        _tempBitmap = bitmap
    }

    fun setBitmap(bitmap: Bitmap) {_tempBitmap = bitmap}

    override fun loadTexture(director: IDirector, bitmap: Bitmap?): Boolean {
        return if (_tempBitmap!=null && !_tempBitmap!!.isRecycled) {
            val bitmap: Bitmap = _tempBitmap!!
            _tempBitmap = null

            GLES20.glGenTextures(1, _textureId, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            true
        } else false
    }

    fun updateTexture(bitmap: Bitmap): Boolean {
        if (_textureId[0]== NO_TEXTURE) {
            GLES20.glGenTextures(1, _textureId, 0)
        }

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        return true
    }

    override fun loadTextureBitmap(context: Context): Bitmap? {
        return null
    }

    override fun initTextureDimen(context: Context) {

    }
}