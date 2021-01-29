package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector

open class DelayTime(director:IDirector) : ActionInterval(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, t:Float):DelayTime? {
            val action:DelayTime = DelayTime(director)
            if (action.initWithDuration(t)) {
                return action
            }

            return null
        }
    }

    override fun update(dt: Float) {
        return
    }

    override fun reverse(): DelayTime? {
        return DelayTime.create(getDirector(), _duration)
    }

    override fun Clone(): DelayTime? {
        return DelayTime.create(getDirector(), _duration)
    }
}