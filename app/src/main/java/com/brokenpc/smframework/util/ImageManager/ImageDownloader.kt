package com.brokenpc.smframework.util.ImageManager

import android.graphics.Bitmap
import com.brokenpc.smframework.SMDirector
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.types.PERFORM_SEL
import com.brokenpc.smframework.util.cache.ImageCacheEntry
import com.brokenpc.smframework.util.cache.ImageLRUCache
import com.brokenpc.smframework.util.cache.MemoryCacheEntry
import com.brokenpc.smframework.util.cache.MemoryLRUCache
import com.interpark.smframework.util.ImageManager.FileCacheWriteTask
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

open class ImageDownloader {
    companion object {
        val DEFAULT = DownloadConfig(DownloadConfig.CachePolicy.DEFAULT)
        val NO_CACHE = DownloadConfig(DownloadConfig.CachePolicy.NO_CACHE)
        val NO_DISK = DownloadConfig(DownloadConfig.CachePolicy.NO_DISK)
        val NO_IMAGE = DownloadConfig(DownloadConfig.CachePolicy.NO_IMAGE)
        val CACHE_ONLY = DownloadConfig(DownloadConfig.CachePolicy.DEFAULT, true)
        val CACHE_ONLY_NO_IMAGE = DownloadConfig(DownloadConfig.CachePolicy.NO_IMAGE, true)
        val CACHE_ONLY_NO_DISK = DownloadConfig(DownloadConfig.CachePolicy.NO_DISK, true)
        val CACHE_ONLY_DISK_ONLY = DownloadConfig(DownloadConfig.CachePolicy.DISK_ONLY, true)
        val IMAGE_ONLY = DownloadConfig(DownloadConfig.CachePolicy.IMAGE_ONLY, true)

        const val MEM_CACHE_SIZE = (32*1024*1024)
        const val IMAGE_CACHE_SIZE = (4*1080*1920)
        const val CORE_POOL_SIZE = 8
        const val MAXIMUM_POOL_SIZE = 8

        const val DEFAULT_POOL_SIZE = 4

        private var _imageDownloader:ImageDownloader? = null

        @JvmStatic
        fun getInstance():ImageDownloader {
            if (_imageDownloader==null) {
                _imageDownloader = ImageDownloader(MEM_CACHE_SIZE, MEM_CACHE_SIZE, 4, 4)
            }

            return _imageDownloader!!
        }
    }

    constructor(memCacheSize: Int, imageCacheSize: Int, downloadPoolSize: Int, decodePoolSize: Int) {
        _memCacheSize = memCacheSize
        _imageCacehSize = imageCacheSize
        _downloadPoolSize = downloadPoolSize
        _decodePoolSize = decodePoolSize
//        _memCache = null
//        _imageCache = null
//        _decodeThreadPool = null
//        _downloadThreadPool = null
//        _fileCacheWriteThreadPool = null
        init()
    }

    fun init() {
        _memCache = MemoryLRUCache(_memCacheSize)
        _imageCache = ImageLRUCache(_imageCacehSize)
        _decodeThreadPool = ImageThreadPool(_decodePoolSize)
        _downloadThreadPool = ImageThreadPool(_downloadPoolSize)
        _fileCacheWriteThreadPool = ImageThreadPool(1)
    }

    private lateinit var _downloadThreadPool:ImageThreadPool
    private lateinit var _decodeThreadPool:ImageThreadPool
    private lateinit var _fileCacheWriteThreadPool:ImageThreadPool

    private lateinit var _memCache: MemoryLRUCache
    private lateinit var _imageCache: ImageLRUCache

    private val _mutex_download: Lock = ReentrantLock(true)
    private val _mutex_decode: Lock = ReentrantLock(true)
    private val _mutex_file: Lock = ReentrantLock(true)
    private val _mutex_physics: Lock = ReentrantLock(true)

    private var _memCacheSize: Int = 0
    private var _imageCacehSize: Int = 0
    private var _downloadPoolSize: Int = 0
    private var _decodePoolSize: Int = 0

    protected fun finalize() {
        try {
            _decodeThreadPool.interrupt()
            _downloadThreadPool.interrupt()
            _fileCacheWriteThreadPool.interrupt()

            _memCache.evictAll()
            _imageCache.evictAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    enum class State {
        DOWNLOAD_STARTED,
        DOWNLOAD_SUCCESS,
        DOWNLOAD_FAILED,

        DECODE_STARTED,
        DECODE_SUCCESS,
        DECODE_FAILED,

        IMAGE_CACHE_DIRECT
    }

    fun loadImageFromNetwork(target: IDownloadProtocol, requestUrl: String) {
        loadImageFromNetwork(target, requestUrl, 0, null)
    }
    fun loadImageFromNetwork(target: IDownloadProtocol, requestUrl: String, tag: Int) {
        loadImageFromNetwork(target, requestUrl, tag, null)
    }
    fun loadImageFromNetwork(
        target: IDownloadProtocol,
        requestUrl: String,
        tag: Int,
        config: DownloadConfig?
    ) {
        if (requestUrl.isEmpty()) {
            target.onImageLoadComplete(null, tag, true)
            return
        }

        if (target.isDownloadRunning(requestUrl, tag)) {
            return
        }

        val task = ImageDownloadTask.createTaskForTarget(this, target)
        task.init(ImageDownloadTask.MediaType.NETWORK, requestUrl, config)
        task.setTag(tag)

        if (target.addDownloadTask(task)) {
            queueDownloadTask(target, task)
        }
    }


    fun loadImageFromResource(target: IDownloadProtocol, requestUrl: String) {
        loadImageFromResource(target, requestUrl, 0, null)
    }
    fun loadImageFromResource(target: IDownloadProtocol, requestUrl: String, tag: Int) {
        loadImageFromResource(target, requestUrl, tag, null)
    }
    fun loadImageFromResource(
        target: IDownloadProtocol,
        requestUrl: String,
        tag: Int,
        config: DownloadConfig?
    ) {
        if (requestUrl.isEmpty()) {
            target.onImageLoadComplete(null, tag, true)
            return
        }

        if (target.isDownloadRunning(requestUrl, tag)) {
            return
        }

        val task = ImageDownloadTask.createTaskForTarget(this, target)
        task.init(ImageDownloadTask.MediaType.RESOURCE, requestUrl, config)
        task.setTag(tag)

        if (target.addDownloadTask(task)) {
            queueDownloadTask(target, task)
        }
    }

    fun loadImageFromFile(target: IDownloadProtocol, requestUrl: String) {
        loadImageFromFile(target, requestUrl, 0, null)
    }
    fun loadImageFromFile(target: IDownloadProtocol, requestUrl: String, tag: Int) {
        loadImageFromFile(target, requestUrl, tag, null)
    }
    fun loadImageFromFile(
        target: IDownloadProtocol,
        requestUrl: String,
        tag: Int,
        config: DownloadConfig?
    ) {
        if (requestUrl.isEmpty()) {
            target.onImageLoadComplete(null, tag, true)
            return
        }

        if (target.isDownloadRunning(requestUrl, tag)) {
            return
        }

        val task = ImageDownloadTask.createTaskForTarget(this, target)
        task.init(ImageDownloadTask.MediaType.FILE, requestUrl, config)
        task.setTag(tag)

        if (target.addDownloadTask(task)) {
            queueDownloadTask(target, task)
        }
    }

    fun loadImageFromThumbnail(target: IDownloadProtocol, requestUrl: String) {
        loadImageFromThumbnail(target, requestUrl, 0, null)
    }
    fun loadImageFromThumbnail(target: IDownloadProtocol, requestUrl: String, tag: Int) {
        loadImageFromThumbnail(target, requestUrl, tag, null)
    }
    fun loadImageFromThumbnail(
        target: IDownloadProtocol,
        requestUrl: String,
        tag: Int,
        config: DownloadConfig?
    ) {
        if (requestUrl.isEmpty()) {
            target.onImageLoadComplete(null, tag, true)
            return
        }

        if (target.isDownloadRunning(requestUrl, tag)) {
            return
        }

        val task = ImageDownloadTask.createTaskForTarget(this, target)
        task.init(ImageDownloadTask.MediaType.THUMB, requestUrl, config)
        task.setTag(tag)

        if (target.addDownloadTask(task)) {
            queueDownloadTask(target, task)
        }
    }

    fun cancelImageDownload(target: IDownloadProtocol?) {
        target?.resetDownload()
    }

    fun isCachedForNetwork(requestUrl: String): Boolean {
        return isCachedForNetwork(requestUrl, null)
    }
    fun isCachedForNetwork(requestUrl: String, config: DownloadConfig?): Boolean {
        val cacheKey = getCacheKeyForNetwork(requestUrl, config)
        val imageEntry = _imageCache?.get(cacheKey)

        return imageEntry!=null
    }

    fun isFileCachedForNetwork(requestUrl: String): Boolean {
        return isFileCachedForNetwork(requestUrl, null)
    }
    fun isFileCachedForNetwork(requestUrl: String, config: DownloadConfig?): Boolean {
        val cacheKey = getCacheKeyForNetwork(requestUrl, config)
        val imageEntry = _imageCache?.get(cacheKey)

        return imageEntry!=null
    }

    fun getCacheKeyForNetwork(requestUrl: String, config: DownloadConfig?): String {
        return ImageDownloadTask.makeCacheKey(
            ImageDownloadTask.MediaType.NETWORK,
            requestUrl,
            config,
            null
        )
    }

    fun registCacheData(
        requestUrl: String,
        data: ByteArray?,
        length: Int,
        memCache: Boolean,
        diskCache: Boolean
    ): Boolean {
        if (requestUrl.isEmpty() || data==null || length==0) return false

        if (!memCache && !diskCache) return false

        val task = ImageDownloadTask.createTaskForTarget(this, null)
        task.init(ImageDownloadTask.MediaType.NETWORK, requestUrl, DEFAULT)

        val cacheEntry = MemoryCacheEntry.createEntry()
        cacheEntry.appendData(data, length)

        if (memCache) {
            _memCache.put(task.getCacheKey(), cacheEntry)
        }

        if (diskCache) {
            writeToFileCache(task.getCacheKey(), cacheEntry)
        }

        return true
    }

    fun registCacheImage(requestPath: String, bmp: Bitmap?): Boolean {
        if (requestPath.isEmpty() || bmp==null) return false

        val task = ImageDownloadTask.createTaskForTarget(this, null)
        task.init(ImageDownloadTask.MediaType.NETWORK, requestPath, DEFAULT)

        val imageEntry = ImageCacheEntry.createEntry(bmp)
        _imageCache.put(task.getCacheKey(), imageEntry)

        return true
    }

    fun clearCache() {
        _imageCache?.evictAll()
        _memCache?.evictAll()
    }

    fun saveToFileCache(requestUrl: String, data: ByteArray?, length: Int): Boolean {
        return saveToFileCache(requestUrl, data, length, null)
    }
    fun saveToFileCache(requestPath: String, data: ByteArray?, length: Int, config: DownloadConfig?): Boolean {

        // not used.
        return true
    }

    fun getMemCache(): MemoryLRUCache? {return _memCache}
    fun getImageCache(): ImageLRUCache? {return _imageCache}

    fun queueDownloadTask(target: IDownloadProtocol, task: ImageDownloadTask) {
        if (task.getConfig().isEnableImageCache()) {
            val key = task.getCacheKey()
            val imageEntry = _imageCache?.get(key)

            if (imageEntry!=null) {
                val image = imageEntry.getImage()
                if (image!=null) {
                    target.onImageLoadStart(IDownloadProtocol.DownloadStartState.IMAGE_CACHE)
                    handleState(task, State.IMAGE_CACHE_DIRECT, imageEntry)
                    return
                } else {
                    _imageCache?.remove(key)
                }
            }
        }

        if (task.getConfig().isEnableMemoryCache()) {
            val memoryEntry = _memCache?.get(task.getCacheKey())

            if (memoryEntry!=null && memoryEntry.size()>0) {
                task.setMemoryCacheEntry(memoryEntry)
            }
        }

        if (task.getMemoryCacheEntry()!=null) {
            target.onImageLoadStart(IDownloadProtocol.DownloadStartState.MEM_CACHE)
            handleState(task, State.DECODE_STARTED)
        } else {
            target.onImageLoadStart(IDownloadProtocol.DownloadStartState.DOWNLOAD)
            handleState(task, State.DOWNLOAD_STARTED)
        }
    }

    fun addDownloadTask(task: PERFORM_SEL) {
        synchronized(_downloadThreadPool) {
            _downloadThreadPool.addTask(task)
        }
    }

    fun addDecodeTask(task: PERFORM_SEL) {
        synchronized(_decodeThreadPool) {
            _decodeThreadPool.addTask(task)
        }
    }

    fun handleState(task: ImageDownloadTask, state: State) {
        handleState(task, state, null)
    }
    fun handleState(task: ImageDownloadTask, state: State, imageEntry: ImageCacheEntry?) {
        when (state) {
            State.DOWNLOAD_STARTED -> {
                _mutex_download.lock()
                try {
                    when (task.getMediaType()) {
                        ImageDownloadTask.MediaType.NETWORK -> {
                            addDecodeTask(object : PERFORM_SEL {
                                override fun performSelector() {
                                    task.procDownloadThread()
                                }
                            })
                        }
                        ImageDownloadTask.MediaType.RESOURCE -> {
                            addDecodeTask(object : PERFORM_SEL {
                                override fun performSelector() {
                                    task.procLoadFromResourceThread()
                                }
                            })
                        }
                        ImageDownloadTask.MediaType.FILE -> {
                            addDecodeTask(object : PERFORM_SEL {
                                override fun performSelector() {
                                    task.procLoadFromFileThread()
                                }
                            })
                        }
                        ImageDownloadTask.MediaType.THUMB -> {
                            addDecodeTask(object : PERFORM_SEL {
                                override fun performSelector() {
                                    task.procLoadFromThumbnailThread()
                                }
                            })
                        }
                    }
                } catch (e: Exception) {

                } finally {
                    _mutex_download.unlock()
                }
            }
            State.DOWNLOAD_SUCCESS -> {
                _mutex_download.lock()
                try {
                    if (task.getConfig().isCacheOnly() && !task.getConfig().isEnableImageCache()) {
                        if (task.isTargetAlive()) {
                            SMDirector.getDirector().getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
                                override fun performSelector() {
                                    task.getTarget()?.onImageCacheComplete(true, task.getTag())
                                    task.getTarget()?.removeDownloadTask(task)
                                }
                            })
                        }
                    } else {
                        if (task.isTargetAlive()) {
                            handleState(task, State.DECODE_STARTED)
                        }
                    }
                } catch (e: Exception) {

                } finally {
                    _mutex_download.unlock()
                }
            }
            State.DOWNLOAD_FAILED -> {
                _mutex_download.lock()
                try {
                    val entry = task.getImageCacheEntry()
                    if (entry!=null) {
                        task.setImageCacheEntry(entry)
                    }

                    if (task.isTargetAlive()) {
                        SMDirector.getDirector().getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
                            override fun performSelector() {
                                if (task.isTargetAlive()) {
                                if (task.getConfig().isCacheOnly()) {
                                    task.getTarget()?.onImageCacheComplete(true, task.getTag())
                                } else {
                                    task.getTarget()?.onImageLoadComplete(null, task.getTag(), false)
                                }

                                task.getTarget()?.removeDownloadTask(task)
                            }
                            }
                        })
                    }
                } catch (e: Exception) {

                } finally {
                    _mutex_download.unlock()
                }
            }
            State.DECODE_STARTED -> {
                _mutex_decode.lock()
                try {
                    if (task.isTargetAlive()) {
                        addDecodeTask(object : PERFORM_SEL {
                            override fun performSelector() {
                                task.procDecodeThread()
                            }
                        })
                    }
                } catch (e: Exception) {

                } finally {
                    _mutex_decode.unlock()
                }
            }
            State.DECODE_SUCCESS -> {
                _mutex_decode.lock()
                try {
                    if (task.isTargetAlive()) {
                        SMDirector.getDirector().getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
                            override fun performSelector() {
                                val entry = task.getImageCacheEntry()
                                if (task.getConfig().isEnableImageCache()) {
                                    val lruCache = task.getDownloader()?.getImageCache()
                                    lruCache?.put(task.getCacheKey(), entry)
                                }

                                if (task.isTargetAlive()) {
                                    if (task.getConfig().isCacheOnly()) {
                                        task.getTarget()?.onImageCacheComplete(true, task.getTag())
                                    } else {
                                        val bmp = entry?.getImage()
                                        val sprite = BitmapSprite.createFromBitmap(SMDirector.getDirector(), task.getCacheKey(), bmp)

                                        task.getTarget()?.onImageLoadComplete(sprite, task.getTag(), false)
                                    }
                                    task.getTarget()?.removeDownloadTask(task)
                                }
                                task.setImageCacheEntry(null)
                            }
                        })
                    } else {
                        val entry = task.getImageCacheEntry()
                        if (task.getConfig().isEnableImageCache()) {
                            val lruCache = task.getDownloader()?.getImageCache()
                            lruCache?.put(task.getCacheKey(), entry)
                        }

                        task.setImageCacheEntry(null)
                    }
                } catch (e: Exception) {

                } finally {
                    _mutex_decode.unlock()
                }
            }
            State.DECODE_FAILED -> {
                _mutex_decode.lock()
                try {
                    if (task.isTargetAlive()) {
                        SMDirector.getDirector().getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
                            override fun performSelector() {
                                if (task.isTargetAlive()) {
                                    task.getTarget()?.onImageCacheComplete(false, task.getTag())
                                } else {
                                    task.getTarget()?.onImageLoadComplete(null, task.getTag(), false)
                                }
                                task.getTarget()?.removeDownloadTask(task)
                            }
                        })
                    }
                } catch (e: Exception) {

                } finally {
                    _mutex_decode.unlock()
                }
            }
            State.IMAGE_CACHE_DIRECT -> {
                if (task.getConfig().isCacheOnly()) {
                    task.getTarget()?.onImageCacheComplete(true, task.getTag())
                } else {
                    if (task.isTargetAlive()) {
                        val bmp = imageEntry?.getImage()
                        val sprite = BitmapSprite.createFromBitmap(SMDirector.getDirector(), task.getCacheKey(), bmp)
                        task.getTarget()?.onImageLoadComplete(sprite, task.getTag(), false)
                    }
                }
                task.getTarget()?.removeDownloadTask(task)
            }
        }
    }

    fun writeToFileCache(cacheKey: String, cacheEntry: MemoryCacheEntry?) {
        _mutex_file.lock()
        try {
            val task = FileCacheWriteTask.createTaskForCache(cacheKey, cacheEntry)
            synchronized(_fileCacheWriteThreadPool) {
                _fileCacheWriteThreadPool.addTask(object : PERFORM_SEL {
                    override fun performSelector() {
                        task.procFileCacheWriteThread()
                    }
                })
            }
        } catch (e: Exception) {

        } finally {
            _mutex_file.unlock()
        }
    }

}