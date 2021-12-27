package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.Size

class ResizeBy(director: IDirector): ActionInterval(director) {
    protected val _sizeDelta = Size(Size.ZERO)
    protected val _startSize = Size(Size.ZERO)
    protected val _previousSize = Size(Size.ZERO)

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, deltaSize: Size): ResizeBy? {
            val ret = ResizeBy(director)
            ret.initWithDuration(duration, deltaSize)

            return ret
        }
    }

    protected fun initWithDuration(duration: Float, deltaSize: Size): Boolean {
        super.initWithDuration(duration)
        _sizeDelta.set(deltaSize)
        return true
    }

    override fun Clone(): ActionInterval? {
        val a = ResizeBy(getDirector())
        a.initWithDuration(_duration, _sizeDelta)
        return a
    }

    override fun reverse(): ActionInterval? {
        val newSize = Size(-_sizeDelta.width, -_sizeDelta.height)
        return ResizeBy.create(getDirector(), _duration, newSize)
    }

    override fun update(dt: Float) {
        _target?.setContentSize(_startSize.add(_sizeDelta.multiply(dt)))
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        _previousSize.set(_startSize.set(target?.getContentSize()?: Size(Size.ZERO)))
    }
}