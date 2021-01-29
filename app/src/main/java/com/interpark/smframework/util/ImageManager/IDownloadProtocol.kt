package com.brokenpc.smframework.util.ImageManager

import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.downloader.DownloadTask

interface IDownloadProtocol {
    enum class DownloadStartState {
        DOWNLOAD, MEM_CACHE, IMAGE_CACHE
    }

    fun onImageLoadComplete(sprite: Sprite?, tag: Int, direct: Boolean)
    fun onImageCacheComplete(success: Boolean, tag: Int)

    fun onImageLoadStart(state: DownloadStartState)
    fun onDataLoadComplete(data:ByteArray, size: Int, tag: Int)
    fun onDataLoadStart()

    fun resetDownload()
    fun removeDownloadTask(task: DownloadTask)
    fun isDownloadRunning(requestPath: String, requestTag: Int): Boolean
    fun addDownloadTask(task: DownloadTask)


}