package com.brokenpc.smframework.downloader

import android.util.LruCache
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Ref
import com.brokenpc.smframework.util.AppUtil
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class DownloadManager : Ref {
    companion object {
        const val DOWNLOAD_FAILED = -1
        const val DOWNLOAD_STARTED = 1
        const val DOWNLOAD_COMPLETE = 2
        const val DECODE_STARTED = 3
        const val TASK_COMPLETE = 4
        const val TASK_QUEUED = 5
        const val TASK_NONE = 100

        private val DOWNLOAD_CACHE_SIZE = 1024*1024*20
        private val KEEP_ALIVE_TIME = 1
        private val KEEP_ALIVE_TIME_UNIT:TimeUnit = TimeUnit.SECONDS
        const val CORE_POOL_SIZE = 4
        const val MAXIMUM_POOL_SIZE = 8
        private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()

        private var _instance:DownloadManager? = null

        @JvmStatic
        fun initInstance(director: IDirector): DownloadManager {
            if (_instance==null) {
                _instance = DownloadManager(director)
            }

            return _instance!!
        }

        @JvmStatic
        fun initNewInstance(director: IDirector) {
            _instance = DownloadManager(director)
        }
    }

    var _diskCachePath:String = ""
    private val _imageCache:LruCache<String, ByteArray>
    private val _downloadBlockingQueue:BlockingQueue<Runnable>
    private val _decodeBlockingQueue:BlockingQueue<Runnable>
    private val _downloadTaskWorkQueue:Queue<DownloadTask>

    private val _downloadThreadPool:ThreadPoolExecutor
    private val _decodeThreadPool:ThreadPoolExecutor

    constructor(director:IDirector) : super(director) {

        _downloadBlockingQueue = LinkedBlockingQueue()
        _decodeBlockingQueue = LinkedBlockingQueue()
        _downloadTaskWorkQueue = LinkedBlockingQueue()
        _downloadThreadPool = ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME.toLong(), KEEP_ALIVE_TIME_UNIT, _downloadBlockingQueue)
        _decodeThreadPool = ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME.toLong(), KEEP_ALIVE_TIME_UNIT, _decodeBlockingQueue)

        _imageCache = object : LruCache<String, ByteArray>(DOWNLOAD_CACHE_SIZE) {
            override fun sizeOf(key: String?, value: ByteArray): Int {
                return value.size
            }
        }

        val diskCacheDirectory = AppUtil.getExternalFilesDir(director.getContext(), "network_cache")
        _diskCachePath = diskCacheDirectory.absolutePath
    }




}