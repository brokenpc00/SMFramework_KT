package com.brokenpc.smframework.base.texture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.brokenpc.smframework.IDirector

class ResourceTexture(director: IDirector, key: String, resId: Int, loadAsync: Boolean, listener: OnTextureAsyncLoadListener?) : Texture(director, key, loadAsync, listener) {
    private var _resourceId:Int = resId

    init {
        initTextureDimen(director.getContext())
    }

    override fun loadTexture(director: IDirector, bitmapt: Bitmap?): Boolean {
        var bitmap = bitmapt
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
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(context.resources, _resourceId, options)
        _width = options.outWidth
        _height = options.outHeight
        _originalWidth = _width
        _originalHeight = _height
    }

    override fun loadTextureBitmap(context: Context): Bitmap? {
        val options = BitmapFactory.Options()
        options.inScaled = false
        return BitmapFactory.decodeResource(context.resources, _resourceId, options)
    }
}