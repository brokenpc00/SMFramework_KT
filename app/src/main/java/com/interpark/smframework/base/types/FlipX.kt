package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionInstant
import com.brokenpc.smframework.base.types.ActionInterval

class FlipX(director: IDirector) : ActionInstant(director) {
    private var _flipX = false

    companion object {
        @JvmStatic
        fun create(director: IDirector, x: Boolean): FlipX? {
            val action = FlipX(director)
            if (action.initWithFlipX(x)) {
                return action
            }
            return null
        }
    }

    fun initWithFlipX(x: Boolean): Boolean {
        _flipX = x
        return true
    }

    override fun Clone(): ActionInterval? {
        return FlipX.create(getDirector(), _flipX)
    }

//    override fun update(dt: Float) {
//        super.update(dt)
//    }

    override fun reverse(): ActionInstant? {
        return FlipX.create(getDirector(), !_flipX)
    }
}