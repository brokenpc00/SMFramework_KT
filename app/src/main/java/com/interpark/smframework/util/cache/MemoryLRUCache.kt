package com.brokenpc.smframework.util.cache

import android.util.LruCache

class MemoryLRUCache(maxsize: Int) : LruCache<String, MemoryCacheEntry>(maxsize) {
    protected fun sizeOf(key: String, entry: MemoryCacheEntry): Int {
        return entry.size()
    }
}