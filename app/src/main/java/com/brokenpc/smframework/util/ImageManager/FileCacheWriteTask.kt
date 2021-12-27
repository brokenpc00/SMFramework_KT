package com.brokenpc.smframework.util.ImageManager

import com.brokenpc.smframework.util.cache.MemoryCacheEntry
import com.brokenpc.smframework.util.FileManager

class FileCacheWriteTask() {
    private var _cacheEntry:MemoryCacheEntry? = null
    private var _cacheKey:String = ""

    companion object {
        @JvmStatic
        fun createTaskForCache(cacheKey: String, cacheEntry: MemoryCacheEntry?): FileCacheWriteTask {
            val task = FileCacheWriteTask()
            task._cacheKey = cacheKey
            task._cacheEntry = cacheEntry
            return task
        }
    }

    fun procFileCacheWriteThread() {
        FileManager.getInstance().writeToFile(FileManager.FileType.Image, _cacheKey, _cacheEntry?.getData(), _cacheEntry?.size() ?: 0)
    }
}