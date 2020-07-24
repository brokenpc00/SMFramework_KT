package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector

open class DelayBaseAction(director:IDirector) : ActionInterval(director) {
    protected var _delay:Float = 0f
    private var _started:Boolean = false
    private var _ended:Boolean = true
    private var _reverse:Boolean = false

    open fun onStart() {}
    open fun onEnd() {}
    open fun onUpdate(t:Float) {}
    open fun setReverse() { _reverse = true}

    override fun update(dt: Float) {
        val time:Float = dt*getDuration()

        if (time<_delay) return

        if (!_started) {
            _started = true
            _ended = false
            onStart()
        }

        var tt:Float = (time-_delay) / _duration
        if (_reverse) tt = 1f-tt

        onUpdate(tt)

        if (dt>=1f && !_ended) {
            onEnd()
            _ended = true
        }
    }

    open fun setTimeValue(duration:Float, delay:Float) {
        _duration = duration
        _delay = delay
        setDuration(_duration+_delay)
        _started = false
    }

}