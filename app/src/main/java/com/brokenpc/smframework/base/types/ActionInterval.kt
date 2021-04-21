package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import kotlin.math.max
import kotlin.math.min

open class ActionInterval(director:IDirector) : FiniteTimeAction(director) {
    protected var _elapsed:Float = 0f
    protected var _firstTick:Boolean = true
    protected var _done:Boolean = false

    companion object {
        const val EPSILON:Float = 0.0000001f

    }

    open fun initWithDuration(d:Float): Boolean {
        _duration = d
        _elapsed = 0f
        _firstTick = true
        _done = false

        return true
    }

    fun getElapsed():Float {return _elapsed}
    open fun setAmplitudeRate(amp:Float) {}
    open fun getAmplitudeRate():Float {return 0f}

    override fun isDone():Boolean {return _done}
    override fun Clone(): ActionInterval? { return null }
    override fun reverse(): ActionInterval? {return null}

    override fun step(dt: Float) {
        if (_firstTick) {
            _firstTick = false
            _elapsed = 0f
        } else {
            _elapsed += dt
        }

        val updateDt:Float = max(0f, min(1f, _elapsed/_duration))
        this.update(updateDt)

        _done = _elapsed>=_duration
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)

        _elapsed = 0f
        _firstTick = true
        _done = false
    }
}