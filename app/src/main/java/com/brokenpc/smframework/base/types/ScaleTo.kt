package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval

open class ScaleTo(director: IDirector) : ActionInterval(director) {
    protected var _startScaleX = 1f
    protected var _startScaleY = 1f
    protected var _startScaleZ = 1f
    protected var _endScaelX = 1f
    protected var _endScaleY = 1f
    protected var _endScaleZ = 1f
    protected var _deltaX = 1f
    protected var _deltaY = 1f
    protected var _deltaZ = 1f

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, s: Float): ScaleTo {
            val scale = ScaleTo(director)
            scale.initWithDuration(duration, s)
            return scale
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, sx: Float, sy: Float): ScaleTo {
            val scale = ScaleTo(director)
            scale.initWithDuration(duration, sx, sy)
            return scale
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, sx: Float, sy: Float, sz: Float): ScaleTo {
            val scale = ScaleTo(director)
            scale.initWithDuration(duration, sx, sy, sz)
            return scale
        }
    }

    override fun Clone(): ActionInterval? {
        return ScaleTo.create(getDirector(), _duration, _endScaelX, _endScaleY, _endScaleZ)
    }

    override fun reverse(): ActionInterval? {
        return null
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        _startScaleX = target?.getScaleX()?:1f
        _startScaleY = target?.getScaleY()?:1f
        _startScaleZ = target?.getScaleZ()?:1f

        _deltaX = _endScaelX - _startScaleX
        _deltaY = _endScaleY - _startScaleY
        _deltaZ = _endScaleZ - _startScaleZ
    }

    override fun update(dt: Float) {
        _target?.setScaleX(_startScaleX + _deltaX*dt)
        _target?.setScaleY(_startScaleY + _deltaY*dt)
        _target?.setScaleZ(_startScaleZ + _deltaZ*dt)
    }


    protected fun initWithDuration(duration: Float, s: Float): Boolean {
        if (super.initWithDuration(duration)) {
            _endScaelX = s
            _endScaleY = s
            _endScaleY = s
        return true
    }
        return false
    }

    protected fun initWithDuration(duration: Float, sx: Float, sy: Float): Boolean {
        if (super.initWithDuration(duration)) {
        _endScaelX = sx
        _endScaleY = sy
            _endScaleY = 1f
        return true
    }
        return false
    }

    protected fun initWithDuration(duration: Float, sx: Float, sy: Float, sz: Float): Boolean {
        if (super.initWithDuration(duration)) {
        _endScaelX = sx
        _endScaleY = sy
        _endScaleY = sz
        return true
    }
        return false
    }
}