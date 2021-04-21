package com.brokenpc.smframework.util.cache

import java.util.*
import kotlin.math.max

class MemoryCacheEntry() {

    private var _data:ByteArray? = null
    var _size = 0
    var _capacity = 0

    companion object {
        const val INIT_ALLOC_SIZE = 50*1024
        @JvmStatic
        fun createEntry(): MemoryCacheEntry {
            return createEntry(null, 0)
        }

        @JvmStatic
        fun createEntry(data: ByteArray?): MemoryCacheEntry {
            return createEntry(data, data?.size ?: 0)
        }

        @JvmStatic
        fun createEntry(data: ByteArray?, size: Int): MemoryCacheEntry {
            val entry = MemoryCacheEntry()

            if (size>0) {
                entry._data = (data!!).copyOf(size)
            } else {
                entry._data = data
            }

            entry._size = size
            entry._capacity = size

            return entry
        }
    }

    fun getData(): ByteArray? {return _data}
    fun size(): Int {return _size}

    fun appendData(data: ByteArray?, size: Int) {
        if (data== null || size==0) {
            return
        }

        var oldPos = 0
        if (_data==null) {
            val newCapacity = max(_size+size, INIT_ALLOC_SIZE)
            _capacity = newCapacity
            _data = ByteArray(_capacity)
        } else {
            oldPos = _data!!.size-1
            var newCapacity = _capacity
            if (_size+size>newCapacity) {
                newCapacity = max(_size+size, (_capacity*1.65f).toInt())
            }

            if (newCapacity>_capacity) {
                _capacity = newCapacity
                _data = _data!!.copyOfRange(0, _capacity)
            }
        }

        System.arraycopy(data, 0, _data!!, oldPos, data.size)

        _size += size
    }

    fun shrinkToFit() {
        if (_size!=_capacity && _data!=null) {
            if (_size>0) {
                _data = _data!!.copyOfRange(0, _size)
            } else {
                _data = null
            }
            _capacity = _size
        }
    }
}