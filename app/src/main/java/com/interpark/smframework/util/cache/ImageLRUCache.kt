package com.brokenpc.smframework.util.cache

import android.content.Context
import android.util.DisplayMetrics
import android.util.LruCache

class ImageLRUCache : LruCache<String, ImageCacheEntry> {
    constructor(maxSize: Int) : super(maxSize)
    constructor(ctx: Context) : super(getCacheSize(ctx))

    protected override fun sizeOf(key: String, entry: ImageCacheEntry): Int {
        return entry.size()
    }

    companion object {
        @JvmStatic
        fun getCacheSize(ctx: Context): Int {
            val displayMetrics: DisplayMetrics = ctx.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // 4byte per pixel
            val screenBytes: Int = screenWidth * screenHeight * 4

            return screenBytes * 2
        }
    }
}