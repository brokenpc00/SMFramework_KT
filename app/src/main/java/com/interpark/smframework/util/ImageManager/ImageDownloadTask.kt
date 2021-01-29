package com.brokenpc.smframework.util.ImageManager

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class ImageDownloadTask() {

    private val _mutex:Lock = ReentrantLock(true)
    private val _cond: Condition = _mutex.newCondition()
    private val _isSuccess:Boolean = false
//    private val _netDownloader:Downloader

    enum class MediaType {
        NETWORK,
        RESOURCE,
        FILE,
        THUMB
    }

    var _this: ImageDownloadTask? = null

    companion object {
        private var __task_count__: Int = 0

        @JvmStatic
        fun createTaskForTarget(downloader: ImageDownloader, target: IDownloadProtocol): ImageDownloadTask {
            val task = ImageDownloadTask()
            return task
        }
    }
}