package com.brokenpc.smframework.util.ImageManager

import android.graphics.Bitmap
import com.brokenpc.smframework.base.types.PERFORM_SEL
import com.brokenpc.smframework.util.cache.ImageCacheEntry
import com.brokenpc.smframework.util.cache.ImageLRUCache
import com.brokenpc.smframework.util.cache.MemoryCacheEntry
import com.brokenpc.smframework.util.cache.MemoryLRUCache
import java.lang.Exception
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class ImageDownloader {
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
        _memCache = null
        _imageCache = null
        _decodeThreadPool = null
        _downloadThreadPool = null
        _fileCacheWriteThreadPool = null
        init()
    }

    fun init() {
        _memCache = MemoryLRUCache(_memCacheSize)
        _imageCache = ImageLRUCache(_imageCacehSize)
        _decodeThreadPool = ImageThreadPool(_decodePoolSize)
        _downloadThreadPool = ImageThreadPool(_downloadPoolSize)
        _fileCacheWriteThreadPool = ImageThreadPool(1)
    }

    private var _downloadThreadPool:ImageThreadPool? = null
    private var _decodeThreadPool:ImageThreadPool? = null
    private var _fileCacheWriteThreadPool:ImageThreadPool? = null
    private var _decompressThreadPool:ImageThreadPool? = null

    private var _memCache: MemoryLRUCache? = null
    private var _imageCache: ImageLRUCache? = null

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
            _decodeThreadPool?.interrupt()
            _downloadThreadPool?.interrupt()
            _fileCacheWriteThreadPool?.interrupt()

            _memCache?.evictAll()
            _memCache = null

            _imageCache?.evictAll()
            _imageCache = null
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

    fun loadImageFromNetwork(target: IDownloadProtocol, requestUrl: String, tag: Int, config: DownloadConfig?) {
        if (requestUrl.isEmpty()) {
            target.onImageLoadComplete(null, tag, true)
            return
        }

        if (target.isDownloadRunning(requestUrl, tag)) {
            return
        }

        val task = ImageDownloadTask
    }
    fun loadImageFromNetwork(target: IDownloadProtocol, requestUrl: String, tag: Int) {
        loadImageFromNetwork(target, requestUrl, tag, null)
    }
    fun loadImageFromNetwork(target: IDownloadProtocol, requestUrl: String) {
        loadImageFromNetwork(target, requestUrl, 0, null)
    }


    fun loadImageFromResource(target: IDownloadProtocol, requestUrl: String, tag: Int, config: DownloadConfig?) {

    }
    fun loadImageFromResource(target: IDownloadProtocol, requestUrl: String, tag: Int) {

    }
    fun loadImageFromResource(target: IDownloadProtocol, requestUrl: String) {

    }

    fun loadImageFromFile(target: IDownloadProtocol, requestUrl: String, tag: Int, config: DownloadConfig?) {

    }
    fun loadImageFromFile(target: IDownloadProtocol, requestUrl: String, tag: Int) {

    }
    fun loadImageFromFile(target: IDownloadProtocol, requestUrl: String) {

    }

    fun loadImageFromThumbnail(target: IDownloadProtocol, requestUrl: String, tag: Int, config: DownloadConfig?) {

    }
    fun loadImageFromThumbnail(target: IDownloadProtocol, requestUrl: String, tag: Int) {

    }
    fun loadImageFromThumbnail(target: IDownloadProtocol, requestUrl: String) {

    }

    fun cancelImageDownload(target: IDownloadProtocol) {

    }

    fun isCachedForNetwork(requestUrl: String, config: DownloadConfig): Boolean {

    }
    fun isCachedForNetwork(requestUrl: String): Boolean {

    }

    fun isFileCachedForNetwork(requestUrl: String, config: DownloadConfig) {

    }
    fun isFileCachedForNetwork(requestUrl: String) {

    }

    fun getCacheKeyForNetwork(requestUrl: String, config: DownloadConfig?): String {

    }

    fun registCacheData(requestUrl: String, data: ByteArray?, length: Int, memCache: Boolean, diskCache: Boolean): Boolean {

    }

    fun registCacheImage(requestPath: String, bmp: Bitmap): Boolean {

    }

    fun clearCache() {

    }

    fun saveToFileCache(requestPath: String, data: ByteArray?, length: Int, config: DownloadConfig?) {

    }
    fun saveToFileCache(requestUrl: String, data: ByteArray?, length: Int) {

    }

    protected fun getMemCache(): MemoryLRUCache? {return _memCache}
    protected fun getImageCache(): ImageLRUCache? {return _imageCache}

    fun queueDownloadTask(target: IDownloadProtocol, task: ImageDownloadTask) {

    }

    fun addDownloadTask(task: PERFORM_SEL) {

    }

    fun addDecodeTask(task: PERFORM_SEL) {

    }

    fun handleState(task: ImageDownloadTask, state: State) {

    }
    fun handleState(task: ImageDownloadTask, state: State, imageEntry: ImageCacheEntry) {

    }

    fun writeToFileCache(cacheKey: String, cacheEntry: MemoryCacheEntry) {

    }


}