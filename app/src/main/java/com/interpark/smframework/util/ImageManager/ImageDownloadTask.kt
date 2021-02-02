package com.brokenpc.smframework.util.ImageManager

import android.graphics.Bitmap
import android.util.Log
import com.brokenpc.smframework.downloader.ImageManager
import com.brokenpc.smframework.network.Downloader.DownloadTask
import com.brokenpc.smframework.network.Downloader.Downloader
import com.brokenpc.smframework.util.AppUtil
import com.brokenpc.smframework.util.FileUtils
import com.brokenpc.smframework.util.cache.ImageCacheEntry
import com.brokenpc.smframework.util.cache.MemoryCacheEntry
import com.interpark.smframework.util.FileManager
import com.interpark.smframework.util.ImageManager.PhoneAlbum
import com.interpark.smframework.util.ImageManager.PhonePhoto
import java.io.File
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ImageDownloadTask {

    private val _mutex:Lock = ReentrantLock(true)
    private val _cond: Condition = _mutex.newCondition()
    private var _isSuccess:Boolean = false
    private var _netDownloader:Downloader = null
    private var _running:Boolean = true
    private var _tag:Int = -1
    private var _type:MediaType = MediaType.NETWORK
    private lateinit var _config:DownloadConfig
    private var _cacheEntry:MemoryCacheEntry? = null
    private var _cacheKey:String = ""
    private var _requestPath:String = ""
    private var _keyPath:String = ""
    private var _targetRef:WeakReference<IDownloadProtocol>? = null
    private var _imageEntry:ImageCacheEntry? = null
    private var _downloader:ImageDownloader? = null
    private var _taskId:Int? = null



    enum class MediaType {
        NETWORK,
        RESOURCE,
        FILE,
        THUMB
    }

    var _this: ImageDownloadTask? = null

    companion object {
        private var __task_count__: Int = 0

        private const val DEFAULT_SIDE_LENGTH = 256
        private const val MAX_SIDE_LENGTH = 1280
        private var _phoneAlbum:ArrayList<PhoneAlbum> = ArrayList()

        @JvmStatic
        fun createTaskForTarget(downloader: ImageDownloader, target: IDownloadProtocol): ImageDownloadTask {
            val task = ImageDownloadTask()
            task._targetRef = WeakReference(target)
            task._downloader = downloader
            task._this = task
            return task
        }

        @JvmStatic
        fun makeCacheKey(type:MediaType, requestPath:String, config:DownloadConfig, keyPath: StringBuffer?): String {
            var key:String = ""

            when (type) {
                MediaType.NETWORK -> {
                    var find:Int = requestPath.indexOf("?")
                    if (find==-1) {
                        find = requestPath.length-1
                    }
                    key = requestPath.substring(0, find)
                }
                MediaType.RESOURCE -> {
                    key = "@RES:$requestPath"
                }
                MediaType.FILE -> {
                    key = "@FILE:$requestPath"
                }
                MediaType.THUMB -> {
                    key = "@THUMB:$requestPath"
                }
            }

            var resampleKey = ""
            when (config._resamplePolicy) {
                DownloadConfig.ResamplePolicy.EXACT_FIT -> {
                    resampleKey = "_@EXACT_FIT_${config._resParam1}x${config._resParam2}"
                }
                DownloadConfig.ResamplePolicy.EXACT_CROP -> {
                    resampleKey = "_@EXACT_CROP_${config._resParam1}x${config._resParam2}"
                }
                DownloadConfig.ResamplePolicy.AREA -> {
                    resampleKey = "_@AREA_${config._resParam1}x${config._resParam2}"
                }
                DownloadConfig.ResamplePolicy.LONGSIDE -> {
                    resampleKey = "_@LONGSIDE_${config._resParam1}x${config._resParam2}"
                }
                DownloadConfig.ResamplePolicy.SCALE -> {
                    resampleKey = "_@SCALE_${config._resParam1}"
                }
            }

            key += resampleKey

            keyPath?.append(key)

            return AppUtil.getMD5(key.toByteArray())
        }
    }



    @Throws(Throwable::class)
    open fun finalize() {
        try {
            --__task_count__
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            println("All done!")
        }
    }

    constructor() {
        _taskId = 0x100 + __task_count__++
    }

    constructor(type:MediaType, requestPath: String, config: DownloadConfig?) {
        _type = type
        _requestPath = requestPath

        if (config==null) {
            _config = DownloadConfig()
            _config.setCachePolicy(DownloadConfig.CachePolicy.DEFAULT)
        } else {
            _config = config
        }

        val keyPath = StringBuffer(_keyPath)
        _cacheKey = makeCacheKey(type, requestPath, _config!!, keyPath)
        _keyPath = keyPath.toString()

        when (_config._cachePolicy) {
            DownloadConfig.CachePolicy.DEFAULT -> {
                _config._enableImageCache = true
                _config._enableMemoryCache = true
                _config._enableDiskCache = type==MediaType.NETWORK
            }
            DownloadConfig.CachePolicy.NO_CACHE -> {
                _config._enableImageCache = false
                _config._enableMemoryCache = false
                _config._enableDiskCache = false
            }
            DownloadConfig.CachePolicy.ALL_CACHE -> {
                _config._enableImageCache = true
                _config._enableMemoryCache = true
                _config._enableDiskCache = true
            }
            DownloadConfig.CachePolicy.MEMORY_ONLY -> {
                _config._enableImageCache = false
                _config._enableMemoryCache = true
                _config._enableDiskCache = false
            }
            DownloadConfig.CachePolicy.DISK_ONLY -> {
                _config._enableImageCache = false
                _config._enableMemoryCache = false
                _config._enableDiskCache = true
            }
            DownloadConfig.CachePolicy.NO_IMAGE -> {
                _config._enableImageCache = false
                _config._enableMemoryCache = true
                _config._enableDiskCache = true
            }
            DownloadConfig.CachePolicy.NO_MEMORY -> {
                _config._enableImageCache = true
                _config._enableMemoryCache = false
                _config._enableDiskCache = true
            }
            DownloadConfig.CachePolicy.NO_DISK -> {
                _config._enableImageCache = true
                _config._enableMemoryCache = true
                _config._enableDiskCache = false
            }
        }

        if (_config._enableDiskCache && type!=MediaType.NETWORK) {
            _config._enableDiskCache = false
        }
    }

    fun interrupt() {
        _running = false
    }

    fun isRunning(): Boolean {
        return _running
    }

    fun isTargetAlive(): Boolean {
        return getTarget()!=null
    }

    fun getTag():Int {
        return _tag
    }

    fun setTag(tag:Int) {
        _tag = tag
    }

    fun getMediaType(): MediaType {return _type}

    fun getTarget(): IDownloadProtocol? {
        return _targetRef?.get()
    }

    fun getCacheKey(): String { return _cacheKey}

    fun getRequestPath(): String {return _requestPath}

    fun getKeyPath(): String {return _keyPath}

    fun getMemoryCacheEntry(): MemoryCacheEntry? {return _cacheEntry}

    fun setMemoryCacheEntry(entry: MemoryCacheEntry?) {_cacheEntry = entry}

    fun getImageCacheEntry(): ImageCacheEntry? {return _imageEntry}

    fun setImageCacheEntry(entry: ImageCacheEntry?) {_imageEntry = entry}

    fun setDecodedImage(entry: ImageCacheEntry?) {_imageEntry = entry}

    fun getConfig(): DownloadConfig {return _config}

    fun getDownloader(): ImageDownloader? {return _downloader}

    fun procDownloadThread() {
        try {

            do {
                Thread.sleep(1)
                if (!_running) break

                _cacheEntry = null

                if (_config.isEnableMemoryCache()) {
                    // 메모리를 먼저 검색한다.
                    val cacheEntry = _downloader?.getMemCache().get(_cacheKey)
                    if (cacheEntry!=null && cacheEntry.size()>0) {
                        _cacheEntry = cacheEntry
                        _downloader?.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS)
                        return
                    }
                }


                Thread.sleep(1)
                if (!_running) break

                if (_config.isEnableDisckCache()) {
                    // disk에서 검색 한다.
                    val error = IntArray(1)
                    var data = FileManager.getInstance().loadFromFile(FileManager.FileType.Image, _cacheKey, error)

                    Thread.sleep(1)
                    if (!_running) break

                    if (error[0]==FileManager.SUCCESS && data!!.isNotEmpty()) {
                        _cacheEntry = MemoryCacheEntry.createEntry(data, data.size)
                        data = null

                        if (_config.isEnableMemoryCache()) {
                            _downloader?.getMemCache()?.put(_cacheKey, _cacheEntry)
                        }

                        Thread.sleep(1)
                        if (!_running) break

                        _downloader?.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS)
                        return
                    }
                }

                _isSuccess = false

                _cacheEntry = MemoryCacheEntry.createEntry()

                _netDownloader = Downloader()

                _netDownloader.createDownloadDataTask(_requestPath)

                _netDownloader._onTaskError = object : Downloader.OnTaskError {
                    override fun onTaskError(
                        task: DownloadTask,
                        errorCode: Int,
                        errorCodeInternal: Int,
                        errorStr: String
                    ) {
                        Log.i("Scene", "[[[[[ download error : $errorStr")
                        _isSuccess = false

                        _mutex.withLock {
                            _cond.signal()
                        }
                    }
                }

                _netDownloader._onTaskProgress = object : Downloader.OnTaskProgress {
                    override fun onTaskProgress(
                        task: DownloadTask,
                        byteReceived: Long,
                        totalByteReceived: Long,
                        totalByteExpected: Long
                    ) {
                        Log.i("Scene", "[[[[[ download progress received : $byteReceived, total : $totalByteReceived, expected : $totalByteExpected")
                    }
                }

                _netDownloader._onDataTaskSuccess = object : Downloader.OnDataTaskSuccess {
                    override fun onDataTaskSuccess(task: DownloadTask, data: ByteArray?) {
                        writeDataProc(data, data.size)
                    }
                }

                _mutex.withLock {
                    _cond.signal()
                }

                if (_isSuccess) {
                    _cacheEntry?.shrinkToFit()

                    if (_config.isEnableMemoryCache()) {
                        _downloader?.getMemCache()?.put(_cacheKey, _cacheEntry)
                    }

                    _netDownloader = null

                    _downloader?.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS)

                    if (_config.isEnableDisckCache()) {
                        _downloader?.writeToFileCache(_cacheKey, _cacheEntry)
                    }

                    return
                }

            } while (false)

        } catch (e: InterruptedException) {

        }

        _downloader?.handleState(this, ImageDownloader.State.DOWNLOAD_FAILED)
        _cacheEntry = null
    }

    fun procLoadFromResourceThread() {
        try {

            do {

                Thread.sleep(1)
                if (!_running) break

                _cacheEntry = null

                if (_config.isEnableMemoryCache()) {
                    val cacheEntry = _downloader?.getMemCache()?.get(_cacheKey)
                    if (cacheEntry!=null && cacheEntry.size()>0) {
                        _cacheEntry = cacheEntry
                        _downloader?.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS)
                        return
                    }
                }

                Thread.sleep(1)
                if (!_running) break


                if (_config.isEnableDisckCache()) {
                    val error = IntArray(1)
                    var data = FileManager.getInstance().loadFromFile(FileManager.FileType.Image, _cacheKey, error)

                    Thread.sleep(1)
                    if (!_running) break

                    if (error[0]==FileManager.SUCCESS && data?.size!! > 0) {
                        _cacheEntry = MemoryCacheEntry.createEntry(data, data.size)

                        data = null

                        if (_config.isEnableMemoryCache()) {
                            _downloader?.getMemCache()?.put(_cacheKey, _cacheEntry)
                        }

                        Thread.sleep(1)
                        if (!_running) break

                        _downloader?.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS)
                        return
                    }
                }

                val fs = FileUtils.getInstance()
                val filePath = fs.fullPathForFileName(_requestPath)
                var data = fs.getDataFromFile(filePath)

                if (data==null || data.isEmpty()) {
                    break
                }

                _cacheEntry = MemoryCacheEntry.createEntry(data, data.size)
                data = null

                _downloader?.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS)

                if (_config.isEnableMemoryCache()) {
                    _downloader?.getMemCache()?.put(_cacheKey, _cacheEntry)
                }

                Thread.sleep(1)
                if (!_running) break

                if (_config.isEnableDisckCache()) {
                    _downloader?.writeToFileCache(_cacheKey, _cacheEntry)
                }

                return
            } while (false)

        } catch (e: InterruptedException) {

        }

        _downloader?.handleState(this, ImageDownloader.State.DOWNLOAD_FAILED)
        _cacheEntry = null
    }


    fun procLoadFromFileThread() {
        try {
            do {
                Thread.sleep(1)
                if (!_running) break

                val cacheEntry = _downloader?.getMemCache()?.get(_cacheKey)
                if (cacheEntry!=null && cacheEntry.size()>0) {
                    _cacheEntry = cacheEntry
                    _downloader?.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS)
                    return
                } else {
                    _cacheEntry = null
                }


                _isSuccess = false

                _mutex.lock()

//                if ()


            } while (false)
        } catch (e: InterruptedException) {

        }
    }

    fun writeDataProc(buffer: ByteArray?, size: Int?) {
        _isSuccess = false

        if (_cacheEntry!=null) {
            _cacheEntry!!.appendData(buffer, size?:0)
            _isSuccess = true
        }

        _mutex.withLock {
            _cond.signal()
        }
    }

    fun getPhotoImage(imageUrl: String): Bitmap? {
        if (_phoneAlbum.size==0) return null

        val phoneAlbum = _phoneAlbum[0]

        var phonePhoto: PhonePhoto? = null
        for (photo in phoneAlbum.getAlbumPhotos()) {
            if (photo.getPhotoUri().compareTo(imageUrl)==0) {
                phonePhoto = photo
                break
            }
        }

        if (phonePhoto==null) return null

        val orientation = phonePhoto.getOrientation()
        val sideLength = MAX_SIDE_LENGTH

//        return ImageManager.
    }
}