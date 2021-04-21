package com.brokenpc.smframework.base.texture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLU
import android.opengl.GLUtils
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.util.BitmapLoader
import com.brokenpc.smframework.util.webp.WebPFactory
import kotlin.math.min

class FileTexture : Texture {
    private val _fileName:String
    private val _maxSideLength:Int
    private val _degrees:Int
    private var _isWebPFormat:Boolean = false
    constructor(director:IDirector, key: String, fileName: String, loadAsync: Boolean, listener: OnTextureAsyncLoadListener?, degrees:Int, maxSideLength:Int) : super(director, key, loadAsync, listener) {
        _fileName = fileName
        _maxSideLength = maxSideLength
        _degrees = degrees
        initTextureDimen(director.getContext())
    }

    constructor(director:IDirector, key: String, fileName: String, loadAsync: Boolean, listener: OnTextureAsyncLoadListener?, degrees:Int, maxSideLength:Int, srcTexture:Texture) : super(director, key, loadAsync, listener) {
        _fileName = fileName
        _maxSideLength = maxSideLength
        _degrees = degrees

        _width = srcTexture.getWidth()
        _height = srcTexture.getHeight()
        _originalWidth = _width
        _originalHeight = _height

        setId(srcTexture.getId())
        setValid(true)
    }

    fun getFileName(): String {return _fileName}

    override fun loadTexture(director: IDirector, bitmapt: Bitmap?): Boolean {
        var bitmap:Bitmap? = bitmapt
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
            if (!_doNotRecycleBitmap || !isAsyncLoader()) {
                bitmap.recycle()
            }
            true
        } else false
    }

    override fun initTextureDimen(context: Context) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(_fileName, options)

        if (options.outWidth<=0 || options.outHeight<=0) {
            setValid(true)
            return
        }

        var outWidth:Int = 0
        var outHeight:Int = 0

        if (_degrees==90 || _degrees==270) {
            outWidth = options.outHeight
            outHeight = options.outWidth
        } else {
            outWidth = options.outWidth
            outHeight = options.outHeight
        }

        _originalWidth = outWidth
        _originalHeight = outHeight

        if (_maxSideLength>0 && (outWidth>_maxSideLength || outHeight>_maxSideLength)) {
            val scale:Float = min(_maxSideLength/outWidth.toFloat(), _maxSideLength/outHeight.toFloat())
            outWidth = (outWidth*scale).toInt()
            outHeight = (outHeight*scale).toInt()
        }

        // 4 pixel 단위로 끝나야 한다.
        if (outWidth%4 != 0) {
            // 모자른 만큼 보충해준다.
            outWidth += 4-outWidth%4
        }

        _width = outWidth
        _height = outHeight

    }

    override fun loadTextureBitmap(context: Context): Bitmap? {
        var bitmap:Bitmap? = if (_isWebPFormat) {
            WebPFactory.decodeFileScaled(_fileName, _width, _height)
        } else {
            BitmapLoader.loadBitmap(context, _fileName, _degrees, _width, _height)
        }

        if (bitmap==null) {
            setValid(false)
        }

        return bitmap
    }

    override fun setDoNotRecycleBitmap(doNotRecyclebitmap: Boolean) {
        if (isAsyncLoader()) {
            _doNotRecycleBitmap = doNotRecyclebitmap
        }
    }

    fun setWebPFormat() {_isWebPFormat = true}
}