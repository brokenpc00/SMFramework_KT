package com.brokenpc.smframework.base.texture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.opengl.GLES20
import android.opengl.GLUtils
import com.brokenpc.smframework.IDirector

class DrawableTexture(director: IDirector, key: String, drawable: Drawable, loadAsync: Boolean, listener: OnTextureAsyncLoadListener?) : Texture(director, key, loadAsync, listener) {
    private val _drawable:Drawable = drawable

    init {
        initTextureDimen(director.getContext())
    }

    override fun loadTexture(director: IDirector, bitmap_: Bitmap?): Boolean {
        var bitmap:Bitmap? = bitmap_
        if (bitmap==null) {
            bitmap = loadTextureBitmap(director.getContext())
        }
        return if (bitmap!=null) {
            GLES20.glGenTextures(1, _textureId, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
            true
        } else false
    }

    override fun initTextureDimen(context: Context) {
        if (_drawable is BitmapDrawable) {
            val bmp = (_drawable as BitmapDrawable).bitmap
            _originalWidth = bmp.width
            _originalHeight = bmp.height
        } else {
            val width:Int = _drawable.intrinsicWidth
            _width = if (width>0) width else 1
            _originalWidth = _width
            val height:Int = _drawable.intrinsicHeight
            _height = if (height>0) height else 1
            _originalHeight = _height
        }
    }

    override fun loadTextureBitmap(context: Context): Bitmap? {
        val bmp = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        _drawable.setBounds(0, 0, _width, _height)
        _drawable.draw(canvas)

        return bmp
    }
}