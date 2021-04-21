package com.brokenpc.smframework.downloader

import android.graphics.Bitmap
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Ref
import com.brokenpc.smframework.view.DownloaderView
import java.lang.ref.WeakReference

class DownloadTask(director:IDirector) : DownloadRunnable.DownloadRunnableTaskMethods, ImageDecodeRunnable.TaskRunnableDecodeMethods, Ref(director) {
    private var _imageWeakRef:WeakReference<DownloaderView>? = null

    private var _mediaType:Int = 0
    private var _filePath:String = ""
    private var _diskCache:String = ""

    private var _targetHeight:Int = 0
    private var _targetWidth:Int = 0
    private var _targetDegrees:Int = 0

    private var _cacheEnabled:Boolean = false

    private var _downloadRunnable:Runnable
    private var _decodeRunnable:Runnable

    var _dataBuffer:ByteArray? = null

    private var _decodedImage:Bitmap? = null
    private var _currentThread:Thread? = null

    lateinit var _downloadManager:DownloadManager

    companion object {

    }

    init {
        _downloadRunnable = DownloadRunnable(director, this)
        _decodeRunnable = ImageDecodeRunnable(this)
    }

    fun initializeDownloaderTask(downloadManager: DownloadManager, imageView: DownloaderView, cacheFlag: Boolean, width: Int, height: Int, degrees:Int) {
        _downloadManager = downloadManager
        _mediaType = imageView.getMediaType()
        _filePath = imageView.getImagePath() ?: ""
        _diskCache = imageView.getDiskCachPath() ?: ""

        _imageWeakRef = WeakReference(imageView)

        _cacheEnabled = cacheFlag

        _targetWidth = width
        _targetHeight = height
        _targetDegrees = degrees
    }

    override fun getByteBuffer(): ByteArray? {
        return _dataBuffer
    }

    fun recycle() {
        _imageWeakRef?.clear()
        _imageWeakRef = null

        if (_decodedImage!=null && !_decodedImage!!.isRecycled) {
            _decodedImage!!.recycle()
        }

        _dataBuffer = null
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

    override fun getStorageMediaType(): Int {
        return _mediaType
    }

    override fun getMediaType(): Int {
        return _mediaType
    }

    override fun getDownloadPath(): String {
        return _filePath
    }

    override fun getDiskCachePath(): String {
        return _diskCache
    }

    override fun setByteBuffer(buffer: ByteArray) {
        _dataBuffer = buffer
    }

    fun handleState(state:Int) {}

    fun getImage():Bitmap? {return _decodedImage}

    fun getHTTPDownloadRunnable():Runnable {return _downloadRunnable}
    fun getPhotoDecodeRunnable():Runnable {return _decodeRunnable}

    fun getImageView():DownloaderView? {return _imageWeakRef!!.get()}

    fun getCurrentThread():Thread {
        synchronized(_currentThread!!) {
            return _currentThread!!
        }
    }

    fun setCurrentThread(thread: Thread?) {
        if (_currentThread!=thread) {
            synchronized(_currentThread!!) {
                _currentThread = thread
            }
        }
    }

    override fun setImage(image: Bitmap?) {
        _decodedImage = image
    }

    override fun setDownloadThread(currentThread: Thread?) {
        setCurrentThread(currentThread)
    }

    override fun handleDownloadState(state: Int) {
        val outState = when (state) {
            DownloadRunnable.HTTP_STATE_COMPLETED -> DownloadManager.DOWNLOAD_STARTED
            DownloadRunnable.HTTP_STATE_FAILED -> DownloadManager.DOWNLOAD_FAILED
            else -> DownloadManager.DOWNLOAD_STARTED
        }
        handleState(outState)
    }

    override fun setImageDecodeThread(currentThread: Thread?) {
        setCurrentThread(currentThread)
    }

    override fun handelDecodeState(state: Int) {
        val outState = when (state) {
            ImageDecodeRunnable.DECODE_STATE_COMPLETED -> DownloadManager.TASK_COMPLETE
            ImageDecodeRunnable.DECODE_STATE_FAILED -> DownloadManager.DOWNLOAD_FAILED
            else -> DownloadManager.DECODE_STARTED
        }
        handleState(outState)
    }
}