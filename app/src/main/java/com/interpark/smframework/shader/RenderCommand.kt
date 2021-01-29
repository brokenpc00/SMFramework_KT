package com.brokenpc.smframework.shader

import android.util.Log
import com.brokenpc.smframework.base.SMView

open class RenderCommand {
    protected var _globalOrder = 0f
    protected var _type = Type.UNKNOWN_COMMAND
    protected var _isTransparent = true
    protected var _skipBatching = false
    protected var _is3D = false
    protected var _depth = 0f

    constructor() {

    }

    enum class Type {
        UNKNOWN_COMMAND, QUAD_COMMAND, CUSTOM_COMMAND, BATCH_COMMAND, GROUP_COMMAND, MESH_COMMAND, PRIMITIVE_COMMAND, TRIANGLES_COMMAND
    }

    open fun init(globalZOrder: Float, modelViewTransform: FloatArray, flags: Long) {
        _globalOrder = globalZOrder
        if (flags and SMView.FLAGS_RENDER_AS_3D > 0) {
            set3D(true)
        } else {
            set3D(false)
            _depth = 0f
        }
    }

    fun getGlobalOrder(): Float {
        return _globalOrder
    }


    fun getType(): Type? {
        return _type
    }


    fun isTransparent(): Boolean {
        return _isTransparent
    }

    fun setTransparent(transparent: Boolean) {
        _isTransparent = transparent
    }


    fun isSkipBatching(): Boolean {
        return _skipBatching
    }

    fun setSkipBatching(skipBatching: Boolean) {
        _skipBatching = skipBatching
    }


    fun is3D(): Boolean {
        return _is3D
    }

    fun set3D(value: Boolean) {
        _is3D = value
    }


    fun getDepth(): Float {
        return _depth
    }

    protected fun printID() {
        Log.i("RenderCommander", "[[[[[ Command Depth : $_globalOrder")
    }

}