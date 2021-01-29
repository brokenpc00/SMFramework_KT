package com.brokenpc.smframework.base.texture

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.android.volley.Response
import com.android.volley.VolleyError
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.PERFORM_SEL
import com.brokenpc.smframework.util.BitmapLoader
import com.brokenpc.smframework.util.IOUtils.Companion.closeSilently
import com.brokenpc.smframework.util.KeyGenerateUtil
import com.brokenpc.smframework.util.KeyGenerateUtil.Companion.generate
import com.brokenpc.smframework.util.NetworkStreamRequest
import com.brokenpc.smframework.util.SMAsyncTask
import java.io.*
import java.lang.ref.WeakReference
import kotlin.math.min

class NetworkTexture(director:IDirector, key:String, url:String, width:Int, height:Int, listener: OnTextureAsyncLoadListener?, maxSideLength:Int) : Texture(director, key, true, listener), Response.Listener<ByteArrayInputStream>, Response.ErrorListener {
    private val _url:String = url
    private val _maxSideLength:Int = maxSideLength
    private var _reqWidth:Int = width
    private var _reqHeight:Int = height
    private var _request: NetworkStreamRequest? = null
    private var _cached:Boolean = false
    private var _weakReference:WeakReference<OnTextureAsyncLoadListener?> = WeakReference(listener)
    private var _checkTask:CheckTask? = null

    inner class CheckTask(director: IDirector) : SMAsyncTask<Void, Void, Void>(director) {
        override fun doInBackground(vararg params: Void): Void? {
            if (!isCancelled()) {
                val dir = File(getDirector().getContext().getExternalFilesDir(null), "network_cache")
                if (dir.exists()) {
                    val file = File(dir, KeyGenerateUtil.generate(_url))
                    if (file.exists()) {
                        _cached = true
                    }
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            if (!isCancelled()) {
                if (!_cached) {
                    _request = NetworkStreamRequest(_url, this@NetworkTexture, this@NetworkTexture)
                    getDirector().getRequestQueue().add(_request)
                } else {
                    loadTextureAsync(getDirector())
                }
                _checkTask = null
            }
        }
    }

    init {
        initTextureDimen(director.getContext())
    }

    override fun isLoading(): Boolean {
        return _checkTask!=null || _request!=null || super.isLoading()
    }

    override fun loadTextureAsync(director: IDirector) {
        _checkTask = CheckTask(director)
        _checkTask!!.execute()
    }

    override fun onErrorResponse(error: VolleyError?) {
        _request = null
        setValid(false)
    }

    override fun onResponse(response: ByteArrayInputStream?) {
        Thread(Runnable {
            val dir = File(_director.getContext().getExternalFilesDir(null), "network_cache")
            if (!dir.exists()) {
                dir.mkdir()
            }
            val file = File(dir, generate(_url))
            var IS:InputStream? = null
            var OS:OutputStream? = null
            val data = ByteArray(1024)
            var count = 0
            try {
                IS = BufferedInputStream(response, 8192)
                OS = FileOutputStream(file)
                while (IS.read(data).also { count = it } != -1) {
                    OS.write(data, 0, count)
                }
                OS.flush()
            } catch (e:IOException) {
                // error
            } finally {
                closeSilently(OS)
                closeSilently(IS)
            }
            val bitmap:Bitmap? = getCachedBitmap(_director.getContext(), dir)
            _director.getScheduler().performFunctionInMainThread(object : PERFORM_SEL{
                override fun performSelector() {
                    _cached = true
                    if (bitmap!=null) {
                        val l: OnTextureAsyncLoadListener? = _weakReference.get()
                        if (createOrUpdate(_director, bitmap)) {
                            if (l!=null) {
                                l.onTextureLoaded(this@NetworkTexture)
                                if (_doNotRecycleBitmap) {
                                    l.onTextureLoadedBitmap(bitmap)
                                }
                            }
                        } else {
                            l?.onTextureLoaded(null)
                        }
                    }
                    _request = null
                }
            })
        }).start()
    }

    override fun loadTexture(director: IDirector, bitmapt: Bitmap?): Boolean {
        var bitmap = bitmapt
        if (bitmap==null && !isAsyncLoader()) {
            bitmap = loadTextureBitmap(director.getContext())
        }
        if (bitmap!=null) {
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
            return true
        }
        return false
    }

    override fun initTextureDimen(context: Context) {
        var outWidth = _reqWidth
        var outHeight = _reqHeight

        _originalWidth = _reqWidth
        _originalHeight = _reqHeight

        if (_maxSideLength>0 && (outWidth > _maxSideLength || outHeight > _maxSideLength)) {
            val scale = min(_maxSideLength/outWidth.toFloat(), _maxSideLength/outHeight.toFloat())
            outWidth = (outWidth*scale).toInt()
            outHeight = (outHeight*scale).toInt()
        }

        if (outWidth%4!=0) {
            outWidth += 4-(outWidth%4)
        }

        _width = outWidth
        _height = outHeight
    }

    override fun loadTextureBitmap(context: Context): Bitmap? {
        val dir = File(context.getExternalFilesDir(null), "network_cache")
        if (!dir.exists()) {
            dir.mkdir()
        }

        return getCachedBitmap(context, dir)
    }

    private fun getCachedBitmap(context: Context, dir:File):Bitmap? {
        val file = File(dir, KeyGenerateUtil.generate(_url))
        var bitmap:Bitmap? = null
        if (file.exists()) {
            bitmap = BitmapLoader.loadBitmap(context, file.absolutePath, 0, _width, _height)
            if (bitmap==null) {
                file.delete()
                setValid(false)
            }
        }
        return bitmap
    }

    override fun setDoNotRecycleBitmap(doNotRecyclebitmap: Boolean) {
        if (isAsyncLoader()) {
            _doNotRecycleBitmap = doNotRecyclebitmap
        }
    }

    override fun deleteTexture(isGLThread: Boolean) {
        if (_request!=null) {
            _request!!.cancel()
            _request = null
        }
        super.deleteTexture(isGLThread)
    }

}