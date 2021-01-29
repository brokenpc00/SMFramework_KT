
package com.brokenpc.smframework.downloader

import android.graphics.Bitmap
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Ref
import com.brokenpc.smframework.view.DownloaderView
import com.brokenpc.smframework.downloader.ImageDecodeRunnable.TaskRunnableDecodeMethods
import com.brokenpc.smframework.downloader.ImageDownloadRunnable.TaskRunnableDownloadMethods
import java.lang.ref.WeakReference

class ImageTask(director:IDirector) : TaskRunnableDownloadMethods, TaskRunnableDecodeMethods {
    private var _imageWeakRef:WeakReference<DownloaderView>? = null
    private var _mediaType = -1
    private var _imagePath:String? = null
    private var _diskCachePath:String? = null

    private var _targetHeight:Int = -1
    private var _targetWidth:Int = -1
    private var _targetDegrees:Int = 0
    private var _cacheEnabled:Boolean = false
    private var _director:IDirector = director

    var _threadThis:Thread? = null

    var _downloadRunnable:Runnable
    var _decodeRunnable:Runnable

    var _imageBuffer:ByteArray? = null
    private var _decodedImage:Bitmap? = null
    private var _currentThread:Thread? = null

    companion object {
        private var _imageManager:ImageManager? = null
    }

    init {
        _downloadRunnable = ImageDownloadRunnable(director, this)
        _decodeRunnable = ImageDecodeRunnable(this)
        _imageManager = ImageManager.getInstance()
    }

    fun setDirector(director: IDirector) {
        _director = director
    }

    fun getDirector():IDirector {return _director}

    fun initializeDownloaderTask(imageManager: ImageManager, imageView: DownloaderView, cacheFlag:Boolean, width:Int, height:Int, degrees:Int) {
        _imageManager = imageManager
        _mediaType = imageView.getMediaType()
        _imagePath = imageView.getImagePath()
        _diskCachePath = imageView.getDiskCachPath()

        _imageWeakRef = WeakReference(imageView)

        _cacheEnabled = cacheFlag

        _targetWidth = width
        _targetHeight = height
        _targetDegrees = degrees
    }

    override fun getByteBuffer(): ByteArray? {
        return _imageBuffer
    }

    fun recycle() {
        _imageWeakRef?.clear()
        _imageWeakRef = null

        if (_decodedImage!=null && !_decodedImage!!.isRecycled) {
            _decodedImage?.recycle()
        }

        _imageBuffer = null
        _decodedImage = null
    }

    override fun getTargetWidth(): Int {
        return _targetWidth
    }

    override fun getTargetHeight(): Int {
        return _targetHeight
    }

    override fun getTargetDegrees(): Int {
        return _targetDegrees
    }

    fun isCacheEnabled():Boolean {return _cacheEnabled}

    override fun getMediaType(): Int {
        return _mediaType
    }

    override fun getImagePath(): String {
        return _imagePath!!
    }

    override fun getDiskCachePath(): String {
        return _diskCachePath!!
    }

    override fun setByteBuffer(buffer: ByteArray) {
        _imageBuffer = buffer
    }

    fun handleState(state:Int) {
        _imageManager!!.handleState(this, state)
    }

    fun getImage():Bitmap? {return _decodedImage}

    fun getHTTPDownloadRunnable():Runnable {return _downloadRunnable}

    fun getImageDecodeRunnable():Runnable {return _decodeRunnable}

    fun getImageView():DownloaderView? {
        return _imageWeakRef?.get()
    }

    fun getCurrentThread():Thread? {return _currentThread}

    fun setCurrentThread(thread: Thread?) {
        synchronized(_imageManager!!) {
            _currentThread = thread
        }
    }

    override fun setImage(image: Bitmap?) {
        _decodedImage = image
    }

    override fun setDownloadThread(currentThread: Thread?) {
        setCurrentThread(currentThread)
    }

    override fun handleDownloadState(state: Int) {
        val outState = when(state) {
            ImageDownloadRunnable.HTTP_STATE_COMPLETED->ImageManager.DOWNLOAD_COMPLETE
            ImageDownloadRunnable.HTTP_STATE_FAILED->ImageManager.DOWNLOAD_FAILED
            else->ImageManager.DOWNLOAD_STARTED
        }
        handleState(outState)
    }

    override fun setImageDecodeThread(currentThread: Thread?) {
        setCurrentThread(currentThread)
    }

    override fun handelDecodeState(state: Int) {
        val outState = when (state) {
            ImageDecodeRunnable.DECODE_STATE_COMPLETED -> ImageManager.TASK_COMPLETE
            ImageDecodeRunnable.DECODE_STATE_FAILED -> ImageManager.DOWNLOAD_FAILED
            else -> ImageManager.DECODE_STARTED
        }
        handleState(outState)
    }
}