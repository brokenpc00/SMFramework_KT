package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.Size

class ResizeTo(director: IDirector) : ActionInterval(director) {
    protected val _initialSize = Size.ZERO
    protected val _finalSize = Size.ZERO
    protected val _sizeDelta = Size.ZERO

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, finalSize: Size): ResizeTo? {
            val ret = ResizeTo(director)
            ret.initWithDuration(duration, finalSize)
            return ret
        }
    }

    protected fun initWithDuration(duration: Float, finalSize: Size): Boolean {
        super.initWithDuration(duration)
        _finalSize.set(finalSize)
        return true
    }

    override fun Clone(): ActionInterval? {
        val a = ResizeTo(getDirector())
        a.initWithDuration(_duration, _finalSize)
        return a
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)

        _initialSize.set(target?.getContentSize()?:Size.ZERO)
        _sizeDelta.set(_finalSize.minus(_initialSize))
    }

    override fun update(dt: Float) {
        val newSize = Size(_initialSize.add(_sizeDelta.multiply(dt)))
        _target?.setContentSize(newSize)
    }
}