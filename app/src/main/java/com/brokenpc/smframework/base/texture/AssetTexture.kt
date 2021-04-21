package com.brokenpc.smframework.base.texture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.util.IOUtils
import com.brokenpc.smframework.util.webp.WebPFactory
import java.io.InputStream
import java.lang.Exception

class AssetTexture : Texture {
    private var _fileName:String = ""
    constructor(director:IDirector, key: String, fileName: String, loadAsync: Boolean, listener: OnTextureAsyncLoadListener?) : super(director, key, loadAsync, listener) {
        _fileName = fileName
        initTextureDimen(director.getContext())
    }

    constructor(director: IDirector, key: String, fileName: String, loadAsync: Boolean, listener: OnTextureAsyncLoadListener?, srcTexture: Texture) : super(director, key, loadAsync, listener) {
        _fileName = fileName
        _width = srcTexture.getWidth()
        _height = srcTexture.getHeight()
        _originalWidth = _width
        _originalHeight = _height

        setId(srcTexture.getId())
        setValid(true)
    }

    constructor(director: IDirector, key: String, fileName: String, loadAsync: Boolean, listener: OnTextureAsyncLoadListener?, width:Int, height:Int) : super(director, key, loadAsync, listener) {
        _fileName = fileName

        _width = width
        _height = height
        _originalWidth = _width
        _originalHeight = _height

        setValid(true)
    }

    override fun loadTexture(director: IDirector, bitmapt: Bitmap?): Boolean {
        var bitmap:Bitmap? = bitmapt
        if (bitmap==null && !isAsyncLoader()) {
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
            return true
        } else false
    }

    override fun initTextureDimen(context: Context) {
        var IS:InputStream? = null
        val options = BitmapFactory.Options()
        try {
            IS = context.assets.open(_fileName)
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(IS, null, options)
        } catch (e:Exception) {
            setValid(false)
        } finally {
            IOUtils.closeSilently(IS)
        }

        _width = options.outWidth
        _height = options.outHeight
        _originalHeight = _width
        _originalHeight = _height
    }

    override fun loadTextureBitmap(context: Context): Bitmap? {
        var IS:InputStream? = null
        var bitmapt:Bitmap? = null
        try {
            IS = context.assets.open(_fileName)
            bitmapt = WebPFactory.decodeStream(IS)
        } catch (e:Exception) {
            setValid(false)
        } finally {
            IOUtils.closeSilently(IS)
        }

        return bitmapt
    }
}