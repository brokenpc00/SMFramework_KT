package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector

open class ActionInstant(director:IDirector) : ActionInterval(director) {

    override fun Clone(): ActionInstant? {
        return null
    }

    override fun reverse(): ActionInstant? {
        return null
    }

    override fun isDone(): Boolean {
        return _done
    }

    override fun step(dt: Float) {
        val updatedt:Float = 1f
        update(updatedt)
    }

    override fun update(dt: Float) {
        _done = true
    }

}