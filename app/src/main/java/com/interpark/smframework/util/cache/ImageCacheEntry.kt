package com.brokenpc.smframework.util.cache

import android.graphics.Bitmap

class ImageCacheEntry() {
    private var _image:Bitmap? = null

    companion object {
        @JvmStatic
        fun createEntry(bmp: Bitmap): ImageCacheEntry {
            val cacheEntry = ImageCacheEntry()
            cacheEntry._image = bmp

            return cacheEntry
        }

    }

    fun getImage(): Bitmap? {return _image}

    fun size(): Int {
        return _image?.byteCount ?: 0
    }
}