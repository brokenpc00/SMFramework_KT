package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval

open class FadeTo(director: IDirector) : ActionInterval(director) {
    protected var _toAlpha = 1f
    protected var _fromAlpha = 1f
    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, alpha: Float): FadeTo {
            val fade = FadeTo(director)
            fade.initWithDuration(duration, alpha)
            return fade
        }
    }

    fun initWithDuration(d: Float, a: Float) : Boolean {
        if (super.initWithDuration(d)) {
        _toAlpha = a
            return true
        }
        return false
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        if (target!=null) {
            _fromAlpha = target.getAlpha()
        }
    }

    override fun Clone(): ActionInterval? {
        return FadeTo.create(getDirector(), _duration, _toAlpha)
    }

    override fun update(dt: Float) {
        _target?.setAlpha(_fromAlpha + (_toAlpha-_fromAlpha)*dt)
    }

    override fun reverse(): ActionInterval? {
        return null
    }
}