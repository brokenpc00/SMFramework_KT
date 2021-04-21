package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import java.time.Duration

open class FiniteTimeAction (director:IDirector, duration: Float=0f): Action(director)
{
    protected var _duration:Float = 0f

    fun getDuration():Float {return _duration}
    fun setDuration(duration: Float) {_duration = duration}

    override fun reverse(): FiniteTimeAction? {
        return null
    }

    override fun Clone(): FiniteTimeAction? {
        return null
    }
}