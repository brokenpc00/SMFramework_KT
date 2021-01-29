package com.brokenpc.smframework.base

import android.graphics.Bitmap
import java.util.*
import kotlin.collections.HashMap

class SceneParams {
    private val _params:HashMap<String, Any> = HashMap()
    private var _popStackCount:Int = 1


    companion object {
        @JvmStatic
        fun create(): SceneParams {
            return SceneParams()
        }
    }


    fun setPopStackCount(stackCount:Int) {
        _popStackCount = stackCount
        if (_popStackCount<1) {
            _popStackCount = 1
        }
    }

    fun hasParam(key:String):Boolean {
        return _params.containsKey(key)
    }

    fun getPopStackCount():Int {return _popStackCount}


    // put
    private fun putObject(key: String, value: Any) {_params[key] = value}
    fun putString(key: String, value: String) {putObject(key, value)}
    fun putBitmap(key: String, value: Bitmap) {putObject(key, value)}
    fun putBoolean(key: String, value: Boolean) {putObject(key, value)}
    fun putInt(key: String, value: Int) {putObject(key, value)}
    fun putLong(key: String, value: Long) {putObject(key, value)}
    fun putFloat(key: String, value: Float) {putObject(key, value)}
    fun putDouble(key: String, value: Double) {putObject(key, value)}

    // get
    private fun getObject(key: String):Any? {return _params[key]}
    fun getString(key: String): String? {
        val obj:Any? = getObject(key)
        if (obj!=null && obj is String) {
            return obj
        }
        return null
    }
    fun getBitmap(key: String): Bitmap? {
        val obj:Any? = getObject(key)
        if (obj!=null && obj is Bitmap) {
            return obj
        }
        return null
    }
    fun getBoolean(key: String): Boolean? {
        val obj:Any? = getObject(key)
        if (obj!=null && obj is Boolean) {
            return obj
        }
        return false
    }
    fun getInt(key: String): Int? {
        val obj:Any? = getObject(key)
        if (obj!=null && obj is Int) {
            return obj
        }
        return Int.MIN_VALUE
    }
    fun getLong(key: String): Long? {
        val obj:Any? = getObject(key)
        if (obj!=null && obj is Long) {
            return obj
        }
        return Long.MIN_VALUE
    }
    fun getFloat(key: String): Float? {
        val obj:Any? = getObject(key)
        if (obj!=null && obj is Float) {
            return obj
        }
        return Float.MIN_VALUE
    }
    fun getDouble(key: String): Double? {
        val obj:Any? = getObject(key)
        if (obj!=null && obj is Double) {
            return obj
        }
        return Double.MIN_VALUE
    }
}