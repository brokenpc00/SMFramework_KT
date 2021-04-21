package com.brokenpc.smframework.downloader

import android.util.LruCache
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.SMDirector
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.texture.BitmapTexture
import com.brokenpc.smframework.base.texture.FileTexture
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.types.PERFORM_SEL
import com.brokenpc.smframework.base.types.Ref
import com.brokenpc.smframework.util.AppUtil
import com.brokenpc.smframework.util.KeyGenerateUtil
import com.brokenpc.smframework.view.DownloaderView
import java.io.File
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.max

class ImageManager(director: IDirector) : Ref(director) {
    val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS
    val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
    var _diskCachePath:String = ""
    var _imageCache:LruCache<String, ByteArray>
    var _downloadWorkQueue:BlockingQueue<Runnable>
    var _decodeWorkQueue:BlockingQueue<Runnable>
    var _imageTaskWorkQueue:Queue<ImageTask>
    var _downloadThreadPool:ThreadPoolExecutor
    var _decodeThreadPool:ThreadPoolExecutor


    companion object {
        private var _instance:ImageManager? = null

        const val DOWNLOAD_FAILED = -1
        const val DOWNLOAD_STARTED = 1
        const val DOWNLOAD_COMPLETE = 2
        const val DECODE_STARTED = 3
        const val TASK_COMPLETE = 4
        const val TASK_QUEUED = 5
        const val TASK_NONE = 100
        const val IMAGE_CACHE_SIZE = 1024 * 1024 * 10
        const val KEEP_ALIVE_TIME = 1
        const val CORE_POOL_SIZE = 8
        const val MAXIMUM_POOL_SIZE = 8

        @JvmStatic
        fun getInstance():ImageManager? {
            return _instance
        }

        @JvmStatic
        fun initInstance(director:IDirector) {
            if (_instance==null) {
                _instance = ImageManager(director)
            }
        }

        @JvmStatic
        fun startDownload(director: IDirector, imageView:DownloaderView, cacheFlag:Boolean, width:Int, height:Int, degrees:Int):ImageTask? {
            var downloaderTask: ImageTask? = _instance!!._imageTaskWorkQueue.poll()
            if (downloaderTask==null) {
                downloaderTask = ImageTask(director)
            }

            downloaderTask.setDirector(director)

            downloaderTask.initializeDownloaderTask(_instance!!, imageView, cacheFlag, width, height, degrees)

            var texture = getExistTexture(downloaderTask)
            if (texture!=null) {
                val sprite = Sprite(director, texture, texture.getWidth()/2.0f, texture.getHeight()/2.0f)
                imageView.setSprite(sprite)
                _instance!!.recycleTask(downloaderTask)
                return null
            } else {
                downloaderTask.setByteBuffer(_instance!!._imageCache.get(downloaderTask.getImagePath()))

                if (null==downloaderTask.getByteBuffer()) {
                    _instance!!._downloadThreadPool.execute(downloaderTask.getHTTPDownloadRunnable())
                    imageView.setStatus(TASK_QUEUED)
                } else {
                    _instance!!.handleState(downloaderTask, DOWNLOAD_COMPLETE)
                }
            }

            return downloaderTask
        }

        @JvmStatic
        fun removeDownload(downloaderTask:ImageTask?, imagePath:String?) {
            if (downloaderTask!=null && downloaderTask.getImagePath()==imagePath) {
                synchronized(_instance!!) {
                    downloaderTask.getCurrentThread()?.interrupt()
                }

                _instance!!._downloadThreadPool.remove(downloaderTask.getHTTPDownloadRunnable())
            }
        }

        @JvmStatic
        fun getExistTexture(downloadTask: ImageTask): Texture? {
            return when (downloadTask.getMediaType()) {
                Constants.MEDIA_ASSETS -> downloadTask.getDirector().getTextureManager().getTextureFromAssets(downloadTask.getImagePath())
                Constants.MEDIA_SDCARD -> downloadTask.getDirector().getTextureManager().getTextureFromFile(downloadTask.getImagePath())
                else -> downloadTask.getDirector().getTextureManager().getTextureFromNetwork(downloadTask.getImagePath())
            }
        }

        @JvmStatic
        fun createTexture(downloadTask:ImageTask): Texture? {
            val textureManager = downloadTask.getDirector().getTextureManager()
            var texture = when (downloadTask.getMediaType()) {
                Constants.MEDIA_ASSETS -> textureManager.getTextureFromAssets(downloadTask.getImagePath())
                Constants.MEDIA_SDCARD -> textureManager.getTextureFromFile(downloadTask.getImagePath())
                else -> textureManager.getTextureFromNetwork(downloadTask.getImagePath())
            }
            if (texture!=null) { return texture }

            val bitmap = downloadTask.getImage()
            if (bitmap==null || bitmap.isRecycled) { return null }

            val tempKey = "DOWNLOADER_TEMP"
            val fakeTexture = textureManager.createTextureFromBitmap(bitmap, tempKey) as BitmapTexture
            fakeTexture.updateTexture(bitmap)
            downloadTask.setImage(null)
            bitmap.recycle()

            when(downloadTask.getMediaType()) {
                Constants.MEDIA_ASSETS -> texture = textureManager.createFakeAssetsTexture(downloadTask.getImagePath(), false, null, fakeTexture)
                Constants.MEDIA_SDCARD -> texture = textureManager.createFakeFileTexture(downloadTask.getImagePath(), false, null,
                    downloadTask.getTargetDegrees(),
                    max(downloadTask.getTargetWidth(), downloadTask.getTargetHeight()),
                    fakeTexture)
                else -> {
                    texture = textureManager.createFakeFileTexture(downloadTask.getDiskCachePath(), false, null, 0,
                        max(downloadTask.getTargetWidth(), downloadTask.getTargetHeight()),
                        fakeTexture)
                    (texture as FileTexture).setWebPFormat()
                }
            }
            textureManager.removeFakeTexture(fakeTexture)

            return texture
        }
    }

    init {
        _downloadWorkQueue = LinkedBlockingQueue()
        _decodeWorkQueue = LinkedBlockingQueue()
        _imageTaskWorkQueue = LinkedBlockingQueue()

        _downloadThreadPool = ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME.toLong(), KEEP_ALIVE_TIME_UNIT, _downloadWorkQueue)
        _decodeThreadPool = ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME.toLong(), KEEP_ALIVE_TIME_UNIT, _decodeWorkQueue)

        _imageCache = object : LruCache<String, ByteArray>(IMAGE_CACHE_SIZE) {
            override fun sizeOf(key: String?, value: ByteArray?): Int {
                return value?.size ?: 0
            }
        }

        val diskCachDirectory:File = AppUtil.getExternalFilesDir(director.getContext(), "network_cache")
        _diskCachePath = diskCachDirectory.absolutePath
    }

    fun handleMessage(message:Int, imageTask:ImageTask) {
        SMDirector.getDirector().getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
            override fun performSelector() {
                val localView:DownloaderView? = imageTask.getImageView()
                if (localView!=null) {
                    var localPath = ""

                    if (imageTask.getImagePath()==localPath) {
                        when (message) {
                            DOWNLOAD_STARTED -> localView.setStatus(DOWNLOAD_STARTED)
                            DOWNLOAD_COMPLETE -> localView.setStatus(DOWNLOAD_COMPLETE)
                            DECODE_STARTED -> localView.setStatus(DECODE_STARTED)
                            TASK_COMPLETE -> {
                                localView.setStatus(TASK_COMPLETE)

                                var texture = getExistTexture(imageTask)
                                if (texture==null) {
                                    texture = createTexture(imageTask)
                                }

                                if (texture!=null) {
                                    val sprite = Sprite(imageTask.getDirector(), texture, texture.getWidth()/2.0f, texture.getHeight()/2.0f)
                                    localView.setSprite(sprite)
                                }

                                recycleTask(imageTask)
                            }
                            DOWNLOAD_FAILED -> {
                                localView.setStatus(DOWNLOAD_FAILED)
                                recycleTask(imageTask)
                            }
                        }
                    }
                }
            }
        })
    }

    fun recycleTask(downloadTask: ImageTask) {
        downloadTask.recycle()
        _imageTaskWorkQueue.offer(downloadTask)
    }

    fun getCachePath(inputPath:String):String {
        return _instance!!._diskCachePath + File.separator + KeyGenerateUtil.generate(inputPath)
    }

    fun handleState(imageTask:ImageTask, state:Int) {
        when (state) {
            TASK_COMPLETE -> {
                if (imageTask.isCacheEnabled()) {
                    _imageCache.put(imageTask.getImagePath(), imageTask.getByteBuffer())
                }

                handleMessage(state, imageTask)
            }
            DOWNLOAD_COMPLETE -> _decodeThreadPool.execute(imageTask.getImageDecodeRunnable())
            else -> handleMessage(state, imageTask)
        }
    }

}