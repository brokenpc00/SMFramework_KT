package com.brokenpc.smframework.util.ImageManager

import com.brokenpc.smframework_kt.BuildConfig

class DownloadConfig {

    constructor() {
        _cachePolicy = CachePolicy.DEFAULT
        _resamplePolicy = ResamplePolicy.NONE
        _resampleMethod = ResampleMethod.LINEAR
        _resampleShrinkOnly = true
        _resParam1 = 0f
        _resParam2 = 0f
        _cacheOnly = false
        _smallThumbnail = false
    }

    constructor(cachePolicy: CachePolicy) {
        _cachePolicy = cachePolicy
        _resamplePolicy = ResamplePolicy.NONE
        _resampleMethod = ResampleMethod.LINEAR
        _resampleShrinkOnly = true
        _resParam1 = 0f
        _resParam2 = 0f
        _cacheOnly = false
        _smallThumbnail = false
    }

    constructor(cachePolicy: CachePolicy, cacheOnly: Boolean) {
        _cachePolicy = cachePolicy
        _resamplePolicy = ResamplePolicy.NONE
        _resampleMethod = ResampleMethod.LINEAR
        _resampleShrinkOnly = true
        _resParam1 = 0f
        _resParam2 = 0f
        _cacheOnly = cacheOnly
        _smallThumbnail = false
    }

    enum class CachePolicy {
        DEFAULT,
        NO_CACHE,
        ALL_CACHE,
        IMAGE_ONLY,
        MEMORY_ONLY,
        DISK_ONLY,
        NO_IMAGE,
        NO_MEMORY,
        NO_DISK
    }

    enum class ResampleMethod {
        NEAREST,
        LINEAR,
        CUBIC,
        LANCZOS
    }

    enum class ResamplePolicy {
        NONE,
        EXACT_FIT,
        EXACT_CROP,
        AREA,
        LONGSIDE,
        SCALE
    }

    var _cachePolicy:CachePolicy = CachePolicy.DEFAULT
    var _resamplePolicy:ResamplePolicy = ResamplePolicy.NONE
    var _resampleMethod:ResampleMethod = ResampleMethod.LINEAR
    var _resParam1:Float = 0f
    var _resParam2:Float = 0f
    var _reqDegrees:Float = 0f

    var _resampleShrinkOnly = false
    var _cacheOnly = false
    var _enableMemoryCache = false
    var _enableImageCache = false
    var _enableDiskCache = false
    var _enablePhysicsBody = false
    var _smallThumbnail = false

    fun isSmallThumbnail():Boolean {return _smallThumbnail}
    fun isCacheOnly():Boolean {return _cacheOnly}
    fun isEnablePhysicsBody():Boolean {return _enablePhysicsBody}
    fun isEnableDisckCache():Boolean {return _enableDiskCache}
    fun isEnableImageCache():Boolean {return _enableImageCache}
    fun isEnableMemoryCache():Boolean {return _enableMemoryCache}

    fun setSmallThumbnail() {_smallThumbnail = true}
    fun setCacheOnly() {_cacheOnly = true}
    fun setRotation(degrees:Int) {_reqDegrees = degrees.toFloat()}
    fun setResampleMethod(resampleMethod:ResampleMethod) {_resampleMethod=resampleMethod}
    fun setResamplePolicy(resamplePolicy: ResamplePolicy, param1: Float, param2: Float) {
        _resamplePolicy = resamplePolicy
        _resParam1 = param1
        _resParam2 = param2

        when (_resamplePolicy) {
            ResamplePolicy.AREA -> {
                if (BuildConfig.DEBUG && (_resParam1<=0 || _resParam2 <= 0)) {
                    error("Param1 and Param2 must be bigger than zero")
                }
            }
            ResamplePolicy.EXACT_FIT -> {
                if (BuildConfig.DEBUG && (_resParam1<=0 || _resParam2 <= 0)) {
                    error("Param1 and Param2 must be bigger than zero")
                }
            }
            ResamplePolicy.EXACT_CROP -> {
                if (BuildConfig.DEBUG && (_resParam1<=0 || _resParam2 <= 0)) {
                    error("Param1 and Param2 must be bigger than zero")
                }
            }
            ResamplePolicy.LONGSIDE -> {
                if (BuildConfig.DEBUG && _resParam1<=0) {
                    error("Param1 and Param2 must be bigger than zero")
                }
            }
            ResamplePolicy.SCALE -> {
                if (BuildConfig.DEBUG && _resParam1<=0) {
                    error("Param1 and Param2 must be bigger than zero")
                    return
                }
                if (_resParam1>=1.0f) {
                    _resamplePolicy = ResamplePolicy.NONE
                }
            }
            else -> {

            }
        }
    }

    fun setResamplePolicy(resamplePolicy: ResamplePolicy, param1: Float) {
        setResamplePolicy(resamplePolicy, param1, 0f)
    }

    fun setResamplePolicy(resamplePolicy: ResamplePolicy) {
        setResamplePolicy(resamplePolicy, 0f, 0f)
    }

    fun setCachePolicy(cachePolicy: CachePolicy) {_cachePolicy = cachePolicy}
}