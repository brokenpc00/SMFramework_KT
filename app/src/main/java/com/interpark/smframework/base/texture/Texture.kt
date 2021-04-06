package com.brokenpc.smframework.base.texture

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.util.SMAsyncTask
import java.lang.ref.WeakReference
import java.sql.Ref

abstract class Texture(director: IDirector, key: String, loadAsync: Boolean, listener: OnTextureAsyncLoadListener?) {
    protected var _director:IDirector

    companion object {
        const val NO_TEXTURE = -1
    }

    private var _refCount:Int = 0
    private var _isValid:Boolean = true

    interface OnTextureAsyncLoadListener {
        fun onTextureLoaded(texture: Texture?)
        fun onTextureLoadedBitmap(bitmap: Bitmap?)
    }

    private var _asyncLoadListener:OnTextureAsyncLoadListener? = listener
    var _asyncTask:TextureLoaderTask? = null

    interface TextureLoader {
        fun onCreateOrUpdateTexture(director: IDirector, bitmap: Bitmap?):Boolean
    }

    private var _textureLoader:TextureLoader? = null

    fun setOnTextureLoader(loader: TextureLoader?) {_textureLoader = loader}

    private var _key:String = key
    protected var _textureId:IntArray = IntArray(1)
    protected var _width:Int = 0
    protected var _height:Int = 0
    protected var _originalWidth:Int = 0
    protected var _originalHeight:Int = 0

    private var _loadAsync:Boolean = loadAsync
    protected var _doNotRecycleBitmap:Boolean = false

    init {
        _textureId[0] = NO_TEXTURE
        _director = director
    }

    fun isAsyncLoader():Boolean {return _loadAsync}
    open fun isLoading():Boolean {return _asyncTask!=null}
    fun getKey():String {return _key}
    fun getId():Int {return _textureId[0]}
    fun getIdRef():IntArray {return _textureId}
    fun setId(textureId:Int) {_textureId[0] = textureId}
    fun getWidth():Int {return _width}
    fun getHeight():Int {return _height}

    fun getUnscaledWidth():Int {return _originalWidth}
    fun getUnscaledHeight():Int {return _originalHeight}

    fun incRefCount() {_refCount++}
    fun decRefCount():Int {return --_refCount}

    fun getRefCount():Int {return _refCount}

    open fun deleteTexture(isGLThread:Boolean) {
        if (_asyncTask!=null) {
            _asyncTask?.stop()
            _asyncTask = null
        }

        if (_textureId[0]!= NO_TEXTURE) {
            if (isGLThread) {
                GLES20.glDeleteTextures(1, _textureId, 0)
            }
            _textureId[0] = NO_TEXTURE
        }
    }

    fun isValid():Boolean {return _isValid}
    fun setValid(valid:Boolean) {_isValid = valid}



    abstract fun loadTexture(director: IDirector, bitmap: Bitmap?):Boolean
    open fun loadTextureAsync(director: IDirector) {
        if (_asyncTask!=null && !_asyncTask!!.isCancelled()) {
            _asyncTask?.stop()
            _asyncTask = null
        }

        _asyncTask = TextureLoaderTask(director, _asyncLoadListener!!)
        _asyncTask!!.executeOnExecutor(SMAsyncTask.THREAD_POOL_EXECUTOR)
    }

    protected abstract fun initTextureDimen(context: Context)
    abstract fun loadTextureBitmap(context: Context): Bitmap?

    inner class TextureLoaderTask(director: IDirector, listener: OnTextureAsyncLoadListener) : SMAsyncTask<Void?, Void?, Bitmap?>(director) {
        private var _weakReference:WeakReference<OnTextureAsyncLoadListener>? = WeakReference(listener)
        private var _cancelled:Boolean = false

        fun stop() {
            if (_weakReference!=null) {
                val l:OnTextureAsyncLoadListener = _weakReference!!.get()!!
                _weakReference!!.clear()
            }

            _cancelled = true
            cancel(true)
        }

        override fun doInBackground(vararg params: Void?): Bitmap? {
            if (!_cancelled && !isCancelled()) {
                return loadTextureBitmap(getDirector().getContext())
            }
            return null
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            _asyncTask = null
            if (_cancelled || isCancelled()) {
                if (bitmap!=null && !bitmap.isRecycled) {
                    bitmap.recycle()
                }
                return
            }

            val l:OnTextureAsyncLoadListener? = _weakReference?.get()

            if (createOrUpdate(getDirector(), bitmap)) {
                l?.onTextureLoaded(this@Texture)
                if (_doNotRecycleBitmap) {
                    l?.onTextureLoadedBitmap(bitmap)
                }
            } else {
                l?.onTextureLoaded(null)
            }
        }
    }

    fun createOrUpdate(director: IDirector, bitmap: Bitmap?):Boolean {
        if (_textureLoader!=null) {
            return _textureLoader!!.onCreateOrUpdateTexture(director, bitmap)
        }

        return loadTexture(director, bitmap)
    }

    open fun setDoNotRecycleBitmap(doNotRecyclebitmap:Boolean) {}
}